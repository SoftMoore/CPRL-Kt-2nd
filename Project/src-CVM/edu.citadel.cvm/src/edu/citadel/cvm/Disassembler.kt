package edu.citadel.cvm

import edu.citadel.compiler.util.ByteUtil

import java.io.*
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess

private const val SUFFIX = ".obj"

/**
 * Translates CVM machine code into CVM assembly language
 * for each object code file named in args.
 */
fun main(args : Array<String>)
  {
    if (args.isEmpty())
        printUsageMessageAndExit()

    for (fileName in args)
      {
        val file = FileInputStream(fileName)

        // get object code file name minus the suffix
        val suffixIndex = fileName.lastIndexOf(SUFFIX)
        val baseName    = fileName.substring(0, suffixIndex)

        val outputFileName = "$baseName.dis.txt"
        val fileWriter = FileWriter(outputFileName, StandardCharsets.UTF_8)
        val out = PrintWriter(BufferedWriter(fileWriter), true)

        println("Disassembling $fileName to $outputFileName")

        var inByte : Int
        var opCodeAddr = 0
        var strLength : Int

        var c : Char

        inByte = file.read()
        while (inByte != -1)
          {
            val opCode = inByte.toByte()
            val opCodeAddrStr = String.format("%4s", opCodeAddr)

            when (opCode)
              {
                // opcodes with zero operands
                OpCode.ADD,     OpCode.DEC,     OpCode.DIV,
                OpCode.GETCH,   OpCode.GETINT,  OpCode.HALT,
                OpCode.LOADB,   OpCode.LOAD2B,  OpCode.LOADW,
                OpCode.LOADSTR, OpCode.LDCB0,   OpCode.LDCB1,
                OpCode.LDCINT0, OpCode.LDCINT1, OpCode.INC,
                OpCode.MOD,     OpCode.MUL,     OpCode.NEG,
                OpCode.NOT,     OpCode.PUTBYTE, OpCode.PUTCH,
                OpCode.PUTINT,  OpCode.PUTEOL,  OpCode.RET0,
                OpCode.RET4,    OpCode.STOREB,  OpCode.STORE2B,
                OpCode.STOREW,  OpCode.STOREST, OpCode.SUB  ->
                  {
                    out.println("$opCodeAddrStr:  ${OpCode.toString(opCode)}")
                    opCodeAddr = opCodeAddr + 1
                  }

                // opcodes with one byte operand
                OpCode.SHL,
                OpCode.SHR,
                OpCode.LDCB  ->
                  {
                    out.print("$opCodeAddrStr:  ${OpCode.toString(opCode)}")
                    out.println(" " + readByte(file))
                    opCodeAddr = opCodeAddr + 2  // one byte for opcode and one byte for shift amount
                  }

                // opcodes with one int operand
                OpCode.ALLOC,   OpCode.BR,      OpCode.BE,
                OpCode.BNE,     OpCode.BG,      OpCode.BGE,
                OpCode.BL,      OpCode.BLE,     OpCode.BZ,
                OpCode.BNZ,     OpCode.CALL,    OpCode.GETSTR,
                OpCode.LOAD,    OpCode.LDCINT,  OpCode.LDLADDR,
                OpCode.LDGADDR, OpCode.PROC,    OpCode.PROGRAM,
                OpCode.PUTSTR,  OpCode.RET,     OpCode.STORE  ->
                  {
                    out.print("$opCodeAddrStr:  ${OpCode.toString(opCode)}")
                    out.println(" " + readInt(file))
                    opCodeAddr = opCodeAddr + 1 + Constants.BYTES_PER_INTEGER
                  }

                // special case:  LDCCH
                OpCode.LDCCH  ->
                  {
                    out.print("$opCodeAddrStr:  ${OpCode.toString(opCode)}")
                    out.print(" \'")

                    c = readChar(file)
                    if (isEscapeChar(c))
                        out.print(getUnescapedChar(c))
                    else
                        out.print(c)

                    out.println("\'")
                    opCodeAddr = opCodeAddr + 1 + Constants.BYTES_PER_CHAR
                  }

                // special case:  LDCSTR
                OpCode.LDCSTR ->
                  {
                    out.print("$opCodeAddrStr:  ${OpCode.toString(opCode)}")

                    // now print the string
                    out.print("  \"")
                    strLength = readInt(file)
                    for (i in 0 until strLength)
                      {
                        c = readChar(file)
                        if (isEscapeChar(c))
                            out.print(getUnescapedChar(c))
                        else
                            out.print(c)
                      }
                    out.println("\"")
                    opCodeAddr = (opCodeAddr + 1 + Constants.BYTES_PER_INTEGER
                                + strLength*Constants.BYTES_PER_CHAR)
                  }
                else -> System.err.println("*** Unknown opCode in file $fileName ***")
              }

            inByte = file.read()
          }

        out.close()
      }
  }

/*
 * Returns true if c is an escaped character.
 */
private fun isEscapeChar(c: Char): Boolean
  {
    return c == '\t' || c == '\n' || c == '\r'
        || c == '\"' || c == '\'' || c == '\\'
  }

/**
 * Unescapes characters.  For example, if the parameter c is a tab,
 * this method will return "\\t"
 *
 * @return the string for an escaped character.
 */
private fun getUnescapedChar(c : Char) : String
  {
    when (c)
      {
        '\b' -> return "\\b"    // backspace
        '\t' -> return "\\t"    // tab
        '\n' -> return "\\n"    // linefeed (a.k.a. newline)
        '\r' -> return "\\r"    // carriage return
        '\"' -> return "\\\""   // double quote
        '\'' -> return "\\\'"   // single quote
        '\\' -> return "\\\\"   // backslash
        else -> return c.toString()
      }
  }

/**
 * Reads an integer argument from the stream.
 */
private fun readInt(iStream : InputStream) : Int
  {
    val b0 = iStream.read().toByte()
    val b1 = iStream.read().toByte()
    val b2 = iStream.read().toByte()
    val b3 = iStream.read().toByte()

    return ByteUtil.bytesToInt(b0, b1, b2, b3)
  }

/**
 * Reads a currentChar argument from the stream.
 */
private fun readChar(iStream : InputStream) : Char
  {
    val b0 = iStream.read().toByte()
    val b1 = iStream.read().toByte()

    return ByteUtil.bytesToChar(b0, b1)
  }

/**
 * Reads a byte argument from the stream.
 */
private fun readByte(iStream : InputStream) : Byte
  {
    return iStream.read().toByte()
  }

private fun printUsageMessageAndExit()
  {
    println("Usage: java edu.citadel.cvm.Disassembler filename")
    println()
    exitProcess(0)
  }
