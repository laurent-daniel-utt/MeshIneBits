package meshIneBits.gui.view3d.util;

import java.awt.Color;
import java.util.Vector;
import meshIneBits.gui.view3d.animation.AnimationProcessor.AnimationOption;
import meshIneBits.gui.view3d.builder.BitShape;
import meshIneBits.gui.view3d.builder.PavedMeshBuilderResult;
import meshIneBits.gui.view3d.builder.SubBitShape;

public class AlternatingColorPaintPattern implements IPaintShapePattern {

  private final Color color1 = new Color(112, 66, 20);
  private final Color color2 = new Color(20, 66, 112);

  @Override
  public void paintAnimation(PavedMeshBuilderResult pavedMesh, AnimationOption animationOption) {
    Vector<BitShape> bitShapes = pavedMesh.getBitShapes();
    if (bitShapes == null || bitShapes.size() == 0) {
      return;
    }
    switch (animationOption) {
      case BY_LAYER:
        for (BitShape bitShape : bitShapes) {
          if (bitShape.getLayerId() % 2 == 0) {
            bitShape.getShape().setFill(color1.getRGB());
          } else {
            bitShape.getShape().setFill(color2.getRGB());
          }
        }
        break;
      case BY_BATCH:
        for (BitShape bitShape : bitShapes) {
          for (SubBitShape subBitShape : bitShape.getSubBitShapes()) {
            if (subBitShape.getBatchId() % 2 == 0) {
              subBitShape.getShape().setFill(color1.getRGB());
            } else {
              subBitShape.getShape().setFill(color2.getRGB());
            }
          }
        }
        break;
      case BY_BIT:
        for (BitShape bitShape : bitShapes) {
          bitShape.getShape().setFill(color1.getRGB());
        }
        break;
    }
  }

  @Override
  public void paintMesh(PavedMeshBuilderResult pavedMesh) {

  }
}
