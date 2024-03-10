package edu.citadel.cprl

import edu.citadel.compiler.ErrorHandler
import edu.citadel.compiler.InternalCompilerException
import edu.citadel.compiler.ParserException
import edu.citadel.compiler.Position

import java.util.EnumSet

/**
 * This class uses recursive descent to perform syntax analysis of
 * the CPRL source language.
 *
 * @constructor Construct a parser with the specified scanner,
 *              identifier table, and error handler.
 */
class Parser(private val scanner : Scanner,
             private val idTable : IdTable,
             private val errorHandler : ErrorHandler)
  {
    /** Symbols that can follow a statement. */
    private val stmtFollowers = EnumSet.of(
// ...
      )

    /** Symbols that can follow a subprogram declaration. */
    private val subprogDeclFollowers = EnumSet.of(
// ...
      )

    /** Symbols that can follow a factor. */
    private val factorFollowers = EnumSet.of(
        Symbol.semicolon, Symbol.loopRW,       Symbol.thenRW,      Symbol.rightParen,
        Symbol.andRW,     Symbol.orRW,         Symbol.equals,      Symbol.notEqual,
        Symbol.lessThan,  Symbol.lessOrEqual,  Symbol.greaterThan, Symbol.greaterOrEqual,
        Symbol.plus,      Symbol.minus,        Symbol.times,       Symbol.divide,
        Symbol.modRW,     Symbol.rightBracket, Symbol.comma)

    /** Symbols that can follow an initial declaration (computed property).
     *  Set is computed dynamically based on the scope level. */
    private val initialDeclFollowers : Set<Symbol>
        get()
          {
            // An initial declaration can always be followed by another
            // initial declaration, regardless of the scope level.
            val followers = EnumSet.of(Symbol.constRW, Symbol.varRW, Symbol.typeRW)

            if (idTable.scopeLevel == ScopeLevel.GLOBAL)
                followers.addAll(EnumSet.of(Symbol.procRW, Symbol.funRW))
            else
              {
                followers.addAll(stmtFollowers)
                followers.remove(Symbol.elseRW)
              }

            return followers
          }

    /**
     * Parse the following grammar rule:<br>
     * `program = initialDecls subprogramDecls .`
     */
    fun parseProgram()
      {
        try
          {
            parseInitialDecls()
            parseSubprogramDecls()

            // match(Symbol.EOF)
            // Let's generate a better error message than "Expecting "End-of-File" but ..."
            if (scanner.symbol != Symbol.EOF)
              {
                val errorMsg = "Expecting \"${Symbol.procRW}\" or \"${Symbol.funRW}\" " +
                               "but found \"${scanner.token}\" instead."
                throw error(errorMsg)
              }
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)
            recover(EnumSet.of(Symbol.EOF))
          }
      }

    /**
     * Parse the following grammar rule:<br>
     * `initialDecls = { initialDecl } .`
     */
    private fun parseInitialDecls()
      {
        while (scanner.symbol.isInitialDeclStarter())
            parseInitialDecl()
      }

    /**
     * Parse the following grammar rule:<br>
     * `initialDecl = constDecl | varDecl | typeDecl .`
     */
    private fun parseInitialDecl()
      {
// ...   throw an internal error if the symbol is not one of constRW, varRW, or typeRW
      }

    /**
     * Parse the following grammar rule:<br>
     * `constDecl = "const" constId ":=" literal ";" .`
     */
    private fun parseConstDecl()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `literal = intLiteral | charLiteral | stringLiteral | "true" | "false" .`
     */
    private fun parseLiteral()
      {
        try
          {
            if (scanner.symbol.isLiteral())
                matchCurrentSymbol()
            else
                throw error("Invalid literal expression.")
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)
            recover(factorFollowers)
          }
      }

    /**
     * Parse the following grammar rule:<br>
     * `varDecl = "var" identifiers ":" typeName [ ":=" constValue] ";" .`
     */
    private fun parseVarDecl()
      {
        try
          {
            match(Symbol.varRW)
            val identifiers : List<Token> = parseIdentifiers()
            match(Symbol.colon)
            parseTypeName()

            if (scanner.symbol == Symbol.assign)
              {
                matchCurrentSymbol()
                parseConstValue()
              }

            match(Symbol.semicolon)

            for (identifier in identifiers)
                idTable.add(identifier, IdType.variableId)
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)
            recover(initialDeclFollowers)
          }
      }

    /**
     * Parse the following grammar rule:<br>
     * `identifiers = identifier { "," identifier } .`
     *
     * @return the list of identifier tokens.  Returns an empty list if parsing fails.
     */
    private fun parseIdentifiers() : List<Token>
      {
        try
          {
            val identifiers = ArrayList<Token>(10)
            var idToken = scanner.token
            match(Symbol.identifier)
            identifiers.add(idToken)

            while (scanner.symbol == Symbol.comma)
              {
                matchCurrentSymbol()
                idToken = scanner.token
                match(Symbol.identifier)
                identifiers.add(idToken)
              }

            return identifiers
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)
            recover(EnumSet.of(Symbol.colon))
            return emptyList()   // should never execute
          }
      }

    /**
     * Parse the following grammar rule:<br>
     * `typeDecl = arrayTypeDecl | recordTypeDecl | stringTypeDecl .`
     */
    private fun parseTypeDecl()
      {
        assert(scanner.symbol == Symbol.typeRW)

        try
          {
            when (scanner.lookahead(4).symbol)
              {
                Symbol.arrayRW  -> parseArrayTypeDecl()
                Symbol.recordRW -> parseRecordTypeDecl()
                Symbol.stringRW -> parseStringTypeDecl()
                else ->
                  {
                    val errorPos = scanner.lookahead(4).position
                    throw error(errorPos, "Invalid type declaration.")
                  }
              }
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)
            matchCurrentSymbol()   // force scanner past "type"
            recover(initialDeclFollowers)
          }
      }

    /**
     * Parse the following grammar rule:<br>
     * `arrayTypeDecl = "type" typeId "=" "array" "[" intConstValue "]"
     *                  "of" typeName ";" .`
     */
    private fun parseArrayTypeDecl()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `recordTypeDecl = "type" typeId "=" "record" "{" fieldDecls "}" ";" .`
     */
    private fun parseRecordTypeDecl()
      {
        try
          {
            match(Symbol.typeRW)
            val typeId = scanner.token
            match(Symbol.identifier)
            match(Symbol.equals)
            match(Symbol.recordRW)
            match(Symbol.leftBrace)

            try
              {
                idTable.openScope(ScopeLevel.RECORD)
                parseFieldDecls()
              }
            finally
              {
                idTable.closeScope()
              }

            match(Symbol.rightBrace)
            match(Symbol.semicolon)
            idTable.add(typeId, IdType.recordTypeId)
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)
            recover(initialDeclFollowers)
          }
      }

    /**
     * Parse the following grammar rule:<br>
     * `fieldDecls = { fieldDecl } .`
     */
    private fun parseFieldDecls()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `fieldDecl = fieldId ":" typeName ";" .`
     */
    private fun parseFieldDecl()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `stringTypeDecl = "type" typeId "=" "string" "[" intConstValue "]" ";" .`
     */
    private fun parseStringTypeDecl()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `typeName = "Integer" | "Boolean" | "Char" | typeId .`
     */
    private fun parseTypeName()
      {
        try
          {
            when (scanner.symbol)
              {
                Symbol.IntegerRW  -> matchCurrentSymbol()
                Symbol.BooleanRW  -> matchCurrentSymbol()
                Symbol.CharRW     -> matchCurrentSymbol()
                Symbol.identifier ->
                  {
                    val typeId = scanner.token
                    matchCurrentSymbol()
                    val type = idTable[typeId.text]

                    if (type != null)
                      {
                        if (type == IdType.arrayTypeId || type == IdType.recordTypeId || type == IdType.stringTypeId)
                            ;   // empty statement for versions 1 and 2 of Parser
                        else
                          {
                            val errorMsg = "Identifier \"$typeId\" is not a valid type name."
                            throw error(typeId.position, errorMsg)
                          }
                      }
                    else
                      {
                        val errorMsg = "Identifier \"$typeId\" has not been declared."
                        throw error(typeId.position, errorMsg)
                      }
                  }
                else -> throw error("Invalid type name.")
              }
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)
            recover(EnumSet.of(Symbol.semicolon,  Symbol.comma,
                               Symbol.rightParen, Symbol.leftBrace))
          }
      }

    /**
     * Parse the following grammar rule:<br>
     * `subprogramDecls = { subprogramDecl } .`
     */
    private fun parseSubprogramDecls()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `subprogramDecl = procedureDecl | functionDecl .`
     */
    private fun parseSubprogramDecl()
      {
// ...   throw an internal error if the symbol is not one of procedureRW or functionRW
      }

    /**
     * Parse the following grammar rule:<br>
     * `procedureDecl = "proc" procId "(" [ formalParameters ] ")"
     *                  "{" initialDecls statements "}" .`
     */
    private fun parseProcedureDecl()
      {
        try
          {
            match(Symbol.procRW)
            val procId = scanner.token
            match(Symbol.identifier)
            idTable.add(procId, IdType.procedureId)
            match(Symbol.leftParen)

            try
              {
                idTable.openScope(ScopeLevel.LOCAL)

                if (scanner.symbol.isParameterDeclStarter())
                    parseFormalParameters()

                match(Symbol.rightParen)
                match(Symbol.leftBrace)
                parseInitialDecls()
                parseStatements()
              }
            finally
              {
                idTable.closeScope()
              }

            match(Symbol.rightBrace)
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)
            recover(subprogDeclFollowers)
          }
      }

    /**
     * Parse the following grammar rule:<br>
     * `functionDecl = "fun" funcId "(" [ formalParameters ] ")" ":" typeName
     *                 "{" initialDecls statements "}" .`
     */
    private fun parseFunctionDecl()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `formalParameters = parameterDecl { "," parameterDecl } .`
     */
    private fun parseFormalParameters()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `parameterDecl = [ "var" ] paramId ":" typeName .`
     */
    private fun parseParameterDecl()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `statements = { statement } .`
     */
    private fun parseStatements()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `statement = assignmentStmt | procedureCallStmt | compoundStmt | ifStmt
     *            | loopStmt | exitStmt | readStmt | writeStmt | writelnStmt
     *            | returnStmt .`
     */
    private fun parseStatement()
      {
        // assumes that scanner.getSymbol() can start a statement
        assert(scanner.symbol.isStmtStarter()) { "Invalid statement." }

        try
          {
            val symbol = scanner.symbol

            if (symbol == Symbol.identifier)
              {
                // Handle identifiers based on how they are declared,
                // or use the lookahead symbol if not declared.
                val idStr  = scanner.text
                val idType = idTable[idStr]

                if (idType != null)
                  {
                    if (idType == IdType.variableId)
                        parseAssignmentStmt()
                    else if (idType == IdType.procedureId)
                        parseProcedureCallStmt()
                    else
                        throw error("Identifier \"$idStr\" cannot start a statement.")
                  }
                else
                  {
                    // make parsing decision using lookahead symbol
// ...
                  }
              }
            else if (symbol == Symbol.leftBrace)
                parseCompoundStmt()
// ...
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)

            // Error recovery here is complicated for identifiers since they can both
            // start a statement and appear elsewhere in the statement.  (Consider,
            // for example, an assignment statement or a procedure call statement.)
            // Since the most common error is to declare or reference an identifier
            // incorrectly, we will assume that this is the case and advance to the
            // next semicolon (which hopefully ends the erroneous statement) before
            // performing error recovery.
            scanner.advanceTo(Symbol.semicolon)
            recover(stmtFollowers)
          }
      }

    /**
     * Parse the following grammar rule:<br>
     * `assignmentStmt = variable ":=" expression ";" .`
     */
    private fun parseAssignmentStmt()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `compoundStmt = "{" statements "}" .`
    ` */
    private fun parseCompoundStmt()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `ifStmt = "if" booleanExpr "then" statement  [ "else" statement ] .`
     */
    private fun parseIfStmt()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `loopStmt = [ "while" booleanExpr ] "loop" statement .`
     */
    private fun parseLoopStmt()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `exitStmt = "exit" [ "when" booleanExpr ] ";" .`
     */
    private fun parseExitStmt()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `readStmt = "read" variable ";" .`
     */
    private fun parseReadStmt()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `writeStmt = "write" expressions ";" .`
     */
    private fun parseWriteStmt()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `expressions = expression { "," expression } .`
     */
    private fun parseExpressions()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `writelnStmt = "writeln" [ expressions ] ";" .`
     */
    private fun parseWritelnStmt()
      {
        try
          {
            match(Symbol.writelnRW)

            if (scanner.symbol.isExprStarter())
                parseExpressions()

            match(Symbol.semicolon)
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)
            recover(stmtFollowers)
          }
      }

    /**
     * Parse the following grammar rules:<br>
     * `procedureCallStmt = procId "(" [ actualParameters ] ")" ";" .
     *  actualParameters = expressions .`
     */
    private fun parseProcedureCallStmt()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `returnStmt = "return" [ expression ] ";" .`
     */
    private fun parseReturnStmt()
      {
// ...
      }

    /**
     * Parse the following grammar rules:<br>
     * `variable = ( varId | paramId ) { indexExpr | fieldExpr } .<br>
     *  indexExpr = "[" expression "]" .<br>
     *  fieldExpr = "." fieldId .</code>`
     * <br>
     * This method provides common logic for methods `parseVariable()` and
     * `parseVariableExpr()`.  The method does not handle any parser exceptions but
     * throws them back to the calling method where they can be handled appropriately.
     *
     * @throws ParserException if parsing fails.
     * @see .parseVariable
     * @see .parseVariableExpr
     */
    private fun parseVariableCommon()
      {
        val idToken = scanner.token
        match(Symbol.identifier)
        val idType = idTable[idToken.text]

        if (idType == null)
          {
            val errorMsg = "Identifier \"$idToken\" has not been declared."
            throw error(idToken.position, errorMsg)
          }
        else if (idType !== IdType.variableId)
          {
            val errorMsg = "Identifier \"$idToken\" is not a variable."
            throw error(idToken.position, errorMsg)
          }

        while (scanner.symbol.isSelectorStarter())
          {
            if (scanner.symbol == Symbol.leftBracket)
              {
                match(Symbol.leftBracket);
                parseExpression();
                match(Symbol.rightBracket);
              }
            else if (scanner.symbol == Symbol.dot)
              {
                match(Symbol.dot);
                match(Symbol.identifier);
              }
          }
      }

    /**
     * Parse the following grammar rule:<br>
     * `variable = ( varId | paramId ) { indexExpr | fieldExpr } .`
     */
    private fun parseVariable()
      {
        try
          {
            parseVariableCommon()
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)
            recover(EnumSet.of(Symbol.assign, Symbol.semicolon))
          }
      }

    /**
     * Parse the following grammar rules:<br>
     * `expression = relation { logicalOp relation } .<br>
     *  logicalOp = "and" | "or" .`
     */
    private fun parseExpression()
      {
        parseRelation()
        while (scanner.symbol.isLogicalOperator())
          {
            matchCurrentSymbol()
            parseRelation()
          }
      }

    /**
     * Parse the following grammar rules:<br>
     * `relation = simpleExpr [ relationalOp simpleExpr ] .<br>
     *  relationalOp = "=" | "!=" | "<" | "<=" | ">" | ">=" .`
     */
    private fun parseRelation()
      {
// ...
      }

    /**
     * Parse the following grammar rules:<br>
     * `simpleExpr = [ signOp ] term { addingOp term } .<br>
     *  signOp = "+" | "-" .<br>
     *  addingOp = "+" | "-" .`
     */
    private fun parseSimpleExpr()
      {
// ...
      }

    /**
     * Parse the following grammar rules:<br>
     * `term = factor { multiplyingOp factor } .<br>
     *  multiplyingOp = "*" | "/" | "mod" .`
     */
    private fun parseTerm()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `factor = "not" factor | constValue | variableExpr | functionCallExpr
     *         | "(" expression ")" .`
     */
    private fun parseFactor()
      {
        try
          {
            if (scanner.symbol == Symbol.notRW)
              {
                matchCurrentSymbol()
                parseFactor()
              }
            else if (scanner.symbol.isLiteral())
              {
                // Handle constant literals separately from constant identifiers.
                parseConstValue()
              }
            else if (scanner.symbol == Symbol.identifier)
              {
                // Handle identifiers based on how they are declared,
                // or use the lookahead symbol if not declared.
                val idStr  = scanner.text
                val idType = idTable[idStr]

                if (idType != null)
                  {
                    when (idType)
                      {
                        IdType.constantId -> parseConstValue()
                        IdType.variableId -> parseVariableExpr()
                        IdType.functionId -> parseFunctionCallExpr()
                        else ->
                          {
                            val errorPos = scanner.position
                            val errorMsg = "Identifier \"$idStr\" is not valid as an expression."

                            // special handling when procedure call is used as a function call
                            if (idType == IdType.procedureId)
                              {
                                scanner.advance()
                                if (scanner.symbol == Symbol.leftParen)
                                  {
                                    scanner.advanceTo(Symbol.rightParen)
                                    scanner.advance()   // advance past the right paren
                                  }
                              }

                            throw error(errorPos, errorMsg)
                          }
                      }
                  }
                else
                  {
                    // Make parsing decision using an additional lookahead symbol.
                    if (scanner.lookahead(2).symbol == Symbol.leftParen)
                        parseFunctionCallExpr()
                    else
                        throw error("Identifier \"${scanner.token}\" has not been declared.")
                  }
              }
            else if (scanner.symbol == Symbol.leftParen)
              {
                matchCurrentSymbol()
                parseExpression()
                match(Symbol.rightParen)
              }
            else
                throw error("Invalid expression.")
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)
            recover(factorFollowers)
          }
      }

    /**
     * Parse the following grammar rule:<br>
     * `constValue = literal | constId .`
     */
    private fun parseConstValue()
      {
// ...
      }

    /**
     * Parse the following grammar rule:<br>
     * `variableExpr = variable .`
     */
    private fun parseVariableExpr()
      {
        try
          {
            parseVariableCommon()
          }
        catch (e : ParserException)
          {
            errorHandler.reportError(e)
            recover(factorFollowers)
          }
      }

    /**
     * Parse the following grammar rules:<br>
     * `functionCallExpr = funcId "(" [ actualParameters ] ")" .
     *  actualParameters = expressions .`
     */
    private fun parseFunctionCallExpr()
      {
// ...
      }

    // Utility parsing methods

    /**
     * Check that the current scanner symbol is the expected symbol.  If it
     * is, then advance the scanner.  Otherwise, throw a ParserException.
     */
    private fun match(expectedSymbol : Symbol)
      {
        if (scanner.symbol == expectedSymbol)
            scanner.advance()
        else
          {
            val errorMsg = "Expecting \"$expectedSymbol\" but " +
                           "found \"${scanner.token}\" instead."
            throw error(errorMsg)
          }
      }

    /**
     * Advance the scanner.  This method represents an unconditional match
     * with the current scanner symbol.
     */
    private fun matchCurrentSymbol() = scanner.advance()

    /**
     * Advance the scanner until the current symbol is one
     * of the symbols in the specified set of follows.
     */
    private fun recover(followers : Set<Symbol>) = scanner.advanceTo(followers)

    /**
     * Create a parser exception with the specified error message and
     * the current scanner position.
     */
    private fun error(errorMsg : String) : ParserException
        = error(scanner.position, errorMsg)

    /**
     * Create a parser exception with the specified error position
     * and error message.
     */
    private fun error(errorPos : Position, errorMsg : String)
        = ParserException(errorPos, errorMsg)

    /**
     * Create an internal compiler exception with the specified error
     * message and the current scanner position.
     */
    private fun internalError(errorMsg : String)
        = InternalCompilerException(scanner.position, errorMsg)
  }
