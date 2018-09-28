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

package meshIneBits.util;

import meshIneBits.config.CraftConfig;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Vector;

/**
 * Vector2 represent a point in a 2D space
 */
public class Vector2 implements Serializable {
    public final double x, y;

    public static double dist2(Vector2 v, Vector2 w) {
        return (((v.x - w.x) * (v.x - w.x)) + ((v.y - w.y) * (v.y - w.y)));
    }

    public static double dist(Vector2 v, Vector2 w) {
        return Math.sqrt(dist2(v, w));
    }

    //

    /**
     * @param angleDegrees in degrees
     * @return The orientation vector equivalent to the given angle
     */
    public static Vector2 getEquivalentVector(double angleDegrees) {
        double angleRadian = Math.toRadians(angleDegrees);
        return new Vector2(Rounder.round(Math.cos(angleRadian), CraftConfig.errorAccepted),
                Rounder.round(Math.sin(angleRadian), CraftConfig.errorAccepted));
    }

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
        if (Double.isNaN(x) || Double.isNaN(y)) {
            throw new RuntimeException("Vector has NaN component...");
        }
    }

    public Vector2 add(Vector2 v) {
        return new Vector2(x + v.x, y + v.y);
    }

    /**
     * Accelerating the comparison
     *
     * @param v approximation
     * @return <tt>true</tt> if inferior to 10E({@link CraftConfig#errorAccepted errorAccepted})
     */
    public boolean asGoodAsEqual(Vector2 v) {
        return (Math.abs(x - v.x) + Math.abs(y - v.y)) < Math.pow(10, -CraftConfig.errorAccepted);
    }

    /**
     * Return the vector mirrored on the Z axis
     *
     * @return reflection
     */
    public Vector2 crossZ() {
        return new Vector2(y, -x);
    }

    /**
     * Return the vector divided by a double
     *
     * @param f scale not null
     * @return a scaled vector
     */
    public Vector2 div(double f) {
        return new Vector2(x / f, y / f);
    }

    /**
     * Return the scalar of two vector
     *
     * @param v can be identical to itself
     * @return scale product
     */
    public double dot(Vector2 v) {
        return (x * v.x) + (y * v.y);
    }

    /**
     * Value between 0 - 180
     *
     * @return The angle between Ox and vector.
     */
    public double getEquivalentAngle() {
        return (Math.atan(y / x) * 180) / Math.PI;
    }

    /**
     * Value between 0 - 360
     *
     * @return The angle between Ox and vector.
     */
    public double getEquivalentAngle2() {
        return (Math.atan2(y, x) * 180) / Math.PI;
    }

    public Vector2 getInLowerCooSystem(Vector2 myOrientation, Vector2 myOrigin) {
        Vector2 orientation = myOrientation.normal();
        double angleRotation = Math.acos(orientation.x);
        double computedX = ((x * Math.cos(angleRotation)) - (y * Math.sin(angleRotation))) + myOrigin.x;
        double computedY = (x * Math.sin(angleRotation)) + (y * Math.cos(angleRotation)) + myOrigin.y;
        return new Vector2(computedX, computedY);
    }

    public Vector2 getTransformed(AffineTransform transfoMatrix) {
        Point2D.Double point = new Point2D.Double(this.x, this.y);
        Point2D.Double transformedPoint = (java.awt.geom.Point2D.Double) transfoMatrix.transform(point, null);
        return new Vector2(transformedPoint.getX(), transformedPoint.getY());
    }

    public boolean isOnPath(Polygon poly) {
        for (Segment2D s : poly) {
            if (this.isOnSegment(s)) {
                return true;
            }
        }

        return false;
    }

    /*
     * Taken from http://stackoverflow.com/a/17590923
     */
    public boolean isOnPath(Vector<Segment2D> segmentList) {
        for (Segment2D s : segmentList) {
            if (this.isOnSegment(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check the linearity, using approximate triangular inequality
     * <tt>c &le; a + b &le; c + 10^(-{@link CraftConfig#errorAccepted error})</tt>.
     *
     * @param segment <tt>c</tt> in inequality above
     * @return <tt>true</tt> if close enough
     * @see CraftConfig#errorAccepted
     */
    public boolean isOnSegment(Segment2D segment) {
        double x1 = segment.start.x;
        double y1 = segment.start.y;
        double x2 = segment.end.x;
        double y2 = segment.end.y;

        double AB = Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
        double AP = Math.sqrt(((x - x1) * (x - x1)) + ((y - y1) * (y - y1)));
        double PB = Math.sqrt(((x2 - x) * (x2 - x)) + ((y2 - y) * (y2 - y)));

        if (AB <= (AP + PB) && (AP + PB) <= AB + Math.pow(10, -CraftConfig.errorAccepted)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return the vector multiplied by a double
     *
     * @param f nullable
     * @return scaled vector
     */
    public Vector2 mul(double f) {
        return new Vector2(x * f, y * f);
    }

    /**
     * Returns a normalized vector with a length of 1, having the same direction as
     * the original vector. Note: if the length is smaller than
     * 10^({@link meshIneBits.config.CraftConfig#errorAccepted -errorAccepted}) then
     * the returned result will 0.
     *
     * @return normalized vector
     */
    public Vector2 normal() {
        double d = vSize();
        if (d < Math.pow(10, -CraftConfig.errorAccepted)) {
            return new Vector2(0, 0);
        }
        return new Vector2(x / d, y / d);
    }

    public Vector2 sub(Vector2 v) {
        return new Vector2(x - v.x, y - v.y);
    }

    @Override
    public String toString() {
        return "(" + x + ";" + y + ")";
    }

    /**
     * Returns the length of the vector.
     *
     * @return euclidean length
     */
    public double vSize() {
        return Math.sqrt((x * x) + (y * y));
    }

    /**
     * Returns the squared length of the vector (faster then vSize())
     *
     * @return euclidean norm
     */
    public double vSize2() {
        return (x * x) + (y * y);
    }

    /**
     * @return the vector after 90° CW rotation.
     */
    public Vector2 getCWAngularRotated() {
        return new Vector2(-y, x);
    }

    /**
     * @return the opposite vector
     */
    public Vector2 getOpposite() {
        return new Vector2(-x, -y);
    }

    /**
     * @return a new vector with x and y rounded following
     * {@link CraftConfig#errorAccepted}
     */
    public Vector2 getRounded() {
        return new Vector2(Rounder.round(x, CraftConfig.errorAccepted), Rounder.round(y, CraftConfig.errorAccepted));
    }

    /**
     * Special tools for vectors.
     *
     * @author NHATHAN
     */
    public static class Tools {
        /**
         * Check if 2 points P1, P2 are on different sides in comparison to the line
         *
         * @param p1   first point
         * @param p2   second point
         * @param line anywhere on same plan
         * @return <tt>true</tt> if directional product is negative
         */
        static boolean checkOnDifferentSides(Vector2 p1, Vector2 p2, Segment2D line) {
            // Construct the equation of the line
            Vector2 d = line.end.sub(line.start);// directional vector
            Vector2 n = (new Vector2(-d.y, d.x)).normal();// normal vector
            // Equation: n * (v - start) = 0 with v = (x,y)
            if ((n.dot(p1.sub(line.start))) * (n.dot(p2.sub(line.start))) < 0) {
                return true;
            }
            return false;
        }

        /**
         * Calculate the distance from a point to the a line (or a segment)
         *
         * @param point target
         * @param line  not reduced to null
         * @return height from point to line
         */
        public static double distanceFromPointToLine(Vector2 point, Segment2D line) {
            Vector2 d = line.end.sub(line.start);// directional vector
            Vector2 n = (new Vector2(-d.y, d.x)).normal();// normal vector
            // Equation: n * (v - start) = 0 with v = (x,y)
            double nLength = n.vSize();
            if (nLength == 0) {
                return 0;
            } else {
                return Math.abs(n.dot(point.sub(line.start))) / nLength;
            }
        }

        /**
         * Find the vector whose origin is point p, moving perpendicularly away from
         * (origin, v) and length is 1
         *
         * @param o origin of departure of v
         * @param v vector of direction departing from origin
         * @param p point of depart
         * @return null if p is on the line passing by o with v as directing vector
         */
        public static Vector2 getCentrifugalVector(Vector2 o, Vector2 v, Vector2 p) {
            Vector2 v2 = v.getCWAngularRotated();
            if (v2.dot(p.sub(o)) == 0) {
                return null;
            } else {
                // Create a segment as border
                Segment2D border = new Segment2D(o, o.add(v));
                Vector2 samplePoint = o.add(v2);
                if (!Vector2.Tools.checkOnDifferentSides(samplePoint, p, border)) {
                    return v2;
                } else {
                    return v2.getOpposite();
                }
            }
        }
    }

}
