package edu.citadel.cvm

/**
 * The set of opcodes for the CPRL virtual machine
 */
object OpCode
  {
    // halt opcode
    const val HALT    : Byte = 0

    // load opcodes (move data from memory to top of stack)
    const val LOAD    : Byte =  9
    const val LOADB   : Byte = 10
    const val LOAD2B  : Byte = 11
    const val LOADW   : Byte = 12
    const val LOADSTR : Byte = 13
    const val LDCB    : Byte = 14
    const val LDCCH   : Byte = 15
    const val LDCINT  : Byte = 16
    const val LDCSTR  : Byte = 17
    const val LDLADDR : Byte = 18
    const val LDGADDR : Byte = 19

    // optimized loads for special constants
    const val LDCB0   : Byte = 20
    const val LDCB1   : Byte = 21
    const val LDCINT0 : Byte = 22
    const val LDCINT1 : Byte = 23

    // store opcodes (move data from top of stack to memory)
    const val STORE   : Byte = 30
    const val STOREB  : Byte = 31
    const val STORE2B : Byte = 32
    const val STOREW  : Byte = 33
    const val STOREST : Byte = 34

    // compare/branch opcodes
    const val BR      : Byte = 40
    const val BE      : Byte = 41
    const val BNE     : Byte = 42
    const val BG      : Byte = 43
    const val BGE     : Byte = 44
    const val BL      : Byte = 45
    const val BLE     : Byte = 46
    const val BZ      : Byte = 47
    const val BNZ     : Byte = 48

    // shift opcodes
    const val SHL     : Byte = 50
    const val SHR     : Byte = 51

    // logical not opcode
    const val NOT     : Byte = 60

    // arithmetic opcodes
    const val ADD     : Byte = 70
    const val SUB     : Byte = 71
    const val MUL     : Byte = 72
    const val DIV     : Byte = 73
    const val MOD     : Byte = 74
    const val NEG     : Byte = 75
    const val INC     : Byte = 76
    const val DEC     : Byte = 77

    // I/O opcodes
    const val GETCH   : Byte = 80
    const val GETINT  : Byte = 81
    const val GETSTR  : Byte = 82
    const val PUTBYTE : Byte = 83
    const val PUTCH   : Byte = 84
    const val PUTINT  : Byte = 85
    const val PUTEOL  : Byte = 86
    const val PUTSTR  : Byte = 87

    // program/procedure opcodes
    const val PROGRAM : Byte = 90
    const val PROC    : Byte = 91
    const val CALL    : Byte = 92
    const val RET     : Byte = 93
    const val ALLOC   : Byte = 94

    // optimized returns for special constants
    const val RET0    : Byte = 100
    const val RET4    : Byte = 101

    /**
     * Returns a string representation for a declared opcode.
     * Returns the string value of the argument if it does not
     * have a value equal to any of the declared opcodes.
     */
    fun toString(n : Byte) : String
      {
        when (n)
          {
            HALT    -> return "HALT"
            LOAD    -> return "LOAD"
            LOADB   -> return "LOADB"
            LOAD2B  -> return "LOAD2B"
            LOADW   -> return "LOADW"
            LOADSTR -> return "LOADSTR"
            LDCB    -> return "LDCB"
            LDCCH   -> return "LDCCH"
            LDCINT  -> return "LDCINT"
            LDCSTR  -> return "LDCSTR"
            LDLADDR -> return "LDLADDR"
            LDGADDR -> return "LDGADDR"
            LDCB0   -> return "LDCB0"
            LDCB1   -> return "LDCB1"
            LDCINT0 -> return "LDCINT0"
            LDCINT1 -> return "LDCINT1"
            STORE   -> return "STORE"
            STOREB  -> return "STOREB"
            STORE2B -> return "STORE2B"
            STOREW  -> return "STOREW"
            STOREST -> return "STOREST"
            BR      -> return "BR"
            BE      -> return "BE"
            BNE     -> return "BNE"
            BG      -> return "BG"
            BGE     -> return "BGE"
            BL      -> return "BL"
            BLE     -> return "BLE"
            BZ      -> return "BZ"
            BNZ     -> return "BNZ"
            SHL     -> return "SHL"
            SHR     -> return "SHR"
            NOT     -> return "NOT"
            ADD     -> return "ADD"
            SUB     -> return "SUB"
            MUL     -> return "MUL"
            DIV     -> return "DIV"
            MOD     -> return "MOD"
            NEG     -> return "NEG"
            INC     -> return "INC"
            DEC     -> return "DEC"
            GETCH   -> return "GETCH"
            GETINT  -> return "GETINT"
            GETSTR  -> return "GETSTR"
            PUTBYTE -> return "PUTBYTE"
            PUTCH   -> return "PUTCH"
            PUTINT  -> return "PUTINT"
            PUTEOL  -> return "PUTEOL"
            PUTSTR  -> return "PUTSTR"
            CALL    -> return "CALL"
            PROC    -> return "PROC"
            PROGRAM -> return "PROGRAM"
            RET     -> return "RET"
            RET0    -> return "RET0"
            RET4    -> return "RET4"
            ALLOC   -> return "ALLOC"
            else    -> return "$n"
          }
      }
  }
