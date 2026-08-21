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

package meshIneBits.gui.view3d.view;

import meshIneBits.util.Vector3;
import processing.core.PShape;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.InteractiveFrame;
import remixlab.proscene.Scene;

public class CustomInteractiveFrame extends InteractiveFrame {

  private PShape shape;

  @SuppressWarnings("unused")
  public CustomInteractiveFrame(Scene scene, PShape pShape) {
    super(scene, pShape);
    this.shape = pShape;
  }

  public CustomInteractiveFrame(Scene scene) {
    super(scene);
  }

  public Vector3 getMinShapeInFrameCoordinate() {
    double minx = Double.MAX_VALUE;
    double miny = Double.MAX_VALUE;
    double minz = Double.MAX_VALUE;
    int size = shape.getChildCount();
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < 3; j++) {
        Vec vertex = new Vec(shape.getChild(i).getVertex(j).x
            , shape.getChild(i).getVertex(j).y
            , shape.getChild(i).getVertex(j).z);
        Vec v = this.inverseCoordinatesOf(vertex);
        if (minx > v.x()) {
          minx = v.x();
        }
        if (miny > v.y()) {
          miny = v.y();
        }
        if (minz > v.z()) {
          minz = v.z();
        }
      }
    }
    return new Vector3(minx, miny, minz);
  }

  @Override
  public void setShape(PShape pShape) {
    super.setShape(pShape);
    this.shape=pShape;
  }

  public Vector3 getMaxShapeInFrameCoordinate() {
    double maxX = -Double.MIN_VALUE;
    double maxY = -Double.MIN_VALUE;
    double maxZ = -Double.MIN_VALUE;
    int size = shape.getChildCount();
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < 3; j++) {
        Vec vertex = new Vec(shape.getChild(i)
            .getVertex(j).x, shape.getChild(i)
            .getVertex(j).y,
            shape.getChild(i)
                .getVertex(j).z);
        Vec v = this.inverseCoordinatesOf(vertex);
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
