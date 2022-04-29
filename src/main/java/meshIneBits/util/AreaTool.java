/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2021 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020 CLARIS Etienne & RUSSO André.
 * Copyright (C) 2020-2021 DO Quang Bao.
 * Copyright (C) 2021 VANNIYASINGAM Mithulan.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package meshIneBits.util;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;
import meshIneBits.config.CraftConfig;

public class AreaTool {

  /**
   * Returns the barycenter of an area
   *
   * @param area target¶
   * @return Barycenter. <tt>null</tt> if area is empty
   */


  static Vector2 compute2DPolygonCentroid(Area area) {

    Vector<Segment2D> segments = getLargestPolygon(area);
    if (segments == null) {
      return null;
    }

    Vector<Vector2> vertices = new Vector<>();
    for (Segment2D s : segments) {
      vertices.add(s.start);
      vertices.addElement(s.end);
    }

    double centroidX = 0;
    double centroidY = 0;
    double signedArea = 0.0;
    double x0; // Current vertex X
    double y0; // Current vertex Y
    double x1; // Next vertex X
    double y1; // Next vertex Y
    double a; // Partial signed area
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
   * @param poly target to extract
   * @return the surface constraint in the given polygon. Empty if polygon is null
   */
  public static Area getAreaFrom(Polygon poly) {
    if (poly == null) {
      return new Area();
    } else {
      return new Area(poly.toPath2D());
    }
  }

  /**
   * @param shape target to extract
   * @return the surface defined by the shape
   */
  public static Area getAreaFrom(Shape2D shape) {
    Area resultArea = new Area();
    Vector<Area> areas = getLevel0AreasFrom(shape);
    for (Area a : Objects.requireNonNull(areas)) {
      resultArea.add(a);
    }
    return resultArea;
  }

  /**
   * Returns the outside boundary of an area (an area can have more than one boundary when there is
   * a/some hole(s) in it)
   *
   * @param area target to extract
   * @return outside boundaries. <tt>null</tt> if area is empty
   */
  public static Vector<Segment2D> getLargestPolygon(Area area) {
    Vector<Vector<Segment2D>> segments = AreaTool.getSegmentsFrom(area);
    if (segments.isEmpty()) {
      return null;
    }
    Vector<Double> boundLength = new Vector<>();
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
   * @param shape target to extract
   * @return All the constraint areas that do not contain any others
   */
  public static Vector<Area> getLevel0AreasFrom(Shape2D shape) {
    Vector<Area> areas = new Vector<>();
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
   * Get separated areas from a {@link Shape2D}.
   *
   * @param shape supposedly contains non self-intersected and non inter-intersected polygons
   * @return list of continuous area. Empty if no area
   * @see #getContinuousSurfacesFrom(List)
   * @since 0.3
   */
  public static List<Area> getContinuousSurfacesFrom(Shape2D shape) {
    return getContinuousSurfacesFrom(shape.polygons);
  }

  /**
   * @param areas targets
   * @return All the constraint areas that do not contain any others
   * @see #getContinuousSurfacesFrom(Shape2D)
   */
  private static Vector<Area> getLevel0AreasFrom(Vector<Area> areas) {

    if (areas.isEmpty()) {
      return null;
    }

    Vector<Vector<Area>> areasByLevel = new Vector<>();
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
            areasByLevel.set(i, new Vector<>());
          }
        }
      }
      areasByLevel.get(levelCurrentArea)
          .add(currentArea);
      if (levelCurrentArea > levelMax) {
        levelMax = levelCurrentArea;
      }
    }

//		for (Area level0Area : areasByLevel.get(0)) {
//			for (int level = 1; level <= levelMax; level++) {
//				for (Area higherLevelArea : areasByLevel.get(level)) {
//					if ((level % 2) != 0) {
//						level0Area.subtract(higherLevelArea);
//					} else {
//						level0Area.add(higherLevelArea);
//					}
//				}
//			}
//		}

    Vector<Area> surfaces = new Vector<>();

    for (int i = 0; i <= levelMax; i = i + 2) {
      if (i + 1 > levelMax || areasByLevel.get(i + 1)
          .isEmpty()) {
        surfaces.addAll(areasByLevel.get(i));
        continue;
      }
      Area hole = new Area();
      areasByLevel.get(i + 1)
          .forEach(hole::add);
      // Reconstruct even level area
      areasByLevel.get(i)
          .forEach(area -> {
            area.subtract(hole);
            if (!area.isEmpty()) {
              surfaces.add(area);
            }
          });
    }

//		return areasByLevel.get(0);
    return surfaces;
  }

  /**
   * Get separated areas extract from border-defining polygons. Use approx check of polygon
   *
   * @param polygons border of areas. Not self-intersected and non inter-intersected.
   * @return list of continuous area. Empty if no polygons
   * @see CraftConfig#errorAccepted
   * @since 0.3
   */
  private static List<Area> getContinuousSurfacesFrom(List<Polygon> polygons) {
    Map<Polygon, Integer> ranking = new HashMap<>();
    for (Polygon newPolygon : polygons) {
      List<Polygon> containingPolygons = new Vector<>();
      List<Polygon> containedPolygons = new Vector<>();
      for (Polygon polygon : ranking.keySet()) {
        if (polygon.approximatelyContains(newPolygon)) {
          // Check the keys containing the new polygon
          containingPolygons.add(polygon);
        } else if (newPolygon.approximatelyContains(polygon)) {
          // Check if it contains old polygons
          containedPolygons.add(polygon);
        }
      }
      // Calculate the level of new polygon
      ranking.put(newPolygon, 0);
      containingPolygons.stream()
          .mapToInt(ranking::get)
          .max()
          .ifPresent(l -> ranking.put(newPolygon, l + 1));
      // Promote level of contained polygons
      containedPolygons.forEach((Polygon p)
          -> ranking.put(p, ranking.get(p) + 1));
    }
    // Regroup into level
    List<List<Polygon>> classing = new Vector<>();
    polygons.forEach(polygon -> classing.add(new Vector<>()));
    ranking.keySet()
        .forEach((Polygon p) -> {
          int r = ranking.get(p);
          if (classing.get(r) == null) {
            classing.set(r, new Vector<>());
          } else {
            classing.get(r)
                .add(p);
          }
        });
    // Calculate areas
    // Even level polygons will be the outer border,
    // minus the holes which are the next odd level polygons
    List<Area> surfaces = new Vector<>();
    for (int i = 0; i < classing.size(); i++) {
      assert classing.get(i) != null;
      if (i % 2 != 0) {
        continue;
      }
      // Prepare the big hole
      Area hole = new Area();
      if (i + 1 < classing.size()) {
        classing.get(i + 1)
            .forEach((Polygon p)
                -> hole.add(AreaTool.getAreaFrom(p)));
      }
      // Dig the polygon in even level
      for (Polygon p : classing.get(i)) {
        Area newSurface = AreaTool.getAreaFrom(p);
        newSurface.subtract(hole);
        if (!newSurface.isEmpty()) {
          surfaces.add(newSurface);
        }
      }
    }
    return surfaces;
  }

  /**
   * Dissect an area into single continuous areas
   *
   * @param area of bit or of slice
   * @return list of areas not in union with others
   * @see #getContinuousSurfacesFrom(List)
   * @since 0.3
   */
  public static List<Area> getContinuousSurfacesFrom(Area area) {
    return getContinuousSurfacesFrom(getPolygonsFrom(area));
  }

  /**
   * Reconstruct border of area into polygons
   *
   * @param area target
   * @return boundary polygons
   */
  public static List<Polygon> getPolygonsFrom(Area area) {
    return getSegmentsFrom(area).stream()
        .map(Polygon::extractFrom)
        .filter(Objects::nonNull)
        .filter(Polygon::isNotNegligible)
        .collect(Collectors.toList());
  }

  /**
   * Returns the best point to take that bit. By best we mean the point the closest to the
   * barycenter of the bit and presenting enough material around for the sucker cup to work
   * properly. It returns null if this bit cannot be lifted.
   *
   * @param area      surface of bit
   * @param minRadius half of {@link CraftConfig#suckerDiameter}
   * @return liftPoint. <tt>null</tt> if area is empty
   */
  public static Vector2 getLiftPoint(Area area, double minRadius) {

    // We check if the barycenter would be ok
    Vector2 barycenter = AreaTool.compute2DPolygonCentroid(area);
    if (barycenter == null) {
      return null;
    }
    Vector<Vector<Segment2D>> segments = AreaTool.getSegmentsFrom(area);
    if (area.contains(barycenter.x, barycenter.y)) {
      // To be sure every other
      // distances will be smaller
      double minDist = CraftConfig.lengthFull * 2;
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
    Vector<Vector2> points = new Vector<>();
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
    Vector<Double> distances = new Vector<>();
    Vector<Vector2> sortedPoints = new Vector<>();
    distances.add(
        Math.sqrt(Vector2.dist2(new Vector2(points.get(0).x, points.get(0).y), barycenter)));
    sortedPoints.add(points.get(0));
    for (int j = 1; j < points.size(); j++) {
      double distance = Math.sqrt(
          Vector2.dist2(new Vector2(points.get(j).x, points.get(j).y), barycenter));
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
      double minDistFromBounds = CraftConfig.lengthFull * 2;
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
   * It converts the outline of an area into a vector of segment2D. Taken from
   * <a href= "http://stackoverflow.com/questions/8144156/using-pathiterator-to-return-all-line-segments-that-constrain-an-area">this
   * link</a>.
   *
   * @param area target to extract
   * @return segmentation of border
   */
  public static Vector<Vector<Segment2D>> getSegmentsFrom(Area area) {
    Vector<double[]> areaPoints = new Vector<>();

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

    Vector<Vector<Segment2D>> polygons = new Vector<>(polygonCount);

    for (int i = 0; i < polygonCount; i++) {
      polygons.add(new Vector<>());
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
        if (!polygons.get(currentPolygonIndex)
            .isEmpty()) {
          currentPolygonIndex++;
          if (currentPolygonIndex >= polygonCount) {
            currentPolygonIndex = 0;
          }
        }
      }

      if (nextElement[0] == PathIterator.SEG_LINETO) {
        polygons.get(currentPolygonIndex)
            .insertElementAt(new Segment2D(
                new Vector2(nextElement[1], nextElement[2]),
                new Vector2(currentElement[1], currentElement[2])

            ), 0);
      } else if (nextElement[0] == PathIterator.SEG_CLOSE) {
        polygons.get(currentPolygonIndex)
            .insertElementAt(
                new Segment2D(new Vector2(start[1], start[2]),
                    new Vector2(currentElement[1], currentElement[2])

                ), 0);
      }
    }

    // Clean the result by removing segments which have the same start and
    // end (java.awt.geom.Area.intersect issue)
    Iterator<Vector<Segment2D>> itrPolygons = polygons.iterator();
    while (itrPolygons.hasNext()) {
      Vector<Segment2D> polygon = itrPolygons.next();
      polygon.removeIf(s -> s.start.asGoodAsEqual(s.end));
      if (polygon.isEmpty()) {
        itrPolygons.remove();
      }
    }

    // areaSegments now contains all the line segments
    return polygons;
  }

  /**
   * @param area target to extract
   * @return the separated surfaces
   */
  public static Vector<Area> segregateArea(Area area) {

    Vector<Vector<Segment2D>> polygons = AreaTool.getSegmentsFrom(area);
    Vector<Area> segregatedAreas = new Vector<>();

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
      // Remove area if too tiny
      if (Rounder.round(path2D.getBounds2D()
          .getHeight(), CraftConfig.errorAccepted) > 0
          && Rounder.round(path2D.getBounds2D()
          .getWidth(), CraftConfig.errorAccepted) > 0) {
        segregatedAreas.add(new Area(path2D));
      }
    }
    return AreaTool.getLevel0AreasFrom(segregatedAreas);
  }

  /**
   * Expand <tt>area</tt> by a certain width
   *
   * @param area  target. Should not be <tt>null</tt> or empty
   * @param width in mm. Positive to enlarge, negative to shrink
   * @return <tt>null</tt> if <tt>area</tt> is <tt>null</tt> or empty
   */
  public static Area expand(Area area, Double width) {
    if (area == null || area.isEmpty()) {
      return new Area();
    }
    if (width == 0) {
      return area;
    }

    // Each polygon is a set of unordered segments
    List<Polygon> inflatedPolygons = getPolygonsFrom(area).stream()
        .map(p -> expand(p, area, width))
        .collect(Collectors.toList());
    Area finalArea = new Area();
    getContinuousSurfacesFrom(inflatedPolygons).forEach(finalArea::add);
    return finalArea;
  }

  /**
   * Expand / Shrink polygon given its area. For fast computing, we only check approximately. So if
   * an edge is too close to another ( &lt; 2 * 10^ {@link CraftConfig#errorAccepted
   * -errorAccepted}), some unpredictable behaviors will occur.
   *
   * @param polygon boundary of area
   * @param area    to determine interior
   * @param width   in mm. Positive to expand, negative to shrink
   * @return transformed polygon. <tt>null</tt> if invalid border
   */
  public static Polygon expand(Polygon polygon, Area area, Double width) {
    Iterator<Segment2D> pi = polygon.iterator();
    // Init
    Segment2D first = pi.next(),
        firstTranslated = translateOutward(first, area, width),
        currentTranslatedSegment = firstTranslated;
    // Continue
    while (pi.hasNext()) {
      Segment2D segment2D = pi.next(),
          newTranslatedSegment = translateOutward(segment2D, area, width);
      // Concat to last segment
      // By extending to the intersection point between last segment and this
      Vector2 intersection = currentTranslatedSegment.intersect(newTranslatedSegment);
      if (intersection == null)
      // Some kind of irregularity
      {
        return null;
      } else {
        currentTranslatedSegment.end = intersection;
        newTranslatedSegment.start = intersection;
        currentTranslatedSegment.setNext(newTranslatedSegment);
        currentTranslatedSegment = newTranslatedSegment;
      }
    }
    // Close polygon
    Vector2 intersection = currentTranslatedSegment.intersect(firstTranslated);
    if (intersection == null) {
      return null;
    } else {
      currentTranslatedSegment.end = intersection;
      firstTranslated.start = intersection;
      currentTranslatedSegment.setNext(firstTranslated);
    }
    return new Polygon(firstTranslated);
  }

  private static Segment2D translateOutward(Segment2D segment, Area area, Double width) {
    // Calculate perpendicular normal vector
    Vector2 n = segment.getNormal();
    // Choose right direction
    double length = 2 * Math.pow(10, -CraftConfig.errorAccepted);
    Vector2 translatedMidpoint = segment.getMidPoint()
        .add(n.mul(length));
    double directionalCoefficient;
    if (width > 0) {
      // To expand
      if (!area.contains(translatedMidpoint.x, translatedMidpoint.y))
      // If area does not contain the translated midpoint
      {
        directionalCoefficient = 1;
      } else {
        directionalCoefficient = -1;
      }
    } else {
      // To shrink
      if (area.contains(translatedMidpoint.x, translatedMidpoint.y))
      // If area contains the translated midpoint
      {
        directionalCoefficient = 1;
      } else {
        directionalCoefficient = -1;
      }
    }
    Vector2 distance = n.mul(directionalCoefficient * width);
    return new Segment2D(
        segment.start.add(distance),
        segment.end.add(distance)
    );
  }

  /**
   * Get two most distant {@link Vector2 point} from the {@link Area} provided. It is used in the
   * {@link #defineTwoPointNearTwoMostDistantPointsInAreaWithRadius(Area, double)}
   *
   * @param area The {@link Area} provided to find the two {@link Vector2 points}
   * @return {@link Vector list} of {@link Vector2 points}points founded
   */
  public static Vector<Vector2> getTwoMostDistantPointFromArea(Area area) {
    Vector<Vector2> positionTwoMostDistantPoint = new Vector<>();
    double longestDistance = 0;
    for (PathIterator p1 = area.getPathIterator(null); !p1.isDone(); p1.next()) {
      double[] coord1 = new double[6];
      int type1 = p1.currentSegment(coord1);
      Vector2 v1 = new Vector2(coord1[0], coord1[1]);
      if (type1 == PathIterator.SEG_CLOSE) {
        continue;
      }

      for (PathIterator p2 = area.getPathIterator(null); !p2.isDone(); p2.next()) {
        double[] coord2 = new double[6];
        int type2 = p2.currentSegment(coord2);
        if (type2 == PathIterator.SEG_CLOSE) {
          continue;
        }
        Vector2 v2 = new Vector2(coord2[0], coord2[1]);
        if (Vector2.dist(v1, v2) > longestDistance) {
          positionTwoMostDistantPoint.removeAllElements();
          positionTwoMostDistantPoint.add(v1);
          positionTwoMostDistantPoint.add(v2);
          longestDistance = Vector2.dist(v1, v2);
        }
      }
    }
    return positionTwoMostDistantPoint.size() > 0 ? positionTwoMostDistantPoint : null;
  }

  /**
   * this method hasn't been optimized yet
   *
   * @param area
   * @param radius
   * @return
   * @deprecated
   */
  @Deprecated
  public static Vector<Vector2> defineTwoPointNearTwoMostDistantPointsInAreaWithRadiusOtherWay(
      Area area, double radius) {
    Vector<Vector2> twoMostDistantPoint = getTwoMostDistantPointFromArea(area);
    if (twoMostDistantPoint == null) {
      return null;
    }
    Vector<Vector2> twoPointNearestTwoPointMostDistant = new Vector<>();
    for (Vector2 point : twoMostDistantPoint) {
      Vector2 pointFound = detectPointNearestPointProvidedInAreaWithRadius(area, point, radius);
      if (pointFound != null) {
        twoPointNearestTwoPointMostDistant.add(pointFound);
      }
    }
    if (twoPointNearestTwoPointMostDistant.size() == 2) {
      return twoPointNearestTwoPointMostDistant;
    }
    return null;
  }

  /**
   * This method allows us to determine the two closest points {@link Vector2} to the two furthest
   * points in the provided {@link Area} , which are located on the border of the {@link Area}
   * <br></br>
   * TODO: <b>Optimized the algorithm</b>
   *
   * @param area   The {@link Area} provided to find the two {@link Vector2 points}
   * @param radius the value of the detection interval of two {@link Vector2 point} from the two
   *               most distant {@link Vector2 point}. ex: [radius,radius*3]
   * @return {@link Vector list} of {@link Vector2 points}points founded
   */
  public static Vector<Vector2> defineTwoPointNearTwoMostDistantPointsInAreaWithRadius(Area area,
      double radius) {
    Vector<Vector2> twoDistantPoints = new Vector<>();
    Vector<Vector2> positionTwoMostDistantPoint = getTwoMostDistantPointFromArea(area);
    if (positionTwoMostDistantPoint == null) {
      return twoDistantPoints;
    }
    Vector2 point1 = positionTwoMostDistantPoint.firstElement();
    Vector2 point2 = positionTwoMostDistantPoint.lastElement();
    Rectangle2D rectangle2D = area.getBounds2D();
    double startX = rectangle2D.getMinX();
    double startY = rectangle2D.getMinY();
    double endX = rectangle2D.getMaxX();
    double endY = rectangle2D.getMaxY();
    boolean foundFirstPoint = false;
    boolean foundSecondPoint = false;
    Vector2 pointResult1 = null;
    Vector2 pointResult2 = null;
    double distant1;
    double distant2;

    //Find a point near first point of two most distant points
    while (radius < 20 && !(foundFirstPoint && foundSecondPoint)) {
      for (double x = startX; x < endX; x += 1) {
        for (double y = startY; y < endY; y += 1) {
          if (area.contains(x, y)
              && !(x == CraftConfig.lengthFull / 2 || x == -CraftConfig.lengthFull / 2)
              && !(y == CraftConfig.bitWidth / 2 || y == -CraftConfig.bitWidth / 2)) {
            if (!foundFirstPoint) {
              distant1 = Math.sqrt(
                  ((x - point1.x) * (x - point1.x)) + ((y - point1.y) * (y - point1.y)));
              if (pointResult2 == null || (x != pointResult2.x && y != pointResult2.y)) {
                if (distant1 > radius && distant1 <= radius * 3) {
                  if (checkPointInsideAreaWithRadius(x, y, area, CraftConfig.suckerDiameter / 4)) {
                    pointResult1 = new Vector2(x, y);
                    foundFirstPoint = true;
                  }
                }
              }
            }
            if (!foundSecondPoint) {
              distant2 = Math.sqrt(
                  ((x - point2.x) * (x - point2.x)) + ((y - point2.y) * (y - point2.y)));
              if (pointResult1 == null || (x != pointResult1.x && y != pointResult1.y)) {
                if (distant2 > radius && distant2 <= radius * 3) {
                  if (checkPointInsideAreaWithRadius(x, y, area, CraftConfig.suckerDiameter / 4)) {
                    pointResult2 = new Vector2(x, y);
                    foundSecondPoint = true;
                  }
                }
              }
            }
          }
          if (foundFirstPoint && foundSecondPoint) {
            break;
          }
        }
        if (foundFirstPoint && foundSecondPoint) {
          break;
        }
      }
      if (pointResult1 != null && pointResult2 != null) {
        twoDistantPoints.add(pointResult1);
        twoDistantPoints.add(pointResult2);
        return twoDistantPoints;
      }
      radius++;
    }
    return twoDistantPoints;
  }

  public static Vector<double[]> getListPointBorderOfArea(Area area) {
    Vector<double[]> listPoints = new Vector<>();
    for (PathIterator p2 = area.getPathIterator(null); !p2.isDone(); p2.next()) {
      double[] coord2 = new double[6];
      double[] point;
      int type2 = p2.currentSegment(coord2);
      if (type2 == PathIterator.SEG_CLOSE) {
        continue;
      }
      point = new double[]{type2, coord2[0], coord2[1]};
      listPoints.add(point);
    }
    return listPoints;
  }

  /**
   * This method determines if the {@link Vector2 point} in parameter is contained in the {@link
   * Area} and its position respects the distances with the edge of {@link Area} whose value must be
   * less than {@link Double radius}
   *
   * @param x      coordinate X of the point
   * @param y      coordinate X of the point
   * @param area   The {@link Area} provided to check
   * @param radius The {@link Double value} indicates that the two {@link Vector2 value} found must
   *               ensure a distance, equal to this {@link Double value}, to the edge of {@link
   *               Area}
   * @return
   */
  public static boolean checkPointInsideAreaWithRadius(double x, double y, Area area,
      double radius) {
    Vector<Vector<Segment2D>> segments = AreaTool.getSegmentsFrom(area);
    Vector2 point = new Vector2(x, y);
    //check if the area contain point and the distant of the point inside with the segment is always smaller the radius
    if (area.contains(point.x, point.y)) {
      for (Vector<Segment2D> polygon : segments) {
        for (Segment2D segment2D : polygon) {
          if (radius > 0 && segment2D.distFromPoint(point) < radius) {
            return false;
          }
        }
      }
    } else {
      return false;
    }

    return true;
  }

  public static Vector2 detectPointNearestPointProvidedInAreaWithRadius(Area area,
      Vector2 pointProvided, double radius) {
    //get rectangle enclosed completely this area
    Rectangle2D rectangle2D = area.getBounds2D();
    double startX = rectangle2D.getMinX();
    double startY = rectangle2D.getMinY();
    double endX = rectangle2D.getMaxX();
    double endY = rectangle2D.getMaxY();
    double distantMin = Math.sqrt(CraftConfig.lengthFull * CraftConfig.lengthFull
        + CraftConfig.bitWidth * CraftConfig.bitWidth);
    double distant;
    Vector2 pointResult = null;
    for (double x = startX; x < endX; x += 0.1) {
      for (double y = startY; y < endY; y += 0.1) {
        if (area.contains(x, y)) {
          Vector2 pointSearch = new Vector2(x, y);
          distant = Vector2.dist(pointProvided, pointSearch);
          if (distant < distantMin
              && distant > radius
              && distant <= radius * 3
              && checkPointInsideAreaWithRadius(pointSearch.x, pointSearch.y, area,
              CraftConfig.suckerDiameter / 4)) {
            pointResult = pointSearch;
          }
        }
      }
    }
    return pointResult;
  }


}
