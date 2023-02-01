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

package meshIneBits.gui.view3d.oldversion;

import java.util.Vector;

/**
 * @author Nicolas
 */
public class PolygonPointsList {

  private Vector<int[]> points;
  private Vector<double[]> pointsforArea;
  private int curPosition = -1;

  /**
   * @param points to be inserted
   * @throws Exception when not enough points to build polygon
   */

  public PolygonPointsList(Vector<int[]> points) throws Exception {
    if (points.size() < 3) {
      throw new Exception("Not enough points to build a polygon !");
    }
    this.points = points;
  }
  public PolygonPointsList(Vector<double[]> points,boolean Area) throws Exception {
    if (points.size() < 3) {
      throw new Exception("Not enough points to build a polygon !");
    }
    this.pointsforArea = points;
  }
  /**
   * @return the next point in the list
   */
  public int[] getNextPoint() {
    if (curPosition < points.size() - 1) {
      curPosition++;
    } else {
      curPosition = 0;
    }
    return points.get(curPosition);
  }

  public double[] getNextPointforArea() {
    if (curPosition < pointsforArea.size() - 1) {
      curPosition++;
    } else {
      curPosition = 0;
    }
    return pointsforArea.get(curPosition);
  }

  /**
   * @return Number of points in the list
   */
  public int getLengthForArea() {
    return pointsforArea.size();
  }
  public int getLength() {
    return points.size();
  }
}
