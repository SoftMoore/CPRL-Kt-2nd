package edu.citadel.cprl.ast

import edu.citadel.compiler.ConstraintException

import edu.citadel.cprl.Token
import edu.citadel.cprl.Type
import edu.citadel.cprl.StringType

/**
 * The abstract syntax tree node for a string type declaration.
 *
 * @constructor Construct a string type declaration with the specified
 *              type name and capacity.
 *
 * @param typeId the identifier token containing the string type name
 * @property capacity the maximum number of characters in the string
 */
class StringTypeDecl(typeId : Token, val capacity : ConstValue)
    : InitialDecl(typeId, StringType(typeId.text, capacity.literalIntValue))
  {
    override fun checkConstraints()
      {
// ...
      }
  }
