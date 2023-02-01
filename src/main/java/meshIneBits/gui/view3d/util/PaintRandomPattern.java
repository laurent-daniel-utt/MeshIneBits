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
