package edu.citadel.cprl.ast


import edu.citadel.compiler.ConstraintException
import edu.citadel.compiler.Position

import edu.citadel.cprl.Type
import edu.citadel.cprl.ArrayType
import edu.citadel.cprl.RecordType
import edu.citadel.cprl.StringType
import edu.citadel.cprl.ScopeLevel


/**
 * The abstract syntax tree node for a variable, which is any named variable
 * that can appear on the left-hand side of an assignment statement.
 *
 * @constructor Construct a variable with a reference to its declaration,
 *              its position, and a list of selector expressions.
 */
open class Variable(val decl : VariableDecl,   // nonstructural reference
                    position : Position, val selectorExprs : List<Expression>)
    : Expression(decl.type, position)
  {
    /**
     * Construct a variable that corresponds to a variable expression.
     */
    constructor(varExpr : VariableExpr)
        : this(varExpr.decl, varExpr.position, varExpr.selectorExprs)


    override fun checkConstraints()
      {
        try
          {
            assert(decl is SingleVarDecl || decl is ParameterDecl)
              { "Declaration is not a variable." }

            for (expr in selectorExprs)
              {
                expr.checkConstraints()

                // Each selector expression must correspond to
                // an array type, a record type, or a string type.
                if (type is ArrayType)
                  {
                    // Applying the selector effectively changes the
                    // variable's type to the element type of the array
                    val arrayType = type as ArrayType
                    type = arrayType.elementType

                    // check that the selector expression is not a field expression
// ...

                    // check that the type of the index expression is Integer
// ...
                  }
                else if (type is RecordType)
                  {
                    // check that the selector expression is a field expression
// ...

                    // Applying the selector effectively changes the
                    // variable's type to the type of the field.
                    val recType   = type as RecordType
                    val fieldId   = expr.fieldId
                    val fieldDecl = recType[fieldId.text]
                    if (fieldDecl != null)
                      {
                        expr.fieldDecl = fieldDecl
                        type = fieldDecl.type
                      }
                    else
                      {
                        val errorMsg = "\"${fieldId.text}\" is not a " +
                                       "valid field name for $recType."
                        throw error(fieldId.position, errorMsg)
                      }
                  }
                else if (type is StringType)
                  {
                    // A string can have both a field expression for length (always
                    // at offset 0) and an index expression for the characters.

                    if (expr is FieldExpr)
                      {
                        // Applying length field selector effectively changes the
                        // variable's type to Integer.
                        type = Type.Integer

                        // check that the field identifier is "length"
                        val fieldId = expr.fieldId
                        if (fieldId.text != "length")
                          {
                            val errorMsg = "Field name must be \"length\" for strings."
                            throw error(fieldId.position, errorMsg)
                          }
                      }
                    else
                      {
                        // Applying an index selector effectively changes the
                        // variable's type to Char.
                        type = Type.Char

                        // must be an index expression; check that the type is Integer
                        if (expr.type != Type.Integer)
                          {
                            val errorMsg = "Index expression must have type Integer."
                            throw error(expr.position, errorMsg)
                          }
                      }
                  }
                else
                  {
                    val errorMsg = "Selector expression not allowed; " +
                                   "not an array, record, or string."
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
        if (decl is ParameterDecl && decl.isVarParam)
          {
            // address of actual parameter is value of var parameter
            emit("LDLADDR ${decl.relAddr}")
            emit("LOADW")
          }
        else if (decl.scopeLevel == ScopeLevel.GLOBAL)
            emit("LDGADDR ${decl.relAddr}")
        else
            emit("LDLADDR ${decl.relAddr}")

        // For an array, record, or string, at this point the base address of the array
        // record, or string is on the top of the stack.  We need to replace it by the
        // sum: base address + offset
        var type = decl.type

        for (expr in selectorExprs)
          {
            if (type is ArrayType)
              {
                expr.emit()   // emit the index

                // multiply by size of array base type to get offset
// ...

                // Note: No code to perform bounds checking for the index to
                // ensure that the index is >= 0 and < number of elements.

                // add offset to the base address
                emit("ADD")

                type = type.elementType
              }
            else if (type is RecordType)
              {
                expr as FieldExpr

                if (expr.fieldDecl.offset != 0)
                  {
                    // add offset to the base address
// ...
                  }

                type = expr.fieldDecl.type
              }
            else if (type is StringType)
              {
                if (expr is FieldExpr)
                  {
                    // The only allowed field expression for strings is length, which
                    // is at offset 0; we don't need to emit code for the offset.
                  }
                else   // selector expression must be an index expression
                  {
                    // skip over length (type Integer)
                    emit("LDCINT ${Type.Integer.size}")
                    emit("ADD")

                    expr.emit()   // emit offset

                    // multiply by size of type Char to get offset
                    emit("LDCINT ${Type.Char.size}")
                    emit("MUL")

                    // add offset to the base address
                    emit("ADD")

                    // Note: No code to perform bounds checking for the index to
                    // ensure that the index is >= 0 and < string capacity.
                  }
              }
          }
      }
  }
