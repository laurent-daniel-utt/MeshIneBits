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

package meshIneBits.gui.view3d.builder;

import meshIneBits.Model;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.util.Triangle;
import meshIneBits.util.Vector3;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;

public class STLModelShapeBuilder extends PApplet implements IModelShapeBuilder {
public static CountDownLatch shaping=new CountDownLatch(1);
  public static STLModelShapeBuilder createInstance(PApplet pApplet, Model model) {
    return new STLModelShapeBuilder(pApplet, model);
  }

  private final PApplet context;
  private final Model stlModel;

  public STLModelShapeBuilder(PApplet context, Model stlModel) {
    this.context = context;
    this.stlModel = stlModel;
  }

  @Override
  public PShape buildModelShape() {
    PShape modelShape = context.createShape(PConstants.GROUP);

    Vector<Triangle> triangles = stlModel.getTriangles();
    for (Triangle triangle : triangles) {

      PShape shape = buildShapeFromTriangle(triangle);
      modelShape.addChild(shape);
    }
    return modelShape;
  }

  private PShape buildShapeFromTriangle(Triangle triangle) {

    PShape face = context.createShape();
    face.setFill(context.color(
        Visualization3DConfig.MODEL_COLOR.getRed(),
        Visualization3DConfig.MODEL_COLOR.getGreen(),
        Visualization3DConfig.MODEL_COLOR.getBlue()));
    face.beginShape();
    face.noStroke();

    for (Vector3 p : triangle.point) {
      face.vertex((float) p.x, (float) p.y, (float) p.z);
    }
    face.endShape(PConstants.CLOSE);
    return face;
  }
}
