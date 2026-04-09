package io.github.no1evil.protogen.util;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.StaticJavaParser;

public final class GeneratorUtil {
  private GeneratorUtil() {}

  public static String capitalize(String str) {
    if (str == null || str.isEmpty()) return str;
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

  public static String pascalToCamelCase(String str) {
    if (str == null || str.isEmpty()) return str;
    return str.substring(0, 1).toLowerCase() + str.substring(1);
  }

  public static String toCamelCase(String str) {
    if (str == null || str.isEmpty()) return str;
    StringBuilder builder = new StringBuilder();
    boolean capitalizeNext = false;
    for (char c : str.toCharArray()) {
      if (c == '_') {
        capitalizeNext = true;
      } else if (capitalizeNext) {
        builder.append(Character.toUpperCase(c));
        capitalizeNext = false;
      } else {
        builder.append(c);
      }
    }
    return builder.toString();
  }

  public static String toUpperSnakeCase(String str) {
    if (str == null) return null;
    return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();
  }

  public static String resolveProtoSetterName(String fieldName, boolean isRepeated) {
    String prefix = isRepeated ? "addAll" : "set";
    return prefix + capitalize(fieldName);
  }

  public static MethodCallExpr createGetter(String scope, String fieldName, boolean isDomain, boolean isRepeated) {
    String methodName;
    if (isDomain) {
      methodName = fieldName;
    } else {
      String base = "get" + capitalize(fieldName);
      methodName = isRepeated ? base + "List" : base;
    }
    return new MethodCallExpr(new NameExpr(scope), methodName);
  }

  public static MethodCallExpr createMapperCall(String mapperFqn, String methodName, Expression argument) {
    FieldAccessExpr instance = new FieldAccessExpr(StaticJavaParser.parseExpression(mapperFqn), "INSTANCE");
    return new MethodCallExpr(instance, methodName, new NodeList<>(argument));
  }

  public static MethodCallExpr createStreamMap(String scope, String getterName, String mapperFqn, String methodName, boolean isDomain, boolean isRepeated) {
    Expression getter = createGetter(scope, getterName, isDomain, isRepeated);
    MethodCallExpr stream = new MethodCallExpr(getter, "stream");
    MethodReferenceExpr methodRef = new MethodReferenceExpr(
        new FieldAccessExpr(StaticJavaParser.parseExpression(mapperFqn), "INSTANCE"),
        new NodeList<>(),
        methodName
    );
    MethodCallExpr map = new MethodCallExpr(stream, "map", new NodeList<>(methodRef));
    return new MethodCallExpr(map, "toList");
  }
}