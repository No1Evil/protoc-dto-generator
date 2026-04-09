package io.github.no1evil.protogen.strategies;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import io.github.no1evil.protogen.model.FieldModel;

public interface FieldGenerationStrategy {
  // For Record generation
  Expression generateFieldDefinition(FieldModel field);

  // For Mapper generation
  Expression generateToDomain(FieldModel field, String scope, String mapperFqn);
  void generateToProto(FieldModel field, String builderVar, String scope, BlockStmt body);
}