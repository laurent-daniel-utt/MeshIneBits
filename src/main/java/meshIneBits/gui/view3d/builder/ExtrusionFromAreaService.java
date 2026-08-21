/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2016-2026 DANIEL Laurent.
 -  Copyright (C) 2015 Cédric Siourakhan
 -  Copyright (C) 2016 Gabriel Magny
 -  Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 -  Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 -  Copyright (C) 2018 VALLON Benjamin.
 -  Copyright (C) 2018 LORIMER Campbell.
 -  Copyright (C) 2018 D'AUTUME Christian.
 -  Copyright (C) 2019 DURINGER Nathan (Tests).
 -  Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
 -  Copyright (C) 2020-2021 DO Quang Bao.
 -  Copyright (C) 2021 VANNIYASINGAM Mithulan.
 -  Copyright (C) 2021  Quang Long
 -  Copyright (C) 2022 Mhamad Atlab
 -  Copyright (C) 2022 Majed Hlaihel
 -  Copyright (C) 2026 Aymeric Sirejol
 -
 -  This program is free software: you can redistribute it and/or modify
 -  it under the terms of the GNU General Public License as published by
 -  the Free Software Foundation, either version 3 of the License, or
 -  (at your option) any later version.
 -
 -  This program is distributed in the hope that it will be useful,
 -  but WITHOUT ANY WARRANTY; without even the implied warranty of
 -  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 -  GNU General Public License for more details.
 -
 -  You should have received a copy of the GNU General Public License
 -  along with this program. If not, see <https://www.gnu.org/licenses/>.
 -----------------------------------------------------------------------------*/

package meshIneBits.gui.view3d.builder;

import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view3d.oldversion.PolygonPointsList;
import meshIneBits.util.AreaTool;
import meshIneBits.util.CustomLogger;
import meshIneBits.util.Segment2D;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

import java.awt.*;
import java.awt.geom.Area;
import java.util.Vector;

public class ExtrusionFromAreaService {

  private static final CustomLogger logger = new CustomLogger(ExtrusionFromAreaService.class);

  private  static ExtrusionFromAreaService instance = new ExtrusionFromAreaService();

  public static ExtrusionFromAreaService getInstance() {
    return instance;
  }
/*
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
  }*/
public PShape buildShapeFromArea(PApplet context, Area area, float extrudeDepth) {
  Vector<Segment2D> segmentList = AreaTool.getLargestPolygon(area);
  if (segmentList == null) {
    return null;
  }
  Vector<double[]> pointList = new Vector<>();
  for (Segment2D s : segmentList) {
    pointList.add(new double[]{ s.start.x,  s.start.y});
    pointList.add(new double[]{s.end.x,  s.end.y});
  }
  PolygonPointsList poly;
  try {
    poly = new PolygonPointsList(pointList,true);
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

 /* private PShape getPShape(PApplet context, PolygonPointsList poly, int extrudeDepth) {
    int length;
    int[] point;

    PShape shape = context.createShape();
    shape.beginShape();
shape.fill(255,250,150);
   if(WindowStatus==2) shape.fill(0);
Color c=new Color(255,250,150);
    // Exterior path
    length = poly.getLength();
    for (int j = 0; j < length + 1; j++) {
      point = poly.getNextPoint();
      shape.vertex(point[0], point[1], extrudeDepth);
    }

    shape.endShape();
    return shape;
  }*/
 private PShape getPShape(PApplet context, PolygonPointsList poly, int extrudeDepth) {
   int length;
   double[] point;

   PShape shape = context.createShape();
   shape.beginShape();
   shape.fill(255,250,150);
   //if(WindowStatus==2) shape.fill(0);
   Color c=new Color(255,250,150);
   // Exterior path
   length = poly.getLengthForArea();
   for (int j = 0; j < length + 1; j++) {
     point = poly.getNextPointforArea();
     shape.vertex((float) point[0], (float)point[1], extrudeDepth);
   }

   shape.endShape();
   return shape;
 }

  /*private PShape getSideExtrude(PApplet context, PolygonPointsList poly, int extrudeDepth) {
    PShape side = context.createShape(PConstants.GROUP);
    int length = poly.getLength();
    double[] pointA = poly.getNextPointforArea();
    double[] pointB = poly.getNextPointforArea();
    for (int j = 0; j < length; j++) {
      side.addChild(getFaceExtrude(context, pointA, pointB, extrudeDepth));
      pointA = pointB;
      pointB = poly.getNextPoint();
    }
    return side;
  }*/

  private PShape getSideExtrude(PApplet context, PolygonPointsList poly, int extrudeDepth) {
    PShape side = context.createShape(PConstants.GROUP);
    int length = poly.getLengthForArea();
    double[] pointA = poly.getNextPointforArea();
    double[] pointB = poly.getNextPointforArea();
    for (int j = 0; j < length; j++) {
      side.addChild(getFaceExtrude(context, pointA, pointB, extrudeDepth));
      pointA = pointB;
      pointB = poly.getNextPointforArea();
    }
    return side;
  }

  /*private PShape getFaceExtrude(PApplet context, int[] pointA, int[] pointB, int z) {
    PShape face = context.createShape();
System.out.println("Begin");

face.beginShape();
    if((Math.abs(pointA[0])==CraftConfig.lengthFull/2 && Math.abs(pointB[0])==CraftConfig.lengthFull/2)
            || pointA[1]+pointB[1]==CraftConfig.bitWidth || pointA[1]+pointB[1]==-CraftConfig.bitWidth ) {
      System.out.println("beige");
      face.fill(255,250,150);
      }
  else {  face.fill(255,0,0); }
    if(WindowStatus==2) face.fill(0);
    face.vertex(pointA[0], pointA[1], z);
    face.vertex(pointB[0], pointB[1], z);
    face.vertex(pointB[0], pointB[1], 0);
    face.vertex(pointA[0], pointA[1], 0);
    face.endShape(PConstants.CLOSE);
System.out.println("A0:"+pointA[0]+" A1:"+ pointA[1]);
    System.out.println("B0:"+pointB[0]+" B1:"+ pointB[1]+"\n");
    return face;
  }*/

  private PShape getFaceExtrude(PApplet context, double[] pointA, double[] pointB, double z) {
    PShape face = context.createShape();
    face.beginShape();
    /**if-else to fill the faces with right colors used for (CuttedBit and DepositedBit) interfaces */
    if((Math.abs(pointA[0])>=CraftConfig.lengthFull/2 && Math.abs(pointB[0])>=CraftConfig.lengthFull/2)
            || pointA[1]+pointB[1]>=CraftConfig.bitWidth || pointA[1]+pointB[1]<=-CraftConfig.bitWidth ) {
      face.fill(255,250,150);
    }
    else {  face.fill(255,0,0); }
   // if(WindowStatus==2) face.fill(0);
    face.vertex((float)pointA[0], (float)pointA[1], (float)z);
    face.vertex((float)pointB[0],(float) pointB[1], (float)z);
    face.vertex((float)pointB[0], (float)pointB[1], 0);
    face.vertex((float)pointA[0],(float) pointA[1], 0);
    face.endShape(PConstants.CLOSE);

    return face;
  }

}
