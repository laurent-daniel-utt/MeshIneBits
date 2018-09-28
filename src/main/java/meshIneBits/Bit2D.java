/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 Vallon BENJAMIN.
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

package meshIneBits;

import meshIneBits.config.CraftConfig;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

/**
 * Bit2D represent a bit in 2D : boundaries and cut path. A {@link Bit3D} is
 * build with multiple Bit2D <br>
 * <img src="./doc-files/bit2d.png" alt="">
 *
 * @see Bit3D
 */
public class Bit2D implements Cloneable, Serializable {
    /**
     * In the pattern coordinate system without rotation or translation of whole
     * object.
     */
    private Vector2 origin;
    private Vector2 orientation;
    private double length;
    private double width;
    private AffineTransform transfoMatrix = new AffineTransform();
    private AffineTransform inverseTransfoMatrix;
    private Vector<Path2D> cutPaths = new Vector<>();

    /**
     * A bit should only have one area
     */
    private Vector<Area> areas = new Vector<>();

    /**
     * Constructor to clone an existing bit into a smaller one.
     * <p>
     * All the other parameters remain unchanged in comparison to model bit.
     *
     * @param modelBit         the model
     * @param percentageLength from 0 to 100
     * @param percentageWidth  from 0 to 100
     */
    public Bit2D(Bit2D modelBit, double percentageLength, double percentageWidth) {
        this.origin = modelBit.origin;
        this.orientation = modelBit.orientation;
        length = (CraftConfig.bitLength * percentageLength) / 100;
        width = (CraftConfig.bitWidth * percentageWidth) / 100;

        setTransfoMatrix();
        buildBoundaries();
    }

    /**
     * A new full bit with <tt>originBit</tt> and <tt>orientation</tt> in the
     * coordinate system of the associated pattern
     *
     * @param origin      the center of bit's outer bound
     * @param orientation the rotation of bit
     */
    public Bit2D(Vector2 origin, Vector2 orientation) {
        this.origin = origin;
        this.orientation = orientation;
        length = CraftConfig.bitLength;
        width = CraftConfig.bitWidth;

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
     * Create the area of the bit and set an initial cut path if necessary. This is
     * necessary when the bit has been reduced manually. Note: Oy axe points
     * downward and Ox points to the right. We always take the up right corner as
     * ({@link CraftConfig#bitLength bitLength} / 2, - {@link CraftConfig#bitWidth
     * bitWidth} / 2 ). The bit' boundary is a rectangle.
     */
    private void buildBoundaries() {
        Vector2 cornerUpRight = new Vector2(+CraftConfig.bitLength / 2.0, -CraftConfig.bitWidth / 2.0);
        Vector2 cornerDownRight = new Vector2(cornerUpRight.x, cornerUpRight.y + width);
        Vector2 cornerUpLeft = new Vector2(cornerUpRight.x - length, cornerUpRight.y);
        Vector2 cornerDownLeft = new Vector2(cornerDownRight.x - length, cornerDownRight.y);

        Path2D path = new Path2D.Double();
        path.moveTo(cornerUpRight.x, cornerUpRight.y);
        path.lineTo(cornerDownRight.x, cornerDownRight.y);
        path.lineTo(cornerDownLeft.x, cornerDownLeft.y);
        path.lineTo(cornerUpLeft.x, cornerUpLeft.y);
        path.closePath();

        this.areas.add(new Area(path));

        // Set a cut path if necessary
        if ((length != CraftConfig.bitLength) || (width != CraftConfig.bitWidth)) {
            cutPaths = new Vector<>();
            Path2D.Double cutPath = new Path2D.Double();
            if ((length != CraftConfig.bitLength) && (width != CraftConfig.bitWidth)) {
                cutPath.moveTo(cornerDownLeft.x, cornerDownLeft.y);
                cutPath.lineTo(cornerUpLeft.x, cornerUpLeft.y);
                cutPath.lineTo(cornerUpRight.x, cornerUpRight.y);
            } else if (length != CraftConfig.bitLength) {
                cutPath.moveTo(cornerDownLeft.x, cornerDownLeft.y);
                cutPath.lineTo(cornerUpLeft.x, cornerUpLeft.y);
            } else if (width != CraftConfig.bitWidth) {
                cutPath.moveTo(cornerUpLeft.x, cornerUpLeft.y);
                cutPath.lineTo(cornerUpRight.x, cornerUpRight.y);
            }
            this.cutPaths.add(cutPath);
        }
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Bit2D clone() {
        return new Bit2D(origin, orientation, length, width, (AffineTransform) transfoMatrix.clone(),
                (AffineTransform) inverseTransfoMatrix.clone(), getClonedRawCutPaths(), getClonedRawAreas());
    }

    /**
     * @return the union of all surfaces making this bit transformed by
     * <tt>transfoMatrix</tt>
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
     * layer)
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
     * @return orientation in coordinates system of layer
     */
    public Vector2 getOrientation() {
        return orientation;
    }

    /**
     * @return the origin in the pattern coordinate system
     */
    public Vector2 getOrigin() {
        return origin;
    }

    /**
     * @return the center of the rectangle of this bit, not necessarily the
     * {@link #origin origin}
     */
    public Vector2 getCenter() {
        double verticalDistance = -(CraftConfig.bitWidth - width) / 2,
                horizontalDistance = (CraftConfig.bitLength - length) / 2;
        return origin.add(new Vector2(horizontalDistance, verticalDistance));
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
     * Incorporate new cut paths into bit
     *
     * @param paths measured in coordinate system of layer
     */
    @SuppressWarnings("unused")
    public void setCutPath(Vector<Path2D> paths) {
        if (this.cutPaths == null) {
            this.cutPaths = new Vector<>();
        }

        for (Path2D p : paths) {
            this.cutPaths.add(new Path2D.Double(p, inverseTransfoMatrix));
        }
    }

    /**
     * Set up the matrix transformation from bit into coordinate system of layer
     */
    private void setTransfoMatrix() {

        transfoMatrix.translate(origin.x, origin.y);
        transfoMatrix.rotate(orientation.x, orientation.y);

        try {
            inverseTransfoMatrix = ((AffineTransform) transfoMatrix.clone()).createInverse();
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    /**
     * Given an area cut from a zone, construct the surface of this bit
     *
     * @param transformedArea a bit of surface
     */
    public void updateBoundaries(Area transformedArea) {
        areas.clear();
        Area newArea = (Area) transformedArea.clone();
        newArea.transform(inverseTransfoMatrix);
        areas.addAll(AreaTool.segregateArea(newArea));
    }

    // /**
    // * @return a set of lift points, each of which is in charge of each separated
    // * area (in case a bit has many separated areas)
    // */

    /**
     * A bit should only have one lift point
     *
     * @return <tt>null</tt> if this bit has multiple separated areas or no area.
     * The lift point is calculated in coordinate system of layer
     */
    public Vector2 computeLiftPoint() {
        if (areas.size() != 1)
            return null;
        return AreaTool.getLiftPoint(this.getArea(), CraftConfig.suckerDiameter / 2);
        // Vector<Vector2> result = new Vector<Vector2>();
        // for (Area area : areas) {
        // Vector2 localLiftPoint = AreaTool.getLiftPoint(area,
        // CraftConfig.suckerDiameter / 2);
        // if (localLiftPoint != null) {
        // result.add(localLiftPoint);
        // }
        // }
        // return result;
    }

    /**
     * To resize a bit, keeping up-right corner as reference.
     *
     * @param newPercentageLength 100 means retain 100% of old bit's length
     * @param newPercentageWidth  100 means retain 100% of old bit's width
     */
    public void resize(double newPercentageLength, double newPercentageWidth) {
        length = length * newPercentageLength / 100;
        width = width * newPercentageWidth / 100;
        // Rebuild the boundary
        buildBoundaries();
    }

    @Override
    public String toString() {
        return "Bit2D [origin=" + origin + ", length=" + length + ", width=" + width + ", orientation=" + orientation
                + "]";
    }

    /**
     * This method only accepts the conservative transformation (no scaling). The
     * coordinates are rounded by {@link CraftConfig#errorAccepted} to accelerate
     * calculation.
     *
     * @param transformation a combination of affine transformation
     * @return a new bit with same geometric with initial one transformed by
     * <tt>transfoMatrix</tt>
     */
    public Bit2D createTransformedBit(AffineTransform transformation) {
        Vector2 newOrigin = origin.getTransformed(transformation).getRounded(), newOrientation = origin.add(orientation)
                .getTransformed(transformation).sub(newOrigin).normal().getRounded();
        Bit2D newBit = new Bit2D(newOrigin, newOrientation, length, width);
        newBit.updateBoundaries(this.getArea().createTransformedArea(transformation));
        return newBit;
    }

    /**
     * Reset cut paths and recalculate them after defining area
     */
    void calcCutPath() {
        // We all calculate in coordinate
        // Reset cut paths
        this.cutPaths = new Vector<>();
        Vector<Vector<Segment2D>> polygons = AreaTool.getSegmentsFrom(this.getRawArea());
        // Define 4 corners
        Vector2 cornerUpRight = new Vector2(+CraftConfig.bitLength / 2.0, -CraftConfig.bitWidth / 2.0);
        Vector2 cornerDownRight = new Vector2(cornerUpRight.x, cornerUpRight.y + width);
        Vector2 cornerUpLeft = new Vector2(cornerUpRight.x - length, cornerUpRight.y);
        Vector2 cornerDownLeft = new Vector2(cornerDownRight.x - length, cornerDownRight.y);
        // Define 4 sides
        Segment2D sideTop = new Segment2D(cornerUpLeft, cornerUpRight);
        Segment2D sideBottom = new Segment2D(cornerDownLeft, cornerDownRight);
        Segment2D sideRight = new Segment2D(cornerUpRight, cornerDownRight);
        Segment2D sideLeft = new Segment2D(cornerUpLeft, cornerDownLeft);
        // Check cut path
        // If and edge lives on sides of the bit
        // We remove it
        polygons.forEach(polygon -> polygon.removeIf(edge -> sideTop.contains(edge) || sideBottom.contains(edge)
                || sideRight.contains(edge) || sideLeft.contains(edge)));
        // After filter out the edges on sides
        // We form cut paths from these polygons
        // Each polygon may contain multiple cut paths
        for (Vector<Segment2D> polygon : polygons) {
            if (polygon.isEmpty())
                continue;
            Path2D cutPath2D = new Path2D.Double();
            Segment2D currentEdge = polygon.get(0);
            cutPath2D.moveTo(currentEdge.start.x, currentEdge.start.y);
            for (int i = 0; i < polygon.size(); i++) {
                currentEdge = polygon.get(i);
                cutPath2D.lineTo(currentEdge.end.x, currentEdge.end.y);
                // Some edges may have been deleted
                // So we check beforehand to skip
                if (i + 1 < polygon.size() && !polygon.contains(currentEdge.getNext())) {
                    // If the next edge has been removed
                    // We complete the path
                    this.cutPaths.add(cutPath2D);
                    // Then we create a new one
                    // And move to the start of the succeeding edge
                    cutPath2D = new Path2D.Double();
                    cutPath2D.moveTo(polygon.get(i + 1).start.x, polygon.get(i + 1).start.y);
                }
            }
            // Finish the last cut path
            if (!this.cutPaths.contains(cutPath2D)) {
                this.cutPaths.add(cutPath2D);
            }
        }
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
        Shape s = (Shape) ois.readObject();
        this.updateBoundaries(new Area(s));
    }
}