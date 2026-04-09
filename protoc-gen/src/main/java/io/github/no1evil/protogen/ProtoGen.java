package io.github.no1evil.protogen;

import io.github.no1evil.protogen.engine.GeneratorEngine;
import java.io.IOException;

public class ProtoGen {

  public static void main(String[] args) throws IOException {
    new GeneratorEngine().execute();
  }
}