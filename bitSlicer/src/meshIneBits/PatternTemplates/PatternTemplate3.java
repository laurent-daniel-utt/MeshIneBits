package meshIneBits.PatternTemplates;

import java.util.Iterator;
import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.Config.CraftConfig;
import meshIneBits.Slicer.Slice;
import meshIneBits.util.DetectorTool;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;

/**
 * Pattern improved from classic pattern
 * {@link meshIneBits.PatternTemplates.PatternTemplate1 PatternTemplate1}.
 * 
 * @author NHATHAN
 *
 */
public class PatternTemplate3 extends PatternTemplate {

	public PatternTemplate3(double skirtRadius) {
		super(skirtRadius);
	}

	public Pattern createPattern(int layerNumber) {
		Vector<Bit2D> bits = new Vector<Bit2D>();
		// space between 2 consecutive
		// bits' height's side
		double f = CraftConfig.bitsWidthSpace;
		// space between 2 consecutive
		// bits' length's side
		double e = CraftConfig.bitsLengthSpace;
		double L = CraftConfig.bitLength;
		double H = CraftConfig.bitWidth;
		// The first bit is displaced by diffxOffset and diffyOffset
		Vector2 _1stBit = new Vector2(CraftConfig.diffxOffset, CraftConfig.diffyOffset);
		// Fill out the square
		int lineNum = 0;// Initialize
		// Vertically downward
		while (_1stBit.y - H / 2 + lineNum * (H + e) <= patternEnd.y) {
			// Horizontally
			if (lineNum % 2 == 0) {
				fillHorizontally(new Vector2(_1stBit.x, _1stBit.y + lineNum * (H + e)), bits);
			} else {
				fillHorizontally(new Vector2(_1stBit.x + L / 2 + f / 2, _1stBit.y + lineNum * (H + e)), bits);
			}
			lineNum++;
		}
		// Vertically upward
		lineNum = 1; // Reinitialize
		while (_1stBit.y + H / 2 - lineNum * (H + e) >= patternStart.y) {
			// Horizontally
			if (lineNum % 2 == 0) {
				fillHorizontally(new Vector2(_1stBit.x, _1stBit.y - lineNum * (H + e)), bits);
			} else {
				fillHorizontally(new Vector2(_1stBit.x + L / 2 + f / 2, _1stBit.y - lineNum * (H + e)), bits);
			}
			lineNum++;
		}

		// Rotation for this layer
		double alpha = CraftConfig.diffRotation;
		Vector2 customizedRotation = Vector2.getEquivalentVector((alpha * layerNumber) % 360);
		return new Pattern(bits, customizedRotation, skirtRadius);
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
		double f = CraftConfig.bitsWidthSpace;
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
	public void optimize(Layer actualState) {
		// this boolean to check that if we'd tried all possibilities
		boolean allFail = false;
		while (!allFail) {
			Pattern selectedPattern = actualState.getSelectedPattern();
			Slice selectedBoundary = actualState.getSelectedSlice();
			Vector2 directionToMove = null, irBitKeyToMove = null;
			Bit2D initialStateOfBitToMove = null;
			Vector<Vector2> irregularBitKeys = DetectorTool.detectIrregularBits(selectedPattern);
			// We will find the first irregular bit that we can resolve
			for (Iterator<Vector2> irBitKeyIterator = irregularBitKeys.iterator(); irBitKeyIterator.hasNext();) {
				Vector2 irBitKey = (Vector2) irBitKeyIterator.next();
				// We try to move this irregular bit in 4 directions, starting
				// with height's sides.
				// If there is at least one way to reduce the number of
				// irregular bits in the pattern,
				// we choose that direction and apply on the pattern
				directionToMove = attemptToSolve(selectedPattern, selectedBoundary, irBitKey);
				if (directionToMove != null) {
					irBitKeyToMove = irBitKey;
					initialStateOfBitToMove = selectedPattern.getBit(irBitKey);
					break;
				}
			}
			// If we have at least one chance to move
			if (directionToMove != null & irBitKeyToMove != null) {
				actualState.moveBit(irBitKeyToMove, directionToMove);
//				this.pushBit(selectedPattern, irBitKeyToMove, directionToMove);
				Logger.message("Moved bit of origin " + irBitKeyToMove + " in (local) direction " + directionToMove);
				// Try to recover the gap left behind
				if (shouldCover(selectedPattern, selectedBoundary, initialStateOfBitToMove, directionToMove)) {
					cover(selectedPattern, initialStateOfBitToMove, directionToMove);
					Logger.message("Covered");
				}
				// Apply the changes on whole layer
//				actualState.rebuild();
			} else {
				// Else if we don't have anyway to solve
				// We stop the process of resolution
				allFail = true;
			}
		}
		Logger.message("Layer " + actualState.getLayerNumber() + " optimized.");
	}

	/**
	 * Attempt to resolve by moving the bit in 4 directions.
	 * 
	 * Prioritizing the height's sides. If the obtained state has less irregular
	 * bits, we will follow that way.
	 * 
	 * Note: Once we move the bit, we will leave behind a space that we'll try
	 * to cover by an other bit (to eliminate the chance that we have a spacing
	 * hole in the pattern).
	 * 
	 * @param pattern
	 * @param boundary
	 *            used to re-validate the attempt
	 * @param irBitKey
	 *            the key of the bit to try
	 * @return the first direction which reduce the total number of irregular
	 *         bits in the pattern. Null if no way to get better state.
	 *         Calculated in local coordinate system of input b.it
	 */
	private Vector2 attemptToSolve(Pattern pattern, Slice boundary, Vector2 irBitKey) {
		// Initial number of irregularities
		int initialIrregularities = DetectorTool.detectIrregularBits(pattern).size();
		Vector2[] directionsForTrying = { new Vector2(1, 0), // right
				new Vector2(-1, 0), // left
				new Vector2(0, 1), // up
				new Vector2(0, -1) // down
		};
		for (int i = 0; i < directionsForTrying.length; i++) {
			Vector2 directionToTry = directionsForTrying[i];
			// We need to conserve pattern
			// So we work on a clone
			Pattern clonedPattern = pattern.clone();
			Vector2 newOrigin = this.pushBit(clonedPattern, irBitKey, directionToTry);
			clonedPattern.computeBits(boundary);
			// Check that we did not push the bit into the air
			if (clonedPattern.getBit(newOrigin) == null) {
				continue;
			}
			if (initialIrregularities > DetectorTool.detectIrregularBits(clonedPattern).size()) {
				return directionToTry;
			}
		}
		return null;
	}

	/**
	 * Attempt to cover the gap left behind after moving a bit forward.
	 * 
	 * @param actualState
	 * @param boundary
	 *            to validate the cover
	 * @param movedBit
	 *            the moved bit (in the initial location)
	 * @param localDirectionOfMove
	 *            in which we move the bit. Calculated in the local coordinate
	 *            system of bit.
	 * @return false if the move will increase the number or irregularities
	 */
	private boolean shouldCover(Pattern actualState, Slice boundary, Bit2D movedBit, Vector2 localDirectionOfMove) {
		int actualIrregularities = DetectorTool.detectIrregularBits(actualState).size();
		Pattern clonedPattern = actualState.clone();
		this.cover(clonedPattern, movedBit, localDirectionOfMove);
		// Validate
		clonedPattern.computeBits(boundary);
		// Check
		if (DetectorTool.detectIrregularBits(clonedPattern).size() <= actualIrregularities) {
			return true;
		}
		return false;
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
			paddle = CraftConfig.bitsLengthSpace / 2;
		} else {
			paddle = CraftConfig.bitsWidthSpace / 2;
		}
		Vector2 coveringBitKey = actualState.addBit(movedBit);
		coveringBitKey = this.pushBit(actualState, coveringBitKey, localDirectionOfMove.getOpposite());
		// Maintaining a gap between covering bit and the newly moved bit
		this.moveBit(actualState, coveringBitKey, localDirectionOfMove, paddle);
	}

	/**
	 * To push forward a bit into one direction.
	 * 
	 * Automatically reduce back every bit in front of it. The step of push is
	 * determined by direction, either a half of {@link CraftConfig#bitLength}
	 * or {@link CraftConfig#bitWidth}
	 * 
	 * @param actualState
	 * @param keyOfBitToMove
	 *            key of the bitToMove
	 * @param localDirection
	 *            in the coordinate system of the bitToMove
	 * @return new origin of bit after being pushed into the given direction
	 */
	public Vector2 pushBit(Pattern actualState, Vector2 keyOfBitToMove, Vector2 localDirection) {
		// Detect the side of bit staying in that direction
		Bit2D bitToMove = actualState.getBit(keyOfBitToMove);

		// Recalculate the direction in according to the coordinate system of
		// actualState
		Vector2 direction = localDirection.getTransformed(actualState.getAffineTransform());

		// Find all the bits in front of bitToMove.
		// And classify them into 2 groups:
		// One consisting of bits whose centers are in front of bitToMove,
		// Other consisting of bits whose centers are not.
		// The way we treat these 2 groups are different
		Vector<Vector2> bitEntirelyInFrontOfBitToMove = new Vector<>();
		Vector<Vector2> bitPartiallyInFrontOfBitToMove = new Vector<>();
		for (Vector2 bitKey : actualState.getBitsKeys()) {
			Bit2D bitToCompare = actualState.getBit(bitKey);
			if (checkAdjacence(bitToMove, bitToCompare)) {
				if (checkInFront(bitToMove, bitToCompare, direction)) {
					if (checkPartiallyInFront(bitToMove, bitToCompare, direction)) {
						bitPartiallyInFrontOfBitToMove.add(bitKey);
					} else {
						bitEntirelyInFrontOfBitToMove.add(bitKey);
					}
				}
			}
		}
		// Calculating the distance to push
		double lengthToReduce = 0,
				// these 2 are for calculating origins of covering bits
				horizontalPaddle = 0, verticalPaddle = 0;
		if (localDirection.x == 0) {
			lengthToReduce = CraftConfig.bitWidth / 2;
			horizontalPaddle = CraftConfig.bitLength / 4;
			verticalPaddle = CraftConfig.bitWidth / 4 + CraftConfig.bitsLengthSpace / 2;
		} else {
			lengthToReduce = CraftConfig.bitLength / 2;
			horizontalPaddle = CraftConfig.bitWidth / 4;
			verticalPaddle = CraftConfig.bitLength / 4 + CraftConfig.bitsWidthSpace / 2;
		}

		// Treating the group bitEntirelyInFrontOfBitToMove.
		// We just reducing them back.
		for (Vector2 bitKey : bitEntirelyInFrontOfBitToMove) {
			this.reduceBit(bitKey, actualState, localDirection, lengthToReduce);
		}

		// Treating the group bitPartiallyInFrontOfBitToMove
		// We need to recover the space left behind
		// after reducing them back.
		// We try the simplest way of covering.
		for (Vector2 bitKey : bitPartiallyInFrontOfBitToMove) {
			Bit2D actualBit = actualState.getBit(bitKey);
			this.reduceBit(bitKey, actualState, localDirection, lengthToReduce);
			// The covering bit will always be a quart of a full one.
			// We have to define its origin
			Vector2 coveringBitOrigin = null, coveringBitOrientation = actualBit.getOrientation();
			double coveringBitWidth = CraftConfig.bitWidth / 2, coveringBitLength = CraftConfig.bitLength / 2;
			Vector2 centrifugalVector = Vector2.Tools.getCentrifugalVector(keyOfBitToMove, direction, bitKey);
			if (actualBit.getLength() == CraftConfig.bitLength && actualBit.getWidth() == CraftConfig.bitWidth) {
				// If the actually considered bit is full
				coveringBitOrigin = bitKey
						// horizontally move
						.add(centrifugalVector.mul(horizontalPaddle))
						// vertically move backward
						.sub(direction.mul(verticalPaddle));
			} else {
				// The actually considered bit has been modified
				// (not in full form)
				coveringBitOrigin = bitKey.add(centrifugalVector.mul(horizontalPaddle));
			}
			// Add the "petit" covering bit
			actualState
					.addBit(new Bit2D(coveringBitOrigin, coveringBitOrientation, coveringBitLength, coveringBitWidth));
		}

		// Finally, push the given bit forward
		// Note that, we move by a distance
		// equal to what we reduce the bits in front of us
		return actualState.moveBit(keyOfBitToMove, localDirection, lengthToReduce);
	}

	/**
	 * Cut a bit and push it back.
	 * 
	 * @param bitKey
	 * @param actualState
	 * @param localDirection
	 *            in the coordinate system of bit
	 * @param lengthToReduce
	 *            in millimeter. If greater than sides, the bit will be removed.
	 * @return new origin of reduced bit. Null if bit is removed.
	 */
	private Vector2 reduceBit(Vector2 bitKey, Pattern actualState, Vector2 localDirection, double lengthToReduce) {
		Bit2D bit = actualState.getBit(bitKey);
		double actualLength = 0, percentageWidth = 100, percentageLength = 100;
		if (localDirection.x == 0) {
			actualLength = bit.getWidth();
			percentageWidth = (1 - lengthToReduce / actualLength) * 100;
		} else {
			actualLength = bit.getLength();
			percentageLength = (1 - lengthToReduce / actualLength) * 100;
		}
		if (lengthToReduce >= actualLength) {
			actualState.removeBit(bitKey);
			return null;
		} else {
			bit.resize(percentageLength, percentageWidth);
			return this.moveBit(actualState, bitKey, localDirection, lengthToReduce / 2);
		}
	}

	/**
	 * To check if the second input bit is directly in front of the first one.
	 * 
	 * Ensure to use {@link #checkAdjacence(Bit2D, Bit2D, Pattern)
	 * checkAdjacence} before this.
	 * 
	 * @param bit1
	 *            the first input bit (reference bit)
	 * @param bit2
	 *            the second input bit (bit to check)
	 * @param direction
	 *            in the coordinate system of layer
	 * @return
	 */
	private boolean checkInFront(Bit2D bit1, Bit2D bit2, Vector2 direction) {
		Vector2 orientation = bit1.getOrientation().normal();
		direction = direction.normal(); // Normalize the vector of direction
		// If the direction is not perpendicular
		// or parallel to orientation of 2 bits
		if (direction.dot(orientation) != 0 && direction.getCWAngularRotated().dot(orientation) != 0) {
			return false;
		}
		Vector2 dist = bit2.getOrigin().sub(bit1.getOrigin());
		double v1 = 0, v2 = 0;// Vertical measures
		if (direction.dot(orientation) == 0) {
			// If we check with length sides
			v1 = bit1.getWidth();
			v2 = bit2.getWidth();
		} else {
			// If we check with width sides
			v1 = bit1.getLength();
			v2 = bit2.getLength();
		}
		if (dist.dot(direction) > (v1 + v2) / 2) {
			return true;
		}
		return false;
	}

	/**
	 * Check if the second bit's center is in front of the first one's side,
	 * given the direction.
	 * 
	 * Ensure to use {@link #checkInFront(Bit2D, Bit2D, Vector2) checkInFront}
	 * before this.
	 * 
	 * @param bit1
	 * @param bit2
	 * @param direction
	 * @return
	 */
	private boolean checkPartiallyInFront(Bit2D bit1, Bit2D bit2, Vector2 direction) {
		Vector2 orientation = bit1.getOrientation();
		direction = direction.normal(); // Normalize the vector of direction
		double h1 = 0;// Horizontal measures
		Vector2 y = direction.getCWAngularRotated(), dist = bit2.getOrigin().sub(bit1.getOrigin());
		// Direction must be orthogonal or parallel to orientation of bits.
		if (direction.dot(orientation) == 0) {
			// If we check with length sides
			h1 = bit1.getLength();
		} else {
			// If we check with width sides
			h1 = bit1.getWidth();
		}
		return (h1 / 2 < Math.abs(dist.dot(y)));
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
		Vector2 x = bit1.getOrientation().normal(), y = x.getCWAngularRotated(),
				dist = bit2.getOrigin().sub(bit1.getOrigin());

		double length1 = bit1.getLength(), length2 = bit1.getLength(), width1 = bit1.getWidth(),
				width2 = bit2.getWidth();

		// Firstly, we check if they do not overlap.
		// Even if they have only one common point,
		// we will consider it as overlapped.
		// Horizontally && vertically
		if (Math.abs(dist.dot(x)) <= (length1 + length2) / 2 && Math.abs(dist.dot(y)) <= (width1 + width2) / 2) {
			return false;
		}

		// Secondly, we check if they are not too far
		if (Math.abs(dist.dot(x)) > (length1 + length2) / 2 + CraftConfig.bitsWidthSpace
				|| Math.abs(dist.dot(y)) > (width1 + width2) / 2 + CraftConfig.bitsLengthSpace) {
			return false;
		}
		return true;
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

}