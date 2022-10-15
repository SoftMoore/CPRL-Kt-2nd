package edu.citadel.cprl.ast

import edu.citadel.compiler.ConstraintException
import edu.citadel.compiler.Position

/**
 * The abstract syntax tree node for an assignment statement.
 *
 * @property variable  the variable on the left side of the assignment symbol
 * @property expr the expression on the right side of the assignment symbol
 * @property assignPosition the position of the assignment symbol (for error reporting)
 */
class AssignmentStmt(private val variable : Variable,
                     private val expr : Expression,
                     private val assignPosition : Position)
    : Statement()
  {
    override fun checkConstraints()
      {
// ...
      }

    override fun emit()
      {
// ...
      }
  }
