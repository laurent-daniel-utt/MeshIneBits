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

import java.io.Serializable;

/**
 * The triangle class represents a 3D triangle in a 3D model
 */
public class Triangle implements Serializable {

  public Vector3[] point = new Vector3[3];

  public Vector3 getNormal() {
    return point[1].sub(point[0])
        .cross(point[2].sub(point[0]))
        .normal();
  }

  public Segment2D project2D(double layerZ) {
    Segment2D ret = null;

    if ((point[0].z < layerZ) && (point[1].z >= layerZ) && (point[2].z >= layerZ)) {
      ret = setSegment(null, layerZ, point[0], point[2], point[1]);
    } else if ((point[0].z > layerZ) && (point[1].z <= layerZ) && (point[2].z <= layerZ)) {
      ret = setSegment(null, layerZ, point[0], point[1], point[2]);
    } else if ((point[1].z < layerZ) && (point[0].z >= layerZ) && (point[2].z >= layerZ)) {
      ret = setSegment(null, layerZ, point[1], point[0], point[2]);
    } else if ((point[1].z > layerZ) && (point[0].z <= layerZ) && (point[2].z <= layerZ)) {
      ret = setSegment(null, layerZ, point[1], point[2], point[0]);
    } else if ((point[2].z < layerZ) && (point[1].z >= layerZ) && (point[0].z >= layerZ)) {
      ret = setSegment(null, layerZ, point[2], point[1], point[0]);
    } else if ((point[2].z > layerZ) && (point[1].z <= layerZ) && (point[0].z <= layerZ)) {
      ret = setSegment(null, layerZ, point[2], point[0], point[1]);
    } else {
      // Logger.error("Cannot handle triangle:\n" + point[0] + "\n" + point[1] + "\n" +
      // point[2] + "\non Z: " + layerZ);
      return null;
    }
    if (Double.isNaN(ret.start.x) || Double.isNaN(ret.end.x)) {
      Logger.error(
          "Error on triangle:\n" + point[0] + "\n" + point[1] + "\n" + point[2] + "\non Z: "
              + layerZ);
    }

    return ret;
  }

  private Segment2D setSegment(Segment2D ret, double layerZ, Vector3 v0, Vector3 v1, Vector3 v2) {
    double a1 = (layerZ - v0.z) / (v1.z - v0.z);
    double a2 = (layerZ - v0.z) / (v2.z - v0.z);
    Vector2 start = new Vector2(v0.x + ((v1.x - v0.x) * a1), v0.y + ((v1.y - v0.y) * a1));
    Vector2 end = new Vector2(v0.x + ((v2.x - v0.x) * a2), v0.y + ((v2.y - v0.y) * a2));
    return new Segment2D(start, end);
  }
}