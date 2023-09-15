package edu.citadel.assembler.optimize

import edu.citadel.assembler.Symbol
import edu.citadel.assembler.Token
import edu.citadel.assembler.ast.Instruction
import edu.citadel.assembler.ast.InstructionOneArg

/**
 * If an instruction follows a return or an unconditional branch, and if the instruction
 * has no targeted labels, then that instruction is unreachable (dead) and can be removed.
 */

class DeadCodeElimination : Optimization
  {
    private val labelBranchCounts: MutableMap<String, Int> = HashMap()

    override fun optimize(instructions: MutableList<Instruction>, instNum: Int)
      {
        // quick check that there are at least 2 instructions remaining
        if (instNum > instructions.size - 2)
            return

        // When instNum = 1, create a map that maps labels -> n, where n is the number
        // of branch or call instructions that branch to that label.  If a branch
        // instruction gets removed, decrement the corresponding n for its target label.
        // Any instruction that follows a return or an unconditional branch is unreachable
        // if it has no labels OR if the total count of branches to it is zero.
        if (instNum == 1)
          {
            for (inst in instructions)
              {
                if (isBranchOrCallInstruction(inst))
                  {
                    val labelTarget = getLabelTarget(inst as InstructionOneArg)
                    if (labelBranchCounts.containsKey(labelTarget))
                      {
                        // increment the branch count for this label
                        val count = labelBranchCounts[labelTarget]!!
                        labelBranchCounts[labelTarget] = count + 1
                      }
                    else
                        labelBranchCounts[labelTarget] = 1
                  }
              }
          }

        val instruction0 = instructions[instNum]
        val symbol0      = instruction0.opcode.symbol

        // Check that symbol0 is either an unconditional branch or a return instruction.
        if (symbol0 == Symbol.BR || symbol0.isReturnSymbol())
          {
            val instruction1 = instructions[instNum + 1]
            val labels1      = instruction1.labels

            // check that the second instruction does not have any labels
            if (labels1.isEmpty() || getTotalBranchCounts(labels1) == 0)
              {
                if (isBranchOrCallInstruction(instruction1))
                  {
                    // decrement the branch count for this label in this instruction
                    val labelTarget = getLabelTarget(instruction1 as InstructionOneArg)
                    val count = labelBranchCounts[labelTarget]
                    if (count != null)
                        labelBranchCounts[labelTarget] = count - 1
                  }

                // We are free to remove the second instruction.
                instructions.removeAt(instNum + 1)
              }
          }
      }

    private fun getLabelTarget(instruction: InstructionOneArg): String
      {
        assert(isBranchOrCallInstruction(instruction))
          { "invalid branch instruction" }
        val targetLabel = instruction.arg
        assert(targetLabel.symbol == Symbol.identifier)
          { "invalid argument for branch instruction" }
        return targetLabel.text + ":"
      }

    private fun isBranchOrCallInstruction(instruction: Instruction): Boolean
      = instruction.opcode.symbol.isBranchOrCallSymbol()

    private fun Symbol.isBranchOrCallSymbol(): Boolean
      {
        return when (this)
          {
            Symbol.BR, Symbol.BE,  Symbol.BNE, Symbol.BG,  Symbol.BGE,
            Symbol.BL, Symbol.BLE, Symbol.BZ,  Symbol.BNZ, Symbol.CALL -> true
            else -> false
          }
      }

    private fun Symbol.isReturnSymbol(): Boolean
      = this == Symbol.RET || this == Symbol.RET0 || this == Symbol.RET4

    private fun getTotalBranchCounts(labels: List<Token>): Int
      {
        var sum = 0
        for (label in labels)
            sum = sum + labelBranchCounts.getOrDefault(label.text, 0)
        return sum
      }
  }

