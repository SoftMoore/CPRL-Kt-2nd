package edu.citadel.cvm.assembler.ast

import edu.citadel.cvm.Constants
import edu.citadel.cvm.OpCode
import edu.citadel.cvm.assembler.Symbol
import edu.citadel.cvm.assembler.Token

/**
 * This class implements the abstract syntax tree for the assembly
 * language instruction BL.
 */
class InstructionBL(labels : MutableList<Token>, opCode : Token, arg : Token)
    : InstructionOneArg(labels, opCode, arg)
  {
    override val argSize : Int
        get() = Constants.BYTES_PER_INTEGER

    public override fun assertOpCode()
      {
        assertOpCode(Symbol.BL)
      }

    public override fun checkArgType()
      {
        checkArgType(Symbol.identifier)
        checkLabelArgDefined()
      }

    override fun emit()
      {
        emit(OpCode.BL)
        emit(getDisplacement(arg))
      }
  }
