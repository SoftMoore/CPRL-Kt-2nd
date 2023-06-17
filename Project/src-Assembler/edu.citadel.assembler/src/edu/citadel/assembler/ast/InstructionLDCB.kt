package edu.citadel.assembler.ast

import edu.citadel.cvm.Opcode
import edu.citadel.assembler.Symbol
import edu.citadel.assembler.Token

/**
 * This class implements the abstract syntax tree for the assembly
 * language instruction LDCB.
 */
class InstructionLDCB(labels : MutableList<Token>, opcode : Token, arg : Token)
    : InstructionOneArg(labels, opcode, arg)
  {
    override val argSize : Int
        get() = 1

    override fun assertOpcode() = assertOpcode(Symbol.LDCB)

    override fun checkArgType() = checkArgType(Symbol.intLiteral)

    override fun emit() {
        emit(Opcode.LDCB)
        emit(argToByte())
    }
  }
