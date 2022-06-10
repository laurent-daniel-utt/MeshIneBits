package meshIneBits.util;

import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.Vector;
import meshIneBits.config.CraftConfig;

public class TwoDistantPointsCalc {

  public final static TwoDistantPointsCalc instance = new TwoDistantPointsCalc();

  public Vector<Vector2> defineTwoPointNearTwoMostDistantPointsInAreaWithRadius(Area area,
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
    while (radius < 20) {
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

  public Vector<Vector2> getTwoMostDistantPointFromArea(Area area) {
    Vector<Vector2> positionTwoMostDistantPoint = new Vector<>();
    double longestDistance = 0;
    for (PathIterator p1 = area.getPathIterator(null); !p1.isDone(); p1.next()) {
      double[] coord1 = new double[6];
      int type1 = p1.currentSegment(coord1);
      if (type1 == PathIterator.SEG_CLOSE) {
        continue;
      }
      Vector2 v1 = new Vector2(coord1[0], coord1[1]);
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
    return positionTwoMostDistantPoint;
  }

  public boolean checkPointInsideAreaWithRadius(double x, double y, Area area,
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
}
