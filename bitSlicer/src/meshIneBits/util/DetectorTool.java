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
	 * @return all irregular bits in the given slice
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

	/**
	 * Compare the number of lift points on <tt>bit</tt> (each separated area
	 * has no more than 1 lift point) and its areas' one.
	 * 
	 * @param bit
	 * @return <tt>true</tt> if those 2 numbers are different
	 */
	public static boolean checkIrregular(Bit2D bit) {
		int numLiftPoints = bit.computeLiftPoints().size(), numLevel0Areas = bit.getRawAreas().size();
		return (numLiftPoints != numLevel0Areas);
	}
}
