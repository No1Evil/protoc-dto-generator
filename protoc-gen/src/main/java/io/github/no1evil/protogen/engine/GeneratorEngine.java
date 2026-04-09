package io.github.no1evil.protogen.engine;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.*;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.Feature;
import io.github.no1evil.protogen.core.TypeResolver;
import io.github.no1evil.protogen.generators.CodeGenerator;
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

    Set<String> targets = parseTargets(request.getParameter());

    // Stage 1: Analysis
    request.getProtoFileList().forEach(TypeResolver.get()::registerFile);
    Map<String, ProtoFileModel> projectIr = request.getProtoFileList().stream()
        .collect(Collectors.toMap(FileDescriptorProto::getName, parser::parse));

    // Stage 2: Generation
    request.getFileToGenerateList().forEach(fileName -> {
      ProtoFileModel fileModel = projectIr.get(fileName);
      for (TypeModel type : fileModel.types()) {
        registry.getGeneratorsFor(type).stream()
            .filter(gen -> shouldGenerate(gen, targets))
            .forEach(gen -> {
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

  /**
   * Parses the plugin parameters. e.g., "target=dto,target=mappers"
   */
  private Set<String> parseTargets(String parameter) {
    if (parameter == null || parameter.isBlank()) {
      return Set.of("all");
    }

    Set<String> targets = new HashSet<>();
    for (String param : parameter.split(",")) {
      String[] parts = param.trim().split("=");
      if (parts.length == 2 && parts[0].trim().equalsIgnoreCase("target")) {
        targets.add(parts[1].trim().toLowerCase());
      }
    }

    return targets.isEmpty() ? Set.of("all") : targets;
  }

  /**
   * Decides whether a generator should run based on the parsed target option.
   */
  private boolean shouldGenerate(CodeGenerator<?> gen, Set<String> targets) {
    if (targets.contains("all")) {
      return true;
    }

    String subPackage = gen.getSubPackage();

    if ("models".equals(subPackage) && (targets.contains("dto") || targets.contains("models"))) {
      return true;
    }
    if ("mapper".equals(subPackage) && (targets.contains("mappers") || targets.contains("mapper"))) {
      return true;
    }
    if ("service".equals(subPackage) && (targets.contains("services") || targets.contains("service"))) {
      return true;
    }

    return false;
  }
}