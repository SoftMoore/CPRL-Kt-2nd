package edu.citadel.cprl.ast

import edu.citadel.cprl.Token
import edu.citadel.cprl.Type

/**
 * The abstract syntax tree node for a field declaration.
 * A field declaration has the form `x : Integer;`.
 * A record declaration can contain multiple field declarations.
 *
 * @constructor Construct a field declaration with its identifier and type.
 */
class FieldDecl(fieldId : Token, type : Type) : Declaration(fieldId, type)
  {
    var offset = 0    // offset for this field within the record; initialized
                      // to 0 but can be updated during constraint analysis

    /** The size (number of bytes) associated with this field declaration,
     *  which is simply the number of bytes associated with its type. */
    val size: Int = type.size

    override fun checkConstraints()
      {
        assert(type != Type.UNKNOWN
            && type != Type.none
            && type != Type.Address)
            { "Invalid CPRL type in field declaration." }
      }
  }
