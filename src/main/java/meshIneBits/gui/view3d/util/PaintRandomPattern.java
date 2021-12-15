package meshIneBits.gui.view3d.util;

import java.awt.Color;
import java.util.Random;
import java.util.Vector;
import meshIneBits.gui.view3d.builder.PavedMeshBuilderResult;
import meshIneBits.gui.view3d.animation.AnimationProcessor.AnimationOption;
import meshIneBits.gui.view3d.builder.BitShape;

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
        int currentBatch = bitShapes.get(0).getBatchId();
        for (BitShape bitShape : bitShapes) {
          if (bitShape.getBatchId() == currentBatch) {
            bitShape.getShape().setFill(ColorRandomUtil.instance.getCurrentColor().getRGB());
          } else {
            currentBatch = bitShape.getBatchId();
            bitShape.getShape().setFill(ColorRandomUtil.instance.generateNewColor().getRGB());
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
