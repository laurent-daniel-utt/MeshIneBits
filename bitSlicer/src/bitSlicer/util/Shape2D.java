package bitSlicer.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import bitSlicer.util.Polygon;
import bitSlicer.util.Segment2D;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.AABBTree;
import bitSlicer.util.Logger;
import bitSlicer.util.Vector2;
import bitSlicer.util.AABBrect;

/**
 * Define a shape. Contain one or more polygons. Polygons are created from the segmentList
 */
public class Shape2D implements Iterable<Polygon>
{
	private Vector<Segment2D> segmentList = new Vector<Segment2D>();
	private Vector<Polygon> polygons =  new Vector<Polygon>();
	private AABBTree<Segment2D> segmentTree = new AABBTree<Segment2D>(); // The Tree2D allows a fast query of all objects in an area.
	
	public Shape2D() {
		
	}
	
	public Shape2D(Polygon poly) {
		this.polygons.add(poly);
	}
	
	public Shape2D(Vector<Segment2D> segmentList) {
		this.segmentList = new Vector<Segment2D>(segmentList);
	}

	public Vector<Segment2D> getSegmentList() {
		return this.segmentList;
	}
	
	public void addModelSegment(Segment2D segment)
	{
		if (segment.start.asGoodAsEqual(segment.end))
			return;
		segmentTree.insert(segment);
		segmentList.add(segment);
	}
	
	private void removeModelSegment(Segment2D segment)
	{
		segmentList.remove(segment);
		segmentTree.remove(segment);
	}
	
	public boolean optimize()
	{
		// Link up the segments with start/ends, so polygons are created.
		for (Segment2D s1 : segmentList)
		{
			if (s1.getPrev() == null)
			{
				Segment2D best = null;
				double bestDist2 = 0.01;
				for (Segment2D s2 : segmentTree.query(new AABBrect(s1.start, s1.start)))
				{
					if (s1 != s2 && s2.getNext() == null && s1.start.asGoodAsEqual(s2.end) && s1.start.sub(s2.end).vSize2() < bestDist2)
					{
						best = s2;
						bestDist2 = s1.start.sub(s2.end).vSize2();
						break;
					}
				}
				if (best != null)
				{
					s1.start = best.end;
					best.setNext(s1);
				}
			}
			if (s1.getNext() == null)
			{
				Segment2D best = null;
				double bestDist2 = 0.01;
				for (Segment2D s2 : segmentTree.query(new AABBrect(s1.end, s1.end)))
				{
					if (s1 != s2 && s2.getPrev() == null && s1.end.asGoodAsEqual(s2.start) && s1.end.sub(s2.start).vSize2() < bestDist2)
					{
						best = s2;
						bestDist2 = s1.end.sub(s2.start).vSize2();
						break;
					}
				}
				if (best != null)
				{
					s1.end = best.start;
					s1.setNext(best);
				}
			}
		}
		
		for (Segment2D s : segmentList)
		{
			if (s.getPrev() != null && s.getPrev().getNext() != s)
				throw new RuntimeException();
			if (s.getNext() != null && s.getNext().getPrev() != s)
				throw new RuntimeException();
			if (s.getNext() != null && !segmentList.contains(s.getNext()))
				throw new RuntimeException();
			if (s.getPrev() != null && !segmentList.contains(s.getPrev()))
				throw new RuntimeException();
		}
		
		boolean manifoldErrorReported = false;
		HashSet<Segment2D> tmpSet = new HashSet<Segment2D>(segmentList);
		while (tmpSet.size() > 0)
		{
			Segment2D start = tmpSet.iterator().next();
			boolean manifold = false;
			for (Segment2D s = start; s != null; s = s.getNext())
			{
				if (!tmpSet.contains(s))
				{
					Logger.warning("Problem : tried to create a segment link from links that where already used...");
					break;
				}
				if (s.getNext() == start)
				{
					manifold = true;
					break;
				}
			}
			if (manifold)
			{
				Polygon poly = new Polygon(start);
				for (Segment2D s : poly)
				{
					tmpSet.remove(s);
				}
				addModelPolygon(poly);
			} else
			{
				if (!manifoldErrorReported)
					Logger.warning("Object not manifold");
				manifoldErrorReported = true;
				for (Segment2D s = start; s != null; s = s.getNext())
				{
					tmpSet.remove(s);
					s.setType(Segment2D.TYPE_ERROR);
					if (s.getNext() == start)
						break;
				}
				for (Segment2D s = start; s != null; s = s.getPrev())
				{
					tmpSet.remove(s);
					s.setType(Segment2D.TYPE_ERROR);
					if (s.getPrev() == start)
						break;
				}
			}
		}
		return manifoldErrorReported;
	}
	
	private void addModelPolygon(Polygon poly)
	{
		for (Segment2D s : poly)
		{
			if (s.getNormal().dot(s.getNext().getNormal()) > CraftConfig.joinMinCosAngle)
			{
				removeModelSegment(s);
				Segment2D next = s.getNext();
				segmentTree.remove(next);
				poly.remove(s);
				segmentTree.insert(next);
			}
		}
		this.addPolygon(poly);
	}
	
	public boolean contains(Vector2 point) {
		int i = 0;
		for (Polygon poly : this) {
			if (poly.contains(point))
				i++;
		}
		
		return (i % 2 == 0) ? false : true; // If Bit in inside 2n poly, then it's outside. If inside 2n+1 poly it's inside.
	}
	
	/**
	 * Count of many walls of the shape a segment is colliding with
	 */
	public int countCollisions(Segment2D segment) {
		int result = 0;
		for (Segment2D s : this.getSegmentList()) {
			Vector2 collisionPoint = s.getCollisionPoint(segment);
			if (collisionPoint != null)
				result++;
		}
		
		return result;
	}
	
	public Iterator<Polygon> iterator() {
		return polygons.iterator();
	}
	
	public Polygon getLargestPolygon()
	{
		Polygon largestPoly = null;
		double largestPolySize = 0;
		for (Polygon poly : polygons)
		{
			AABBrect polygonRect = poly.getAABB();
			
			if (polygonRect.getPerimeter() > largestPolySize)
			{
				largestPolySize = polygonRect.getPerimeter();
				largestPoly = poly;
			}
		}
		return largestPoly;
	}
	
	public void addPolygon(Polygon poly)
	{
		poly.check();
		if (poly.empty())
		{
			return;
		}
		polygons.add(poly);
	}
	
	/**
	 * Return the segment of the shape in parameter that cut this shape (ie segment inside this shape)
	 */
	public Vector<Segment2D> getCuttingSegmentsFrom(Shape2D shape) {
		Vector<Segment2D> cuttingSegmentsList = new Vector<Segment2D>();
		for (Segment2D s : shape.getSegmentList()) {
			for(Segment2D trimmedSegment : s.trim(this))
				cuttingSegmentsList.add(trimmedSegment);
		}	
		
		return cuttingSegmentsList;
	}
	
	/**
	 * Return a shape with removed parts outside of the shape in parameter
	 * TODO make this.optimize possible
	 */
	public Shape2D trim(Shape2D shape) {
		Vector<Segment2D> newShapeSegmentList = new Vector<Segment2D>();
		
		// Get segments inside shape tool
		for (Segment2D s : this.getSegmentList()) {
			for(Segment2D trimmedSegment : s.trim(shape))
				newShapeSegmentList.add(trimmedSegment);
		}
		
		// Get segments of the shapes tools that cut through this shape
		for (Segment2D s : this.getCuttingSegmentsFrom(shape))
			newShapeSegmentList.add(s);
		
		return new Shape2D(newShapeSegmentList);
	}
}
