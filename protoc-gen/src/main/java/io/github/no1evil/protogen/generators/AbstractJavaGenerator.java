package io.github.no1evil.protogen.generators;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import java.util.Collection;

public abstract class AbstractJavaGenerator {

  protected String assemble(String pkg, TypeDeclaration<?> type, Collection<String> imports) {
    CompilationUnit cu = new CompilationUnit();
    cu.setPackageDeclaration(pkg);

    if (imports != null) {
      imports.forEach(cu::addImport);
    }

    cu.addType(type);
    return cu.toString();
  }
}