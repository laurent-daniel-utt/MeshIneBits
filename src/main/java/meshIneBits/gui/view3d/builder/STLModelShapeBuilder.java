package meshIneBits.gui.view3d.builder;

import meshIneBits.Model;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.util.Triangle;
import meshIneBits.util.Vector3;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;

public class STLModelShapeBuilder extends PApplet implements IModelShapeBuilder {
public static CountDownLatch shaping=new CountDownLatch(1);
  public static STLModelShapeBuilder createInstance(PApplet pApplet, Model model) {
    return new STLModelShapeBuilder(pApplet, model);
  }

  private final PApplet context;
  private final Model stlModel;

  public STLModelShapeBuilder(PApplet context, Model stlModel) {
    this.context = context;
    this.stlModel = stlModel;
  }

  @Override
  public PShape buildModelShape() {System.out.println("in BuildModelShape");
    PShape modelShape = context.createShape(PConstants.GROUP);

    Vector<Triangle> triangles = stlModel.getTriangles();
    for (Triangle triangle : triangles) {

      PShape shape = buildShapeFromTriangle(triangle);
      modelShape.addChild(shape);
    }
    System.out.println("modelShape_Lenth="+modelShape.getChildren().length);
    System.out.println("modelShapeChild1_depth="+modelShape.getChild(1).getDepth());
    return modelShape;
  }

  private PShape buildShapeFromTriangle(Triangle triangle) {

    PShape face = context.createShape();
   // System.out.println("triangle"+triangle);
    //System.out.println("face-depthBEFORE="+face.getDepth());
    face.setFill(context.color(
        Visualization3DConfig.MODEL_COLOR.getRed(),
        Visualization3DConfig.MODEL_COLOR.getGreen(),
        Visualization3DConfig.MODEL_COLOR.getBlue()));

    //  System.out.println("face afterFill"+face.getDepth());
    face.beginShape();
    face.noStroke();

    for (Vector3 p : triangle.point) {
      face.vertex((float) p.x, (float) p.y, (float) p.z);
//System.out.println("pz="+p.z);
    }

    face.endShape(PConstants.CLOSE);

    //System.out.println("face-depth="+face.getDepth()+" Thread="+Thread.currentThread().getName());
    return face;
  }
}
