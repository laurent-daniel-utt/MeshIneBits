package meshIneBits.util;

import java.util.Vector;

/**
 * Segment2D represents a line in 2D space.
 */
public class Segment2D extends AABBrect {
	public Vector2 start;

	public Vector2 end;
	private Vector2 normal;
	private Segment2D next, prev;

	/**
	 * Sort segments by paths
	 */
	@SuppressWarnings("unchecked")
	public static Vector<Vector<Segment2D>> segregateSegments(Vector<Segment2D> segments) {
		Vector<Vector<Segment2D>> segregateSegments = new Vector<Vector<Segment2D>>();
		Vector<Segment2D> currentLine = new Vector<Segment2D>();

		Segment2D nextSegment;

		for (int i = 0; i < segments.size(); i++) {

			if (i < (segments.size() - 1)) {
				nextSegment = segments.get(i + 1);
			} else {
				nextSegment = segments.get(0);
			}

			Segment2D currentSegment = segments.get(i);
			currentLine.add(currentSegment);
			if (!currentSegment.end.asGoodAsEqual(nextSegment.start)) {
				segregateSegments.add((Vector<Segment2D>) currentLine.clone());
				currentLine.clear();
			}
		}
		if (!currentLine.isEmpty()) {
			if (!segregateSegments.isEmpty()) {
				currentLine.addAll(segregateSegments.get(0));
				segregateSegments.set(0, currentLine);
			} else {
				segregateSegments.add(currentLine);
			}
		}
		return segregateSegments;
	}

	public Segment2D(Vector2 start, Vector2 end) {
		// Make the AABB 1mm larger then the actual segment, to account for
		// inaccuracies and moving
		// around the segment ends a bit.
		super(start, end, 1.0);

		update(start, end);
	}

	public double distFromPoint(Vector2 p) {
		Vector2 v = this.start;
		Vector2 w = this.end;
		double l2 = Vector2.dist2(v, w);
		if (l2 == 0) {
			return Vector2.dist2(p, v);
		}
		double t = (((p.x - v.x) * (w.x - v.x)) + ((p.y - v.y) * (w.y - v.y))) / l2;
		t = Math.max(0, Math.min(1, t));

		return Math.sqrt(Vector2.dist2(p, new Vector2(v.x + (t * (w.x - v.x)), v.y + (t * (w.y - v.y)))));
	}

	public double getLength() {
		return Math.sqrt(((end.x - start.x) * (end.x - start.x)) + ((end.y - start.y) * (end.y - start.y)));
	}

	public Vector2 getMidPoint() {
		return this.getPointAtRatio(0.5);
	}

	public Segment2D getNext() {
		return next;
	}

	public Vector2 getNormal() {
		return normal;
	}

	/**
	 * Return the position of the point at a given ratio
	 */
	public Vector2 getPointAtRatio(double ratio) {
		return new Vector2(this.start.x + ((this.end.x - this.start.x) * ratio),
				this.start.y + ((this.end.y - this.start.y) * ratio));
	}

	public Segment2D getPrev() {
		return prev;
	}

	public void setNext(Segment2D newNext) {
		if (newNext == null) {
			if (next == null) {
				throw new UnsupportedOperationException();
			}
			next.prev = null;
			next = null;
		} else {
			if (next != null) {
				throw new UnsupportedOperationException();
			}
			if (newNext.prev != null) {
				throw new UnsupportedOperationException();
			}
			next = newNext;
			next.prev = this;
		}
	}

	@Override
	public String toString() {
		return "Segment:" + start + " " + end;
	}

	/**
	 * For large updates we need to fix the normal, and the AABB. Only call this
	 * when the segment is not in a Tree2D
	 */
	public void update(Vector2 start, Vector2 end) {
		this.start = start;
		this.end = end;
		this.normal = end.sub(start).crossZ().normal();
		updateAABB(start, end, 1.0);
	}

	/**
	 * Check if this segment contains a point (x,y).
	 * 
	 * @param x
	 * @param y
	 * @return
	 * @see {@link Vector2#isOnSegment(Segment2D)}
	 */
	public boolean contains(double x, double y) {
		return (new Vector2(x, y)).isOnSegment(this);
	}

	/**
	 * Check if this segment contains a point (x,y)
	 * 
	 * @param point
	 * @return
	 * @see {@link Vector2#isOnSegment(Segment2D)}
	 */
	public boolean contains(Vector2 point) {
		return point.isOnSegment(this);
	}

	/**
	 * Check if this segment is perpendicular another.
	 * 
	 * @param that
	 * @return
	 */
	public boolean isPerpendicularTo(Segment2D that) {
		Vector2 v1 = this.end.sub(this.start);
		Vector2 v2 = that.end.sub(that.start);
		return (v1.dot(v2) == 0);
	}

}
