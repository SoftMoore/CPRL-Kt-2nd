package edu.citadel.cprl.ast

import edu.citadel.cprl.Token
import edu.citadel.cprl.Type

/**
 * Base class for all initial declarations.
 *
 * @constructor Construct an initial declaration with its identifier and type.
 */
abstract class InitialDecl(identifier : Token, declType : Type)
    : Declaration(identifier, declType)
  {
    // Note: Many initial declarations do not require code generation.
    // A default implementation is provided for convenience.

    override fun emit()
      {
        // nothing to emit for most initial declarations
      }
  }
