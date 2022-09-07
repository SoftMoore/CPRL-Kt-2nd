package edu.citadel.cprl.ast


import edu.citadel.compiler.ConstraintException

import edu.citadel.cprl.StringType
import edu.citadel.cprl.Type


/**
 * This class implements both write and writeln statements.
 *
 * @constructor Construct an output statement with the list of expressions
 *              and isWriteln flag.
 */
class OutputStmt(private val expressions : List<Expression>,
                 private val isWriteln   : Boolean = false)
    : Statement()
  {
    override fun checkConstraints()
      {
        try
          {
            for (expr in expressions)
              {
                expr.checkConstraints()

                if (expr.type != Type.Integer && expr.type != Type.Boolean
                    && expr.type != Type.Char && expr.type !is StringType)
                {
                  val errorMsg = ("Output supported only for integers, "
                                + "characters, booleans, and strings.")
                  throw error(expr.position, errorMsg)
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
        for (expr in expressions)
          {
            expr.emit()

            when (val exprType = expr.type)
              {
                Type.Integer  -> emit("PUTINT")
                Type.Boolean  -> emit("PUTBYTE")
                Type.Char     -> emit("PUTCH")
                is StringType -> emit("PUTSTR ${exprType.capacity}")
              }
          }

        if (isWriteln)
            emit("PUTEOL")
      }
  }
