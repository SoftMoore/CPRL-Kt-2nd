package edu.citadel.assembler.optimize

import edu.citadel.assembler.Symbol
import edu.citadel.assembler.Token
import edu.citadel.assembler.ast.*

/**
 * Replaces RET 0 with RET0 and RET 4 with RET4.
 * IMPORTANT: This optimization should not be performed until after
 * the optimization for dead code elimination.
 */
class ReturnSpecialConstants : Optimization
  {
    override fun optimize(instructions : MutableList<Instruction>, instNum : Int)
      {
        val instruction = instructions[instNum]
        val symbol = instruction.opcode.symbol
        if (symbol == Symbol.RET)
          {
            instruction as InstructionOneArg
            val arg = instruction.arg.text
            val labels : MutableList<Token> = instruction.labels

            if (arg == "0")
              {
                // replace RET 0 with RET0
                val retToken = Token(Symbol.RET0)
                val retInst = InstructionRET0(labels, retToken)
                instructions[instNum] = retInst
              }
            else if (arg == "4")
              {
                // replace RET 4 with RET4
                val retToken = Token(Symbol.RET4)
                val retInst = InstructionRET4(labels, retToken)
                instructions[instNum] = retInst
              }
          }
      }
  }
