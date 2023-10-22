package edu.citadel.assembler.ast

import edu.citadel.compiler.ConstraintException

import edu.citadel.cvm.Constants
import edu.citadel.assembler.Symbol
import edu.citadel.assembler.Token

/**
 * This abstract class implements common methods for the abstract
 * syntax tree of a single assembly language instruction.
 *
 * @constructor Construct an instruction with a list of labels and an opcode.
 */
abstract class Instruction(val labels : MutableList<Token>, val opcode : Token) : AST()
  {
    /**
     * The address of this instruction.
     */
    var address : Int = 0
        /**
         * Sets the memory address and defines label values for this instruction.
         */
        set(address)
          {
            field = address

            // define addresses for labels
            for (label in labels)
              {
                if (labelMap.containsKey(label.text))
                  {
                    val errorMessage = "This label has already been defined."
                    throw error(label.position, errorMessage)
                  }
                else
                    labelMap[label.text] = Integer.valueOf(address)
              }
          }

    /**
     * Returns the number of bytes in memory occupied by the argument.
     */
    protected abstract val argSize : Int

    /**
     * Returns the number of bytes in memory occupied by the instruction,
     * computed as 1 (for the opcode) plus the sizes of the operands.
     */
    open val size : Int
        get() = Constants.BYTES_PER_OPCODE + argSize

    /**
     * Returns the stack address associated with an identifier.
     */
    protected fun getIdAddress(identifier : Token) : Int = idMap[identifier.text] as Int

    /**
     * Checks that each label has a value defined in the label map.  This method
     * should not be called for an instruction before method setAddress().
     *
     * @throws ConstraintException if the instruction has a label that
     *                             is not defined in the label map.
     */
    protected fun checkLabels()
      {
        for (label in labels)
          {
            if (!labelMap.containsKey(label.text))
              {
                val errorMessage = ("label \"$label.text\" has not been defined.")
                throw ConstraintException(label.position, errorMessage)
              }
          }
      }

    /**
     * Calculates the displacement between an instruction's address and
     * a label (computed as label's address - instruction's address).
     * This method is used by branching and call instructions.
     */
    protected fun getDisplacement(labelArg : Token) : Int
      {
        val labelId = labelArg.text + ":"

        assert(labelMap.containsKey(labelId)) { "Label ${labelArg.text} not found." }

        val labelAddress = labelMap[labelId] as Int
        return labelAddress - (address + size)
      }

    /**
     * Asserts that the opcode token of the instruction has
     * the correct Symbol.  Implemented in each instruction
     * by calling the method assertOpcode(Symbol).
     */
    protected abstract fun assertOpcode()

    protected fun assertOpcode(opcode : Symbol)
      {
        assert(this.opcode.symbol == opcode) { "Wrong opcode." }
      }

    override fun toString() : String
      {
        val buffer = StringBuffer(100)

        // return labels and the instruction
        for (label in labels)
            buffer.append(label.text + "\n")

        buffer.append("   ${opcode.text}")

        return buffer.toString()
      }

    companion object
      {
        /**
         * Maps label text (type String) to an address (type Int).
         * Note that the label text always includes the colon (:) at the end.
         */
        var labelMap = mutableMapOf<String, Int>()

        /**
         * Maps identifier text (type String) to a stack address (type Int).
         */
        var idMap = mutableMapOf<String, Int>()

        /**
         * Initialize maps.  These maps are shared with all instructions,
         * but they must be re-initialized if the assembler is run on more
         * than one file; e.g., via a command like assemble *.asm.
         */
        fun resetMaps()
          {
            labelMap = mutableMapOf()
            idMap    = mutableMapOf()
          }
      }
  }
