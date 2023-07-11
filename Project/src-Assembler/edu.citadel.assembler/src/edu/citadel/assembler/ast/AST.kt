package edu.citadel.assembler.ast

import edu.citadel.compiler.ConstraintException
import edu.citadel.compiler.ErrorHandler
import edu.citadel.compiler.Position
import edu.citadel.compiler.util.ByteUtil
import edu.citadel.cvm.Opcode

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
        = ConstraintException(errorPosition, errorMessage)

    /**
     * Emit the instruction opcode.
     */
    protected open fun emit(opcode: Opcode) = out.write(opcode.toInt())

    /**
     * Emit a byte argument for the instruction.
     */
    protected fun emit(arg : Byte) = out.write(arg.toInt())

    /**
     * Emit an integer argument for the instruction.
     */
    protected fun emit(arg : Int) = out.write(ByteUtil.intToBytes(arg))

    /**
     * Emit a character argument for the instruction.
     */
    protected fun emit(arg : Char) = out.write(ByteUtil.charToBytes(arg))

    /**
     * Check semantic/contextual constraints.
     */
    abstract fun checkConstraints()

    /**
     * Emit the object code for the AST.
     */
    abstract fun emit()

    companion object
      {
        /**
         * The output stream to be used for code generation.
         */
        lateinit var out : OutputStream

        /**
         * The error handler to be used for code generation.
         */
        lateinit var errorHandler : ErrorHandler

        /**
         * Initializes static members that are shared with all instructions.
         * The members must be re-initialized each time that the assembler is
         * run on a different file; e.g., via a command like ipAssemble *.asm.
         */
        fun initCompanionObject()
          {
            Instruction.initMaps()
          }
      }
  }
