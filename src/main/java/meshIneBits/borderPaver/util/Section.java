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

package meshIneBits.borderPaver.util;

import meshIneBits.borderPaver.BorderedPatternAlgorithm;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * The description of a list of {@link Vector2}s or {@link Segment2D}s which make a section.
 * Contains methods to convert (vector2 <-> segment2D, compute hull, convexity,...)
 */
public class Section {
    public static final int CONVEX_TYPE_CONVEX = 1;
    public static final int CONVEX_TYPE_CONCAVE = -1;
    public static final int CONVEX_TYPE_UNDEFINED = 0;
    /**
     * The error threshold for the convexity computation.
     */
    private static final int CONVEX_ERROR = -4;
    private final Vector2 startPoint;
    private final Vector<Vector2> points;
    private final Vector<Segment2D> segments;


    public Section(Vector<Vector2> sectionPoints) {
        this.points = sectionPoints;
        this.segments = pointsToSegments(sectionPoints);
        this.startPoint = sectionPoints.firstElement();
    }

    /**
     * Computes the distance between a startPoint and an endPoint. But not a direct distance.
     * The distance is calculated passing by the segments of the bound
     *
     * @param startPoint a point which is on a segment
     * @param endPoint   the end of a segment
     * @param bound      the Slice
     * @return the distance
     */
    public static double getDistViaSegments(Vector2 startPoint, Vector2 endPoint, Vector<Segment2D> bound) {
        //finds the nearest point from startPoint and which is after the startPoint
        Segment2D minSeg = null;
        Segment2D startSeg = null;
        for (Segment2D segment2D : bound) {
            if (startPoint.isOnSegment(segment2D)) {
                startSeg = segment2D;
                minSeg = new Segment2D(segment2D.end, startPoint);
                break;
            }
        }

        //distance between the startPoint and the nearest segment
        //then we loop through the segments, starting from minPoint to end
        double totalDist = minSeg.getLength();//+sum of segments

        for (Segment2D segment2D : bound.subList(bound.indexOf(startSeg), bound.size())) {
            if (endPoint.isOnSegment(segment2D)) {
                return totalDist + new Segment2D(endPoint, segment2D.start).getLength();
            }
            totalDist += segment2D.getLength();
        }
        return totalDist;
    }

    /**
     * Checks if the given point A is located before the given point B on the given polygon.
     *
     * @param A       a {@link Vector2}.
     * @param B       a {@link Vector2}.
     * @param polygon a {@link Vector} of {@link Vector2}.
     * @return true if A is located before B on the polygon, false otherwise.
     */
    public static boolean isABeforeBOnPolygon(Vector2 A, Vector2 B, Vector<Segment2D> polygon) {
        // we go through the polygon, and we look if we find the point A before the point B
        for (Segment2D segment : polygon) {
            if (A.isOnSegment(segment) && B.isOnSegment(segment)) {
                return Vector2.dist(segment.start, A) < Vector2.dist(segment.start, B);
            }
            if (A.isOnSegment(segment)) return true;
            if (B.isOnSegment(segment)) return false;
        }
        throw new RuntimeException("Points not found on polygon");
    }

    /**
     * Checks if a given point is in a list of points with an error threshold.
     *
     * @param point  a {@link Vector2}.
     * @param points a {@link Vector} of {@link Vector2}.
     * @return true if the point is in the list of points, false otherwise.
     */
    public static boolean listContainsAsGoodAsEqual(Vector2 point, List<Vector2> points) {
        for (Vector2 p : points) {
            if (point.asGoodAsEqual(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a given list of points is contained in a list of points with an error threshold.
     *
     * @param containedList a {@link Vector} of {@link Vector2}.
     * @param containerList a {@link Vector} of {@link Vector2}.
     * @return true if the list of points is entirely contained in the list of points, false otherwise.
     */
    public static boolean listContainsAllAsGoodAsEqual(Vector<Vector2> containedList, List<Vector2> containerList) {
        for (Vector2 p : containedList) {
            if (!listContainsAsGoodAsEqual(p, containerList)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Computes the convexity of a given list of points.
     *
     * @param pts the list of points.
     * @return true if the list is convex, false otherwise.
     */
    public static boolean isConvex(List<Vector2> pts) {
        boolean sign = false;
        int n = pts.size();

        for (int i = 0; i < n; i++) {
            double distX1 = pts.get((i + 2) % n).x - pts.get((i + 1) % n).x;
            double distY1 = pts.get((i + 2) % n).y - pts.get((i + 1) % n).y;
            double distX2 = pts.get(i).x - pts.get((i + 1) % n).x;
            double distY2 = pts.get(i).y - pts.get((i + 1) % n).y;
            double zCrossProduct = distX1 * distY2 - distY1 * distX2;

            //we check if the sign is the same for all points with a precision margin
            if (i == 0) sign = zCrossProduct > CONVEX_ERROR;
            else if (sign != (zCrossProduct > CONVEX_ERROR)) return false;
        }
        return true;
    }

    /**
     * Computes the number of intersection between a segment and a list of points. The points are first converted into segments.
     *
     * @param segmentToTest the segment to test.
     * @param sectionPoints the list of points.
     * @return the number of intersection between the segment and the list of points.
     */
    public static int getNumberOfIntersection(Segment2D segmentToTest, List<Vector2> sectionPoints) {
        int nbIntersections = 0;
        Vector<Segment2D> segments = pointsToSegments(sectionPoints);
        for (Segment2D segment2D : segments) {
            if (Segment2D.doSegmentsIntersect(segmentToTest, segment2D)) {// && segment2D.end != Segment2D.getIntersectionPoint(segmentToTest, segment2D)) {
                nbIntersections++;
            }
        }
        return nbIntersections;
    }

    /**
     * Converts a list of {@link Vector2} to a list of {@link Segment2D} that would connect each point to the other,
     * following the order of the list of points given as entry. Deletes the points that are duplicated.
     *
     * @param points the points requiring to be converted into segments.
     * @return the segments resulting from the conversion.
     */
    @NotNull
    public static Vector<Segment2D> pointsToSegments(List<Vector2> points) {
        //remove the duplicated points
        Vector<Vector2> pointsNoDuplicates = new Vector<>();
        for (Vector2 point : points) {
            if (!pointsNoDuplicates.contains(point)) {
                pointsNoDuplicates.add(point);
            }
        }
        Vector<Segment2D> sectionSegments = new Vector<>();
        for (int i = 0; i < pointsNoDuplicates.size() - 1; i++) {
            sectionSegments.add(new Segment2D(pointsNoDuplicates.get(i), pointsNoDuplicates.get(i + 1)));
        }
        return sectionSegments;
    }

    /**
     * Converts a list of {@link Segment2D} to a list of {@link Vector2} that would connect each point to the other,
     * If the first and last segment are connected, the first point is not added.
     *
     * @param segmentList the segment list
     * @return the list of point computed from the segment list
     */
    public static @NotNull Vector<Vector2> segmentsToPoints(@NotNull Vector<Segment2D> segmentList) {
        Vector<Vector2> pointsList = new Vector<>();
        for (Segment2D segment : segmentList) {
            pointsList.add(new Vector2(segment.start.x, segment.start.y));
        }
        if (pointsList.firstElement().asGoodAsEqual(pointsList.lastElement())) pointsList.remove(0);
        return pointsList;
    }

    /**
     * Checks if most of the points are located at the left of a reference point
     *
     * @return true if most of the points are at the left of the reference point.
     */
    public boolean arePointsMostlyOrientedToTheLeft() {
        int leftPoints = 0;
        int rightPoints = 0;
        for (Vector2 point : points) {
            if (point.x < startPoint.x) {
                leftPoints++;
            } else {
                rightPoints++;
            }
        }
        return leftPoints >= rightPoints;
    }

    /**
     * Compute and return the longest segment of the given list
     *
     * @return the longest segment
     */
    public Segment2D getLongestSegment() {
        return segments.stream().max(Comparator.comparing(Segment2D::getLength)).orElseThrow(NoSuchElementException::new);
    }

    /**
     * Compute and return the most distant point to a segment
     *
     * @param segment2D the segment
     * @return the furthest point from the segment
     */
    public Vector2 getFurthestPointFromSegment(Segment2D segment2D) {
        Vector2 furthestPoint = null;
        double maxDistance = -1;
        for (Vector2 p : points) {
            if (Vector2.Tools.distanceFromPointToLine(p, segment2D) > maxDistance) {
                furthestPoint = p;
                maxDistance = Vector2.Tools.distanceFromPointToLine(p, segment2D);
            }
        }
        return furthestPoint;
    }

    /**
     * Search for the furthest point from ref point via a vector by computing a projection along the vector.
     *
     * @param refPoint          the first point
     * @param directionalVector the second point
     * @return the furthest point
     */
    public Vector2 getFurthestPointFromRefPointViaVector(Vector2 refPoint, Vector2 directionalVector) {
        directionalVector = directionalVector.normal();
        double maxDist = Double.NEGATIVE_INFINITY;
        Vector2 furthestPoint = null;
        for (Vector2 p : points) {
            double dist = BorderedPatternAlgorithm.getDistFromFromRefPointViaVector(refPoint, p, directionalVector);
            if (dist > maxDist) {
                maxDist = dist;
                furthestPoint = p;
            }
        }
        return furthestPoint;
    }

    /**
     * Finds the furthest point among a list of points from a reference point (refPoint), calculating direct distances
     * between them.
     *
     * @param refPoint the point from which the method calculates the distances
     * @return the furthest point from refPoint among the list  of points
     */
    public Vector2 getFurthestPoint(Vector2 refPoint) {
        double maxDist = 0;
        Vector2 furthestPoint = null;
        for (Vector2 p : points) {
            double dist = Vector2.dist(refPoint, p);
            if (dist > maxDist) {
                maxDist = dist;
                furthestPoint = p;
            }
        }
        return furthestPoint;
    }

    /**
     * Does successive reductions of the section until all points fit under a bit.
     *
     * @param sectionPoints  the list of points to reduce.
     * @param minWidthToKeep the minimum width to keep.
     * @return the reduced section
     */
    public Section getSectionReduced(Section sectionPoints, double minWidthToKeep) {
        Section sectionToReduce = new Section(SectionTransformer.repopulateWithNewPoints(200, sectionPoints, true));

        boolean sectionReductionCompleted = false;

        Segment2D constraintSegment;
        Vector2 furthestPoint;

        do {

            boolean sectionIsClosed = sectionToReduce.startPoint.asGoodAsEqual(sectionToReduce.points.lastElement());

            // calculates the convex hull of the section's points
            Section hull = sectionToReduce.getHull();

            // find the constraint segment, which is the longest segment of the hull // todo, maybe not always the case
            constraintSegment = hull.getLongestSegment();

            // find the constraint point, which is the convex hull's furthest point from the constraint segment
            furthestPoint = hull.getFurthestPointFromSegment(constraintSegment);

            // calculate distance between constraint point and constraint segment
            double sectionWidth = Vector2.Tools.distanceFromPointToLine(furthestPoint, constraintSegment);

            /*
             If this condition is true is executed, this means the bit can't be placed over all the section's points
             while respecting the minWidthToKeep. In this case the content of the following "if" reduces the section,
             starting by the last point, until the first "cut point" (the constraint point or an end of the
             constraint segment) reached, thus this point is the fist one that prevents the section to be thinner.
             */
            if ((!sectionIsClosed && (sectionWidth > CraftConfig.bitWidth - minWidthToKeep)) || (sectionIsClosed && sectionWidth > CraftConfig.bitWidth)) {

                // list the cut points
                Vector<Vector2> cutPoints = new Vector<>();
                cutPoints.add(constraintSegment.start);
                cutPoints.add(constraintSegment.end);
                cutPoints.add(furthestPoint);
                // research of the first "cut point" of the section, starting research by its last point.
                boolean cutPointFound = false;
                int iSection = sectionToReduce.getPoints().size() - 1;
                while (!cutPointFound) {
                    if (cutPoints.contains(sectionToReduce.getPoints().get(iSection))) {
                        // delete section's points from the cut point at iSection (included) to the last point of the section
                        while (sectionToReduce.getPoints().size() > iSection) {
                            sectionToReduce.getPoints().remove(iSection);
                        }
                        cutPointFound = true;
                    }
                    iSection--;
                }
            } else {
                sectionReductionCompleted = true;
            }
        }
        //We do successive reductions of the section until all points fit under a bit
        while (!sectionReductionCompleted);

        return sectionToReduce;
    }

    /**
     * Computes the convexHull of the section's points.
     *
     * @return the convexHull.
     */
    public Section getHull() {
        Vector<Vector2> hull = new Vector<>();

        // find leftMost
        Vector<Integer> iLeftMost = new Vector<>();
        double xMin = Double.POSITIVE_INFINITY;
        for (Vector2 point : points) {
            double x = point.x;
            if (x <= xMin) {
                xMin = x;
            }
        }
        for (int i = 0; i < points.size(); i++) {
            if (Math.abs(points.get(i).x - xMin) < Math.pow(10, -5)) iLeftMost.add(i);
        }
        // find higher of leftMost
        int iHigherLeftMost = iLeftMost.get(0);
        for (int i = 1; i < iLeftMost.size(); i++) {
            if (points.get(iLeftMost.get(i)).y < points.get(iHigherLeftMost).y) iHigherLeftMost = iLeftMost.get(i);
        }
        int startIndex = iHigherLeftMost;

        hull.add(points.get(startIndex));


        // starting from that point, find each next point whose angle is the biggest until we return to the start point
        //second point computing
        Vector2 previousPoint = points.get(startIndex).sub(new Vector2(-1, 0));
        Vector2 pointMilieu = points.get(startIndex);

        while (hull.size() < 2 || hull.firstElement() != hull.lastElement()) {
            double maxAngle = 0;
            Vector<Vector2> pointsMaxAngles = new Vector<>();
            for (int i = 0; i < points.size(); i++) {
                // if the pivot point is the last point added, it doesn't need to be tested again, plus it causes inaccurate angles
                if (points.get(i) != pointMilieu) {
                    double angle = Vector2.getAngle(previousPoint, pointMilieu, points.get(i));

                    if (angle >= maxAngle - 1e-10 && i != points.indexOf(pointMilieu)) {
                        if (angle > maxAngle + 1e-10) {  //the points are aligned if the angle is between -1e-10 and +1e-10
                            pointsMaxAngles.removeAllElements();
                        }
                        maxAngle = angle;
                        pointsMaxAngles.add(points.get(i));
                    }
                }
            }

            if (pointsMaxAngles.contains(hull.firstElement())) {
                /*
                in a closed section, the first and the last element of the section are the same, but because of
                imprecision in the decimal numbers, at the end of the hull, it is a different point from the
                first point, which causes the while loop to run infinitely
                 */
                hull.add(hull.firstElement());
            } else {
                Section maxAnglesSection = new Section(pointsMaxAngles);
                hull.add(maxAnglesSection.getFurthestPoint(pointMilieu));
            }
            previousPoint = pointMilieu;
            pointMilieu = hull.lastElement();
        }
        return new Section(hull);
    }

    //GETTERS
    public Vector2 getStartPoint() {
        return startPoint;
    }

    public Vector<Vector2> getPoints() {
        return points;
    }

    public Vector<Segment2D> getSegments() {
        return segments;
    }

    public int getConvexType(Vector2 startPoint) {
        List<Vector2> tempPoints = new Vector<>(points);
        int convexType = 0;

        Vector2 ORIGIN = new Vector2(0, 0);
        tempPoints.add(ORIGIN);//convexity in regard to the origin

        // In order to know if the beginning of the section is convex or concave, we look at
        // all the points that are at a distance less than a bit length from the startPoint.
        int nbPointsToCheck = 0;
        for (Vector2 point : tempPoints) {
            nbPointsToCheck++;
            if (Vector2.dist(point, startPoint) >= CraftConfig.lengthNormal / 2) break;
        }

        //Computes the convexity of the section
        List<Vector2> maxConvexSection = new Vector<>();
        if (Section.isConvex(tempPoints.subList(0, nbPointsToCheck))) {
            //convex section
            convexType = CONVEX_TYPE_CONVEX;

            List<Vector2> convexSection = new Vector<>();
            for (int i = 0; i < nbPointsToCheck; i++) {
                convexSection.add(tempPoints.get(i));
            }
            convexSection.add(ORIGIN);

            // we add the points while the section is convex
            int i = nbPointsToCheck;
            do {
                convexSection.add(convexSection.size() - 1, tempPoints.get(i));
                i++;
            } while (i < tempPoints.size() && Section.isConvex(convexSection));
            maxConvexSection = convexSection;
            maxConvexSection.remove(ORIGIN);
        }

        if (maxConvexSection.size() <= 0) { //the section is concave
            convexType = CONVEX_TYPE_CONCAVE;
        }

        return convexType;
    }
}
