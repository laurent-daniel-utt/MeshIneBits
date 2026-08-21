/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2015-2026 DANIEL Laurent.
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
 -  Copyright (C) 2026 Hugo BENOIT
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

package meshIneBits;

import meshIneBits.config.CraftConfig;
import meshIneBits.util.LiftPointCalc;
import meshIneBits.util.TwoDistantPointsCalc;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import static meshIneBits.config.CraftConfig.precision;
import static meshIneBits.gui.view2d.MeshController.thecontroller;

//TODO define readObject and writeObject for save mesh
public class SubBit2D implements Serializable {

  private Bit2D parentBit;
  private Vector2 originPositionCS;
  private Vector2 orientationCS;
  private Vector2 liftPointCB;

  private Vector2 firstDistantPointCB;
  private Vector2 secondDistantPointCB;

  private AffineTransform transformMatrixToCS;
  private AffineTransform inverseMatrixToCB;

  private Vector2 XminPoint;

  private Vector2 XmaxPoint;
  private transient Area areaCB;
  private Path2D cutPath;
  private boolean removed=false;

  public SubBit2D(
      @NotNull Vector2 originPositionCS,
      @NotNull Vector2 orientationCS,
      @NotNull AffineTransform transformMatrixToCS,
      @NotNull AffineTransform inverseMatrixToCB,
      @NotNull Area area,
      @Nullable Path2D cutPath,
      @NotNull Bit2D parentBit) {

    this.originPositionCS = originPositionCS;
    this.orientationCS = orientationCS;
    this.transformMatrixToCS = transformMatrixToCS;
    this.inverseMatrixToCB = inverseMatrixToCB;
    this.areaCB = area;
    this.cutPath = cutPath;
    this.parentBit = parentBit;
    computeLiftPoints();
   if(liftPointCB!=null) computeDistantPoints(getLiftPointCB());
    computeXminXmaxPoints();
  }

  public static boolean compareSubs(SubBit2D sub1,SubBit2D sub2){
      ArrayList<Vector2> points1=new ArrayList<>();
      ArrayList<Vector2> points2=new ArrayList<>();
      points1.addAll(TwoDistantPointsCalc.instance.getPointsFromPath(sub1.cutPath));
      points2.addAll(TwoDistantPointsCalc.instance.getPointsFromPath(sub2.cutPath));
  for(int i=0;i<points1.size();i++){
      if (Math.abs(points1.get(i).x - points2.get(i).x)>0.0005 || Math.abs(points1.get(i).y - points2.get(i).y)>0.0005) return false;
  }
 return true;
  }


  public Vector2 getOriginPositionCS() {
    return originPositionCS;
  }

  public Vector2 getOrientationCS() {
    return orientationCS;
  }

  public Bit2D getParentBit() {
    return parentBit;
  }

  public Area getAreaCB() {
    return areaCB;
  }

  public Area getAreaCS() {
    Area areaCS = (Area) areaCB.clone();
    areaCS.transform(transformMatrixToCS);
    return areaCS;
  }

  public boolean isRemoved() {
    return removed;
  }
 public void setRemoved(boolean b){
    this.removed=b;

 }
  @SuppressWarnings("unused")
  public Path2D getCutPathCB() {
    return cutPath;
  }

  public Vector2 getLiftPointCS() {
    if (liftPointCB == null) {
      return null;
    }
    return liftPointCB.getTransformed(transformMatrixToCS);
  }

  public Vector2 getLiftPointCB() {
    return liftPointCB;
  }


  private void computeDistantPoints(Vector2 liftPointCB) {
    Vector<Vector2> points = TwoDistantPointsCalc.instance.defineTwoMostDistantPointsInArea(
        areaCB,liftPointCB,precision);
    if (points.size() == 2) {
      points.sort((a, b) -> (int) (a.x == b.x ? a.y - b.y : a.x - b.x));
      firstDistantPointCB = points.get(0);
      secondDistantPointCB = points.get(1);
    }
  }

    /**
     * compute the max and min x point of the subbit (the 2 extrimist points to right and to left)
     */
  private void computeXminXmaxPoints() {
    Vector<Vector2> points = TwoDistantPointsCalc.instance.getXminXmaxFromArea(areaCB, this);
    if (points.size() == 2) {

      XminPoint = points.get(0);//extreme point to left
      XmaxPoint = points.get(1);//extreme point to right
    }
  }

  @SuppressWarnings("unused")
  public Vector<Vector2> getTwoDistantPointsCB() {
    return new Vector<>(Arrays.asList(firstDistantPointCB, secondDistantPointCB));
  }

  public Vector<Vector2> getTwoDistantPointsCS() {
    if (firstDistantPointCB == null || secondDistantPointCB == null) {
      return new Vector<>();
    }
    return new Vector<>(
        Arrays.asList(
            firstDistantPointCB.getTransformed(transformMatrixToCS),
            secondDistantPointCB.getTransformed(transformMatrixToCS)));
  }


    /**
     * returns the max and min x point of the subbit (the 2 extrimist points to right and to left)
     */
  public Vector<Vector2> getTwoExtremeXPointsCS() {
    if (XminPoint == null || XmaxPoint == null) {
      return new Vector<>();
    }
    return new Vector<>(
            Arrays.asList(
                    XminPoint,
                    XmaxPoint));
  }

  private void computeLiftPoints() {
    liftPointCB = LiftPointCalc.instance.getLiftPoint(areaCB, CraftConfig.suckerDiameter / 2);
  }

  public static class SubBitBuilder {

    private Vector2 iOriginPositionCS;
    private Vector2 iOrientationCS;

    private AffineTransform iTransformMatrixToCS = new AffineTransform();
    private AffineTransform iInverseMatrixToCB = new AffineTransform();

    private Area iAreaCB;
    private Path2D iCutPath;
    private Bit2D parentBit;

    public SubBitBuilder setOriginPositionCS(Vector2 originPositionCS) {
      this.iOriginPositionCS = originPositionCS;
      return this;
    }

    public SubBitBuilder setOrientationCS(Vector2 orientationCS) {
      this.iOrientationCS = orientationCS;
      return this;
    }

    public SubBitBuilder setTransformMatrixToCS(AffineTransform transformMatrixToCS) {
      this.iTransformMatrixToCS = transformMatrixToCS;
      return this;
    }

    public SubBitBuilder setInverseMatrixToCB(AffineTransform inverseMatrixToCB) {
      this.iInverseMatrixToCB = inverseMatrixToCB;
      return this;
    }

    public SubBitBuilder setAreaCB(Area areaCB) {
      this.iAreaCB = areaCB;
      return this;
    }

    public SubBitBuilder setCutPath(Path2D cutPath) {
      this.iCutPath = cutPath;
      return this;
    }

    public SubBitBuilder setParentBit(Bit2D parentBit) {
      this.parentBit = parentBit;
      return this;
    }

    public SubBit2D build() {
      return new SubBit2D(
          iOriginPositionCS,
          iOrientationCS,
          iTransformMatrixToCS,
          iInverseMatrixToCB,
          iAreaCB,
          iCutPath,
          parentBit);
    }
  }

    public boolean isValid() {
        return liftPointCB != null;
    }
    public boolean isInValid() {
        return liftPointCB == null;
    }
    public boolean isregular(){
        if(getLiftPointCS()!=null) {return true;}
        else {return false;}
    }


    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(parentBit);
        oos.writeObject(originPositionCS);
        oos.writeObject(orientationCS);
        oos.writeObject(liftPointCB);
        oos.writeObject(firstDistantPointCB);
        oos.writeObject(secondDistantPointCB);
        oos.writeObject(transformMatrixToCS);
        oos.writeObject(inverseMatrixToCB);
        oos.writeObject(XminPoint);
        oos.writeObject(XmaxPoint);
        oos.writeObject(cutPath);
        oos.writeObject(AffineTransform.getTranslateInstance(0, 0)
                .createTransformedShape(this.getAreaCB()));
        oos.writeBoolean(removed);
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.parentBit= (Bit2D) ois.readObject();
        this.originPositionCS= (Vector2) ois.readObject();
        this.orientationCS= (Vector2) ois.readObject();
        this.liftPointCB= (Vector2) ois.readObject();
        this.firstDistantPointCB= (Vector2) ois.readObject();
        this.secondDistantPointCB= (Vector2) ois.readObject();
        this.transformMatrixToCS= (AffineTransform) ois.readObject();
        this.inverseMatrixToCB= (AffineTransform) ois.readObject();
        this.XminPoint= (Vector2) ois.readObject();
        this.XmaxPoint= (Vector2) ois.readObject();
        this.cutPath= (Path2D) ois.readObject();

        Shape s = (Shape) ois.readObject();
        this.areaCB=new Area(s);
        this.removed=ois.readBoolean();
        thecontroller.updateCore();
        // this.updateBoundaries(new Area(s));
    }
}
