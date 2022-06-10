package meshIneBits.util;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;
import meshIneBits.config.CraftConfig;

public class LiftPointCalc {

  public static final LiftPointCalc instance = new LiftPointCalc();

  public Vector2 getLiftPoint(Area area, double minRadius) {

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
}
