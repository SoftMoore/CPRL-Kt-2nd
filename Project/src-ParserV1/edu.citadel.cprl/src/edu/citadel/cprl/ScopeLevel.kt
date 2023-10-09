package edu.citadel.cprl

/**
 * An enum class for the three scope levels in CPRL.
 */
enum class ScopeLevel(val text : String)
  {
    GLOBAL("global"),
    LOCAL("local"),
    RECORD("record");

    /**
     * Returns a "nice" string for the name of the scope type.  For
     * example, this method returns "local" instead of "LOCAL".
     */
    override fun toString() = text
  }
