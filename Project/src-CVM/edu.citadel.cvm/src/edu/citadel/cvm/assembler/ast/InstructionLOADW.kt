package edu.citadel.cvm.assembler.ast

import edu.citadel.cvm.OpCode
import edu.citadel.cvm.assembler.Symbol
import edu.citadel.cvm.assembler.Token

/**
 * This class implements the abstract syntax tree for the assembly
 * language instruction LOADW.
 */
class InstructionLOADW(labels : MutableList<Token>, opCode : Token)
    : InstructionNoArgs(labels, opCode)
  {
    public override fun assertOpCode()
      {
        assertOpCode(Symbol.LOADW)
      }

    override fun emit()
      {
        emit(OpCode.LOADW)
      }
  }
