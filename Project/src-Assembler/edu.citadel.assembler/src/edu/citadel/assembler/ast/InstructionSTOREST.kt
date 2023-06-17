package edu.citadel.assembler.ast

import edu.citadel.cvm.Opcode
import edu.citadel.assembler.Symbol
import edu.citadel.assembler.Token

/**
 * This class implements the abstract syntax tree for the assembly
 * language instruction STOREST.
 */
class InstructionSTOREST(labels : MutableList<Token>, opcode : Token)
    : InstructionNoArgs(labels, opcode)
  {
    override fun assertOpcode() = assertOpcode(Symbol.STOREST)

    override fun emit() = emit(Opcode.STOREST)
  }
