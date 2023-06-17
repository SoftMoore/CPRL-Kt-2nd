package edu.citadel.cprl

/**
 * This class encapsulates the symbols (also known as token types)
 * of the programming language CPRL.
 *
 * @constructor Construct a new symbol with its label.
 */
enum class Symbol(val label : String)
  {
    // reserved words
    BooleanRW("Boolean"),
    CharRW("Char"),
    IntegerRW("Integer"),
    andRW("and"),
    arrayRW("array"),
    classRW("class"),
    constRW("const"),
    elseRW("else"),
    enumRW("enum"),
    exitRW("exit"),
    falseRW("false"),
    forRW("for"),
    funRW("fun"),
    ifRW("if"),
    loopRW("loop"),
    modRW("mod"),
    notRW("not"),
    ofRW("of"),
    orRW("or"),
    privateRW("private"),
    procRW("proc"),
    protectedRW("protected"),
    publicRW("public"),
    readRW("read"),
    readlnRW("readln"),
    recordRW("record"),
    returnRW("return"),
    stringRW("string"),
    thenRW("then"),
    trueRW("true"),
    typeRW("type"),
    varRW("var"),
    whenRW("when"),
    whileRW("while"),
    writeRW("write"),
    writelnRW("writeln"),

    // arithmetic operator symbols
    plus("+"),
    minus("-"),
    times("*"),
    divide("/"),

    // relational operator symbols
    equals("="),
    notEqual("!="),
    lessThan("<"),
    lessOrEqual("<="),
    greaterThan(">"),
    greaterOrEqual(">="),

    // assignment, punctuation, and grouping symbols
    assign(":="),
    leftParen("("),
    rightParen(")"),
    leftBracket("["),
    rightBracket("]"),
    leftBrace("{"),
    rightBrace("}"),
    comma(","),
    colon(":"),
    semicolon(";"),
    dot("."),

    // literal and identifier symbols
    intLiteral("Integer Literal"),
    charLiteral("Char Literal"),
    stringLiteral("String Literal"),
    identifier("Identifier"),

    // special scanning symbols
    EOF("End-of-File"),
    unknown("Unknown");

    /**
     * Returns true if this symbol is a CPRL reserved word.
     */
    fun isReservedWord() : Boolean = this in BooleanRW..writelnRW

    /**
     * Returns true if this symbol can start an initial declaration.
     */
    fun isInitialDeclStarter() : Boolean
        = this == constRW || this == varRW || this == typeRW

    /**
     * Returns true if this symbol can start a subprogram declaration.
     */
    fun isSubprogramDeclStarter() : Boolean = this == procRW || this == funRW

    /**
     * Returns true if this symbol can start a statement.
     */
    fun isStmtStarter() : Boolean
        =  this == identifier || this == leftBrace || this == ifRW
        || this == loopRW     || this == whileRW   || this == exitRW
        || this == readRW     || this == writeRW   || this == writelnRW
        || this == returnRW

    /**
     * Returns true if this symbol is a literal.
     */
    fun isLiteral() : Boolean
        =  this == intLiteral || this == charLiteral || this == stringLiteral
        || this == trueRW     || this == falseRW

    /**
     * Returns true if this symbol can start an expression.
     */
    fun isExprStarter() : Boolean
        = isLiteral()   || this == identifier || this == leftParen
        || this == plus || this == minus      || this == notRW

    /**
     * Returns true if this symbol can start a parameter declaration.
     */
    fun isParameterDeclStarter() : Boolean = this == identifier || this == varRW

    /**
     * Returns true if this symbol can start a variable selector.
     */
    fun isSelectorStarter() : Boolean = this == leftBracket || this == dot

    /**
     * Returns true if this symbol is a logical operator.
     */
    fun isLogicalOperator() : Boolean = this == andRW || this == orRW

    /**
     * Returns true if this symbol is a relational operator.
     */
    fun isRelationalOperator() : Boolean
        =  this == equals      || this == notEqual
        || this == lessThan    || this == lessOrEqual
        || this == greaterThan || this == greaterOrEqual

    /**
     * Returns true if this symbol is a binary adding operator.
     */
    fun isAddingOperator() : Boolean = this == plus || this == minus

    /**
     * Returns true if this symbol is a unary sign operator.
     */
    fun isSignOperator() : Boolean = this == plus || this == minus

    /**
     * Returns true if this symbol is a multiplying operator.
     */
    fun isMultiplyingOperator() : Boolean
        = this == times || this == divide || this == modRW

    override fun toString() : String = label
  }
