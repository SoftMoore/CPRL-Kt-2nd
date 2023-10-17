package edu.citadel.assembler

import edu.citadel.compiler.ErrorHandler
import edu.citadel.compiler.ParserException
import edu.citadel.assembler.ast.*

import java.util.EnumSet

/**
 * This class uses recursive descent to perform syntax analysis of the source language.
 *
 * @constructor Construct a parser with the specified scanner.
 */
class Parser(private val scanner : Scanner, private val errorHandler : ErrorHandler)
  {
    /** Symbols that can follow an assembly language instruction. */
    private val instructionFollowers : Set<Symbol> = makeInstructionFollowers()

    /**
     * Returns a set of symbols that can follow an instruction.
     */
    private fun makeInstructionFollowers() : Set<Symbol>
      {
        val followers = ArrayList<Symbol>()

        // add all opcodes
        for (symbol in Symbol.values())
          {
            if (symbol.isOpcode)
                followers.add(symbol)
          }

        // add labelId and EOF
        followers.add(Symbol.labelId)
        followers.add(Symbol.EOF)

        return followers.toSet()
      }

    // program = { instruction } .
    fun parseProgram() : Program
      {
        val program = Program()

        try
          {
            // NOTE:  identifier is not a valid starter for an instruction,
            // but we handle it as a special case in order to give better
            // error reporting/recovery when an opcode mnemonic is misspelled.
            var symbol = scanner.symbol
            while (symbol.isOpcode || symbol == Symbol.labelId
                || symbol == Symbol.identifier)
              {
                val instruction = parseInstruction()
                if (instruction != null)
                    program.addInstruction(instruction)
                symbol = scanner.symbol
              }

            matchEOF()
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)
            val followers = EnumSet.of(Symbol.EOF)
            scanner.advanceTo(followers)
          }

        return program
      }

    // instruction = { labelId } opcodeMnemonic [ arg ] .
    private fun parseInstruction() : Instruction?
      {
        try
          {
            val labels = ArrayList<Token>()

            while (scanner.symbol == Symbol.labelId)
              {
                labels.add(scanner.token)
                matchCurrentSymbol()
              }

            if (scanner.symbol == Symbol.EOF)
              {
                // return HALT when a label is followed by EOF
                return makeInstruction(labels, Token(Symbol.HALT), null)
              }
            else
              {
                checkOpcode()
                val opcode = scanner.token
                matchCurrentSymbol()

                var arg : Token? = null
                val numArgs = opcode.symbol.numArgs
                if (numArgs == 1)
                  {
                    arg = scanner.token
                    matchCurrentSymbol()
                  }

                return makeInstruction(labels, opcode, arg)
              }
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)
            scanner.advanceTo(instructionFollowers)
            return null
          }
      }

    private fun makeInstruction(labels : MutableList<Token>, opcode : Token, arg : Token?)
            : Instruction
      {
        checkArgs(opcode, arg)

        return when (opcode.symbol)
          {
            Symbol.HALT     -> InstructionHALT(labels, opcode)
            Symbol.LOAD     -> InstructionLOAD(labels, opcode, arg!!)
            Symbol.LOADB    -> InstructionLOADB(labels, opcode)
            Symbol.LOAD2B   -> InstructionLOAD2B(labels, opcode)
            Symbol.LOADW    -> InstructionLOADW(labels, opcode)
            Symbol.LDCB     -> InstructionLDCB(labels, opcode, arg!!)
            Symbol.LDCB0    -> InstructionLDCB0(labels, opcode)
            Symbol.LDCB1    -> InstructionLDCB1(labels, opcode)
            Symbol.LDCCH    -> InstructionLDCCH(labels, opcode, arg!!)
            Symbol.LDCINT   -> InstructionLDCINT(labels, opcode, arg!!)
            Symbol.LDCINT0  -> InstructionLDCINT0(labels, opcode)
            Symbol.LDCINT1  -> InstructionLDCINT1(labels, opcode)
            Symbol.LDCSTR   -> InstructionLDCSTR(labels, opcode, arg!!)
            Symbol.LDLADDR  -> InstructionLDLADDR(labels, opcode, arg!!)
            Symbol.LDGADDR  -> InstructionLDGADDR(labels, opcode, arg!!)
            Symbol.STORE    -> InstructionSTORE(labels, opcode, arg!!)
            Symbol.STOREB   -> InstructionSTOREB(labels, opcode)
            Symbol.STORE2B  -> InstructionSTORE2B(labels, opcode)
            Symbol.STOREW   -> InstructionSTOREW(labels, opcode)
            Symbol.BR       -> InstructionBR(labels, opcode, arg!!)
            Symbol.BE       -> InstructionBE(labels, opcode, arg!!)
            Symbol.BNE      -> InstructionBNE(labels, opcode, arg!!)
            Symbol.BG       -> InstructionBG(labels, opcode, arg!!)
            Symbol.BGE      -> InstructionBGE(labels, opcode, arg!!)
            Symbol.BL       -> InstructionBL(labels, opcode, arg!!)
            Symbol.BLE      -> InstructionBLE(labels, opcode, arg!!)
            Symbol.BZ       -> InstructionBZ(labels, opcode, arg!!)
            Symbol.BNZ      -> InstructionBNZ(labels, opcode, arg!!)
            Symbol.BYTE2INT -> InstructionBYTE2INT(labels, opcode)
            Symbol.INT2BYTE -> InstructionINT2BYTE(labels, opcode)
            Symbol.NOT      -> InstructionNOT(labels, opcode)
            Symbol.SHL      -> InstructionSHL(labels, opcode, arg!!)
            Symbol.SHR      -> InstructionSHR(labels, opcode, arg!!)
            Symbol.ADD      -> InstructionADD(labels, opcode)
            Symbol.SUB      -> InstructionSUB(labels, opcode)
            Symbol.MUL      -> InstructionMUL(labels, opcode)
            Symbol.DIV      -> InstructionDIV(labels, opcode)
            Symbol.MOD      -> InstructionMOD(labels, opcode)
            Symbol.NEG      -> InstructionNEG(labels, opcode)
            Symbol.INC      -> InstructionINC(labels, opcode)
            Symbol.DEC      -> InstructionDEC(labels, opcode)
            Symbol.GETCH    -> InstructionGETCH(labels, opcode)
            Symbol.GETINT   -> InstructionGETINT(labels, opcode)
            Symbol.GETSTR   -> InstructionGETSTR(labels, opcode, arg!!)
            Symbol.PUTBYTE  -> InstructionPUTBYTE(labels, opcode)
            Symbol.PUTCH    -> InstructionPUTCH(labels, opcode)
            Symbol.PUTINT   -> InstructionPUTINT(labels, opcode)
            Symbol.PUTEOL   -> InstructionPUTEOL(labels, opcode)
            Symbol.PUTSTR   -> InstructionPUTSTR(labels, opcode, arg!!)
            Symbol.PROGRAM  -> InstructionPROGRAM(labels, opcode, arg!!)
            Symbol.PROC     -> InstructionPROC(labels, opcode, arg!!)
            Symbol.CALL     -> InstructionCALL(labels, opcode, arg!!)
            Symbol.RET      -> InstructionRET(labels, opcode, arg!!)
            Symbol.RET0     -> InstructionRET0(labels, opcode)
            Symbol.RET4     -> InstructionRET4(labels, opcode)
            Symbol.ALLOC    -> InstructionALLOC(labels, opcode, arg!!)
            else            -> // force an exception
                               throw IllegalArgumentException("Parser.makeInstruction():"
                                   + " opcode not handled at position ${opcode.position}")
          }
      }

    // utility parsing methods

    private fun checkOpcode()
      {
        if (!scanner.symbol.isOpcode)
          {
            val errorMsg = "Expecting an opcode but found \"${scanner.token}\" instead."
            throw error(errorMsg)
          }
      }

    private fun checkArgs(opcode : Token, arg : Token?)
      {
        val errorPosition = opcode.position
        val symbol = opcode.symbol
        val numArgs = symbol.numArgs

        if (numArgs == 0)
          {
            if (arg != null)
              {
                val errorMsg = "No arguments allowed for this opcode."
                throw ParserException(errorPosition, errorMsg)
              }
          }
        else if (numArgs == 1)
          {
            if (arg == null)
              {
                val errorMsg = "One argument is required for this opcode."
                throw ParserException(errorPosition, errorMsg)
              }
          }
        else
          {
            val errorMsg = "Invalid number of arguments for opcode $opcode."
            throw ParserException(errorPosition, errorMsg)
          }
      }

    private fun matchEOF()
      {
        if (scanner.symbol != Symbol.EOF)
          {
            val errorMsg = "Expecting \"${Symbol.EOF}\" but found " +
                           "\"${scanner.token}\" instead."
            throw error(errorMsg)
          }
      }

    private fun matchCurrentSymbol() = scanner.advance()

    private fun error(message : String) : ParserException
      {
        val errorPos = scanner.position
        return ParserException(errorPos, message)
      }
  }
