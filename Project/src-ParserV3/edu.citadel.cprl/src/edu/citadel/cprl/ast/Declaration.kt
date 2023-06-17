package edu.citadel.cprl.ast

import edu.citadel.compiler.Position

import edu.citadel.cprl.Token
import edu.citadel.cprl.Type

/**
 * Base class for all CPRL declarations (constants, variables, procedures, etc.).
 *
 * @constructor Construct a declaration with its identifier token and type.  If not
 *              specified, the type is initialized to Type.none (e.g. for procedures).
 */
abstract class Declaration(val idToken : Token, var type : Type = Type.none) : AST()
  {
    /**
     * The position of this declaration, which is simply the
     * position of the identifier token.
     */
    val position : Position
        get() = idToken.position

    // Note: Many declarations do not require code generation.
    // A default implementation is provided for convenience.

    override fun emit()
      {
        // nothing to do for most declarations
      }
  }
