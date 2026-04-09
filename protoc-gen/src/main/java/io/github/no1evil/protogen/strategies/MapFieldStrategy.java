package io.github.no1evil.protogen.strategies;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.NodeList;
import io.github.no1evil.protogen.model.FieldModel;
import io.github.no1evil.protogen.util.GeneratorUtil;

public class MapFieldStrategy implements FieldGenerationStrategy {
  @Override
  public Expression generateFieldDefinition(FieldModel field) {
    return new NameExpr(field.javaType());
  }

  @Override
  public Expression generateToDomain(FieldModel field, String scope, String mapperFqn) {
    if (!field.isMapValueComplex()) {
      return new MethodCallExpr(new NameExpr(scope), "get" + GeneratorUtil.capitalize(field.name()) + "Map");
    }

    String mapExpr = scope + ".get" + GeneratorUtil.capitalize(field.name()) + "Map().entrySet().stream().collect(" +
        "java.util.stream.Collectors.toMap(" +
        "java.util.Map.Entry::getKey, " +
        "e -> " + field.mapValueMapperFqn() + ".INSTANCE.toDomain(e.getValue())))";

    return StaticJavaParser.parseExpression(mapExpr);
  }

  @Override
  public void generateToProto(FieldModel field, String builderVar, String scope, BlockStmt body) {
    String setter = "putAll" + GeneratorUtil.capitalize(field.name());
    Expression valueToPut;

    if (!field.isMapValueComplex()) {
      valueToPut = GeneratorUtil.createGetter(scope, field.name(), true, false);
    } else {
      String mapExpr = "domain." + field.name() + "().entrySet().stream().collect(" +
          "java.util.stream.Collectors.toMap(" +
          "java.util.Map.Entry::getKey, " +
          "e -> " + field.mapValueMapperFqn() + ".INSTANCE.toProto(e.getValue())))";
      valueToPut = StaticJavaParser.parseExpression(mapExpr);
    }

    IfStmt ifStmt = new IfStmt();
    ifStmt.setCondition(StaticJavaParser.parseExpression("domain." + field.name() + "() != null"));
    ifStmt.setThenStmt(new ExpressionStmt(new MethodCallExpr(new NameExpr(builderVar), setter, new NodeList<>(valueToPut))));
    body.addStatement(ifStmt);
  }
}