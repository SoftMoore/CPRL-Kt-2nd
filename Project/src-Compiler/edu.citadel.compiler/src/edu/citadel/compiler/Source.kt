package edu.citadel.compiler

import java.io.Reader

/**
 * This class encapsulates the source file reader.  It maintains
 * the position of each character in the source file.
 *
 * @constructor Initialize source with a reader.
 */
class Source(private val sourceReader : Reader)
  {
    /** A constant representing end of file. */
    val EOF = -1

    /**
     * An integer representing the current character in the source file.
     * This property has the value EOF (-1) if the end of file has been
     * reached.
     */
    var currentChar = 0
        private set

    /** The source line number of the current character. */
    private var lineNumber = 1

    /** The offset of the current character within its line. */
    private var charNumber = 0

    /**
     * The position (line number, char number) of the current
     * character in the source file.
     */
    val charPosition : Position
        get() = Position(lineNumber, charNumber)

    /**
     * Advance to the first character.
     */
    init
      {
        advance()   // advance to the first character
      }

    /**
     * Advance to the next character in the source file.
     */
    fun advance()
      {
        if (currentChar == '\n'.code)
          {
            ++lineNumber
            charNumber = 1
          }
        else
            ++charNumber

        currentChar = sourceReader.read()
      }
  }
