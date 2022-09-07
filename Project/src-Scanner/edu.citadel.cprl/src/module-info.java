module edu.citadel.cprl
  {
    exports edu.citadel.cprl;
    exports edu.citadel.cprl.ast;
    requires edu.citadel.cvm;
    requires kotlin.stdlib;
    requires transitive edu.citadel.compiler;
    requires java.base;
  }
