package edu.citadel.cprl.ast


import edu.citadel.cprl.Token
import edu.citadel.cvm.Constants


/**
 * Base class for CPRL procedures and functions.
 *
 * @constructor Construct a subprogram declaration with the specified subprogram identifier.
 */
abstract class SubprogramDecl(subprogramId : Token) : Declaration(subprogramId)
  {
    /** The list of formal parameters for this subprogram. */
    var formalParams : List<ParameterDecl> = emptyList()

    /** The list of initial declarations for this subprogram. */
    var initialDecls : List<InitialDecl> = emptyList()

    /** The list of statements for this subprogram. */
    var statements : List<Statement> = emptyList()

    /** The number of bytes required for all variables in the initial declarations. */
    protected var varLength = 0

    /** The label associated with the first statement of the subprogram. */
    val subprogramLabel : String = "_$subprogramId"

    /** The number of bytes for all parameters. */
    val paramLength : Int
        get()
          {
            var paramLength = 0

            for (decl in formalParams)
                paramLength += decl.size

            return paramLength
          }


    override fun checkConstraints()
      {
        for (paramDecl in formalParams)
            paramDecl.checkConstraints()

        for (decl in initialDecls)
            decl.checkConstraints()

        for (statement in statements)
            statement.checkConstraints()
      }


    /**
     * Set the relative address (offset) for each variable and
     * parameter, and compute the length of all variables.
     */
    protected fun setRelativeAddresses()
      {
        // initial relative address for a subprogram
        var currentAddr : Int = Constants.BYTES_PER_CONTEXT

        for (decl in initialDecls)
          {
            if (decl is VarDecl)
              {
                // set relative address for single variable declarations
                for (singleVarDecl in decl.singleVarDecls)
                  {
                    singleVarDecl.relAddr = currentAddr
                    currentAddr  = currentAddr + singleVarDecl.size
                 }
              }
          }

        // compute length of all variables by subtracting initial relative address
        varLength = currentAddr - Constants.BYTES_PER_CONTEXT

        // set relative address for parameters
        if (formalParams.isNotEmpty())
          {
            // initial relative address for a subprogram parameter
            currentAddr = 0

            // we need to process the parameter declarations in reverse order
            val iter = formalParams.listIterator(formalParams.size)
            while (iter.hasPrevious())
              {
                val decl = iter.previous()
                currentAddr = currentAddr - decl.size
                decl.relAddr = currentAddr
              }
          }
      }
  }
