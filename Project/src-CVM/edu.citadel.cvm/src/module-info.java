module edu.citadel.cvm
  {
    exports edu.citadel.cvm;
    exports edu.citadel.cvm.assembler;
    exports edu.citadel.cvm.assembler.ast;
    requires kotlin.stdlib;
    requires transitive edu.citadel.compiler;
  }
