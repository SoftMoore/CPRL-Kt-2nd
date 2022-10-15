package edu.citadel.cprl.ast

import edu.citadel.compiler.ConstraintException
import edu.citadel.cprl.Token
import edu.citadel.cprl.StringType

/**
 * The abstract syntax tree node for a procedure call statement.
 *
 * @constructor Construct a procedure call statement with the procedure
 *              name (an identifier token) and the list of actual parameters
 *              being passed as part of the call.
 */
class ProcedureCallStmt(private val procId : Token, actualParams : List<Expression>)
    : Statement()
  {
    // We need a mutable list since, for var parameters,
    // we have to replace variable expressions by variables
    private val actualParams : MutableList<Expression> = actualParams.toMutableList()

    // declaration of the procedure being called
    private lateinit var procDecl : ProcedureDecl   // nonstructural reference

    override fun checkConstraints()
      {
// ...
      }

    override fun emit()
      {
// ...
      }
  }
