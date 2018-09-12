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

package meshIneBits.patterntemplates;

import java.util.Iterator;
import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.Mesh;
import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.slicer.Slice;
import meshIneBits.util.DetectorTool;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;

/**
 * Pattern improved from classic pattern
 * {@link meshIneBits.patterntemplates.ClassicBrickPattern ClassicBrickPattern}.
 * 
 * @author NHATHAN
 *
 */
public class ImprovedBrickPattern extends PatternTemplate {

	private Vector2 patternStart;
	private Vector2 patternEnd;
	private double skirtRadius;
	private double bitsWidthSpace;
	private double bitsLengthSpace;
	private double diffxOffset;
	private double diffyOffset;
	private double diffRotation;

	public Pattern createPattern(int layerNumber) {
		Vector<Bit2D> bits = new Vector<Bit2D>();
		// Setup parameters
		bitsWidthSpace = (double) config.get("bitsWidthSpace").getCurrentValue();
		bitsLengthSpace = (double) config.get("bitsLengthSpace").getCurrentValue();
		diffxOffset = (double) config.get("diffxOffset").getCurrentValue();
		diffyOffset = (double) config.get("diffyOffset").getCurrentValue();
		diffRotation = (double) config.get("diffRotation").getCurrentValue();
		// The first bit is displaced by multiples of diffxOffset and
		// diffyOffset
		Vector2 _1stBit = new Vector2(diffxOffset * layerNumber % CraftConfig.bitLength,
				diffyOffset * layerNumber % CraftConfig.bitWidth);
		// Fill out the square
		int lineNum = 0;// Initialize
		// Vertically downward
		while (_1stBit.y - CraftConfig.bitWidth / 2
				+ lineNum * (CraftConfig.bitWidth + bitsLengthSpace) <= patternEnd.y) {
			// Horizontally
			if (lineNum % 2 == 0) {
				fillHorizontally(new Vector2(_1stBit.x, _1stBit.y + lineNum * (CraftConfig.bitWidth + bitsLengthSpace)),
						bits);
			} else {
				fillHorizontally(new Vector2(_1stBit.x + CraftConfig.bitLength / 2 + bitsWidthSpace / 2,
						_1stBit.y + lineNum * (CraftConfig.bitWidth + bitsLengthSpace)), bits);
			}
			lineNum++;
		}
		// Vertically upward
		lineNum = 1; // Reinitialize
		while (_1stBit.y + CraftConfig.bitWidth / 2
				- lineNum * (CraftConfig.bitWidth + bitsLengthSpace) >= patternStart.y) {
			// Horizontally
			if (lineNum % 2 == 0) {
				fillHorizontally(new Vector2(_1stBit.x, _1stBit.y - lineNum * (CraftConfig.bitWidth + bitsLengthSpace)),
						bits);
			} else {
				fillHorizontally(new Vector2(_1stBit.x + CraftConfig.bitLength / 2 + bitsWidthSpace / 2,
						_1stBit.y - lineNum * (CraftConfig.bitWidth + bitsLengthSpace)), bits);
			}
			lineNum++;
		}

		// Rotation for this layer
		Vector2 customizedRotation = Vector2.getEquivalentVector((diffRotation * layerNumber) % 360);
		return new Pattern(bits, customizedRotation);
	}

	/**
	 * Fill a line of bits into set of bits, given the origin of the first bit.
	 * 
	 * @param _1stBitOrigin
	 *            origin of departure
	 * @param bits
	 *            set of bits of this layer
	 */
	private void fillHorizontally(Vector2 _1stBitOrigin, Vector<Bit2D> bits) {
		double L = CraftConfig.bitLength;
		double f = bitsWidthSpace;
		// To the right
		int colNum = 0; // Initialize
		while (_1stBitOrigin.x - L / 2 + colNum * (L + f) <= patternEnd.x) {
			bits.add(new Bit2D(new Vector2(_1stBitOrigin.x + colNum * (L + f), _1stBitOrigin.y), new Vector2(1, 0)));
			colNum++;
		}
		// To the left
		colNum = 1; // Reinitialize
		while (_1stBitOrigin.x + L / 2 - colNum * (L + f) >= patternStart.x) {
			bits.add(new Bit2D(new Vector2(_1stBitOrigin.x - colNum * (L + f), _1stBitOrigin.y), new Vector2(1, 0)));
			colNum++;
		}
	}


	@Override
	public int optimize(Layer actualState) {
		Logger.updateStatus("Optimizing layer " + actualState.getLayerNumber());
		// this boolean to check that if we'd tried all possibilities
		boolean allFail = false;
		while (!allFail) {
			Pattern selectedPattern = actualState.getSelectedPattern();
			Slice selectedBoundary = actualState.getSelectedSlice();
			Vector2 localDirectionToMove = null, irBitKeyToMove = null;
			Vector<Vector2> irregularBitKeys = DetectorTool.detectIrregularBits(selectedPattern);
			// We will find the first irregular bit that we can resolve
			for (Iterator<Vector2> irBitKeyIterator = irregularBitKeys.iterator(); irBitKeyIterator.hasNext();) {
				Vector2 irBitKey = (Vector2) irBitKeyIterator.next();
				// We try to move this irregular bit in 4 directions, starting
				// with height's sides.
				// If there is at least one way to reduce the number of
				// irregular bits in the pattern,
				// we choose that direction and apply on the pattern
				localDirectionToMove = attemptToSolve(selectedPattern, selectedBoundary, irBitKey);
				if (localDirectionToMove != null) {
					irBitKeyToMove = irBitKey;
					break;
				}
			}
			// If we have at least one chance to move
			if (localDirectionToMove != null && irBitKeyToMove != null) {
				Bit2D initialStateOfBitToMove = selectedPattern.getBit(irBitKeyToMove);
				Vector2 newPos = this.pushBit(selectedPattern, irBitKeyToMove, localDirectionToMove);
				double lengthToReduce;
				if (localDirectionToMove.x == 0){
					lengthToReduce = bitsLengthSpace;
				}
				else{
					lengthToReduce = bitsWidthSpace;
				}
				reduceBit(newPos,selectedPattern,localDirectionToMove, lengthToReduce);
				Logger.updateStatus("Moved bit at " + irBitKeyToMove + " in direction "
						+ localDirectionToMove.getTransformed(selectedPattern.getAffineTransform()) + " to " + newPos);
				// Re-validate
				selectedPattern.computeBits(selectedBoundary);
				// Try to recover the gap left behind
				this.cover(selectedPattern, initialStateOfBitToMove, localDirectionToMove);
				// Re-validate
				selectedPattern.computeBits(selectedBoundary);
				// Apply the changes on whole layer
				actualState.setReferentialPattern(selectedPattern);
				actualState.rebuild();
			} else {
				// Else if we don't have anyway to solve
				// We stop the process of resolution
				allFail = true;
			}
		}
		int irregularitiesRest = DetectorTool.detectIrregularBits(actualState.getSelectedPattern()).size();
		Logger.updateStatus("Layer " + actualState.getLayerNumber() + " optimized. " + irregularitiesRest
				+ " irregularities not solved yet.");
		return irregularitiesRest;
	}

	/**
	 * Attempt to resolve by moving the bit in 4 directions.
	 * 
	 * Prioritizing the height's sides. If the obtained state has less irregular
	 * bits, we will follow that way. Note: we also try to cover what we left behind
	 * by a full bit.
	 * 
	 * @param pattern
	 *            the selected pattern in the layer. This method will work on its
	 *            clone
	 * @param boundary
	 *            used to re-validate the attempt
	 * @param irBitKey
	 *            the key of the bit to try
	 * @return the first direction which reduce the total number of irregular bits
	 *         in the pattern. Null if no way to get better state. Calculated in
	 *         local coordinate system of input b.it
	 */
	private Vector2 attemptToSolve(Pattern pattern, Slice boundary, Vector2 irBitKey) {
		// Initial number of irregularities
		int initialIrregularities = DetectorTool.detectIrregularBits(pattern).size();
		Vector2[] localDirectionsForTrying = { new Vector2(1, 0), // right
				new Vector2(-1, 0), // left
				new Vector2(0, 1), // up
				new Vector2(0, -1) // down
		};
		for (int i = 0; i < localDirectionsForTrying.length; i++) {
			Vector2 localDirectionToTry = localDirectionsForTrying[i];
			// We need to conserve pattern
			// So we work on a clone
			Pattern clonedPattern = pattern.clone();
			Bit2D initialBit = clonedPattern.getBit(irBitKey);
			Vector2 newOrigin = this.pushBit(clonedPattern, irBitKey, localDirectionToTry);
			// Rebuild the boundary
			clonedPattern.computeBits(boundary);
			// Check that we did not push the bit into the air
			if (clonedPattern.getBit(newOrigin) == null) {
				continue;
			}
			// We cover what we left behind
			this.cover(clonedPattern, initialBit, localDirectionToTry);
			// Rebuild the boundary
			clonedPattern.computeBits(boundary);
			// Re-validate
			if (initialIrregularities > DetectorTool.detectIrregularBits(clonedPattern).size()) {
				return localDirectionToTry;
			}
		}
		return null;
	}

	/**
	 * 
	 * Cover the gap left behind after moving a bit forward.
	 * 
	 * @param actualState
	 * @param movedBit
	 *            the moved bit (in the initial location)
	 * @param localDirectionOfMove
	 *            in which we move the bit. Calculated in the local coordinate
	 *            system of bit.
	 */
	private void cover(Pattern actualState, Bit2D movedBit, Vector2 localDirectionOfMove) {
		double paddle = 0;
		if (localDirectionOfMove.x == 0) {
			paddle = bitsLengthSpace / 2;
		} else {
			paddle = bitsWidthSpace / 2;
		}
		Vector2 coveringBitKey = actualState.addBit(movedBit);
		coveringBitKey = this.pushBit(actualState, coveringBitKey, localDirectionOfMove.getOpposite());
		// Maintaining a gap between covering bit and the newly moved bit
		//this.moveBit(actualState, coveringBitKey, localDirectionOfMove.getOpposite(), paddle);
	}

	/**
	 * To push forward a bit into one direction.
	 * 
	 * Automatically reduce back every bit in front of it. The step of push is
	 * determined by direction, either a half of {@link CraftConfig#bitLength} or
	 * {@link CraftConfig#bitWidth}
	 * 
	 * @param actualState
	 * @param keyOfBitToMove
	 *            key of the bitToMove
	 * @param localDirectionToPush
	 *            in the coordinate system of the bitToMove. Either (1, 0), (-1, 0),
	 *            (0, 1), (0, -1).
	 * @return new origin of bit after being pushed into the given direction
	 */
	private Vector2 pushBit(Pattern actualState, Vector2 keyOfBitToMove, Vector2 localDirectionToPush) {
		// Detect the side of bit staying in that direction
		Bit2D bitToPush = actualState.getBit(keyOfBitToMove);

		// Find all the bits in front of bitToMove.
		// And classify them into 2 groups:
		// One consisting of bits whose centers are in front of bitToMove,
		// Other consisting of bits whose centers are not.
		// The way we treat these 2 groups are different
		Vector<Vector2> bitEntirelyInFrontOfBitToMove = new Vector<Vector2>();
		Vector<Vector2> bitPartiallyInFrontOfBitToMove = new Vector<Vector2>();
		for (Vector2 bitKey : actualState.getBitsKeys()) {
			Bit2D bitToCompare = actualState.getBit(bitKey);
			if (checkAdjacence(bitToPush, bitToCompare)) {
				if (checkInFront(bitToPush, bitToCompare, localDirectionToPush)) {
					if (checkEntirelyInFront(bitToPush, bitToCompare, localDirectionToPush)) {
						bitEntirelyInFrontOfBitToMove.add(bitKey);
					} else {
						bitPartiallyInFrontOfBitToMove.add(bitKey);
					}
				}
			}
		}
		// Calculating the distance to push
		double verticalDisplacement = 0,
				// these 2 are for calculating origins of covering bits
				horizontalDisplacement = 0,
				// to move the bits in front a little more
				// in order to have space between bits
				additionalVerticalDisplacement = 0;
		if (localDirectionToPush.x == 0) {
			// If we push up and down
			verticalDisplacement = CraftConfig.bitWidth / 2;
			horizontalDisplacement = CraftConfig.bitLength / 2;
			additionalVerticalDisplacement = bitsLengthSpace / 2;

		} else {
			// If we push right or left
			verticalDisplacement = CraftConfig.bitLength / 2;
			horizontalDisplacement = CraftConfig.bitWidth / 2;
			additionalVerticalDisplacement = bitsWidthSpace / 2;
		}

		// Treating the group bitEntirelyInFrontOfBitToMove.
		// We just reducing them back.
		// We also add a little space between
		// the bit we are going to move
		// and the ones in front of its
		for (Vector2 bitKey : bitEntirelyInFrontOfBitToMove) {
			this.reduceBit(bitKey, actualState, localDirectionToPush, verticalDisplacement);
		}

		// Treating the group bitPartiallyInFrontOfBitToMove
		// We need to recover the space left behind
		// after reducing them back.
		// We try the simplest way of covering.
		for (Vector2 bitKey : bitPartiallyInFrontOfBitToMove) {
			// Save the initial state of the actual bit
			Bit2D bit = actualState.getBit(bitKey).clone();
			// Reduce the actual bit
			this.reduceBit(bitKey, actualState, localDirectionToPush, verticalDisplacement);
			// The covering bit will always be a quart of a full one.
			// We have to define its origin
			Vector2 coveringBitOrigin = null;
			Vector2 centrifugalVector = Vector2.Tools.getCentrifugalVector(bitToPush.getOrigin(), localDirectionToPush,
					bit.getOrigin());
			if (bit.getLength() == CraftConfig.bitLength && bit.getWidth() == CraftConfig.bitWidth) {
				// If the actually considered bit is full
				coveringBitOrigin = bit.getOrigin()
						// horizontally move
						.add(centrifugalVector.mul(horizontalDisplacement))
						// vertically move backward
						.add(centrifugalVector.mul(additionalVerticalDisplacement*2));
				// Add the "petit" covering bit
				// First, we add it as a full bit
				Vector2 coveringBitKey = actualState.addBit(new Bit2D(coveringBitOrigin, bit.getOrientation()));
				// Then reform
				coveringBitKey = this.reduceBit(coveringBitKey, actualState, localDirectionToPush.getOpposite(),
						verticalDisplacement + additionalVerticalDisplacement*2);
				coveringBitKey = this.reduceBit(coveringBitKey, actualState, centrifugalVector.getOpposite(),
						horizontalDisplacement + additionalVerticalDisplacement*2);
			} else {
				// The actually considered bit has been modified
				// (not in full form)
				// Add the "petit" covering bit
				// First, we add the clone of the actual bit,
				// which has just been reduced to none
				Vector2 coveringBitKey = actualState.addBit(bit);
				// Then reform
				coveringBitKey = this.reduceBit(coveringBitKey, actualState, centrifugalVector, horizontalDisplacement);
			}
		}

		// Finally, push the given bit forward
		// Note that, we move by a distance
		// equal to what we reduce the bits in front of us
		return actualState.moveBit(keyOfBitToMove, localDirectionToPush, verticalDisplacement);
	}

	/**
	 * Cut a bit and push it into the given local direction.
	 * 
	 * @param bitKey
	 *            origin of the bit in coordinate system of this layer
	 * @param actualState
	 *            the selected pattern
	 * @param localDirectionToReduce
	 *            in the coordinate system of bit. Should be either (0, 1), (0, -1),
	 *            (1, 0), (-1, 0).
	 * @param lengthToReduce
	 *            in millimeter. If greater than sides, the bit will be removed.
	 * @return origin of reduced bit. Null if bit is removed.
	 */
	private Vector2 reduceBit(Vector2 bitKey, Pattern actualState, Vector2 localDirectionToReduce,
			double lengthToReduce) {
		Bit2D initialBit = actualState.getBit(bitKey);
		double actualLength = 0, newPercentageWidth = 100, newPercentageLength = 100;
		if (localDirectionToReduce.x == 0) {
			actualLength = initialBit.getWidth();
			newPercentageWidth = (1 - lengthToReduce / actualLength) * 100;
		} else {
			actualLength = initialBit.getLength();
			newPercentageLength = (1 - lengthToReduce / actualLength) * 100;
		}
		if (lengthToReduce >= actualLength) {
			actualState.removeBit(bitKey);
			return null;
		} else {
			initialBit.resize(newPercentageLength, newPercentageWidth);
			// Check that if we should move the bit
			if (localDirectionToReduce.x == 1 || localDirectionToReduce.y == -1) {
				// If we push up or to the right
				// we do not need to move further
				// because the resize already did
				return this.moveBit(actualState, bitKey, localDirectionToReduce, 0);
			} else {
				return this.moveBit(actualState, bitKey, localDirectionToReduce, lengthToReduce);
			}
		}
	}

	/**
	 * Check if these 2 bits are adjacent in the reality
	 * 
	 * @param bit1
	 * @param bit2
	 *            the actual situation (to get the matrix of transformation)
	 * @return
	 */
	private boolean checkAdjacence(Bit2D bit1, Bit2D bit2) {
		// The orientation of 2 bits is always (1, 0).
		Vector2 x = bit1.getOrientation().normal(), y = x.getCWAngularRotated(),
				dist = bit2.getCenter().sub(bit1.getCenter());
		double length1 = bit1.getLength(), width1 = bit1.getWidth(), length2 = bit2.getLength(),
				width2 = bit2.getWidth();

		// Firstly, we check if they do not overlap.
		// Even if they have only one common point,
		// (not on the border)
		// we will consider it as overlapped.
		// Horizontally && vertically
		if (Math.abs(dist.dot(x)) < (length1 + length2) / 2 && Math.abs(dist.dot(y)) < (width1 + width2) / 2) {
			return false;
		}

		// Secondly, we check if they are not too far
		if (Math.abs(dist.dot(x)) < (length1 + length2) / 2 + CraftConfig.bitLength / 2
				&& Math.abs(dist.dot(y)) < (width1 + width2) / 2) {
			return true;
		}
		if (Math.abs(dist.dot(y)) < (width1 + width2) / 2 + CraftConfig.bitWidth / 2
				&& Math.abs(dist.dot(x)) < (length1 + length2) / 2) {
			return true;
		}
		return false;
	}

	/**
	 * To check if the second input bit is directly in front of the first one.
	 * 
	 * Ensure to use {@link #checkAdjacence(Bit2D, Bit2D) checkAdjacence}
	 * before this.
	 * 
	 * @param bit1
	 *            the first input bit (reference bit)
	 * @param bit2
	 *            the second input bit (bit to check)
	 * @param localDirection
	 *            in the local coordinate system of bit1. Either (0, 1), (1, 0), (0,
	 *            -1), (-1, 0)
	 * @return true if 2 bits' sides have common length.
	 */
	private boolean checkInFront(Bit2D bit1, Bit2D bit2, Vector2 localDirection) {
		// By default, these 2 bits have the same orientation
		Vector2 orientation = bit1.getOrientation().normal();
		// Normalize the vector of direction
		localDirection = localDirection.normal();
		// If the direction is not perpendicular
		// or parallel to orientation of 2 bits
		if (localDirection.dot(orientation) != 0 && localDirection.dot(orientation.getCWAngularRotated()) != 0) {
			return false;
		}
		Vector2 dist = bit2.getCenter().sub(bit1.getCenter());
		double h1 = 0, h2 = 0;// Horizontal measures
		if (localDirection.dot(orientation) == 0) {
			// If we check with length sides
			h1 = bit1.getLength();
			h2 = bit2.getLength();
		} else {
			// If we check with width sides
			h1 = bit1.getWidth();
			h2 = bit2.getWidth();
		}
		return (Math.abs(dist.dot(localDirection.getCWAngularRotated())) < (h1 + h2) / 2
				&& dist.dot(localDirection) > 0);
	}

	/**
	 * Check if the second bit's center is in front of the first one's side, given
	 * the direction.
	 * 
	 * Ensure to use {@link #checkInFront(Bit2D, Bit2D, Vector2) checkInFront}
	 * before this.
	 * 
	 * @param bit1
	 *            the first input bit (reference bit)
	 * @param bit2
	 *            the second input bit (bit to check)
	 * @param localDirection
	 *            in the local coordinate system of bit1. Either (0, 1), (1, 0), (0,
	 *            -1), (-1, 0)
	 * @return <tt>true</tt> if the second bit's center is in front of the first
	 *         one's side
	 */
	private boolean checkEntirelyInFront(Bit2D bit1, Bit2D bit2, Vector2 localDirection) {
		// By default, the orientation is (1, 0)
		Vector2 orientation = bit1.getOrientation().normal();
		// Normalize the vector of direction
		localDirection = localDirection.normal();
		// Horizontal measures
		double h1 = 0;
		Vector2 dist = bit2.getCenter().sub(bit1.getCenter());
		// Direction must be orthogonal or parallel to orientation of bits.
		if (localDirection.dot(orientation) == 0) {
			// If we check with length sides
			h1 = bit1.getLength();
		} else {
			// If we check with width sides
			h1 = bit1.getWidth();
		}
		return (Math.abs(dist.dot(localDirection.getCWAngularRotated())) <= h1 / 2);
	}

	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection) {
		// audit to control the movement
		// We should have here an audit to control the movement (not to overlap
		// other bits)
		// The vector will be either (0,1), (0,-1), (1,0), (-1,0)
		double distance = 0;
		if (localDirection.x == 0) {// up or down
			distance = CraftConfig.bitWidth / 2;
		} else if (localDirection.y == 0) {// left or right
			distance = CraftConfig.bitLength / 2;
		}
		return this.moveBit(actualState, bitKey, localDirection, distance);
	}

	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection, double distance) {
		return actualState.moveBit(bitKey, localDirection, distance);
	}

	@Override
	public String getCommonName() {
		return "Improved Brick Pattern";
	}

	@Override
	public String getIconName() {
		return "p3.png";
	}

	@Override
	public String getDescription() {
		return "A pattern improved from classic Brick Pattern with much more flexibility.";
	}

	@Override
	public String getHowToUse() {
		return "You can add incremental rotations or displacement for layers.";
	}

	@Override
	public void initiateConfig() {
		// bitsLengthSpace
		config.add(new DoubleParam("bitsLengthSpace", "Space between bits' lengths",
				"The gap between two consecutive bits' lengths (in mm)", 1.0, 100.0, 1.0, 1.0));
		// bitsWidthSpace
		config.add(new DoubleParam("bitsWidthSpace", "Space between bits' widths",
				"The gap between two consecutive bits' widths (in mm)", 1.0, 100.0, 5.0, 1.0));
		// diffRotation
		config.add(new DoubleParam("diffRotation", "Differential rotation",
				"Rotation of a layer in comparison to the previous one (in degrees °)", 0.0, 360.0, 90.0, 0.1));
		// diffxOffset
		config.add(new DoubleParam("diffxOffset", "Differential X offset",
				"Offset in the X-axe of a layer in comparison to the previous one (in mm)", -1000.0, 1000.0, 0.0, 1.0));
		// diffyOffset
		config.add(new DoubleParam("diffyOffset", "Differential Y offset",
				"Offset in the Y-axe of a layer in comparison to the previous one (in mm)", -1000.0, 1000.0, 0.0, 1.0));

	}
	@Override
	public boolean ready(Mesh mesh) {
		// Setting the skirtRadius and starting/ending points
		this.skirtRadius = mesh.getSkirtRadius();
		double maxiSide = Math.max(CraftConfig.bitLength, CraftConfig.bitWidth);
		this.patternStart = new Vector2(-skirtRadius - maxiSide, -skirtRadius - maxiSide);
		this.patternEnd = new Vector2(skirtRadius + maxiSide, skirtRadius + maxiSide);
		return true;
	}
}