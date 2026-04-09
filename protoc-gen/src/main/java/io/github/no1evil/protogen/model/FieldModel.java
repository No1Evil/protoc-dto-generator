package io.github.no1evil.protogen.model;

public record FieldModel(
    String name,
    String javaType,
    String javaTypeFqn,
    boolean isRepeated,
    boolean isOptional,
    boolean isComplex,
    boolean isMessage,
    boolean hasOneOf,
    String protoTypeName,
    String mapperFqn,
    // Map Support
    boolean isMap,
    String mapKeyType,
    String mapValueType,
    String mapValueMapperFqn,
    boolean isMapValueComplex
) {}
