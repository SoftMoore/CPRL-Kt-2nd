package edu.citadel.compiler


/**
 * Class for exceptions encountered during parsing.
 *
 * @constructor Construct a parser exception with the specified position
 *              and error message.
 *
 * @param position the position in the source file where the error was detected.
 * @param errorMsg a brief message about the nature of the error.
 */
class ParserException(position : Position, errorMsg : String)
    : CompilerException("Syntax", position, errorMsg)
