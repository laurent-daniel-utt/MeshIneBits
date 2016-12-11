package bitSlicer.util;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
//import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import bitSlicer.Slicer.Config.CraftConfig;

import java.lang.Double;

public class AreaTool {

	/*
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
	*/
	
	public static Vector<Area> getLevel0AreasFrom(Shape2D shape){
		Vector<Area> areas = new Vector<Area>();
		for(Polygon p : shape){
			areas.add(getAreaFrom(p));
		}
		if (areas.isEmpty())
			return null;
		else
			return getLevel0AreasFrom(areas);
	}
	
	public static Area getAreaFrom2(Shape2D shape){
		Area resultArea = new Area();
		Vector<Area> areas = getLevel0AreasFrom(shape);
		for(Area a : areas)
			resultArea.add(a);
		return resultArea;
	}
	
	public static Vector<Area> getLevel0AreasFrom(Vector<Area> areas) {
		
		if (areas.isEmpty())
			return null;

		Vector<Vector<Area>> areasByLevel = new Vector<Vector<Area>>();
		//We fill the vector with null values, it cannot have more levels than areas
		for(Area a : areas)
			areasByLevel.add(null);
				
		/*
		Sort the areas by their "inclusion level": if no other area contains the area A, then A's level will be 0,
		if A is contained by only one then A's level will be 1, etc...
		*/
		int levelMax = 0;
		for(Area currentArea : areas){
			int levelCurrentArea = 0; //If level is even this area is filled, if it's odd this area is a hole
			for(Area otherArea : areas){
				if(!currentArea.equals(otherArea)){
					Area currentAreaClone = (Area) currentArea.clone();
					currentAreaClone.intersect(otherArea);
					if(currentAreaClone.equals(currentArea)){
						//currentArea is inside otherArea
						levelCurrentArea++;
					}
					/*Following code is just a help to understand the algorithm:

					else if(currentAreaClone.equals(otherArea)){
						//otherArea is inside currentArea
					}
					else{
						//These two are two separate areas
					}
					 */
				}
			}
			if(areasByLevel.get(levelCurrentArea) == null){
				for(int i = 0; i <= levelCurrentArea; i++){
					if(areasByLevel.get(i) == null)
						areasByLevel.set(i, new Vector<Area>());
				}
			}
			areasByLevel.get(levelCurrentArea).add(currentArea);
			if(levelCurrentArea > levelMax)
				levelMax = levelCurrentArea;
		}
		
		for(Area level0Area : areasByLevel.get(0)){
			for(int level = 1; level <= levelMax; level++){
				for(Area higherLevelArea : areasByLevel.get(level)){
					if(level % 2 != 0)
						level0Area.subtract(higherLevelArea);
					else
						level0Area.add(higherLevelArea);
				}
			}
		}
		return areasByLevel.get(0);		
	}
	
	public static Area getAreaFrom(Polygon poly) {
		return new Area(poly.toPath2D());
	}
	
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
		
		//Clean the result by removing segments which have the same start and end (java.awt.geom.Area.intersect issue)
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
	
	public static Vector<Area> segregateArea(Area area){
		
		Vector<Vector<Segment2D>> polygons = AreaTool.getSegmentsFrom(area);
		Vector<Area> segregatedAreas = new Vector<Area>();
		
		for(Vector<Segment2D> pathLine : polygons){
			Path2D path2D = new Path2D.Double();
			path2D.moveTo(pathLine.get(0).start.x, pathLine.get(0).start.y);
			for(int i = 1; i < pathLine.size(); i++)
				path2D.lineTo(pathLine.get(i).start.x, pathLine.get(i).start.y);
			//path2D.lineTo(pathLine.get(pathLine.size() - 1).end.x, pathLine.get(pathLine.size() - 1).end.y);
			//cutPaths.add(cutPath2D);
			path2D.closePath();
			segregatedAreas.add(new Area(path2D));
		}
		return AreaTool.getLevel0AreasFrom(segregatedAreas);
	}
	
	public static Point2D getLiftPoint3(Area area, double minRadius){
		
		Rectangle2D bounds = area.getBounds2D();
		double stepX = 1;
		double stepY = 1;
		double startX = bounds.getMinX();
		double startY = bounds.getMinY();
		double endX = bounds.getMaxX();
		double endY = bounds.getMaxY();
		Vector<Point2D.Double> points = new Vector<Point2D.Double>();
		for(double x = startX; x <= endX; x += stepX){
			for(double y = startY; y <= endY; y += stepY){
				Point2D.Double point = new Point2D.Double(x, y);
				if(area.contains(point))
					points.add(point);
			}
		}
		Vector<Double> minDistances = new Vector<Double>();
		Vector<Vector<Segment2D>> segments = AreaTool.getSegmentsFrom(area);
		for(Point2D.Double p : points){
			double minDist = CraftConfig.bitLength * 2; //To be sure every other distances will be smaller
			for(Vector<Segment2D> polygon : segments){
				for(Segment2D segment : polygon){
					double dist = segment.distFromPoint(new Vector2(p.getX(), p.getY()));
					if(dist < minDist)
						minDist = dist;
				}
			}
			minDistances.add(minDist);
		}
		
		//We are looking for the point with the bigger minDistance
		int minDistanceIndex = 0;
		for(int i = points.size() - 1; i > 0; i--){
			//System.out.println(minDistances.get(i));
			if(minDistances.get(i) > minDistances.get(minDistanceIndex))
				minDistanceIndex = i;
		}
		
		if(minDistances.get(minDistanceIndex) < minRadius)
			return null;
		
		Vector<Point2D.Double> okPoints = new Vector<Point2D.Double>();
		for(int i = 0; i < points.size(); i++){
			if((minDistances.get(i) < minDistances.get(minDistanceIndex) + 0.001) && (minDistances.get(i) > minDistances.get(minDistanceIndex) - 0.001)){
				okPoints.add(points.get(i));
			}
		}
		
		Vector2 barycenter = AreaTool.compute2DPolygonCentroid(area);
		
		Point2D.Double liftPoint = okPoints.get(0);
		double distanceTest = (new Segment2D(1, new Vector2(okPoints.get(0).getX(), okPoints.get(0).getY()), barycenter)).getLength();
		for(Point2D p : okPoints){
			double distance = (new Segment2D(1, new Vector2(p.getX(), p.getY()), barycenter)).getLength();
			if(distance < distanceTest){
				liftPoint = (java.awt.geom.Point2D.Double) p;
				distanceTest = distance;
			}
		}
		
		return liftPoint;		
	}
	
	public static Vector2 compute2DPolygonCentroid(Area area){
		
		Vector<Segment2D> segments = getLargestPolygon(area);
		
		Vector<Vector2> vertices = new Vector<Vector2>();
		for(Segment2D s : segments){
			vertices.add(s.start);
			vertices.addElement(s.end);
		}
		
		double centroidX = 0;
	    double centroidY = 0;
	    double signedArea = 0.0;
	    double x0 = 0.0; // Current vertex X
	    double y0 = 0.0; // Current vertex Y
	    double x1 = 0.0; // Next vertex X
	    double y1 = 0.0; // Next vertex Y
	    double a = 0.0;  // Partial signed area
	    int vertexCount = vertices.size();

	    // For all vertices
	    for (int i = 0; i < vertexCount; ++i)
	    {
	        x0 = vertices.get(i).x;
	        y0 = vertices.get(i).y;
	        x1 = vertices.get((i+1) % vertexCount).x;
	        y1 = vertices.get((i+1) % vertexCount).y;
	        a = x0*y1 - x1*y0;
	        signedArea += a;
	        centroidX += (x0 + x1)*a;
	        centroidY += (y0 + y1)*a;
	    }

	    signedArea *= 0.5;
	    centroidX /= (6.0*signedArea);
	    centroidY /= (6.0*signedArea);

	    return new Vector2(centroidX, centroidY);
	}
	
	public static Vector<Segment2D> getLargestPolygon(Area area){
		Vector<Vector<Segment2D>> segments = AreaTool.getSegmentsFrom(area);
		Vector<Double> boundLength = new Vector<Double>();
		for(Vector<Segment2D> poly : segments){
			double length = 0;
			for(Segment2D s : poly)
				length += s.getLength();
			boundLength.add(length);
		}
		int largestPolygonIndex = 0;
		for(int i = 1; i < segments.size(); i++){
			if(boundLength.get(i) > boundLength.get(largestPolygonIndex))
				largestPolygonIndex = i;
		}
		return segments.get(largestPolygonIndex);
	}
	
}
