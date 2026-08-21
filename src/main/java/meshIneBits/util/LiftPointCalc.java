/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2016-2026 DANIEL Laurent.
 -  Copyright (C) 2015 Cédric Siourakhan
 -  Copyright (C) 2016 Gabriel Magny
 -  Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 -  Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 -  Copyright (C) 2018 VALLON Benjamin.
 -  Copyright (C) 2018 LORIMER Campbell.
 -  Copyright (C) 2018 D'AUTUME Christian.
 -  Copyright (C) 2019 DURINGER Nathan (Tests).
 -  Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
 -  Copyright (C) 2020-2021 DO Quang Bao.
 -  Copyright (C) 2021 VANNIYASINGAM Mithulan.
 -  Copyright (C) 2021  Quang Long
 -  Copyright (C) 2022 Mhamad Atlab
 -  Copyright (C) 2022 Majed Hlaihel
 -  Copyright (C) 2026 Aymeric Sirejol
 -
 -  This program is free software: you can redistribute it and/or modify
 -  it under the terms of the GNU General Public License as published by
 -  the Free Software Foundation, either version 3 of the License, or
 -  (at your option) any later version.
 -
 -  This program is distributed in the hope that it will be useful,
 -  but WITHOUT ANY WARRANTY; without even the implied warranty of
 -  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 -  GNU General Public License for more details.
 -
 -  You should have received a copy of the GNU General Public License
 -  along with this program. If not, see <https://www.gnu.org/licenses/>.
 -----------------------------------------------------------------------------*/

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
