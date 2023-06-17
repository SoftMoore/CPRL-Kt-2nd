package edu.citadel.assembler.ast

import edu.citadel.cvm.Constants
import edu.citadel.cvm.Opcode
import edu.citadel.assembler.Symbol
import edu.citadel.assembler.Token

/**
 * This class implements the abstract syntax tree for the assembly
 * language instruction LDCSTR.
 *
 * Note: Only one argument (the string literal) is specified for this instruction
 * in assembly language, but two args are generated for the CVM machine code.
 */
class InstructionLDCSTR(labels : MutableList<Token>, opcode : Token, arg : Token)
    : InstructionOneArg(labels, opcode, arg)
  {
    // need to subtract 2 to handle the opening and closing quotes
    private val strLength : Int
        get() = arg.text.length - 2

    // Note: We must return the size for both the integer arg and
    //       the string arg that will be generated in machine code
    override val argSize : Int
        get() = Constants.BYTES_PER_INTEGER + Constants.BYTES_PER_CHAR*strLength

    override fun assertOpcode() = assertOpcode(Symbol.LDCSTR)

    override fun checkArgType() = checkArgType(Symbol.stringLiteral)

    override fun emit()
      {
        emit(Opcode.LDCSTR)
        emit(strLength)

        val text = arg.text

        // omit opening and closing quotes
        for (i in 1..strLength)
            emit(text[i])
      }
  }
