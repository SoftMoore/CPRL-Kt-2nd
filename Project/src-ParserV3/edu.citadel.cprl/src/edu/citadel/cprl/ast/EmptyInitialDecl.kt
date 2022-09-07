package edu.citadel.cprl.ast


import edu.citadel.cprl.Token
import edu.citadel.cprl.Type


/**
 * An empty initial declaration passes constraint checks and emits no code.
 * It is returned from parsing initial declarations as an alternative to
 * returning null when parsing errors are encountered.
 */
object EmptyInitialDecl : InitialDecl(Token(), Type.UNKNOWN)
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