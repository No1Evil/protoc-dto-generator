package io.github.no1evil.protogen.model;

import java.util.List;

public record OneOfModel(
    String name,           // Raw proto name e.g. "payload"
    String camelCaseName,  // e.g. "payload"
    String pascalCaseName, // e.g. "Payload"
    List<FieldModel> fields
) {}