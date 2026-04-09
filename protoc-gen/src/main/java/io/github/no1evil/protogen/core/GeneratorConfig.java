package io.github.no1evil.protogen.core;

public record GeneratorConfig(
    String modelsSubPackage,
    String mapperSubPackage
) {
  public static GeneratorConfig load() {
    return new GeneratorConfig("models", "mapper");
  }
}
