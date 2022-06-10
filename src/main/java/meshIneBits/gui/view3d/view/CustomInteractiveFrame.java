package meshIneBits.gui.view3d.view;

import meshIneBits.util.Vector3;
import processing.core.PShape;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.InteractiveFrame;
import remixlab.proscene.Scene;

public class CustomInteractiveFrame extends InteractiveFrame {

  private PShape shape;

  @SuppressWarnings("unused")
  public CustomInteractiveFrame(Scene scene, PShape pShape) {
    super(scene, pShape);
    this.shape = pShape;
  }

  public CustomInteractiveFrame(Scene scene) {
    super(scene);
  }

  public Vector3 getMinShapeInFrameCoordinate() {
    double minx = Double.MAX_VALUE;
    double miny = Double.MAX_VALUE;
    double minz = Double.MAX_VALUE;
    int size = shape.getChildCount();
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < 3; j++) {
        Vec vertex = new Vec(shape.getChild(i).getVertex(j).x
            , shape.getChild(i).getVertex(j).y
            , shape.getChild(i).getVertex(j).z);
        Vec v = this.inverseCoordinatesOf(vertex);
        if (minx > v.x()) {
          minx = v.x();
        }
        if (miny > v.y()) {
          miny = v.y();
        }
        if (minz > v.z()) {
          minz = v.z();
        }
      }
    }
    return new Vector3(minx, miny, minz);
  }

  @Override
  public void setShape(PShape pShape) {
    super.setShape(pShape);
    this.shape=pShape;
  }

  public Vector3 getMaxShapeInFrameCoordinate() {
    double maxX = -Double.MIN_VALUE;
    double maxY = -Double.MIN_VALUE;
    double maxZ = -Double.MIN_VALUE;
    int size = shape.getChildCount();
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < 3; j++) {
        Vec vertex = new Vec(shape.getChild(i)
            .getVertex(j).x, shape.getChild(i)
            .getVertex(j).y,
            shape.getChild(i)
                .getVertex(j).z);
        Vec v = this.inverseCoordinatesOf(vertex);
        if (maxX < v.x()) {
          maxX = v.x();
        }
        if (maxY < v.y()) {
          maxY = v.y();
        }
        if (maxZ < v.z()) {
          maxZ = v.z();
        }
      }
    }
    return new Vector3(maxX, maxY, maxZ);
  }


}
