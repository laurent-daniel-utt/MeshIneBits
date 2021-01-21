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

import meshIneBits.artificialIntelligence.GeneralTools;

import java.io.Serializable;
import java.util.Vector;

/**
 * Segment2D represents a line in 2D space.
 */
public class Segment2D extends AABBrect implements Serializable {
    public Vector2 start;

    public Vector2 end;
    private Vector2 normal;
    private Segment2D next, prev;

    /**
     * Sort segments by paths
     *
     * @param segments target
     * @return disintegrating segments
     */
    @SuppressWarnings("unchecked")
    public static Vector<Vector<Segment2D>> segregateSegments(Vector<Segment2D> segments) {
        Vector<Vector<Segment2D>> segregateSegments = new Vector<>();
        Vector<Segment2D> currentLine = new Vector<>();

        Segment2D nextSegment;

        for (int i = 0; i < segments.size(); i++) {

            if (i < (segments.size() - 1)) {
                nextSegment = segments.get(i + 1);
            } else {
                nextSegment = segments.get(0);
            }

            Segment2D currentSegment = segments.get(i);
            currentLine.add(currentSegment);
            if (!currentSegment.end.asGoodAsEqual(nextSegment.start)) {
                segregateSegments.add((Vector<Segment2D>) currentLine.clone());
                currentLine.clear();
            }
        }
        if (!currentLine.isEmpty()) {
            if (!segregateSegments.isEmpty()) {
                currentLine.addAll(segregateSegments.get(0));
                segregateSegments.set(0, currentLine);
            } else {
                segregateSegments.add(currentLine);
            }
        }
        return segregateSegments;
    }

    public Segment2D(Vector2 start, Vector2 end) {
        // Make the AABB 1mm larger then the actual segment, to account for
        // inaccuracies and moving
        // around the segment ends a bit.
        super(start, end, 1.0);

        update(start, end);
    }

    /**
     * Checks if two segments intersects
     * @param AB  the first segment
     * @param CD  the second segment
     * @return  true if the two segments intersects. false otherwise
     */
    public static boolean doSegmentsIntersect(Segment2D AB, Segment2D CD) {
        Vector2 A = AB.start;
        Vector2 B = AB.end;
        Vector2 C = CD.start;
        Vector2 D = CD.end;

        // evident situations
        if (A.asGoodAsEqual(C) || A.asGoodAsEqual(D)) {
            return true;
        }
        if (B.asGoodAsEqual(C) || B.asGoodAsEqual(D)) {
            return true;
        }
        if (GeneralTools.isPointOnSegment(A, CD)){
            return true;
        }
        if (GeneralTools.isPointOnSegment(B, CD)){
            return true;
        }
        if (GeneralTools.isPointOnSegment(C, AB)){
            return true;
        }
        if (GeneralTools.isPointOnSegment(D, AB)){
            return true;
        }

        double DAC = Vector2.getAngle(D, A, C);
        double ACB = Vector2.getAngle(A, C, B);
        double CBD = Vector2.getAngle(C, B, D);
        double BDA = Vector2.getAngle(B, D, A);


        double sum = Math.abs(DAC) + Math.abs(ACB) + Math.abs(CBD) + Math.abs(BDA);

        // if sum is not 2pi, then ABCD is a complex quadrilateral (2 edges cross themselves).
        // This means that segments intersect
        double errorThreshold = 0.1;
        return Math.abs(360 - sum) < errorThreshold;
    }

    public static Vector2 getIntersectionPoint(Segment2D AB, Segment2D CD) {
        // points
        Vector2 A = AB.start;
        Vector2 B = AB.end;
        Vector2 C = CD.start;
        Vector2 D = CD.end;

        // particular cases
        if (A.asGoodAsEqual(C) || A.asGoodAsEqual(D)) {
            return A;
        }
        if (B.asGoodAsEqual(C) || B.asGoodAsEqual(D)) {
            return B;
        }

        if (GeneralTools.isPointOnSegment(A, CD)){
            return A;
        }
        if (GeneralTools.isPointOnSegment(B, CD)){
            return B;
        }
        if (GeneralTools.isPointOnSegment(C, AB)){
            return C;
        }
        if (GeneralTools.isPointOnSegment(D, AB)){
            return D;
        }

        if (doSegmentsIntersect(AB, CD)) {

            double AD = Vector2.dist(A, D);
            double AID = 180 - Vector2.getAngle(D, A, B) - Vector2.getAngle(C, D, A);

            double IA = (AD / Math.sin(Math.toRadians(AID))) * Math.sin(Math.toRadians(Vector2.getAngle(A, D, C)));

            return A.add(B.sub(A).normal().mul(IA));
        }
        return null;
    }

    public double distFromPoint(Vector2 p) {
        Vector2 v = this.start;
        Vector2 w = this.end;
        double l2 = Vector2.dist2(v, w);
        if (l2 == 0) {
            return Vector2.dist2(p, v);
        }
        double t = (((p.x - v.x) * (w.x - v.x)) + ((p.y - v.y) * (w.y - v.y))) / l2;
        t = Math.max(0, Math.min(1, t));

        return Math.sqrt(Vector2.dist2(p, new Vector2(v.x + (t * (w.x - v.x)), v.y + (t * (w.y - v.y)))));
    }

    public double getLength() {
        return Math.sqrt(((end.x - start.x) * (end.x - start.x)) + ((end.y - start.y) * (end.y - start.y)));
    }

    public Vector2 getMidPoint() {
        return this.getPointAtRatio(0.5);
    }

    public Segment2D getNext() {
        return next;
    }

    public Vector2 getNormal() {
        return normal;
    }

    /**
     * Return the position of the point at a given ratio
     */
    private Vector2 getPointAtRatio(double ratio) {
        return new Vector2(this.start.x + ((this.end.x - this.start.x) * ratio),
                this.start.y + ((this.end.y - this.start.y) * ratio));
    }

    public Segment2D getPrev() {
        return prev;
    }

    public void setNext(Segment2D newNext) {
        if (newNext == null) {
            if (next == null) {
                throw new UnsupportedOperationException();
            }
            next.prev = null;
            next = null;
        } else {
            if (next != null) {
                throw new UnsupportedOperationException();
            }
            if (newNext.prev != null) {
                throw new UnsupportedOperationException();
            }
            next = newNext;
            next.prev = this;
        }
    }

    @Override
    public String toString() {
        return "Segment:" + start + " " + end;
    }

    /**
     * For large updates we need to fix the normal, and the AABB. Only call this
     * when the segment is not in a Tree2D
     *
     * @param start to rewrite starting point
     * @param end   to rewrite ending point
     */
    public void update(Vector2 start, Vector2 end) {
        this.start = start;
        this.end = end;
        this.normal = end.sub(start).crossZ().normal();
        updateAABB(start, end, 1.0);
    }

    /**
     * Check if this segment contains a point (x,y).
     *
     * @param x absciss
     * @param y coordinate
     * @return <tt>true</tt> if the point is on the segment with an acceptable
     * error
     * @see Vector2#isOnSegment(Segment2D)
     */
    public boolean contains(double x, double y) {
        return (new Vector2(x, y)).isOnSegment(this);
    }

    /**
     * Check if this segment contains a point (x,y)
     *
     * @param point described in x, y
     * @return <tt>true</tt> if close enough
     * @see Vector2#isOnSegment(Segment2D)
     */
    private boolean contains(Vector2 point) {
        return point.isOnSegment(this);
    }

    /**
     * Check if this segment is perpendicular another.
     *
     * @param that an other segment
     * @return <tt>true</tt> if vector product is zero
     */
    public boolean isPerpendicularTo(Segment2D that) {
        Vector2 v1 = this.end.sub(this.start);
        Vector2 v2 = that.end.sub(that.start);
        return (v1.dot(v2) == 0);
    }

    /**
     * Check if a segment lies entirely (vertex included) in this
     *
     * @param other a different segment to check
     * @return <tt>true</tt> if two ends stay inside
     * @see #contains(Vector2)
     */
    public boolean contains(Segment2D other) {
        return this.contains(other.start) && this.contains(other.end);
    }

    public Vector2 getDirectionalVector() {
        return end.sub(start).normal();
    }

    /**
     * Calculate the intersection
     *
     * @param that target
     * @return <tt>null</tt> if 2 segments are parallel (even overlapped)
     */
    public Vector2 intersect(Segment2D that) {
        Vector2 n = this.getDirectionalVector(), m = that.getDirectionalVector();
        if (n.v(m) == 0) return null;// If parallel
        return this.start.add(
                n.mul(
                        m.v(that.start.sub(this.start)) / m.v(n)
                )
        );
    }


}
