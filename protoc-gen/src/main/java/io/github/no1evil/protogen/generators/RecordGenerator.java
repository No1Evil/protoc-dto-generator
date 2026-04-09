package io.github.no1evil.protogen.generators;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.github.no1evil.protogen.core.GeneratorConfig;
import io.github.no1evil.protogen.model.FieldModel;
import io.github.no1evil.protogen.model.MessageModel;
import io.github.no1evil.protogen.model.OneOfModel;
import io.github.no1evil.protogen.model.ProtoFileModel;
import io.github.no1evil.protogen.strategies.FieldGenerationStrategy;
import io.github.no1evil.protogen.strategies.StrategyProvider;
import io.github.no1evil.protogen.util.GeneratorUtil;

import java.util.HashSet;
import java.util.Set;

public class RecordGenerator extends AbstractJavaGenerator implements CodeGenerator<MessageModel> {
  private final StrategyProvider strategies = new StrategyProvider();
  private final GeneratorConfig config = GeneratorConfig.load();

  @Override
  public String generate(MessageModel model, ProtoFileModel file) {
    RecordDeclaration record = new RecordDeclaration()
        .setName(model.name())
        .setPublic(true);

    Set<String> imports = new HashSet<>();

    // generate Standard Fields
    for (FieldModel field : model.standardFields()) {
      FieldGenerationStrategy strategy = strategies.get(field);
      Expression typeExpr = strategy.generateFieldDefinition(field);
      String finalType = field.isRepeated() ? "List<" + typeExpr + ">" : typeExpr.toString();
      record.addParameter(finalType, field.name());
    }

    // generate OneOf Fields and their nested Sealed Interfaces
    for (OneOfModel oneOf : model.oneOfs()) {
      record.addParameter(oneOf.pascalCaseName(), oneOf.camelCaseName());

      ClassOrInterfaceDeclaration sealedIfc = new ClassOrInterfaceDeclaration()
          .setInterface(true).setPublic(true).setName(oneOf.pascalCaseName())
          .addModifier(Modifier.Keyword.SEALED);

      NodeList<ClassOrInterfaceType> permittedTypes = new NodeList<>();

      for (FieldModel field : oneOf.fields()) {
        // appending "Value" to prevent naming collisions with the inner domain model
        String wrapperName = GeneratorUtil.capitalize(field.name()) + "Value";

        RecordDeclaration wrapper = new RecordDeclaration()
            .setName(wrapperName)
            .addParameter(field.javaType(), "event")
            .addImplementedType(oneOf.pascalCaseName());

        sealedIfc.addMember(wrapper);
      }

      sealedIfc.setPermittedTypes(permittedTypes);

      record.addMember(sealedIfc);
    }

    // accumulate Imports Cleanly
    for (FieldModel field : model.allFields()) {
      if (field.javaTypeFqn() != null) imports.add(field.javaTypeFqn());
      if (field.isRepeated()) imports.add("java.util.List");
    }

    return assemble(file.javaPackage() + "." + getSubPackage(), record, imports);
  }

  @Override
  public String getFileName(MessageModel type) {
    return type.name() + ".java";
  }

  @Override
  public String getSubPackage() {
    return config.modelsSubPackage();
  }
}