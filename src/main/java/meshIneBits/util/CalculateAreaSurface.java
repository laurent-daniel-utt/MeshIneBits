/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2022 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
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
 *
 */

package meshIneBits.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

import static java.lang.Double.NaN;

public abstract class CalculateAreaSurface {

  public static double approxArea(Area area, double flatness, int limit) {
    PathIterator i =
        new FlatteningPathIterator(area.getPathIterator(identity),
            flatness,
            limit);
    return approxArea(i);
  }

  public static double approxArea(Area area, double flatness) {
    PathIterator i = area.getPathIterator(identity, flatness);
    return approxArea(i);
  }

  public static void main(String[] args) {
    Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, 10, 1000);
    System.out.println(approxArea(new Area(rectangle2D), 0));
  }

  public static double approxArea(PathIterator i) {
    double a = 0.0;
    double[] coords = new double[6];
    double startX = NaN, startY = NaN;
    Line2D segment = new Line2D.Double(NaN, NaN, NaN, NaN);
    while (!i.isDone()) {
      int segType = i.currentSegment(coords);
      double x = coords[0], y = coords[1];
      switch (segType) {
        case PathIterator.SEG_CLOSE:
          segment.setLine(segment.getX2(), segment.getY2(), startX, startY);
          a += hexArea(segment);
          startX = startY = NaN;
          segment.setLine(NaN, NaN, NaN, NaN);
          break;
        case PathIterator.SEG_LINETO:
          segment.setLine(segment.getX2(), segment.getY2(), x, y);
          a += hexArea(segment);
          break;
        case PathIterator.SEG_MOVETO:
          startX = x;
          startY = y;
          segment.setLine(NaN, NaN, x, y);
          break;
        default:
          throw new IllegalArgumentException("PathIterator contains curved segments");
      }
      i.next();
    }
    if (Double.isNaN(a)) {
      throw new IllegalArgumentException("PathIterator contains an open path");
    } else {
      return 0.5 * Math.abs(a);
    }
  }

  private static double hexArea(Line2D seg) {
    return seg.getX1() * seg.getY2() - seg.getX2() * seg.getY1();
  }

  private static final AffineTransform identity =
      AffineTransform.getQuadrantRotateInstance(0);
}
