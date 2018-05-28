package meshIneBits.util;

import meshIneBits.config.CraftConfig;

import java.awt.geom.*;
import java.util.*;

public class AreaTool {

	/**
	 * Returns the barycenter of an area
	 *
	 * @param area
	 * @return Barycenter
	 */
	public static Vector2 compute2DPolygonCentroid(Area area) {

		Vector<Segment2D> segments = getLargestPolygon(area);

		Vector<Vector2> vertices = new Vector<Vector2>();
		for (Segment2D s : segments) {
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
		double a = 0.0; // Partial signed area
		int vertexCount = vertices.size();

		// For all vertices
		for (int i = 0; i < vertexCount; ++i) {
			x0 = vertices.get(i).x;
			y0 = vertices.get(i).y;
			x1 = vertices.get((i + 1) % vertexCount).x;
			y1 = vertices.get((i + 1) % vertexCount).y;
			a = (x0 * y1) - (x1 * y0);
			signedArea += a;
			centroidX += (x0 + x1) * a;
			centroidY += (y0 + y1) * a;
		}

		signedArea *= 0.5;
		centroidX /= (6.0 * signedArea);
		centroidY /= (6.0 * signedArea);

		return new Vector2(centroidX, centroidY);
	}

	/**
	 * @param poly
	 * @return the surface constraint in the given polygon
	 */
	public static Area getAreaFrom(Polygon poly) {
		return new Area(poly.toPath2D());
	}

	/**
	 * @param shape
	 * @return the surface defined by the shape
	 */
	public static Area getAreaFrom(Shape2D shape) {
		Area resultArea = new Area();
		Vector<Area> areas = getLevel0AreasFrom(shape);
		for (Area a : areas) {
			resultArea.add(a);
		}
		return resultArea;
	}

	/**
	 * Returns the outside boundary of an area (an area can have more than one
	 * boundary when there is a/some hole(s) in it)
	 *
	 * @param area
	 * @return outside boundaries
	 */
	public static Vector<Segment2D> getLargestPolygon(Area area) {
		Vector<Vector<Segment2D>> segments = AreaTool.getSegmentsFrom(area);
		Vector<Double> boundLength = new Vector<Double>();
		for (Vector<Segment2D> poly : segments) {
			double length = 0;
			for (Segment2D s : poly) {
				length += s.getLength();
			}
			boundLength.add(length);
		}
		int largestPolygonIndex = 0;
		for (int i = 1; i < segments.size(); i++) {
			if (boundLength.get(i) > boundLength.get(largestPolygonIndex)) {
				largestPolygonIndex = i;
			}
		}
		return segments.get(largestPolygonIndex);
	}

	/**
	 * @param shape
	 * @return All the constraint areas that do not contain any others
	 */
	public static Vector<Area> getLevel0AreasFrom(Shape2D shape) {
		Vector<Area> areas = new Vector<Area>();
		for (Polygon p : shape) {
			areas.add(getAreaFrom(p));
		}
		if (areas.isEmpty()) {
			return null;
		} else {
			return getLevel0AreasFrom(areas);
		}
	}

	/**
	 * @param areas
	 * @return All the constraint areas that do not contain any others
	 * @see #getContinuousSurfacesFrom(Shape2D)
	 */
	public static Vector<Area> getLevel0AreasFrom(Vector<Area> areas) {

		if (areas.isEmpty()) {
			return null;
		}

		Vector<Vector<Area>> areasByLevel = new Vector<Vector<Area>>();
		// We fill the vector with null values, it cannot have more levels than
		// areas
		for (@SuppressWarnings("unused")
				Area a : areas) {
			areasByLevel.add(null);
		}

		/*
		 * Sort the areas by their "inclusion level": if no other area contains
		 * the area A, then A's level will be 0, if A is contained by only one
		 * then A's level will be 1, etc...
		 */
		int levelMax = 0;
		for (Area currentArea : areas) {
			int levelCurrentArea = 0; // If level is even this area is filled,
			// if it's odd this area is a hole
			for (Area otherArea : areas) {
				if (!currentArea.equals(otherArea)) {
					Area currentAreaClone = (Area) currentArea.clone();
					currentAreaClone.intersect(otherArea);
					if (currentAreaClone.equals(currentArea)) {
						// currentArea is inside otherArea
						levelCurrentArea++;
					}
					/*
					 * Following code is just a help to understand the
					 * algorithm:
					 *
					 * else if(currentAreaClone.equals(otherArea)){ //otherArea
					 * is inside currentArea } else{ //These two are two
					 * separate areas }
					 */
				}
			}
			if (areasByLevel.get(levelCurrentArea) == null) {
				for (int i = 0; i <= levelCurrentArea; i++) {
					if (areasByLevel.get(i) == null) {
						areasByLevel.set(i, new Vector<Area>());
					}
				}
			}
			areasByLevel.get(levelCurrentArea).add(currentArea);
			if (levelCurrentArea > levelMax) {
				levelMax = levelCurrentArea;
			}
		}

		for (Area level0Area : areasByLevel.get(0)) {
			for (int level = 1; level <= levelMax; level++) {
				for (Area higherLevelArea : areasByLevel.get(level)) {
					if ((level % 2) != 0) {
						level0Area.subtract(higherLevelArea);
					} else {
						level0Area.add(higherLevelArea);
					}
				}
			}
		}
		return areasByLevel.get(0);
	}

	/**
	 * Get separated areas from a {@link Shape2D}.
	 *
	 * @param shape supposedly contains non self-intersected and non
	 *              inter-intersected polygons
	 * @return list of continuous area. Empty if no area
	 * @since 0.3
	 */
	public static List<Area> getContinuousSurfacesFrom(Shape2D shape) {
		Map<Polygon, Integer> ranking = new HashMap<>();
		for (Polygon newPolygon : shape.polygons) {
			List<Polygon> containingPolygons = new Vector<>();
			List<Polygon> containedPolygons = new Vector<>();
			for (Polygon polygon : ranking.keySet()) {
				if (polygon.contains(newPolygon)) {
					// Check the keys containing the new polygon
					containingPolygons.add(polygon);
				} else if (newPolygon.contains(polygon)) {
					// Check if it contains old polygons
					containedPolygons.add(polygon);
				}
			}
			// Calculate the level of new polygon
			ranking.put(newPolygon, 0);
			containingPolygons.stream()
					.mapToInt(ranking::get).max()
					.ifPresent(l -> ranking.put(newPolygon, l + 1));
			// Promote level of contained polygons
			containedPolygons.forEach((Polygon p)
					-> ranking.put(p, ranking.get(p) + 1));
		}
		// Regroup into level
		List<List<Polygon>> classing = new Vector<>();
		shape.polygons.forEach(polygon -> classing.add(new Vector<>()));
		ranking.keySet().forEach((Polygon p) -> {
			int r = ranking.get(p);
			if (classing.get(r) == null)
				classing.set(r, new Vector<>());
			else
				classing.get(r).add(p);
		});
		// Calculate areas
		// Even level polygons will be the outer border,
		// minus the holes which are the next odd level polygons
		List<Area> surfaces = new Vector<>();
		for (int i = 0; i < classing.size(); i++) {
			assert classing.get(i) != null;
			if (i % 2 != 0) continue;
			// Prepare the big hole
			Area hole = new Area();
			if (i + 1 < classing.size())
				classing.get(i + 1).forEach((Polygon p)
						-> hole.add(AreaTool.getAreaFrom(p)));
			// Dig the polygon in even level
			for (Polygon p : classing.get(i)) {
				Area newSurface = AreaTool.getAreaFrom(p);
				newSurface.subtract(hole);
				if (!newSurface.isEmpty())
					surfaces.add(newSurface);
			}
		}
		return surfaces;
	}

	/**
	 * Returns the best point to take that bit. By best we mean the point the
	 * closest to the barycenter of the bit and presenting enough material
	 * around for the sucker cup to work properly. It returns null if this bit
	 * cannot be lifted.
	 *
	 * @param area
	 * @param minRadius
	 * @return liftPoint
	 */
	public static Vector2 getLiftPoint(Area area, double minRadius) {

		// We check if the barycenter would be ok
		Vector2 barycenter = AreaTool.compute2DPolygonCentroid(area);
		Vector<Vector<Segment2D>> segments = AreaTool.getSegmentsFrom(area);
		if (area.contains(barycenter.x, barycenter.y)) {
			// To be sure every other
			// distances will be smaller
			double minDist = CraftConfig.bitLength * 2;
			for (Vector<Segment2D> polygon : segments) {
				for (Segment2D segment : polygon) {
					double dist = segment.distFromPoint(barycenter);
					if (dist < minDist) {
						minDist = dist;
					}
				}
			}
			if (minDist >= minRadius) {
				return new Vector2(barycenter.x, barycenter.y);
			}
		}
		// In case the barycenter is not in the area
		// or the circle of sucker is not fit in the area,
		// we fill the area with points
		Rectangle2D bounds = area.getBounds2D();
		double stepX = 1;
		double stepY = 1;
		double startX = bounds.getMinX();
		double startY = bounds.getMinY();
		double endX = bounds.getMaxX();
		double endY = bounds.getMaxY();
		Vector<Vector2> points = new Vector<Vector2>();
		for (double x = startX; x <= endX; x += stepX) {
			for (double y = startY; y <= endY; y += stepY) {
				Vector2 point = new Vector2(x, y);
				if (area.contains(new Point2D.Double(point.x, point.y))) {
					points.add(point);
				}
			}
		}

		if (points.isEmpty()) {
			return null;
		}

		// We sort the points by their distance from the barycenter, the smaller
		// distances on top
		Vector<Double> distances = new Vector<Double>();
		Vector<Vector2> sortedPoints = new Vector<Vector2>();
		distances.add(Math.sqrt(Vector2.dist2(new Vector2(points.get(0).x, points.get(0).y), barycenter)));
		sortedPoints.add(points.get(0));
		for (int j = 1; j < points.size(); j++) {
			double distance = Math.sqrt(Vector2.dist2(new Vector2(points.get(j).x, points.get(j).y), barycenter));
			boolean addAtTheEnd = true;
			for (int i = 0; i < distances.size(); i++) {
				if (distance < distances.get(i)) {
					distances.insertElementAt(distance, i);
					sortedPoints.insertElementAt(points.get(j), i);
					addAtTheEnd = false;
					break;
				}
			}
			if (addAtTheEnd) {
				distances.addElement(distance);
				sortedPoints.add(points.get(j));
			}
		}

		// We review each points and check if it is far enough from the edges to
		// fit the sucker cup, the first one to be ok will be the liftPoint
		Vector2 liftPoint = null;
		for (Vector2 p : sortedPoints) {
			// To be sure every other distances will be smaller
			double minDistFromBounds = CraftConfig.bitLength * 2;
			for (Vector<Segment2D> polygon : segments) {
				for (Segment2D segment : polygon) {
					double dist = segment.distFromPoint(new Vector2(p.x, p.y));
					if (dist < minDistFromBounds) {
						minDistFromBounds = dist;
					}
				}
			}
			if (minDistFromBounds >= minRadius) {
				liftPoint = p;
				break;
			}
		}
		return liftPoint;
	}

	/**
	 * It converts the outline of an area into a vector of segment2D. Taken
	 * from
	 * <a href= "http://stackoverflow.com/questions/8144156/using-pathiterator-to-return-all-line-segments-that-constrain-an-area">this
	 * link</a>.
	 */
	public static Vector<Vector<Segment2D>> getSegmentsFrom(Area area) {
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
			if (type == PathIterator.SEG_MOVETO) {
				polygonCount++;
			}
		}

		double[] start = new double[3]; // To record where each polygon starts

		Vector<Vector<Segment2D>> polygons = new Vector<Vector<Segment2D>>(polygonCount);

		for (int i = 0; i < polygonCount; i++) {
			polygons.add(new Vector<Segment2D>());
		}
		int currentPolygonIndex = 0;

		for (int i = 0; i < areaPoints.size(); i++) {
			// If we're not on the last point, return a line from this point to
			// the next
			double[] currentElement = areaPoints.get(i);

			// We need a default value in case we've reached the end of the
			// ArrayList
			double[] nextElement = {-1, -1, -1};
			if (i < (areaPoints.size() - 1)) {
				nextElement = areaPoints.get(i + 1);
			}

			// Make the lines
			if (currentElement[0] == PathIterator.SEG_MOVETO) {
				start = currentElement; // Record where the polygon started to
				// close it later
				if (!polygons.get(currentPolygonIndex).isEmpty()) {
					currentPolygonIndex++;
					if (currentPolygonIndex >= polygonCount) {
						currentPolygonIndex = 0;
					}
				}
			}

			if (nextElement[0] == PathIterator.SEG_LINETO) {
				polygons.get(currentPolygonIndex).insertElementAt(new Segment2D(
						new Vector2(nextElement[1], nextElement[2]), new Vector2(currentElement[1], currentElement[2])

				), 0);
			} else if (nextElement[0] == PathIterator.SEG_CLOSE) {
				polygons.get(currentPolygonIndex).insertElementAt(
						new Segment2D(new Vector2(start[1], start[2]), new Vector2(currentElement[1], currentElement[2])

						), 0);
			}
		}

		// Clean the result by removing segments which have the same start and
		// end (java.awt.geom.Area.intersect issue)
		Iterator<Vector<Segment2D>> itrPolygons = polygons.iterator();
		while (itrPolygons.hasNext()) {
			Vector<Segment2D> polygon = itrPolygons.next();
			Iterator<Segment2D> itr = polygon.iterator();
			while (itr.hasNext()) {
				Segment2D s = itr.next();
				if (s.start.asGoodAsEqual(s.end)) {
					itr.remove();
				}
			}
			if (polygon.isEmpty()) {
				itrPolygons.remove();
			}
		}

		// areaSegments now contains all the line segments
		return polygons;
	}

	/**
	 * @param area
	 * @return the separated surfaces
	 */
	public static Vector<Area> segregateArea(Area area) {

		Vector<Vector<Segment2D>> polygons = AreaTool.getSegmentsFrom(area);
		Vector<Area> segregatedAreas = new Vector<Area>();

		for (Vector<Segment2D> pathLine : polygons) {
			Path2D path2D = new Path2D.Double();
			path2D.moveTo(pathLine.get(0).start.x, pathLine.get(0).start.y);
			for (int i = 1; i < pathLine.size(); i++) {
				path2D.lineTo(pathLine.get(i).start.x, pathLine.get(i).start.y);
			}
			// path2D.lineTo(pathLine.get(pathLine.size() - 1).end.x,
			// pathLine.get(pathLine.size() - 1).end.y);
			// cutPaths.add(cutPath2D);
			path2D.closePath();
			segregatedAreas.add(new Area(path2D));
		}
		return AreaTool.getLevel0AreasFrom(segregatedAreas);
	}

}
