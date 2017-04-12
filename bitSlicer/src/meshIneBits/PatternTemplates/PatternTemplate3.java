package meshIneBits.PatternTemplates;

import java.awt.geom.Area;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.Config.CraftConfig;
import meshIneBits.util.AreaTool;
import meshIneBits.util.DetectorTool;
import meshIneBits.util.Direction;
import meshIneBits.util.Optimizer;
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
		// TODO Auto-generated method stub
		boolean allFail = false;// this boolean to check that if we'd tried all
								// possibilities
		while (!allFail) {
			Pattern pattern = actualState.getSelectedPattern();
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
				dirToMove = attemptToSolve(pattern, irBitKey);
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
	 * Attempt to move the bit in 4 directions. Prioritizing the height's sides
	 * 
	 * @param pattern
	 * @param irBitKey
	 *            the key of the bit to try
	 * @return the first direction which reduce the total number of irregular
	 *         bits in the pattern. Null if no way to get better state.
	 */
	private Direction attemptToSolve(Pattern pattern, Vector2 irBitKey) {
		int initialSize = pattern.getBitsKeys().size();
		if (initialSize > attemptToMoveLeftRight(pattern, irBitKey, Direction.RIGHT).getBitsKeys().size()) {
			return Direction.RIGHT;
		}
		if (initialSize > attemptToMoveLeftRight(pattern, irBitKey, Direction.LEFT).getBitsKeys().size()) {
			return Direction.LEFT;
		}
		if (initialSize > attemptToMoveUpDown(pattern, irBitKey, Direction.UP).getBitsKeys().size()) {
			return Direction.UP;
		}
		if (initialSize > attemptToMoveUpDown(pattern, irBitKey, Direction.DOWN).getBitsKeys().size()) {
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
	 * @param irBitKey
	 * @param upOrDown
	 *            considered in the coordinate system of the chosen bit
	 * @return the visualization of pattern after the move
	 */
	private Pattern attemptToMoveUpDown(Pattern pattern, Vector2 irBitKey, Direction upOrDown) {
		// TODO Auto-generated method stub
		return pattern;
	}

	/**
	 * Attempting to move a bit to the left or to the right.
	 * 
	 * If it detects an other bit in that direction, it will reduce its width by
	 * half.
	 * 
	 * It will always work on a clone of the input pattern.
	 * 
	 * @param pattern
	 * @param bitKeyToMove
	 * @param rightOrLeft
	 *            considered in the coordinate system of the chosen bit
	 * @return the visualization of pattern after the move
	 */
	private Pattern attemptToMoveLeftRight(Pattern pattern, Vector2 bitKeyToMove, Direction rightOrLeft) {
		// TODO Auto-generated method stub
		Pattern clonedPattern = pattern.clone();
		Bit2D bitToMove = clonedPattern.getBit(bitKeyToMove);
		HashMap<Direction, Vector2> corners = bitToMove.getTransfoNormalCorners();
		// Constructing the side in the direction we will move
		Segment2D side = null;
		Vector2 dir = rightOrLeft.toVector2();
		switch (rightOrLeft) {
		case RIGHT:
			side = new Segment2D(corners.get(Direction.DOWNRIGHT), corners.get(Direction.UPRIGHT));
			break;
		case LEFT:
			side = new Segment2D(corners.get(Direction.UPLEFT), corners.get(Direction.DOWNLEFT));
			break;
		default:
			break;
		}
		// Find the nearest bit in that direction
		double dist = 0;
		Vector2 nearestBitKey = null;
		for (Vector2 bitKey : clonedPattern.getBitsKeys()) {
			// Check if the vector connecting between centers of irBit and bit
			// is perpendicular to side
			if (side.isPerpendicularTo(new Segment2D(bitKeyToMove, bitKey))) {
				// Check if that bit's origin is on the other side of side
				if (isOnDifferentSides(bitKeyToMove, bitKey, side)) {
					if (dist == 0 || Vector2.dist2(bitKeyToMove, bitKey) < dist) {
						dist = Vector2.dist2(bitKeyToMove, bitKey);
						nearestBitKey = bitKey;
					}
				}
			}
		}
		if (nearestBitKey == null) {
			// No bit detected in that direction
			// That means no use in moving that way
			return clonedPattern;
		} else {
			// We will try to reduce the nearest bit by half of its length
			// in order to have space to move the irregular bit
			Bit2D nearestBit = clonedPattern.getBit(nearestBitKey);
			if (nearestBit.getLength() != CraftConfig.bitLength) {
				// This bit has already been reduced recently
				// We just need to remove it
				clonedPattern.removeBit(nearestBitKey);
			} else {
				// This bit's length has been remained unmodified
				// We need to reduce its lengths by half
				clonedPattern.removeBit(nearestBitKey);
				// Calculate the new position of the origin
				Bit2D newBit = new Bit2D(nearestBit, 50, 100);
				clonedPattern.addBit(newBit);
				clonedPattern.moveBit(newBit.getOrigin(), dir, CraftConfig.bitLength / 2);
			}
		}
		moveLeftRight(clonedPattern, bitKeyToMove, rightOrLeft.toVector2());
		return clonedPattern;
	}

	/**
	 * Move a bit to the left or to the right.
	 * 
	 * @param pattern
	 * @param bitKey
	 * @param direction
	 *            considered in the coordinate system of the chosen bit
	 */
	private void moveLeftRight(Pattern pattern, Vector2 bitKey, Vector2 direction) {
		// TODO
		// We should calculate the distance of moving a bit more exact
		pattern.moveBit(bitKey, direction, CraftConfig.bitLength / 2);
	}

	/**
	 * Move a bit upward or downward.
	 * 
	 * @param pattern
	 * @param bitKey
	 * @param direction
	 *            considered in the coordinate system of the chosen bit
	 */
	private void moveUpDown(Pattern pattern, Vector2 bitKey, Vector2 direction) {
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
	private boolean isOnDifferentSides(Vector2 p1, Vector2 p2, Segment2D line) {
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

	@Override
	public void moveBit(Pattern actualState, Vector2 keyOfBitToMove, Vector2 direction) {
		// TODO
		// We should have here an audit to control the movement (not to overlap
		// other bits)
		// The vector will be either (0,1), (0,-1), (1,0), (-1,0)
		if (direction.x == 0) {// up or down
			this.moveUpDown(actualState, keyOfBitToMove, direction);
		} else if (direction.y == 0) {// left or right
			this.moveLeftRight(actualState, keyOfBitToMove, direction);
		}
	}

}