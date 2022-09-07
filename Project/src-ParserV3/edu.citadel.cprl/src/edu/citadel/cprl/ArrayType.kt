package edu.citadel.cprl


/**
 * This class encapsulates the language concept of an array type
 * in the programming language CPRL.
 *
 * @constructor Construct an array type with the specified type name,
 *              number of elements, and the type of elements contained
 *              in the array.
 */
class ArrayType(typeName : String, numElements : Int, val elementType : Type)
    : Type(typeName, 0)
// ... In call to superclass constructor, 0 is not correct as the size for the array type.
// ... What is the size for the array type?  Hint: Read the book.
