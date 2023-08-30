package edu.citadel.cprl.ast

import edu.citadel.compiler.CodeGenException
import edu.citadel.compiler.ConstraintException

import edu.citadel.cprl.Symbol
import edu.citadel.cprl.Token
import edu.citadel.cprl.Type
import edu.citadel.cprl.StringType

/**
 * The abstract syntax tree node for a constant value expression, which is
 * either a literal or a declared constant identifier representing a literal.
 */
class ConstValue : Expression
  {
    /**
     * The literal token for the constant.  If the constant value is
     * created from a constant declaration, then the literal is the
     * one contained in the declaration.
     */
    private var literal : Token

    // constant declaration containing the constant value
    private val decl : ConstDecl?   // nonstructural reference

    /**
     * Construct a constant value from a literal token.
     */
    constructor(literal : Token) : super(Type.typeOf(literal), literal.position)
      {
        this.literal = literal
        this.decl    = null
      }

    /**
     * Construct a constant value from a constant identifier
     * token and its corresponding constant declaration.
     */
    constructor(identifier : Token, decl : ConstDecl)
        : super(decl.type, identifier.position)
      {
        this.literal = decl.literal
        this.decl    = decl
      }

    /**
     * An integer value for the declaration literal.  For an integer literal,
     * this property simply returns its integer value.  For a char literal,
     * this property returns the underlying integer value for the character.
     * For a boolean literal, this property returns 0 for false and 1 for true.
     * For any other literal, the property returns 0.
     */
    val literalIntValue : Int
        get()
          {
            if (literal.symbol == Symbol.intLiteral)
                return Integer.parseInt(literal.text)
            else if (literal.symbol == Symbol.trueRW)
                return 1
            else if (literal.symbol == Symbol.charLiteral)
              {
                val ch = literal.text[1]
                return ch.code
              }
            else
                return 0
          }

    override fun checkConstraints()
      {
        try
          {
            // Check that an integer literal can be converted to an integer.
            // Check is not required for literal in constant declaration.
            if (literal.symbol == Symbol.intLiteral && decl == null)
              {
                try
                  {
                    Integer.parseInt(literal.text)
                  }
                catch (e : NumberFormatException)
                  {
                    val errorMsg = "The number \"${literal.text}\" cannot " +
                                   "be converted to an integer in CPRL."
                    throw error(literal.position, errorMsg)
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
        when (type)
          {
            Type.Integer  -> emit("LDCINT ${literalIntValue}")
            Type.Boolean  -> emit("LDCB ${literalIntValue}")
            Type.Char     -> emit("LDCCH ${literal.text}")
            is StringType -> emit("LDCSTR ${literal.text}")
            else ->
              {
                val errorMsg = "Invalid type for constant value."
                throw CodeGenException(literal.position, errorMsg)
              }
          }
      }
  }
