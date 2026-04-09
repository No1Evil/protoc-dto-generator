package io.github.no1evil.protogen.model;

import java.util.ArrayList;
import java.util.List;

public record MessageModel(
    String name,
    String fqn,
    List<FieldModel> standardFields,
    List<OneOfModel> oneOfs
) implements TypeModel {
  @Override public String getName() { return name; }
  @Override public String getFqn() { return fqn; }

  // helper to accumulate all fields for imports cleanly
  public List<FieldModel> allFields() {
    List<FieldModel> all = new ArrayList<>(standardFields);
    oneOfs.forEach(o -> all.addAll(o.fields()));
    return all;
  }
}