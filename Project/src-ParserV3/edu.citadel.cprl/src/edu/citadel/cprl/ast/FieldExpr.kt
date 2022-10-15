package edu.citadel.cprl.ast

import edu.citadel.cprl.Token
import edu.citadel.cprl.Type

/**
 * The abstract syntax tree node for a field expression.  The value of a field
 * expression is simply the value of the offset of the field within the record.
 *
 * @constructor Construct a field expression with its field name.
 */
class FieldExpr(val fieldId : Token) : Expression(Type.Integer, fieldId.position)
  {
    // Note: value for fieldDecl is assigned in Variable.checkConstraints()
    lateinit var fieldDecl : FieldDecl   // nonstructural reference

    override fun checkConstraints()
      {
        // nothing to do for now
      }

    override fun emit()
      {
        assert(fieldDecl.offset >= 0) {"Invalid value for field offset."}
// ...
      }
  }
