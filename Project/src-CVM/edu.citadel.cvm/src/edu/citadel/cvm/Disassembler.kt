package edu.citadel.cvm

import edu.citadel.compiler.util.ByteUtil

import java.io.*
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess

private const val SUFFIX = ".obj"

// exit return value for failure
private const val FAILURE = -1

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
        // get object code file name minus the suffix
        val suffixIndex = fileName.lastIndexOf(SUFFIX)
        if (suffixIndex < 0)
          {
            System.err.println("*** Invalid file name suffix: $fileName ***")
            exitProcess(FAILURE)
          }

        val file = FileInputStream(fileName)

        val baseName = fileName.substring(0, suffixIndex)
        val outputFileName = "$baseName.dis.txt"
        val writer = FileWriter(outputFileName, StandardCharsets.UTF_8)
        val out    = PrintWriter(BufferedWriter(writer), true)

        println("Disassembling $fileName to $outputFileName")

        var opcodeAddr = 0   // first opcode is at address 0
        var c : Char

        var inByte : Int = file.read()
        while (inByte != -1)
          {
            val opcode = Opcode.toOpcode(inByte)
            val opcodeAddrStr = String.format("%4s", opcodeAddr)

            if (opcode == null)
                System.err.println("*** Unknown opcode $inByte in file $fileName ***")
            else if (opcode.isZeroOperandOpcode())
              {
                out.println("$opcodeAddrStr:  $opcode")
                opcodeAddr = opcodeAddr + 1
              }
            else if (opcode.isByteOperandOpcode())
              {
                out.print("$opcodeAddrStr:  $opcode")
                out.println(" " + readByte(file).toUByte())
                opcodeAddr = opcodeAddr + 2   // byte for opcode plus byte for operand
              }
            else if (opcode.isIntOperandOpcode())
              {
                out.print("$opcodeAddrStr:  $opcode")
                out.println(" " + readInt(file))
                opcodeAddr = opcodeAddr + 1 + Constants.BYTES_PER_INTEGER
              }
            else if (opcode == Opcode.LDCCH)
              {
                // special case LDCCH
                out.print("$opcodeAddrStr:  $opcode")
                out.print(" \'")

                c = readChar(file)
                if (isEscapeChar(c))
                    out.print(getUnescapedChar(c))
                else
                    out.print(c)

                out.println("\'")
                opcodeAddr = opcodeAddr + 1 + Constants.BYTES_PER_CHAR
              }
            else if (opcode == Opcode.LDCSTR)
              {
                // special case LDCSTR
                out.print("$opcodeAddrStr:  $opcode")

                // now print the string
                out.print("  \"")
                val strLength : Int = readInt(file)
                for (i in 0 until strLength)
                  {
                    c = readChar(file)
                    if (isEscapeChar(c))
                        out.print(getUnescapedChar(c))
                    else
                        out.print(c)
                  }
                out.println("\"")
                opcodeAddr = (opcodeAddr + 1 + Constants.BYTES_PER_INTEGER
                           + strLength*Constants.BYTES_PER_CHAR)
              }
            else
                System.err.println("*** Unknown opcode in file $fileName ***")

            inByte = file.read()
          }

        out.close()
      }
  }

/*
 * Returns true if c is an escaped character.
 */
private fun isEscapeChar(c : Char) : Boolean
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
    return when (c)
      {
        '\t' -> "\\t"    // tab
        '\n' -> "\\n"    // newline
        '\r' -> "\\r"    // carriage return
        '\"' -> "\\\""   // double quote
        '\'' -> "\\\'"   // single quote
        '\\' -> "\\\\"   // backslash
        else -> c.toString()
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
private fun readByte(iStream : InputStream) : Byte = iStream.read().toByte()

private fun printUsageMessageAndExit()
  {
    println("Usage: Expecting one or more file names ending in \".obj\"")
    println()
    exitProcess(0)
  }
