package edu.citadel.assembler.ast

import edu.citadel.compiler.ConstraintException
import edu.citadel.assembler.optimize.Optimizations

/**
 * This class implements the abstract syntax tree for an assembly language program.
 */
class Program : AST()
  {
    private val instructions : ArrayList<Instruction> = ArrayList()

    fun addInstruction(inst : Instruction) = instructions.add(inst)

    fun getInstructions() : List<Instruction> = instructions

    override fun checkConstraints()
      {
        for (inst in instructions)
            inst.checkConstraints()
      }

    /**
     * Perform code transformations that improve performance.  This method is normally
     * called after parsing and before setAddresses(), checkConstraints() and emit().
     */
    fun optimize()
      {
        val opts = Optimizations()

        // Note: Instructions.size can change during optimizations
        //       so a Kotlin for loop will not work here.
        var n = 0
        while (n < instructions.size)
          {
            for (optimization in opts.optimizations)
                optimization.optimize(instructions, n)
            ++n
          }
      }

    /**
     * Sets the starting memory address for each instruction and defines label
     * addresses.  Note: This method should be called after optimizations have
     * been performed and immediately before code generation.
     */
    fun setAddresses()
      {
        // the starting address for the first instruction
        var address = 0

        for (inst in instructions)
          {
            try
              {
                inst.address = address
                address += inst.size
              }
            catch (e : ConstraintException)
              {
                errorHandler.reportError(e)
              }
          }
      }

    override fun emit()
      {
        for (inst in instructions)
            inst.emit()
      }

    override fun toString() : String
      {
        val buffer = StringBuffer(1000)

        for (inst in instructions)
          {
            buffer.append(inst.toString())
                  .append("\n")
          }

        return buffer.toString()
      }
  }
