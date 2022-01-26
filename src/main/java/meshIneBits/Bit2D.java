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
import meshIneBits.util.CutPathUtil;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Bit2D represent a bit in 2D : boundaries and cut path. A {@link Bit3D} is
 * build with multiple Bit2D <br>
 * <img src="./doc-files/bit2d.png" alt="">
 * <br/>
 * We always take the upper left corner as
 * (- {@link CraftConfig#lengthFull bitLength} / 2, - {@link CraftConfig#bitWidth
 * bitWidth} / 2 ). The bit's normal boundary is a rectangle.
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
    private AffineTransform transfoMatrix = new AffineTransform();
    private AffineTransform inverseTransfoMatrix = new AffineTransform();
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
     * A new full bit with <tt>origin</tt> and <tt>orientation</tt> in the
     * coordinate system of {@link Mesh}
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
//        for (Path2D longestCrossingSegment : longestCrossingSegments) {
//            longestCrossingSegment.transform(transfoMatrix);
//        }
//        for (Area area : areas) {
//            area.getPathIterator()
//        }
    }

    /**
     * This constructor will decide itself the origin, base on upper left corner
     *
     * @param boundaryCenterX in {@link Mesh} coordinate system
     * @param boundaryCenterY in {@link Mesh} coordinate system
     * @param length          of boundary
     * @param width           of boundary
     * @param orientationX    in {@link Mesh} coordinate system
     * @param orientationY    in {@link Mesh} coordinate system
     */
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
    private Bit2D(Vector2 origin, Vector2 orientation, double length, double width, AffineTransform transfoMatrix,
                  AffineTransform inverseTransfoMatrix, Vector<Path2D> cutPaths, Vector<Area> areas) {
        this.origin = origin;
        this.orientation = orientation;
        this.length = length;
        this.width = width;
        this.transfoMatrix = transfoMatrix;
        this.inverseTransfoMatrix = inverseTransfoMatrix;
        this.cutPaths = cutPaths;
        this.areas = areas;
    }

    /**
     * Returns the four segments of a Bit2D (the Bit2D is not cut by cut paths)
     * @return a Vector of the four segments.
     */
    public Vector<Segment2D> getBitSidesSegments() {
        // bit's colinear and orthogonal unit vectors computation
        Vector2 colinear = this.getOrientation().normal();
        Vector2 orthogonal = colinear.rotate(new Vector2(0, -1).normal()); // 90deg anticlockwise rotation

        Vector2 A = this.getOrigin()
                .add(colinear.mul(length / 2))
                .add(orthogonal.mul(CraftConfig.bitWidth/2));

        Vector2 B = this.getOrigin()
                .sub(colinear.mul(length / 2))
                .add(orthogonal.mul(CraftConfig.bitWidth/2));

        Vector2 C = this.getOrigin()
                .sub(colinear.mul(length / 2))
                .sub(orthogonal.mul(CraftConfig.bitWidth/2));

        Vector2 D = this.getOrigin()
                .add(colinear.mul(length / 2))
                .sub(orthogonal.mul(CraftConfig.bitWidth/2));

        return new Vector<>(Arrays.asList(
                new Segment2D(A, B),
                new Segment2D(B, C),
                new Segment2D(C, D),
                new Segment2D(D, A)));
    }

    /**
     * Create the area of the bit. This is
     * necessary when the bit has been reduced manually.<br/>
     * <b>Note</b>: We always take the upper left corner as
     * (- {@link CraftConfig#lengthFull bitLength} / 2, - {@link CraftConfig#bitWidth
     * bitWidth} / 2 ). The bit's boundary is a rectangle.
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
        return new Bit2D(origin, orientation, length, width, (AffineTransform) transfoMatrix.clone(),
                (AffineTransform) inverseTransfoMatrix.clone(), getClonedRawCutPaths(), getClonedRawAreas());
    }

    /**
     * @return the union of all cloned surfaces making this {@link Bit2D}. Expressed
     * in {@link Mesh} coordinate system
     */
    public Area getArea() {
        Area transformedArea = new Area();
        for (Area a : areas) {
            transformedArea.add(a);
        }
        transformedArea.transform(transfoMatrix);
        return transformedArea;
    }

    /**
     * @return clone of all surfaces making this bit transformed by
     * <tt>transforMatrix</tt>
     */
    @SuppressWarnings("unused")
    public Vector<Area> getAreas() {
        Vector<Area> result = new Vector<>();
        for (Area a : areas) {
            Area transformedArea = new Area(a);
            transformedArea.transform(transfoMatrix);
            result.add(transformedArea);
        }
        return result;
    }

    /**
     * @return clone of raw areas
     */
    private Vector<Area> getClonedRawAreas() {
        Vector<Area> clonedAreas = new Vector<>();
        for (Area a : areas) {
            clonedAreas.add((Area) a.clone());
        }
        return clonedAreas;
    }

    /**
     * @return clone of raw cut paths
     */
    private Vector<Path2D> getClonedRawCutPaths() {
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
     * @return clone of cut paths (after transforming into coordinates system of
     * {@link Mesh})
     */
    @SuppressWarnings("unused")
    public Vector<Path2D> getCutPaths() {
        if (this.cutPaths == null) {
            return null;
        } else {
            Vector<Path2D> paths = new Vector<>();
            for (Path2D p : this.cutPaths) {
                paths.add(new Path2D.Double(p, transfoMatrix));
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

    /**
     * @return the origin in the {@link Mesh} coordinate system
     */
    public Vector2 getOrigin() {
        return origin;
    }

    /**
     * @return the center of the rectangle boundary of this bit, not necessarily the
     * {@link #origin}. Calculate from the upper left corner
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
     * A raw area is an area that has not been transformed to another coordinate
     * system.
     *
     * @return the union of raw (non intersected) areas
     */
    Area getRawArea() {
        Area area = new Area();
        for (Area a : areas) {
            area.add(a);
        }
        return area;
    }

    /**
     * @return set of raw areas of this bit (not transformed)
     */
    public Vector<Area> getRawAreas() {
        return areas;
    }

    /**
     * Any change will reflect on bit itself
     *
     * @return raw cut paths
     */
    Vector<Path2D> getRawCutPaths() {
        return cutPaths;
    }


    /**
     * @return vertical side
     */
    public double getWidth() {
        return width;
    }

    /**
     * Set up the matrix transformation from {@link Bit2D} coordinate system
     * into {@link Pavement}
     */
    private void setTransfoMatrix() {

        transfoMatrix.translate(origin.x, origin.y);
        transfoMatrix.rotate(orientation.x, orientation.y);
        try {
            inverseTransfoMatrix = ((AffineTransform) transfoMatrix.clone()).createInverse();
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
            inverseTransfoMatrix = AffineTransform.getScaleInstance(1, 1); // Fallback
        }
    }

    /**
     * Given an area cut from a zone, construct the surface of this bit
     *
     * @param transformedArea a bit of surface in real
     */
    public void updateBoundaries(@NotNull Area transformedArea) {
        areas.clear();
        Area newArea = (Area) transformedArea.clone();
        if(!checkSectionHoldingToCut(origin,orientation,newArea)){
            removeSectionHolding(this,newArea);
            checkFullLength=false;
        }else if(checkInverseBit(this,newArea)){
            inverseInCut = true;
        }
        newArea.transform(inverseTransfoMatrix);
        Vector<Area> listAreas =AreaTool.segregateArea(newArea);
//        if(listAreas!=null){
//            areas.addAll(listAreas);
//        }else
        areas.addAll(listAreas!=null ? listAreas : new Vector<>());

    }

    private static void removeSectionHolding(Bit2D bit,Area bitArea) {
        Vector<Area> sections=getTwoSectionHolding(bit.origin,bit.orientation,0.0);
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
        Rectangle2D bound = this.getArea().getBounds2D();
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
        Vector2 newOrigin = origin.getTransformed(transformation).getRounded(),
                newOrientation = origin.add(orientation)
                        .getTransformed(transformation)
                        .sub(newOrigin)
                        .normal()
                        .getRounded();
        Bit2D newBit = new Bit2D(newOrigin, newOrientation, length, width);
        newBit.updateBoundaries(this.getArea().createTransformedArea(transformation));
        return newBit;
    }

    /**
     * Reset cut paths and recalculate them after defining area
     */
    public void calcCutPath() {
        // We all calculate in coordinate
        // Reset cut paths
        this.cutPaths = new Vector<>();
        Vector<Vector<Segment2D>> polygons = AreaTool.getSegmentsFrom(this.getRawArea());
        // Define 4 corners
        Vector2 cornerUpRight = new Vector2(+CraftConfig.lengthFull / 2.0, -CraftConfig.bitWidth / 2.0);
        Vector2 cornerDownRight = new Vector2(cornerUpRight.x, cornerUpRight.y + width);
        Vector2 cornerUpLeft = new Vector2(cornerUpRight.x - length, cornerUpRight.y);
        Vector2 cornerDownLeft = new Vector2(cornerDownRight.x - length, cornerDownRight.y);
        // Define 4 sides
        Segment2D sideTop = new Segment2D(cornerUpLeft, cornerUpRight);
        Segment2D sideBottom = new Segment2D(cornerDownLeft, cornerDownRight);
        Segment2D sideRight = new Segment2D(cornerUpRight, cornerDownRight);
        Segment2D sideLeft = new Segment2D(cornerUpLeft, cornerDownLeft);

        AtomicBoolean insideSideRight = new AtomicBoolean(false);
        AtomicBoolean insideSideLeft = new AtomicBoolean(false);
        // Check cut path
        // If and edge lives on sides of the bit
        // We remove it
//        Set<Vector<Segment2D>> listPolygons = new HashSet<>();
        polygons.forEach(polygon -> polygon.removeIf(edge -> sideBottom.contains(edge)||sideLeft.contains(edge)||sideRight.contains(edge)||sideTop.contains(edge)));
//            if (sideLeft.contains(edge)) {
//                insideSideLeft.set(true);
//                return true;
//            } else if (sideBottom.contains(edge) || sideTop.contains(edge)) {
//                return true;
//            }else if(sideRight.contains(edge)){
//                listPolygons.add(polygon);
//            }
//            return false;
//        }));
//
//        if(!insideSideLeft.get()){
//            listPolygons.forEach(polygon -> polygon.removeIf(edge -> {
//                if (sideRight.contains(edge)) {
//                    insideSideRight.set(true);
//                    return true;
//                }
//                return false;
//            }));
//        }
//        if(checkFullLength&&)
//        if(!insideSideLeft.get()&&insideSideRight.get()){
//            inverseInCut =true;
//        }

        // After filter out the edges on sides
        // We form cut paths from these polygons
        // Each polygon may contain multiple cut paths
        for (Vector<Segment2D> polygon : polygons) {
            if (polygon.isEmpty())
                continue;
            Path2D cutPath2D = new Path2D.Double();
            Vector<Path2D> cutPaths2D = new Vector<>();
//            Segment2D currentEdge = polygon.get(0);
//            cutPath2D.moveTo(currentEdge.start.x, currentEdge.start.y);
            Point2D.Double moveToGlobal;
            for (int i = 0; i < polygon.size(); i++) {
                Point2D moveToCurrent;
                Segment2D currentEdge = polygon.get(i);
                if (i == 0 || !(currentEdge.start.asGoodAsEqual(polygon.get(i - 1).end))) {
                    cutPath2D.moveTo(currentEdge.start.x, currentEdge.start.y);
                }
                cutPath2D.lineTo(currentEdge.end.x, currentEdge.end.y);
                // Some edges may have been deleted
                // So we check beforehand to skip
//                if (i + 1 < polygon.size() && currentEdge.getNext()!=null && !polygon.contains(currentEdge.getNext())) {
                // If the next edge has been removed
                // We complete the path
//                    cutPaths2D.add(cutPath2D);
//                    // Then we create a new one
//                    // And move to the start of the succeeding edge
//                    cutPath2D = new Path2D.Double();
//                    cutPath2D.moveTo(polygon.get(i + 1).start.x, polygon.get(i + 1).start.y);
//                }
                if((currentEdge.end.isOnSegment(sideBottom)||currentEdge.end.isOnSegment(sideTop))&&currentEdge.getNext()!=null){
                    cutPath2D.moveTo(currentEdge.end.x,currentEdge.end.y);
                }
            }
            // Finish the last cut path
            if (!cutPaths2D.contains(cutPath2D)) {
                cutPath2D = CutPathUtil.OrganizeOrderCutInPath2D(cutPath2D);
                cutPaths2D.add(cutPath2D);
            }
            this.cutPaths.addAll(cutPaths2D);

//            cutPathsSeparate.add(cutPaths2D);
        }
        CutPathUtil.sortCutPath(this.cutPaths);

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
        oos.writeObject(transfoMatrix);
        oos.writeObject(inverseTransfoMatrix);
        oos.writeBoolean(inverseInCut);
        oos.writeBoolean(checkFullLength);
        // Special writing for areas
        oos.writeObject(AffineTransform.getTranslateInstance(0, 0)
                .createTransformedShape(this.getArea()));
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
        this.transfoMatrix = (AffineTransform) ois.readObject();
        this.inverseTransfoMatrix = (AffineTransform) ois.readObject();
        this.areas = new Vector<>();
        this.checkFullLength = ois.readBoolean();
        this.inverseInCut = ois.readBoolean();
        Shape s = (Shape) ois.readObject();
        this.updateBoundaries(new Area(s));
    }

    public AffineTransform getTransfoMatrix() {
        return transfoMatrix;
    }


    public AffineTransform getInverseTransfoMatrix() {
        return inverseTransfoMatrix;
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
     * return true if the bit can use full length of bit
     * This methode is used after area of bit is transformed in Bit's coordinate
     * @return
     */
    public boolean checkSectionHoldingToCut(){
        Area bitArea = new Area();
        for(Area area : areas){
            bitArea.add(area);
        }
        Vector<Rectangle2D> twoSides = getTwoSideOfBit(CraftConfig.incertitude);
        return bitArea.contains(twoSides.firstElement())||bitArea.contains(twoSides.lastElement());
    }

    /**
     * Return two section holding to cut of the bit without the cut paths
     */
    public static Vector<Area> getTwoSectionHolding(Vector2 position,Vector2 orientation,double incertitude){

        Vector<Area> result = new Vector();
        Vector<Rectangle2D> rectangles = getTwoSideOfBit(incertitude);

        Area leftArea =new Area(rectangles.firstElement());
        Area rightArea = new Area(rectangles.lastElement());

        AffineTransform affineTransform = new AffineTransform();
        affineTransform.translate(position.x,position.y);
        affineTransform.rotate(orientation.x,orientation.y);

        leftArea.transform(affineTransform);
        rightArea.transform(affineTransform);
        result.add(leftArea); result.add(rightArea);

        return result;
    }

    /**
     * Return a list of two sides of the bit dans le coordinate of {@link Bit2D}
     * @param incertitude
     * @return
     */
    public static Vector<Rectangle2D> getTwoSideOfBit(double incertitude){
        Rectangle2D leftArea = new Rectangle2D.Double(-CraftConfig.lengthFull /2+incertitude
                ,-CraftConfig.bitWidth/2+incertitude
                ,CraftConfig.sectionHoldingToCut-2*incertitude
                ,CraftConfig.bitWidth-2*incertitude);
        Rectangle2D rightArea = new Rectangle2D.Double(CraftConfig.lengthFull /2-CraftConfig.sectionHoldingToCut+incertitude
                ,-CraftConfig.bitWidth/2+incertitude
                ,CraftConfig.sectionHoldingToCut-2*incertitude
                ,CraftConfig.bitWidth-2*incertitude);
        Vector<Rectangle2D> result = new Vector<>();
        result.add(leftArea); result.add(rightArea);
        return result;
    }

    /**
     * This static method is used to check if the {@link Bit2D} will be created can use full length of {@link Bit2D}, only for before create {@link Bit2D}
     * @param position position of {@link Bit2D}
     * @param orientation Orientation of {@link Bit2D}
     * @param bitArea Area of {@link Bit2D}, before calculate
     * @return true for full length,
     */
    public static boolean checkSectionHoldingToCut(Vector2 position,Vector2 orientation,Area bitArea){
        Area area = (Area) bitArea.clone();
        Vector<Rectangle2D> twoSides = getTwoSideOfBit(CraftConfig.incertitude);

        AffineTransform affineTransform = new AffineTransform();
        affineTransform.translate(position.x,position.y);
        affineTransform.rotate(orientation.x,orientation.y);

        try {
            AffineTransform inverse = affineTransform.createInverse();
            area.transform(inverse);
            return  !(!area.contains(twoSides.firstElement())&&!area.contains(twoSides.lastElement())
                    &&area.intersects(twoSides.firstElement())&&area.intersects(twoSides.lastElement()));
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
            return false;
        }

    }
    public static boolean checkInverseBit(Bit2D bit2D, Area bitArea){
        Area newBitArea=(Area) bitArea.clone();
        if(!bit2D.isFullLength()){
            return false;
        }
        Vector<Rectangle2D> twoSide = getTwoSideOfBit(CraftConfig.incertitude);
        newBitArea.transform(bit2D.getInverseTransfoMatrix());
        //last element is right side
        return !newBitArea.contains(twoSide.lastElement()) && newBitArea.intersects(twoSide.lastElement());
    }


    public void setCutPaths(Vector<Path2D> cutPaths) {
        this.cutPaths = cutPaths;
    }

    public boolean isUsedForNN() {
        return usedForNN;
    }

    public void setUsedForNN(boolean usedForNN) {
        this.usedForNN = usedForNN;
    }
}