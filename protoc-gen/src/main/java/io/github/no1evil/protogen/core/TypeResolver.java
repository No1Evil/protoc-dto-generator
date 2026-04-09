package io.github.no1evil.protogen.core;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TypeResolver {
  private static final TypeResolver INSTANCE = new TypeResolver();
  private final Map<String, String> packageMap = new ConcurrentHashMap<>();
  private final Map<String, String> resolutionCache = new ConcurrentHashMap<>();

  private TypeResolver() {}
  public static TypeResolver get() { return INSTANCE; }

  public void registerFile(FileDescriptorProto file) {
    String protoPackage = file.getPackage();
    if (protoPackage.isBlank()) return;

    if (file.getOptions().hasJavaPackage()) {
      // always favor an explicit java_package option
      packageMap.put(protoPackage, file.getOptions().getJavaPackage());
    } else {
      // only fallback to protoPackage if no file in this package has defined java_package yet
      packageMap.putIfAbsent(protoPackage, protoPackage);
    }
  }

  public String resolve(String protoTypeName) {
    if (protoTypeName == null || protoTypeName.isBlank()) return null;
    return resolutionCache.computeIfAbsent(protoTypeName, path -> {
      String cleanPath = path.startsWith(".") ? path.substring(1) : path;
      return packageMap.entrySet().stream()
          .filter(entry -> cleanPath.startsWith(entry.getKey()))
          .max((e1, e2) -> Integer.compare(e1.getKey().length(), e2.getKey().length()))
          .map(entry -> entry.getValue() + cleanPath.substring(entry.getKey().length())) // Safe prefix replacement
          .orElse(cleanPath);
    });
  }
}