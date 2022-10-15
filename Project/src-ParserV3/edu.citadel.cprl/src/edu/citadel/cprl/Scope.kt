package edu.citadel.cprl

import edu.citadel.cprl.ast.Declaration

/**
 * This class encapsulates a scope in CPRL.
 */
class Scope(val scopeLevel : ScopeLevel) : HashMap<String, Declaration>()
