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
import meshIneBits.util.Vector2;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * A bit 3D is the equivalent of a real wood bit. The 3D shape is determined by
 * extrusion of a {@link Bit2D}
 */
public class Bit3D implements Serializable, Cloneable {
    private static final String TAG = "Bit3D";
    /**
     * In {@link #bit2dToExtrude} coordinate system
     */
    private Vector<Path2D> rawCutPaths;
    private LinkedList<Vector<Path2D>> rawCutPathsSeparate;
    /**
     * In {@link Mesh} coordinate system
     */
    private Vector2 origin;
    /**
     * In {@link Mesh} coordinate system
     */
    private Vector2 orientation;
    private Bit2D bit2dToExtrude;
    /**
     * In {@link #bit2dToExtrude} coordinate system
     */
    private Vector<Vector2> rawLiftPoints = new Vector<>();
    /**
     * In {@link Mesh} coordinate system
     */
    private Vector<Vector2> liftPoints = new Vector<>();
    private boolean irregular = false;
    private double lowerAltitude;
    private double higherAltitude;

    /**
     * Construct bit 3D from horizontal section and calculate lift points
     *
     * @param baseBit horizontal cut
     * @param layer in which this bit resides
     */
    Bit3D(Bit2D baseBit, Layer layer) {
        bit2dToExtrude = baseBit;
        origin = baseBit.getOrigin();
        orientation = baseBit.getOrientation();
        rawCutPaths = baseBit.getRawCutPaths();
        rawCutPathsSeparate=baseBit.getCutPathsSeparate();
        computeLiftPoints();
        lowerAltitude = layer.getLowerAltitude();
        higherAltitude = layer.getHigherAltitude();
    }
    Bit3D(Bit3D bit3D){
        bit2dToExtrude = bit3D.getBaseBit();
        origin = bit3D.getOrigin();
        orientation = bit3D.getOrientation();
        rawCutPaths = (Vector<Path2D>) bit3D.getRawCutPaths();
        computeLiftPoints();
        lowerAltitude = bit3D.getLowerAltitude();
        higherAltitude = bit3D.getHigherAltitude();
    }

    private Vector2 computeLiftPoint(Area subBit) {
//        System.out.println(TAG+"computeLiftPoint");
        return AreaTool.getLiftPoint(subBit, CraftConfig.suckerDiameter / 2);
    }

    /**
     * Calculate the lift point, which is the best position to grip
     * the bit by vacuuming.
     */
    private void computeLiftPoints() {
//        System.out.println(TAG+"computeLiftPoint void");
        for (Area subBit : bit2dToExtrude.getRawAreas()) {
            Vector2 liftPoint = computeLiftPoint(subBit);
            if (liftPoint != null) {
                rawLiftPoints.add(liftPoint);
                liftPoints.add(liftPoint.getTransformed(bit2dToExtrude.getTransfoMatrix()));
            } else {
                irregular = true;
            }
        }
    }

    public List<Path2D> getRawCutPaths() {
        return rawCutPaths;
    }

    public List<Vector2> getLiftPoints() {
        return liftPoints;
    }

    public List<Vector2> getRawLiftPoints() {
        return rawLiftPoints;
    }

    public Vector2 getOrientation() {
        return orientation;
    }

    public Vector2 getOrigin() {
        return origin;
    }

    public Area getRawArea() {
        return bit2dToExtrude.getRawArea();
    }

    public Bit2D getBaseBit() {
        return bit2dToExtrude;
    }

    public boolean isIrregular() {
        return irregular;
    }

    @Override
    public String toString() {
        Rectangle2D bound = bit2dToExtrude.getArea().getBounds2D();
        return "Bit3D[" +
                "origin=" + origin +
                ", orientation=" + orientation +
                ", width=" + bit2dToExtrude.getLength() +
                ", height=" + bit2dToExtrude.getWidth() +
                ", liftPoints=" + liftPoints +
                ", irregular=" + irregular +
                ", areaBound=" +
                "[x=" + bound.getX() +
                ", y=" + bound.getY() +
                ", w=" + bound.getWidth() +
                ", h=" + bound.getHeight() +
                "]" +
                ']';
    }
    public boolean isCutable() {
        return this.getRawLiftPoints().size() > 0;
    }

    public double getLowerAltitude() {
        return lowerAltitude;
    }

    public double getHigherAltitude() {
        return higherAltitude;
    }

    public Bit3D clone() {
        return new Bit3D(this);
    }

    public LinkedList<Vector<Path2D>> getRawCutPathsSeparate() {
        return rawCutPathsSeparate;
    }
}
