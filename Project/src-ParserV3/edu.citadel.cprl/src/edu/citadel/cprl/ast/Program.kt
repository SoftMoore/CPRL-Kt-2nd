package edu.citadel.cprl.ast

import edu.citadel.compiler.ConstraintException

/**
 * The abstract syntax tree node for a CPRL program.
 *
 * @constructor Construct a program with the specified initial declarations
 *              and subprogram declarations.
 */
class Program(private val initialDecls    : List<InitialDecl>    = emptyList(),
              private val subprogramDecls : List<SubprogramDecl> = emptyList())
    : AST()
  {
    private var varLength = 0      // # bytes of all declared variables

    override fun checkConstraints()
      {
        try
          {
            for (decl in initialDecls)
                decl.checkConstraints()

            for (decl in subprogramDecls)
                decl.checkConstraints()

            // check procedure main
            val decl = idTable["main"]
            if (decl == null)
                throw error("Program is missing procedure \"main()\".")
            else if (decl !is ProcedureDecl)
                throw error(decl.position, "Identifier \"main\" was not declared as a procedure.")
            else if (decl.paramLength != 0)
                throw error(decl.position, "Procedure \"main\" cannot have parameters.")
          }
        catch (e : ConstraintException)
          {
            errorHandler.reportError(e)
          }
      }

    /**
     * Set the relative address (offset) for each variable
     * and compute the length of all variables.
     */
    private fun setRelativeAddresses()
      {
        // initial relative address is 0 for a program
        var currentAddr = 0

        for (decl in initialDecls)
          {
            if (decl is VarDecl)
              {
                // set relative address for single variable declarations
                for (singleVarDecl in decl.singleVarDecls)
                  {
                    singleVarDecl.relAddr = currentAddr
                    currentAddr = currentAddr + singleVarDecl.size
                 }
              }
          }

        // compute length of all variables
        varLength = currentAddr
      }

    override fun emit()
      {
        setRelativeAddresses()

        // no need to emit PROGRAM instruction if varLength == 0
        if (varLength > 0)
            emit("PROGRAM $varLength")

        for (decl in initialDecls)
            decl.emit()

        emit("CALL _main")
        emit("HALT")

        for (decl in subprogramDecls)
            decl.emit()
      }
  }
