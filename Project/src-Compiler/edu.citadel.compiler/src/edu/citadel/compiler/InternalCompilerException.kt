package edu.citadel.compiler

/**
 * Class for exceptions encountered within the compiler.  These
 * exceptions represent problems with the implementation of the compiler
 * and should never occur if the compiler is implemented correctly.
 *
 * @constructor Construct an internal compiler exception with the specified
 *              position and error message.
 */
class InternalCompilerException(position: Position, errorMsg: String)
    : RuntimeException("*** Internal Compiler Error near $position:\n    $errorMsg")
