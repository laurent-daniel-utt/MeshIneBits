package meshIneBits.PatternTemplates;

import java.awt.geom.NoninvertibleTransformException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.Config.CraftConfig;
import meshIneBits.Slicer.Slice;
import meshIneBits.util.DetectorTool;
import meshIneBits.util.Direction;
import meshIneBits.util.Segment2D;
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

		double f = CraftConfig.bitsWidthSpace; // space between 2 consecutive
												// bits' height's side
		double e = CraftConfig.bitsLengthSpace; // space between 2 consecutive
												// bits' length's side
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
			Pattern pattern = actualState.getSelectedPattern();
			Slice boundary = actualState.getSelectedSlice();
			Direction dirToMove = null;
			Vector2 irBitKeyToMove = null;
			Vector<Vector2> irregularBitKeys = DetectorTool.detectIrregularBits(pattern);
			// We will find the first irregular bit that we can resolve
			for (Iterator<Vector2> irBitKeyIterator = irregularBitKeys.iterator(); irBitKeyIterator.hasNext();) {
				Vector2 irBitKey = (Vector2) irBitKeyIterator.next();
				// We try to move this irregular bit in 4 directions, starting
				// with height's sides.
				// If there is at least one way to reduce the number of
				// irregular bits in the pattern,
				// we choose that direction and apply on the pattern
				dirToMove = attemptToSolve(pattern, boundary, irBitKey);
				if (dirToMove != null) {
					irBitKeyToMove = irBitKey;
					break;
				}
			}
			// If we have at least one chance to move
			if (dirToMove != null & irBitKeyToMove != null) {
				actualState.moveBit(irBitKeyToMove, dirToMove.toVector2());
			} else {
				// Else if we don't have anyway to solve
				// We stop the process of resolution
				allFail = true;
			}
		}
		// for (Vector2 irBitKey : irregularBitKeys) {
		// Bit2D irBit = pattern.getBit(irBitKey);
		// if (irBit == null){ // just in case
		// irregularBitKeys.remove(irBitKey);
		// } else {
		// Area irBitArea = irBit.getArea();
		// if (irBitArea.isSingular()){// for a moment, we try to solve the
		// easiest case
		// Vector<Segment2D> segments = AreaTool.getLargestPolygon(irBitArea);
		// HashMap<String, Vector2> corners = irBit.getTransfoNormalCorners();
		// // Build 4 sides of the bit
		// Segment2D rightSide = new Segment2D(corners.get("UpRight"),
		// corners.get("DownRight"));
		// Segment2D downSide = new Segment2D(corners.get("DownRight"),
		// corners.get("DownLeft"));
		// Segment2D leftSide = new Segment2D(corners.get("DownLeft"),
		// corners.get("UpLeft"));
		// Segment2D upSide = new Segment2D(corners.get("UpLeft"),
		// corners.get("UpRight"));
		// // A store of every segment lying on the bit's side
		// HashMap<String, Vector<Segment2D>> sides = new HashMap<String,
		// Vector<Segment2D>>();
		// sides.put("Right", new Vector<>());
		// sides.put("Down", new Vector<>());
		// sides.put("Left", new Vector<>());
		// sides.put("Up", new Vector<>());
		// // Determine all the segments lying on the normal sides of the bit
		// for (Segment2D segment : segments) {
		// Vector2 pStart = segment.start;
		// Vector2 pEnd = segment.end;
		// if (rightSide.contains(pStart) && rightSide.contains(pEnd)){
		// sides.get("Right").add(segment);
		// } else if (downSide.contains(pStart) && downSide.contains(pEnd)){
		// sides.get("Down").add(segment);
		// } else if (leftSide.contains(pStart) && leftSide.contains(pEnd)){
		// sides.get("Left").add(segment);
		// } else if (upSide.contains(pStart) && upSide.contains(pEnd)){
		// sides.get("Up").add(segment);
		// }
		// }
		// // Count the sides which have at least one segment
		// int count = 0;
		// for (String key: sides.keySet()){
		// if (!sides.get(key).isEmpty()){
		// count++;
		// }
		// }
		// // Depending on count, we try to resolve
		// switch (count) {
		// case 0:
		//
		// break;
		// case 1:
		// solve1(irBitKey,pattern,irregularBitKeys);
		// break;
		// case 2:
		// break;
		// case 3:
		// break;
		// case 4:
		// break;
		// default:
		// break;
		// }
		// }
		// }
		// }
	}

	/**
	 * Attempt to resolve by moving the bit in 4 directions.
	 * 
	 * Prioritizing the height's sides. If the obtained state has less irregular
	 * bits, we will follow that way.
	 * 
	 * Note: Once we move the bit, we will leave behind a space that'll be
	 * covered by another half-bit (to eliminate the chance that we have a
	 * spacing hole in the pattern).
	 * 
	 * @param pattern
	 * @param boundary
	 *            used to re-validate the attempt
	 * @param irBitKey
	 *            the key of the bit to try
	 * @return the first direction which reduce the total number of irregular
	 *         bits in the pattern. Null if no way to get better state.
	 */
	private Direction attemptToSolve(Pattern pattern, Slice boundary, Vector2 irBitKey) {
		int initialSize = pattern.getBitsKeys().size();
		if (initialSize > attemptToMoveHorizontally(pattern, boundary, irBitKey, Direction.RIGHT).getBitsKeys()
				.size()) {
			return Direction.RIGHT;
		}
		if (initialSize > attemptToMoveHorizontally(pattern, boundary, irBitKey, Direction.LEFT).getBitsKeys().size()) {
			return Direction.LEFT;
		}
		if (initialSize > attemptToMoveVertically(pattern, boundary, irBitKey, Direction.UP).getBitsKeys().size()) {
			return Direction.UP;
		}
		if (initialSize > attemptToMoveVertically(pattern, boundary, irBitKey, Direction.DOWN).getBitsKeys().size()) {
			return Direction.DOWN;
		}
		return null;
	}

	/**
	 * Attempting to move a bit upward or downward.
	 * 
	 * If it detects others bits in that direction, it will reduce theirs
	 * lengths by half.
	 * 
	 * It will always work on a clone of the input pattern.
	 * 
	 * @param pattern
	 * @param boundary
	 *            to re-validate the choice
	 * @param bitKeyToMove
	 * @param upOrDown
	 *            considered in the coordinate system of the chosen bit
	 * @return the preview of pattern after the move
	 */
	private Pattern attemptToMoveVertically(Pattern pattern, Slice boundary, Vector2 bitKeyToMove, Direction upOrDown) {
		// TODO Auto-generated method stub
		Pattern clonedPattern = pattern.clone();
		Bit2D bitToMove = clonedPattern.getBit(bitKeyToMove);
		HashMap<Direction, Vector2> corners = bitToMove.getTransfoNormalCorners();
		// Constructing the side in the direction we will move
		Segment2D front = null;
		// The rear
		Segment2D rear = null;
		Vector2 directionInVector = upOrDown.toVector2();
		switch (upOrDown) {
		case UP:
			front = new Segment2D(corners.get(Direction.UPLEFT), corners.get(Direction.UPRIGHT));
			rear = new Segment2D(corners.get(Direction.DOWNRIGHT), corners.get(Direction.DOWNLEFT));
			break;
		case DOWN:
			front = new Segment2D(corners.get(Direction.DOWNRIGHT), corners.get(Direction.DOWNLEFT));
			rear = new Segment2D(corners.get(Direction.UPLEFT), corners.get(Direction.UPRIGHT));
			break;
		default:
			break;
		}
		// Store all bit keys of bits on that direction
		// To accelerate calculations
		Vector<Vector2> adjBitKeysInFront = new Vector<>();
		Vector<Vector2> adjBitKeysInRear = new Vector<>();
		for (Vector2 bitKey : clonedPattern.getBitsKeys()) {
			if (checkAdjacence(clonedPattern.getBit(bitKey), clonedPattern.getBit(bitKeyToMove), pattern)) {
				if (checkIfOnDifferentSides(bitKeyToMove, bitKey, front)) {
					adjBitKeysInFront.add(bitKey);
				} else if (checkIfOnDifferentSides(bitKeyToMove, bitKey, rear)) {
					adjBitKeysInRear.add(bitKey);
				}
			}
		}
		if (adjBitKeysInFront.isEmpty()) {
			// No adjacent bit detected in that direction
			// That means no use in moving that way
		} else {
			// First of all, we reduce the width of all the adjacent bits in
			// front by half.
			// We detect if there is some width-modified bits
			boolean hasWidthModifiedBit = false;
			for (Iterator<Vector2> iterator = adjBitKeysInFront.iterator(); iterator.hasNext();) {
				Vector2 bitKey = (Vector2) iterator.next();
				if (this.hasModifiedWidth(clonedPattern.getBit(bitKey))) {
					hasWidthModifiedBit = true;
					break;
				}
			}
			if (!hasWidthModifiedBit){
				// All the front-bits' width remained unchanged
				
			}
			if (hasWidthModifiedBit) {
				Bit2D thisBit = clonedPattern.getBit(bitKeyToMove);
				Vector2 oThis = null;
				try {
					oThis = thisBit.getOrigin().getTransformed(clonedPattern.getAffineTransform().createInverse());
				} catch (NoninvertibleTransformException e1) {
					e1.printStackTrace();
				}
				Vector<Vector2> bitEntirelyInFrontOfThisBit = new Vector<>();
				Vector<Vector2> bitPartiallyInFrontOfThisBit = new Vector<>();
				for (Vector2 bitKey : adjBitKeysInFront) {
					Bit2D frontBit = clonedPattern.getBit(bitKey);
					// Firstly, we need to reform the input bits' origin
					// back to normal coordinate system
					// (no rotation, no translation)
					Vector2 oFront = null;
					try {
						oFront = frontBit.getOrigin()
								.getTransformed(clonedPattern.getAffineTransform().createInverse());
					} catch (NoninvertibleTransformException e) {
						e.printStackTrace();
					}
					// Next, we check the distance between two origins
					if (Math.abs(oFront.x - oThis.x) <= thisBit.getLength() / 4) {
						// frontBit lies entirely in front of thisBit
						bitEntirelyInFrontOfThisBit.add(bitKey);
						// frontBit.resize(100, 50);
						// this.moveBit(clonedPattern, bitKey,
						// directionInVector, frontBit.getWidth() / 4);
					} else {
						// frontBit lies partially in front of thisBit
						bitPartiallyInFrontOfThisBit.add(bitKey);
					}
				}
				// We resolve the bits that lie partially in front of thisBit.
				// We "split" them apart to create an open space in front of
				// this Bit
				// by decrease theirs lengths by bitLength / 2.
				for (Vector2 bitKey : bitPartiallyInFrontOfThisBit) {
					if (this.hasModifiedLength(clonedPattern.getBit(bitKey))) {
						clonedPattern.removeBit(bitKey);
					} else {
						// Determine the direction in which we will move this
						// frontBit out of the way
						// TODO
						Vector2 dirMovingAway = getVectorMovingAway(bitKeyToMove, directionInVector, bitKey);
					}
				}
			}
		}
		// There are many cases.
		// To be simple, we try to resolve the cases
		// where the "front" bits are complete
//		if (adjBitKeysInFront.size() == 1) {
//			// Check if this bit is entirely in front of the input bit
//			Bit2D frontBit = clonedPattern.getBit(adjBitKeysInFront.firstElement());
//			Bit2D thisBit = clonedPattern.getBit(bitKeyToMove);
//			// Firstly, we need to reform the input bits' origin back to
//			// normal
//			// coordinate system (no rotation, no translation)
//			Vector2 oFront = null;
//			Vector2 oThis = null;
//			try {
//				oFront = frontBit.getOrigin().getTransformed(clonedPattern.getAffineTransform().createInverse());
//				oThis = thisBit.getOrigin().getTransformed(clonedPattern.getAffineTransform().createInverse());
//			} catch (NoninvertibleTransformException e) {
//				e.printStackTrace();
//			}
//			// Next, we check the distance between two origins
//			if (Math.abs(oFront.x - oThis.x) <= thisBit.getLength() / 4) {
//
//			}
//		}

		//
		// // Reduce the width of that nearest line
		// // Firstly, check the line's width
		// if (clonedPattern.getBit(nearestLine.firstElement()).getWidth() !=
		// CraftConfig.bitWidth) {
		// // In other word, that line has been recently modified the width
		// // We just need to remove it
		// for (Iterator<Vector2> iterator = nearestLine.iterator();
		// iterator.hasNext();) {
		// Vector2 bitKey = (Vector2) iterator.next();
		// clonedPattern.removeBit(bitKey);
		// }
		// } else {
		// // This line is completely "normal"
		// for (Iterator<Vector2> iterator = nearestLine.iterator();
		// iterator.hasNext();) {
		// Vector2 bitKey = (Vector2) iterator.next();
		// Bit2D newBit = new Bit2D(clonedPattern.getBit(bitKey), 100, 50);
		// clonedPattern.removeBit(bitKey);
		// Vector2 newKey = clonedPattern.addBit(newBit);
		// moveVertically(clonedPattern, newKey, directionInVector);
		// }
		// // Move the input bit
		// moveVertically(clonedPattern, bitKeyToMove, directionInVector);
		// }
		return clonedPattern;

	}

	/**
	 * @param o
	 *            origin of departure of v
	 * @param v
	 *            vector of direction departing from origin
	 * @param p
	 *            point of depart
	 * @return the vector whose origin is point p, moving perpendicularly away
	 *         from (origin, v)
	 */
	private Vector2 getVectorMovingAway(Vector2 o, Vector2 v, Vector2 p) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Check if this pattern has reduced the length of the bit in the process of
	 * auto-optimization.
	 * 
	 * @param bit
	 * @return
	 */
	private boolean hasModifiedLength(Bit2D bit) {
		return (bit.getLength() != CraftConfig.bitLength);
	}

	/**
	 * Check if this pattern has reduced the width of the bit in the process of
	 * auto-optimization.
	 * 
	 * @param bit
	 * @return
	 */
	private boolean hasModifiedWidth(Bit2D bit) {
		return (bit.getWidth() != CraftConfig.bitWidth);
	}

	/**
	 * Attempting to move a bit to the left or to the right.
	 * 
	 * If it detects an other bit in that direction, it will reduce its width by
	 * half. Also, it will fill the space left behind.
	 * 
	 * This method will always work on a clone of the input pattern.
	 * 
	 * @param pattern
	 * @param boundary
	 *            to re-validate the choice
	 * @param bitKeyToMove
	 * @param rightOrLeft
	 *            considered in the coordinate system of the chosen bit
	 * @return the preview of pattern after the move
	 */
	private Pattern attemptToMoveHorizontally(Pattern pattern, Slice boundary, Vector2 bitKeyToMove,
			Direction rightOrLeft) {
		Pattern clonedPattern = pattern.clone();
		Bit2D bitToMove = clonedPattern.getBit(bitKeyToMove);
		HashMap<Direction, Vector2> corners = bitToMove.getTransfoNormalCorners();
		// The front (the direction we will move along)
		Segment2D front = null;
		// The rear
		Segment2D rear = null;

		Vector2 directionInVector = rightOrLeft.toVector2();
		switch (rightOrLeft) {
		case RIGHT:
			front = new Segment2D(corners.get(Direction.DOWNRIGHT), corners.get(Direction.UPRIGHT));
			rear = new Segment2D(corners.get(Direction.UPLEFT), corners.get(Direction.DOWNLEFT));
			break;
		case LEFT:
			front = new Segment2D(corners.get(Direction.UPLEFT), corners.get(Direction.DOWNLEFT));
			rear = new Segment2D(corners.get(Direction.DOWNRIGHT), corners.get(Direction.UPRIGHT));
			break;
		default:
			break;
		}
		// Find the adjacent bits in that direction
		// We find also the adjacent bits behind the bit we attempt to move
		Vector<Vector2> adjBitKeysInFront = new Vector<>();
		Vector<Vector2> adjBitKeysInRear = new Vector<>();
		for (Vector2 bitKey : clonedPattern.getBitsKeys()) {
			if (checkAdjacence(clonedPattern.getBit(bitKey), clonedPattern.getBit(bitKeyToMove), pattern)) {
				if (checkIfOnDifferentSides(bitKeyToMove, bitKey, front)) {
					adjBitKeysInFront.add(bitKey);
				} else if (checkIfOnDifferentSides(bitKeyToMove, bitKey, rear)) {
					adjBitKeysInRear.add(bitKey);
				}
			}
		}
		if (adjBitKeysInFront.isEmpty()) {
			// No adjacent bit detected in that direction
			// That means no use in moving that way
		} else {
			// We will try to reduce the adjacent bits in front by half of its
			// length in order to have space to move the irregular bit
			for (Iterator<Vector2> iterator = adjBitKeysInFront.iterator(); iterator.hasNext();) {
				Vector2 bitKey = (Vector2) iterator.next();
				if (this.hasModifiedLength(clonedPattern.getBit(bitKey))) {
					// This bit has already been reduced its length recently
					// We just need to remove it
					clonedPattern.removeBit(bitKey);
				} else {
					// This bit's length has been remained unmodified
					// We n eed to reduce its lengths by half
					clonedPattern.getBit(bitKey).resize(50, 100);
					this.moveBit(clonedPattern, bitKey, directionInVector, CraftConfig.bitLength / 4);
				}
			}
			this.moveBit(clonedPattern, bitKeyToMove, directionInVector, CraftConfig.bitLength / 2);

			// Redraw the border
			clonedPattern.computeBits(boundary);

			// Next, we try to fill the space left behind.
			// If there is no bit there, we will pass. Else:
			// We reduce the lengths of its adjacent bits in rear by
			// half (and move them backward a little), then insert a full bit in
			// the newly created space. If this does not increase the number of
			// irregularities, we will keep that filling bit. Otherwise, we
			// leave that space uncovered.

			if (adjBitKeysInRear.size() != 0) {
				// To realize the cover, we need to check that in the rear there
				// is no bit that has been reduced its lengths
				boolean realisable = true;
				for (Iterator<Vector2> bitKeyIterator = adjBitKeysInRear.iterator(); bitKeyIterator.hasNext();) {
					Vector2 bitKey = (Vector2) bitKeyIterator.next();
					if (clonedPattern.getBit(bitKey).getLength() != CraftConfig.bitLength) {
						realisable = false;
						break;
					}
				}
				if (realisable) {
					Pattern trialPattern = clonedPattern.clone();
					// Reduce the lengths of bits in the rear by half
					for (Iterator<Vector2> iterator = adjBitKeysInRear.iterator(); iterator.hasNext();) {
						Vector2 bitKey = (Vector2) iterator.next();
						trialPattern.getBit(bitKey).resize(50, 100);
						trialPattern.moveBit(bitKey, rightOrLeft.getOppositeDirection().toVector2(),
								CraftConfig.bitLength / 4);
					}
					// Insert a full bit to fill the space
					Vector2 originalOrientaion = pattern.getBit(bitKeyToMove).getOrientation();
					Vector2 fillingBitOrigin = bitKeyToMove.sub(originalOrientaion.normal()
							.mul(CraftConfig.bitLength / 2 + CraftConfig.bitsLengthSpace / 2));
					trialPattern.addBit(new Bit2D(fillingBitOrigin, originalOrientaion));
					// Redraw border
					trialPattern.computeBits(boundary);

					// Re-validate this choice.
					// If the number of irregularities does not increase,
					// this choice is good enough.
					if (DetectorTool.detectIrregularBits(trialPattern).size() < DetectorTool
							.detectIrregularBits(clonedPattern).size()) {
						clonedPattern = trialPattern;
					}
				}
			}
		}
		return clonedPattern;
	}

	/**
	 * Check if these 2 bits are adjacent in the reality
	 * 
	 * @param bit1
	 * @param bit2
	 * @param actualState
	 *            the actual situation (to get the matrix of transformation)
	 * @return
	 */
	private boolean checkAdjacence(Bit2D bit1, Bit2D bit2, Pattern actualState) {
		// This method needs a default setting that spaces between bits is much
		// smaller than bit's sides

		// Firstly, we need to reform the input bits' origin back to normal
		// coordinate system (no rotation, no translation)
		Vector2 o1 = null;
		Vector2 o2 = null;
		try {
			o1 = bit1.getOrigin().getTransformed(actualState.getAffineTransform().createInverse());
			o2 = bit2.getOrigin().getTransformed(actualState.getAffineTransform().createInverse());
		} catch (NoninvertibleTransformException e) {
			return false;
		}
		Vector2 dist = o1.sub(o2);
		double length1 = bit1.getLength();
		double length2 = bit2.getLength();
		double width1 = bit1.getWidth();
		double width2 = bit2.getWidth();

		// Firstly, we check if they do not overlap.
		// Even if they have only one common point,
		// we will consider it as overlapped.
		// Horizontally && vertically
		if (Math.abs(dist.x) <= (length1 + length2) / 2 || Math.abs(dist.y) <= (width1 + width2) / 2) {
			return false;
		}

		// Secondly, we check if they are not too far
		// Note: the smallest length of a bit is bitLength/2
		// and the smallest width is bitWidth/2
		if (Math.abs(dist.x) > (length1 + length2) / 2 + 2 * CraftConfig.bitsWidthSpace + CraftConfig.bitLength / 2
				|| Math.abs(dist.y) > (width1 + width2) / 2 + 2 * CraftConfig.bitsLengthSpace
						+ CraftConfig.bitWidth / 2) {
			return false;
		}
		return true;
	}

	/**
	 * Move a bit to the left or to the right. The step is by default.
	 * 
	 * @param pattern
	 * @param bitKey
	 * @param direction
	 *            considered in the coordinate system of the chosen bit
	 */
	private void moveHorizontally(Pattern pattern, Vector2 bitKey, Vector2 direction) {
		// TODO
		// We should calculate the distance of moving a bit more exact
		pattern.moveBit(bitKey, direction, CraftConfig.bitLength / 2);
	}

	/**
	 * Move a bit upward or downward. The step is by default.
	 * 
	 * @param pattern
	 * @param bitKey
	 * @param direction
	 *            considered in the coordinate system of the chosen bit
	 */
	private void moveVertically(Pattern pattern, Vector2 bitKey, Vector2 direction) {
		// TODO
		// We should calculate the distance of moving a bit more exact
		pattern.moveBit(bitKey, direction, CraftConfig.bitWidth / 2);
	}

	/**
	 * Check if 2 points P1, P2 are on different sides in comparison to the line
	 * 
	 * @param p1
	 * @param p2
	 * @param line
	 * @return
	 */
	private boolean checkIfOnDifferentSides(Vector2 p1, Vector2 p2, Segment2D line) {
		// TODO Auto-generated method stub
		// Construct the equation of the line
		Vector2 d = line.end.sub(line.start);// directional vector
		Vector2 n = (new Vector2(-d.y, d.x)).normal();// normal vector
		// Equation: n * (v - start) = 0 with v = (x,y)
		if ((n.dot(p1.sub(line.start))) * (n.dot(p2.sub(line.start))) < 0) {
			return true;
		}
		return false;
	}

	/**
	 * Calculate the distance from a point to the a line (or a segment)
	 * 
	 * @param point
	 * @param line
	 * @return
	 */
	private double distanceFromPointToLine(Vector2 point, Segment2D line) {
		Vector2 d = line.end.sub(line.start);// directional vector
		Vector2 n = (new Vector2(-d.y, d.x)).normal();// normal vector
		// Equation: n * (v - start) = 0 with v = (x,y)
		double nLength = n.vSize();
		if (nLength == 0) {
			return 0;
		} else {
			return Math.abs(n.dot(point.sub(line.start))) / nLength;
		}
	}

	@Override
	public void moveBit(Pattern actualState, Vector2 keyOfBitToMove, Vector2 direction) {
		// TODO
		// We should have here an audit to control the movement (not to overlap
		// other bits)
		// The vector will be either (0,1), (0,-1), (1,0), (-1,0)
		if (direction.x == 0) {// up or down
			this.moveVertically(actualState, keyOfBitToMove, direction);
		} else if (direction.y == 0) {// left or right
			this.moveHorizontally(actualState, keyOfBitToMove, direction);
		}
	}

	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 keyOfBitToMove, Vector2 direction, double distance) {
		return actualState.moveBit(keyOfBitToMove, direction, distance);
	}

}