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

package meshIneBits;

import meshIneBits.SubBit2D.SubBitBuilder;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.CutPathCalc;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;
import java.util.stream.Collectors;

public class NewBit2D extends Bit2D {
  private Vector<SubBit2D> subBits = new Vector<>();
  private Area areaSent;

  public NewBit2D(Vector2 origin, Vector2 orientation) {
    this(origin, orientation, CraftConfig.lengthFull, CraftConfig.bitWidth);
  }

  public NewBit2D(double boundaryCenterX,
      double boundaryCenterY,
      double length,
      double width,
      double orientationX,
      double orientationY) {
    super(boundaryCenterX, boundaryCenterY, length, width, orientationX, orientationY);
    calcCutPath();
    buildSubBits(getAreasCS(), getCutPathsCB());
  }

  public NewBit2D(Vector2 origin, Vector2 orientation, double length, double width) {
    super(origin, orientation, length, width);
    calcCutPath();
    buildSubBits(getAreasCS(), getCutPathsCB());
  }

  private NewBit2D(Vector2 origin,
      Vector2 orientation,
      double length,
      double width,
      AffineTransform transfoMatrix,
      AffineTransform inverseTransfoMatrix,
      Vector<Path2D> cutPaths,
      Vector<Area> areas) {
    super(origin, orientation, length, width, transfoMatrix, inverseTransfoMatrix, cutPaths, areas);
    buildSubBits(getAreasCB(), getCutPathsCB());

  }

  private void buildSubBits(Vector<Area> areas, Vector<Path2D> cutPaths) {
    subBits.clear();
    if (areas.isEmpty()) {
      return;
    }
    if (cutPaths.size() == 0) {
      SubBitBuilder builder = new SubBitBuilder();
      SubBit2D subBit2D = builder.setOriginPositionCS(getOriginCS())
          .setOrientationCS(getOrientation())
          .setTransformMatrixToCS(getTransfoMatrixToCS())
          .setInverseMatrixToCB(getInverseTransfoMatrixToCB())
          .setAreaCB(areas.get(0))
          .setCutPath(null)
          .setParentBit(this)
          .build();
      subBits.add(subBit2D);
      return;
    }
    if (areas.size() != cutPaths.size()) {
//      throw new IllegalArgumentException("SubBit areas and subBit cutPath must have the same size");
      return;
    }
    for (int i = 0; i < areas.size(); i++) {
      SubBitBuilder builder = new SubBitBuilder();
      SubBit2D subBit2D = builder.setOriginPositionCS(getOriginCS())
          .setOrientationCS(getOrientation())
          .setTransformMatrixToCS(getTransfoMatrixToCS())
          .setInverseMatrixToCB(getInverseTransfoMatrixToCB())
          .setAreaCB(areas.get(i))
          .setCutPath(cutPaths.get(i))
          .setParentBit(this)
          .build();
      subBits.add(subBit2D);
    }
    subBits.sort((sub1, sub2) -> {
      Vector2 lift1 = sub1.getLiftPointCB();
      Vector2 lift2 = sub2.getLiftPointCB();
      if (lift1 == null) {
        return -1;
      }
      if (lift2 == null) {
        return 1;
      }
      return Double.compare(lift1.y, lift2.y) == 0 ?
          Double.compare(lift1.x, lift2.x) :
          Double.compare(lift1.y, lift2.y);
    });
  }

  public Vector<SubBit2D> getSubBits() {
    return new Vector<>(subBits);

  }
public void removeSubbit(SubBit2D sub){
    subBits.remove(sub);
    rebuildBit2d(this);
  }


  private void rebuildBit2d(Bit2D bit){

    NewBit2D newbit=(NewBit2D)bit;
    Vector<SubBit2D> subs=newbit.getValidSubBits();
    Vector<Area> areas=new Vector<>();
    for (SubBit2D sub:subs){
      areas.add(sub.getAreaCB());
    }
    bit.setAreas(areas);
    bit.setCutPaths(CutPathCalc.instance.calcCutPathFrom(bit));
  }
  public void addSubbit(SubBit2D sub){
    subBits.add(sub);
  }

  public Vector<SubBit2D> getValidSubBits() {
    return subBits.stream()
        .filter(SubBit2D::isValid)
        .collect(Collectors.toCollection(Vector::new));
  }
  public Vector<SubBit2D> getInValidSubBits() {
    return subBits.stream()
            .filter(SubBit2D::isInValid)
            .collect(Collectors.toCollection(Vector::new));
  }


  @SuppressWarnings("unused")
  public boolean isValid() {
    return getValidSubBits().size() > 0;
  }


  @Override
  public void updateBoundaries(@NotNull Area transformedArea) {
    super.updateBoundaries(transformedArea);
    calcCutPath();
    buildSubBits(getAreasCB(), getCutPathsCB());
  //Thread.dumpStack();
  }

  @Override
  public NewBit2D createTransformedBit(AffineTransform transformation) {
    Vector2 newOrigin = getOriginCS().getTransformed(transformation).getRounded();
    Vector2 newOrientation = getOriginCS().add(getOrientation())
        .getTransformed(transformation)
        .sub(newOrigin)
        .normal()
        .getRounded();
    NewBit2D newBit = new NewBit2D(newOrigin, newOrientation, getLength(), getWidth());
    newBit.updateBoundaries(this.getAreaCS().createTransformedArea(transformation));
    return newBit;
  }

  @Override
  public void resize(double newPercentageLength, double newPercentageWidth) {
    super.resize(newPercentageLength, newPercentageWidth);
    calcCutPath();
    buildSubBits(getAreasCB(), getCutPathsCB());
  }

  @Override
  @SuppressWarnings("all")
  public NewBit2D clone() {
    return new NewBit2D(getOriginCS().clone(),
        getOrientation().clone(),
        getLength(),
        getWidth(),
        (AffineTransform) getTransfoMatrixToCS().clone(),
        (AffineTransform) getInverseTransfoMatrixToCB().clone(),
        (Vector<Path2D>) getCutPathsCB().clone(),
        (Vector<Area>) getAreasCB().clone());
  }

  @Override
  public void calcCutPath() {
    super.calcCutPath();
  }




  private void writeObject(ObjectOutputStream oos) throws IOException {
   oos.writeObject(subBits);
  }


  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    this.subBits= (Vector<SubBit2D>) ois.readObject();
    //updateBoundaries(areaSent);
  }

public void sendArea(Area a){
    this.areaSent=a;
}
}
