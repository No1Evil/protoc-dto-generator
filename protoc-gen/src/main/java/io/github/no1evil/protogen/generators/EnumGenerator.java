package io.github.no1evil.protogen.generators;

import com.github.javaparser.ast.body.EnumDeclaration;
import io.github.no1evil.protogen.core.GeneratorConfig;
import io.github.no1evil.protogen.model.EnumModel;
import io.github.no1evil.protogen.model.ProtoFileModel;

import java.util.Collections;

public class EnumGenerator extends AbstractJavaGenerator implements CodeGenerator<EnumModel> {
  private final GeneratorConfig config = GeneratorConfig.load();

  @Override
  public String generate(EnumModel model, ProtoFileModel file) {
    EnumDeclaration enumDecl = new EnumDeclaration()
        .setName(model.name())
        .setPublic(true);

    model.values().forEach(enumDecl::addEnumConstant);

    return assemble(file.javaPackage() + "." + getSubPackage(), enumDecl, Collections.emptySet());
  }

  @Override
  public String getFileName(EnumModel type) {
    return type.name() + ".java";
  }

  @Override
  public String getSubPackage() {
    return config.modelsSubPackage();
  }
}