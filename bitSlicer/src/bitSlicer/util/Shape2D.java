package bitSlicer.util;

import java.util.Iterator;
import java.util.Vector;

import bitSlicer.util.Polygon;
import bitSlicer.util.AABBrect;

/**
 * Define a shape. Contain one or more polygons.
 */
public class Shape2D implements Iterable<Polygon>
{
	private Vector<Polygon> polygons =  new Vector<Polygon>();
	
	public Shape2D() {
		
	}
	
	public Shape2D(Polygon poly) {
		this.polygons.add(poly);
	}
	
	public Shape2D(Vector<Polygon> polygons) {
		this.polygons = new Vector<Polygon>(polygons);
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
}
