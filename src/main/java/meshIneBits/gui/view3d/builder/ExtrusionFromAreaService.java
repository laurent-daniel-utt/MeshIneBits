package meshIneBits.gui.view3d.builder;

import meshIneBits.gui.view3d.oldversion.PolygonPointsList;
import meshIneBits.util.AreaTool;
import meshIneBits.util.CustomLogger;
import meshIneBits.util.Segment2D;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

import java.awt.geom.Area;
import java.util.Vector;

import static meshIneBits.gui.view3d.view.BaseVisualization3DView.WindowStatus;

public class ExtrusionFromAreaService {

  private static final CustomLogger logger = new CustomLogger(ExtrusionFromAreaService.class);

  private  static ExtrusionFromAreaService instance = new ExtrusionFromAreaService();

  public static ExtrusionFromAreaService getInstance() {
    return instance;
  }

  public PShape buildShapeFromArea(PApplet context, Area area, float extrudeDepth) {
    Vector<Segment2D> segmentList = AreaTool.getLargestPolygon(area);
    if (segmentList == null) {
      return null;
    }
    Vector<int[]> pointList = new Vector<>();
    for (Segment2D s : segmentList) {
      pointList.add(new int[]{(int) Math.round(s.start.x), (int) Math.round(s.start.y)});
      pointList.add(new int[]{(int) Math.round(s.end.x), (int) Math.round(s.end.y)});
    }
    PolygonPointsList poly;
    try {
      poly = new PolygonPointsList(pointList);
    } catch (Exception e) {
      logger.logERRORMessage(e.getMessage());
      return null;
    }
    return extrude(context, poly, (int) extrudeDepth);
  }

  private PShape extrude(PApplet context, PolygonPointsList poly, int extrudeDepth) {

    PShape extrudedObject = context.createShape(PConstants.GROUP);

    PShape exterior = getSideExtrude(context, poly, extrudeDepth);
    extrudedObject.addChild(exterior);

    PShape topFace = getPShape(context, poly, 0);
    extrudedObject.addChild(topFace);
    PShape bottomFace = getPShape(context, poly, extrudeDepth);
    extrudedObject.addChild(bottomFace);

    return extrudedObject;
  }

  private PShape getPShape(PApplet context, PolygonPointsList poly, int extrudeDepth) {
    int length;
    int[] point;

    PShape shape = context.createShape();
//shape.noStroke();
    shape.beginShape();

   if(WindowStatus==2) shape.fill(0);

    // Exterior path
    length = poly.getLength();
    for (int j = 0; j < length + 1; j++) {
      point = poly.getNextPoint();
      shape.vertex(point[0], point[1], extrudeDepth);
    }

    shape.endShape();

    return shape;
  }

  private PShape getSideExtrude(PApplet context, PolygonPointsList poly, int extrudeDepth) {
    PShape side = context.createShape(PConstants.GROUP);
    int length = poly.getLength();
    int[] pointA = poly.getNextPoint();
    int[] pointB = poly.getNextPoint();

    for (int j = 0; j < length; j++) {
      side.addChild(getFaceExtrude(context, pointA, pointB, extrudeDepth));
      pointA = pointB;
      pointB = poly.getNextPoint();
    }
    return side;
  }

  private PShape getFaceExtrude(PApplet context, int[] pointA, int[] pointB, int z) {
    PShape face = context.createShape();

    //face.noStroke();
    face.beginShape();

    if(WindowStatus==2) face.fill(0);
    face.vertex(pointA[0], pointA[1], z);
    face.vertex(pointB[0], pointB[1], z);
    face.vertex(pointB[0], pointB[1], 0);
    face.vertex(pointA[0], pointA[1], 0);
    face.endShape(PConstants.CLOSE);


    return face;
  }

}
