/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2021 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020 CLARIS Etienne & RUSSO André.
 * Copyright (C) 2020-2021 DO Quang Bao.
 * Copyright (C) 2021 VANNIYASINGAM Mithulan.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package meshIneBits.gui.view3d.oldversion;

import java.awt.geom.Area;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;
import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Model;
import meshIneBits.config.CraftConfig;
import meshIneBits.scheduler.AScheduler;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Logger;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Triangle;
import meshIneBits.util.Vector2;
import meshIneBits.util.Vector3;
import processing.core.PApplet;
import processing.core.PShape;

/*
 * Used by ProcessingModelView to create displayable PShape of Model and 3Dbits
 *
 */

class Builder extends PApplet implements Observer {

  private final int MODEL_COLOR = color(219, 100, 50);
  private final int BIT_COLOR = color(112, 66, 20);

  public final int COLOR_BATCH_1 = color(112, 66, 20);
  public final int COLOR_BATCH_2 = color(20, 66, 112);

  private ControllerView3D controllerView3D;
  private PApplet pApplet;
  private boolean waitting;
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private ExecutorService executorService2 = Executors.newFixedThreadPool(2);


  Builder(PApplet pApplet) {
    controllerView3D = ControllerView3D.getInstance();
    this.pApplet = pApplet;
  }

  /**
   * This method build a {@link PShape} function {@link Model} provided as a parameter, and assign
   * value to the given {@link Model} parameter.
   *
   * @param model {@link Model} to build
   * @param shape {@link PShape} to assign
   */
  void buildShape(Model model, PShape shape) {
    Logger.updateStatus("Start building STL model");
    Vector<Triangle> stlTriangles = model.getTriangles();
    //pApplet.shapeMode(CORNER);
    for (Triangle t : stlTriangles) {
      PShape shape1 = getPShapeFromTriangle(t);
      shape.addChild(shape1);
    }
    Logger.updateStatus("STL model built.");
  }

  /**
   * Return {@link PShape} from {@link Triangle}
   *
   * @param t {@link Triangle triangle}
   * @return {@link PShape}
   */
  private PShape getPShapeFromTriangle(Triangle t) {

    PShape face = pApplet.createShape();
    //face.setStroke(color(9, 72, 217));
    face.setFill(MODEL_COLOR);
    face.beginShape();
    face.noStroke();
    for (Vector3 p : t.point) {
      face.vertex((float) p.x, (float) p.y, (float) p.z);
    }
    face.endShape(CLOSE);

    return face;
  }

  private void buildBits(Vector<Pair<Position, PShape>> shapeMap) {

    Logger.updateStatus("Start building 3D model");

    Vector<Layer> layers = controllerView3D.getCurrentMesh()
        .getLayers();
    float bitThickness = (float) CraftConfig.bitThickness;
    float layersOffSet = (float) CraftConfig.layersOffset;

//    getUncutBitPShape(bitThickness);
    int bitCount = 0;
    Vector<Pair<Bit3D, Vector2>> sortedBits = controllerView3D.getCurrentMesh()
        .getScheduler()
        .getSortedBits();

    for (Pair<Bit3D, Vector2> sortedBit : sortedBits) {
      bitCount++;
      Bit3D curBit = sortedBit.getKey();
      PShape bitPShape;
      pApplet.fill(BIT_COLOR);
      pApplet.stroke(0);
      bitPShape = getBitPShapeFrom(curBit.getRawAreas(), bitThickness);
      if (bitPShape != null) {
        Vector2 curBitCenter = curBit.getOrigin();
        float curBitCenterX = (float) curBitCenter.x;
        float curBitCenterY = (float) curBitCenter.y;
        float[] translation = {curBitCenterX, curBitCenterY, (float) curBit.getLowerAltitude()};
        float rotation = (float) curBit.getOrientation()
            .getEquivalentAngle2();
        Position curBitPosition = new Position(translation, rotation);
        shapeMap.add(new Pair<>(curBitPosition, bitPShape));
      }
    }
    Logger.updateStatus("3D model built : " + bitCount + " bits generated.");
  }

  /**
   * Assign PShape from the paved {@link meshIneBits.Mesh}, get from {@link ControllerView3D} to the
   * two lists in parameter. It's used in {@link #buildMeshPaved }
   *
   * @param shapeMapByLayer the {@Vector list} contains {@link PShape} of each Layer by its
   *                        position
   * @param shapeMaByBit    the {@Vector list} contains {@link PShape} of each Bit by its position
   */
  private void buildShapePaved(Vector<Pair<Layer, PShape>> shapeMapByLayer,
      Vector<Pair<Bit3D, PShape>> shapeMaByBit) {
    int currentBatch = -1;
    int newBatch;
    int currentColor = COLOR_BATCH_2;
    if (!controllerView3D.getCurrentMesh()
        .isPaved()) {
      return;
    }

    Vector<Layer> layers = controllerView3D.getCurrentMesh()
        .getLayers();
    float bitThickness = (float) CraftConfig.bitThickness;

    getUncutBitPShape(bitThickness);
    int bitCount = 0;
    for (Layer layer : layers) {
      Vector3 v = controllerView3D.getModel().getPos();
      List<Bit3D> bitsInCurrentLayer = AScheduler.getSetBit3DsSortedFrom(
          controllerView3D.getCurrentMesh()
              .getScheduler()
              .filterBits(layer.sortBits()));
      PShape layerPShape = this.pApplet.createShape(GROUP);
      for (Bit3D curBit : bitsInCurrentLayer) {
        newBatch = controllerView3D.getCurrentMesh()
            .getScheduler()
            .getBitBatch(curBit);
        if (currentBatch != newBatch) {
          currentBatch = newBatch;
          currentColor = currentColor == COLOR_BATCH_1 ? COLOR_BATCH_2 : COLOR_BATCH_1;
        }
        bitCount++;
        PShape bitPShape;
        bitPShape = getBitPShapeFrom(curBit.getRawAreas(), bitThickness);
        bitPShape.setFill(currentColor);
        if (bitPShape != null) {
          Vector2 curBitCenter = curBit.getOrigin();
          float curBitCenterX = (float) curBitCenter.x;
          float curBitCenterY = (float) curBitCenter.y;
          float[] translation = {curBitCenterX, curBitCenterY, (float) curBit.getLowerAltitude()};
          float rotation = (float) curBit.getOrientation()
              .getEquivalentAngle2();
          Position curBitPosition = new Position(translation, rotation);
          bitPShape.rotateZ(radians((float) curBit.getOrientation()
              .getEquivalentAngle2()));
          bitPShape.translate(curBitCenterX, curBitCenterY, (float) curBit.getLowerAltitude());
          layerPShape.addChild(bitPShape);
          shapeMaByBit.add(new Pair<>(curBit, bitPShape));
        }
      }

      Position curLayerPosition = new Position(new float[]{(float) v.x, (float) v.y, (float) v.z},
          0);

      shapeMapByLayer.add(new Pair<>(layer, layerPShape));
    }

    Logger.updateStatus("3D model built : " + bitCount + " bits generated.");
  }

  private void buildShapePaved2(Vector<Pair<Layer, PShape>> shapeMapByLayer,
      Vector<Pair<Bit3D, PShape>> shapeMapByBits) {

    if (!controllerView3D.getCurrentMesh()
        .isPaved()) {
      return;
    }

    Vector<Layer> layers = controllerView3D.getCurrentMesh()
        .getLayers();
    float bitThickness = (float) CraftConfig.bitThickness;

//    getUncutBitPShape(bitThickness);
    for (Layer layer : layers) {
      Vector3 v = controllerView3D.getModel()
          .getPos();
      List<Bit3D> bitsInCurrentLayer = AScheduler.getSetBit3DsSortedFrom(
          controllerView3D.getCurrentMesh()
              .getScheduler()
              .filterBits(layer.sortBits()));
      PShape layerPShape = this.pApplet.createShape(GROUP);
//            executorService.execute(() -> {
      for (Bit3D curBit : bitsInCurrentLayer) {
        PShape bitPShape;
        bitPShape = getBitPShapeFrom(curBit.getRawAreas(), bitThickness);
        if (bitPShape != null) {
          Vector2 curBitCenter = curBit.getOrigin();
          float curBitCenterX = (float) curBitCenter.x;
          float curBitCenterY = (float) curBitCenter.y;
          float[] translation = {curBitCenterX, curBitCenterY, (float) curBit.getLowerAltitude()};
          float rotation = (float) curBit.getOrientation()
              .getEquivalentAngle2();
          Position curBitPosition = new Position(translation, rotation);
          bitPShape.rotateZ(radians((float) curBit.getOrientation()
              .getEquivalentAngle2()));
          bitPShape.translate(curBitCenterX, curBitCenterY, (float) curBit.getLowerAltitude());
          synchronized (layerPShape) {
            layerPShape.addChild(bitPShape);
          }
          synchronized (shapeMapByBits) {
            shapeMapByBits.add(new Pair<>(curBit, bitPShape));
          }
        }
      }
      Position curLayerPosition = new Position(
          new float[]{(float) v.x, (float) v.y, (float) layer.getLowerAltitude()}, 0);
      synchronized (shapeMapByLayer) {
        shapeMapByLayer.add(new Pair<>(layer, layerPShape));
      }
//            });

    }
    executorService.shutdown();
    try {
      executorService.awaitTermination(20, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
//        executorService2.execute(() -> {
//            shapeMapByLayer.sort((ele1, ele2) -> (int) (ele1.getKey().getLowerAltitude() - ele2.getKey().getLowerAltitude()));
//            shapeMaByBit.sort((ele1, ele2) -> {
//                if (ele1.getKey().getTranslation()[2] != ele2.getKey().getTranslation()[2])
//                    return (int) (ele1.getKey().getTranslation()[2] - ele2.getKey().getTranslation()[2]);
//                return ele1.getValue().colorMode - ele2.getValue().colorMode;
//            });
//        });
  }


  /**
   * Assign PShape from the paved {@link meshIneBits.Mesh}, get from {@link ControllerView3D} to the
   * two lists in parameter and return whole {@link PShape} of the paved Mesh
   *
   * @param shapeMapByLayer the {@Vector list} contains {@link PShape} of each Layer by its
   *                        position
   * @param shapeMapByBits  the {@Vector list} contains {@link PShape} of each Bit by its position
   * @return {@link PShape} of the paved Mesh
   */
  public PShape buildMeshPaved(Vector<Pair<Layer, PShape>> shapeMapByLayer,
      Vector<Pair<Bit3D, PShape>> shapeMapByBits) {
    controllerView3D.getCurrentMesh()
        .getScheduler()
        .schedule();
    buildShapePaved2(shapeMapByLayer, shapeMapByBits);
    PShape meshShape = this.pApplet.createShape(GROUP);
    for (Pair<Layer, PShape> aShapeMap : shapeMapByLayer) {
      PShape s = aShapeMap.getValue();
      meshShape.addChild(s);
    }
    organizePositionAndColor(shapeMapByLayer, shapeMapByBits);

    return meshShape;
  }

  private void organizePositionAndColor(Vector<Pair<Layer, PShape>> shapeMapByLayer,
      Vector<Pair<Bit3D, PShape>> shapeMapByBits) {
    final int[] currentBatch = {-1};
    final int[] newBatch = new int[1];
    final int[] currentColor = {COLOR_BATCH_2};
    executorService2.execute(() -> {
      shapeMapByLayer.sort((ele1, ele2) -> (int) (ele1.getKey()
          .getLowerAltitude() - ele2.getKey()
          .getLowerAltitude()));

    });
    executorService2.execute(() -> {
      shapeMapByBits.sort(Comparator.comparingInt(
          ele -> controllerView3D.getCurrentMesh()
              .getScheduler()
              .getBitIndex(ele.getKey())));
      shapeMapByBits.forEach((ele) -> {
        newBatch[0] = controllerView3D.getCurrentMesh()
            .getScheduler()
            .getBitBatch(ele.getKey());
        if (currentBatch[0] != newBatch[0]) {
          currentBatch[0] = newBatch[0];
          currentColor[0] = currentColor[0] == COLOR_BATCH_1 ? COLOR_BATCH_2 : COLOR_BATCH_1;
        }
        ele.getValue()
            .setFill(currentColor[0]);
      });
    });
  }

  /**
   * @param extrudeDepth thickness
   */
  private void getUncutBitPShape(float extrudeDepth) {

    Vector2 cornerUpRight = new Vector2(+CraftConfig.lengthFull / 2.0, -CraftConfig.bitWidth / 2.0);
    Vector2 cornerDownRight = new Vector2(cornerUpRight.x, cornerUpRight.y + CraftConfig.bitWidth);
    Vector2 cornerUpLeft = new Vector2(cornerUpRight.x - CraftConfig.lengthFull, cornerUpRight.y);
    Vector2 cornerDownLeft = new Vector2(cornerDownRight.x - CraftConfig.lengthFull,
        cornerDownRight.y);

    Vector<int[]> pointList = new Vector<>();
    pointList.add(new int[]{(int) cornerUpRight.x, (int) cornerUpRight.y, 0});
    pointList.add(new int[]{(int) cornerDownRight.x, (int) cornerDownRight.y, 0});
    pointList.add(new int[]{(int) cornerDownLeft.x, (int) cornerDownLeft.y, 0});
    pointList.add(new int[]{(int) cornerUpLeft.x, (int) cornerUpLeft.y, 0});

    PolygonPointsList poly;
    try {
      poly = new PolygonPointsList(pointList);
    } catch (Exception e) {
      Logger.error("Polygon point list exception");
      return;
    }

    extrude(new PolygonPointsList[]{poly, null}, (int) extrudeDepth);
  }

  /**
   * @param bitAreas     horizontal section
   * @param extrudeDepth thickness
   * @return part of rectangular parallelepiped
   */
  private PShape getBitPShapeFrom(List<Area> bitAreas, float extrudeDepth) {
    PShape bitShape = pApplet.createShape(GROUP);
    for (Area bitArea : bitAreas) {
      Vector<Segment2D> segmentList = AreaTool.getLargestPolygon(bitArea);
      if (segmentList == null) {
        return null;
      }

      Vector<int[]> pointList = new Vector<>();
      for (Segment2D s : segmentList) {
        pointList.add(new int[]{(int) Math.round(s.start.x), (int) Math.round(s.start.y), 0});
        pointList.add(new int[]{(int) Math.round(s.end.x), (int) Math.round(s.end.y), 0});
      }

      PolygonPointsList poly;
      try {
        poly = new PolygonPointsList(pointList);
      } catch (Exception e) {
        System.out.println("Polygon point list exception");
        return null;
      }

      PShape subitShape = extrude(new PolygonPointsList[]{poly, null}, (int) extrudeDepth);
//            return subitShape;
      bitShape.addChild(subitShape);
    }
    return bitShape;
  }

  public PShape getBatchPShapeForm(Vector<Pair<Bit3D, PShape>> shapeMapByBits, int batchNumber) {
    PShape batchShape = pApplet.createShape(GROUP);
    for (Pair<Bit3D, PShape> pair : shapeMapByBits) {
      if (controllerView3D.getCurrentMesh()
          .getScheduler()
          .getBitBatch(pair.getKey())
          == batchNumber) {
        batchShape.addChild(pair.getValue());
      }
    }
    return batchShape;
  }

  private PShape getFaceExtrude(int[] pointA, int[] pointB, int z) {
    PShape face = pApplet.createShape();
//        face.setStroke(15);
    face.beginShape();
    face.vertex(pointA[0], pointA[1], pointA[2] + z);
    face.vertex(pointB[0], pointB[1], pointB[2] + z);
    face.vertex(pointB[0], pointB[1], pointB[2]);
    face.vertex(pointA[0], pointA[1], pointA[2]);
    face.endShape(CLOSE);

    return face;
  }

  /**
   * @param poly horizontal boundary
   * @param z    thickness
   * @return side wall
   */
  private PShape getSideExtrude(PolygonPointsList poly, int z) {

    PShape side = pApplet.createShape(GROUP);

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
   * @param poly horizontal boundary
   * @param z    thickness
   * @return 3D presentation of bit
   */
  private PShape getPShape(PolygonPointsList[] poly, int z) {

    int length;
    int[] point;

    PShape myShape = pApplet.createShape();
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
   */
  private PShape extrude(PolygonPointsList[] poly, int z) {

    PShape extrudedObject = pApplet.createShape(GROUP);

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

  public synchronized void pause() {
    this.waitting = true;
    while (this.waitting) {
      try {
        this.wait();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public synchronized void continu() {
    this.waitting = false;
    this.notifyAll();
  }

  public void onTerminated() {
    executorService.shutdownNow();
  }
}