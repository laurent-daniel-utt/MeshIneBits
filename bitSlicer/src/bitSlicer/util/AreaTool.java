package bitSlicer.util;

import java.awt.geom.Area;
import java.awt.geom.Path2D;

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
	
	public static Path2D getIntersectionPath(Area area1, Area area2) {
		
	}
}
