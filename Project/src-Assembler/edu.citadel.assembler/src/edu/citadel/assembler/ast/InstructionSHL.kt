package edu.citadel.assembler.ast

import edu.citadel.cvm.Opcode
import edu.citadel.assembler.Symbol
import edu.citadel.assembler.Token

/**
 * This class implements the abstract syntax tree for the assembly
 * language instruction SHL.
 */
class InstructionSHL(labels : MutableList<Token>, opcode : Token, arg : Token)
    : InstructionOneArg(labels, opcode, arg)
  {
    override val argSize : Int
        get() = 1   // 1 byte

    override fun assertOpcode() = assertOpcode(Symbol.SHL)

    override fun checkArgType()
      {
        checkArgType(Symbol.intLiteral)

        // check that the value is in the range 0..31
        val argValue = argToInt()
        if (argValue < 0 || argValue > 31)
          {
            val errorMsg = "Shift amount must be be in the range 0..31"
            throw error(arg.position, errorMsg)
          }
      }

    override fun emit()
      {
        emit(Opcode.SHL)
        emit(argToByte())
      }
  }
