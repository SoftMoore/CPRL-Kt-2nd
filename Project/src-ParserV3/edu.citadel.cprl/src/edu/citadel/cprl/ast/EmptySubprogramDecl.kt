package edu.citadel.cprl.ast

import edu.citadel.cprl.Token

/**
 * An empty subprogram declaration passes constraint checks and emits no code.
 * It is returned from parsing subprogram declarations as an alternative to
 * returning null when parsing errors are encountered.
 */
object EmptySubprogramDecl : SubprogramDecl(Token())
  {
    override fun checkConstraints()
      {
        // nothing to check
      }

    override fun emit()
      {
        // nothing to emit
      }
  }
