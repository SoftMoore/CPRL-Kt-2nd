package edu.citadel.cprl.ast

import edu.citadel.cprl.Token
import edu.citadel.cprl.Type

/**
 * Base class for all initial declarations.
 *
 * @constructor Construct an initial declaration with its identifier and type.
 */
abstract class InitialDecl(identifier : Token, declType : Type)
    : Declaration(identifier, declType)
