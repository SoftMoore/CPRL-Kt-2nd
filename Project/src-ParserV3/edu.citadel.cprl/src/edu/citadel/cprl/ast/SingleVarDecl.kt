package edu.citadel.cprl.ast


import edu.citadel.cprl.ScopeLevel
import edu.citadel.cprl.Token
import edu.citadel.cprl.Type


/**
 * The abstract syntax tree node for a single variable declaration.
 * A single variable declaration has the form `var x : Integer;`
 *
 * Note: A variable declaration where more than one variable is declared
 * is simply a container for multiple single variable declarations.
 *
 * @constructor Construct a single variable declaration with its identifier,
 *              type, initial value, and scope level.
 */
class SingleVarDecl(identifier : Token, varType : Type, private val initialValue : ConstValue?,
                    override val scopeLevel : ScopeLevel)
    : InitialDecl(identifier, varType), VariableDecl
  {
    override var relAddr = 0   // relative address for the variable
                               // introduced by this declaration

    /**
     * The size (number of bytes) associated with this single variable declaration,
     * which is simply the number of bytes associated with its type.
     */
    override val size : Int
        get() = type.size


    override fun checkConstraints()
      {
        // nothing to do for now
      }


    override fun emit()
      {
        if (initialValue != null)
          {
            if (scopeLevel == ScopeLevel.GLOBAL)
                emit("LDGADDR $relAddr")
            else
                emit("LDLADDR $relAddr")

            initialValue.emit()
            emitStoreInst(initialValue.type)
          }
      }
  }
