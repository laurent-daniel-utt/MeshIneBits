package meshIneBits.gui.view3d.builder;

import processing.core.PShape;

public class SubBitShape {
  private final PShape shape;
  private int batchId;
  private int layerId;

  public SubBitShape(PShape shape) {
    this.shape = shape;
  }

  public PShape getShape() {
    return shape;
  }

  public SubBitShape setBatchId(int batchId) {
    this.batchId = batchId;
    return this;
  }

  public SubBitShape setLayerId(int layerId) {
    this.layerId = layerId;
    return this;
  }

  public int getBatchId() {
    return batchId;
  }

  public int getLayerId() {
    return layerId;
  }
}
