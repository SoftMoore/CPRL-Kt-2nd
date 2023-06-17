package edu.citadel.cprl

import edu.citadel.cvm.Constants

/**
 * This class encapsulates the language types for the programming language CPRL.
 * Type sizes are initialized to values appropriate for the CPRL virtual machine.
 *
 * @constructor Construct a new type with the specified type name and size.
 */
open class Type protected constructor(val typeName : String, var size : Int = 0)
  {
    /**
     * Returns true if and only if this type is a scalar type.
     * The scalar types in CPRL are Integer, Boolean, and Char.
     */
    val isScalar : Boolean
        get() = this == Integer || this == Boolean || this == Char

    /**
     * Returns the name for this type.
     */
    override fun toString() : String = typeName

    override fun hashCode() : Int = typeName.hashCode()

    override fun equals(other : Any?) : Boolean =
        this === other || (other is Type && typeName == other.typeName)

    companion object
      {
        // predefined types
        val Boolean = Type("Boolean", Constants.BYTES_PER_BOOLEAN)
        val Integer = Type("Integer", Constants.BYTES_PER_INTEGER)
        val Char    = Type("Char",    Constants.BYTES_PER_CHAR)

        // an address of the target machine
        val Address = Type("Address", Constants.BYTES_PER_ADDRESS)

        // compiler-internal types
        val UNKNOWN = Type("UNKNOWN")
        val none    = Type("none")

        /**
         * String literals contain quotes and possibly escape characters,
         * so we need to compute the actual capacity for the string type.
         */
        private fun capacityOf(literalText : String) : Int
          {
            // subtract 2 for the double quotes at each end
            var capacity = literalText.length - 2

            // assume that the literal text was parsed correctly by the compiler
            var i = 1
            while (i < literalText.length - 3)
              {
                if (literalText[i] == '\\')
                  {
                    --capacity    // subtract for an escaped character
                    ++i           // skip over the escaped character
                  }

                ++i
              }

            return capacity
          }

        /**
         * Returns the type of a literal symbol.  For example, if the
         * symbol is an intLiteral, then Type.Integer is returned.
         * Returns UNKNOWN if the symbol is not a valid literal symbol.
         */
        fun typeOf(literal : Token) : Type
          {
            when (literal.symbol)
              {
                Symbol.intLiteral    -> return Integer
                Symbol.charLiteral   -> return Char
                Symbol.trueRW,
                Symbol.falseRW       -> return Boolean
                Symbol.stringLiteral -> return StringType(capacityOf(literal.text))
                else                 -> return UNKNOWN
              }
          }
      }
  }
