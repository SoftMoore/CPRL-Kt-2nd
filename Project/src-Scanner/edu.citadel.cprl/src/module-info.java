module edu.citadel.cprl
  {
    exports edu.citadel.cprl;
    exports edu.citadel.cprl.ast;
    requires kotlin.stdlib;
    requires edu.citadel.cvm;
    requires transitive edu.citadel.compiler;
  }
