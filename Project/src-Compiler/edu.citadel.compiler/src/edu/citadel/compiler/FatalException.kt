package edu.citadel.compiler

/**
 * Class for fatal exceptions encountered during compilation.
 * A fatal exception is one for which compilation of the source
 * file should be abandoned.
 *
 * @constructor Construct a fatal exception with the specified error message.
 *
 * @param errorMsg  a brief message about the nature of the error.
 */
class FatalException(errorMsg : String)
    : CompilerException("*** Fatal exception: $errorMsg")
