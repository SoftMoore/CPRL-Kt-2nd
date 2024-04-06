package edu.citadel.cprl.ast

import edu.citadel.cprl.Token

/**
 * The abstract syntax tree node for a procedure declaration.
 *
 * @constructor Construct a procedure declaration with its name (an identifier).
 */
class ProcedureDecl(procId : Token) : SubprogramDecl(procId)
  {
    // inherited checkConstraints() is sufficient

    override fun emit()
      {
        setRelativeAddresses()

        emitLabel(subprogramLabel)

        // no need to emit PROC instruction if varLength == 0
        if (varLength > 0)
            emit("PROC $varLength")

        for (decl in initialDecls)
            decl.emit()

        for (statement in statements)
            statement.emit()

        emit("RET $paramLength")
      }
  }
