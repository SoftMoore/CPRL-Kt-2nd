package edu.citadel.assembler.optimize

import edu.citadel.assembler.Symbol
import edu.citadel.assembler.ast.Instruction

/**
 * If an instruction without labels follows a return or an unconditional
 * branch, then that instruction is unreachable (dead) and can be removed.
 * This optimization should be performed after all other optimizations
 * except ReturnSpecialConstants since this optimization looks specifically
 * for a "RET" symbol.
 */
class DeadCodeElimination : Optimization
  {
    override fun optimize(instructions : MutableList<Instruction>, instNum : Int)
      {
        // quick check that there are at least 2 instructions remaining
        if (instNum > instructions.size - 2)
            return

        val instruction0 = instructions[instNum]
        val symbol0 = instruction0.opcode.symbol

        // Check that symbol0 is either BR or RET.
        if (symbol0 === Symbol.BR || symbol0 === Symbol.RET)
          {
            val instruction1 = instructions[instNum + 1]

            // check that the second instruction does not have any labels
            if (instruction1.labels.isEmpty())
              {
                // We are free to remove the second instruction.
                instructions.removeAt(instNum + 1)
              }
          }
      }
  }
