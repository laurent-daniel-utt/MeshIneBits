/**
 * 
 */
package meshIneBits.util;

import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.Pattern;

/**
 * To determine if a bit is not regular (not having a lift point)
 * 
 * @author NHATHAN
 *
 */
public class DetectorTool {

	/**
	 * @param pattern
	 *            container of all bits in a slice, already computed (limited by
	 *            boundary)
	 * @return all irregular bits in the given layer
	 */
	public static Vector<Vector2> detectIrregularBits(Pattern pattern) {
		Vector<Vector2> result = new Vector<Vector2>();
		for (Vector2 bitKey : pattern.getBitsKeys()) {
			Bit2D bit = pattern.getBit(bitKey);
			if (checkIrregular(bit)) {
				result.add(bitKey);
			}
		}
		return result;
	}

	// /**
	// * Detect if <tt>bit</tt> is irregular by comparing the number of lift
	// * points on <tt>bit</tt> and its separated areas' one (each separated area
	// * has no more than 1 lift point).
	// *
	// * @param bit
	// * @return <tt>true</tt> if those 2 numbers are different
	// */
	/**
	 * Check if a bit is regular.
	 * <ol>
	 * <li>Only one area per bit</li>
	 * <li>Area is large enough to contain one lift point</li>
	 * </ol>
	 * 
	 * @param bit
	 * @return <tt>true</tt> if this bit is irregular, <tt>false</tt> otherwise.
	 */
	public static boolean checkIrregular(Bit2D bit) {
		// int numLiftPoints = bit.computeLiftPoints().size(), numLevel0Areas =
		// bit.getRawAreas().size();
		// return (numLiftPoints != numLevel0Areas);
		if (bit.getRawAreas().size() != 1)
			return true;
		if (bit.computeLiftPoint() == null)
			return true;
		return false;
	}
}