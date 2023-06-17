package edu.citadel.cprl

/**
 * Bounded circular buffer for tokens.
 *
 * @constructor Construct buffer with the specified capacity.
 */
class TokenBuffer(private val capacity : Int)
  {
    private val buffer : Array<Token> = Array(capacity) { Token() }
    private var tokenIndex = 0   // circular index

    /**
     * Return the token at index i.  Does not remove the token.
     */
    operator fun get(i : Int) : Token = buffer[(tokenIndex + i) % capacity]

    /**
     * Add a token to the buffer.  Overwrites if the buffer is full.
     */
    fun add(token : Token)
      {
        buffer[tokenIndex] = token
        tokenIndex = (tokenIndex + 1) % capacity
      }
  }
