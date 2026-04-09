package io.github.no1evil.protogen.strategies;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.NodeList;
import io.github.no1evil.protogen.model.FieldModel;
import io.github.no1evil.protogen.parser.FastParser;
import io.github.no1evil.protogen.util.GeneratorUtil;

public class PrimitiveFieldStrategy implements FieldGenerationStrategy {
  @Override
  public Expression generateFieldDefinition(FieldModel field) {
    return new NameExpr(field.javaType());
  }

  @Override
  public Expression generateToDomain(FieldModel field, String scope, String mapperFqn) {
    Expression getter = GeneratorUtil.createGetter(scope, field.name(), false, false);

    if (field.isOptional() || field.hasOneOf()) {
      MethodCallExpr hasCall = new MethodCallExpr(new NameExpr(scope), "has" + GeneratorUtil.capitalize(field.name()));
      return new ConditionalExpr(hasCall, getter, new NullLiteralExpr());
    }
    return getter;
  }

  @Override
  public void generateToProto(FieldModel field, String builderVar, String scope, BlockStmt body) {
    String setter = GeneratorUtil.resolveProtoSetterName(field.name(), field.isRepeated());
    Expression getter = GeneratorUtil.createGetter(scope, field.name(), true, false);

    if (field.javaType().equals("String") || field.isOptional() || field.hasOneOf()) {
      IfStmt ifStmt = new IfStmt();
      ifStmt.setCondition(FastParser.parseExpression("domain." + field.name() + "() != null"));
      ifStmt.setThenStmt(new ExpressionStmt(new MethodCallExpr(new NameExpr(builderVar), setter, new NodeList<>(getter))));
      body.addStatement(ifStmt);
    } else {
      body.addStatement(new ExpressionStmt(new MethodCallExpr(new NameExpr(builderVar), setter, new NodeList<>(getter))));
    }
  }
}