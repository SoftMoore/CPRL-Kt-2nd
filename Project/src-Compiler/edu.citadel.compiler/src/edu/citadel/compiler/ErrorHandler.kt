package edu.citadel.compiler


import java.io.PrintWriter
import java.nio.charset.StandardCharsets


/**
 * This class handles the reporting of error messages.
 */
class ErrorHandler
  {
    /** Maximum number of errors to be reported. */
    private val MAX_ERRORS = 15

    private val err = PrintWriter(System.err, true, StandardCharsets.UTF_8)
    private var errorCount    = 0
    private var lastMessage   = ""   // remember last error message
    private var undeclaredIds = mutableSetOf<String>()


    /**
     * Returns true if errors have been reported by the error handler.
     */
    fun errorsExist() : Boolean = errorCount > 0


    /**
     * Reports the error.
     *
     * @throws FatalException if the maximum number of errors have been reported.
     */
    fun reportError(e : CompilerException)
      {
        if (errorCount <= MAX_ERRORS)
          {
            if (shouldPrint(e.message))
              {
                err.println(e.message)
                ++errorCount
                lastMessage = e.message ?: ""
              }
           }
        else
            throw FatalException("Max errors exceeded.")
      }


    /**
     * Reports the fatal error.
     */
    fun reportFatalError(e : FatalException) = err.println(e.message)


    /**
     * Prints the specified message and continues compilation.
     */
    fun printMessage(message : String) = err.println(message)


    /*
     * Checks for repeated error messages and error messages of
     * the form "Identifier \"x\" has not been declared.".
     * Returns true if this error message should be printed.
     */
    private fun shouldPrint(message: String?): Boolean
      {
        if (message == null || message == lastMessage)
            return false

        lastMessage = message

        // check for messages of the form "Identifier \"x\" has not been declared."
        val endIndex = message.indexOf("\" has not been declared.")
        if (endIndex < 0)
            return true

        val beginIndex = message.indexOf('\"') + 1
        if (beginIndex < endIndex)
          {
            val idName = message.substring(beginIndex, endIndex)
            if (undeclaredIds.contains(idName))
                return false
            else
              {
                undeclaredIds.add(idName)
                return true
              }
          }

        return true
      }
  }
