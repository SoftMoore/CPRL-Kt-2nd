package edu.citadel.cprl

import edu.citadel.compiler.ParserException

/**
 * The identifier table (also known as a symbol table) is used to
 * hold attributes of identifiers in the programming language CPRL.
 */
class IdTable
  {
    // IdTable is implemented as a stack of scopes.  Opening a scope
    // pushes a new scope onto the stack.  Searching for an identifier
    // involves searching at the current level (top scope in the stack)
    // and then at enclosing scopes (scopes under the top).

    private val initialScopeLevels = 3

    private val table = ArrayList<Scope>(initialScopeLevels)

    // The current level of scope.  The current level is incremented every time
    // a new scope is opened and decremented every time a scope is closed.
    private var currentLevel : Int = 0

    init
      {
        table.add(currentLevel, Scope(ScopeLevel.GLOBAL))
      }

    /**
     * The current scope level (computed property).
     */
    val scopeLevel : ScopeLevel
        get() = table[currentLevel].scopeLevel

    /**
     * Opens a new scope for identifiers.
     */
    fun openScope(scopeLevel : ScopeLevel)
      {
        ++currentLevel
        table.add(currentLevel, Scope(scopeLevel))
      }

    /**
     * Closes the outermost scope.
     */
    fun closeScope()
      {
        table.removeAt(currentLevel)
        --currentLevel
      }

    /**
     * Add an identifier and its type to the current scope.
     *
     * @throws ParserException if the identifier already exists
     *                         in the current scope.
     */
    fun add(idToken : Token, idType : IdType)
      {
        // assumes that idToken is an identifier token
        assert(idToken.symbol == Symbol.identifier)
          { "IdTable.add(): The token in the declaration is not an identifier." }

        val scope = table[currentLevel]
        val oldDecl = scope.put(idToken.text, idType)

        // check that the identifier has not been defined previously
        if (oldDecl != null)
          {
            val errorMsg = "Identifier \"${idToken.text}\" is " +
                           "already defined in the current scope."
            throw ParserException(idToken.position, errorMsg)
          }
      }

    /**
     * Returns the identifier type associated with the identifier name
     * (type String).  Returns null if the identifier is not found.
     * Searches enclosing scopes if necessary.
     */
    operator fun get(idStr : String) : IdType?
      {
        var idType : IdType? = null
        var level = currentLevel

        while (level >= 0 && idType == null)
          {
            val scope = table[level]
            idType = scope[idStr]
            --level
          }

        return idType
      }
  }
