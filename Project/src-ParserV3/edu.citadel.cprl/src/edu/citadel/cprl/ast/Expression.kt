package edu.citadel.cprl.ast

import edu.citadel.compiler.Position
import edu.citadel.cprl.Type

const val FALSE = "0"
const val TRUE  = "1"

/**
 * Base class for all CPRL expressions.
 *
 * @constructor Construct an expression with the specified type and position.
 */
abstract class Expression(var type : Type, val position : Position) : AST()
  {
    /**
     * Construct an expression with the specified position.  Initializes
     * the type of the expression to UNKNOWN.
     */
    constructor(position : Position) : this(Type.UNKNOWN, position)

    /**
     * For Boolean expressions, this method emits the appropriate branch opcode
     * based on the condition.  For example, if the expression is a "<"
     * relational expression and the condition is false, then the opcode "BGE"
     * is emitted.  The method defined in this class works correctly for most
     * Boolean expressions, but it should be overridden for relational expressions.
     *
     * @param condition the condition that determines the branch to be emitted.
     * @param label     the label for the branch destination.
     */
    open fun emitBranch(condition : Boolean, label : String)
      {
        // default behavior unless overridden; correct for constants and variable expressions
        assert(type == Type.Boolean) { "Expression type is not Boolean." }
        emit()  // leaves boolean expression value on top of stack
        emit(if (condition) "BNZ $label" else "BZ $label")
      }
  }
