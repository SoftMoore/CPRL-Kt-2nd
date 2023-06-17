package edu.citadel.assembler.ast

import edu.citadel.cvm.Opcode
import edu.citadel.assembler.Symbol
import edu.citadel.assembler.Token

/**
 * This class implements the abstract syntax tree for the assembly
 * language instruction LDCB0.
 */
class InstructionLDCB0(labels : MutableList<Token>, opcode : Token)
    : InstructionNoArgs(labels, opcode)
  {
    override fun assertOpcode() = assertOpcode(Symbol.LDCB0)

    override fun emit() = emit(Opcode.LDCB0)
  }
