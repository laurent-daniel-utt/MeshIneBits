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

//import java.awt.BasicStroke;
//import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
//import java.awt.geom.Path2D;
import java.util.Hashtable;
import java.util.Vector;

import meshIneBits.patterntemplates.PatternTemplate;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Logger;
//import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

/**
 * Build by a {@link PatternTemplate}. Contains a set of {@link Bit2D}.
 *
 */
public class Pattern implements Cloneable {
	private Vector2 rotation;
	/**
	 * The key is the origin of bit in coordinate system of global object.
	 */
	private Hashtable<Vector2, Bit2D> mapBits;
	private AffineTransform transfoMatrix = new AffineTransform();
	private AffineTransform inverseTransfoMatrix;

	private Pattern(Hashtable<Vector2, Bit2D> mapBits, Vector2 rotation, AffineTransform transfoMatrix,
					AffineTransform inverseTransfoMatrix) {
		this.rotation = rotation;
		this.mapBits = mapBits;
		this.transfoMatrix = transfoMatrix;
		this.inverseTransfoMatrix = inverseTransfoMatrix;
	}

	/**
	 * 
	 * @param bits
	 * @param rotation
	 */

	public Pattern(Vector<Bit2D> bits, Vector2 rotation) {

		this.rotation = rotation;

		// Each pattern can have a rotation, usually linked to the layer number
		transfoMatrix.rotate(rotation.x, rotation.y);
		// // Rotation of the whole patternTemplate
		// transfoMatrix.rotate(Vector2.getEquivalentVector(CraftConfig.rotation).x,
		// Vector2.getEquivalentVector(CraftConfig.rotation).y);
		// // Translation of the whole patternTemplate
		// transfoMatrix.translate(CraftConfig.xOffset, CraftConfig.yOffset);

		try {
			inverseTransfoMatrix = ((AffineTransform) transfoMatrix.clone()).createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}

		setMapBits(bits);
	}

	/**
	 * @param bit
	 * @return the key of inserted bit in this pattern
	 */
	public Vector2 addBit(Bit2D bit) {
		// the key of each bit is its origin's coordinates in the general coo
		// system
		Vector2 bitKey = bit.getOrigin().getTransformed(transfoMatrix);
		// We check that there is not already a bit at this place
		for (Vector2 key : getBitsKeys()) {
			if (bitKey.asGoodAsEqual(key)) {
				Logger.warning(
						"A bit already exists at these coordinates: " + key + ", it has been replaced by the new one.");
				removeBit(key);
			}

		}
		mapBits.put(bitKey, bit);
		return bitKey;
	}

	@Override
	public Pattern clone() {

		Hashtable<Vector2, Bit2D> clonedMapBits = new Hashtable<Vector2, Bit2D>();
		for (Vector2 key : this.getBitsKeys()) {
			clonedMapBits.put(key, mapBits.get(key).clone());
		}

		return new Pattern(clonedMapBits, rotation, transfoMatrix, inverseTransfoMatrix);
	}

	/**
	 * Removes the {@link Bit2D} that are outside the boundaries of the
	 * {@link Slice} and cut at right shape the ones that are on the boundaries.
	 * 
	 * @param slice
	 */
	public void computeBits(Slice slice) {
		Area sliceArea = AreaTool.getAreaFrom(slice);
		sliceArea.transform(inverseTransfoMatrix);
		// 0.1f is the smaller stroke possible
		// Shape str = new BasicStroke(0.1f).createStrokedShape(sliceArea);
		// Area cutLine = new Area(str);
		// Area cutLineClone;
		Vector<Vector2> keys = new Vector<Vector2>(mapBits.keySet());
		for (Vector2 key : keys) {
			Bit2D bit = mapBits.get(key);
			Area bitArea = new Area();
			bitArea.add(bit.getArea());
			// cutLineClone = (Area) cutLine.clone();
			// cutLineClone.intersect(bitArea);
			bitArea.intersect(sliceArea);
			if (bitArea.isEmpty()) {
				mapBits.remove(key);
				continue;
			} else {
				bit.updateBoundaries(bitArea);
				// setCutPath(cutLineClone, bitArea, key);
				bit.calcCutPath();
			}
		}
	}

	public AffineTransform getAffineTransform() {
		return transfoMatrix;
	}

	public Bit2D getBit(Vector2 key) {
		return mapBits.get(key);
	}

	public Vector<Vector2> getBitsKeys() {
		return new Vector<Vector2>(mapBits.keySet());
	}

	/**
	 * Move the chosen bit in the wanted direction. Note: not exactly "moving", but
	 * rather "removing" then "adding" new one
	 * 
	 * @param key
	 *            the key of the bit we want to move
	 * @param direction
	 *            in the local coordinate system of the bit
	 * @param offsetValue
	 *            the distance of displacement
	 * @return the key of the newly added bit
	 */
	public Vector2 moveBit(Vector2 key, Vector2 direction, double offsetValue) {
		Bit2D bitToMove = mapBits.get(key);
		removeBit(key);
		Vector2 localDirection = bitToMove.getOrientation();
		AffineTransform rotateMatrix = new AffineTransform();
		rotateMatrix.rotate(direction.x, direction.y);
		localDirection = localDirection.getTransformed(rotateMatrix);
		localDirection = localDirection.normal();
		Vector2 newCenter = new Vector2(bitToMove.getOrigin().x + (localDirection.x * offsetValue),
				bitToMove.getOrigin().y + (localDirection.y * offsetValue));
		return addBit(new Bit2D(newCenter, bitToMove.getOrientation(), bitToMove.getLength(), bitToMove.getWidth()));
	}

	public void removeBit(Vector2 key) {
		mapBits.remove(key);
	}

	// private void setCutPath(Area cutLineStroke, Area bitArea, Vector2 key) {
	// Vector<Vector<Segment2D>> edges = AreaTool.getSegmentsFrom(bitArea);
	//
	// Vector<Segment2D> cutLine = new Vector<Segment2D>();
	//
	// for (Vector<Segment2D> polygon : edges) {
	// for (Segment2D edge : polygon) {
	// // System.out.println(edge);
	// if (cutLineStroke.contains(edge.getMidPoint().x, edge.getMidPoint().y)) {
	// cutLine.add(edge);
	// }
	// }
	// }
	//
	// Vector<Path2D> cutPaths = new Vector<Path2D>();
	//
	// if (cutLine.isEmpty()) {
	// return;
	// } else if (cutLine.size() == 1) {
	// Path2D cutPath2D = new Path2D.Double();
	// cutPath2D.moveTo(cutLine.get(0).start.x, cutLine.get(0).start.y);
	// cutPath2D.lineTo(cutLine.get(0).end.x, cutLine.get(0).end.y);
	// cutPaths.addElement(cutPath2D);
	// mapBits.get(key).setCutPath(cutPaths);
	// return;
	// }
	//
	// Vector<Vector<Segment2D>> cutLines = Segment2D.segregateSegments(cutLine);
	//
	// for (Vector<Segment2D> pathLine : cutLines) {
	// Path2D cutPath2D = new Path2D.Double();
	// cutPath2D.moveTo(pathLine.get(0).start.x, pathLine.get(0).start.y);
	// for (int i = 1; i < pathLine.size(); i++) {
	// cutPath2D.lineTo(pathLine.get(i).start.x, pathLine.get(i).start.y);
	// }
	// cutPath2D.lineTo(pathLine.get(pathLine.size() - 1).end.x,
	// pathLine.get(pathLine.size() - 1).end.y);
	// cutPaths.add(cutPath2D);
	// }
	//
	// mapBits.get(key).setCutPath(cutPaths);
	//
	// }

	private void setMapBits(Vector<Bit2D> bits) {
		mapBits = new Hashtable<Vector2, Bit2D>();
		for (Bit2D bit : bits) {
			addBit(bit);
		}
	}

}
