package bitSlicer.util;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
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
	
}
