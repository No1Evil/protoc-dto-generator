package io.github.no1evil.protogen.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.JavaParserAdapter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

public class FastParser {
  private static final JavaParser PARSER;
  private static final JavaParserAdapter ADAPTER;

  static {
    ParserConfiguration config = new ParserConfiguration()
        .setLanguageLevel(ParserConfiguration.LanguageLevel.RAW)
        .setAttributeComments(false)
        .setStoreTokens(false);

    PARSER = new JavaParser(config);
    ADAPTER = new JavaParserAdapter(PARSER);
  }

  public static Statement parseStatement(String statement) {
    return ADAPTER.parseStatement(statement);
  }

  public static Type parseType(String type) {
    return ADAPTER.parseType(type);
  }

  public static Expression parseExpression(String expression){
    return ADAPTER.parseExpression(expression);
  }

  public static ClassOrInterfaceType parseClassOrInterfaceType(String type){
    return ADAPTER.parseClassOrInterfaceType(type);
  }
}