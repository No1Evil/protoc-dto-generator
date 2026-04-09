package io.github.no1evil.protogen.engine;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.*;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.Feature;
import io.github.no1evil.protogen.core.TypeResolver;
import io.github.no1evil.protogen.model.*;
import io.github.no1evil.protogen.parser.ProtoSemanticParser;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class GeneratorEngine {
  private final ProtoSemanticParser parser = new ProtoSemanticParser();
  private final GeneratorRegistry registry = new GeneratorRegistry();

  public void execute() throws IOException {
    CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(System.in);
    CodeGeneratorResponse.Builder response = CodeGeneratorResponse.newBuilder();
    response.setSupportedFeatures(Feature.FEATURE_PROTO3_OPTIONAL_VALUE);

    // Stage 1: Analysis
    request.getProtoFileList().forEach(TypeResolver.get()::registerFile);
    Map<String, ProtoFileModel> projectIr = request.getProtoFileList().stream()
        .collect(Collectors.toMap(FileDescriptorProto::getName, parser::parse));

    // Stage 2: Generation
    request.getFileToGenerateList().forEach(fileName -> {
      ProtoFileModel fileModel = projectIr.get(fileName);
      for (TypeModel type : fileModel.types()) {
        registry.getGeneratorsFor(type).forEach(gen -> {
          String code = gen.generate(type, fileModel);
          String path = (fileModel.javaPackage() + "." + gen.getSubPackage())
              .replace('.', '/') + "/" + gen.getFileName(type);
          response.addFile(CodeGeneratorResponse.File.newBuilder()
              .setName(path).setContent(code).build());
        });
      }
    });

    response.build().writeTo(System.out);
  }
}