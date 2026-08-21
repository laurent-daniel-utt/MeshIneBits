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

package meshIneBits.gui.view3d.builder;

import java.util.Vector;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

public class BitShape {

  private final PShape shape;
  private final Vector<SubBitShape> subBitShapes = new Vector<>();
  private Integer layerId;

  public BitShape(PShape shapeBit) {
    this.shape = shapeBit;
  }

  public static BitShape create(PApplet context) {
    return new BitShape(context.createShape(PConstants.GROUP));
  }

  public void addChild(PShape shape) {
    this.shape.addChild(shape);
  }

  @SuppressWarnings("unused")
  public void addSubBit(SubBitShape subBitShape) {
    this.shape.addChild(subBitShape.getShape());
    if (!subBitShapes.contains(subBitShape)) {
      subBitShapes.add(subBitShape);
    }
  }

  public PShape getShape() {
    return shape;
  }

  @SuppressWarnings("unused")
  public Vector<SubBitShape> getSubBitShapes() {
    return subBitShapes;
  }

  public int getLayerId() {
    return layerId == null ? -1 : layerId;
  }

  public BitShape setLayerId(int layerId) {
    this.layerId = layerId;
    return this;
  }

  public void translate(float x, float y, float lowerAltitude) {
//    shape.translate(x,y,lowerAltitude);
    subBitShapes.forEach(subBitShape -> subBitShape.getShape().translate(x,y,lowerAltitude));
  }

  public void rotateZ(float radians) {
    subBitShapes.forEach(subBitShape -> subBitShape.getShape().rotate(radians));
  }
}
