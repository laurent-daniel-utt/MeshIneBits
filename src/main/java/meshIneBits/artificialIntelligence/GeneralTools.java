package meshIneBits.artificialIntelligence;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.util.Curve;
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
import java.util.Vector;
import java.util.stream.IntStream;

/**
 * GeneralTools groups together the methods used to prepare the data for Neural Network or Genetic Algorithms.
 * It also provides methods to perform intersection point search.
 */
public class GeneralTools {

    /**
     * Returns the points of each bound of a given Slice
     * The points are rearranged to be in the correct order.
     *
     * @param currentSlice the slice to get the bounds.
     * @return the bounds of the given slice, once rearranged.
     * @see #rearrangeSegments
     * @see #rearrangePoints
     */
    @SuppressWarnings("unchecked")
    public @NotNull Vector<Vector<Vector2>> getBoundsAndRearrange(@NotNull Slice currentSlice) {
        /*
         TODO: 2021-01-18
        * this method could be replaced by the optimize method of Shape2D
        *  Just make sure to get all "bounds" = all polygons that makes the Slice.
        */
        Vector<Vector<Vector2>> boundsList = new Vector<>();
        Vector<Vector<Segment2D>> borderList = rearrangeSegments((Vector<Segment2D>) currentSlice.getSegmentList().clone());

        for (Vector<Segment2D> border : borderList) {
            Vector<Vector2> unorderedPoints = computePoints(border);
            boundsList.add(rearrangePoints(unorderedPoints));
        }
        return boundsList;
    }

    /**
     * Rearranges the given segments so that each segment follows the previous one.
     *
     * @param segmentList the segments to rearranged
     * @return the rearranged segments. Returns more than one Vector of Segment2D if there's more than one bound on the Slice
     */
    private static @NotNull Vector<Vector<Segment2D>> rearrangeSegments(@NotNull Vector<Segment2D> segmentList) {
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
     * Rearranges the given points so that the list begins at the rightmost point
     *
     * @param pointList the points to be rearranged.
     * @return the rearranged points.
     */
    private static @NotNull Vector<Vector2> rearrangePoints(@NotNull Vector<Vector2> pointList) {
        Vector<Vector2> newPointList = new Vector<>();
        int PointIndex;
        double maxX = -1000000000;
        int indexMax = 0;

        for (PointIndex = 0; PointIndex < pointList.size(); PointIndex++) {
            Vector2 actualPoint = pointList.get(PointIndex);
            if (actualPoint.x > maxX) {
                maxX = actualPoint.x;
                indexMax = PointIndex;
            }
        }

        IntStream.range(indexMax, pointList.size()).mapToObj(pointList::get).forEachOrdered(newPointList::add);
        IntStream.range(0, indexMax + 1).mapToObj(pointList::get).forEachOrdered(newPointList::add);

        return newPointList;
    }

    /**
     * Returns a point list from a segment list
     *
     * @param segmentList the segment list
     * @return the list of point computed from the segment list
     */
    private static @NotNull Vector<Vector2> computePoints(@NotNull Vector<Segment2D> segmentList) {
        Vector<Vector2> pointList = new Vector<>();
        for (Segment2D segment : segmentList) {
            pointList.add(new Vector2(segment.start.x, segment.start.y));
        }
        pointList.remove(0);
        return pointList;
    }

    /**
     * Searches the next segment of the given segment, in a list of segments.
     * And returns the rearranged list.
     *
     * @param segment        the current segment.
     * @param segmentList    the list of all segments.
     * @param newSegmentList the list segments that have already been rearranged.
     * @return the rearranged list.
     * @see #rearrangeSegments
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

    /**
     * Calculates the angle of the local coordinate system used for the coordinate system transformation made
     * by {@link #getSectionInLocalCoordinateSystem(Vector)}.
     *
     * @param sectionPoints the points that we want to transform in a local coordinate system.
     * @return the angle of the local coordinate system.
     */
    public static double getLocalCoordinateSystemAngle(@NotNull Vector<Vector2> sectionPoints) {

        //map for more accurate result
        Vector<Vector2> mappedPoints = repopulateWithNewPoints(30, sectionPoints);

        //get an angle in degrees
        double angle = getSectionOrientation(mappedPoints);


        //check if abscissa axe of local coordinate system and and section are directed in the same direction.
        if (arePointsMostlyOrientedToTheLeft(sectionPoints, sectionPoints.firstElement())) {
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
    public static @NotNull Vector<Vector2> getSectionInLocalCoordinateSystem(@NotNull Vector<Vector2> sectionPoints) {
        /*
         * TODO: 2021-01-20
         *  methods that transform points from one Coordinate System to another one should be replaced with AffineTransform.
         */
        double angle = getLocalCoordinateSystemAngle(sectionPoints);
        return transformCoordinateSystem(sectionPoints, angle);
    }


    /**
     * Rotates a list of points by a given angle
     *
     * @param points the points to transform
     * @param angle  the angle in degrees
     * @return the rotated list of points
     */
    private static @NotNull Vector<Vector2> transformCoordinateSystem(@NotNull Vector<Vector2> points, double angle) {
        angle = Math.toRadians(angle);
        Vector<Vector2> finalPoints = new Vector<>();
        finalPoints.add(new Vector2(0, 0)); // first point is always on origin
        double translatX = points.firstElement().x * Math.cos(angle) + points.firstElement().y * Math.sin(angle);
        double translatY = -points.firstElement().x * Math.sin(angle) + points.firstElement().y * Math.cos(angle);
        for (int i = 1; i < points.size(); i++) {
            double x = points.get(i).x * Math.cos(angle) + points.get(i).y * Math.sin(angle) - translatX;
            double y = -points.get(i).x * Math.sin(angle) + points.get(i).y * Math.cos(angle) + -translatY;
            finalPoints.add(new Vector2(x, y));
        }
        return finalPoints;
    }


    /**
     * Takes a list of points, and returns the part of the polygon which can be used to place a bit.
     * Section acquisition is done clockwise.
     *
     * @param polyPoints the points on which the bit will be placed
     * @param startPoint the point on which the left side of the bit will be placed. startPoint must be on the polygon.
     * @return a vector of vector2, the part of the polygon which can be used to place a bit
     */
    public static @NotNull Vector<Vector2> getSectionPointsFromBound(@NotNull Vector<Vector2> polyPoints, Vector2 startPoint) {

        double bitLength = CraftConfig.bitLength;

        // first we look for the segment on which the startPoint is.
        int startIndex = 0;
        for (int i = 0; i < polyPoints.size() - 1; i++) {
            Segment2D segment2D = new Segment2D(polyPoints.get(i), polyPoints.get(i + 1));
            if (isPointOnSegment(startPoint, segment2D)) {
                startIndex = i + 1;
                break;
            }
        }

        // so we will get the points starting from the one situated at startIndex, and add them to sectionPoints Vector, plus the startPoint
        Vector<Vector2> sectionPoints = new Vector<>();
        sectionPoints.add(startPoint); // first we add the startPoint which is the first point of the section

        // direct distance between start point and selected point.
        double d = Vector2.dist(startPoint, polyPoints.get(startIndex));
        int iPoint = startIndex;

        // we add all the point that are at less than bitLength distance from the startPoint
        boolean revolutionCompleted = false;
        while (d < bitLength && !revolutionCompleted) {
            sectionPoints.add(polyPoints.get(iPoint));
            iPoint++;
            if (iPoint == startIndex) { // we have browsed all the points of the bound
                revolutionCompleted = true;
            }
            if (iPoint == polyPoints.size()) { // come back to index 0
                iPoint = 0;
            }
            d = Vector2.dist(startPoint, polyPoints.get(iPoint));
        }

        // this segment intersects with a circle : center -> startPoint; radius -> bitLength
        Segment2D segment = new Segment2D(polyPoints.get(iPoint - 1), polyPoints.get(iPoint));

        // find this intersection : this is the last point of the section
        sectionPoints.add(circleAndSegmentIntersection(startPoint, bitLength,
                segment));

        //AI_Tool.dataPrep.pointsADessiner.addAll(sectionPoints);

        return sectionPoints;
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
        double[] coefs_inverse = fitter.fit(weightedObservedPoints.toList());
        return Math.toDegrees(Math.atan(coefs_inverse[1]));
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
        int nbPoints = 30;
        return repopulateWithNewPoints(nbPoints, sectionPoints);
    }

    /**
     * Repopulate a section a points with new points. Doesn't keep old points.
     *
     * @param nbNewPoints the number of points to add between two points.
     * @param points      the section of points to repopulate
     * @return the section repopulated with new points.
     */
    private static @NotNull Vector<Vector2> repopulateWithNewPoints(int nbNewPoints, @NotNull Vector<Vector2> points) {

        Vector<Vector2> newPoints = new Vector<>();
        Vector<Double> segmentLength = new Vector<>();
        // faire un tableau de longueurs des segments initiaux
        for (int i = 0; i < points.size() - 1; i++) {
            double size = Math.sqrt(Math.pow(points.get(i).x - points.get(i + 1).x, 2)
                    + Math.pow(points.get(i).y - points.get(i + 1).y, 2));
            segmentLength.add(size);
        }
        double spacing = segmentLength.stream().mapToDouble(Double::valueOf).sum() / (nbNewPoints - 1);

        double baseSegmentSum = 0;
        double newSegmentSum = 0;
        int basePointsIndex = 0;

        // --- Placer chaque nouveau point l'un après l'autre ---

        for (int i = 0; i < nbNewPoints; i++) { // Placer un nouveau point

            double absNewPoint;
            double ordNewPoint;

            // --- selection du segment initial sur lequel on va placer le nouveau point---
            //System.out.println("baseSegmentSum + segmentLength = " + baseSegmentSum + segmentLength[basePointsIndex] + " newSegmentSum = " + newSegmentSum);
            while (basePointsIndex < points.size() - 2 && baseSegmentSum + segmentLength.get(basePointsIndex) <= newSegmentSum) {
                baseSegmentSum += segmentLength.get(basePointsIndex);
                basePointsIndex += 1;
            }

            //Calculer l'angle du segment par rapport à l'horizontale
            double segmentAngle;
            //FIXME @Andre la condition est en double !!
            if (points.get(basePointsIndex).x == points.get(basePointsIndex + 1).x
                    && points.get(basePointsIndex).y <= points.get(basePointsIndex + 1).y) { // alors segment vertical vers le haut
                segmentAngle = Math.PI / 2;
            } else if (points.get(basePointsIndex).x == points.get(basePointsIndex + 1).x
                    && points.get(basePointsIndex).y <= points.get(basePointsIndex + 1).y) { // alors segment vertical vers le haut)
                segmentAngle = -Math.PI / 2;
            } else {
                segmentAngle = Math.atan((points.get(basePointsIndex + 1).y - points.get(basePointsIndex).y)
                        / (points.get(basePointsIndex + 1).x - points.get(basePointsIndex).x)); // Coef directeur du segment
            }

            int sign = 1;
            if (points.get(basePointsIndex + 1).x < points.get(basePointsIndex).x) {
                sign = -1;
            }

            absNewPoint = points.get(basePointsIndex).x + sign * (newSegmentSum - baseSegmentSum) * Math.cos(segmentAngle);
            ordNewPoint = points.get(basePointsIndex).y + sign * (newSegmentSum - baseSegmentSum) * Math.sin(segmentAngle);

            newPoints.add(new Vector2(absNewPoint, ordNewPoint));

            newSegmentSum += spacing;

        }
        return newPoints;
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
        Vector<Double> coefsX = new Vector<>();
        Vector<Double> coefsY = new Vector<>();
        for (int i = 0; i < coefficients_inverseX.length; i++) {
            coefsX.add(coefficients_inverseX[coefficients_inverseX.length - i - 1]);
            coefsY.add(coefficients_inverseY[coefficients_inverseX.length - i - 1]);
        }
        // return result
        Vector<Vector<Double>> coefficients = new Vector<>();
        coefficients.add(coefsX);
        coefficients.add(coefsY);
        return coefficients;
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

        Vector2 nextBitStartPoint = getBitAndContourSecondIntersectionPoint(bit, boundPoints);

        if (nextBitStartPoint != null) {
            return nextBitStartPoint;
        } else {
            throw new Exception("The bit start point has not been found.");
        }
    }


    /**
     * Returns points all points associated with a Bit2D.
     * Points associated are the points of the Slice from the startPoint of the Bit2D,
     * till the distance with the point become greater than the lengh of a Bit2D.
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
        boolean boundfound = false;
        while (iContour < boundsList.size() && !boundfound) {
            startPoint = getBitAndContourFirstIntersectionPoint(bit2D, boundsList.get(iContour));
            if (startPoint != null)
                boundfound = true;
            iContour++;
        }

        // finds the points associated whith the bit, using the startPoint and the bound previously found
        if (startPoint != null)
            return getSectionPointsFromBound(boundsList.get(iContour - 1), startPoint);
        else
            throw new Exception("The bit start point has not been found.");
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

        // first we fill an vector of segments with the points of the bound :
        Vector<Segment2D> boundSegments = new Vector<>();
        for (int i = 0; i < boundPoints.size() - 1; i++) {
            boundSegments.add(
                    new Segment2D(boundPoints.get(i), boundPoints.get(i + 1)));
        }

        // We will have to scan each segment of the bound, to check if an edge of the bit intersects with it.
        // But we have to start scanning by a segment whose its start is not under the bit, otherwise the intersection
        // point found won't be the good one.
        // So first we have to find a segment whose its start is not under the bit.

        Polygon rectangle = new Polygon();
        bit.getBitSidesSegments().forEach(rectangle::addEnd);
        Area bitRectangleArea = new Area(rectangle.toPath2D());

        int startSegIndex = 0;

        while (bitRectangleArea.contains(boundSegments.get(startSegIndex).start.x,
                boundSegments.get(startSegIndex).start.y)) {
            startSegIndex++;
        }


        // finally we can scan the bound, starting with segment at index startSegIndex.
        boolean scanCompleted = false;
        int iSeg = startSegIndex;

        while (!scanCompleted) { //look for an intersecion


            // sometimes there will be more than 1 bit's edges intersecting a segment. We have to make sure that
            // we return the first of theses intersections. So we will store all intersection points and return
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
     * Looks for an intersection point between a side of a bit and a closed contour (bound). TJe point returned
     * is the first intersecton between the bit and the contour, while scanning the contour in the direction of
     * increasing indices of the points of the contour.
     *
     * @param bit         a {@link Bit2D}.
     * @param boundPoints the points of the contour.
     * @return the first intersection between the bit and the closed contour.
     */
    public static Vector2 getBitAndContourSecondIntersectionPoint(@NotNull Bit2D bit, @NotNull Vector<Vector2> boundPoints) {

        Vector<Segment2D> boundSegments = getSegment2DS(boundPoints);
        Vector<Segment2D> bitSegments = bit.getBitSidesSegments();

        Vector<Vector2> intersectionPoints = new Vector<>();

        for (Segment2D boundSegment : boundSegments) {

            Vector<Vector2> intersectionsWithSegment = new Vector<>();

            for (Segment2D bitSegment : bitSegments) {

                if (Segment2D.doSegmentsIntersect(boundSegment, bitSegment)) {
                    Vector2 inter = Segment2D.getIntersectionPoint(bitSegment, boundSegment);
                    intersectionsWithSegment.add(inter);
                }
            }


            while (!intersectionsWithSegment.isEmpty()) {

                double distMin = Double.POSITIVE_INFINITY;
                Vector2 firstPoint = null;
                for (Vector2 inter : intersectionsWithSegment) {
                    if (Vector2.dist2(inter, boundSegment.start) < distMin) {
                        firstPoint = inter;
                        distMin = Vector2.dist2(inter, boundSegment.start);
                    }
                }
                intersectionPoints.add(firstPoint);
                intersectionsWithSegment.remove(firstPoint);
            }

        }
        return intersectionPoints.get(1);
    }

    /**
     * Converts a list of {@link Vector2} to a list of {@link Vector} that would connect each point to the other,
     * following the order of the list of points given as entry.
     *
     * @param points the points requiring to be converted into segments.
     * @return the segments resulting from the conversion.
     */
    @NotNull
    public static Vector<Segment2D> getSegment2DS(@NotNull Vector<Vector2> points) {
        Vector<Segment2D> sectionSegments = new Vector<>();
        for (int i = 0; i < points.size() - 1; i++) {
            sectionSegments.add(new Segment2D(
                    points.get(i),
                    points.get(i + 1)
            ));
        }
        return sectionSegments;
    }

    /**
     * Find an approximation of the intersection point between a segment and a circle
     * If there is more than one intersection, this method will return the point that is
     * the closest to the end of the segment.
     * Initial condition : an intersection should exist
     *
     * @param center center of the circle
     * @param radius radius of the circle
     * @param seg    segment
     * @return an approximation of the intersection point between the segment and the circle
     */
    public static @NotNull Vector2 circleAndSegmentIntersection(Vector2 center, double radius, @NotNull Segment2D seg) {

        double step = 0.01;

        double t = 1;
        double x = seg.end.x;
        double y = seg.end.y;
        double dist = Vector2.dist(center, new Vector2(x, y));

        while (dist > radius) {
            t = t - step;
            x = seg.start.x + t * (seg.end.x - seg.start.x);
            y = seg.start.y + t * (seg.end.y - seg.start.y);
            dist = Vector2.dist(center, new Vector2(x, y));
        }

        return new Vector2(x, y);
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
}