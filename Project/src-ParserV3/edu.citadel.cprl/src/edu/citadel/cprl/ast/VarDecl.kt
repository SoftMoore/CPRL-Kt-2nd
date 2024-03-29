package edu.citadel.cprl.ast

import edu.citadel.compiler.ConstraintException

import edu.citadel.cprl.ScopeLevel
import edu.citadel.cprl.Token
import edu.citadel.cprl.Type

/**
 * The abstract syntax tree node for a variable declaration.  Note that a
 * variable declaration is simply a container for a list of single variable
 * declarations (SingleVarDecls).
 *
 * @constructor Construct a variable declaration with its list of identifier
 *              tokens, type, initial value, and scope level.
 */
class VarDecl(identifiers : List<Token>, varType : Type,
              private val initialValue : ConstValue?, scopeLevel : ScopeLevel)
    : InitialDecl(Token(), varType)
  {
    // the list of single variable declarations for this variable declaration
    val singleVarDecls = ArrayList<SingleVarDecl>(identifiers.size)

    init
      {
        for (id in identifiers)
            singleVarDecls.add(SingleVarDecl(id, varType, initialValue, scopeLevel))
      }

    override fun checkConstraints()
      {
        try
          {
            for (singleVarDecl in singleVarDecls)
                singleVarDecl.checkConstraints()

            if (initialValue != null && !matchTypes(type, initialValue))
              {
                val errorMsg = "Type mismatch for variable initialization."
                throw error(initialValue.position, errorMsg)
              }
          }
        catch (e : ConstraintException)
          {
            errorHandler.reportError(e)
          }
      }

    override fun emit()
      {
        for (singleVarDecl in singleVarDecls)
            singleVarDecl.emit()
      }
  }
