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
