package meshIneBits.gui.view3d.builder;

import processing.core.PShape;

public class SubBitShape {
  private final PShape shape;

  public SubBitShape(PShape shape) {
    this.shape = shape;
  }

  public PShape getShape() {
    return shape;
  }
}
