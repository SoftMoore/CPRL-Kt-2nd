package edu.citadel.assembler

import edu.citadel.compiler.Source
import edu.citadel.compiler.Position
import edu.citadel.compiler.ErrorHandler
import edu.citadel.compiler.ScannerException

import java.io.*

import java.util.Locale

import kotlin.system.exitProcess

/**
 * Performs lexical analysis for CVM assembly language.
 *
 * @constructor Construct scanner with its associated source and error handler.
 */
class Scanner(sourceFile : File, private val errorHandler : ErrorHandler)
  {
    private lateinit var source : Source
    private val scanBuffer : StringBuilder = StringBuilder(100)

    // maps opcode names to opcode symbols
    private val opcodeMap : HashMap<String, Symbol> = HashMap(100)

    init
      {
        val fileReader = FileReader(sourceFile)
        val reader = BufferedReader(fileReader)
        source = Source(reader)

        // initialize opcodeMap with reserved word symbols
        val symbols = Symbol.values()
        for (symbol in symbols)
          {
            if (symbol.isOpcode)
                opcodeMap[symbol.toString()] = symbol
          }

        advance()   // advance to the first token
      }

    /**  The current token in the source file.  */
    lateinit var token : Token
        private set

    /**  Short version for token.symbol, the next lookahead symbol. */
    val symbol : Symbol
        get() = token.symbol

    /**  Short version for token.text, the next lookahead text. */
    val text : String
        get() = token.text

    /**  Short version for token.position, the next lookahead position. */
    val position : Position
        get() = token.position

    /**
     * Advance scanner to the next token.
     */
    fun advance()
      {
        token = nextToken()
      }

    /**
     * Advance until lookahead(1).symbol matches one of the symbols
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
        val position : Position
        var text = ""

        try
          {
            skipWhiteSpace()

            // currently at starting character of next token
            position = source.charPosition

            if (source.currentChar == source.EOF)
              {
                // set symbol but don't advance
                symbol = Symbol.EOF
              }
            else if (Character.isLetter(source.currentChar.toChar())
                  || source.currentChar.toChar() == '_')
              {
                // opcode symbol, identifier, or label
                val idString = scanIdentifier()
                symbol= getIdentifierSymbol(idString)

                if (symbol == Symbol.identifier)
                  {
                    // check to see if we have a label
                    if (source.currentChar.toChar() == ':')
                      {
                        symbol = Symbol.labelId
                        text = "$idString:"
                        source.advance()
                      }
                    else
                        text = idString
                  }
              }
            else if (Character.isDigit(source.currentChar.toChar()))
              {
                symbol = Symbol.intLiteral
                text   = scanIntegerLiteral()
              }
            else
              {
                when (source.currentChar.toChar())
                  {
                    ';' ->  {
                              skipComment()
                              return nextToken()   // continue scanning for next token
                            }
                    '\'' -> {
                              symbol = Symbol.charLiteral
                              text   = scanCharLiteral()
                            }
                    '\"' -> {
                              symbol = Symbol.stringLiteral
                              text   = scanStringLiteral()
                            }
                    '-' ->  {
                              // should be a negative integer literal
                              source.advance()
                              if (Character.isDigit(source.currentChar.toChar()))
                                {
                                  symbol = Symbol.intLiteral
                                  text = "-" + scanIntegerLiteral()
                                }
                              else
                                  throw error("Expecting an integer literal")
                            }
                    else -> {
                              throw error(position, "Invalid Token")
                            }
                  }
              }
          }
        catch (e : ScannerException)
          {
            // stop on first error -- no error recovery
            errorHandler.reportError(e)
            exitProcess(1)
          }

        return Token(symbol, position, text)
      }

    /**
     * Clear the scan buffer (makes it empty).
     */
    private fun StringBuilder.clear() = delete(0, length)

    /**
     * Scans characters in the source file for a valid identifier using the
     * lexical rule: identifier = (letter | "_" ) ( letter | digit )* .
     */
    private fun scanIdentifier() : String
      {
        // assumes that that source.currentChar is the first character of the identifier
        assert(Character.isLetter(source.currentChar.toChar())
            || source.currentChar.toChar() == '_')
          { "Check identifier start for letter at position $position." }

        scanBuffer.clear()

        do
          {
            scanBuffer.append(source.currentChar.toChar())
            source.advance()
          }
        while (Character.isLetterOrDigit(source.currentChar.toChar()))

        return scanBuffer.toString()
      }

    /**
     * Scans characters in the source file for a valid integer literal.  Assumes
     * that source.currentChar is the first character of the Integer literal.
     *
     * @return the string of digits for the integer literal.
     */
    private fun scanIntegerLiteral() : String
      {
        // assumes that source.currentChar is the first digit of the integer literal
        assert(Character.isDigit(source.currentChar.toChar()))
          { "Check integer literal start for digit at position $position." }

        scanBuffer.clear()

        do
          {
            scanBuffer.append(source.currentChar.toChar())
            source.advance()
          }
        while (Character.isDigit(source.currentChar.toChar()))

        return scanBuffer.toString()
      }

    private fun skipComment()
      {
        // assumes that source.currentChar is the leading ';'
        assert(source.currentChar.toChar() == ';')
          { "Check for ';' to start comment." }

        skipToEndOfLine()
        source.advance()
      }

    /**
     * Scans characters in the source file for a String literal.
     * Escaped characters are converted; e.g., '\t' is converted to
     * the tab character.  Assumes that that source.currentChar is
     * the opening quote (") of the String literal.
     *
     * @return the string of characters for the string literal, including
     * opening and closing quotes
     */
    private fun scanStringLiteral() : String
      {
        // assumes that that source.currentChar is the
        // opening double quote for the string literal
        assert(source.currentChar.toChar() == '\"')
          { "Check for opening quote (\") at position $position." }

        scanBuffer.clear()

        do
          {
            checkGraphicChar(source.currentChar)
            val c = source.currentChar.toChar()

            if (c == '\\')
                scanBuffer.append(scanEscapedChar())   // call to scanEscapedChar() advances source
            else
              {
                scanBuffer.append(c)
                source.advance()
              }
          }
        while (source.currentChar.toChar() != '\"')

        scanBuffer.append('\"')     // append closing quote
        source.advance()

        return scanBuffer.toString()
      }

    /**
     * Scans characters in the source file for a valid char literal.
     * Escaped characters are converted; e.g., '\t' is converted to
     * the tab character.  Assumes that that source.currentChar is
     * the opening single quote (') of the Char literal.
     *
     * @return the string of characters for the char literal, including
     * opening and closing single quotes.
     */
    private fun scanCharLiteral() : String
      {
        // assumes that that source.currentChar is the
        // opening single quote for the char literal
        assert(source.currentChar.toChar() == '\'')
          { "Check for opening quote (\') at position $position." }

        scanBuffer.clear()

        var c = source.currentChar.toChar()        // opening quote
        scanBuffer.append(c)                       // append the opening quote

        source.advance()
        checkGraphicChar(source.currentChar)
        c = source.currentChar.toChar()            // the character literal

        if (c == '\\')
          {
            // escaped character
            scanBuffer.append(scanEscapedChar())   // call to scanEscapedChar() advances source
          }
        else if (c == '\'')                        // check for empty char literal
          {
            source.advance()
            throw error("Char literal must contain exactly one character")
          }
        else
          {
            scanBuffer.append(c)                   // append the character literal
            source.advance()
          }

        checkGraphicChar(source.currentChar)
        c = source.currentChar.toChar()            // should be the closing quote

        if (c == '\'')                             // should be the closing quote
          {
            scanBuffer.append(c)                   // append the closing quote
            source.advance()
          }
        else
            throw error("Char literal not closed properly")

        return scanBuffer.toString()
      }

    /**
     * Scans characters in the source file for an escaped character; i.e.,
     * a character preceded by a backslash.  This method handles escape
     * characters \t, \n, \r, \", \', and \\.  If the character following
     * a backslash is anything other than one of these characters, then an
     * exception is thrown.  Assumes that that source.currentChar is the
     * escape character (\).
     *
     * @return the value for an escaped character.
     */
    private fun scanEscapedChar() : Char
      {
        // assumes that that source.currentChar is a backslash character
        assert(source.currentChar.toChar() == '\\')
          { "Check for escape character ('\\') at position $position." }

        // Need to save current position for error reporting.
        val backslashPosition = source.charPosition

        source.advance()
        checkGraphicChar(source.currentChar)
        val c = source.currentChar.toChar()

        source.advance()   // leave source at second character following the backslash

        return when (c)
          {
            't'  -> '\t'     // tab
            'n'  -> '\n'     // newline
            'r'  -> '\r'     // carriage return
            '\"' -> '\"'     // double quote
            '\'' -> '\''     // single quote
            '\\' -> '\\'     // backslash
            else -> throw ScannerException(backslashPosition, "Illegal escape character.")
          }
      }

    /**
     * Returns the symbol associated with an identifier
     * (Symbol.ADD, Symbol.AND, Symbol.identifier, etc.)
     */
    private fun getIdentifierSymbol(idString : String) : Symbol
      {
        val tempIdString = idString.uppercase(Locale.getDefault())
        return opcodeMap.getOrDefault(tempIdString, Symbol.identifier)
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
     * Checks that the integer represents a graphic character in the Unicode
     * Basic Multilingual Plane (BMP).
     *
     * @throws ScannerException  if the integer does not represent a BMP
     *                           graphic character.
     */
    private fun checkGraphicChar(n : Int)
      {
        if (n == source.EOF)
            throw error("End of file reached before closing quote.")
        else if (n > 0xffff)
            throw error("Character not in Unicode Basic Multilingual Pane (BMP)")
        else
          {
            val c = n.toChar()
            if (c == '\r' || c == '\n')
              {
                // special check for end of line
                throw error("Char and String literals can not extend past end of line.")
              }
            else if (Character.isISOControl(c))
              {
                // Sorry.  No ISO control characters.
                throw ScannerException(source.charPosition,
                    "Control characters not allowed in Char or String literal.")
              }
          }
      }

    /**
     * Returns a ScannerException with the specified error message.
     */
    private fun error(errorMsg : String) : ScannerException = error(position, errorMsg)

    /**
     * Returns a ScannerException with the specified position and  error message.
     */
    private fun error(errorPos : Position, errorMsg : String) : ScannerException =
            ScannerException(errorPos, errorMsg)

    /**
     * Used to check for EOF in the middle of scanning tokens that
     * require closing characters such as strings and comments.
     *
     * @throws ScannerException  if source is at end of file.
     */
    private fun checkEOF()
      {
        if (source.currentChar == source.EOF)
            throw error("Unexpected end of file")
      }
  }
