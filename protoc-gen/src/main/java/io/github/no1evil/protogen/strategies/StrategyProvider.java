package io.github.no1evil.protogen.strategies;

import io.github.no1evil.protogen.model.FieldModel;

public class StrategyProvider {
  private final PrimitiveFieldStrategy primitiveStrategy = new PrimitiveFieldStrategy();
  private final ComplexFieldStrategy complexStrategy = new ComplexFieldStrategy();
  private final MapFieldStrategy mapStrategy = new MapFieldStrategy();

  public FieldGenerationStrategy get(FieldModel field) {
    if (field.isMap()) return mapStrategy;
    return field.isComplex() ? complexStrategy : primitiveStrategy;
  }
}