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

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Vector;
import meshIneBits.Bit2D;
import meshIneBits.config.CraftConfig;

public class CutPathCalc {

  public static final CutPathCalc instance = new CutPathCalc();
  public Vector<Path2D> calcCutPathFrom(Bit2D bit2D) {
    Vector<Path2D> cutPaths = new Vector<>();
    Vector<Vector<Segment2D>> polygons = AreaTool.getSegmentsFrom(bit2D.getAreaCB());
    // Define 4 corners
    Vector2 cornerUpRight   = new Vector2(CraftConfig.lengthFull / 2.0, -CraftConfig.bitWidth / 2.0);
    Vector2 cornerDownRight = new Vector2(cornerUpRight.x, cornerUpRight.y + bit2D.getWidth());
    Vector2 cornerUpLeft    = new Vector2(cornerUpRight.x - bit2D.getLength(), cornerUpRight.y);
    Vector2 cornerDownLeft  = new Vector2(cornerDownRight.x - bit2D.getLength(), cornerDownRight.y);
    // Define 4 sides
    Segment2D sideTop     = new Segment2D(cornerUpLeft, cornerUpRight);
    Segment2D sideBottom  = new Segment2D(cornerDownLeft, cornerDownRight);
    Segment2D sideRight   = new Segment2D(cornerUpRight, cornerDownRight);
    Segment2D sideLeft    = new Segment2D(cornerUpLeft, cornerDownLeft);

    // Check cut path
    // If and edge lives on sides of the bit
    // We remove it
    polygons.forEach(polygon -> polygon.removeIf(
        edge -> sideBottom.contains(edge)
            || sideLeft.contains(edge)
            || sideRight.contains(edge)
            || sideTop.contains(edge)));

    // After filter out the edges on sides
    // We form cut paths from these polygons
    // Each polygon may contain multiple cut paths
    for (Vector<Segment2D> polygon : polygons) {
      if (polygon.isEmpty()) {
        continue;
      }
      Path2D cutPath2D = new Path2D.Double();
      Vector<Path2D> cutPaths2D = new Vector<>();
      for (int i = 0; i < polygon.size(); i++) {
        Segment2D currentEdge = polygon.get(i);
        if (i == 0 || !(currentEdge.start.asGoodAsEqual(polygon.get(i - 1).end))) {
          cutPath2D.moveTo(currentEdge.start.x, currentEdge.start.y);
        }
        cutPath2D.lineTo(currentEdge.end.x, currentEdge.end.y);
        if ((currentEdge.end.isOnSegment(sideBottom) || currentEdge.end.isOnSegment(sideTop))
            && currentEdge.getNext() != null) {
          cutPath2D.moveTo(currentEdge.end.x, currentEdge.end.y);
        }
      }
      // Finish the last cut path
      cutPath2D = organizeOrderCutInPath2D(cutPath2D);
      cutPaths2D.add(cutPath2D);
      cutPaths.addAll(cutPaths2D);
    }
    sortCutPath(cutPaths);
    return cutPaths;
  }

  /**
   * This method is used to order the position of the {@link Path2D} in the {@link Vector list}
   *
   * @param cutPaths {@link Vector list} of cut paths
   */
  public void sortCutPath(Vector<Path2D> cutPaths) {
    cutPaths.sort((path1, path2) -> {
      Path2D path1Clone = (Path2D) path1.clone();
      Point2D currentPoint1Start = path1Clone.getCurrentPoint();
      path1Clone.closePath();
      Point2D currentPoint1End = path1Clone.getCurrentPoint();
      Path2D path2Clone = (Path2D) path2.clone();
      Point2D currentPoint2Start = path2Clone.getCurrentPoint();
      path2Clone.closePath();
      Point2D currentPoint2End = path2Clone.getCurrentPoint();
      if ((currentPoint1Start.getX() < currentPoint2Start.getX()
          || currentPoint1Start.getX() < currentPoint2End.getX())
          && (currentPoint1End.getX() < currentPoint2Start.getX()
          || currentPoint1End.getX() < currentPoint2End.getX())) {
        return -1;
      } else if ((currentPoint2Start.getX() < currentPoint1Start.getX()
          || currentPoint2Start.getX() < currentPoint1End.getX())
          && (currentPoint2End.getX() < currentPoint1Start.getX()
          || currentPoint2End.getX() < currentPoint1End.getX())) {
        return 1;
      } else {
        return 0;
      }

    });
  }

  public Path2D organizeOrderCutInPath2D(Path2D path2D) {
    PathIterator iterator = path2D.getPathIterator(null);
    Vector<Path2D> listCutPaths = new Vector<>();
    Path2D currentPath = new Path2D.Double();
    for (PathIterator pi = iterator; !pi.isDone(); pi.next()) {
      double[] coord = new double[2];
      int type = pi.currentSegment(coord);
      if (type == PathIterator.SEG_MOVETO) {
        if (currentPath.getCurrentPoint() != null) {

          currentPath = organizePath(currentPath);
          listCutPaths.add(currentPath);
        }

        currentPath = new Path2D.Double();
        currentPath.moveTo(coord[0], coord[1]);
      } else if (type == PathIterator.SEG_LINETO) {
        currentPath.lineTo(coord[0], coord[1]);
      }
    }
    if (!listCutPaths.contains(currentPath)) {
      currentPath = organizePath(currentPath);
      listCutPaths.add(currentPath);
    }
    sortCutPath(listCutPaths);
    currentPath = new Path2D.Double();

    for (int i = 0; i < listCutPaths.size(); i++) {
      if (i == 0) {
        currentPath.append(listCutPaths.get(0), false);
        continue;
      }
      Path2D pathPrevious = listCutPaths.get(i - 1);
      Path2D currentPathClone = (Path2D) listCutPaths.get(i)
          .clone();
      currentPathClone.closePath();
      if (pathPrevious.getCurrentPoint()
          .getX() == currentPathClone.getCurrentPoint()
          .getX()
          && pathPrevious.getCurrentPoint()
          .getY() == currentPathClone.getCurrentPoint()
          .getY()) {
        currentPath.append(listCutPaths.get(i), true);
      } else {
        currentPath.append(listCutPaths.get(i), false);
      }
    }
    return currentPath;

  }

  private Path2D organizePath(Path2D path) {
    int countMoveTo = 0;
    Point2D currentPoint = path.getCurrentPoint();
    Path2D result = new Path2D.Double();
    Vector<double[]> list = new Vector<>();
    for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) {
      double[] coord = new double[2];
      int type = pi.currentSegment(coord);
      if (type == PathIterator.SEG_MOVETO) {
        if (currentPoint.getX() > coord[0]) {
          return path;
        }
        countMoveTo++;
      }
      list.add(coord);
      if (countMoveTo > 1) {
        throw new IllegalArgumentException("path argument must have just one Move To");
      }
    }
    for (int i = list.size() - 1; i >= 0; i--) {
      if (i == list.size() - 1) {
        result.moveTo(list.get(i)[0], list.get(i)[1]);
      } else {
        result.lineTo(list.get(i)[0], list.get(i)[1]);
      }
    }
    return result;

  }

  public Path2D transformPath2D(Path2D path2D, AffineTransform affineTransform) {
    Path2D result = (Path2D) path2D.clone();
    result.transform(affineTransform);
    return organizeOrderCutInPath2D(result);
  }

}
