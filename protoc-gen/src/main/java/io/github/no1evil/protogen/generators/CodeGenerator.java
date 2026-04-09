package io.github.no1evil.protogen.generators;

import io.github.no1evil.protogen.model.*;
import io.github.no1evil.protogen.model.TypeModel;

public interface CodeGenerator<T extends TypeModel> {
  String generate(T type, ProtoFileModel fileModel);
  String getFileName(T type);
  String getSubPackage();
}
