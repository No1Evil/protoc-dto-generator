package io.github.no1evil.protogen.model;

public record MethodModel(
    String name,
    String inputTypeFqn,
    String outputTypeFqn,
    boolean clientStreaming,
    boolean serverStreaming
) {}