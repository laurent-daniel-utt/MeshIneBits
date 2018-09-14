/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * 
 */
package meshIneBits.util;

import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.Pavement;

/**
 * To determine if a bit is not regular (not having a lift point)
 * 
 * @author NHATHAN
 *
 */
public class DetectorTool {

	/**
	 * @param pavement
	 *            container of all bits in a slice, already computed (limited by
	 *            boundary)
	 * @return all irregular bits in the given layer
	 */
	public static Vector<Vector2> detectIrregularBits(Pavement pavement) {
		Vector<Vector2> result = new Vector<Vector2>();
		for (Vector2 bitKey : pavement.getBitsKeys()) {
			Bit2D bit = pavement.getBit(bitKey);
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