package edu.citadel.cprl.ast


import edu.citadel.compiler.ConstraintException

import edu.citadel.cprl.ArrayType
import edu.citadel.cprl.StringType
import edu.citadel.cprl.Token


/**
 * The abstract syntax tree node for a function call expression.
 *
 * @constructor Construct a function call expression with the function name
 *              (an identifier token) and the list of actual parameters being
 *              passed as part of the call.
 */
class FunctionCallExpr(private val funId : Token,
                       actualParams : List<Expression>)
    : Expression(funId.position)
  {
    // We need a mutable list since, for var parameters,
    // we have to replace variable expressions by variables
    private val actualParams : MutableList<Expression> = actualParams.toMutableList()

    // declaration of the function being called
    private lateinit var funDecl : FunctionDecl   // nonstructural reference


    override fun checkConstraints()
      {
        try
          {
            // get the declaration for this function call from the identifier table
            val decl = idTable[funId.text]

            if (decl == null)
              {
                val errorMsg = "Function \"$funId\" has not been declared."
                throw error(funId.position, errorMsg)
              }
            else if (decl !is FunctionDecl)
              {
                val errorMsg = "Identifier \"$funId\" was not declared as a function."
                throw error(funId.position, errorMsg)
              }
            else
                funDecl = decl

            // at this point funDecl should not be null
            type = funDecl.type

            val formalParams : List<ParameterDecl> = funDecl.formalParams

            // check that numbers of parameters match
            if (actualParams.size != formalParams.size)
                throw error(funId.position, "Incorrect number of actual parameters.")

            // call checkConstraints for each actual parameter
            for (expr in actualParams)
                expr.checkConstraints()

            for (i in actualParams.indices)
              {
                var expr  : Expression    = actualParams[i]
                val param : ParameterDecl = formalParams[i]

                // check that parameter types match
                if (!matchTypes(param.type, expr))
                    throw error(expr.position, "Parameter type mismatch.")

                // check that string parameters are not literals
                if (expr.type is StringType && expr is ConstValue)
                  {
                    val errorMsg = "String literals can't be passed as parameters."
                    throw error(expr.position, errorMsg)
                  }

                // arrays are passed as var parameters (checked in FunctionDecl)
                if (param.isVarParam && param.type is ArrayType)
                  {
                    // replace variable expression by a variable
                    expr = Variable(expr as VariableExpr)
                    actualParams[i] = expr
                  }
              }
          }
        catch (e : ConstraintException)
          {
          errorHandler.reportError(e)
          }
      }


    override fun emit()
      {
        // Allocate space on the stack for the return value.
        emit("ALLOC ${funDecl.type.size}")

        // emit code for actual parameters
        for (expr in actualParams)
            expr.emit()

        emit("CALL ${funDecl.subprogramLabel}")
      }
  }
