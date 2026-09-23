package meshIneBits.gui.view3d.builder;

import java.util.Vector;

import meshIneBits.Bit3D;
import meshIneBits.SubBit2D;
import meshIneBits.util.Vector3;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

public class BitShape {

  private final PShape shape;
  private final Vector<SubBitShape> subBitShapes = new Vector<>();
  private Integer layerId;

  private Bit3D bit;

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
  public void addSubBit(SubBitShape subBitShape, SubBit2D subbit) {
    subBitShape.setSubbit(subbit);
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

  public BitShape setLayerId(int layerId) {
    this.layerId = layerId;
    return this;
  }

  public void translate(float x, float y, float lowerAltitude) {
//    shape.translate(x,y,lowerAltitude);
    subBitShapes.forEach(subBitShape -> subBitShape.getShape().translate(x,y,lowerAltitude));
  }

  public void rotateZ(float radians) {
    subBitShapes.forEach(subBitShape -> subBitShape.getShape().rotate(radians));
  }

  public void setBit(Bit3D bit){
    this.bit = bit;
  }

  public Bit3D getBit(){
    return bit;
  }
}
