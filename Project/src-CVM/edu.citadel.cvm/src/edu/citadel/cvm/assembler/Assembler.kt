package edu.citadel.cvm.assembler

import edu.citadel.compiler.ErrorHandler
import edu.citadel.compiler.FatalException
import edu.citadel.compiler.Source

import edu.citadel.cvm.assembler.ast.AST
import edu.citadel.cvm.assembler.ast.Instruction
import edu.citadel.cvm.assembler.ast.Program

import java.io.*
import kotlin.system.exitProcess

private const val SUFFIX  = ".asm"
private const val FAILURE = -1
private const val DEBUG   = false
private var optimize = true

/**
 * Translates the assembly source files named in args to CVM machine
 * code.  Object files have the same name but with a ".obj" suffix.
 */
fun main(args : Array<String>)
  {
    // check args
    if (args.isEmpty())
        printUsageAndExit()

    var startIndex = 0

    if (args[0].startsWith("-opt:"))
      {
        processOption(args[0])
        startIndex = 1
      }

    for (i in startIndex until args.size)
      {
        try
          {
            var fileName = args[i]
            var sourceFile = File(fileName)

            if (!sourceFile.isFile)
              {
                // see if we can find the file by appending the suffix
                val index = fileName.lastIndexOf('.')

                if (index < 0 || fileName.substring(index) != SUFFIX)
                  {
                    fileName += SUFFIX
                    sourceFile = File(fileName)

                    if (!sourceFile.isFile)
                        throw FatalException("*** File $fileName not found ***")
                  }
                else
                  {
                    // don't try to append the suffix
                    throw FatalException("*** File $fileName not found ***")
                  }
              }

            val assembler = Assembler(sourceFile)
            assembler.assemble()
          }
        catch (e : FatalException)
          {
            // report error and continue compiling
            val errorHandler = ErrorHandler()
            errorHandler.reportFatalError(e)
          }

        println()
      }
  }

/**
 * This method is useful for debugging.
 *
 * @param instructions the list of instructions to print
 */
private fun printInstructions(instructions : List<Instruction>)
  {
    println("There are " + instructions.size + " instructions")
    for (instruction in instructions)
        println(instruction)
    println()
  }

private fun printProgressMessage(message : String)
  {
    println(message)
  }

private fun printUsageAndExit()
  {
    println("Usage: Assembler expecting [<option>] and one or more source files")
    println("where the option is omitted or is one of the following:")
    println("-opt:off  Turns off all assembler optimizations")
    println("-opt:on   Turns on all assembler optimizations (default)")
    println()
    exitProcess(0)
  }

private fun processOption(option: String)
  {
    if (option == "-opt:off")
        Assembler.optimize = false
    else if (option == "-opt:on")
        Assembler.optimize = true
    else
        printUsageAndExit()
  }

/**
 * Assembler for the CPRL Virtual Machine.
 *
 * @constructor Construct an assembler with the specified source file.
 */
class Assembler(private val sourceFile : File)
  {
    /**
     * Assembles the source file.  If there are no errors in the source
     * file, the object code is placed in a file with the same base file
     * name as the source file but with a ".obj" suffix.
     *
     * @throws IOException if there are problems reading the source file
     *                     or writing to the target file.
     */
    fun assemble()
      {
        val errorHandler = ErrorHandler()
        val reader  = FileReader(sourceFile, Charsets.UTF_8)
        val source  = Source(reader)
        val scanner = Scanner(source, errorHandler)
        val parser  = Parser(scanner, errorHandler)
        AST.errorHandler = errorHandler
        Instruction.p()

        printProgressMessage("Starting assembly for ${sourceFile.name}")

        // parse source file
        val program: Program = parser.parseProgram()

        if (DEBUG)
          {
            println("...program after parsing")
            printInstructions(program.getInstructions())
          }

        // optimize
        if (!errorHandler.errorsExist() && optimize)
          {
            printProgressMessage("...performing optimizations")
            program.optimize()
          }

        if (DEBUG)
          {
            println("...program after performing optimizations")
            printInstructions(program.getInstructions())
          }

        // set addresses
        if (!errorHandler.errorsExist())
          {
            printProgressMessage("...setting memory addresses")
            program.setAddresses()
          }

        // check constraints
        if (!errorHandler.errorsExist())
          {
            printProgressMessage("...checking constraints")
            program.checkConstraints()
          }

        if (DEBUG)
          {
            println("...program after checking constraints")
            printInstructions(program.getInstructions())
          }

        // generate code
        if (!errorHandler.errorsExist())
          {
            printProgressMessage("...generating code")
            AST.outputStream = getTargetOutputStream(sourceFile)

            // no error recovery from errors detected during code generation
            program.emit()
          }

        if (errorHandler.errorsExist())
            errorHandler.printMessage("*** Errors detected in ${sourceFile.name} " +
                                      "-- assembly terminated. ***")
        else
            printProgressMessage("Assembly complete.")
      }

    private fun getTargetOutputStream(sourceFile : File): OutputStream
      {
        // get source file name minus the suffix
        var baseName = sourceFile.name
        val suffixIndex = baseName.lastIndexOf(SUFFIX)
        if (suffixIndex > 0)
            baseName = sourceFile.name.substring(0, suffixIndex)

        val targetFileName = "$baseName.obj"

        try
          {
            val targetFile = File(sourceFile.parent, targetFileName)
            return FileOutputStream(targetFile)
          }
        catch (e : IOException)
          {
            e.printStackTrace()
            exitProcess(FAILURE)
          }
      }
  }
