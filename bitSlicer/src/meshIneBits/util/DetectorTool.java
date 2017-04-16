/**
 * 
 */
package meshIneBits.util;

import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.Pattern;
import meshIneBits.Slicer.Slice;

/**
 * To determine if a bit is not regular (not having a lift point, overlapping
 * another, etc.)
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
			if (bit.computeLiftPoint() == null) {
				result.add(bitKey);
			}
		}
		return result;
	}
	
	/**
	 * @param pattern container of all bits in a slice
	 * @param boundary
	 * @return all irregular bits in the given slice
	 */
	public static Vector<Vector2> detectIrregularBits(Pattern pattern, Slice boundary){
		Pattern clone = pattern.clone();
		clone.computeBits(boundary);
		Vector<Vector2> result = new Vector<Vector2>();
		for (Vector2 bitKey : clone.getBitsKeys()) {
			Bit2D bit = pattern.getBit(bitKey);
			if (bit.computeLiftPoint() == null) {
				result.add(bitKey);
			}
		}
		return result;
	}

}
