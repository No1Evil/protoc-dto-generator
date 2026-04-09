package io.github.no1evil.protogen.generators;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import io.github.no1evil.protogen.core.GeneratorConfig;
import io.github.no1evil.protogen.model.EnumModel;
import io.github.no1evil.protogen.model.ProtoFileModel;

import java.util.Collections;

public class EnumMapperGenerator extends AbstractJavaGenerator implements CodeGenerator<EnumModel> {
  private final GeneratorConfig config = GeneratorConfig.load();

  @Override
  public String generate(EnumModel model, ProtoFileModel file) {
    String className = model.name() + "Mapper";
    String protoEnumFqn = file.javaPackage() + "." + model.name();
    String domainEnumFqn = model.fqn();

    ClassOrInterfaceDeclaration mapperClass = new ClassOrInterfaceDeclaration()
        .setName(className).setPublic(true).setFinal(true);

    mapperClass.addFieldWithInitializer(className, "INSTANCE",
        StaticJavaParser.parseExpression("new " + className + "()"),
        Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);
    mapperClass.addConstructor(Modifier.Keyword.PRIVATE);

    // the first value in Protobuf enums is always index 0
    String indexZeroValue = model.values().isEmpty() ? "UNSPECIFIED" : model.values().get(0);

    // toDomain explicit Java 21 Switch Expression
    MethodDeclaration toDomain = mapperClass.addMethod("toDomain", Modifier.Keyword.PUBLIC);
    toDomain.addParameter(StaticJavaParser.parseType(protoEnumFqn), "proto");
    toDomain.setType(domainEnumFqn);

    StringBuilder toDomainBody = new StringBuilder("return switch (proto) {\n");
    // start at 1 to intentionally skip the 0-index value, routing it to default
    for (int i = 1; i < model.values().size(); i++) {
      String val = model.values().get(i);
      if (val.equals("UNRECOGNIZED")) continue;
      toDomainBody.append("  case ").append(val).append(" -> ").append(domainEnumFqn).append(".").append(val).append(";\n");
    }
    toDomainBody.append("  case null, default -> ").append(domainEnumFqn).append(".").append(indexZeroValue).append(";\n");
    toDomainBody.append("};");

    toDomain.setBody(new BlockStmt().addStatement(StaticJavaParser.parseStatement(toDomainBody.toString())));

    // toProto explicit Java 21 Switch Expression
    MethodDeclaration toProto = mapperClass.addMethod("toProto", Modifier.Keyword.PUBLIC);
    toProto.addParameter(StaticJavaParser.parseType(domainEnumFqn), "domain");
    toProto.setType(protoEnumFqn);

    StringBuilder toProtoBody = new StringBuilder("return switch (domain) {\n");
    // start at 1 to intentionally skip the 0-index value, routing it to default
    for (int i = 1; i < model.values().size(); i++) {
      String val = model.values().get(i);
      if (val.equals("UNRECOGNIZED")) continue;
      toProtoBody.append("  case ").append(val).append(" -> ").append(protoEnumFqn).append(".").append(val).append(";\n");
    }
    toProtoBody.append("  case null, default -> ").append(protoEnumFqn).append(".").append(indexZeroValue).append(";\n");
    toProtoBody.append("};");

    toProto.setBody(new BlockStmt().addStatement(StaticJavaParser.parseStatement(toProtoBody.toString())));

    return assemble(file.javaPackage() + "." + getSubPackage(), mapperClass, Collections.emptySet());
  }

  @Override
  public String getFileName(EnumModel type) {
    return type.name() + "Mapper.java";
  }

  @Override
  public String getSubPackage() {
    return config.mapperSubPackage();
  }
}