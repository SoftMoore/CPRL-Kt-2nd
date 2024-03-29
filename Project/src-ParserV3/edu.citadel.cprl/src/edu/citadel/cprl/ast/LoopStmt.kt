package edu.citadel.cprl.ast

import edu.citadel.compiler.ConstraintException
import edu.citadel.cprl.Type

/**
 * The abstract syntax tree node for a loop statement.
 */
class LoopStmt : Statement()
  {
    var whileExpr : Expression? = null
    var statement : Statement = EmptyStatement

    // labels used during code generation
    private val L1 : String = getNewLabel()   // label for start of loop
    private val L2 : String = getNewLabel()   // label for end of loop

    /**
     * Returns the label for the end of the loop statement.
     */
    fun getExitLabel() : String = L2

    override fun checkConstraints()
      {
// ...
      }

    override fun emit()
      {
// ...
      }
  }
