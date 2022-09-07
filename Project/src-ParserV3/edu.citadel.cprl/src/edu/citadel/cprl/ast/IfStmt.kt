package edu.citadel.cprl.ast


import edu.citadel.compiler.ConstraintException

import edu.citadel.cprl.Type


/**
 * The abstract syntax tree node for an if statement.
 *
 * @property booleanExpr the boolean expression that, if true, will result
 *                       in the execution of the list of "then" statements.
 * @property thenStmt    the statement to be executed when the boolean
 *                       expression evaluates to true.
 * @property elseStmt    the statement to be executed when the boolean
 *                       expression evaluates to false.
 */
class IfStmt(private val booleanExpr : Expression,
             val thenStmt : Statement, val elseStmt : Statement?)
    : Statement()
  {
    // labels used during code generation
    private val L1 : String = getNewLabel()   // label of address at end of then statement
    private val L2 : String = getNewLabel()   // label of address at end of if statement


    override fun checkConstraints()
      {
        try
          {
            booleanExpr.checkConstraints()
            thenStmt.checkConstraints()
            elseStmt?.checkConstraints()

            if (booleanExpr.type != Type.Boolean)
              {
                val errorMsg = "An \"if\" condition should have type Boolean."
                throw error(booleanExpr.position, errorMsg)
              }
          }
        catch (e : ConstraintException)
          {
            errorHandler.reportError(e)
          }
      }


    override fun emit()
      {
        // if expression evaluates to false, branch to L1
        booleanExpr.emitBranch(false, L1)

        thenStmt.emit()

        // if there is an else part, branch to end of if statement
        elseStmt?.let{emit("BR $L2")}

        // L1:
        emitLabel(L1)

        if (elseStmt != null)
          {
            elseStmt.emit()

            // L2:
            emitLabel(L2)
          }
      }
  }
