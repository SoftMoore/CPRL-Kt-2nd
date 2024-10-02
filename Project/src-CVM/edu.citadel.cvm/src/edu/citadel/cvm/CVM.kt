package edu.citadel.cvm

import edu.citadel.compiler.util.ByteUtil

import java.io.*
import java.nio.charset.StandardCharsets

import kotlin.system.exitProcess

private const val DEBUG  = false
private const val SUFFIX = ".obj"

// exit return value for failure
private const val FAILURE = -1

// 1K = 2**10
private const val K = 1024

// default memory size for the virtual machine
private const val DEFAULT_MEMORY_SIZE = 16*K

// virtual machine constant for false
private const val FALSE : Byte = 0

// virtual machine constant for true
private const val TRUE : Byte = 1

// end of file
private const val EOF = -1

/**
 * This method constructs a CPRL virtual machine, loads into memory the
 * byte code from the file specified by args[0], and runs the byte code.
 *
 * @throws FileNotFoundException if the file specified in args[0] can't be found.
 */
fun main(args : Array<String>)
  {
    if (args.size != 1)
      {
        System.err.println("Usage: java edu.citadel.cvm.CVM filename")
        exitProcess(FAILURE)   // stop the VM with a nonzero status code
      }

    var filename = args[0]
    var file = File(filename)

    if (!file.isFile)
      {
        // see if we can find the file by appending the suffix
        val index = filename.lastIndexOf('.')
        if (index < 0 || filename.substring(index) != SUFFIX)
          {
            filename += SUFFIX
            file = File(filename)

            if (!file.isFile)
              {
                System.err.println("*** File $filename not found ***")
                exitProcess(FAILURE)
              }
          }
        else
          {
            // don't try to append the suffix
            System.err.println("*** File $filename not found ***")
            exitProcess(FAILURE)
          }
      }

    val codeFile = FileInputStream(file)
    val cvm = CVM(DEFAULT_MEMORY_SIZE)
    cvm.loadProgram(codeFile)
    cvm.run()
  }

/**
 * This class implements a virtual machine for the programming language CPRL.
 * It interprets instructions for a hypothetical CPRL computer.
 *
 * @param numOfBytes the number of bytes in memory of the virtual machine
 */
class CVM(numOfBytes : Int)
  {
    // Reader for handling basic char I/O
    private val reader = InputStreamReader(System.`in`, StandardCharsets.UTF_8)

    // PrintStream for handling output
    private val out = PrintStream(System.out, true, StandardCharsets.UTF_8)

    // computer memory (for the virtual CPRL machine)
    private val memory = ByteArray(numOfBytes)

    // program counter (index of the next instruction in memory)
    private var pc = 0

    // base pointer
    private var bp = 0

    // stack pointer (index of the top of the stack)
    private var sp = 0

    // stack base (bottom of the stack)
    private var sb = 0

    // true if the virtual computer is currently running
    private var running = false

    /**
     * Loads the program into memory.
     *
     * @param codeFile the FileInputStream containing the object code
     */
    fun loadProgram(codeFile : FileInputStream) {
        var address = 0
        var inByte : Int

        try {
            inByte = codeFile.read()
            while (inByte != -1) {
                memory[address++] = inByte.toByte()
                inByte = codeFile.read()
            }

            bp = address
            sb = address
            sp = bp - 1
            codeFile.close()
        }
        catch (ex : IOException)
          {
            error(ex.toString())
          }
    }

    /**
     * Prints values of internal registers to standard output.
     */
    private fun printRegisters() = out.println("PC=$pc, BP=$bp, SB=$sb, SP=$sp")

    /**
     * Prints a view of memory to standard output.
     */
    private fun printMemory() {
        var memAddr = 0
        var byte0 : Byte
        var byte1 : Byte
        var byte2 : Byte
        var byte3 : Byte

        while (memAddr < sb) {
            // Prints "PC ->" in front of the correct memory address
            if (pc == memAddr)
                out.print("PC ->")
            else
                out.print("     ")

            val memAddrStr = String.format("%4s", memAddr)
            val opcode = Opcode.toOpcode(memory[memAddr])

            if (opcode == null)
                error("*** PrintMemory: Unknown opcode ${memory[memAddr]} at memory address $memAddr ***")
            else if (opcode.isZeroOperandOpcode())
              {
                out.println("$memAddrStr:  $opcode")
                ++memAddr
              }
            else if (opcode.isByteOperandOpcode())
              {
                out.print("$memAddrStr:  $opcode")
                ++memAddr
                out.println(" ${memory[memAddr++]}")
              }
            else if (opcode.isIntOperandOpcode())
              {
                out.print("$memAddrStr:  $opcode")
                ++memAddr
                byte0 = memory[memAddr++]
                byte1 = memory[memAddr++]
                byte2 = memory[memAddr++]
                byte3 = memory[memAddr++]
                out.println(" ${ByteUtil.bytesToInt(byte0, byte1, byte2, byte3)}")
              }
            else if (opcode == Opcode.LDCCH)
              {
                // special case: LDCCH
                out.print("$memAddrStr:  $opcode")
                ++memAddr
                byte0 = memory[memAddr++]
                byte1 = memory[memAddr++]
                out.println(" ${ByteUtil.bytesToChar(byte0, byte1)}")
              }
            else if (opcode == Opcode.LDCSTR)
              {
                // special case: LDCSTR
                out.print("$memAddrStr:  $opcode")
                ++memAddr
                // now print the string
                out.print("  \"")
                byte0 = memory[memAddr++]
                byte1 = memory[memAddr++]
                byte2 = memory[memAddr++]
                byte3 = memory[memAddr++]
                val strLength = ByteUtil.bytesToInt(byte0, byte1, byte2, byte3)
                repeat (strLength)
                  {
                    byte0 = memory[memAddr++]
                    byte1 = memory[memAddr++]
                    out.print(ByteUtil.bytesToChar(byte0, byte1))
                  }
                out.println("\"")
              }
            else
                error("*** PrintMemory: Unknown opcode ${memory[memAddr]} at memory address $memAddr ***")
        }

        // now print remaining values that compose the stack
        memAddr = sb
        while (memAddr <= sp) {
            // Prints "SB ->", "BP ->", and "SP ->" in front of the correct memory address
            when {
                sb == memAddr -> out.print("SB ->")
                bp == memAddr -> out.print("BP ->")
                sp == memAddr -> out.print("SP ->")
                else -> out.print("     ")
            }

            val memAddrStr = String.format("%4s", memAddr)
            out.println("$memAddrStr:  ${memory[memAddr]}")
            ++memAddr
        }

        out.println()
    }

    /**
     * Prompt user and wait for user to press the enter key.
     */
    private fun pause() {
        out.println("Press enter to continue...")
        readlnOrNull()
    }

    /**
     * Runs the program currently in memory.
     */
    fun run()
      {
        running = true
        pc = 0

        while (running)
          {
            if (DEBUG)
              {
                printRegisters()
                printMemory()
                pause()
              }

            when (Opcode.toOpcode(fetchByte()))
              {
                Opcode.ADD      -> add()
                Opcode.BITAND   -> bitAnd()
                Opcode.BITOR    -> bitOr()
                Opcode.BITXOR   -> bitXor()
                Opcode.BITNOT   -> bitNot()
                Opcode.ALLOC    -> allocate()
                Opcode.BR       -> branch()
                Opcode.BE       -> branchEqual()
                Opcode.BNE      -> branchNotEqual()
                Opcode.BG       -> branchGreater()
                Opcode.BGE      -> branchGreaterOrEqual()
                Opcode.BL       -> branchLess()
                Opcode.BLE      -> branchLessOrEqual()
                Opcode.BZ       -> branchZero()
                Opcode.BNZ      -> branchNonZero()
                Opcode.BYTE2INT -> byteToInteger()
                Opcode.CALL     -> call()
                Opcode.DEC      -> decrement()
                Opcode.DIV      -> divide()
                Opcode.GETCH    -> getCh()
                Opcode.GETINT   -> getInt()
                Opcode.GETSTR   -> getString()
                Opcode.HALT     -> halt()
                Opcode.INC      -> increment()
                Opcode.INT2BYTE -> intToByte()
                Opcode.LDCB     -> loadConstByte()
                Opcode.LDCB0    -> loadConstByteZero()
                Opcode.LDCB1    -> loadConstByteOne()
                Opcode.LDCCH    -> loadConstCh()
                Opcode.LDCINT   -> loadConstInt()
                Opcode.LDCINT0  -> loadConstIntZero()
                Opcode.LDCINT1  -> loadConstIntOne()
                Opcode.LDCSTR   -> loadConstStr()
                Opcode.LDLADDR  -> loadLocalAddress()
                Opcode.LDGADDR  -> loadGlobalAddress()
                Opcode.LOAD     -> load()
                Opcode.LOADB    -> loadByte()
                Opcode.LOAD2B   -> load2Bytes()
                Opcode.LOADW    -> loadWord()
                Opcode.MOD      -> modulo()
                Opcode.MUL      -> multiply()
                Opcode.NEG      -> negate()
                Opcode.NOT      -> not()
                Opcode.PROC     -> procedure()
                Opcode.PROGRAM  -> program()
                Opcode.PUTBYTE  -> putByte()
                Opcode.PUTCH    -> putChar()
                Opcode.PUTEOL   -> putEOL()
                Opcode.PUTINT   -> putInt()
                Opcode.PUTSTR   -> putString()
                Opcode.RET      -> returnInst()
                Opcode.RET0     -> returnZero()
                Opcode.RET4     -> returnFour()
                Opcode.SHL      -> shiftLeft()
                Opcode.SHR      -> shiftRight()
                Opcode.STORE    -> store()
                Opcode.STOREB   -> storeByte()
                Opcode.STORE2B  -> store2Bytes()
                Opcode.STOREW   -> storeWord()
                Opcode.SUB      -> subtract()
                else            -> error("invalid machine instruction")
              }
          }
      }

    /**
     * Print an error message and exit with nonzero status code.
     */
    private fun error(message : String)
      {
        System.err.println(message)
        exitProcess(1)
      }

    /**
     * Pop the top byte off the stack and return its value.
     */
    private fun popByte() : Byte = memory[sp--]

    /**
     * Pop the top character off the stack and return its value.
     */
    private fun popChar() : Char
      {
        val b1 = popByte()
        val b0 = popByte()
        return ByteUtil.bytesToChar(b0, b1)
      }

    /**
     * Pop the top integer off the stack and return its value.
     */
    private fun popInt() : Int
      {
        val b3 = popByte()
        val b2 = popByte()
        val b1 = popByte()
        val b0 = popByte()
        return ByteUtil.bytesToInt(b0, b1, b2, b3)
      }

    /**
     * Push a byte onto the stack.
     */
    private fun pushByte(b : Byte)
      {
        if (++sp >= memory.size)
            error("*** Out of memory ***")
        memory[sp] = b
      }

    /**
     * Push a character onto the stack.
     */
    private fun pushChar(c : Char)
      {
        val bytes = ByteUtil.charToBytes(c)
        pushByte(bytes[0])
        pushByte(bytes[1])
      }

    /**
     * Push an integer onto the stack.
     */
    private fun pushInt(n : Int)
      {
        val bytes = ByteUtil.intToBytes(n)
        pushByte(bytes[0])
        pushByte(bytes[1])
        pushByte(bytes[2])
        pushByte(bytes[3])
      }

    /**
     * Fetch the next instruction/byte from memory.
     */
    private fun fetchByte() : Byte = memory[pc++]

    /**
     * Fetch the next character operand from memory.
     */
    private fun fetchChar() : Char
      {
        val b0 = fetchByte()
        val b1 = fetchByte()
        return ByteUtil.bytesToChar(b0, b1)
      }

    /**
     * Fetch the next integer operand from memory.
     */
    private fun fetchInt() : Int
      {
        val b0 = fetchByte()
        val b1 = fetchByte()
        val b2 = fetchByte()
        val b3 = fetchByte()
        return ByteUtil.bytesToInt(b0, b1, b2, b3)
      }

    /**
     * Returns the character at the specified memory address.
     * Does not alter pc, sp, or bp.
     */
    private fun getCharAtAddr(address : Int) : Char
      {
        val b0 = memory[address + 0]
        val b1 = memory[address + 1]
        return ByteUtil.bytesToChar(b0, b1)
      }

    /**
     * Returns the integer at the specified memory address.
     * Does not alter pc, sp, or bp.
     */
    private fun getIntAtAddr(address : Int) : Int
      {
        val b0 = memory[address + 0]
        val b1 = memory[address + 1]
        val b2 = memory[address + 2]
        val b3 = memory[address + 3]
        return ByteUtil.bytesToInt(b0, b1, b2, b3)
      }

    /**
     * Returns the word at the specified memory address.
     * Does not alter pc, sp, or bp.
     */
    private fun getWordAtAddr(address : Int) : Int = getIntAtAddr(address)

    /**
     * Writes the char value to the specified memory address.
     * Does not alter pc, sp, or bp.
     */
    private fun putCharToAddr(value : Char, address : Int)
      {
        val bytes : ByteArray = ByteUtil.charToBytes(value)
        memory[address + 0] = bytes[0]
        memory[address + 1] = bytes[1]
      }

    /**
     * Writes the integer value to the specified memory address.
     * Does not alter pc, sp, or bp.
     */
    private fun putIntToAddr(value : Int, address : Int)
      {
        val bytes : ByteArray = ByteUtil.intToBytes(value)
        memory[address + 0] = bytes[0]
        memory[address + 1] = bytes[1]
        memory[address + 2] = bytes[2]
        memory[address + 3] = bytes[3]
      }

    /**
     * Writes the word value to the specified memory address.
     * Does not alter pc, sp, or bp.
     */
    private fun putWordToAddr(value : Int, address : Int) = putIntToAddr(value, address)

    //----------------------------------------------------------------------
    // End: internal machine instructions that do NOT correspond to opcodes
    // Start: machine instructions corresponding to opcodes
    //------------------------------------------------------

    private fun add()
      {
        val operand2 = popInt()
        val operand1 = popInt()
        pushInt(operand1 + operand2)
      }

    private fun allocate()
      {
        val numBytes = fetchInt()
        sp = sp + numBytes
        if (sp >= memory.size)
            error("*** Out of memory ***")
      }

    private fun bitAnd()
      {
        val operand2 = popInt()
        val operand1 = popInt()
        pushInt(operand1 and operand2)
      }

    private fun bitOr()
      {
        val operand2 = popInt()
        val operand1 = popInt()
        pushInt(operand1 or operand2)
      }

    private fun bitXor()
      {
        val operand2 = popInt()
        val operand1 = popInt()
        pushInt(operand1 xor operand2)
      }

    private fun bitNot()
      {
        val operand = popInt()
        pushInt(operand.inv())
      }

    private fun branch()
      {
        val displacement = fetchInt()
        pc = pc + displacement
      }

    private fun branchEqual()
      {
        val displacement = fetchInt()
        val operand2 = popInt()
        val operand1 = popInt()

        if (operand1 == operand2)
            pc = pc + displacement
      }

    private fun branchNotEqual()
      {
        val displacement = fetchInt()
        val operand2 = popInt()
        val operand1 = popInt()

        if (operand1 != operand2)
            pc = pc + displacement
      }

    private fun branchGreater()
      {
        val displacement = fetchInt()
        val operand2 = popInt()
        val operand1 = popInt()

        if (operand1 > operand2)
            pc = pc + displacement
      }

    private fun branchGreaterOrEqual()
      {
        val displacement = fetchInt()
        val operand2 = popInt()
        val operand1 = popInt()

        if (operand1 >= operand2)
            pc = pc + displacement
      }

    private fun branchLess()
      {
        val displacement = fetchInt()
        val operand2 = popInt()
        val operand1 = popInt()

        if (operand1 < operand2)
            pc = pc + displacement
      }

    private fun branchLessOrEqual()
      {
        val displacement = fetchInt()
        val operand2 = popInt()
        val operand1 = popInt()

        if (operand1 <= operand2)
            pc = pc + displacement
      }

    private fun branchZero()
      {
        val displacement = fetchInt()
        val value = popByte()

        if (value.toInt() == 0)
            pc = pc + displacement
      }

    private fun branchNonZero()
      {
        val displacement = fetchInt()
        val value = popByte()

        if (value.toInt() != 0)
            pc = pc + displacement
      }

    private fun byteToInteger()
      {
        val b = popByte()
        pushInt(ByteUtil.byteToInt(b))
      }

    private fun call()
      {
        val displacement = fetchInt()

        pushInt(bp)          // dynamic link
        pushInt(pc)          // return address

        // set bp to starting address of new frame
        bp = sp - Constants.BYTES_PER_CONTEXT + 1

        // set pc to first statement of called procedure
        pc = pc + displacement
      }

    private fun decrement()
      {
        val operand = popInt()
        pushInt(operand - 1)
      }

    private fun divide()
      {
        val operand2 = popInt()
        val operand1 = popInt()

        if (operand2 != 0)
            pushInt(operand1/operand2)
        else
            error("*** FAULT:  Divide by zero ***")
      }

    private fun getCh()
      {
        try
          {
            val destAddr : Int = popInt()
            val ch = reader.read()

            if (ch == EOF)
                error("Invalid input: EOF")

            putCharToAddr(ch.toChar(), destAddr)
          }
        catch (ex : IOException)
          {
            error(ex.toString())
          }
      }

    private fun getInt()
      {
        try
          {
            val destAddr : Int = popInt()
            val n : Int = readln().toInt()
            putIntToAddr(n, destAddr)
          }
        catch (ex : Exception)
          {
            error(ex.toString())
          }
      }

    private fun getString()
      {
        try
          {
            var destAddr : Int = popInt()
            val capacity : Int = fetchInt()
            val data     : String = readlnOrNull() ?: ""
            val length   : Int = if (data.length < capacity) data.length else capacity

            putIntToAddr(length, destAddr)
            destAddr = destAddr + Constants.BYTES_PER_INTEGER
            for (i in 0 until length) {
                putCharToAddr(data[i], destAddr)
                destAddr = destAddr + Constants.BYTES_PER_CHAR
            }
          }
        catch (ex : Exception)
          {
            error(ex.toString())
          }
      }

    private fun halt()
      {
        running = false
      }

    private fun increment()
      {
        val operand = popInt()
        pushInt(operand + 1)
      }

    private fun intToByte()
      {
        val n = popInt()
        pushByte(ByteUtil.intToByte(n))
      }

    /**
     * Loads a multibyte variable onto the stack.  The number of bytes
     * is an argument of the instruction, and the address of the
     * variable is obtained by popping it off the top of the stack.
     */
    private fun load()
      {
        val length = fetchInt()
        val address = popInt()

        for (i in 0 until length)
            pushByte(memory[address + i])
      }

    private fun loadConstByte()
      {
        val b = fetchByte()
        pushByte(b)
      }

    private fun loadConstByteZero() = pushByte(0.toByte())

    private fun loadConstByteOne() = pushByte(1.toByte())

    private fun loadConstCh()
      {
        val ch = fetchChar()
        pushChar(ch)
      }

    private fun loadConstInt()
      {
        val value = fetchInt()
        pushInt(value)
      }

    private fun loadConstIntZero() = pushInt(0)

    private fun loadConstIntOne() = pushInt(1)

    private fun loadConstStr()
      {
        val capacity = fetchInt()
        pushInt(capacity)

        // fetch each character and push it onto the stack
        repeat (capacity) { pushChar(fetchChar()) }
      }

    private fun loadLocalAddress()
      {
        val displacement = fetchInt()
        pushInt(bp + displacement)
      }

    private fun loadGlobalAddress()
      {
        val displacement = fetchInt()
        pushInt(sb + displacement)
      }

    /**
     * Loads a single byte onto the stack.  The address of the
     * byte is obtained by popping it off the top of the stack.
     */
    private fun loadByte()
      {
        val address = popInt()
        val b = memory[address]
        pushByte(b)
      }

    /**
     * Loads two bytes onto the stack.  The address of the first
     * byte is obtained by popping it off the top of the stack.
     */
    private fun load2Bytes()
      {
        val address = popInt()
        val b0 = memory[address + 0]
        val b1 = memory[address + 1]
        pushByte(b0)
        pushByte(b1)
      }

    /**
     * Loads a single word-size variable (four bytes) onto the stack.  The address
     * of the variable is obtained by popping it off the top of the stack.
     */
    private fun loadWord()
      {
        val address = popInt()
        val word = getWordAtAddr(address)
        pushInt(word)
      }

    private fun modulo()
      {
        val operand2 = popInt()
        val operand1 = popInt()
        pushInt(operand1%operand2)
      }

    private fun multiply()
      {
        val operand2 = popInt()
        val operand1 = popInt()
        pushInt(operand1*operand2)
      }

    private fun negate()
      {
        val operand1 = popInt()
        pushInt(-operand1)
      }

    private operator fun not()
      {
        val operand = popByte()

        if (operand == FALSE)
            pushByte(TRUE)
        else
            pushByte(FALSE)
      }

    private fun procedure() = allocate()

    private fun program()
      {
        val varLength = fetchInt()

        bp = sb
        sp = bp + varLength - 1

        if (sp >= memory.size)
            error("*** Out of memory ***")
      }

    private fun putChar() = out.print(popChar())

    private fun putByte() = out.print(popByte())

    private fun putInt() = out.print(popInt())

    private fun putEOL() = out.println()

    private fun putString()
      {
        val capacity = fetchInt()

        // number of bytes in the string
        val numBytes = Constants.BYTES_PER_INTEGER + capacity*Constants.BYTES_PER_CHAR

        var addr : Int = sp - numBytes + 1      // initialize to starting address of string
        val strLength = getIntAtAddr(addr)
        addr = addr + Constants.BYTES_PER_INTEGER

        repeat (strLength)
          {
            out.print(getCharAtAddr(addr))
            addr = addr + Constants.BYTES_PER_CHAR
          }

        // remove (pop) the string off the stack
        sp = sp - capacity
      }

    private fun returnInst()
      {
        val paramLength = fetchInt()
        pc = getIntAtAddr(bp + Constants.BYTES_PER_INTEGER)
        sp = bp - paramLength - 1
        bp = getIntAtAddr(bp)
      }

    private fun returnZero()
      {
        pc = getIntAtAddr(bp + Constants.BYTES_PER_INTEGER)
        sp = bp - 1
        bp = getIntAtAddr(bp)
      }

    private fun returnFour()
      {
        pc = getIntAtAddr(bp + Constants.BYTES_PER_INTEGER)
        sp = bp - 5
        bp = getIntAtAddr(bp)
      }

    private fun shiftLeft()
      {
        val operand2 = popInt()
        val operand1 = popInt()

        // zero out all except rightmost 5 bits of shiftAmount
        val shiftAmount = operand2 and 31

        pushInt(operand1 shl shiftAmount)
      }

    private fun shiftRight()
      {
        val operand2 = popInt()
        val operand1 = popInt()

        // zero out all except rightmost 5 bits of shiftAmount
        val shiftAmount = operand2 and 31

        pushInt(operand1 shr shiftAmount)
      }

    private fun store()
      {
        val length = fetchInt()
        val destAddr = getIntAtAddr(sp - length - 3)

        // pop bytes of data, storing in reverse order
        for (i in length - 1 downTo 0)
            memory[destAddr + i] = popByte()
        popByte()  // remove destAddr from stack
      }

    private fun storeByte()
      {
        val value = popByte()
        val destAddr = popInt()
        memory[destAddr] = value
      }

    private fun store2Bytes()
      {
        val byte1 = popByte()
        val byte0 = popByte()
        val destAddr = popInt()
        memory[destAddr + 0] = byte0
        memory[destAddr + 1] = byte1
      }

    private fun storeWord()
      {
        val value    = popInt()
        val destAddr = popInt()
        putWordToAddr(value, destAddr)
      }

    private fun subtract()
      {
        val operand2 = popInt()
        val operand1 = popInt()
        val result   = operand1 - operand2
        pushInt(result)
      }
  }
