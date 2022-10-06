package edu.citadel.cvm.assembler.ast

import edu.citadel.compiler.Position
import edu.citadel.compiler.ErrorHandler
import edu.citadel.compiler.ConstraintException
import edu.citadel.compiler.util.ByteUtil

import java.io.OutputStream

/**
 * Base class for all abstract syntax trees
 */
abstract class AST
  {
    /**
     * Create a constraint exception with the specified position and message.
     */
    protected fun error(errorPosition : Position, errorMessage : String) : ConstraintException
      {
        return ConstraintException(errorPosition, errorMessage)
      }

    /**
     * Emit the opCode for the instruction.
     */
    protected fun emit(opCode : Byte)
      {
        outputStream.write(opCode.toInt())
      }

    /**
     * Emit an integer argument for the instruction.
     */
    protected fun emit(arg : Int)
      {
        outputStream.write(ByteUtil.intToBytes(arg))
      }

    /**
     * Emit a character argument for the instruction.
     */
    protected fun emit(arg : Char)
      {
        outputStream.write(ByteUtil.charToBytes(arg))
      }

    /**
     * Check semantic/contextual constraints.
     */
    abstract fun checkConstraints()

    /**
     * emit the object code for the AST
     */
    abstract fun emit()

    companion object
      {
        /**
         * The output stream to be used for code generation.
         */
        lateinit var outputStream : OutputStream

        /**
         * The error handler to be used for code generation.
         */
        lateinit var errorHandler : ErrorHandler
      }
  }
