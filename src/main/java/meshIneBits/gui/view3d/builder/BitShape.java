package meshIneBits.gui.view3d.builder;

import java.util.Vector;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

public class BitShape {

  private final PShape shape;
  private final Vector<SubBitShape> subBitShapes = new Vector<>();
  private Integer layerId;
  private Integer batchId;

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

  public int getBatchId() {
    return batchId == null ? -1 : batchId;
  }

  public BitShape setLayerId(int layerId) {
    this.layerId = layerId;
    return this;
  }

  @SuppressWarnings("all")
  public BitShape setBatchId(int batchId) {
    this.batchId = batchId;
    return this;
  }
}
