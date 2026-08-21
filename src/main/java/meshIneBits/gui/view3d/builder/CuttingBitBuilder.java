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

import meshIneBits.NewBit2D;
import meshIneBits.NewBit3D;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view3d.Visualization3DConfig;
import processing.core.PApplet;
import processing.core.PShape;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Vector;

import static processing.core.PConstants.GROUP;

public class CuttingBitBuilder {

  private final PApplet context;

  public CuttingBitBuilder(PApplet context) {
    this.context = context;
  }

  public CuttingBitShape buildCuttingBitShape(NewBit3D bit3D) {
    NewBit2D bit2D = bit3D.getBaseBit();
    Vector<Path2D> cutPaths = bit2D.getCutPathsCB();
    PShape shape = context.createShape(GROUP);

    PShape shapeBit = context.createShape();

    shapeBit.beginShape();
    shapeBit.fill(Visualization3DConfig.CUTTING_BIT_COLOR.getRGB());
    shapeBit.vertex(0, 0, 0);
    shapeBit.vertex((float) CraftConfig.lengthFull, 0, 0);
    shapeBit.vertex((float) CraftConfig.lengthFull, (float) CraftConfig.bitWidth, 0);
    shapeBit.vertex(0, (float) CraftConfig.bitWidth, 0);
    shapeBit.vertex(0, 0, 0);
    shapeBit.endShape();
    boolean isFirst = true;

    PShape newCutPath = context.createShape();
    for (Path2D cutPath : cutPaths) {
      for (PathIterator pi = cutPath.getPathIterator(null); !pi.isDone(); pi.next()) {
        double[] coords = new double[2];

        int type = pi.currentSegment(coords);
        float x = (float) coords[0];
        float y = (float) coords[1];

        double widthBit = CraftConfig.lengthFull;
        double heightBit = CraftConfig.bitWidth;

        if (x > 0 && y > 0) {
          x += widthBit / 2;
          y += heightBit / 2;
        } else if (x > 0 && y < 0) {
          x += widthBit / 2;
          y = (float) heightBit / 2 - PApplet.abs(y);
        } else if (x < 0 && y > 0) {
          x = (float) widthBit / 2 - PApplet.abs(x);
          y += heightBit / 2;
        } else {
          x = (float) widthBit / 2 - PApplet.abs(x);
          y = (float) heightBit / 2 - PApplet.abs(y);
        }

        switch (type) {
          case PathIterator.SEG_MOVETO:
            if (!isFirst) {
              newCutPath.endShape();
              shape.addChild(newCutPath);
            } else {
              isFirst = false;
            }
            newCutPath = context.createShape();
            newCutPath.setStroke(context.color(Visualization3DConfig.CUT_PATH_COLOR.getRGB()));
            newCutPath.beginShape();
            newCutPath.vertex(x, y);
            break;
          case PathIterator.SEG_LINETO:
            newCutPath.vertex(x, y);
            break;
        }
      }
      newCutPath.endShape();
      shape.addChild(newCutPath);
    }
    shape.addChild(shapeBit);
//    shape.scale(4);
//    shape.translate(0.34f * context.width, 0.6f * context.height);
    return new CuttingBitShape(shape);
  }

}
