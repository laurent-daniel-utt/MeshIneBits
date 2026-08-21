/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2015-2026 DANIEL Laurent.
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
 -  Copyright (C) 2026 Hugo BENOIT
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

package meshIneBits.gui.view3d.util;

import meshIneBits.util.Vector3;
import processing.core.PShape;
import remixlab.dandelion.geom.Vec;

public class PShapeUtil {

  public static final PShapeUtil instance = new PShapeUtil();

  public static PShapeUtil getInstance() {
    return instance;
  }

  public Vector3 getMinShapeInFrameCoordinate(PShape shape) {
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double minZ = Double.MAX_VALUE;

    int size = shape.getChildCount();
    for (int i = 0; i < size; i++) {
      for(int k = 0; k < shape.getChild(i).getChildCount();k++){
        for (int j = 0; j < shape.getChild(i).getChild(k).getVertexCount(); j++) {
          Vec vertex = new Vec(shape.getChild(i).getChild(k).getVertex(j).x
              , shape.getChild(i).getChild(k).getVertex(j).y
              , shape.getChild(i).getChild(k).getVertex(j).z);
          if (minX > vertex.x()) {
            minX = vertex.x();
          }
          if (minY > vertex.y()) {
            minY = vertex.y();
          }
          if (minZ > vertex.z()) {
            minZ = vertex.z();
          }
        }
      }
    }
    return new Vector3(minX, minY, minZ);
  }

  public Vector3 getMaxShapeInFrameCoordinate(PShape shape) {
    double maxX = -Double.MIN_VALUE;
    double maxY = -Double.MIN_VALUE;
    double maxZ = -Double.MIN_VALUE;
    int size = shape.getChildCount();
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < 3; j++) {
        Vec v = new Vec(shape.getChild(i)
            .getVertex(j).x, shape.getChild(i)
            .getVertex(j).y,
            shape.getChild(i)
                .getVertex(j).z);
        if (maxX < v.x()) {
          maxX = v.x();
        }
        if (maxY < v.y()) {
          maxY = v.y();
        }
        if (maxZ < v.z()) {
          maxZ = v.z();
        }
      }
    }
    return new Vector3(maxX, maxY, maxZ);
  }

}
