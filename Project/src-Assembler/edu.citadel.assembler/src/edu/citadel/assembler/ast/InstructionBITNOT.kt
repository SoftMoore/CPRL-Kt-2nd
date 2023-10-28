package edu.citadel.assembler.ast

import edu.citadel.assembler.Symbol
import edu.citadel.assembler.Token
import edu.citadel.cvm.Opcode
import java.io.IOException

/**
 * This class implements the abstract syntax tree for the assembly
 * language instruction BITNOT.
 */
class InstructionBITNOT(labels : MutableList<Token>, opcode : Token)
    : InstructionNoArgs(labels, opcode)
  {
    override fun assertOpcode() = assertOpcode(Symbol.BITNOT)

    override fun emit() = emit(Opcode.BITNOT)
  }
