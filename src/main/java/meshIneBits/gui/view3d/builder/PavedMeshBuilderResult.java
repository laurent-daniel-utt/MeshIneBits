package meshIneBits.gui.view3d.builder;

import processing.core.PShape;

import java.util.Vector;

public class PavedMeshBuilderResult {

  //private final PShape meshShape;
  private  PShape meshShape;
  //private final Vector<BitShape> bitShapes;
  private  Vector<BitShape> bitShapes;
  public PavedMeshBuilderResult(PShape meshShape, Vector<BitShape> bitShapes) {
    this.meshShape = meshShape;
    this.bitShapes = bitShapes;
  }

  public PShape getMeshShape() {
    return meshShape;
  }

  public Vector<BitShape> getBitShapes() {
    return bitShapes;
  }

  public boolean isNull() {
    return meshShape == null || bitShapes == null;
  }
}
