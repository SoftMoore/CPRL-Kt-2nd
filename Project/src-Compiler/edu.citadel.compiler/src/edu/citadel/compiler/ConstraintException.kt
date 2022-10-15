package edu.citadel.compiler

/**
 * Class for exceptions encountered during constraint analysis.
 */
class ConstraintException : CompilerException
  {
    /**
     * Construct a constraint exception with the specified position
     * and error message.
     *
     * @param position  the position in the source file where the error was detected.
     * @param errorMsg  a brief message about the nature of the error.
     */
    constructor(position : Position, errorMsg : String)
        : super("Constraint", position, errorMsg)

    /**
     * Construct a constraint exception with the specified error message.
     *
     * @param errorMsg a brief message about the nature of the error.
     */
    constructor(errorMsg : String) : super("Constraint", errorMsg)
  }
