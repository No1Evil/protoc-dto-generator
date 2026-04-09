package io.github.no1evil.protogen.strategies;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.NodeList;
import io.github.no1evil.protogen.model.FieldModel;
import io.github.no1evil.protogen.util.GeneratorUtil;

public class ComplexFieldStrategy implements FieldGenerationStrategy {
  @Override
  public Expression generateFieldDefinition(FieldModel field) {
    return new NameExpr(field.javaType());
  }

  @Override
  public Expression generateToDomain(FieldModel field, String scope, String mapperFqn) {
    if (field.isRepeated()) {
      return GeneratorUtil.createStreamMap(scope, field.name(), field.mapperFqn(), "toDomain", false, true);
    }

    Expression getter = GeneratorUtil.createGetter(scope, field.name(), false, false);
    Expression mapperCall = GeneratorUtil.createMapperCall(field.mapperFqn(), "toDomain", getter);

    if (field.isMessage() || field.isOptional() || field.hasOneOf()) {
      MethodCallExpr hasCall = new MethodCallExpr(new NameExpr(scope), "has" + GeneratorUtil.capitalize(field.name()));
      return new ConditionalExpr(hasCall, mapperCall, new NullLiteralExpr());
    }

    // for required Enums (not optional, not oneof), execute the mapper directly on the getter
    return mapperCall;
  }

  @Override
  public void generateToProto(FieldModel field, String builderVar, String scope, BlockStmt body) {
    String setter = GeneratorUtil.resolveProtoSetterName(field.name(), field.isRepeated());
    Expression getter = GeneratorUtil.createGetter(scope, field.name(), true, false);
    Expression mapperCall = GeneratorUtil.createMapperCall(field.mapperFqn(), "toProto", getter);

    if (field.isRepeated()) {
      Expression stream = GeneratorUtil.createStreamMap(scope, field.name(), field.mapperFqn(), "toProto", true, true);
      body.addStatement(new ExpressionStmt(new MethodCallExpr(new NameExpr(builderVar), setter, new NodeList<>(stream))));
    } else {
      // guard against null pointer exceptions
      // when executing Builder.setField(...) on null sub-messages
      IfStmt ifStmt = new IfStmt();
      ifStmt.setCondition(StaticJavaParser.parseExpression("domain." + field.name() + "() != null"));
      ifStmt.setThenStmt(new ExpressionStmt(new MethodCallExpr(new NameExpr(builderVar), setter, new NodeList<>(mapperCall))));
      body.addStatement(ifStmt);
    }
  }
}