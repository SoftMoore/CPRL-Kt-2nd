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
    HALT(0),

    // load/store opcodes
    LOAD(1),
    LOADB(0),
    LOAD2B(0),
    LOADW(0),
    LDCB(1),
    LDCCH(1),
    LDCINT(1),
    LDCSTR(1),
    LDLADDR(1),
    LDGADDR(1),

    LDCB0(0),
    LDCB1(0),
    LDCINT0(0),
    LDCINT1(0),

    STORE(1),
    STOREB(0),
    STORE2B(0),
    STOREW(0),

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

    // shift opcodes
    SHL(1),
    SHR(1),

    // logical not opcode
    NOT(0),

    // arithmetic opcodes
    ADD(0),
    SUB(0),
    MUL(0),
    DIV(0),
    MOD(0),
    NEG(0),
    INC(0),
    DEC(0),

    // I/O opcodes
    GETCH(0),
    GETINT(0),
    GETSTR(1),
    PUTBYTE(0),
    PUTCH(0),
    PUTINT(0),
    PUTEOL(0),
    PUTSTR(1),

    // program/procedure opcodes
    PROGRAM(1),
    PROC(1),
    CALL(1),
    RET(1),
    RET0(0),
    RET4(0),
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

    open val isOpcode : Boolean
        get() = this < UNKNOWN
  }
