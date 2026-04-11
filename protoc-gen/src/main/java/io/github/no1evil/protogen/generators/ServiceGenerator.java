package io.github.no1evil.protogen.generators;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.github.no1evil.protogen.model.MethodModel;
import io.github.no1evil.protogen.model.ProtoFileModel;
import io.github.no1evil.protogen.model.ServiceModel;
import io.github.no1evil.protogen.util.GeneratorUtil;

import java.util.HashSet;
import java.util.Set;

public class ServiceGenerator extends AbstractJavaGenerator implements CodeGenerator<ServiceModel> {

  @Override
  public String generate(ServiceModel model, ProtoFileModel file) {
    ClassOrInterfaceDeclaration serviceIfc = new ClassOrInterfaceDeclaration()
        .setName(model.name())
        .setPublic(true)
        .setInterface(true);

    Set<String> imports = new HashSet<>();

    boolean usesStream = false;

    for (MethodModel method : model.methods()) {
      String camelCaseName = GeneratorUtil.pascalToCamelCase(method.name());
      MethodDeclaration md = serviceIfc.addMethod(camelCaseName, Modifier.Keyword.PUBLIC);
      md.removeBody(); // Ensures it acts as a pure interface definition

      String inSimple = method.inputTypeFqn().substring(method.inputTypeFqn().lastIndexOf('.') + 1);
      String outSimple = method.outputTypeFqn().substring(method.outputTypeFqn().lastIndexOf('.') + 1);

      imports.add(method.inputTypeFqn());
      imports.add(method.outputTypeFqn());

      if (!method.clientStreaming() && !method.serverStreaming()) {
        // Type 1: Unary
        md.setType(outSimple);
        md.addParameter(inSimple, "request");
      }
      else if (!method.clientStreaming() && method.serverStreaming()) {
        // Type 2: Server Streaming
        md.setType("Stream<" + outSimple + ">");
        md.addParameter(inSimple, "request");
        usesStream = true;
      }
      else if (method.clientStreaming() && !method.serverStreaming()) {
        // Type 3: Client Streaming
        md.setType(outSimple);
        md.addParameter("Stream<" + inSimple + ">", "requestStream");
        usesStream = true;
      }
      else {
        // Type 4: Bidirectional Streaming
        md.setType("Stream<" + outSimple + ">");
        md.addParameter("Stream<" + inSimple + ">", "requestStream");
        usesStream = true;
      }
    }

    if (usesStream) imports.add("java.util.stream.Stream");

    return assemble(file.javaPackage() + "." + getSubPackage(), serviceIfc, imports);
  }

  @Override
  public String getFileName(ServiceModel type) {
    return type.name() + ".java";
  }

  @Override
  public String getSubPackage() {
    return "service";
  }
}