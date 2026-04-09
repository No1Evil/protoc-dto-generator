package io.github.no1evil.protogen.parser;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.*;
import io.github.no1evil.protogen.core.*;
import io.github.no1evil.protogen.model.*;
import io.github.no1evil.protogen.util.GeneratorUtil;
import java.util.*;

public class ProtoSemanticParser {
  private final TypeResolver typeResolver = TypeResolver.get();
  private final GeneratorConfig config = GeneratorConfig.load();

  private static final Map<String, String> WKT_TYPES = Map.of(
      "google.protobuf.Timestamp", "java.time.Instant",
      "google.protobuf.Duration", "java.time.Duration",
      "google.protobuf.Empty", "java.lang.Void",
      "google.type.Money", "java.math.BigDecimal"
  );

  private static final Map<String, String> WKT_MAPPERS = Map.of(
      "google.protobuf.Timestamp", "io.github.no1evil.protogen.base.TimestampMapper",
      "google.protobuf.Duration", "io.github.no1evil.protogen.base.DurationMapper",
      "google.type.Money", "io.github.no1evil.protogen.base.MoneyMapper"
  );

  private MessageModel parseMessage(DescriptorProto msg, String javaPkg, Map<String, DescriptorProto> mapEntries) {
    List<FieldModel> standardFields = new ArrayList<>();
    Map<Integer, List<FieldModel>> oneOfGroups = new LinkedHashMap<>();

    for (FieldDescriptorProto f : msg.getFieldList()) {
      FieldModel field = parseField(f, mapEntries);
      if (f.hasOneofIndex() && !f.hasProto3Optional()) {
        oneOfGroups.computeIfAbsent(f.getOneofIndex(), k -> new ArrayList<>()).add(field);
      } else {
        standardFields.add(field);
      }
    }

    List<OneOfModel> oneOfs = new ArrayList<>();
    for (Map.Entry<Integer, List<FieldModel>> entry : oneOfGroups.entrySet()) {
      String protoOneOfName = msg.getOneofDecl(entry.getKey()).getName();
      String camelCase = GeneratorUtil.toCamelCase(protoOneOfName);
      String pascalCase = GeneratorUtil.capitalize(camelCase);

      List<FieldModel> cleanFields = entry.getValue().stream()
          .map(f -> new FieldModel(f.name(), f.javaType(), f.javaTypeFqn(), f.isRepeated(), false, f.isComplex(), f.isMessage(), false, f.protoTypeName(), f.mapperFqn(), f.isMap(), f.mapKeyType(), f.mapValueType(), f.mapValueMapperFqn(), f.isMapValueComplex()))
          .toList();

      oneOfs.add(new OneOfModel(protoOneOfName, camelCase, pascalCase, cleanFields));
    }

    return new MessageModel(msg.getName(), javaPkg + "." + config.modelsSubPackage() + "." + msg.getName(), standardFields, oneOfs);
  }

  private EnumModel parseEnum(EnumDescriptorProto enm, String javaPkg) {
    return new EnumModel(enm.getName(), javaPkg + "." + config.modelsSubPackage() + "." + enm.getName(),
        enm.getValueList().stream().map(DescriptorProtos.EnumValueDescriptorProto::getName).toList());
  }

  private ServiceModel parseService(ServiceDescriptorProto svc, String javaPkg) {
    List<MethodModel> methods = new ArrayList<>();
    for (MethodDescriptorProto m : svc.getMethodList()) {
      methods.add(new MethodModel(
          m.getName(),
          resolveServiceTypeFqn(m.getInputType()),
          resolveServiceTypeFqn(m.getOutputType()),
          m.getClientStreaming(),
          m.getServerStreaming()
      ));
    }
    return new ServiceModel(svc.getName(), javaPkg + ".service." + svc.getName(), methods);
  }

  private String resolveServiceTypeFqn(String protoTypeName) {
    String rawType = protoTypeName.startsWith(".") ? protoTypeName.substring(1) : protoTypeName;
    if (WKT_TYPES.containsKey(rawType)) {
      return WKT_TYPES.get(rawType);
    }
    String resolvedRootFqn = typeResolver.resolve(protoTypeName);
    if (resolvedRootFqn != null) {
      String javaType = resolvedRootFqn.substring(resolvedRootFqn.lastIndexOf('.') + 1);
      int lastDot = resolvedRootFqn.lastIndexOf('.');
      String pkgOnly = (lastDot != -1) ? resolvedRootFqn.substring(0, lastDot) : "";
      return pkgOnly + "." + config.modelsSubPackage() + "." + javaType;
    }
    return "Object";
  }

  private FieldModel parseField(FieldDescriptorProto f, Map<String, DescriptorProto> mapEntries) {
    boolean isMessage = f.getType() == FieldDescriptorProto.Type.TYPE_MESSAGE;
    boolean isComplex = isMessage || f.getType() == FieldDescriptorProto.Type.TYPE_ENUM;
    boolean isRepeated = f.getLabel() == FieldDescriptorProto.Label.LABEL_REPEATED;
    boolean isOptional = f.hasProto3Optional();
    boolean hasOneOf = f.hasOneofIndex() && !f.hasProto3Optional();

    String rawType = f.getTypeName().startsWith(".") ? f.getTypeName().substring(1) : f.getTypeName();
    String javaType;
    String javaTypeFqn = null;
    String mapperFqn = null;

    String camelCaseName = GeneratorUtil.toCamelCase(f.getName());

    boolean isMap = false;
    String mapKeyType = null;
    String mapValueType = null;
    String mapValueMapperFqn = null;
    boolean isMapValueComplex = false;

    // Detect Maps
    if (isRepeated && isMessage && mapEntries.containsKey(f.getTypeName())) {
      isMap = true;
      isRepeated = false; // Maps handle their own iterators
      isComplex = false;
      isMessage = false;

      DescriptorProto mapEntryMsg = mapEntries.get(f.getTypeName());
      FieldDescriptorProto keyField = mapEntryMsg.getField(0);
      FieldDescriptorProto valField = mapEntryMsg.getField(1);

      mapKeyType = resolvePrimitive(keyField.getType(), true);

      boolean valIsMessage = valField.getType() == FieldDescriptorProto.Type.TYPE_MESSAGE;
      boolean valIsEnum = valField.getType() == FieldDescriptorProto.Type.TYPE_ENUM;
      isMapValueComplex = valIsMessage || valIsEnum;
      String rawValType = valField.getTypeName().startsWith(".") ? valField.getTypeName().substring(1) : valField.getTypeName();

      if (WKT_TYPES.containsKey(rawValType)) {
        mapValueType = WKT_TYPES.get(rawValType).substring(WKT_TYPES.get(rawValType).lastIndexOf('.') + 1);
        mapValueMapperFqn = WKT_MAPPERS.get(rawValType);
        isMapValueComplex = true;
      } else if (isMapValueComplex) {
        String resolvedFqn = typeResolver.resolve(valField.getTypeName());
        if (resolvedFqn != null) {
          mapValueType = resolvedFqn.substring(resolvedFqn.lastIndexOf('.') + 1);
          int lastDot = resolvedFqn.lastIndexOf('.');
          String pkgOnly = (lastDot != -1) ? resolvedFqn.substring(0, lastDot) : "";
          mapValueMapperFqn = pkgOnly + "." + config.mapperSubPackage() + "." + mapValueType + "Mapper";
        } else {
          mapValueType = "Object";
        }
      } else {
        mapValueType = resolvePrimitive(valField.getType(), true);
      }
      javaType = "java.util.Map<" + mapKeyType + ", " + mapValueType + ">";
    } else {
      if (WKT_TYPES.containsKey(rawType)) {
        javaTypeFqn = WKT_TYPES.get(rawType);
        javaType = javaTypeFqn.substring(javaTypeFqn.lastIndexOf('.') + 1);
        mapperFqn = WKT_MAPPERS.get(rawType);
        isComplex = true;
        isMessage = true;
      } else if (isComplex) {
        String resolvedRootFqn = typeResolver.resolve(f.getTypeName());
        if (resolvedRootFqn != null) {
          javaType = resolvedRootFqn.substring(resolvedRootFqn.lastIndexOf('.') + 1);
          int lastDot = resolvedRootFqn.lastIndexOf('.');
          String pkgOnly = (lastDot != -1) ? resolvedRootFqn.substring(0, lastDot) : "";
          javaTypeFqn = pkgOnly + "." + config.modelsSubPackage() + "." + javaType;
          mapperFqn = pkgOnly + "." + config.mapperSubPackage() + "." + javaType + "Mapper";
        } else {
          javaType = "Object";
        }
      } else {
        javaType = resolvePrimitive(f.getType(), isOptional || isRepeated || hasOneOf);
      }
    }

    return new FieldModel(camelCaseName, javaType, javaTypeFqn, isRepeated, isOptional, isComplex, isMessage, hasOneOf, f.getTypeName(), mapperFqn, isMap, mapKeyType, mapValueType, mapValueMapperFqn, isMapValueComplex);
  }

  public ProtoFileModel parse(FileDescriptorProto file) {
    String javaPkg = file.getOptions().hasJavaPackage() ? file.getOptions().getJavaPackage() : file.getPackage();
    String protoPkg = file.getPackage();
    Map<String, DescriptorProto> localMapEntries = new HashMap<>();

    for (DescriptorProto msg : file.getMessageTypeList()) {
      String prefix = protoPkg.isEmpty() ? "." + msg.getName() : "." + protoPkg + "." + msg.getName();
      collectMapEntries(msg, prefix, localMapEntries);
    }

    List<TypeModel> types = new ArrayList<>();
    file.getMessageTypeList().forEach(msg -> types.add(parseMessage(msg, javaPkg, localMapEntries)));
    file.getEnumTypeList().forEach(enm -> types.add(parseEnum(enm, javaPkg)));
    file.getServiceList().forEach(svc -> types.add(parseService(svc, javaPkg)));

    return new ProtoFileModel(file.getPackage(), javaPkg, types);
  }

  private void collectMapEntries(DescriptorProto msg, String prefix, Map<String, DescriptorProto> out) {
    for (DescriptorProto nested : msg.getNestedTypeList()) {
      String fqn = prefix + "." + nested.getName();
      if (nested.getOptions().getMapEntry()) {
        out.put(fqn, nested);
      }
      collectMapEntries(nested, fqn, out);
    }
  }

  private String resolvePrimitive(FieldDescriptorProto.Type type, boolean useWrapper) {
    return switch (type) {
      case TYPE_DOUBLE -> useWrapper ? "Double" : "double";
      case TYPE_FLOAT -> useWrapper ? "Float" : "float";
      case TYPE_INT64, TYPE_UINT64, TYPE_FIXED64, TYPE_SFIXED64, TYPE_SINT64 -> useWrapper ? "Long" : "long";
      case TYPE_INT32, TYPE_FIXED32, TYPE_UINT32, TYPE_SFIXED32, TYPE_SINT32 -> useWrapper ? "Integer" : "int";
      case TYPE_BOOL -> useWrapper ? "Boolean" : "boolean";
      case TYPE_STRING -> "String";
      case TYPE_ENUM -> "Integer";
      default -> "Object";
    };
  }
}