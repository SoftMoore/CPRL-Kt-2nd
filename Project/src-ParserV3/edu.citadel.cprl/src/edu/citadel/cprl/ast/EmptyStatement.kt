package edu.citadel.cprl.ast

/**
 * An empty statement passes constraint checks and emits no code.
 * It is returned from parsing statements as an alternative to
 * returning null when parsing errors are encountered.
 */
object EmptyStatement : Statement()
  {
    override fun checkConstraints()
      {
        // nothing to check
      }

    override fun emit()
      {
        // nothing to emit
      }
  }
