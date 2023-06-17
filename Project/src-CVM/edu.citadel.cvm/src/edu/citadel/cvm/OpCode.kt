package edu.citadel.cvm

/**
 * The set of opcodes for the CPRL virtual machine.
 *
 * @constructor Construct an opcode with its machine instruction value.
 */
enum class Opcode(value : Int)
  {
    // halt opcode
    HALT(0),

    // load opcodes (move data from memory to top of stack)
    LOAD(9),
    LOADB(10),
    LOAD2B(11),
    LOADW(12),
    LOADSTR(13),
    LDCB(14),
    LDCCH(15),
    LDCINT(16),
    LDCSTR(17),
    LDLADDR(18),
    LDGADDR(19),

    // optimized loads for special constants
    LDCB0(20),
    LDCB1(21),
    LDCINT0(22),
    LDCINT1(23),

    // store opcodes (move data from top of stack to memory)
    STORE(30),
    STOREB(31),
    STORE2B(32),
    STOREW(33),
    STOREST(34),

    // compare/branch opcodes
    BR(40),
    BE(41),
    BNE(42),
    BG(43),
    BGE(44),
    BL(45),
    BLE(46),
    BZ(47),
    BNZ(48),

    // shift opcodes
    SHL(50),
    SHR(51),

    // logical not opcode
    NOT(60),

    // arithmetic opcodes
    ADD(70),
    SUB(71),
    MUL(72),
    DIV(73),
    MOD(74),
    NEG(75),
    INC(76),
    DEC(77),

    // I/O opcodes
    GETCH(80),
    GETINT(81),
    GETSTR(82),
    PUTBYTE(83),
    PUTCH(84),
    PUTINT(85),
    PUTEOL(86),
    PUTSTR(87),

    // program/procedure opcodes
    PROGRAM(90),
    PROC(91),
    CALL(92),
    RET(93),
    ALLOC(94),

    // optimized returns for special constants
    RET0(100),
    RET4(101);

    private val value : Byte = value.toByte()

    /**
     * Returns true if this opcode has no operands.
     */
    fun isZeroOperandOpcode() : Boolean
      {
        return when (this)
          {
            ADD,     DEC,     DIV,     GETCH,   GETINT,  HALT,
            LOADB,   LOAD2B,  LOADW,   LOADSTR, LDCB0,   LDCB1,
            LDCINT0, LDCINT1, INC,     MOD,     MUL,     NEG,
            NOT,     PUTBYTE, PUTCH,   PUTINT,  PUTEOL,  RET0,
            RET4,    STOREB,  STORE2B, STOREW,  STOREST, SUB   -> true
            else -> false
          }
      }

    /**
     * Returns true if this opcode has a byte operand.
     */
    fun isByteOperandOpcode() : Boolean
      {
        return when (this)
          {
            SHL, SHR, LDCB -> true
            else -> false
          }
      }

    /**
     * Returns true if this opcode has an int operand.
     */
    fun isIntOperandOpcode() : Boolean
      {
        return when (this)
          {
            ALLOC,   BR,      BE,    BNE,     BG,     BGE,  BL,
            BLE,     BZ,      BNZ,   CALL,    GETSTR, LOAD, LDCINT,
            LDLADDR, LDGADDR, PROC,  PROGRAM, PUTSTR, RET,  STORE  -> true
            else -> false
          }
      }

    fun toByte() : Byte = value

    fun toInt() : Int = value.toInt()

    companion object
      {
        // maps byte value of opcode to the opcode
        private val valueOpcodeMap = mutableMapOf<Byte, Opcode>()

        init
          {
            for (opcode in values())
                valueOpcodeMap[opcode.value] = opcode
          }

        /**
         * Returns the opcode for the specified integer value,
         * or null if the value does not correspond to an opcode.
         */
        fun toOpcode(n : Int) : Opcode? =
            if (n < 0 || n > 256) null else toOpcode(n.toByte())

        /**
         * Returns the opcode for the specified byte value,
         * or null if the value does not correspond to an opcode.
         */
        fun toOpcode(b : Byte) : Opcode? = valueOpcodeMap[b]
      }
  }

