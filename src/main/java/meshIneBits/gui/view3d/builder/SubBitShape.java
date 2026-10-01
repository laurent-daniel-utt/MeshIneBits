package meshIneBits.gui.view3d.builder;

import meshIneBits.SubBit2D;
import meshIneBits.util.Vector3;
import processing.core.PShape;

public class SubBitShape {
  //private final PShape shape;
  private  PShape shape;
  private int batchId;
  private int layerId;

  private SubBit2D subbit;

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

  public void setSubbit(SubBit2D subbit){
    this.subbit = subbit;
  }

  public SubBit2D getSubbitOrigin(){
    return subbit;
  }
}
