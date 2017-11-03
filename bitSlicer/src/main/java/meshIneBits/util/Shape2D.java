package meshIneBits.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import meshIneBits.config.CraftConfig;

/**
 * Shape2D is composed of {@link Segment2D} and {@link Polygon}. The segments represent all the
 * "lines" that make up the outline but do not make it possible to know, for
 * example, whether a point is inside or outside the matter. This is where the
 * polygons are useful: a polygon is a closed set of segments, so there is an
 * inside and an outside. The Polygon class contains references to the Segment2D
 * contained in a Shape2D. There can be several polygons per Shape2D, if for
 * example a piece is hollow, pierced, with a hanse ...
 * <br><img src="./doc-files/polygone.png" alt="">
 */
public class Shape2D implements Iterable<Polygon> {
	protected Vector<Segment2D> segmentList = new Vector<Segment2D>();
	protected Vector<Polygon> polygons = new Vector<Polygon>();
	protected AABBTree<Segment2D> segmentTree = new AABBTree<Segment2D>(); // The Tree2D allows a fast query of all objects in an area.

	public Shape2D() {

	}

	private void addModelPolygon(Polygon poly) {
		for (Segment2D s : poly) {
			if (s.getNormal().dot(s.getNext().getNormal()) > CraftConfig.joinMinCosAngle) {
				removeModelSegment(s);
				Segment2D next = s.getNext();
				segmentTree.remove(next);
				poly.remove(s);
				segmentTree.insert(next);
			}
		}
		this.addPolygon(poly);
	}

	public void addModelSegment(Segment2D segment) {
		if (segment.start.asGoodAsEqual(segment.end)) {
			return;
		}
		segmentTree.insert(segment);
		segmentList.add(segment);
	}

	private void addPolygon(Polygon poly) {
		poly.check();
		if (poly.empty()) {
			return;
		}
		polygons.add(poly);
	}

	public Vector<Segment2D> getSegmentList() {
		return this.segmentList;
	}

	@Override
	public Iterator<Polygon> iterator() {
		return polygons.iterator();
	}

	/**
	 * Link up the segments with start/ends, so polygons are created.
	 * 
	 * @return if this process encounters an error
	 */
	public boolean optimize() {

		for (Segment2D s1 : segmentList) {
			if (s1.getPrev() == null) {
				Segment2D best = null;
				double bestDist2 = 0.01;
				for (Segment2D s2 : segmentTree.query(new AABBrect(s1.start, s1.start))) {
					if ((s1 != s2) && (s2.getNext() == null) && s1.start.asGoodAsEqual(s2.end) && (s1.start.sub(s2.end).vSize2() < bestDist2)) {
						best = s2;
						bestDist2 = s1.start.sub(s2.end).vSize2();
						break;
					}
				}
				if (best != null) {
					s1.start = best.end;
					best.setNext(s1);
				}
			}
			if (s1.getNext() == null) {
				Segment2D best = null;
				double bestDist2 = 0.01;
				for (Segment2D s2 : segmentTree.query(new AABBrect(s1.end, s1.end))) {
					if ((s1 != s2) && (s2.getPrev() == null) && s1.end.asGoodAsEqual(s2.start) && (s1.end.sub(s2.start).vSize2() < bestDist2)) {
						best = s2;
						bestDist2 = s1.end.sub(s2.start).vSize2();
						break;
					}
				}
				if (best != null) {
					s1.end = best.start;
					s1.setNext(best);
				}
			}
		}

		for (Segment2D s : segmentList) {
			if ((s.getPrev() != null) && (s.getPrev().getNext() != s)) {
				throw new RuntimeException();
			}
			if ((s.getNext() != null) && (s.getNext().getPrev() != s)) {
				throw new RuntimeException();
			}
			if ((s.getNext() != null) && !segmentList.contains(s.getNext())) {
				throw new RuntimeException();
			}
			if ((s.getPrev() != null) && !segmentList.contains(s.getPrev())) {
				throw new RuntimeException();
			}
		}

		boolean manifoldErrorReported = false;
		HashSet<Segment2D> tmpSet = new HashSet<Segment2D>(segmentList);
		while (tmpSet.size() > 0) {
			Segment2D start = tmpSet.iterator().next();
			boolean manifold = false;
			for (Segment2D s = start; s != null; s = s.getNext()) {
				if (!tmpSet.contains(s)) {
					Logger.warning("Problem : tried to create a segment link from links that where already used...");
					break;
				}
				if (s.getNext() == start) {
					manifold = true;
					break;
				}
			}
			if (manifold) {
				Polygon poly = new Polygon(start);
				for (Segment2D s : poly) {
					tmpSet.remove(s);
				}
				addModelPolygon(poly);
			} else {
				if (!manifoldErrorReported) {
					Logger.warning("Object not manifold");

					for (Segment2D s : segmentList) {
						System.out.println(s);

					}
					throw new RuntimeException();

				}

				manifoldErrorReported = true;
				for (Segment2D s = start; s != null; s = s.getNext()) {
					tmpSet.remove(s);
					if (s.getNext() == start) {
						break;
					}
				}
				for (Segment2D s = start; s != null; s = s.getPrev()) {
					tmpSet.remove(s);
					if (s.getPrev() == start) {
						break;
					}
				}
			}
		}

		return manifoldErrorReported;
	}

	private void removeModelSegment(Segment2D segment) {
		segmentList.remove(segment);
		segmentTree.remove(segment);
	}
}
