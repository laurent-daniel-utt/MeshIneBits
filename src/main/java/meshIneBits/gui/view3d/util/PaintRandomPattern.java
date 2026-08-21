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

import meshIneBits.gui.view3d.builder.BitShape;
import meshIneBits.gui.view3d.builder.PavedMeshBuilderResult;
import meshIneBits.gui.view3d.builder.SubBitShape;
import meshIneBits.gui.view3d.util.animation.AnimationProcessor.AnimationOption;

import java.awt.*;
import java.util.Random;
import java.util.Vector;

@SuppressWarnings("unused")
public class PaintRandomPattern implements IPaintShapePattern {

  @Override
  public void paintAnimation(PavedMeshBuilderResult pavedMesh, AnimationOption animationOption) {
    Vector<BitShape> bitShapes = pavedMesh.getBitShapes();
    if (bitShapes == null || bitShapes.size() == 0) {
      return;
    }
    switch (animationOption) {
      case BY_LAYER:
        int currentLayer = bitShapes.get(0).getLayerId();
        for (BitShape bitShape : bitShapes) {
          if (bitShape.getLayerId() == currentLayer) {
            bitShape.getShape().setFill(ColorRandomUtil.instance.getCurrentColor().getRGB());
          } else {
            currentLayer = bitShape.getLayerId();
            bitShape.getShape().setFill(ColorRandomUtil.instance.generateNewColor().getRGB());
          }
        }
        break;
      case BY_BATCH:
        int currentBatch = bitShapes.get(0).getSubBitShapes().get(0).getBatchId();
        for (BitShape bitShape : bitShapes) {
          for(SubBitShape subBitShape : bitShape.getSubBitShapes())
          if (subBitShape.getBatchId() == currentBatch) {
            subBitShape.getShape().setFill(ColorRandomUtil.instance.getCurrentColor().getRGB());
          } else {
            currentBatch = subBitShape.getBatchId();
            subBitShape.getShape().setFill(ColorRandomUtil.instance.generateNewColor().getRGB());
          }
        }
        break;
      case BY_BIT:
        for (BitShape bitShape : bitShapes) {
          bitShape.getShape().setFill(ColorRandomUtil.instance.generateNewColor().getRGB());
        }
        break;
    }
  }

  public static class ColorRandomUtil {

    public static final ColorRandomUtil instance = new ColorRandomUtil();

    private Color currentColor = generateNewColor();

    public Color generateNewColor() {
      Random rand = new Random();
      float r = rand.nextFloat();
      float g = rand.nextFloat();
      float b = rand.nextFloat();
      currentColor = new Color(r, g, b);
      return currentColor;
    }

    public Color getCurrentColor() {
      return currentColor;
    }
  }
}
