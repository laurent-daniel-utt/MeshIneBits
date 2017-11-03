/**
 * A pattern template describe how the space is filled with Bit2D
 * There is only one instance by part, it is determined at the beginning and cannot be changed during the process.
 * Each pattern (one by slice) will ask this object to build the appropriate pattern of bits.
 * It will ensure that each floor of bits is coherent with the others.
 * This PatternTemplate can have a rotation around the Z-axis of the part and an offset regarding the part origin.
 */

package meshIneBits.patterntemplates;