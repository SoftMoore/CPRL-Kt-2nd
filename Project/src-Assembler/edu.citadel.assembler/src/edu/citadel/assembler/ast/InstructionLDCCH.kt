package edu.citadel.assembler.ast

import edu.citadel.cvm.Constants
import edu.citadel.cvm.Opcode
import edu.citadel.assembler.Symbol
import edu.citadel.assembler.Token

/**
 * This class implements the abstract syntax tree for the assembly
 * language instruction LDCCH.
 */
class InstructionLDCCH(labels : MutableList<Token>, opcode : Token, arg : Token)
    : InstructionOneArg(labels, opcode, arg)
  {
    override val argSize : Int
        get() = Constants.BYTES_PER_CHAR

    override fun assertOpcode() = assertOpcode(Symbol.LDCCH)

    override fun checkArgType() = checkArgType(Symbol.charLiteral)

    override fun emit()
      {
        val argCH = arg.text[1]
        emit(Opcode.LDCCH)
        emit(argCH)
      }
  }
