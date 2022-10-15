package edu.citadel.compiler

/**
 * Class for exceptions encountered during code generation.
 *
 * @constructor  Construct a code generation exception with the
 *               specified position and error message.
 */
class CodeGenException(position: Position, errorMsg: String)
    : CompilerException("Code Generation", position, errorMsg)
