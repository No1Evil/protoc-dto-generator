package io.github.no1evil.protogen.model;

import java.util.List;

public record ServiceModel(
    String name,
    String fqn,
    List<MethodModel> methods
) implements TypeModel {
  @Override public String getName() { return name; }
  @Override public String getFqn() { return fqn; }
}
