package io.github.no1evil.protogen.engine;

import io.github.no1evil.protogen.generators.*;
import io.github.no1evil.protogen.model.*;
import java.util.*;

public class GeneratorRegistry {
  private final Map<Class<?>, List<CodeGenerator<?>>> generators = new HashMap<>();

  public GeneratorRegistry() {
    generators.put(MessageModel.class, List.of(new RecordGenerator(), new MapperGenerator()));
    generators.put(EnumModel.class, List.of(new EnumGenerator(), new EnumMapperGenerator()));
    generators.put(ServiceModel.class, List.of(new ServiceGenerator()));
  }

  @SuppressWarnings("unchecked")
  public <T extends TypeModel> List<CodeGenerator<T>> getGeneratorsFor(T type) {
    return (List<CodeGenerator<T>>) (List<?>) generators.getOrDefault(type.getClass(), Collections.emptyList());
  }
}