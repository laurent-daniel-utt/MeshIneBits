package meshIneBits.gui.view3d.util;

import meshIneBits.util.Vector3;
import processing.core.PShape;
import remixlab.dandelion.geom.Vec;

public class PShapeUtil {

  public static final PShapeUtil instance = new PShapeUtil();

  public static PShapeUtil getInstance() {
    return instance;
  }

  public Vector3 getMinShapeInFrameCoordinate(PShape shape) {
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double minZ = Double.MAX_VALUE;

    int size = shape.getChildCount();
    for (int i = 0; i < size; i++) {
      for(int k = 0; k < shape.getChild(i).getChildCount();k++){
        for (int j = 0; j < shape.getChild(i).getChild(k).getVertexCount(); j++) {
          Vec vertex = new Vec(shape.getChild(i).getChild(k).getVertex(j).x
              , shape.getChild(i).getChild(k).getVertex(j).y
              , shape.getChild(i).getChild(k).getVertex(j).z);
          if (minX > vertex.x()) {
            minX = vertex.x();
          }
          if (minY > vertex.y()) {
            minY = vertex.y();
          }
          if (minZ > vertex.z()) {
            minZ = vertex.z();
          }
        }
      }
    }
    return new Vector3(minX, minY, minZ);
  }

  public Vector3 getMaxShapeInFrameCoordinate(PShape shape) {
    double maxX = -Double.MIN_VALUE;
    double maxY = -Double.MIN_VALUE;
    double maxZ = -Double.MIN_VALUE;
    int size = shape.getChildCount();
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < 3; j++) {
        Vec v = new Vec(shape.getChild(i)
            .getVertex(j).x, shape.getChild(i)
            .getVertex(j).y,
            shape.getChild(i)
                .getVertex(j).z);
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
