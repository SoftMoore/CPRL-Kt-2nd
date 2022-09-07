package edu.citadel.cvm


import edu.citadel.compiler.util.ByteUtil
import edu.citadel.compiler.util.format

import java.io.*
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess


private const val DEBUG = false

/** exit return value for failure */
private const val FAILURE = -1

/** 1K = 2**10 */
private const val K = 1024

/** default memory size for the virtual machine */
private const val DEFAULT_MEMORY_SIZE = 8*K

/** virtual machine constant for false */
private const val FALSE = 0.toByte()

/** virtual machine constant for true */
private const val TRUE = 1.toByte()

/** end of file  */
private const val EOF = -1

/** field width for printing memory addresses */
private const val FIELD_WIDTH = 4

/**
 * This method constructs a CPRL virtual machine, loads into memory the
 * byte code from the file specified by args[0], and runs the byte code.
 *
 * Usage: java CVM filename
 *
 * where filename is the name of a file containing the byte code
 * for a CPRL program.
 */
fun main(args : Array<String>)
  {
    if (args.size != 1)
      {
        System.err.println("Usage: java edu.citadel.cvm.CVM filename")
        exitProcess(FAILURE)   // stop the VM with a nonzero status code
      }

    val sourceFile = File(args[0])

    if (!sourceFile.isFile)
      {
        System.err.println("*** File " + args[0] + " not found ***")
        exitProcess(FAILURE)
      }

    val codeFile = FileInputStream(sourceFile)

    val vm = CVM(DEFAULT_MEMORY_SIZE)
    vm.loadProgram(codeFile)
    vm.run()
  }


/**
 * This class implements a virtual machine for the programming language CPRL.
 * It interprets instructions for a hypothetical CPRL computer.
 *
 * @param numOfBytes the number of bytes in memory of the virtual machine
 */
class CVM (numOfBytes : Int)
  {
    /** Reader for handling basic char I/O */
    private val reader = InputStreamReader(System.`in`, StandardCharsets.UTF_8)

    /** PrintStream for handling output */
    private val out = PrintStream(System.out, true, StandardCharsets.UTF_8)

    /** computer memory (for the virtual CPRL machine) */
    private val memory = ByteArray(numOfBytes)

    /** program counter (index of the next instruction in memory) */
    private var pc = 0

    /** base pointer */
    private var bp = 0

    /** stack pointer (index of the top of the stack) */
    private var sp = 0

    /** stack base (bottom of the stack) */
    private var sb = 0

    /** true if the virtual computer is currently running */
    private var running = false


    /**
     * Loads the program into memory.
     *
     * @param codeFile the FileInputStream containing the object code
     */
    fun loadProgram(codeFile : FileInputStream)
      {
        var address = 0
        var inByte : Int

        try
          {
            inByte = codeFile.read()
            while (inByte != -1)
            {
              memory[address++] = inByte.toByte()
              inByte = codeFile.read()
            }

            bp = address
            sb = address
            sp = bp - 1
            codeFile.close()
          }
        catch (e : IOException)
          {
            error(e.toString())
          }
      }


    /**
     * Prints values of internal registers to standard output.
     */
    private fun printRegisters() = out.println("PC=$pc, BP=$bp, SB=$sb, SP=$sp")


    /**
     * Prints a view of memory to standard output.
     */
    private fun printMemory()
      {
        var memAddr = 0
        var strLength : Int
        var byte0: Byte
        var byte1: Byte
        var byte2: Byte
        var byte3: Byte

        while (memAddr < sb)
          {
            // Prints "PC ->" in front of the correct memory address
            if (pc == memAddr)
                out.print("PC ->")
            else
                out.print("     ")

            val memAddrStr = format(memAddr, FIELD_WIDTH)

            when (val opCode = memory[memAddr])
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
                    out.println("$memAddrStr:  ${OpCode.toString(opCode)}")
                    ++memAddr
                  }

                // opcodes with one byte operand
                OpCode.SHL,     OpCode.SHR,     OpCode.LDCB   ->
                  {
                    out.print("$memAddrStr:  ${OpCode.toString(opCode)}")
                    ++memAddr
                    out.println(" ${memory[memAddr++]}")
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
                    out.print("$memAddrStr:  ${OpCode.toString(opCode)}")
                    ++memAddr
                    byte0 = memory[memAddr++]
                    byte1 = memory[memAddr++]
                    byte2 = memory[memAddr++]
                    byte3 = memory[memAddr++]
                    out.println(" ${ByteUtil.bytesToInt(byte0, byte1, byte2, byte3)}")
                  }

                // special case: LDCCH
                OpCode.LDCCH  ->
                  {
                    out.print("$memAddrStr:  ${OpCode.toString(opCode)}")
                    ++memAddr
                    byte0 = memory[memAddr++]
                    byte1 = memory[memAddr++]
                    out.println(" ${ByteUtil.bytesToChar(byte0, byte1)}")
                  }

                // special case: LDCSTR
                OpCode.LDCSTR ->
                  {
                    out.print("$memAddrStr:  ${OpCode.toString(opCode)}")
                    ++memAddr
                    // now print the string
                    out.print("  \"")
                    byte0 = memory[memAddr++]
                    byte1 = memory[memAddr++]
                    byte2 = memory[memAddr++]
                    byte3 = memory[memAddr++]
                    strLength = ByteUtil.bytesToInt(byte0, byte1, byte2, byte3)

                    for (i in 0 until strLength)
                      {
                        byte0 = memory[memAddr++]
                        byte1 = memory[memAddr++]
                        out.print(ByteUtil.bytesToChar(byte0, byte1))
                      }

                    out.println("\"")
                  }

                else -> error("*** PrintMemory: Unknown opCode $opCode ***")
              }
          }

        // now print remaining values that compose the stack
        memAddr = sb
        while (memAddr <= sp)
          {
            // Prints "SB ->", "BP ->", and "SP ->" in front of the correct memory address
            when
              {
                sb == memAddr -> out.print("SB ->")
                bp == memAddr -> out.print("BP ->")
                sp == memAddr -> out.print("SP ->")
                else          -> out.print("     ")
              }

            val memAddrStr = format(memAddr, FIELD_WIDTH)
            out.println("$memAddrStr:  ${memory[memAddr]}")
            ++memAddr
          }

        out.println()
    }


    /**
     * Prompt user and wait for user to press the enter key.
     */
    private fun pause()
      {
        out.println("Press enter to continue...")
        readLine()
    }


    /**
     * Runs the program currently in memory.
     */
    fun run()
      {
        var opCode : Byte

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

            opCode = fetchByte()

            when (opCode)
              {
                OpCode.ADD     -> add()
                OpCode.ALLOC   -> allocate()
                OpCode.BR      -> branch()
                OpCode.BE      -> branchEqual()
                OpCode.BNE     -> branchNotEqual()
                OpCode.BG      -> branchGreater()
                OpCode.BGE     -> branchGreaterOrEqual()
                OpCode.BL      -> branchLess()
                OpCode.BLE     -> branchLessOrEqual()
                OpCode.BZ      -> branchZero()
                OpCode.BNZ     -> branchNonZero()
                OpCode.CALL    -> call()
                OpCode.DEC     -> decrement()
                OpCode.DIV     -> divide()
                OpCode.GETCH   -> getCh()
                OpCode.GETINT  -> getInt()
                OpCode.GETSTR  -> getString()
                OpCode.HALT    -> halt()
                OpCode.INC     -> increment()
                OpCode.LDCB    -> loadConstByte()
                OpCode.LDCB0   -> loadConstByteZero()
                OpCode.LDCB1   -> loadConstByteOne()
                OpCode.LDCCH   -> loadConstCh()
                OpCode.LDCINT  -> loadConstInt()
                OpCode.LDCINT0 -> loadConstIntZero()
                OpCode.LDCINT1 -> loadConstIntOne()
                OpCode.LDCSTR  -> loadConstStr()
                OpCode.LDLADDR -> loadLocalAddress()
                OpCode.LDGADDR -> loadGlobalAddress()
                OpCode.LOAD    -> load()
                OpCode.LOADB   -> loadByte()
                OpCode.LOAD2B  -> load2Bytes()
                OpCode.LOADW   -> loadWord()
                OpCode.LOADSTR -> loadString()
                OpCode.MOD     -> modulo()
                OpCode.MUL     -> multiply()
                OpCode.NEG     -> negate()
                OpCode.NOT     -> not()
                OpCode.PROC    -> procedure()
                OpCode.PROGRAM -> program()
                OpCode.PUTBYTE -> putByte()
                OpCode.PUTCH   -> putChar()
                OpCode.PUTEOL  -> putEOL()
                OpCode.PUTINT  -> putInt()
                OpCode.PUTSTR  -> putString()
                OpCode.RET     -> returnInst()
                OpCode.RET0    -> returnZero()
                OpCode.RET4    -> returnFour()
                OpCode.SHL     -> shiftLeft()
                OpCode.SHR     -> shiftRight()
                OpCode.STORE   -> store()
                OpCode.STOREB  -> storeByte()
                OpCode.STORE2B -> store2Bytes()
                OpCode.STOREW  -> storeWord()
                OpCode.STOREST -> storeString()
                OpCode.SUB     -> subtract()
                else           -> error("invalid machine instruction")
              }
          }
      }


    /**
     * Print an error message and exit with nonzero status code.
     */
    private fun error(message: String)
      {
        System.err.println(message)
        exitProcess(1)
      }


    /**
     * Pop the top byte off the stack and return its value.
     */
    private fun popByte() : Byte
      {
        return memory[sp--]
      }


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
        memory[++sp] = b
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
     * Fetch the next instruction currentChar operand from memory.
     */
    private fun fetchChar() : Char
      {
        val b0 = fetchByte()
        val b1 = fetchByte()

        return ByteUtil.bytesToChar(b0, b1)
      }


    /**
     * Fetch the next instruction int operand from memory.
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
    private fun getCharAtAddr(address: Int) : Char
      {
        val b0 = memory[address + 0]
        val b1 = memory[address + 1]

        return ByteUtil.bytesToChar(b0, b1)
      }


    /**
     * Returns the integer at the specified memory address.
     * Does not alter pc, sp, or bp.
     */
    private fun getIntAtAddr(address: Int) : Int
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
    private fun getWordAtAddr(address: Int) : Int = getIntAtAddr(address)


    /**
     * Writes the char value to the specified memory address.
     * Does not alter pc, sp, or bp.
     */
    private fun putCharToAddr(value : Char, address: Int)
      {
        val bytes : ByteArray = ByteUtil.charToBytes(value)
        memory[address + 0] = bytes[0]
        memory[address + 1] = bytes[1]
      }


    /**
     * Writes the integer value to the specified memory address.
     * Does not alter pc, sp, or bp.
     */
    private fun putIntToAddr(value : Int, address: Int)
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
    private fun putWordToAddr(value : Int, address: Int) = putIntToAddr(value, address)

    //----------------------------------------------------------------------
    // End:  internal machine instructions that do NOT correspond to OpCodes


    // Start:  machine instructions corresponding to OpCodes
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
      }


    /**
     * Unconditional branch.
     */
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
            ex.printStackTrace()
            error("Invalid input")
          }
      }


    private fun getInt()
      {
        try
          {
            val destAddr : Int = popInt()
            val n : Int = readLine()!!.toInt()
            putIntToAddr(n, destAddr)
          }
        catch (e: Exception)   // e could be a NumberFormatException or a NullPointerException
          {
            error("Invalid input")
          }
      }


    private fun getString()
      {
        try
          {
            var destAddr : Int = popInt()
            val capacity : Int = fetchInt()
            val data     : String = readLine()!!
            val length   : Int = if (data.length < capacity) data.length else capacity

            putIntToAddr(length, destAddr)
            destAddr = destAddr + Constants.BYTES_PER_INTEGER
            for (i in 0 until length)
              {
                putCharToAddr(data[i], destAddr)
                destAddr = destAddr + Constants.BYTES_PER_CHAR
              }
          }
        catch (e: Exception)   // e could be a NumberFormatException or a NullPointerException
          {
            error("Invalid input")
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


    /**
     * Loads a multibyte variable onto the stack.  The number of bytes
     * is an argument of the instruction, and the address of the
     * variable is obtained by popping it off the top of the stack.
     */
    private fun load()
      {
        val length  = fetchInt()
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
        val capacity  = fetchInt()
        pushInt(capacity)

        // fetch each character and push it onto the stack
        for (i in 0 until capacity)
            pushChar(fetchChar())
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


    private fun loadString()
      {
        // loads (pushes) the string onto the stack in reverse order,
        // so that the length is on the top of the stack
        var address   = popInt()       // initialize to source address
        val strLength = getIntAtAddr(address)

        // update address to point to the first character in the string
        address = address + Constants.BYTES_PER_INTEGER

        // We need to push the characters and the string length in reverse order
        val chars = CharArray(strLength)
        for (i in 0 until strLength)
          {
            chars[i] = getCharAtAddr(address)
            address = address + Constants.BYTES_PER_CHAR
          }

        for (i in strLength - 1 downTo 0)
            pushChar(chars[i])

        pushInt(strLength)
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

        var addr : Int = sp - numBytes + 1      // initialized to starting address of the string

        val strLength = getIntAtAddr(addr)

        addr = addr + Constants.BYTES_PER_INTEGER

        for (i in 0 until strLength)
          {
            out.print(getCharAtAddr(addr))
            addr = addr + Constants.BYTES_PER_CHAR
          }

        // remove (pop) the string off the stack
        sp = sp - capacity
      }


    private fun returnInst()
      {
        val bpSave = bp
        val paramLength = fetchInt()

        sp = bpSave - paramLength - 1
        bp = getIntAtAddr(bpSave)
        pc = getIntAtAddr(bpSave + Constants.BYTES_PER_INTEGER)
      }


    private fun returnZero()
      {
        val bpSave = bp
        sp = bpSave - 1
        bp = getIntAtAddr(bpSave)
        pc = getIntAtAddr(bpSave + Constants.BYTES_PER_INTEGER)
      }


    private fun returnFour()
      {
        val bpSave = bp
        sp = bpSave - 5
        bp = getIntAtAddr(bpSave)
        pc = getIntAtAddr(bpSave + Constants.BYTES_PER_INTEGER)
      }


    private fun shiftLeft()
      {
        val operand = popInt()

        // zero out left three bits of shiftAmount
        val mask : Byte = 0x1F   // = 00011111 in binary
        val shiftAmount = (fetchByte().toInt() and mask.toInt())

        pushInt(operand shl shiftAmount)
      }


    private fun shiftRight()
      {
        val operand = popInt()

        // zero out left three bits of shiftAmount
        val mask : Byte = 0x1F   // = 00011111 in binary
        val shiftAmount = (fetchByte().toInt() and mask.toInt())

        pushInt(operand shr shiftAmount)
      }


    private fun store()
      {
        val length = fetchInt()
        val data   = ByteArray(length)

        // pop bytes of data, storing in reverse order
        for (i in length - 1 downTo 0)
            data[i] = popByte()

        val destAddr = popInt()

        for (i in 0 until length)
            memory[destAddr + i] = data[i]
      }


    private fun storeByte()
      {
        val value    = popByte()
        val destAddr = popInt()

        memory[destAddr] = value
      }


    private fun store2Bytes()
      {
        val byte1    = popByte()
        val byte0    = popByte()
        val destAddr = popInt()

        memory[destAddr + 0] = byte0
        memory[destAddr + 1] = byte1
      }


    private fun storeString()
      {
        val strLength = popInt()

        val chars = CharArray(strLength)
        for (i in 0 until strLength)
            chars[i] = popChar()

        var address = popInt()     // initialize to destination address

        // values were on the stack in reverse order
        putIntToAddr(strLength, address)
        address = address + Constants.BYTES_PER_INTEGER

        for (i in 0 until strLength)
          {
            putCharToAddr(chars[i], address)
            address = address + Constants.BYTES_PER_CHAR
          }
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
