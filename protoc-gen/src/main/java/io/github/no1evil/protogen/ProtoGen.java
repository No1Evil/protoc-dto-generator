package io.github.no1evil.protogen;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import io.github.no1evil.protogen.engine.GeneratorEngine;
import java.io.IOException;

public class ProtoGen {

  static {
    StaticJavaParser.getParserConfiguration()
        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
  }

  static void main(String[] args) throws IOException {
    new GeneratorEngine().execute();
  }
}