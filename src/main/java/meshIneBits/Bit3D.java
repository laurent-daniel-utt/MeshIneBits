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

package meshIneBits;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Vector;

import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Vector2;

/**
 * A bit 3D is the equivalent of a real wood bit. The 3D shape is determined by
 * extrusion of a {@link Bit2D} : the {@link bit2dToExtrude}
 *
 */
public class Bit3D {

	private Vector<Path2D> cutPaths = null; // In the local coordinate system
	private Vector2 origin; // Position of the center of the full bit in the
							// general coordinate system
	private Vector2 orientation; // Rotation around its local origin point
	private Bit2D bit2dToExtrude;
	private Vector<Vector2> liftPoints = new Vector<Vector2>();
	private Vector<Vector2> depositPoints = new Vector<Vector2>();

	/**
	 * 2 reasons to have all the included bit2D in parameter : - It is the Bit3D
	 * itself which determines if it can be build or not, therefore it needs all
	 * the information to do so. - The next improvement will be a 3D laser cut
	 * of the bits, in that scenario every bit2D are necessary to determine the
	 * 3D shape of the bit
	 * 
	 * Refer to the report for an explanation on the parameter sliceToSelect.
	 * 
	 * @param bits2D
	 * @param origin
	 * @param orientation
	 * @param sliceToSelect
	 * @throws Exception
	 */
	Bit3D(Vector<Bit2D> bits2D, Vector2 origin, Vector2 orientation, int sliceToSelect) throws Exception {

		double maxNumberOfbit2D = bits2D.size();
		double nbrActualBit2D = 0;
		for (Bit2D b : bits2D) {
			if (b != null) {
				nbrActualBit2D++;
			}
		}

		double percentage = (nbrActualBit2D / maxNumberOfbit2D) * 100;

		if (percentage < CraftConfig.minPercentageOfSlices) {
			throw new Exception() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getMessage() {
					return "This bit does not contain enough bit 2D in it";
				}
			};
		} else if (sliceToSelect >= bits2D.size() || bits2D.get(sliceToSelect) == null) {
			throw new Exception() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getMessage() {
					return "The slice to select does not exist in that bit";
				}
			};
		} else {
			this.origin = origin;
			this.orientation = orientation;
			bit2dToExtrude = bits2D.get(sliceToSelect);
			cutPaths = bit2dToExtrude.getRawCutPaths();
		}
	}

	/**
	 * Construct bit 3D from horizontal section
	 *
	 * @param baseBit     horizontal section of bit
	 * @param layerBorder layer's boundary
	 */
	Bit3D(Bit2D baseBit, Slice layerBorder) {
		// TODO construct bit 3D from a horizontal section within border of layer
	}

	private Vector2 computeLiftPoint(Area subBit) {
		return AreaTool.getLiftPoint(subBit, CraftConfig.suckerDiameter / 2);
	}

	/**
	 * Calculate the lift point, which is the best position to grip
	 * the bit by vacuuming.
	 */
	void computeLiftPoints() {
		for (Area subBit : bit2dToExtrude.getRawAreas()) {
			Vector2 liftPoint = computeLiftPoint(subBit);
			liftPoints.add(liftPoint);
			if (liftPoint != null) {
				// A new lift point means a new deposit point which is the
				// addition of the origin point of the bit and the lift point
				// (which is in the local coordinate system of the bit)
				depositPoints.add(origin.add(new Vector2(liftPoints.lastElement().x, liftPoints.lastElement().y)));
			} else {
				depositPoints.addElement(null);
			}
		}
	}

	Bit2D getBit2dToExtrude() {
		return bit2dToExtrude;
	}

	public Vector<Path2D> getCutPaths() {
		return cutPaths;
	}

	public Vector<Vector2> getDepositPoints() {
		return depositPoints;
	}

	public Vector<Vector2> getLiftPoints() {
		return liftPoints;
	}

	public Vector2 getOrientation() {
		return orientation;
	}

	public Vector2 getOrigin() {
		return origin;
	}

	public Area getRawArea() {
		return bit2dToExtrude.getRawArea();
	}

	public Vector2 getRotation() {
		return orientation;
	}
}
