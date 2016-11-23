package bitSlicer.util;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Iterator;
import java.util.Vector;

public class AreaTool {
	public static Area getAreaFrom(Shape2D shape) {
		if (shape.getLargestPolygon() != null) {
			Area area = getAreaFrom(shape.getLargestPolygon());
			for (Polygon poly : shape) {
				if (poly != shape.getLargestPolygon())
					area.subtract(getAreaFrom(poly));
			}
			return area;
		}
		else
			return null;
		
	}
	
	public static Area getAreaFrom(Polygon poly) {
		return new Area(poly.toPath2D());
	}
	
	/*
	public static Path2D getIntersectionPath(Area area1, Area area2) {
		
	}
	*/
	
	public static Vector<double[]> getPathPoints(Area area){
		
		Vector<double[]> areaPoints = new Vector<double[]>();
		double[] coords = new double[6];

		for (PathIterator pi = area.getPathIterator(null); !pi.isDone(); pi.next()) {
	    	// The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
	    	// Because the Area is composed of straight lines
	    	int type = pi.currentSegment(coords);
	    	// We record a double array of {segment type, x coord, y coord}
	    	double[] pathIteratorCoords = {type, coords[0], coords[1]};
	    	areaPoints.add(pathIteratorCoords);
		}
		return areaPoints;
	}
	
	/*
	 * taken from http://stackoverflow.com/questions/8144156/using-pathiterator-to-return-all-line-segments-that-constrain-an-area
	 * It converts the outline of an area into a vector of segment2D
	 */
	public static Vector<Vector<Segment2D>> getSegmentsFrom(Area area){  
		Vector<double[]> areaPoints = new Vector<double[]>();
		
		double[] coords = new double[6];
		int polygonCount = 0;

		for (PathIterator pi = area.getPathIterator(null); !pi.isDone(); pi.next()) {
		    // The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
		    // Because the Area is composed of straight lines
		    int type = pi.currentSegment(coords);
		    // We record a double array of {segment type, x coord, y coord}
		    double[] pathIteratorCoords = {type, coords[0], coords[1]};
		    areaPoints.add(pathIteratorCoords);
		    if (type == PathIterator.SEG_MOVETO)
		    	polygonCount++;
		}

		double[] start = new double[3]; // To record where each polygon starts
		
		Vector<Vector<Segment2D>> polygons = new Vector<Vector<Segment2D>>(polygonCount);
		
		for(int i = 0; i < polygonCount; i++)
			polygons.add(new Vector<Segment2D>());
		int currentPolygonIndex = 0;

		for (int i = 0; i < areaPoints.size(); i++) {
		    // If we're not on the last point, return a line from this point to the next
		    double[] currentElement = areaPoints.get(i);

		    // We need a default value in case we've reached the end of the ArrayList
		    double[] nextElement = {-1, -1, -1};
		    if (i < areaPoints.size() - 1) {
		        nextElement = areaPoints.get(i + 1);
		    }

		    // Make the lines
		    if (currentElement[0] == PathIterator.SEG_MOVETO) {
		        start = currentElement; // Record where the polygon started to close it later
		        if(!polygons.get(currentPolygonIndex).isEmpty()){
		        	currentPolygonIndex++;
		        	if(currentPolygonIndex >= polygonCount)
		        		currentPolygonIndex = 0;
		        }    
		    } 

		    if (nextElement[0] == PathIterator.SEG_LINETO) {
		        polygons.get(currentPolygonIndex).insertElementAt(
		                new Segment2D(1,
		                		new Vector2(nextElement[1], nextElement[2]),
		                		new Vector2(currentElement[1], currentElement[2])
		                		
		                	)
		           ,0);
		    } else if (nextElement[0] == PathIterator.SEG_CLOSE) {
		    	polygons.get(currentPolygonIndex).insertElementAt(
		                new Segment2D(1,
		                		new Vector2(start[1], start[2]),
		                		new Vector2(currentElement[1], currentElement[2])
		                		
		                	)
		            ,0);
		    }
		}
		
		//Clean the result by removing segments which have the same start and end
		Iterator<Vector<Segment2D>> itrPolygons = polygons.iterator();
		while(itrPolygons.hasNext()){
			Vector<Segment2D> polygon = itrPolygons.next();
			Iterator<Segment2D> itr = polygon.iterator();
			while(itr.hasNext()){
		         Segment2D s = itr.next();
		         if(s.start.asGoodAsEqual(s.end)){
		        	 itr.remove();
		         }
		    }
			if(polygon.isEmpty())
				itrPolygons.remove();
		}
		

		// areaSegments now contains all the line segments
		return polygons;
	}
	
}
