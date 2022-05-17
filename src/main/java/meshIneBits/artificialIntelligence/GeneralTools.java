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

package meshIneBits.artificialIntelligence;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.debug.DebugTools;
import meshIneBits.artificialIntelligence.util.Curve;
import meshIneBits.artificialIntelligence.util.SectionTransformer;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Polygon;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Area;
import java.util.List;
import java.util.Vector;


/**
 * GeneralTools groups together the methods used to prepare the data for Neural Network or Genetic Algorithms.
 * It also provides methods to perform intersection point search.
 */
public class GeneralTools {

    /**
     * Returns a point list from a segment list.
     * If the first and last segment are connected, the first point is not added.
     *
     * @param segmentList the segment list
     * @return the list of point computed from the segment list
     */
    private static @NotNull Vector<Vector2> segmentsToPoints(@NotNull Vector<Segment2D> segmentList) {
        Vector<Vector2> pointsList = new Vector<>();
        for (Segment2D segment : segmentList) {
            pointsList.add(new Vector2(segment.start.x, segment.start.y));
        }
        if (pointsList.firstElement().asGoodAsEqual(pointsList.lastElement())) pointsList.remove(0);
        return pointsList;
    }

    /**
     * Checks if most of the points given as parameter are located at the left of a reference point, also given
     * in parameters.
     *
     * @param points   the points whose location we want to test.
     * @param refPoint the reference point.
     * @return true if most of the points are at the left of the reference point.
     */
    public static boolean arePointsMostlyOrientedToTheLeft(@NotNull Vector<Vector2> points, @NotNull Vector2 refPoint) {
        int leftPoints = 0;
        int rightPoints = 0;
        for (Vector2 point : points) {
            if (point.x < refPoint.x) {
                leftPoints++;
            } else {
                rightPoints++;
            }
        }
        return leftPoints >= rightPoints;
    }

    /**
     * Positions a precise number of points on a section of points. Each point is equally spaced to the next point.
     *
     * @param sectionPoints the initial section of points.
     * @return a new section of points, composed of the equally spaced points.
     */
    public static @NotNull Vector<Vector2> getInputPointsForDL(@NotNull Vector<Vector2> sectionPoints) {
        int nbPoints = 10;//todo @Etienne ou Andre : hidden neurons count has depend of this
        return SectionTransformer.repopulateWithNewPoints(nbPoints, sectionPoints, false);
    }


    /**
     * This method makes a double non-linear regression over a section of points saved in DataLog.csv.
     * This an alternative approach to {@link #getInputPointsForDL(Vector)} method : the returned values describe
     * approximately the shape of the section of points entered as parameter. Then the coefficients returned could
     * be used in a neural net.
     * The interest of this method over {@link #getInputPointsForDL(Vector)} is that it reduces the number of features
     * injected in the neural. In return, the representation of the section may be less accurate.
     * The steps below describe how the double non-linear regression works :
     * 1 - The section is split into two curves : x(t) and y(t), where the parameter t is the curvilinear abscissa of
     * the section.
     * 2 - A non-linear regression is made over each curve.
     * 3 - We get two arrays of coefficients related to the two curves.
     *
     * @param sectionPoints the points on which the regression is performed.
     * @param degree        the degree of the regression, witch is linked to the accuracy of the regression performed.
     * @return a {@link Vector} of two {@link Vector} of coefficients related the section entered as parameter.
     */
    @SuppressWarnings("unused")
    private static @NotNull Vector<Vector<Double>> getInputSlopesForDL(Vector<Vector2> sectionPoints, int degree) {
        Curve inputCurve = new Curve("input curve");
        inputCurve.generateCurve(sectionPoints);
        Curve[] splitCurve = inputCurve.splitCurveInTwo();
        Curve xCurve = splitCurve[0];
        Curve yCurve = splitCurve[1];

        // prepare fitting
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
        WeightedObservedPoints weightedObservedPointsX = new WeightedObservedPoints();
        WeightedObservedPoints weightedObservedPointsY = new WeightedObservedPoints();
        for (int i = 0; i < inputCurve.getNumberOfPoints(); i++) {
            weightedObservedPointsX.add(xCurve.getPoints().get(i).x, xCurve.getPoints().get(i).y);
            weightedObservedPointsY.add(yCurve.getPoints().get(i).x, yCurve.getPoints().get(i).y);
        }
        // fit
        double[] coefficients_inverseX = fitter.fit(weightedObservedPointsX.toList());
        double[] coefficients_inverseY = fitter.fit(weightedObservedPointsY.toList());
        // invert coefficients
        Vector<Double> coefficientsX = new Vector<>();
        Vector<Double> coefficientsY = new Vector<>();
        for (int i = 0; i < coefficients_inverseX.length; i++) {
            coefficientsX.add(coefficients_inverseX[coefficients_inverseX.length - i - 1]);
            coefficientsY.add(coefficients_inverseY[coefficients_inverseX.length - i - 1]);
        }
        // return result
        Vector<Vector<Double>> coefficients = new Vector<>();
        coefficients.add(coefficientsX);
        coefficients.add(coefficientsY);
        return coefficients;
    }

    //todo description

    /**
     * @param bit
     * @param boundPoints
     * @param limitTheDistance
     * @param startPoint
     * @return
     */
/*
Plusieurs cas possibles :
  - 0 intersections --> exception, le bit est mal placé
  - 1 intersection  --> exception, le bit est mal placé
  - 2 intersections --> le bit est bien placé. Cas typique, on renvoie le second point
  - 3 intersections --> le bit est bien placé. Cas segment orthogonaux, on renvoie le second point, car le troisième est après le deuxième.
  - 3 intersections --> le bit est bien placé. Cas segment orthogonaux, on renvoie le troisième point, car le premier est avant le startPoint
 */
    public static Vector2 getBitAndContourSecondIntersectionPoint(@NotNull Bit2D bit, @NotNull Vector<Vector2> boundPoints, boolean limitTheDistance, Vector2 startPoint) {
        Vector<Segment2D> bitSidesSegments = bit.getBitSidesSegments();
        System.out.println("boundPoints = " + boundPoints);
        Vector<Segment2D> contourSegments = GeneralTools.pointsToSegments(boundPoints);
        //On ajoute le dernier segment pour pouvoir calculer les intersections. On le retire à la fin
        contourSegments.add(new Segment2D(boundPoints.get(boundPoints.size() - 2), boundPoints.get(0)));

        System.out.println("contourSegments = " + contourSegments);
        Vector<Vector2> intersections = new Vector<>();

        //le startIndex est l'index du


        for (int i = 0; i < contourSegments.size(); i++) {
            Segment2D contourSegment = contourSegments.get(i);
            for (Segment2D bitSideSegment : bitSidesSegments) {
                Vector2 intersection = Segment2D.getIntersectionPoint(bitSideSegment, contourSegment);
                //vérifie si l'intersection trouvée n'est pas quasiment le startPoint
                if (intersection != null && !intersections.contains(intersection)){// && Vector2.dist(intersection, startPoint) > 2) { // todo, mettre une valeur sur le 1
                    System.out.println("GeneralTools.getDistViaSegments(startPoint,intersection,contourSegments) = " + GeneralTools.getDistViaSegments(startPoint, intersection, contourSegments));
//                    if (GeneralTools.getDistViaSegments(startPoint,intersection,contourSegments)<=CraftConfig.lengthFull+CraftConfig.bitWidth) {
                    intersections.add(intersection);
                    System.out.println("Vector2.dist() = " + Vector2.dist(intersection, startPoint));
                    System.out.println("intersection = " + intersection);
                    System.out.println("startPoint = " + startPoint);
//                    }
                }
            }
        }

        DebugTools.pointsToDrawRED.clear();
        DebugTools.pointsToDrawRED.addAll(intersections);
        System.out.println("intersections = " + intersections);
        if (intersections.size() == 0) {
            throw new RuntimeException("No intersection found");
        }

        contourSegments.remove(contourSegments.size() - 1);

        // si la première intersection est le startPoint, on prend la troisième intersection
        if (intersections.size()==3 && GeneralTools.isABeforeBOnPolygon(intersections.get(0), startPoint, contourSegments)) {
            return intersections.get(2);
        }

        return intersections.get(1);
    }

    private static boolean isABeforeBOnPolygon(Vector2 A, Vector2 B, Vector<Segment2D> polygon) {
        // on parcourt le polygon, et on regarde si on trouve le point A avant le point B
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
     * Looks for an intersection point between a side of a bit and a closed contour (bound). The point returned
     * is the first intersection between the bit and the contour, while scanning the contour in the direction of
     * increasing indices of the points of the contour.
     *
     * @param bit              a {@link Bit2D}.
     * @param boundPoints      the points of the contour.
     * @param limitTheDistance if true, it will only look for the next start point at a maximum distance of the length of a bit
     * @return the first intersection between the bit and the closed contour.
     */
    //TODO enlever
    public static Vector2 getBitAndContourSecondIntersectionPoint3(@NotNull Bit2D bit, @NotNull Vector<Vector2> boundPoints, boolean limitTheDistance, Vector2 startPoint) {
        Vector2 intersectionPoint = null;
        Vector2 firstIntersectionPoint = getBitAndContourFirstIntersectionPoint(bit, boundPoints);
        //on trouve le startIndex pour commencer à chercher une intersection.
        // Le startIndex est l'indice du point qui est la fin du segment sur lequel est placé le startPoint
        int startIndex = 0;
        for (int i = 0; i < boundPoints.size() - 1; i++) {
            Segment2D tempSeg = new Segment2D(boundPoints.get(i), boundPoints.get(i + 1));
            if (firstIntersectionPoint.isOnSegment(tempSeg)) {
                //on commence sur le premier segment, car le segment peut être plus long que la longueur d'un bit
                if (tempSeg.getLength() >= CraftConfig.lengthNormal) startIndex = i;
                else //sinon sur le second
                    startIndex = i + 1;
                System.out.println("startIndex = " + startIndex);
                break;
            }
        }


        for (int i = startIndex; i < boundPoints.size(); i++) {
            Vector2 boundPoint = boundPoints.get(i);
            Vector2 intersection = getBitAndLineIntersectionPoint(bit, boundPoint, boundPoints.get(i + 1));
            if (intersection != null) {
                if (limitTheDistance) {
                    if (Math.abs(intersection.x - bit.getCenter().x) < bit.getLength() / 2 && Math.abs(intersection.y - bit.getCenter().y) < bit.getLength() / 2) {
                        intersectionPoint = intersection;
                        break;
                    }
                } else {
                    intersectionPoint = intersection;
                    break;
                }
            }
        }
        return intersectionPoint;
    }

    private static Vector2 getBitAndLineIntersectionPoint(Bit2D bit, Vector2 boundPoint, Vector2 nextBoundPoint) {
        Segment2D segment2D = new Segment2D(boundPoint, nextBoundPoint);
        Vector<Segment2D> bitSegments = bit.getBitSidesSegments();
        Vector2 intersectionPoint = null;
        for (Segment2D bitSegment : bitSegments) {
            Vector2 intersection = Segment2D.getIntersectionPoint(bitSegment, segment2D);
            if (intersection != null) {
                intersectionPoint = intersection;
            }
        }
        return intersectionPoint;
    }

    //TODO enlever
    public static Vector2 getBitAndContourSecondIntersectionPoint2(@NotNull Bit2D bit, @NotNull Vector<Vector2> boundPoints, boolean limitTheDistance, Vector2 startPoint) {

        Vector<Segment2D> boundSegments = pointsToSegments(boundPoints);
        Vector<Segment2D> bitSegments = bit.getBitSidesSegments();
        Vector<Vector2> intersectionPoints = new Vector<>();

        for (Segment2D boundSegment : boundSegments) {
            Vector<Vector2> intersectionsWithSegment = new Vector<>(); //list of intersections between the bit and the Slice

            for (Segment2D bitSegment : bitSegments) {
                //we search for all the intersection points between the bit and the Slice
                if (Segment2D.doSegmentsIntersect(boundSegment, bitSegment)) {
                    Vector2 inter = Segment2D.getIntersectionPoint(bitSegment, boundSegment); //TODO @ANDRE What if the straight lines are combined
                    intersectionsWithSegment.add(inter);
                }
            }

            // loop through the intersections
            if (intersectionsWithSegment.size() == 1) intersectionPoints.add(intersectionsWithSegment.get(0));
            else if (intersectionsWithSegment.size() == 2 && Vector2.dist2(intersectionsWithSegment.get(0), boundSegment.start) < Vector2.dist2(intersectionsWithSegment.get(1), boundSegment.start))
                intersectionPoints.addAll(intersectionsWithSegment);
            else if (intersectionsWithSegment.size() == 2) {
                intersectionPoints.add(intersectionsWithSegment.get(1));
                intersectionPoints.add(intersectionsWithSegment.get(0));
            }

//TODO @ANDRE SUPPRESS IT?
//            while (!intersectionsWithSegment.isEmpty()) {
//                double distMin = Double.POSITIVE_INFINITY;
//                Vector2 firstPoint = null;
//                for (Vector2 inter : intersectionsWithSegment) {
//                    if (Vector2.dist2(inter, boundSegment.start) < distMin) {
//                        firstPoint = inter;
//                        distMin = Vector2.dist2(inter, boundSegment.start);
//                    }
//                }
//                intersectionPoints.add(firstPoint);
//                intersectionsWithSegment.remove(firstPoint);
//            }

        }
        if (bit.getArea().contains(boundPoints.get(0).x, boundPoints.get(0).y) && boundPoints.firstElement() != intersectionPoints.firstElement())
            return intersectionPoints.get(0);
        else return intersectionPoints.get(1);//return the second intersection
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
     * Returns the first intersection point between the bound and bit's edges
     *
     * @param bit         a bit
     * @param boundPoints the points of the bound
     * @return the first intersection point between the bound and bit's edges
     */
    public static @Nullable Vector2 getBitAndContourFirstIntersectionPoint(@NotNull Bit2D bit, @NotNull Vector<Vector2> boundPoints) {
        // get sides of the bit as Segment2Ds (will be used later)
        Vector<Segment2D> bitSides = bit.getBitSidesSegments();

        // first we fill with the points of the bound a vector of segments:
        Vector<Segment2D> boundSegments = new Vector<>();
        for (int i = 0; i < boundPoints.size() - 1; i++) {
            boundSegments.add(new Segment2D(boundPoints.get(i), boundPoints.get(i + 1)));
        }

        // We will have to scan each segment of the bound, to check if an edge of the bit intersects with it.
        // But we have to start scanning by a segment whose its start is not under the bit, otherwise the intersection
        // point found won't be the good one.
        // So first we have to find a segment whose its start is not under the bit.

        Polygon rectangle = new Polygon();
        bit.getBitSidesSegments().forEach(rectangle::addEnd);
        Area bitRectangleArea = new Area(rectangle.toPath2D());

        int startSegIndex = 0;

        while (bitRectangleArea.contains(boundSegments.get(startSegIndex).start.x, boundSegments.get(startSegIndex).start.y)) {
            startSegIndex++;
        }


        // finally, we can scan the bound, starting with segment at index startSegIndex.
        boolean scanCompleted = false;
        int iSeg = startSegIndex;

        while (!scanCompleted) { //look for an intersection


            // sometimes there will be more than 1 bit's edges intersecting a segment. We have to make sure that
            // we return the first of these intersections. So we will store all intersection points and return
            // the one which its distance with segment's start is the lowest.
            Vector<Vector2> intersectionPoints = new Vector<>();


            //fill intersectionPoints Vector<> by checking intersections with all bit's sides
            for (Segment2D bitSide : bitSides) {
                Vector2 intersectionPoint = Segment2D.getIntersectionPoint(bitSide, boundSegments.get(iSeg));
                if (intersectionPoint != null) { // then we store this intersection
                    intersectionPoints.add(intersectionPoint);
                }
            }

            // if we have some intersections we have to return the first one (as explained above)
            if (!intersectionPoints.isEmpty()) {
                double maxDist2 = Double.POSITIVE_INFINITY;
                Vector2 firstIntersectionPoint = null; // can't be null
                for (Vector2 intersectPoint : intersectionPoints) {

                    double dist2 = Vector2.dist2(boundSegments.get(iSeg).start, intersectPoint);
                    if (dist2 < maxDist2) {
                        maxDist2 = dist2;
                        firstIntersectionPoint = intersectPoint;
                    }

                }
                return firstIntersectionPoint;
            }


            // increment
            iSeg++;
            if (iSeg == boundSegments.size()) {
                iSeg = 0;
            }

            // check if scan completed = we reached the segment at index startSegIndex again
            if (iSeg == startSegIndex) {
                scanCompleted = true;
            }
        }

        return null;
    }

    /**
     * Converts a list of {@link Vector2} to a list of {@link Vector} that would connect each point to the other,
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
     * Returns the points of each bound of a given Slice
     * The points are rearranged to be in the correct order.
     *
     * @param currentSlice the slice to get the bounds.
     * @return the bounds of the given slice, once rearranged.
     * @see SectionTransformer#rearrangeSegments
     * @see SectionTransformer#rearrangePoints
     */
    @SuppressWarnings("unchecked")
    public @NotNull Vector<Vector<Vector2>> getBoundsAndRearrange(@NotNull Slice currentSlice) {
        Vector<Vector<Vector2>> boundsList = new Vector<>();
        Vector<Vector<Segment2D>> borderList = SectionTransformer.rearrangeSegments((Vector<Segment2D>) currentSlice.getSegmentList().clone());

        for (Vector<Segment2D> border : borderList) {
//            boundsList.add(segmentsToPoints(border));
            Vector<Vector2> unorderedPoints = segmentsToPoints(border);
            boundsList.add(SectionTransformer.rearrangePoints(unorderedPoints));
        }
        System.out.println("borderList = " + borderList);
        return boundsList;
    }

    /**
     * Return the next Bit2D start point.
     * It is the intersection between the slice and the end side of the Bit2D.
     *
     * @param bit              the current Bit2D (the last placed Bit2D by AI).
     * @param boundPoints      the points of the bounds on which stands the bit.
     * @param limitTheDistance if true, it will only look for the next start point at a maximum distance of the size of a bit
     * @return the next bit start point. Returns <code>null</code> if none was found.
     */
    public Vector2 getNextBitStartPoint(@NotNull Bit2D bit, @NotNull Vector<Vector2> boundPoints, boolean limitTheDistance, Vector2 startPoint) throws Exception {

        Vector2 nextBitStartPoint = getBitAndContourSecondIntersectionPoint(bit, boundPoints, limitTheDistance, startPoint);

        if (nextBitStartPoint != null) {
            return nextBitStartPoint;
        } else {
            throw new Exception("The bit start point has not been found.");
        }
    }

    /**
     * Returns points all points associated with a Bit2D.
     * Points associated are the points of the Slice from the startPoint of the Bit2D,
     * till the distance with the point become greater than the length of a Bit2D.
     *
     * @param bit2D The Bit2D we want to get the points associated with.
     * @return the associated points.
     */
    public @NotNull Vector<Vector2> getCurrentLayerBitAssociatedPoints(@NotNull Bit2D bit2D) throws Exception {

        //First we get all the points of the Slice. getContours returns the points already rearranged.
        Vector<Vector<Vector2>> boundsList = getBoundsAndRearrange(AI_Tool.getMeshController().getCurrentLayer().getHorizontalSection());

        // finds the startPoint (if exists) and the bound related to this startPoint
        int iContour = 0;
        Vector2 startPoint = null;
        boolean boundFound = false;
        while (iContour < boundsList.size() && !boundFound) {
            startPoint = getBitAndContourFirstIntersectionPoint(bit2D, boundsList.get(iContour));
            if (startPoint != null) boundFound = true;
            iContour++;
        }

        // finds the points associated with the bit, using the startPoint and the bound previously found
        if (startPoint != null)
            return SectionTransformer.getSectionPointsFromBound(boundsList.get(iContour - 1), startPoint);
        else throw new Exception("The bit start point has not been found.");
    }

    /**
     * Return the next Bit2D start point.
     * It is the intersection between the slice and the end side of the Bit2D.
     *
     * @param bit         the current Bit2D (the last placed Bit2D by AI).
     * @param boundPoints the points of the bounds on which stands the bit.
     * @return the next bit start point. Returns <code>null</code> if none was found.
     */
    public Vector2 getNextBitStartPoint(@NotNull Bit2D bit, @NotNull Vector<Vector2> boundPoints) throws Exception {
        return getNextBitStartPoint(bit, boundPoints, false, null);
    }
}