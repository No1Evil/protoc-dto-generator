package io.github.no1evil.protogen.model;


import java.util.List;

public record ProtoFileModel(String packageName, String javaPackage, List<TypeModel> types) {

}
