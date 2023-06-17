package edu.citadel.assembler.ast

import edu.citadel.cvm.Constants
import edu.citadel.cvm.Opcode
import edu.citadel.assembler.Symbol
import edu.citadel.assembler.Token

/**
 * This class implements the abstract syntax tree for the assembly
 * language instruction BGE.
 */
class InstructionBGE(labels : MutableList<Token>, opcode : Token, arg : Token)
    : InstructionOneArg(labels, opcode, arg)
  {
    override val argSize : Int
        get() = Constants.BYTES_PER_INTEGER

    override fun assertOpcode() = assertOpcode(Symbol.BGE)

    override fun checkArgType()
      {
        checkArgType(Symbol.identifier)
        checkLabelArgDefined()
      }

    override fun emit()
      {
        emit(Opcode.BGE)
        emit(getDisplacement(arg))
      }
  }
