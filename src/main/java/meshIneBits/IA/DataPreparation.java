package meshIneBits.IA;

import meshIneBits.Bit2D;
import meshIneBits.IA.IA_util.AI_Exception;
import meshIneBits.IA.IA_util.Curve;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Polygon;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Vector;
import java.util.stream.IntStream;

public class DataPreparation {
    //DEBUGONLY
    public Vector2 A = new Vector2(0, 0);
    public Vector2 B = new Vector2(0, 0);
    public Vector2 C = new Vector2(0, 0);
    public Vector2 D = new Vector2(0, 0);
    public AffineTransform transformArea = new AffineTransform();

    public Vector<Vector2> pointsContenus = new Vector<>();
    public Vector<Vector2> pointsADessiner = new Vector<>();
    public Path2D cutPathToDraw = new Path2D.Double();

    public Segment2D currentSegToDraw = new Segment2D(A, B);
    public Segment2D currentSegToDraw2 = new Segment2D(A, B);

    public Polygon poly = new Polygon();

    public Area area = new Area();
    //DEBUGONLY END

    public Area areaToDraw = null;
    public Vector<Bit2D> Bits = new Vector<>();
    public Vector<String> scores = new Vector<>();
    public boolean hasNewBitToDraw;

    /**
     * Renvoie les points de chaque contour d'une Slice.
     * Les points sont réarrangés pour être à nouveau dans l'ordre.
     * Fait appel à rearrangeSegments puis rearrangePoints.
     *
     * @param currentSlice
     * @return
     */
    public static Vector<Vector<Vector2>> getBoundsAndRearrange(Slice currentSlice) {
        Vector<Vector<Vector2>> boundsList = new Vector<>();
        Vector<Vector<Segment2D>> borderList = rearrangeSegments((Vector<Segment2D>) currentSlice.getSegmentList().clone());

        for (Vector<Segment2D> border : borderList) {
            Vector<Vector2> unorderedPoints = computePoints(border);
            boundsList.add(rearrangePoints(unorderedPoints));
        }
        return boundsList;
    }

    public static Vector<Vector<Segment2D>> rearrangeSegments(Vector<Segment2D> segmentList) { //return more than one Vector<Segment2D>, if there's more than one border on the slice
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

    public static Vector<Vector2> rearrangePoints(Vector<Vector2> pointList) {
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
     * @param segmentList
     * @return
     */
    public static Vector<Vector2> computePoints(Vector<Segment2D> segmentList) {
        Vector<Vector2> pointList = new Vector<>();
        for (Segment2D segment : segmentList) {
            pointList.add(new Vector2(segment.start.x, segment.start.y));
        }
        pointList.remove(0);
        return pointList;
    }


    public static Vector<Segment2D> searchNextSegment(Segment2D segment, Vector<Segment2D> segmentList, Vector<Segment2D> newSegmentList) {
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


    public static Vector<Vector2> getSectionInLocalCoordinateSystem(Vector<Vector2> points) {

        //map for more accurate result
        Vector<Vector2> mappedPoints = repopulateWithNewPoints(30, points);

        //get an angle in degrees
        double angle = getSectionOrientation(mappedPoints);

        //transform mapped points in local system
        Vector<Vector2> transformedMappedPoints = transformCoordinateSystem(mappedPoints, angle);

        //check if abscissa axe and and section are directed in the same direction.
        if (!arePointsMostlyToTheRight(transformedMappedPoints)) {
            angle += Math.PI; //rotate coordinate system
        }

        return transformCoordinateSystem(points, angle);
    }


    /**
     * @param points
     * @param angle  in degrees
     * @return
     */
    private static Vector<Vector2> transformCoordinateSystem(Vector<Vector2> points, double angle) {
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


    public static double getAngle(Vector2 A, Vector2 B, Vector2 C) {
        Vector2 AB = B.sub(A);
        Vector2 BC = B.sub(C);

        //double angle = Math.abs( AB.getEquivalentAngle2() - BC.getEquivalentAngle2() );
        double angle = Math.acos((Vector2.dist2(B, A) + Vector2.dist2(B, C) - Vector2.dist2(A, C))
                / (2 * (Vector2.dist(B, A) * Vector2.dist(B, C))));
        return Math.toDegrees(angle);
    }


    public static boolean doIntersect(Segment2D seg1, Segment2D seg2) {
        // points
        Vector2 A = seg1.start;
        Vector2 B = seg1.end;
        Vector2 C = seg2.start;
        Vector2 D = seg2.end;

        double DAC = getAngle(D, A, C);
        double ACB = getAngle(A, C, B);
        double CBD = getAngle(C, B, D);
        double BDA = getAngle(B, D, A);


        double sum = Math.abs(DAC) + Math.abs(ACB) + Math.abs(CBD) + Math.abs(BDA);

        // if sum is not 2pi, then ABCD is a complex quadrilateral (2 edges cross themselves).
        // This means that segments intersect
        double errorThreshold = 0.1;
        return Math.abs(360 - sum) < errorThreshold;
    }

    public static Vector2 getIntersectionPoint(Segment2D seg1, Segment2D seg2) {
        // points
        Vector2 A = seg1.start;
        Vector2 B = seg1.end;
        Vector2 C = seg2.start;
        Vector2 D = seg2.end;

        if (A.asGoodAsEqual(C) || A.asGoodAsEqual(D)) {
            return A;
        }
        else if (B.asGoodAsEqual(C) || B.asGoodAsEqual(D)){
            return B;
        }
        else if (doIntersect(seg1, seg2)) {

            double AD = Vector2.dist(A, D);
            double AID = 180 - getAngle(D, A, B) - getAngle(C, D, A);
            double IA =  (AD/Math.sin(Math.toRadians(AID))) * Math.sin(Math.toRadians(getAngle(A, D, C)));

            return   A.add(B.sub(A).normal().mul(IA));
        }
        return null;
    }


    /**
     * Takes a list of points, and returns the part of the polygon which can be used to place a bit.
     * Section acquisition is done clockwise.
     *
     * @param polyPoints the points on which the bit will be placed
     * @param startPoint the point on which the left side of the bit will be placed. startPoint must be on the polygon.
     * @return a vector of vector2, the part of the polygon which can be used to place a bit
     */
    public static Vector<Vector2> getSectionPoints(Vector<Vector2> polyPoints, Vector2 startPoint) {

        double bitLength = CraftConfig.bitLength;

        // first we look for the segment on which the startPoint is.
        int startIndex = 0;
        for (int i = 0; i < polyPoints.size(); i++) {
            Segment2D segment2D = new Segment2D(polyPoints.get(i), polyPoints.get(i+1));
            if (isPointOnSegment(startPoint, segment2D)) {
                startIndex = i+1;
                break;
            }
        }
        System.out.println("startIndex " + startIndex);
        System.out.println("point " + polyPoints.get(startIndex));
        //AI_Tool.dataPrep.pointsADessiner.add(polyPoints.get(startIndex));

        // so we will get points situated on and after point at startIndex and add them to sectionPoints Vector, plus the startPoint
        Vector<Vector2> sectionPoints = new Vector<>();
        sectionPoints.add(startPoint); // first we add the startPoint which is the first point of the section

        // direct distance between start point and selected point.
        double d = Vector2.dist(startPoint, polyPoints.get(startIndex));
        int iPoint = startIndex;

        while (d < bitLength) { // we add all the point that are at less than bitLength distance from the startPoint
            sectionPoints.add(polyPoints.get(iPoint));
            iPoint++;
            if(iPoint==polyPoints.size()){ // coma back to index 0
                iPoint = 0;
            }
            d = Vector2.dist(startPoint, polyPoints.get(iPoint));
        }

        // this segment intersects with a circle : center -> startPoint; radius -> bitLength
        Segment2D segment = new Segment2D(polyPoints.get(iPoint-1), polyPoints.get(iPoint));

        // find this intersection : this is the last point of the section
        sectionPoints.add(circleAndSegmentIntersection(startPoint, bitLength,
                segment));

        AI_Tool.dataPrep.pointsADessiner.addAll(sectionPoints);


        return sectionPoints;
    }


    private static boolean isPointOnSegment(Vector2 p, Segment2D s){
        double errorAccepted = 5;
        return Math.abs(Vector2.dist(s.start, p) + Vector2.dist(s.end, p) - s.getLength()) < errorAccepted;
    }


    // Initial conditions : intersection exists,the segment is cutting
    // the circle at one unique point.
    //parameters : circle center, segment's first and second point.
    public static Vector2 circleAndSegmentIntersection2(Vector2 center, double radius, Segment2D segment2D) {

        Vector2 p0 = segment2D.start;
        Vector2 p1 = segment2D.end;

        //we express the segment's equation as x(t) and y(t) where t is between 0 and 1.
        //roots of this polynomial are values of t where the circle and the segment intersect:
        //double a = Math.pow((p1.x-p0.x), 2) + Math.pow((p1.y-p0.y), 2);
        //double b = 2*((p1.x-p0.x)*(p0.x-center.x) + (p1.y-p0.y)*(p0.y-center.y));
        //double c = -2*p0.x*center.x + center.x*center.x -2*p0.y*center.y + center.y*center.y;
        double a = Math.pow((p1.x - p0.x), 2) + Math.pow((p1.y - p0.y), 2);
        double b = 2 * ((p1.x - p0.x) * (p0.x - center.x) + (p1.y - p0.y) * (p0.y - center.y));
        double c = Math.pow(p0.x - center.x, 2) + Math.pow(p0.y - center.y, 2) - radius * radius;

        //TODO improve the following part
        double delta = b * b - 4 * a * c;
        double t;
        if (delta == 0) { // le point est situé sur le cercle
            t = -b / (2d * a);
        } else {
            t = (-b - Math.sqrt(delta)) / (2d * a);
            if (t <= 0 || t >= 1) {
                t = (-b + Math.sqrt(delta)) / (2d * a);
            }
        }
        //compute coordinates
        double x = p0.x + t * ((p1.x - p0.x));
        double y = p0.y + t * ((p1.y - p0.y));
        return new Vector2(x, y);
    }


    public static Vector2 circleAndSegmentIntersection(Vector2 center, double radius, Segment2D seg){

        double bitLength = CraftConfig.bitLength;

        double step = 0.01;

        double t = 1;
        double x = seg.end.x;
        double y = seg.end.y;
        double dist = Vector2.dist(center, new Vector2(x, y));

        while(dist>bitLength) {
            t = t - step;
            x = seg.start.x + t*(seg.end.x-seg.start.x);
            y = seg.start.y + t*(seg.end.y-seg.start.y);
            dist = Vector2.dist(center, new Vector2(x, y));
        }

        return new Vector2(x, y);
    }


    public static Double getSectionOrientation(Vector<Vector2> points) {
        // prepare fitting
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);//degré
        WeightedObservedPoints weightedObservedPoints = new WeightedObservedPoints();
        weightedObservedPoints.add(1000, points.get(0).x, points.get(0).y);

        for (int i = 1; i < points.size(); i++) {
            weightedObservedPoints.add(points.get(i).x, points.get(i).y);
        }

        // fit
        double[] coefs_inverse = fitter.fit(weightedObservedPoints.toList());

        return Math.toDegrees(Math.atan(coefs_inverse[1]));

    }


    public static boolean arePointsMostlyToTheRight(Vector<Vector2> points) {
        int leftPoints = 0;
        int rightPoints = 0;
        for (Vector2 point : points) {
            if (point.x < 0) {
                leftPoints++;
            } else {
                rightPoints++;
            }
        }
        return leftPoints < rightPoints;
    }


    public static Vector<Vector2> getInputPointsForDL(Vector<Vector2> sectionPoints) {
        int nbPoints = 30;
        return repopulateWithNewPoints(nbPoints, sectionPoints);
    }


    public Vector<Vector> getInputSlopesForDL(Vector<Vector2> sectionPoints) {
        Curve inputCurve = new Curve("input curve");
        inputCurve.generateCurve(sectionPoints);
        Curve[] splitCurve = inputCurve.splitCurveInTwo();
        Curve xCurve = splitCurve[0];
        Curve yCurve = splitCurve[1];

        /*Grapheur gr = new Grapheur();
        gr.displayGraph(xCurve);
        gr.displayGraph(yCurve);*/

        // prepare fitting
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(4);//degré
        WeightedObservedPoints weightedObservedPointsX = new WeightedObservedPoints();
        WeightedObservedPoints weightedObservedPointsY = new WeightedObservedPoints();
        for (int i = 0; i < inputCurve.getN_points(); i++) {
            weightedObservedPointsX.add(xCurve.getPoints().get(i).x, xCurve.getPoints().get(i).y);
            weightedObservedPointsY.add(yCurve.getPoints().get(i).x, yCurve.getPoints().get(i).y);
        }
        // fit
        double[] coefs_inverseX = fitter.fit(weightedObservedPointsX.toList());
        double[] coefs_inverseY = fitter.fit(weightedObservedPointsY.toList());
        // invert coefficients
        Vector<Double> coefsX = new Vector<>();
        Vector<Double> coefsY = new Vector<>();
        for (int i = 0; i < coefs_inverseX.length; i++) {
            coefsX.add(coefs_inverseX[coefs_inverseX.length - i - 1]);
            coefsY.add(coefs_inverseY[coefs_inverseX.length - i - 1]);
        }
        // return result
        Vector<Vector> coefs = new Vector<>();
        coefs.add(coefsX);
        coefs.add(coefsY);

        Curve x = new Curve("x model");
        Curve y = new Curve("y model");

        x.generateCurve(coefs.get(0), 0, 20, 10);
        y.generateCurve(coefs.get(1), 0, 20, 10);

        //  gr.displayGraph(x);
        //  gr.displayGraph(y);
        return coefs;
    }

    /**
     * Return the next Bit2D start point.
     * It is the intersection between the slice and the end side of the Bit2D.
     *
     * @param bit    the current Bit2D (the last placed Bit2D by AI).
     * @param contourPoints the points of the bounds on which stands the bit.
     * @return the next bit start point. Returns <code>null</code> if none was found.
     */
    public Vector2 getNextBitStartPoint(Bit2D bit, Vector<Vector2> contourPoints) throws AI_Exception {

        Vector<Vector2> contourPointsReverted = new Vector<>();
        for (int i = contourPoints.size() - 1; i >= 0; i--){
            contourPointsReverted.add(contourPoints.get(i));
        }
        return getBitAndContourFirstIntersectionPoint(bit, contourPointsReverted);
    }

    /**
     * Repopulate a section a points with new points.
     * It adds <code>n</code> points between each point given. With for each segment between 2 points <code>n=1 + segmentSize / CraftConfig.bitWidth</code>.
     * The longer the segment is, the more new points will be placed.
     *
     * @param pointsToPopulate the section of points to repopulate
     * @return the section repopulated with new points.
     */
    private Vector<Vector2> repopulateSection(Vector<Vector2> pointsToPopulate) {
        Vector<Vector2> newPoints = new Vector<>();
        for (int i = 0; i < pointsToPopulate.size() - 1; i++) {
            int nbPoints;
            double segmentSize = Vector2.dist(pointsToPopulate.get(i), pointsToPopulate.get(i + 1));
            double bitWidth = CraftConfig.bitWidth; //todo faire que ca dépende de si on prend un quart/demi bit
            nbPoints = (int) (Math.ceil(segmentSize / bitWidth) + 1);

            Vector<Vector2> segment = new Vector<>();
            segment.add(pointsToPopulate.get(i));
            segment.add(pointsToPopulate.get(i + 1));

            newPoints.addAll(repopulateWithNewPoints(nbPoints, segment));
            newPoints.remove(newPoints.size() - 1);
        }

        return newPoints;
    }

    /**
     * Repopulate a section a points with new points. Doesn't keep old points.
     *
     * @param nbNewPoints the number of points to add between two points.
     * @param points      the section of points to repopulate
     * @return the section repopulated with new points.
     */
    public static Vector<Vector2> repopulateWithNewPoints(int nbNewPoints, Vector<Vector2> points) {

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
            double segmentAngle; //todo problème en dessous à résoudre, le code est en double
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
     * Returns the four segments of a Bit2D (a Bit2D not cut by cut paths)
     *
     * @param bit the Bit2D
     * @return a Vector of the four segments.
     */
    public Vector<Segment2D> getBitSidesSegments(Bit2D bit) {

        Vector<Segment2D> sides = new Vector<>();
        //generates the 4 points which makes the rectangular bit
        Vector2 bitOrigin = bit.getOrigin();
        Vector2 A = new Vector2(
                bit.getLength() / 2,
                bit.getWidth() / 2)
                .rotate(bit.getOrientation())
                .add(new Vector2(
                        bitOrigin.x,
                        bitOrigin.y
                ));
        Vector2 B = new Vector2(
                bit.getLength() / 2,
                -bit.getWidth() / 2)
                .rotate(bit.getOrientation())
                .add(new Vector2(
                        bitOrigin.x,
                        bitOrigin.y
                ));

        Vector2 C = new Vector2(
                -bit.getLength() / 2,
                -bit.getWidth() / 2)
                .rotate(bit.getOrientation())
                .add(new Vector2(
                        bitOrigin.x,
                        bitOrigin.y
                ));

        Vector2 D = new Vector2(
                -bit.getLength() / 2,
                bit.getWidth() / 2)
                .rotate(bit.getOrientation())
                .add(new Vector2(
                        bitOrigin.x,
                        bitOrigin.y
                ));

        sides.add(new Segment2D(A, B));
        sides.add(new Segment2D(B, C));
        sides.add(new Segment2D(C, D));
        sides.add(new Segment2D(D, A));

        return sides;
    }

    /**
     * Check if a Segment2D contains a point.
     *
     * @param seg   the segment
     * @param point the point we want to test
     * @return <code>true</code> if the segment contains the point. <code>false</code> otherwise
     */
    public boolean contains(Segment2D seg, Vector2 point) {
        if (seg.start.x < seg.end.x) {
            if (seg.start.y < seg.end.y) {
                if (seg.start.x <= point.x && point.x <= seg.end.x && seg.start.y <= point.y && point.y <= seg.end.y) {
                    return true;
                }
            } else {
                if (seg.start.x <= point.x && point.x <= seg.end.x && seg.start.y >= point.y && point.y >= seg.end.y) {
                    return true;
                }
            }

        } else {
            if (seg.start.y < seg.end.y) {
                if (seg.start.x >= point.x && point.x >= seg.end.x && seg.start.y <= point.y && point.y <= seg.end.y) {
                    return true;
                }
            } else {
                if (seg.start.x >= point.x && point.x >= seg.end.x && seg.start.y >= point.y && point.y >= seg.end.y) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns points all points associated with a Bit2D.
     * Points associated are the points of the Slice from the startPoint of the Bit2D,
     * till the distance with the point become greater than the lengh of a Bit2D.
     *
     * @param bit2D The Bit2D we want to get the points associated with.
     * @return the associated points.
     */
    public Vector<Vector2> getBitAssociatedPoints(Bit2D bit2D) throws AI_Exception {

    //First we get all the points of the Slice. getContours returns the points already rearranged.
        Vector<Vector<Vector2>> boundsListToPopulate = getBoundsAndRearrange(AI_Tool.getMeshController().getCurrentLayer().getHorizontalSection());
        //todo on veut ptet pas le current layer si?

        Vector<Vector<Vector2>> boundsList = new Vector<>();
        for (int i = 0; i < boundsListToPopulate.size(); i++) {
            boundsList.add(repopulateSection(boundsListToPopulate.get(i)));
        }

        //We search which bound intersects with the bit.
        Polygon rectangle = new Polygon();
        getBitSidesSegments(bit2D).forEach(rectangle::addEnd);
        Area bitRectangleArea = new Area(rectangle.toPath2D());

        Vector<Vector2> boundToCheck = new Vector<>();
        boolean hasBeenUnderBit = false;

        Vector<Vector2> pointsToCheckAssociated = new Vector<>(); //Will be just the part of a bound from the first point contained by the bit
        for (Vector<Vector2> bound : boundsList) {
            bound = repopulateSection(bound); //todo faire en sorte que le nb de points à ajouté soit choisi par l'user, mais > au min
            for (Vector2 point : bound) {
                if (bitRectangleArea.contains(point.x, point.y)) {
                    hasBeenUnderBit = true;
                    boundToCheck = bound;
                }
                if (hasBeenUnderBit) {
                    pointsToCheckAssociated.add(point);
                }
            }
            if (hasBeenUnderBit) {
                break;
            }
        }

        //Checks for each point if it is in the radius of the bit from the start point
        Vector2 startPoint = getBitAndContourFirstIntersectionPoint(bit2D, boundToCheck);
        return getBitAssociatedPoints(bit2D, startPoint);
    }

    /**
     * Returns points all points associated with a Bit2D.
     * Points associated are the points of the Slice from the startPoint of the Bit2D,
     * till the distance with the point become greater than the lengh of a Bit2D.
     *
     * @param startPoint The startPoint of the bit.
     * @return the associated points.
     */
    public Vector<Vector2> getBitAssociatedPoints(Bit2D bit2D, Vector2 startPoint) {
        //First we get all the points of the Slice. getContours returns the points already rearranged.
        Vector<Vector<Vector2>> boundsList = getBoundsAndRearrange(AI_Tool.getMeshController().getCurrentLayer().getHorizontalSection());
        //todo on veut ptet pas le current layer si?


        //We search which bound intersects with the bit.
        Polygon rectangle = new Polygon();
        getBitSidesSegments(bit2D).forEach(rectangle::addEnd);
        Area bitRectangleArea = new Area(rectangle.toPath2D());

        boolean hasBeenUnderBit = false;
        Vector<Vector2> pointsToCheckAssociated = new Vector<>(); //Will be just the part of a bound from the first point contained by the bit
        for (Vector<Vector2> bound : boundsList) {
            bound = repopulateSection(bound); //todo faire en sorte que le nb de points à ajouté soit choisi par l'user, mais > au min
            for (Vector2 point : bound) {
                if (bitRectangleArea.contains(point.x, point.y)) {
                    hasBeenUnderBit = true;
                }
                if (hasBeenUnderBit) {
                    pointsToCheckAssociated.add(point);
                }
            }
            if (hasBeenUnderBit) {
                break;
            }
        }

        Vector<Vector2> associatedPoints = new Vector<>();

        // 1) add the first point (intersection between slice's contour and bit's sides)
        associatedPoints.add(startPoint);

        // 2) add interior points
        //Checks for each point if it is in the radius of the bit from the start point
        for (Vector2 point : pointsToCheckAssociated) {
            if (Vector2.dist(startPoint, point) <= CraftConfig.bitLength)//todo prendre en compte les quarts/demis bits
                associatedPoints.add(point);
        }

        // 3) add last point
        //associatedPoints.add(circleAndSegmentIntersection(startPoint, CraftConfig.bitLength, ))

        this.pointsContenus = (Vector<Vector2>) associatedPoints.clone();
        return associatedPoints;
    }


    // only used in Genetic
    public Vector<Vector2> getBitAssociatedPoints(Bit2D bit2D, Vector2 startPoint, Vector<Vector2> points) {
        Vector<Vector2> bound = (Vector<Vector2>) points.clone();
        //We search which bound intersects with the bit.
        Polygon rectangle = new Polygon();
        getBitSidesSegments(bit2D).forEach(rectangle::addEnd);
        Area bitRectangleArea = new Area(rectangle.toPath2D());

        boolean hasBeenUnderBit = false;
        Vector<Vector2> pointsToCheckAssociated = new Vector<>(); //Will be just the part of a bound from the first point contained by the bit
        bound = repopulateSection(bound); //todo faire en sorte que le nb de points à ajouté soit choisi par l'user, mais > au min
        for (Vector2 point : bound) {
            if (bitRectangleArea.contains(point.x, point.y)) {
                hasBeenUnderBit = true;
            }
            if (hasBeenUnderBit) {
                pointsToCheckAssociated.add(point);
            }
        }

        //Checks for each point if it is in the radius of the bit from the start point
        Vector<Vector2> associatedPoints = new Vector<>();
        for (Vector2 point : pointsToCheckAssociated) {
            if (Vector2.dist(startPoint, point) <= CraftConfig.bitLength)//todo prendre en compte les quarts/demis bits
                associatedPoints.add(point);
        }
        this.pointsContenus = (Vector<Vector2>) associatedPoints.clone();
        return associatedPoints;
    }

    /**
     * returns the first intersection point between the contour and bit's edges
     * @param bit a bit
     * @param contourPoints the points of the contour
     * @return the first intersection point between the contour and bit's edges
     * @throws AI_Exception if no point has been found
     */
    public Vector2 getBitAndContourFirstIntersectionPoint(Bit2D bit, Vector<Vector2> contourPoints) throws AI_Exception {

        // get sides of the bit as Segment2Ds (will be used later)
        Vector<Segment2D> bitSides = getBitSidesSegments(bit);


        // first we fill an array of segments with the points of the contour :
        Vector<Segment2D> contourSegments = new Vector<>();
        for (int i = 0; i < contourPoints.size() - 1; i++) {
            contourSegments.add(
                    new Segment2D(contourPoints.get(i), contourPoints.get(i + 1)));
        }

        // We will have to scan each segment of the contour, to check if an edge of the bit intersects with it.
        // But we have to start scanning by a segment whose its start is not under the bit, otherwise the intersection
        // point found won't be the good one.
        // So first we have to find a segment whose its start is not under the bit.

        Polygon rectangle = new Polygon();
        getBitSidesSegments(bit).forEach(rectangle::addEnd);
        Area bitRectangleArea = new Area(rectangle.toPath2D());

        int startSegIndex = 0;

        while (bitRectangleArea.contains(contourSegments.get(startSegIndex).start.x,
                contourSegments.get(startSegIndex).start.y)) {
            startSegIndex++;
        }


        // finally we can scan the contour, starting with segment at index startSegIndex.
        boolean scanCompleted = false;
        int iSeg = startSegIndex;

        while (!scanCompleted) { //look for an intersecion


            // sometimes there will be more than 1 bit's edges intersecting a segment. We have to make sure that
            // we return the first of theses intersections. So we will store all intersection points and return
            // the one which its distance with segment's start is the lowest.
            Vector<Vector2> intersectionPoints = new Vector<>();


            //fill intersectionPoints Vector<> by checking intersections with all bit's sides
            for (Segment2D bitSide : bitSides) {
                Vector2 intersectionPoint = getIntersectionPoint(bitSide, contourSegments.get(iSeg));
                if (intersectionPoint != null) { // then we store this intersection
                    intersectionPoints.add(intersectionPoint);
                }
            }

            // if we have some intersections we have to return the first one (as explained above)
            if (! intersectionPoints.isEmpty()){
                double maxDist2 = 1000000;
                Vector2 firstIntersectionPoint = null; // can't be null
                for(Vector2 intersectPoint : intersectionPoints){

                    double dist2 = Vector2.dist2(contourSegments.get(iSeg).start, intersectPoint);
                    if (dist2 < maxDist2){
                        maxDist2 = dist2;
                        firstIntersectionPoint = intersectPoint;
                    }

                }
                return firstIntersectionPoint;
            }


            // increment
            iSeg++;
            if (iSeg == contourSegments.size()) {
                iSeg = 0;
            }

            // check if scan completed = we reached the segment at index startSegIndex again
            if (iSeg == startSegIndex) {
                scanCompleted = true;
            }
        }

        // reached only if no intersection has been found
        throw new AI_Exception("The bit start point has not been found.");
    }


    // just for tests
    public void tests(Bit2D bit2D){

        Vector<Vector2> contourPoints = getBoundsAndRearrange(AI_Tool.getMeshController().getCurrentLayer().getHorizontalSection()).get(0);

        Vector2 startPoint = null;
        try {
            startPoint = getBitAndContourFirstIntersectionPoint(bit2D, contourPoints);
        } catch (AI_Exception e) {
            e.printStackTrace();
        }
        getSectionPoints(contourPoints, startPoint);
        //AI_Tool.dataPrep.pointsADessiner.addAll(getSectionPoints(contourPoints, startPoint));

    }

}
