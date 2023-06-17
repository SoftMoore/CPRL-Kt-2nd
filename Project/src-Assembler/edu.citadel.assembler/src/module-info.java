module edu.citadel.assembler
  {
    exports edu.citadel.assembler;
    exports edu.citadel.assembler.ast;
    requires kotlin.stdlib;
    requires edu.citadel.cvm;
    requires transitive edu.citadel.compiler;
  }
