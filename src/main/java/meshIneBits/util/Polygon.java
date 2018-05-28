package meshIneBits.util;

import java.awt.geom.Path2D;
import java.util.Iterator;
import java.util.Vector;

/**
 * A polygon is an enclosed set of Segment2D.
 */
public class Polygon implements Iterable<Segment2D> {
	private Segment2D first = null;

	private Segment2D last = null;
	private boolean enclosed = false;
	/**
	 * Cache of path2D
	 */
	private Path2D _path2D = null;

	public Polygon() {
	}

	public Polygon(Segment2D segment) {
		first = segment;
		if (first == null) {
			return;
		}
		last = first;
		for (Segment2D s = first.getNext(); s != null; s = s.getNext()) {
			if (s == first) {
				enclosed = true;
				break;
			}
			last = s;
		}
	}

	public void addEnd(Segment2D s) {
		if (enclosed) {
			throw new RuntimeException();
		}
		if (first == null) {
			first = s;
		}
		if (last != null) {
			last.setNext(s);
		}
		last = s;
	}

	/**
	 * Check integrity of the polygon.
	 */
	public void check() {
		if (first == null) {
			return;
		}
		if (enclosed) {
			if (first.getPrev() == null) {
				throw new RuntimeException();
			}
			if (last.getNext() == null) {
				throw new RuntimeException();
			}
			if (last.getNext() != first) {
				throw new RuntimeException();
			}
			if (first.getPrev() != last) {
				throw new RuntimeException();
			}
			for (Segment2D s = first.getNext(); s != first; s = s.getNext()) {
				if (s == null) {
					throw new RuntimeException();
				}
				if (s.getPrev().getNext() != s) {
					throw new RuntimeException();
				}
			}
		} else {
			if (first.getPrev() != null) {
				throw new RuntimeException();
			}
			if (last.getNext() != null) {
				throw new RuntimeException();
			}
		}
	}

	public void close() {
		if (enclosed) {
			throw new UnsupportedOperationException();
		}
		check();
		enclosed = true;
		last.setNext(first);
		check();
	}

	/**
	 * Get the closest segment in this segment loop
	 */
	public Segment2D closestTo(Vector2 p) {
		Segment2D best = first;
		double bestDist = 99999;
		for (Segment2D s : this) {
			if (s.start.sub(p).vSize2() < bestDist) {
				bestDist = s.start.sub(p).vSize2();
				best = s;
			}
		}
		return best;
	}

	public boolean contains(Segment2D s) {
		if (this.toPath2D().contains(s.getMidPoint().x, s.getMidPoint().y)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check if a point is inside the polygon. Uses boolean
	 * java.awt.geom.Path2D.contains.
	 */
	public boolean contains(Vector2 point) {
		if (this.toPath2D().contains(point.x, point.y)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean empty() {
		return first == null;
	}

	public AABBrect getAABB() {
		AABBrect ret = new AABBrect(first);
		for (Segment2D s : this) {
			ret.addAABB(s);
		}
		return ret;
	}

	@Override
	public Iterator<Segment2D> iterator() {
		return new Segment2DIterator();
	}

	/**
	 * removeEnd removes this segment from the segment list, and links up the
	 * next segment to the previous. Removing 1 point in the polygon. The point
	 * removed is the endpoint of this segment.
	 */
	public void remove(Segment2D s) {
		if (s == first) {
			first = s.getNext();
			//In case we are enclosed with a single segment, the next is the same one. So we are back to an empty polygon.
			if (first == s) {
				first = null;
			}
		}
		if (s == last) {
			last = last.getPrev();
			//In case we are enclosed with a single segment, the prev is the same one. So we are back to an empty polygon.
			if (last == s) {
				last = null;
			}
		}

		if (s.getNext() == null) {
			if (enclosed) {
				throw new RuntimeException();
			}
			// Remove 's' from the linked list.
			s.getPrev().setNext(null);
		} else {
			// Update the start point of s.next to the end of the previous point. Effectively removing
			// s.end from the polygon.
			s.getNext().update(s.getPrev().end, s.getNext().end);
			// Remove 's' from the linked list.
			// We can set 's.next' to null here, even if we are iterating over 's',
			// because the next point of iteration has already been stored by the iterator.
			Segment2D prev = s.getPrev();
			Segment2D next = s.getNext();
			prev.setNext(null);
			s.setNext(null);
			prev.setNext(next);
		}
	}

	/**
	 * Convert polygon to Path2D. Used to generate area.
	 */
	public Path2D toPath2D() {
		if (_path2D != null)
			return _path2D;
		// If not created
		Vector<Double> x = new Vector<Double>();
		Vector<Double> y = new Vector<Double>();
		for (Segment2D s : this) {
			x.add(s.start.x);
			y.add(s.start.y);
		}

		_path2D = new Path2D.Double();
		_path2D.moveTo(x.get(0), y.get(0));
		for (int i = 1; i < x.size(); ++i) {
			_path2D.lineTo(x.get(i), y.get(i));
		}
		_path2D.closePath();

		_path2D.setWindingRule(Path2D.WIND_EVEN_ODD);

		return _path2D;
	}

	private class Segment2DIterator implements Iterator<Segment2D> {
		private Segment2D next;

		public Segment2DIterator() {
			this.next = first;
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Segment2D next() {
			Segment2D ret = next;
			next = next.getNext();
			if (next == first) {
				next = null;
			}
			return ret;
		}
	}

	/**
	 * Check if this polygon contains entirely the other
	 *
	 * @param other non intersecting with this
	 * @return <tt>true</tt> if all other's vertices stay inside this
	 */
	public boolean contains(Polygon other) {
		// Each time we check the starting point of each segment
		for (Iterator<Segment2D> it = other.iterator(); it.hasNext(); ) {
			Segment2D nextSegment = it.next();
			if (!this.contains(nextSegment.start))
				return false;
		}
		return true;
	}
}
