/**
 * 
 */
package meshIneBits.util;

/**
 * Define the direction of moving
 * 
 * @author NHATHAN
 *
 */
public enum Direction {
	RIGHT, LEFT, UP, DOWN, UPRIGHT, DOWNRIGHT, UPLEFT, DOWNLEFT;

	/**
	 * Convert into vector. Only available for {@link #RIGHT}, {@link #LEFT},
	 * {@link #UP}, {@link #DOWN}.
	 * 
	 * @return null if not in {@link #RIGHT}, {@link #LEFT}, {@link #UP},
	 *         {@link #DOWN}.
	 */
	public Vector2 toVector2() {
		switch (this) {
		case RIGHT:
			return new Vector2(-1, 0);
		case LEFT:
			return new Vector2(1, 0);
		case UP:
			return new Vector2(0, -1);
		case DOWN:
			return new Vector2(0, 1);
		default:
			return null;
		}
	}
}
