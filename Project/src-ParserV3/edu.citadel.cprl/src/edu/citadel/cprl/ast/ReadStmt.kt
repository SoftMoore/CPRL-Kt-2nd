package edu.citadel.cprl.ast


import edu.citadel.compiler.ConstraintException

import edu.citadel.cprl.Type
import edu.citadel.cprl.StringType


/**
 * The abstract syntax tree node for a read statement.
 *
 * @constructor Construct a read statement with the specified variable
 *              for storing the input.
 */
class ReadStmt(private val variable : Variable) : Statement()
  {
    override fun checkConstraints()
      {
        // input is limited to integers, characters, and strings
// ...
      }


    override fun emit()
      {
        variable.emit()

        val type = variable.type
        if (type is StringType)
            emit("GETSTR ${type.capacity}")
        else if (type == Type.Integer)
            emit("GETINT")
        else   // type must be Char
            emit("GETCH")
      }
  }
