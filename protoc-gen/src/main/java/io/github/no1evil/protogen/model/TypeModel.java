package io.github.no1evil.protogen.model;

public sealed interface TypeModel permits MessageModel, EnumModel, ServiceModel {
  String getName();
  String getFqn();
}
