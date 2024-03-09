package edu.citadel.assembler

/**
 * This class encapsulates the symbols (a.k.a token types) for the
 * CVM assembly language.
 *
 * @constructor Construct a symbol.  Specify the number of arguments
 *              for symbols corresponding to opcodes.
 */
enum class Symbol(val numArgs : Int = 0)
  {
    // halt opcode
    HALT,

    // load/store opcodes
    LOAD(1),
    LOADB,
    LOAD2B,
    LOADW,
    LDCB(1),
    LDCCH(1),
    LDCINT(1),
    LDCSTR(1),
    LDLADDR(1),
    LDGADDR(1),

    LDCB0,
    LDCB1,
    LDCINT0,
    LDCINT1,

    STORE(1),
    STOREB,
    STORE2B,
    STOREW,

    // branch opcodes
    BR(1),
    BE(1),
    BNE(1),
    BG(1),
    BGE(1),
    BL(1),
    BLE(1),
    BZ(1),
    BNZ(1),

    // type conversion opcodes
    INT2BYTE,
    BYTE2INT,

    // logical not opcode
    NOT,

    // bitwise and shift opcodes
    BITAND,
    BITOR,
    BITXOR,
    BITNOT,
    SHL,
    SHR,

    // arithmetic opcodes
    ADD,
    SUB,
    MUL,
    DIV,
    MOD,
    NEG,
    INC,
    DEC,

    // I/O opcodes
    GETCH,
    GETINT,
    GETSTR(1),
    PUTBYTE,
    PUTCH,
    PUTINT,
    PUTEOL,
    PUTSTR(1),

    // program/procedure opcodes
    PROGRAM(1),
    PROC(1),
    CALL(1),
    RET(1),
    RET0,
    RET4,
    ALLOC(1),

    // unknown symbol (first symbol that is not an opcode)
    UNKNOWN,

    // literal values and identifier symbols
    intLiteral,
    stringLiteral,
    charLiteral,
    identifier,
    labelId,

    // special scanning symbols
    EOF;

    val isOpcode : Boolean
        get() = this < UNKNOWN
  }
