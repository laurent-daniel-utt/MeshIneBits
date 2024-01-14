/*
 * MeshIneBits is a Java software to disintegrate a 3d project (model in .stl)
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

package meshIneBits.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import meshIneBits.config.CraftConfig;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Vector;

/**
 * Vector2 represent a point in a 2D space
 */
public class Vector2 implements Serializable {

  public final double x, y;

  @JsonIgnore
  public Vector2(double x, double y) {
    this.x = x;
    this.y = y;
    if (Double.isNaN(x) || Double.isNaN(y)) {
      throw new RuntimeException("Vector has NaN component...");
    }
  }

  @JsonIgnore
  public static double dist2(Vector2 v, Vector2 w) {
    return (((v.x - w.x) * (v.x - w.x)) + ((v.y - w.y) * (v.y - w.y)));
  }

  //
  @JsonIgnore
  public static double dist(Vector2 v, Vector2 w) {
    return Math.sqrt(dist2(v, w));
  }

    /**
     * @param angleDegrees in degrees
     * @return The orientation vector equivalent to the given angle
     */
    @JsonIgnore
    public static Vector2 getEquivalentVector(double angleDegrees) {
        double angleRadian = Math.toRadians(angleDegrees);
        return new Vector2(Rounder.round(Math.cos(angleRadian), CraftConfig.errorAccepted),
                Rounder.round(Math.sin(angleRadian), CraftConfig.errorAccepted));
    }
    @JsonIgnore
    public static double calcAngleBetweenVectorAndAxeX(Vector2 v) {
        double angle = calcAngleBetweenTwoVector(v, new Vector2(1, 0)) * 180 / Math.PI;
        return v.y > 0 ? angle : -angle;}


    /**
     * Calculate angle in radian between 2 vector
     *
     * @param point1 coordinate vector
     * @param point2 coordinate vector
     * @return angle in radian
     */
    @JsonIgnore
    public static double calcAngleBetweenTwoVector(Vector2 point1, Vector2 point2) {
        double num = (point1.x * point2.x + point1.y * point2.y);
        double den = (Math.sqrt(Math.pow(point1.x, 2) + Math.pow(point1.y, 2)) * (Math.sqrt(Math.pow(point2.x, 2) + Math.pow(point2.y, 2))));
        double cos = num / den;
        return Math.acos(cos);
    }
  @JsonIgnore
  public static void main(String[] args) {
    //Vector2 point1 = new Vector2(1, -1);

        Vector2 point2 = new Vector2(1, 1);
        System.out.println(calcAngleBetweenVectorAndAxeX(point2));
        point2= point2.rotate(Vector2.getEquivalentVector(90));
    System.out.println(calcAngleBetweenVectorAndAxeX(point2));


//        System.out.println(point1.y > 0 ? calcAngleBetweenVectorAndAxeX(point1) * 180 / Math.PI : -calcAngleBetweenVectorAndAxeX(point1) * 180 / Math.PI);
    }
  @JsonIgnore
  public Vector2 add(Vector2 v) {
    return new Vector2(x + v.x, y + v.y);
  }

  /**
   * Accelerating the comparison
   *
   * @param v approximation
   * @return <tt>true</tt> if inferior to 10E({@link CraftConfig#errorAccepted errorAccepted})
   */
  @JsonIgnore
  public boolean asGoodAsEqual(Vector2 v) {
    return (Math.abs(x - v.x) + Math.abs(y - v.y)) < Math.pow(10, -CraftConfig.errorAccepted);
  }

  /**
   * Return the vector mirrored on the Z axis
   *
   * @return reflection
   */
  @SuppressWarnings("all")
  @JsonIgnore
  public Vector2 crossZ() {
    return new Vector2(y, -x);
  }

  /**
   * Return the vector divided by a double
   *
   * @param f scale not null
   * @return a scaled vector
   */
  @JsonIgnore
  public Vector2 div(double f) {
    return new Vector2(x / f, y / f);
  }

  /**
   * Return the scalar of two vector
   *
   * @param v can be identical to itself
   * @return scale product
   */
  @JsonIgnore
  public double dot(Vector2 v) {
    return (x * v.x) + (y * v.y);
  }

  /**
   * Value between 0 - 180 //TODO: 2021-01-17 the value returned is actually between -90 and 90,
   * this has to be corrected
   *
   * @return The angle between Ox and vector.
   */
  @JsonIgnore
  public double getEquivalentAngle() {
    return Math.toDegrees((Math.atan(y / x)));// TODO: 2021-01-17 I think it's better ?
    //return (Math.atan(y / x) * 180) / Math.PI;
  }

  /**
   * Value between 0 - 360 //TODO: 2021-01-17 the value returned is actually between -180 and 180,
   * this has to be corrected
   *
   * @return The angle between Ox and vector.
   */
  @JsonIgnore
  public double getEquivalentAngle2() {
    return Math.toDegrees(Math.atan2(y, x)); // TODO: 2021-01-17 I think it's better ?
    //return (Math.atan2(y, x) * 180) / Math.PI;
  }

  @JsonIgnore
  public static double getAngle(Vector2 A, Vector2 B, Vector2 C) {
    double val = (dist2(B, A) + dist2(B, C) - dist2(A, C))
        / (2 * (dist(B, A) * dist(B, C)));
        val = Math.round(val * 1e10) / (1e10);
        double angle = Math.acos(val);
    return Math.toDegrees(angle);
  }

  /**
     *
     * @param additionalRotation
     * @return
     */
  @JsonIgnore
    public Vector2 rotate(Vector2 additionalRotation) {
        return new Vector2(
                this.x * additionalRotation.x - this.y * additionalRotation.y,
                this.x * additionalRotation.y + this.y * additionalRotation.x
        );
    }

  @JsonIgnore
  public Vector2 getTransformed(AffineTransform transfoMatrix) {
    Point2D.Double point = new Point2D.Double(this.x, this.y);
    Point2D.Double transformedPoint = (Point2D.Double) transfoMatrix.transform(point,
        null);
    return new Vector2(transformedPoint.getX(), transformedPoint.getY());
  }

  @JsonIgnore
  public boolean isOnPath(Polygon poly) {
    for (Segment2D s : poly) {
      if (this.isOnSegment(s)) {
        return true;
      }
    }

        return false;
    }

  @JsonIgnore
    public boolean isOnArea(Area area) {
        Vector<double[]> points = AreaTool.getListPointBorderOfArea(area);
        for (double[] point : points) {
            if (this.asGoodAsEqual(new Vector2(point[1], point[2]))) {
                return true;
            }
        }
        return false;
    }

  /*
   * Taken from http://stackoverflow.com/a/17590923
   */
  @JsonIgnore
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
  @JsonIgnore
  public boolean isOnSegment(Segment2D segment) {
    double x1 = segment.start.x;
    double y1 = segment.start.y;
    double x2 = segment.end.x;
    double y2 = segment.end.y;

        double AB = Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
        double AP = Math.sqrt(((x - x1) * (x - x1)) + ((y - y1) * (y - y1)));
        double PB = Math.sqrt(((x2 - x) * (x2 - x)) + ((y2 - y) * (y2 - y)));
        //return AB - Math.pow(10, -CraftConfig.errorAccepted)<= (AP + PB) && (AP + PB) <= AB + Math.pow(10, -CraftConfig.errorAccepted);
       return AB - Math.pow(10, -2.5)<= (AP + PB) && (AP + PB) <= AB + Math.pow(10, -2.5);
  }



  /**
   * Return the vector multiplied by a double
   *
   * @param f nullable
   * @return scaled vector
   */
  @JsonIgnore
  public Vector2 mul(double f) {
    return new Vector2(x * f, y * f);
  }

  /**
   * Returns a normalized vector with a length of 1, having the same direction as the original
   * vector. Note: if the length is smaller than 10^({@link CraftConfig#errorAccepted
   * -errorAccepted}) then the returned result will 0.
   *
   * @return normalized vector
   */
  @JsonIgnore
  public Vector2 normal() {
    double d = vSize();
    if (d < Math.pow(10, -CraftConfig.errorAccepted)) {
      return new Vector2(0, 0);
    }
    return new Vector2(x / d, y / d);
  }

  @JsonIgnore
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
  @JsonIgnore
  public double vSize() {
    return Math.sqrt((x * x) + (y * y));
  }

  /**
   * Returns the squared length of the vector (faster then vSize())
   *
   * @return euclidean norm
   */
  @JsonIgnore
  public double vSize2() {
    return (x * x) + (y * y);
  }

  /**
   * @return the vector after 90° CW rotation.
   */
  @SuppressWarnings("all")
  @JsonIgnore
  public Vector2 getCWAngularRotated() {
    return new Vector2(-y, x);
  }

  /**
   * @return the opposite vector
   */
  @JsonIgnore
  public Vector2 getOpposite() {
    return new Vector2(-x, -y);
  }

  /**
   * @return a new vector with x and y rounded following {@link CraftConfig#errorAccepted}
   */
  @JsonIgnore
  public Vector2 getRounded() {
    return new Vector2(Rounder.round(x, CraftConfig.errorAccepted),
        Rounder.round(y, CraftConfig.errorAccepted));
  }

  /**
   * <code>(a,b) v (c,d) = ad - bc</code>
   *
   * @param that other vector
   * @return mimic vector product
   */
  @JsonIgnore
  public Double v(Vector2 that) {
    return x * that.y - y * that.x;
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

    public static boolean checkOnDifferentSides(Vector2 p1, Vector2 p2, Segment2D line) {
            // Construct the equation of the line
            Vector2 d = line.end.sub(line.start);// directional vector
            Vector2 n = (new Vector2(-d.y, d.x)).normal();// normal vector
            // Equation: n * (v - start) = 0 with v = (x,y)
            return (n.dot(p1.sub(line.start))) * (n.dot(p2.sub(line.start))) < 0;
        }

    /**
     * Calculate the distance from a point to the a line (or a segment)
     *
     * @param point target
     * @param line  not reduced to null
     * @return height from point to line
     */
    @JsonIgnore
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
     * Find the vector whose origin is point p, moving perpendicularly away from (origin, v) and
     * length is 1
     *
     * @param o origin of departure of v
     * @param v vector of direction departing from origin
     * @param p point of depart
     * @return normalized vector
     */
    @JsonIgnore
    public static Vector2 getCentrifugalVector(Vector2 o, Vector2 v, Vector2 p) {
      Vector2 v2 = v.getCWAngularRotated();
      // Create a segment as border
      Segment2D border = new Segment2D(o, o.add(v));
      Vector2 samplePoint = o.add(v2);
      if (!Tools.checkOnDifferentSides(samplePoint, p, border)) {
        return v2.normal();
      } else {
        return v2.getOpposite()
            .normal();
      }
    }
  }

  @JsonIgnore
    public Vector2 clone() {
        return new Vector2(this.x, this.y);
    }

}
