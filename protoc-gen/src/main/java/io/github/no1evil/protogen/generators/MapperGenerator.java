package io.github.no1evil.protogen.generators;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import io.github.no1evil.protogen.core.GeneratorConfig;
import io.github.no1evil.protogen.model.FieldModel;
import io.github.no1evil.protogen.model.MessageModel;
import io.github.no1evil.protogen.model.OneOfModel;
import io.github.no1evil.protogen.model.ProtoFileModel;
import io.github.no1evil.protogen.strategies.StrategyProvider;
import io.github.no1evil.protogen.util.GeneratorUtil;

import java.util.HashSet;
import java.util.Set;

public class MapperGenerator extends AbstractJavaGenerator implements CodeGenerator<MessageModel> {
  private final StrategyProvider strategies = new StrategyProvider();
  private final GeneratorConfig config = GeneratorConfig.load();

  @Override
  public String generate(MessageModel model, ProtoFileModel file) {
    String className = model.name() + "Mapper";
    String protoFqn = file.javaPackage() + "." + model.name();
    String domainFqn = model.fqn();

    ClassOrInterfaceDeclaration mapperClass = new ClassOrInterfaceDeclaration()
        .setName(className).setPublic(true).setFinal(true);

    mapperClass.addFieldWithInitializer(className, "INSTANCE",
        StaticJavaParser.parseExpression("new " + className + "()"),
        Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);
    mapperClass.addConstructor(Modifier.Keyword.PRIVATE);

    MethodDeclaration toDomain = mapperClass.addMethod("toDomain", Modifier.Keyword.PUBLIC);
    toDomain.addParameter(StaticJavaParser.parseType(protoFqn), "proto");
    toDomain.setType(domainFqn);
    toDomain.setBody(buildToDomain(model, domainFqn));

    MethodDeclaration toProto = mapperClass.addMethod("toProto", Modifier.Keyword.PUBLIC);
    toProto.addParameter(StaticJavaParser.parseType(domainFqn), "domain");
    toProto.setType(protoFqn);
    toProto.setBody(buildToProto(model, protoFqn));

    return assemble(file.javaPackage() + "." + getSubPackage(), mapperClass, calculateImports(model));
  }

  private BlockStmt buildToDomain(MessageModel model, String domainFqn) {
    BlockStmt body = new BlockStmt();
    NodeList<Expression> args = new NodeList<>();

    for (FieldModel field : model.standardFields()) {
      Expression mappedExpr = strategies.get(field).generateToDomain(field, "proto", field.mapperFqn());
      VariableDeclarator vd = new VariableDeclarator(StaticJavaParser.parseType("var"), field.name(), mappedExpr);
      body.addStatement(new VariableDeclarationExpr(vd));
      args.add(new NameExpr(field.name()));
    }

    for (OneOfModel oneOf : model.oneOfs()) {
      StringBuilder sw = new StringBuilder();
      sw.append("switch (proto.get").append(oneOf.pascalCaseName()).append("Case()) {\n");

      for (FieldModel f : oneOf.fields()) {
        String protoEnumCase = GeneratorUtil.toUpperSnakeCase(f.name());
        String wrapperName = domainFqn + "." + oneOf.pascalCaseName() + "." + GeneratorUtil.capitalize(f.name()) + "Value";
        Expression fieldMapping = strategies.get(f).generateToDomain(f, "proto", f.mapperFqn());

        sw.append("  case ").append(protoEnumCase).append(" ->\n    new ").append(wrapperName)
            .append("(").append(fieldMapping.toString()).append(");\n");
      }
      sw.append("  case ").append(GeneratorUtil.toUpperSnakeCase(oneOf.name())).append("_NOT_SET ->\n    null;\n");
      sw.append("  default ->\n    null;\n");
      sw.append("}");

      VariableDeclarator vd = new VariableDeclarator(StaticJavaParser.parseType("var"), oneOf.camelCaseName(), StaticJavaParser.parseExpression(sw.toString()));
      body.addStatement(new VariableDeclarationExpr(vd));
      args.add(new NameExpr(oneOf.camelCaseName()));
    }

    body.addStatement(new ReturnStmt(new ObjectCreationExpr(null, StaticJavaParser.parseClassOrInterfaceType(domainFqn), args)));
    return body;
  }

  private BlockStmt buildToProto(MessageModel model, String protoFqn) {
    BlockStmt body = new BlockStmt();
    body.addStatement(StaticJavaParser.parseStatement("var builder = " + protoFqn + ".newBuilder();"));

    for (FieldModel field : model.standardFields()) {
      strategies.get(field).generateToProto(field, "builder", "domain", body);
    }

    for (OneOfModel oneOf : model.oneOfs()) {
      IfStmt ifStmt = new IfStmt();
      ifStmt.setCondition(StaticJavaParser.parseExpression("domain." + oneOf.camelCaseName() + "() != null"));

      StringBuilder sw = new StringBuilder();
      sw.append("switch (domain.").append(oneOf.camelCaseName()).append("()) {\n");

      for (FieldModel f : oneOf.fields()) {
        String wrapperName = model.fqn() + "." + oneOf.pascalCaseName() + "." + GeneratorUtil.capitalize(f.name()) + "Value";
        String setter = GeneratorUtil.resolveProtoSetterName(f.name(), f.isRepeated());

        String mapExpr = f.isComplex()
            ? f.mapperFqn() + ".INSTANCE.toProto(p.event())"
            : "p.event()";

        sw.append("  case ").append(wrapperName).append(" p ->\n    builder.").append(setter).append("(").append(mapExpr).append(");\n");
      }
      sw.append("}");

      BlockStmt thenBlock = new BlockStmt();
      thenBlock.addStatement(StaticJavaParser.parseStatement(sw.toString()));
      ifStmt.setThenStmt(thenBlock);

      body.addStatement(ifStmt);
    }

    body.addStatement(new ReturnStmt(new MethodCallExpr(new NameExpr("builder"), "build")));
    return body;
  }

  private Set<String> calculateImports(MessageModel model) {
    Set<String> imports = new HashSet<>();
    for (FieldModel field : model.allFields()) {
      if (field.isRepeated()) imports.add("java.util.List");
      if (field.javaTypeFqn() != null) imports.add(field.javaTypeFqn());
      if (field.mapperFqn() != null) imports.add(field.mapperFqn());

      // Specifically import map value mappers
      if (field.isMap() && field.mapValueMapperFqn() != null) {
        imports.add(field.mapValueMapperFqn());
      }
    }
    return imports;
  }

  @Override
  public String getFileName(MessageModel type) {
    return type.name() + "Mapper.java";
  }

  @Override
  public String getSubPackage() {
    return config.mapperSubPackage();
  }
}