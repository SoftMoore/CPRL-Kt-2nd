package edu.citadel.cprl.ast

/**
 * The abstract syntax tree node for a compound statement.
 *
 * @property statements  the list of statements in the compound statement
 */
class CompoundStmt(val statements : List<Statement>) : Statement()
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
