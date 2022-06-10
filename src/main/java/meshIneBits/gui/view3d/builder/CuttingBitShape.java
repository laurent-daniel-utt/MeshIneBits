package meshIneBits.gui.view3d.builder;

import meshIneBits.NewBit3D;
import processing.core.PShape;

public class CuttingBitShape {

  private final PShape shape;

  public CuttingBitShape(PShape shape) {
    this.shape = shape;
  }

  public PShape getShape() {
    return shape;
  }
}
