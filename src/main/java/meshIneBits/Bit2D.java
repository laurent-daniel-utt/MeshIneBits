/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2022 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
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
 *
 */

package meshIneBits;

import meshIneBits.config.CraftConfig;
import meshIneBits.util.AreaTool;
import meshIneBits.util.CutPathCalc;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;

/**
 * Bit2D represent a bit in 2D : boundaries and cut path. A {@link Bit3D} is build with multiple
 * Bit2D <br>
 * <img src="./doc-files/bit2d.png" alt="">
 * <br/> We always take the upper left corner as (- {@link CraftConfig#lengthFull bitLength} / 2, -
 * {@link CraftConfig#bitWidth bitWidth} / 2 ). The bit's normal boundary is a rectangle.
 *
 * @see Bit3D
 */
public class Bit2D implements Cloneable, Serializable {

  /**
   * In the {@link Mesh} coordinate system
   */
  private Vector2 orientation;
  /**
   * In the {@link Mesh} coordinate system
   */
  private Vector2 origin;
  private double length;
  private double width;
  private AffineTransform transfoMatrixCS = new AffineTransform();
  private AffineTransform inverseTransfoMatrixCB = new AffineTransform();
  /**
   * In {@link Bit2D} coordinate system
   */
  private Vector<Path2D> cutPaths = new Vector<>();
  /**
   * In {@link Bit2D} coordinate system
   */
  private Vector<Area> areas = new Vector<>();

  /**
   * <code>true</code> if the bit has been placed in a bid to feed the neural network.
   */
  private boolean usedForNN;


  private Boolean inverseInCut = false;


  private Boolean checkFullLength = true;

  /**
   * A new full bit with <tt>origin</tt> and <tt>orientation</tt> in the coordinate system of {@link
   * Mesh}
   *
   * @param origin      the center of bit's outer bound
   * @param orientation the rotation of bit
   */
  public Bit2D(Vector2 origin, Vector2 orientation) {
    this.origin = origin;
    this.orientation = orientation;
    length = CraftConfig.lengthFull;
    width = CraftConfig.bitWidth;

    setTransfoMatrix();
    buildBoundaries();
  }

  public Bit2D(double boundaryCenterX,
      double boundaryCenterY,
      double length,
      double width,
      double orientationX,
      double orientationY) {
    orientation = new Vector2(
        orientationX,
        orientationY
    );
    this.length = length;
    this.width = width;
    origin = new Vector2(boundaryCenterX, boundaryCenterY)
        .sub(
            // Vector distance in Bit coordinate system
            new Vector2(
                -CraftConfig.lengthFull / 2 + length / 2,
                -CraftConfig.bitWidth / 2 + width / 2
            )
                // Rotate into Mesh coordinate system
                .rotate(orientation)
        );

    setTransfoMatrix();
    buildBoundaries();
  }


  /**
   * Constructor for custom length and width.
   *
   * @param origin      the center of bit's outer bound
   * @param orientation the rotation of bit
   * @param length      length of the bit
   * @param width       width of the bit
   */
  public Bit2D(Vector2 origin, Vector2 orientation, double length, double width) {
    this.origin = origin;
    this.orientation = orientation;
    this.length = length;
    this.width = width;

    setTransfoMatrix();
    buildBoundaries();
  }

  /**
   * Constructor for cloning
   *
   * @param origin               center of bit's outer bound
   * @param orientation          rotation of the bit
   * @param length               length of the bit
   * @param width                width of the bit
   * @param transfoMatrix        transformation to be applied
   * @param inverseTransfoMatrix inversion of <tt>transfoMatrix</tt>
   * @param cutPaths             where to cut this bit
   * @param areas                set of non intersected areas
   */
  Bit2D(Vector2 origin, Vector2 orientation, double length, double width,
      AffineTransform transfoMatrix,
      AffineTransform inverseTransfoMatrix, Vector<Path2D> cutPaths, Vector<Area> areas) {
    this.origin = origin;
    this.orientation = orientation;
    this.length = length;
    this.width = width;
    this.transfoMatrixCS = transfoMatrix;
    this.inverseTransfoMatrixCB = inverseTransfoMatrix;
    this.cutPaths = cutPaths;
    this.areas = areas;
  }

  /**
   * Returns the four segments of a Bit2D (the Bit2D is not cut by cut paths)
   *
   * @return a Vector of the four segments.
   */
  public Vector<Segment2D> getBitSidesSegments() {
    // bit's colinear and orthogonal unit vectors computation
    Vector2 colinear = this.getOrientation()
        .normal();
    Vector2 orthogonal = colinear.rotate(
        new Vector2(0, -1).normal()); // 90deg anticlockwise rotation

        Vector2 A = this.getCenter()
                .add(colinear.mul(CraftConfig.lengthNormal / 2-CraftConfig.sectionHoldingToCut/2))
                .add(orthogonal.mul(CraftConfig.bitWidth / 2));

        Vector2 B = this.getCenter()
                .sub(colinear.mul(CraftConfig.lengthNormal / 2+CraftConfig.sectionHoldingToCut/2))
                .add(orthogonal.mul(CraftConfig.bitWidth / 2));

        Vector2 C = this.getCenter()
                .sub(colinear.mul(CraftConfig.lengthNormal / 2+CraftConfig.sectionHoldingToCut/2))
                .sub(orthogonal.mul(CraftConfig.bitWidth / 2));

        Vector2 D = this.getCenter()
                .add(colinear.mul(CraftConfig.lengthNormal / 2-CraftConfig.sectionHoldingToCut/2))
                .sub(orthogonal.mul(CraftConfig.bitWidth / 2));

    return new Vector<>(Arrays.asList(
        new Segment2D(A, B),
        new Segment2D(B, C),
        new Segment2D(C, D),
        new Segment2D(D, A)));
  }

  /**
   * Create the area of the bit. This is necessary when the bit has been reduced manually.<br/>
   * <b>Note</b>: We always take the upper left corner as
   * (- {@link CraftConfig#lengthFull bitLength} / 2, - {@link CraftConfig#bitWidth bitWidth} / 2 ).
   * The bit's boundary is a rectangle.
   */
  private void buildBoundaries() {
    Rectangle2D.Double r = new Rectangle2D.Double(
        -CraftConfig.lengthFull / 2,
        -CraftConfig.bitWidth / 2,
        length,
        width
    );

    this.areas.clear();
    this.areas.add(new Area(r));


  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public Bit2D clone() {
    return new Bit2D(origin, orientation, length, width, (AffineTransform) transfoMatrixCS.clone(),
        (AffineTransform) inverseTransfoMatrixCB.clone(), getClonedCutPathsCB(),
        getClonedAreasCB());
  }

  /**
   * @return the union of all cloned surfaces making this {@link Bit2D}. Expressed in {@link Mesh}
   * coordinate system
   */
  public Area getAreaCS() {
    Area transformedArea = new Area();
    for (Area a : areas) {
      transformedArea.add(a);
    }
    transformedArea.transform(transfoMatrixCS);
    return transformedArea;
  }





  /**
   * @return clone of all surfaces making this bit transformed by
   * <tt>transforMatrix</tt>
   */
  @SuppressWarnings("unused")
  public Vector<Area> getAreasCS() {
    Vector<Area> result = new Vector<>();
    for (Area a : areas) {
      Area transformedArea = new Area(a);
      transformedArea.transform(transfoMatrixCS);
      result.add(transformedArea);
    }
    return result;
  }

  /**
   * @return clone of raw areas
   */
  private Vector<Area> getClonedAreasCB() {
    Vector<Area> clonedAreas = new Vector<>();
    for (Area a : areas) {
      clonedAreas.add((Area) a.clone());
    }
    return clonedAreas;
  }

  /**
   * @return clone of raw cut paths
   */
  private Vector<Path2D> getClonedCutPathsCB() {
    if (cutPaths != null) {
      Vector<Path2D> clonedCutPaths = new Vector<>();
      for (Path2D p : cutPaths) {
        clonedCutPaths.add((Path2D) p.clone());
      }
      return clonedCutPaths;
    } else {
      return null;
    }
  }

  /**
   * @return clone of cut paths (after transforming into coordinates system of {@link Mesh})
   */
  @SuppressWarnings("unused")
  public Vector<Path2D> getCutPathsCS() {
    if (this.cutPaths == null) {
      return null;
    } else {
      Vector<Path2D> paths = new Vector<>();
      for (Path2D p : this.cutPaths) {
        paths.add(new Path2D.Double(p, transfoMatrixCS));
      }
      return paths;
    }
  }

  /**
   * @return horizontal side
   */
  public double getLength() {
    return length;
  }

  /**
   * @return orientation in {@link Mesh} coordinate system
   */
  public Vector2 getOrientation() {
    return orientation;
  }
public void setOrientation(Vector2 orientation){
    this.orientation=orientation;

}
  /**
   * @return the origin in the {@link Mesh} coordinate system
   */
  public Vector2 getOriginCS() {
    return origin;
  }
  public void setOriginCS(Vector2 origin) {

    this.origin=origin;
  }

  /**
   * @return the center of the rectangle boundary of this bit, not necessarily the {@link #origin}.
   * Calculate from the upper left corner
   */
  public Vector2 getCenter() {
    return origin.sub(
        // Vector distance in Bit coordinate system
        new Vector2(
            CraftConfig.lengthFull / 2 - length / 2,
            CraftConfig.bitWidth / 2 - width / 2
        )
            // Rotate into Mesh coordinate system
            .rotate(orientation)
    );
  }

  /**
   * A raw area is an area that has not been transformed to another coordinate system.
   *
   * @return the union of raw (non intersected) areas
   */
  public Area getAreaCB() {
    Area area = new Area();
    for (Area a : areas) {
      area.add(a);
    }
    return area;
  }

  /**
   * @return set of raw areas of this bit (not transformed)
   */
  public Vector<Area> getAreasCB() {
    return areas;
  }

  /**
   * Any change will reflect on bit itself
   *
   * @return raw cut paths
   */
  public Vector<Path2D> getCutPathsCB() {
    return cutPaths;
  }

  public void setCutPaths(Vector<Path2D> cutPaths) {
    this.cutPaths = cutPaths;
  }

  /**
   * @return vertical side
   */
  public double getWidth() {
    return width;
  }

  /**
   * Set up the matrix transformation from {@link Bit2D} coordinate system into {@link Pavement}
   */
  private void setTransfoMatrix() {

    transfoMatrixCS.translate(origin.x, origin.y);
    transfoMatrixCS.rotate(orientation.x, orientation.y);
    try {
      inverseTransfoMatrixCB = ((AffineTransform) transfoMatrixCS.clone()).createInverse();
    } catch (NoninvertibleTransformException e) {
      e.printStackTrace();
      inverseTransfoMatrixCB = AffineTransform.getScaleInstance(1, 1); // Fallback
    }
  }
public  void callMinusSetTransfoMatrix(){
  transfoMatrixCS.translate(-origin.x,- origin.y);
  transfoMatrixCS.rotate(-orientation.x,- orientation.y);


}

  /**
   * Given an area cut from a zone, construct the surface of this bit
   *
   * @param transformedArea a bit of surface in real
   */
  public void updateBoundaries(@NotNull Area transformedArea) {
    areas.clear();
    Area newArea = (Area) transformedArea.clone();
    if (!checkSectionHoldingToCut(origin, orientation, newArea)) {
      removeSectionHolding(this, newArea);
      checkFullLength = false;
    } else if (checkInverseBit(this, newArea)) {
      inverseInCut = true;
    }
    newArea.transform(inverseTransfoMatrixCB);
    Vector<Area> listAreas = AreaTool.segregateArea(newArea);
    areas.addAll(listAreas != null ? listAreas : new Vector<>());

  }

  public void setAreas(Vector<Area> areas) {
    this.areas = areas;
  }

  private static void removeSectionHolding(Bit2D bit, Area bitArea) {
    Vector<Area> sections = getTwoSectionHolding(bit.origin, bit.orientation, -CraftConfig.incertitude);
    bitArea.subtract(sections.lastElement());
  }

  /**
   * To resize a bit, keeping top left corner as reference.
   *
   * @param newPercentageLength 100 means retain 100% of normal bit's length
   * @param newPercentageWidth  100 means retain 100% of normal bit's width
   */
  public void resize(double newPercentageLength, double newPercentageWidth) {
    length = CraftConfig.lengthFull * newPercentageLength / 100;
    width = CraftConfig.bitWidth * newPercentageWidth / 100;
    // Rebuild the boundary
    buildBoundaries();
  }

  @Override
  public String toString() {
    Rectangle2D bound = this.getAreaCS()
        .getBounds2D();
    return "Bit2D[origin=" + origin
        + ", length=" + length
        + ", width=" + width
        + ", orientation=" + orientation
        + ", areaBound=[x=" + bound.getX()
        + ", y=" + bound.getY()
        + ", w=" + bound.getWidth()
        + ", h=" + bound.getHeight()
        + "]"
        + "]";
  }

  /**
   * This method only accepts the conservative transformation (no scaling).
   *
   * @param transformation a combination of conservative affine transformation
   * @return a new {@link Bit2D} with same geometric of initial one transformed by
   * <tt>transformation</tt>
   */
  public Bit2D createTransformedBit(AffineTransform transformation) {
    Vector2 newOrigin = origin.getTransformed(transformation)
        .getRounded(),
        newOrientation = origin.add(orientation)
            .getTransformed(transformation)
            .sub(newOrigin)
            .normal()
            .getRounded();
    Bit2D newBit = new Bit2D(newOrigin, newOrientation, length, width);
    newBit.updateBoundaries(this.getAreaCS()
        .createTransformedArea(transformation));
    return newBit;
  }

  /**
   * Reset cut paths and recalculate them after defining area
   */
  public void calcCutPath() {
    this.cutPaths = CutPathCalc.instance.calcCutPathFrom(this);
  }

  /**
   * In charge of serializing the object, especially {@link #areas}
   *
   * @param oos stream of writing
   * @throws IOException if error of writing
   */
  private void writeObject(ObjectOutputStream oos) throws IOException {
    // Write normal fields
    oos.writeObject(origin);
    oos.writeObject(orientation);
    oos.writeDouble(length);
    oos.writeDouble(width);
    oos.writeObject(cutPaths);
    oos.writeObject(transfoMatrixCS);
    oos.writeObject(inverseTransfoMatrixCB);
    oos.writeBoolean(inverseInCut);
    oos.writeBoolean(checkFullLength);
    // Special writing for areas
    oos.writeObject(AffineTransform.getTranslateInstance(0, 0)
        .createTransformedShape(this.getAreaCS()));
  Vector<Shape> shapesfromareas=new Vector<>();
  for(Area a:areas){
    shapesfromareas.add(AffineTransform.getTranslateInstance(0, 0)
            .createTransformedShape(a));
  }
  oos.writeObject(shapesfromareas);
  }

  /**
   * In charge of reading the object, especially {@link #areas}
   *
   * @param ois stream of reading
   * @throws IOException if error of reading or mismatching class
   */
  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    this.origin = (Vector2) ois.readObject();
    this.orientation = (Vector2) ois.readObject();
    this.length = ois.readDouble();
    this.width = ois.readDouble();
    //noinspection unchecked
    this.cutPaths = (Vector<Path2D>) ois.readObject();
    this.transfoMatrixCS = (AffineTransform) ois.readObject();
    this.inverseTransfoMatrixCB = (AffineTransform) ois.readObject();
    this.areas = new Vector<>();
    this.inverseInCut = ois.readBoolean();
    this.checkFullLength = ois.readBoolean();
    Shape s = (Shape) ois.readObject();
    NewBit2D newbit= (NewBit2D) this;
    newbit.sendArea(new Area(s));
   Vector<Shape> shapes= (Vector<Shape>) ois.readObject();
   for(Shape shape:shapes){
     areas.add(new Area(shape));
   }
    // this.updateBoundaries(new Area(s));
  }

  public AffineTransform getTransfoMatrixToCS() {
    return transfoMatrixCS;
  }


  public AffineTransform getInverseTransfoMatrixToCB() {
    return inverseTransfoMatrixCB;
  }

  public boolean getInverseInCut() {
    return inverseInCut;
  }


  public void setCheckFullLength(Boolean checkFullLength) {
    this.checkFullLength = checkFullLength;
  }

  public Boolean isFullLength() {
    return checkFullLength;
  }

  /**
   * return true if the bit can use full length of bit This methode is used after area of bit is
   * transformed in Bit's coordinate
   *
   * @return
   */
  public boolean checkSectionHoldingToCut() {
    Area bitArea = new Area();
    for (Area area : areas) {
      bitArea.add(area);
    }
    Vector<Rectangle2D> twoSides = getTwoSideOfBit(CraftConfig.incertitude);
    return bitArea.contains(twoSides.firstElement()) || bitArea.contains(twoSides.lastElement());
  }

  /**
   * Return two section hold to cut of bit without the cut paths
   */
  public static Vector<Area> getTwoSectionHolding(Vector2 position, Vector2 orientation,
      double incertitude) {

    Vector<Area> result = new Vector();
    Vector<Rectangle2D> rectangles = getTwoSideOfBit(incertitude);

    Area leftArea = new Area(rectangles.firstElement());
    Area rightArea = new Area(rectangles.lastElement());

    AffineTransform affineTransform = new AffineTransform();
    affineTransform.translate(position.x, position.y);
    affineTransform.rotate(orientation.x, orientation.y);

    leftArea.transform(affineTransform);
    rightArea.transform(affineTransform);
    result.add(leftArea);
    result.add(rightArea);

    return result;
  }

  /**
   * Return a list of two sides of the bit dans le coordinate of {@link Bit2D}
   *
   * @param incertitude
   * @return
   */
  public static Vector<Rectangle2D> getTwoSideOfBit(double incertitude) {
    Rectangle2D leftArea = new Rectangle2D.Double(-CraftConfig.lengthFull / 2 + incertitude
        , -CraftConfig.bitWidth / 2 + incertitude
        , CraftConfig.sectionHoldingToCut - 2 * incertitude
        , CraftConfig.bitWidth - 2 * incertitude);
    Rectangle2D rightArea = new Rectangle2D.Double(
        CraftConfig.lengthFull / 2 - CraftConfig.sectionHoldingToCut + incertitude
        , -CraftConfig.bitWidth / 2 + incertitude
        , CraftConfig.sectionHoldingToCut - 2 * incertitude
        , CraftConfig.bitWidth - 2 * incertitude);
    Vector<Rectangle2D> result = new Vector<>();
    result.add(leftArea);
    result.add(rightArea);
    return result;
  }

  /**
   * This static method is used to check if the {@link Bit2D} will be created can use full length of
   * {@link Bit2D}, only for before create {@link Bit2D}
   *
   * @param position    position of {@link Bit2D}
   * @param orientation Orientation of {@link Bit2D}
   * @param bitArea     Area of {@link Bit2D}, before calculate
   * @return true for full length,
   */
  public static boolean checkSectionHoldingToCut(Vector2 position, Vector2 orientation,
      Area bitArea) {
    Area area = (Area) bitArea.clone();
    Vector<Rectangle2D> twoSides = getTwoSideOfBit(CraftConfig.incertitude);

    AffineTransform affineTransform = new AffineTransform();
    affineTransform.translate(position.x, position.y);
    affineTransform.rotate(orientation.x, orientation.y);

    try {
      AffineTransform inverse = affineTransform.createInverse();
      area.transform(inverse);
      return !(!area.contains(twoSides.firstElement()) && !area.contains(twoSides.lastElement())
          && area.intersects(twoSides.firstElement()) && area.intersects(twoSides.lastElement()));
    } catch (NoninvertibleTransformException e) {
      e.printStackTrace();
      return false;
    }

  }

  public static boolean checkInverseBit(Bit2D bit2D, Area bitArea) {
    Area newBitArea = (Area) bitArea.clone();
    if (!bit2D.isFullLength()) {
      return false;
    }
    Vector<Rectangle2D> twoSide = getTwoSideOfBit(CraftConfig.incertitude);
    newBitArea.transform(bit2D.getInverseTransfoMatrixToCB());
    //last element is right side
    return !newBitArea.contains(twoSide.lastElement()) && newBitArea.intersects(
        twoSide.lastElement());
  }

  public boolean isUsedForNN() {
    return usedForNN;
  }

  public void setUsedForNN(boolean usedForNN) {
    this.usedForNN = usedForNN;
  }
}