package edu.citadel.cprl

import edu.citadel.compiler.ErrorHandler
import edu.citadel.compiler.Position
import edu.citadel.compiler.ScannerException
import edu.citadel.compiler.Source

/**
 * Performs lexical analysis for the CPRL programming language.
 * Implements k tokens of lookahead.
 *
 * @constructor Construct scanner with its associated source,
 *              number of lookahead tokens, and error handler.
 */
class Scanner(private val source : Source, k : Int,
              private val errorHandler : ErrorHandler)
  {
    // buffer to hold lookahead tokens
    private val tokenBuffer = TokenBuffer(k)

    // buffer to hold identifiers and literals
    private val scanBuffer = StringBuilder(100)

    /**
     * Initialize scanner and advance to the first token.
     */
    init
      {
        // fill buffer with k tokens
        for (i in 0 until k)
            advance()
      }

    /**
     * The current token; equivalent to lookahead(1).
     */
    val token : Token
        get() = lookahead(1)

    /**
     * The current symbol; equivalent to lookahead(1).symbol.
     */
    val symbol : Symbol
        get() = lookahead(1).symbol

    /**
     * The current text; equivalent to lookahead(1).text.
     */
    val text : String
        get() = lookahead(1).text

    /**
     * The current position; equivalent to lookahead(1).position.
     */
    val position : Position
        get() = lookahead(1).position

    /**
     * Returns the ith lookahead token.  Valid parameter values are in the
     * range 1..k; i.e., the first (current) lookahead token is lookahead(1).
     */
    fun lookahead(i : Int) : Token
      {
        assert(i in 1..4) { "Range check for lookahead token index" }
        return tokenBuffer[i - 1]
      }

    /**
     * Advance the scanner one token.
     */
    fun advance() = tokenBuffer.add(nextToken())

    /**
     * Advance until the current symbol matches the symbol specified
     * in the parameter or until end of file is encountered.
     */
    fun advanceTo(symbol : Symbol)
      {
        while (this.symbol != symbol && this.symbol != Symbol.EOF)
            advance()
      }

    /**
     * Advance until the current symbol matches one of the symbols
     * in the given set or until end of file is encountered.
     */
    fun advanceTo(symbols : Set<Symbol>)
      {
        while (!symbols.contains(symbol) && symbol != Symbol.EOF)
            advance()
      }

    /**
     * Returns the next token in the source file.
     */
    private fun nextToken() : Token
      {
        var symbol   : Symbol
        var position = Position()
        var text     = ""

        try
          {
            skipWhiteSpace()

            // currently at starting character of next token
            position = source.charPosition

            if (source.currentChar == source.EOF)
              {
                // set symbol but don't advance source
                symbol = Symbol.EOF
              }
            else if (isLetter(source.currentChar.toChar()))
              {
                val idString = scanIdentifier()
                symbol = getIdentifierSymbol(idString)

                if (symbol == Symbol.identifier)
                    text = idString
              }
            else if (isDigit(source.currentChar.toChar()))
              {
                symbol = Symbol.intLiteral
                text   = scanIntegerLiteral()
              }
            else
              {
                when (source.currentChar.toChar())
                  {
                    '+' ->  {
                              symbol = Symbol.plus
                              source.advance()
                            }
// ...
                    '/' ->  {
                              source.advance()
                              if (source.currentChar.toChar() == '/')
                                {
                                  skipComment()
                                  return nextToken()   // continue scanning for next token
                                }
                              else
                                  symbol = Symbol.divide
                            }
// ...
                    '<' ->  {
                              source.advance()
                              if (source.currentChar.toChar() == '=')
                                {
                                  symbol = Symbol.lessOrEqual
                                  source.advance()
                                }
                              else
                                  symbol = Symbol.lessThan
                            }
// ...
                    else ->
                      {
                        // error:  invalid character
                        val errorMsg = "Invalid character \'${source.currentChar.toChar()}\'"
                        source.advance()
                        throw error(errorMsg)
                      }
                  }
              }
          }
        catch (e : ScannerException)
          {
            errorHandler.reportError(e)
            // set symbol to either EOF or unknown
            symbol = if (source.currentChar == source.EOF) Symbol.EOF else Symbol.unknown
          }

        return Token(symbol, position, text)
      }

    /**
     * Returns the symbol associated with an identifier
     * (Symbol.arrayRW, Symbol.ifRW, Symbol.identifier, etc.)
     */
    private fun getIdentifierSymbol(idString : String) : Symbol
      {
// ...  Hint: Need an efficient search based on the text of the identifier (parameter idString)
      }

    /**
     * Skip over a comment.
     */
    private fun skipComment()
      {
        // assumes that source.currentChar is the second '/'
        assert(source.currentChar.toChar() == '/') { "Check for '/' as part of comment" }
        skipToEndOfLine()
        source.advance()
      }

    /**
     * Clear the scan buffer (makes it empty).
     */
    private fun StringBuilder.clear() = delete(0, length)

    /**
     * Scans characters in the source file for a valid identifier using the
     * lexical rule: identifier = letter ( letter | digit)* .
     *
     * @return the string of letters and digits for the identifier.
     */
    private fun scanIdentifier() : String
      {
        // assumes that source.currentChar is the first letter of the identifier
        assert(isLetter(source.currentChar.toChar()))
          { "Check identifier start for letter at position ${source.charPosition}." }
// ...
      }

    /**
     * Scans characters in the source file for a valid integer literal.
     * Assumes that source.currentChar is the first character of the Integer literal.
     *
     * @return the string of digits for the integer literal.
     */
    private fun scanIntegerLiteral() : String
      {
        // assumes that source.currentChar is the first digit of the integer literal
        assert(isDigit(source.currentChar.toChar()))
          { "Check integer literal start for digit at position ${source.charPosition}." }

        scanBuffer.clear()

        do
          {
            scanBuffer.append(source.currentChar.toChar())
            source.advance()
          }
        while (isDigit(source.currentChar.toChar()))

        return scanBuffer.toString()
      }

    /**
     * Scan characters in the source file for a String literal.  Escaped
     * characters are not converted; e.g., '\t' is not converted to the tab
     * character since the assembler performs the conversion.  Assumes that
     * source.currentChar is the opening double quote (") of the String literal.
     *
     * @return the string of characters for the string literal, including
     *         opening and closing quotes
     */
    private fun scanStringLiteral() : String
      {
        // assumes that source.currentChar is the opening double quote for the string literal
        assert(source.currentChar.toChar() == '\"')
          { "Check for opening quote (\") at position ${source.charPosition}." }
// ...
      }

    /**
     * Scan characters in the source file for a Char literal.  Escaped
     * characters are not converted; e.g., '\t' is not converted to the tab
     * character since the assembler performs that conversion.  Assumes that
     * source.currentChar is the opening single quote (') of the Char literal.
     *
     * @return the string of characters for the char literal, including
     *         opening and closing single quotes.
     */
    private fun scanCharLiteral() : String
      {
        // assumes that source.currentChar is the opening single quote for the char literal
        assert(source.currentChar.toChar() == '\'')
          { "Check for opening quote (\') at position ${source.charPosition}." }

        val errorMsg = "Invalid Char literal."
        scanBuffer.clear()

        // append the opening single quote
        var c = source.currentChar.toChar()
        scanBuffer.append(c)
        source.advance()

        checkGraphicChar(source.currentChar)
        c = source.currentChar.toChar()

        if (c == '\\')   // escaped character
            scanBuffer.append(scanEscapedChar())
        else if (c == '\'')
          {
            // either '' (empty) or '''; both are invalid
            source.advance()
            c = source.currentChar.toChar()

            if (c == '\'')   // three single quotes in a row
                source.advance()

            throw error(errorMsg)
          }
        else
          {
            scanBuffer.append(c)
            source.advance()
          }

        c = source.currentChar.toChar()   // should be the closing single quote
        checkGraphicChar(c.code)

        if (c == '\'')
          {
            scanBuffer.append(c)          // append closing quote
            source.advance()
          }
        else
            throw error(errorMsg)

        return scanBuffer.toString()
      }

    /**
     * Scans characters in the source file for an escaped character; i.e.,
     * a character preceded by a backslash.  This method checks escape
     * characters \t, \n, \r, \", \', and \\.  If the character following
     * a backslash is anything other than one of these characters, then an
     * exception is thrown.  Note that the escaped character sequence is
     * returned unmodified; i.e., \t returns "\t", not the tab character.
     * Assumes that source.currentChar is the escape character (\).
     *
     * @return the escaped character sequence unmodified.
     */
    private fun scanEscapedChar() : String
      {
        // assumes that source.currentChar is the backslash for the escaped char
        assert(source.currentChar.toChar() == '\\')
          { "Check for escape character ('\\') at position ${source.charPosition}." }

        // Need to save current position for error reporting.
        val backslashPosition = source.charPosition

        source.advance()
        checkGraphicChar(source.currentChar)
        val c = source.currentChar.toChar()

        source.advance()   // leave source at second character following backslash

        when (c)
          {
            't'  -> return "\\t"    // tab
            'n'  -> return "\\n"    // newline
            'r'  -> return "\\r"    // carriage return
            '\"' -> return "\\\""   // double quote
            '\'' -> return "\\\'"   // single quote
            '\\' -> return "\\\\"   // backslash
            else ->
              {
                // report error but return the invalid character
                val ex = error(backslashPosition, "Illegal escape character.")
                errorHandler.reportError(ex)
                return "\\$c"
              }
          }
      }

    /**
     * Fast skip over white space.
     */
    private fun skipWhiteSpace()
      {
        while (Character.isWhitespace(source.currentChar.toChar()))
            source.advance()
      }

    /**
     * Advances over source characters to the end of the current line.
     */
    private fun skipToEndOfLine()
      {
        while (source.currentChar.toChar() != '\n')
          {
            source.advance()
            checkEOF()
          }
      }

    /**
     * Checks that the integer represents a graphic character in the
     * Unicode Basic Multilingual Plane (BMP).
     *
     * @throws ScannerException if the integer does not represent a
     *         BMP graphic character.
     */
    private fun checkGraphicChar(n : Int)
      {
        if (n == source.EOF)
            throw error("End of file reached before closing quote for Char or String literal.")
        else if (n > 0xffff)
            throw error("Character not in Unicode Basic Multilingual Pane (BMP)")
        else
          {
            val c = n.toChar()
            if (c == '\r' || c == '\n')           // special check for end of line
                throw error("Char and String literals can not extend past end of line.")
            else if (Character.isISOControl(c))   // Sorry.  No ISO control characters.
                throw error("Control characters not allowed in Char or String literal.")
          }
      }

    /**
     * Returns true only if the specified character is a letter.<br>
     * `'A'..'Z' + 'a'..'z' (r.e. char class: [A-Za-z])`
     */
    private fun isLetter(ch : Char) : Boolean = (ch in 'a'..'z') || (ch in 'A'..'Z')

    /**
     * Returns true only if the specified character is a digit.<br>
     * `'0'..'9' (r.e. char class: [0-9])`
     */
    private fun isDigit(ch : Char) : Boolean = ch in '0'..'9'

    /**
     * Returns true only if the specified character is a letter or a digit.<br>
     * `'A'..'Z' + 'a'..'z + '0'..'9' (r.e. char class: [A-Za-z0-9])`
     */
    private fun isLetterOrDigit(ch : Char) : Boolean = isLetter(ch) || isDigit(ch)

    /**
     * Returns a ScannerException with the specified error message
     * and current token position.
     */
    private fun error(message : String) : ScannerException
        = error(source.charPosition, message)

    /**
     * Returns a ScannerException with the specified error message
     * and token position.
     */
    private fun error(position : Position, message : String)
        = ScannerException(position, message)

    /**
     * Used to check for EOF in the middle of scanning tokens that
     * require closing characters such as strings and comments.
     *
     * @throws ScannerException if source is at end of file.
     */
    private fun checkEOF()
      {
        if (source.currentChar == source.EOF)
            throw error("Unexpected end of file")
      }
  }
