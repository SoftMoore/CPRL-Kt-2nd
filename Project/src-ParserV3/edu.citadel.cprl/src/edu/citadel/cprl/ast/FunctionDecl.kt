package edu.citadel.cprl.ast

import edu.citadel.compiler.ConstraintException

import edu.citadel.cprl.ArrayType
import edu.citadel.cprl.Token

/**
 * The abstract syntax tree node for a function declaration.
 *
 * @constructor Construct a function declaration with its name (an identifier).
 */
class FunctionDecl(funcId : Token) : SubprogramDecl(funcId)
  {
    /**
     * The relative address of the function return value.
     */
    val relAddr : Int
        get()
          {
            var relAddr = -type.size
            for (decl in formalParams)
                relAddr = relAddr - decl.size
            return relAddr
          }

    override fun checkConstraints()
      {
// ...   call super.checkConstraints() before checking any additional constraints
      }

    /**
     * Returns true if the specified list of statements contains at least one
     * return statement.
     *
     * @param statements  the list of statements to check for a return statement.
     *                    If any of the statements in the list contains nested
     *                    statements (e.g., an if statement, a compound statement,
     *                    or a loop statement), then the nested statements are
     *                    also checked for a return statement.
     */
    private fun hasReturnStmt(statements : List<Statement>) : Boolean
      {
        // Check that we have at least one return statement.
        for (statement in statements)
          {
            if (hasReturnStmt(statement))
                return true
          }

        return false
      }

    /**
     * Returns true if the specified statement is a return statement or contains
     * at least one return statement.
     *
     * @param statement the statement to check for a return statement.  If the
     *                  statement contains nested statements (e.g., an if statement,
     *                  a compound statement, or a loop statement), then the nested
     *                  statements are also checked for a return statement.
     */
    private fun hasReturnStmt(statement : Statement) : Boolean
      {
// ...
      }

    override fun emit()
      {
// ...
      }
  }
