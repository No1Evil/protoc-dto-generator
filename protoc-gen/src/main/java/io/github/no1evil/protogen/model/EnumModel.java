package io.github.no1evil.protogen.model;

import java.util.List;

public record EnumModel(
    String name,
    String fqn,
    List<String> values
) implements TypeModel {
  @Override public String getName() { return name; }
  @Override public String getFqn() { return fqn; }
}
