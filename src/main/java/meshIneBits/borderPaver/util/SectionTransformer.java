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

import meshIneBits.config.CraftConfig;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Vector;
import java.util.stream.IntStream;

//todo @Etienn créer une classe SECTION
public class SectionTransformer {

    /**
     * Takes a list of points, and returns the part of the polygon which can be used to place a bit.
     * Section acquisition is done clockwise.
     *
     * @param bound      the bound from which the section will be extracted
     * @param startPoint the point on which the left side of the bit will be placed. startPoint must be on the polygon.
     * @return a vector of vector2, the part of the polygon which can be used to place a bit
     */
    public static Section getSectionFromBound(@NotNull Vector<Vector2> bound, Vector2 startPoint) throws NoninvertibleTransformException {

        double bitLength = CraftConfig.lengthNormal;

        Vector<Segment2D> boundSegments = new Vector<>();
        Segment2D startSegment = null; // the segment which has the startPoint as start.

        // create a list of segments representing the bound, including the startPoint as an end of segments
        for (int iBound = 0; iBound < bound.size() - 1; iBound++) {
            Segment2D newSeg = new Segment2D(bound.get(iBound), bound.get(iBound + 1));
            if (isPointOnSegment(startPoint, newSeg)
                    && !startPoint.asGoodAsEqual(bound.get(iBound))
                    && !startPoint.asGoodAsEqual(bound.get(iBound + 1))) {
                // then startpoint is on this segment, so we split it in 2 to include the startPoint
                boundSegments.add(new Segment2D(bound.get(iBound), startPoint));
                startSegment = new Segment2D(startPoint, bound.get(iBound + 1));
                boundSegments.add(startSegment);
            } else if (isPointOnSegment(startPoint, newSeg) && startPoint.asGoodAsEqual(bound.get(iBound))) {
                // the startPoint is the start of newSeg
                startSegment = newSeg;
                boundSegments.add(newSeg);
            } else {
                // the startPoint isn't on newSeg (last point of newSeg excluded)
                boundSegments.add(newSeg);
            }
        }
        setNextSegments(boundSegments);

        Segment2D currentSegment = startSegment;
        Vector<Vector2> sectionPoints = new Vector<>();
        boolean intersectionFound = false;
        sectionPoints.add(currentSegment.start);
        do {
            Vector<Vector2> intersections = circleAndSegmentIntersection(currentSegment.start, currentSegment.end, startPoint, bitLength, true);
            if (!intersections.isEmpty()) { // il y a une intersection donc on l'ajoute à la section et on a fini
                sectionPoints.add(intersections.firstElement()); // dans notre cas comme on s'éloigne du startpoint il y aura forcément 1 intersection max
                intersectionFound = true;
            } else {
                sectionPoints.add(currentSegment.end);
                currentSegment = currentSegment.getNext();
            }
        } while (!intersectionFound && currentSegment != startSegment);

        return new Section(sectionPoints);
    }


    /**
     * Links each segment of the list to the next one if the end of one touches the start of the next.
     * @param segments the list of segments to link
     */
    private static void setNextSegments(Vector<Segment2D> segments) {
        for (int i = 0; i < segments.size() - 1; i++) {
            if (segments.get(i).end.asGoodAsEqual(segments.get(i + 1).start)) {
                segments.get(i)
                        .setNext(segments.get(i + 1));
            }
        }
        if (segments.lastElement().end.asGoodAsEqual(segments.firstElement().start)) {
            segments.lastElement()
                    .setNext(segments.firstElement());
        }
    }

    /**
     * Similar to isOnSegment() of Vector2, but more reliable
     *
     * @param v a point
     * @param s a segment
     * @return true if the point is on the segment
     */
    public static boolean isPointOnSegment(Vector2 v, @NotNull Segment2D s) {
        double errorAccepted = Math.pow(10, -CraftConfig.errorAccepted);
        return Math.abs(Vector2.dist(s.start, v) + Vector2.dist(s.end, v) - s.getLength()) < errorAccepted;
    }

    /**
     * Calculates the intersection between a circle and a segment.
     * @param p1 the first point of the segment
     * @param p2 the second point of the segment
     * @param center the center of the circle
     * @param radius the radius of the circle
     * @param isSegment true if the two points forms a segment and false if it is a straight line
     * @return a vector of vector2, the intersection points
     */
    public static Vector<Vector2> circleAndSegmentIntersection(Vector2 p1, Vector2 p2, Vector2 center, double radius, boolean isSegment) throws NoninvertibleTransformException {

        Point2D p1P2D = new Point2D.Double(p1.x, p1.y);
        Point2D p2P2D = new Point2D.Double(p2.x, p2.y);
        Point2D centerP2D = new Point2D.Double(center.x, center.y);

        Vector<Vector2> result = new Vector<>();
        double dx = p2P2D.getX() - p1P2D.getX();
        double dy = p2P2D.getY() - p1P2D.getY();
        AffineTransform trans = AffineTransform.getRotateInstance(dx, dy);
        trans.invert();
        trans.translate(-centerP2D.getX(), -centerP2D.getY());
        Point2D p1a = trans.transform(p1P2D, null);
        Point2D p2a = trans.transform(p2P2D, null);
        double y = p1a.getY();
        double minX = Math.min(p1a.getX(), p2a.getX());
        double maxX = Math.max(p1a.getX(), p2a.getX());
        if (y == radius || y == -radius) {
            if (!isSegment || (0 <= maxX && 0 >= minX)) {
                p1a.setLocation(0, y);
                trans.inverseTransform(p1a, p1a);
                result.add(new Vector2(p1a.getX(), p1a.getY()));
            }
        } else if (y < radius && y > -radius) {
            double x = Math.sqrt(radius * radius - y * y);
            if (!isSegment || (-x <= maxX && -x >= minX)) {
                p1a.setLocation(-x, y);
                trans.inverseTransform(p1a, p1a);
                result.add(new Vector2(p1a.getX(), p1a.getY()));
            }
            if (!isSegment || (x <= maxX && x >= minX)) {
                p2a.setLocation(x, y);
                trans.inverseTransform(p2a, p2a);
                result.add(new Vector2(p2a.getX(), p2a.getY()));
            }
        }
        return result;
    }

    /**
     * Calculates the angle of the local coordinate system used for the coordinate system transformation.
     *
     * @param sectionPoints the points that we want to transform in a local coordinate system.
     * @return the angle of the local coordinate system.
     */
    public static double getLocalCoordinateSystemAngle(Section sectionPoints) {

        //map for more accurate result
        Vector<Vector2> mappedPoints = repopulateWithNewPoints(30, sectionPoints, false);

        //get an angle in degrees
        double angle = getSectionOrientation(mappedPoints);

        //check if abscissa axe of local coordinate system and section are directed in the same direction.
        if (sectionPoints.arePointsMostlyOrientedToTheLeft()) {
            angle += 180; //rotate coordinate system
        }
        if (angle > 180) { // make sure that the angle is between -180 and 180 degrees
            angle -= 360;
        }

        return angle;
    }

    /**
     * Translates and rotates a curve to put it in a local coordinate system, centered on the first point of the curve,
     * with the abscissa axis in the direction of the average angle of the curve (regarding in the global coordinate
     * system).
     *
     * @param sectionPoints the points we want to convert in a local coordinate system
     * @return a new {@link Vector} that is the points entered as parameter, transformed in the local coordinate system.
     */
    public static @NotNull Vector<Vector2> getGlobalSectionInLocalCoordinateSystem(@NotNull Vector<Vector2> sectionPoints, double angle, Vector2 startPoint) {
        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.toRadians(-angle));
        transform.translate(-startPoint.x,-startPoint.y);
        return transformCoordinateSystem(sectionPoints, transform);
    }

    /**
     * Translates and rotates a curve to put it in the global coordinate system
     * @param sectionPoints the points we want to convert in a global coordinate system
     * @param angle the angle of the local coordinate system
     * @param startPoint the point that is the origin of the local coordinate system
     * @return a new {@link Vector} that is the points entered as parameter, transformed in the global coordinate system.
     */
    public static @NotNull Vector<Vector2> getLocalSectionInGlobalCoordinateSystem(@NotNull Vector<Vector2> sectionPoints, double angle, Vector2 startPoint) throws NoninvertibleTransformException {
        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.toRadians(-angle));
        transform.translate(-startPoint.x,-startPoint.y);
        return transformCoordinateSystem(sectionPoints, transform.createInverse());
    }


    /**
     * Rotates a list of points by a given angle and translates them to make the first point the origin (startPoint)
     *
     * @param points     the points to transform
     * @param transform  the transformation to apply
     * @return the transformed list of points
     */
    public static @NotNull Vector<Vector2> transformCoordinateSystem(@NotNull Vector<Vector2> points, AffineTransform transform) {
        Vector<Vector2> result = new Vector<>();
        for (Vector2 point : points) {
            result.add(transformCoordinateSystem(point, transform));
        }
        return result;
    }


    /**
     * Rotates a point by a given angle and translates it to make the first point the origin (startPoint)
     *
     * @param vectorToTransform the point to transform
     * @param transform   the transformation to apply
     * @return the transformed point
     */
    public static @NotNull Vector2 transformCoordinateSystem(Vector2 vectorToTransform, AffineTransform transform) {
        Point2D.Double point = new Point2D.Double(vectorToTransform.x, vectorToTransform.y);
        transform.transform(point, point);
        return new Vector2(point.getX(), point.getY());
    }

    /**
     * Returns the angle of the line that fit a list of points
     *
     * @param points the points
     * @return an angle between -90 and 90 degrees
     */
    public static Double getSectionOrientation(@NotNull Vector<Vector2> points) {
        // prepare fitting
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);//degree 1
        WeightedObservedPoints weightedObservedPoints = new WeightedObservedPoints();
        weightedObservedPoints.add(1000, points.get(0).x, points.get(0).y);

        for (int i = 1; i < points.size(); i++) {
            weightedObservedPoints.add(points.get(i).x, points.get(i).y);
        }

        // fit
        double[] coefficients_inverse = fitter.fit(weightedObservedPoints.toList());
        return Math.toDegrees(Math.atan(coefficients_inverse[1]));
    }

    /**
     * Repopulate a section a points with new points. Keep old ones if specified
     *
     * @param nbNewPoints   number of new points to add. If KeepOldPoints, new points are added if they're
     *                      not {@link Vector2#asGoodAsEqual(Vector2)}l the old points
     * @param section        the section of points to repopulate
     * @param keepOldPoints true to keep old points in addition to the old ones.
     * @return the section repopulated with new points.
     */
    public static @NotNull Vector<Vector2> repopulateWithNewPoints(int nbNewPoints, Section section, boolean keepOldPoints) {
        Vector<Vector2> points = section.getPoints();
        Vector<Vector2> newPoints = new Vector<>();
        Vector<Double> segmentLength = new Vector<>(); //contains the length of each segment

        for (int i = 0; i < points.size() - 1; i++) {
            double size = Math.sqrt(Math.pow(points.get(i).x - points.get(i + 1).x, 2) + Math.pow(points.get(i).y - points.get(i + 1).y, 2));
            segmentLength.add(size);
        }
        double spacing = segmentLength.stream()
                .mapToDouble(Double::valueOf)
                .sum() / (nbNewPoints - 1);

        double baseSegmentSum = 0;
        double newSegmentSum = 0;
        int basePointsIndex = 0;

        // --- Positions one point after the other ---

        if (keepOldPoints) { // add first point if keepOldPoints
            newPoints.add(points.firstElement());
        }

        for (int i = 0; i < nbNewPoints; i++) { // Placer un nouveau point

            double absNewPoint;
            double ordNewPoint;

            // --- selection of the first segment to place a point on it + add old points---
            while (basePointsIndex < points.size() - 2 && baseSegmentSum + segmentLength.get(basePointsIndex) <= newSegmentSum) {
                baseSegmentSum += segmentLength.get(basePointsIndex);
                basePointsIndex += 1;
                if (keepOldPoints) {
                    newPoints.add(points.get(basePointsIndex));
                }
            }

            //Calculate the angle between the segment and the abscissa axis
            double segmentAngle;
            if (points.get(basePointsIndex).x == points.get(basePointsIndex + 1).x && points.get(basePointsIndex).y <= points.get(basePointsIndex + 1).y) { // then the segment is vertical
                segmentAngle = Math.PI / 2;
            } else {
                segmentAngle = Math.atan((points.get(basePointsIndex + 1).y - points.get(basePointsIndex).y) / (points.get(basePointsIndex + 1).x - points.get(basePointsIndex).x)); // slope of the segment
            }

            int sign = 1;
            if (points.get(basePointsIndex + 1).x < points.get(basePointsIndex).x) {
                sign = -1;
            }

            absNewPoint = points.get(basePointsIndex).x + sign * (newSegmentSum - baseSegmentSum) * Math.cos(segmentAngle);
            ordNewPoint = points.get(basePointsIndex).y + sign * (newSegmentSum - baseSegmentSum) * Math.sin(segmentAngle);
            Vector2 newPoint = new Vector2(absNewPoint, ordNewPoint);
            if (!keepOldPoints || !containsAsGoodAsEqual(newPoint, points)) { // second condition is reached only if keepOldPoints is true
                newPoints.add(newPoint);
            }

            newSegmentSum += spacing;

        }

        // add last point
        newPoints.add(points.lastElement());

        return newPoints;
    }


    /**
     * Returns true if the point is contained in the list of points, with the precision of AsGoodAsEqual
     *
     * @param v   the point to test
     * @param pts the list of points
     * @return true if the point is contained in the list of points, with the precision of AsGoodAsEqual
     */
    private static boolean containsAsGoodAsEqual(Vector2 v, Vector<Vector2> pts) {
        for (Vector2 pt : pts) {
            if (pt.asGoodAsEqual(v)) return true;
        }
        return false;
    }

    /**
     * Rearranges the given points so that the list begins at the rightmost point
     *
     * @param pointList the points to be rearranged.
     * @return the rearranged points.
     */
    public static @NotNull Vector<Vector2> rearrangePoints(@NotNull Vector<Vector2> pointList) {
        Vector<Vector2> newPointList = new Vector<>();
        int PointIndex;
        double maxX = Double.NEGATIVE_INFINITY;
        int indexMax = 0;

        for (PointIndex = 0; PointIndex < pointList.size(); PointIndex++) {
            Vector2 actualPoint = pointList.get(PointIndex);
            if (actualPoint.x > maxX) {
                maxX = actualPoint.x;
                indexMax = PointIndex;
            }
        }

        IntStream.range(indexMax, pointList.size())
                .mapToObj(pointList::get)
                .forEachOrdered(newPointList::add);
        IntStream.range(0, indexMax + 1)
                .mapToObj(pointList::get)
                .forEachOrdered(newPointList::add);

        return newPointList;
    }

    /**
     * Rearranges the given segments so that each segment follows the previous one.
     *
     * @param segmentList the segments to rearranged
     * @return the rearranged segments. Returns more than one Vector of Segment2D if there's more than one bound on the Slice
     */
    public static @NotNull Vector<Vector<Segment2D>> rearrangeSegments(@NotNull Vector<Segment2D> segmentList) {
        Vector<Vector<Segment2D>> list = new Vector<>();
        Vector<Segment2D> newSegmentList = new Vector<>();
        newSegmentList.add(segmentList.get(0));
        list.add(newSegmentList);

        while (!segmentList.isEmpty()) {
            searchNextSegment(segmentList.get(0), segmentList, newSegmentList);
            newSegmentList = new Vector<>();
            list.add(newSegmentList);
        }
        list.removeIf(Vector::isEmpty);
        return list;
    }

    /**
     * Searches the next segment of the given segment, in a list of segments.
     * And returns the rearranged list.
     *
     * @param segment        the current segment.
     * @param segmentList    the list of all segments.
     * @param newSegmentList the list segments that have already been rearranged.
     * @return the rearranged list.
     * @see SectionTransformer#rearrangeSegments
     */
    private static @NotNull Vector<Segment2D> searchNextSegment(@NotNull Segment2D segment, @NotNull Vector<Segment2D> segmentList, @NotNull Vector<Segment2D> newSegmentList) {
        for (Segment2D segmentSearch : segmentList) {
            if (segmentSearch.start == segment.end) {
                newSegmentList.add(segmentSearch);
                segmentList.remove(segmentSearch);
                newSegmentList = searchNextSegment(segmentSearch, segmentList, newSegmentList);
                return newSegmentList;
            }
        }
        return newSegmentList;
    }
}