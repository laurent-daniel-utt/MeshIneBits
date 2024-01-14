/*
 * MeshIneBits is a Java software to disintegrate a 3d project (model in .stl)
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

import java.io.Serializable;

public class AABBrect implements Serializable {

  private Vector2 lowerBound, upperBound;

  @SuppressWarnings({"rawtypes"})
  public AABBTree.TreeNode node;

  public AABBrect(AABBrect aabb) {
    lowerBound = aabb.lowerBound;
    upperBound = aabb.upperBound;
  }

  public AABBrect(Vector2 p1, Vector2 p2) {
    lowerBound = p1;
    upperBound = p2;
  }

  AABBrect(Vector2 p1, Vector2 p2, double extend) {
    lowerBound = new Vector2(Math.min(p1.x, p2.x) - extend, Math.min(p1.y, p2.y) - extend);
    upperBound = new Vector2(Math.max(p1.x, p2.x) + extend, Math.max(p1.y, p2.y) + extend);
  }

  public void addAABB(AABBrect a) {
    if (node != null) {
      throw new UnsupportedOperationException("addAABB on AABBrect while in a AABBTree");
    }
    lowerBound = new Vector2(Math.min(lowerBound.x, a.lowerBound.x),
        Math.min(lowerBound.y, a.lowerBound.y));
    upperBound = new Vector2(Math.max(upperBound.x, a.upperBound.x),
        Math.max(upperBound.y, a.upperBound.y));
  }

  public AABBrect combine(AABBrect e) {
    return new AABBrect(
        new Vector2(Math.min(lowerBound.x, e.lowerBound.x), Math.min(lowerBound.y, e.lowerBound.y)),
        new Vector2(Math.max(upperBound.x, e.upperBound.x),
            Math.max(upperBound.y, e.upperBound.y)));
  }

  public double getAABBDist(AABBrect t) {
    return upperBound.add(lowerBound)
        .div(2)
        .sub(t.upperBound.add(t.lowerBound)
            .div(2))
        .vSize();
  }

  public double getPerimeter() {
    double w = upperBound.x - lowerBound.x;
    double h = upperBound.y - lowerBound.y;
    return (w + h) * 2.0;
  }

  public boolean overlap(AABBrect t) {
    if (((t.lowerBound.x - upperBound.x) > 0.0f) || ((t.lowerBound.y - upperBound.y) > 0.0f)) {
      return false;
    }

    if (((lowerBound.x - t.upperBound.x) > 0.0f) || ((lowerBound.y - t.upperBound.y) > 0.0f)) {
      return false;
    }

    return true;
  }

  void updateAABB(Vector2 p1, Vector2 p2, double extend) {
    if (node != null) {
      throw new UnsupportedOperationException("Update on AABBrect while in a AABBTree");
    }
    lowerBound = new Vector2(Math.min(p1.x, p2.x) - extend, Math.min(p1.y, p2.y) - extend);
    upperBound = new Vector2(Math.max(p1.x, p2.x) + extend, Math.max(p1.y, p2.y) + extend);
  }
}
