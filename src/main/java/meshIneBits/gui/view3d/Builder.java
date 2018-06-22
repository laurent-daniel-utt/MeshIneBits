package meshIneBits.gui.view3d;

import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Model;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view2d.Controller;
import meshIneBits.util.*;
import processing.core.PApplet;
import processing.core.PShape;

import java.awt.geom.Area;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

public class Builder extends PApplet implements Observer {

    private final int MODEL_COLOR = color(219, 100, 50);
    private final int BIT_COLOR = color(219, 100, 50);
    private Controller controller;


    public Builder() {
        controller = Controller.getInstance();
    }

    public void buildShape(Model model, PShape shape){
        Logger.updateStatus("Start building STL model");

        Vector<Triangle> stlTriangles = model.getTriangles();

        for (Triangle t : stlTriangles){
            shape.addChild(getPShapeFromTriangle(t));
        }

        Logger.updateStatus("STL model built.");
    }

    private PShape getPShapeFromTriangle(Triangle t){

        PShape face = createShape();
        face.setStroke(false);
        face.setFill(MODEL_COLOR);
        face.beginShape();
        for (Vector3 p : t.point){
            face.vertex((float) p.x, (float) p.y, (float) p.z);
        }

        face.endShape(CLOSE);

        return face;
    }

    public void buildBits(HashMap<Position,PShape> shapeMap) {

        Logger.updateStatus("Start building 3D model");
        Vector<Layer> layers = controller.getCurrentPart().getLayers();
        float bitThickness = (float) CraftConfig.bitThickness;
        float layersOffSet = (float) CraftConfig.layersOffset;

        PShape uncutBitPShape = getUncutBitPShape(bitThickness);

        float zLayer;
        int bitCount = 0;

        for (Layer curLayer : layers) {
            zLayer = curLayer.getLayerNumber() * (bitThickness + layersOffSet);
            for (Vector2 curBitKey : curLayer.getBits3dKeys()) {
                bitCount++;
                Bit3D curBit = curLayer.getBit3D(curBitKey);
                PShape bitPShape;
                bitPShape = getBitPShapeFrom(curBit.getRawArea(), bitThickness);
                if (bitPShape != null) {
                    bitPShape.setFill(BIT_COLOR);
                    Vector2 curBitCenter = curBit.getOrigin();
                    float curBitCenterX = (float) curBitCenter.x;
                    float curBitCenterY = (float) curBitCenter.y;
                    float[] translation = { curBitCenterX, curBitCenterY, zLayer };
                    float rotation = (float) curBit.getOrientation().getEquivalentAngle2();
                    Position curBitPosition = new Position(translation, rotation);
                    shapeMap.put(curBitPosition, bitPShape);
                }
            }
        }

        Logger.updateStatus("3D model built : " + bitCount + " bits generated.");
    }
    /**
     *
     * @param extrudeDepth
     * @return
     */
    private PShape getUncutBitPShape(float extrudeDepth) {

        Vector2 cornerUpRight = new Vector2(+CraftConfig.bitLength / 2.0, -CraftConfig.bitWidth / 2.0);
        Vector2 cornerDownRight = new Vector2(cornerUpRight.x, cornerUpRight.y + CraftConfig.bitWidth);
        Vector2 cornerUpLeft = new Vector2(cornerUpRight.x - CraftConfig.bitLength, cornerUpRight.y);
        Vector2 cornerDownLeft = new Vector2(cornerDownRight.x - CraftConfig.bitLength, cornerDownRight.y);

        Vector<int[]> pointList = new Vector<int[]>();
        pointList.add(new int[] { (int) cornerUpRight.x, (int) cornerUpRight.y, 0 });
        pointList.add(new int[] { (int) cornerDownRight.x, (int) cornerDownRight.y, 0 });
        pointList.add(new int[] { (int) cornerDownLeft.x, (int) cornerDownLeft.y, 0 });
        pointList.add(new int[] { (int) cornerUpLeft.x, (int) cornerUpLeft.y, 0 });

        PolygonPointsList poly = null;
        try {
            poly = new PolygonPointsList(pointList);
        } catch (Exception e) {
            System.out.println("Polygon point list exception");
            return null;
        }

        return extrude(new PolygonPointsList[] { poly, null }, (int) extrudeDepth);
    }

    /**
     *
     * @param bitArea
     * @param extrudeDepth
     * @return
     */
    private PShape getBitPShapeFrom(Area bitArea, float extrudeDepth) {

        Vector<Segment2D> segmentList = AreaTool.getLargestPolygon(bitArea);
        if (segmentList == null)
            return null;

        Vector<int[]> pointList = new Vector<int[]>();
        for (Segment2D s : segmentList) {
            pointList.add(new int[] { (int) Math.round(s.start.x), (int) Math.round(s.start.y), 0 });
            pointList.add(new int[] { (int) Math.round(s.end.x), (int) Math.round(s.end.y), 0 });
        }

        PolygonPointsList poly = null;
        try {
            poly = new PolygonPointsList(pointList);
        } catch (Exception e) {
            System.out.println("Polygon point list exception");
            return null;
        }

        return extrude(new PolygonPointsList[] { poly, null }, (int) extrudeDepth);
    }

    private PShape getFaceExtrude(int[] pointA, int[] pointB, int z) {
        PShape face = createShape();
        face.beginShape();
        face.vertex(pointA[0], pointA[1], pointA[2] + z);
        face.vertex(pointB[0], pointB[1], pointB[2] + z);
        face.vertex(pointB[0], pointB[1], pointB[2]);
        face.vertex(pointA[0], pointA[1], pointA[2]);
        face.endShape(CLOSE);

        return face;
    }

    /**
     *
     * @param poly
     * @param z
     * @return
     */
    private PShape getSideExtrude(PolygonPointsList poly, int z) {

        PShape side = createShape(GROUP);

        int length = poly.getLength();
        int[] pointA = poly.getNextPoint();
        int[] pointB = poly.getNextPoint();

        for (int j = 0; j < length; j++) {
            side.addChild(getFaceExtrude(pointA, pointB, z));
            pointA = pointB;
            pointB = poly.getNextPoint();
        }

        return side;
    }

    /**
     *
     * @param poly
     * @param z
     * @return
     */
    private PShape getPShape(PolygonPointsList[] poly, int z) {

        int length;
        int[] point;

        PShape myShape = createShape();
        myShape.beginShape();
        // Exterior path
        length = poly[0].getLength();
        for (int j = 0; j < length + 1; j++) {
            point = poly[0].getNextPoint();
            myShape.vertex(point[0], point[1], point[2] + z);
        }
        // Interior path
        if (poly[1] != null) {
            myShape.beginContour();
            length = poly[1].getLength();
            for (int j = 0; j < length + 1; j++) {
                point = poly[1].getNextPoint();
                myShape.vertex(point[0], point[1], point[2] + z);
            }
            myShape.endContour();
        }

        myShape.endShape();

        return myShape;
    }

    /**
     * Work only for shape on the xy plan
     *
     */
    private PShape extrude(PolygonPointsList[] poly, int z) {

        PShape extrudedObject = createShape(GROUP);

        PShape exterior = getSideExtrude(poly[0], z);
        extrudedObject.addChild(exterior);

        if (poly[1] != null) {
            PShape holeSides = getSideExtrude(poly[1], z);
            extrudedObject.addChild(holeSides);
        }

        PShape topFace = getPShape(poly, 0);
        extrudedObject.addChild(topFace);
        PShape bottomFace = getPShape(poly, z);
        extrudedObject.addChild(bottomFace);

        return extrudedObject;
    }

    @Override
    public void update(Observable o, Object arg) {
    }
}
