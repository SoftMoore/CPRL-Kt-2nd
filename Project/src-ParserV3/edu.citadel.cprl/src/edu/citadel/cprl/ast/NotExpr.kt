package edu.citadel.cprl.ast

import edu.citadel.compiler.ConstraintException

import edu.citadel.cprl.Symbol
import edu.citadel.cprl.Token
import edu.citadel.cprl.Type

/**
 * The abstract syntax tree node for a not expression.  A not expression is a unary
 * expression of the form "not expr".  A simple example would be "not isEmpty()".
 *
 * @constructor Construct a not expression with the specified operator ("not") and operand.
 */
class NotExpr(operator : Token, operand : Expression) : UnaryExpr(operator, operand)
  {
    init
      {
        type = Type.Boolean
        assert(operator.symbol == Symbol.notRW)
          { "Operator is not the reserved word \"not\"." }
      }

    override fun checkConstraints()
      {
// ...
      }

    override fun emit()
      {
// ...
      }
  }
