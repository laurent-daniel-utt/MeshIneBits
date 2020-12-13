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

import static meshIneBits.IA.genetics.Evolution.NB_GEN_MAX;
import static meshIneBits.IA.genetics.Evolution.POP_SIZE;

public class DataPreparation {
    public Vector2 A = new Vector2(0, 0); //debugOnly
    public Vector2 B = new Vector2(0, 0);
    public Vector2 C = new Vector2(0, 0);
    public Vector2 D = new Vector2(0, 0);
    public AffineTransform transformArea = new AffineTransform();

    public Vector<Vector2> pointsContenus = new Vector<>(); //debugOnly
    public Vector<Vector2> pointsADessiner = new Vector<>();
    public Path2D cutPathToDraw = new Path2D.Double();

    public Segment2D currentSegToDraw = new Segment2D(A, B); //debugOnly
    public Segment2D currentSegToDraw2 = new Segment2D(A, B); //debugOnly

    public Polygon poly = new Polygon();
    public Bit2D bit = null;

    public Area area = new Area();
    public double[] scores = new double[POP_SIZE * NB_GEN_MAX + 1];

    public Area areaToDraw = null;

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


    public Vector<Vector2> getSectionInLocalCoordinateSystem(Vector<Vector2> points) {

        //map for more accurate result
        Vector<Vector2> mappedPoints = repopulateWithNewPoints(30, points);

        //get an angle
        double angle = getSectionOrientation(mappedPoints);

        //transform mapped points in local system
        Vector<Vector2> transformedMappedPoints = transformCoordinateSystem(mappedPoints, angle);

        //check if abscissa axe and and section are directed in the same direction.
        if (!arePointsMostlyToTheRight(transformedMappedPoints)) {
            angle += Math.PI; //rotate coordinate system
        }

        return transformCoordinateSystem(points, angle);
    }


    private Vector<Vector2> transformCoordinateSystem(Vector<Vector2> points, double angle) {
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
    //todo on peut surement l'enlever cette méthode? pareil pour circleAndSegmentIntersection()
    public static Vector<Vector2> getSectionToPlaceNewBit(Vector<Vector2> polyPoints, Vector2 startPoint) {
        int startIndex = 0;
        for (int i = 1; i < polyPoints.size(); i++) {
            Segment2D segment2D = new Segment2D(polyPoints.get(i - 1), polyPoints.get(i));
            if (startPoint.isOnSegment(segment2D)) {
                startIndex = i - 1;
                break;
            }
        }
        double bitLength = CraftConfig.bitLength;
        Vector<Vector2> sectionPoints = new Vector<>();
        // direct distance between start point and selected point.
        double d = 0;
        int i = 0;
        while (d < bitLength) {
            sectionPoints.add(polyPoints.get(i));
            i++;
            d = Vector2.dist(polyPoints.get(startIndex), polyPoints.get(i));
        }
        //place the last point of the segment at the distance of one bit from start point.
        sectionPoints.add(circleAndSegmentIntersection(polyPoints.get(startIndex), bitLength,
                polyPoints.get(i - 1), polyPoints.get(i)));
        return sectionPoints;
    }

    // Initial conditions : intersection exists,the segment is cutting
    // the circle at one unique point.
    //parameters : circle center, segment's first and second point.
    public static Vector2 circleAndSegmentIntersection(Vector2 center, double radius, Vector2 p0, Vector2 p1) {
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

    public Double getSectionOrientation(Vector<Vector2> points) {
        // prepare fitting
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);//degré
        WeightedObservedPoints weightedObservedPoints = new WeightedObservedPoints();
        weightedObservedPoints.add(1000, points.get(0).x, points.get(0).y);

        for (int i = 1; i < points.size(); i++) {
            weightedObservedPoints.add(points.get(i).x, points.get(i).y);
        }

        // fit
        double[] coefs_inverse = fitter.fit(weightedObservedPoints.toList());

        return Math.atan(coefs_inverse[1]);

    }


    public boolean arePointsMostlyToTheRight(Vector<Vector2> points) {
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


    public Vector<Vector2> getInputPointsForDL(Vector<Vector2> sectionPoints) {
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
     * @param points the points of the bounds on which stands the bit.
     * @return the next bit start point. Returns <code>null</code> if none was found.
     */
    public Vector2 getNextBitStartPoint(Bit2D bit, Vector<Vector2> points) {
        //todo ajuster pour que ca marche pour des demi/quarts.. de bits
        this.bit = bit.clone();
        Polygon rectangle = new Polygon();
        getBitSidesSegments(bit.clone()).forEach(rectangle::addEnd);
        area = new Area(rectangle.toPath2D());
        Vector<Vector2> clonedPoints = (Vector<Vector2>) points.clone();
        clonedPoints = repopulateSection(clonedPoints);
        for (Vector2 point : clonedPoints) {//debugOnly
            // pointsADessiner.add(point);
        }

        //We are first looking for the first point (at index i) contained by the Area of the bit
        int i;
        for (i = 0; i < clonedPoints.size() - 1; i++) {
            if (area.contains(clonedPoints.get(i).x, clonedPoints.get(i).y)) {
                break;
            }
        }

        //We are now looking for the first point not contained by the Area of the bit.
        //We start from point i.
        for (; i < clonedPoints.size() - 1; i++) {
            if (!area.contains(clonedPoints.get(i).x, clonedPoints.get(i).y))
                break;
        } //todo merge avec getBitStart point, il n'y a que ce for qui change
        Segment2D outGoingSegment = new Segment2D(clonedPoints.get(i - 1), clonedPoints.get(i));

        //We are looking for the intersection between the outGoingSegment and the bounds of the Slice
        Vector2 intersectionPoint;
        Vector<Segment2D> sides = getBitSidesSegments(bit.clone());
        // this.currentSegToDraw = outGoingSegment; //debugOnly
        //pointsADessiner.clear();//debugOnly


        Vector<Segment2D> clonedBitSides = (Vector<Segment2D>) sides.clone();
        for (Segment2D bitSide : clonedBitSides) {
            intersectionPoint = bitSide.intersect(outGoingSegment); //null if parallel

            if (intersectionPoint != null) {
                if (contains(bitSide, intersectionPoint)) {
                    if (contains(outGoingSegment, intersectionPoint)) {
                        // pointsADessiner.add(intersectionPoint); //debugOnly
                        return intersectionPoint;
                    }
                }
            }
        }
        return null;
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
    public Vector<Vector2> repopulateWithNewPoints(int nbNewPoints, Vector<Vector2> points) {

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
     * @param bit The Bit2D we want to get the points associated with.
     * @return the associated points.
     */
    public Vector<Vector2> getBitAssociatedPoints(Bit2D bit) throws AI_Exception {

        //First we get all the points of the Slice. getContours returns the points already rearranged.
        Vector<Vector<Vector2>> boundsList = getBoundsAndRearrange(AI_Tool.getMeshController().getCurrentLayer().getHorizontalSection());

        //We search which bound intersects with the bit.
        Polygon rectangle = new Polygon();
        getBitSidesSegments(bit).forEach(rectangle::addEnd);
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
        Vector2 startPoint = getBitStartPoint(bit, boundToCheck);
        return getBitAssociatedPoints(startPoint);
    }

    /**
     * Returns points all points associated with a Bit2D.
     * Points associated are the points of the Slice from the startPoint of the Bit2D,
     * till the distance with the point become greater than the lengh of a Bit2D.
     *
     * @param startPoint The startPoint of the bit.
     * @return the associated points.
     */
    public Vector<Vector2> getBitAssociatedPoints(Vector2 startPoint) {



        //First we get all the points of the Slice. getContours returns the points already rearranged.
        Vector<Vector<Vector2>> boundsList = getBoundsAndRearrange(AI_Tool.getMeshController().getCurrentLayer().getHorizontalSection());


        //We search which bound intersects with the bit.
        Polygon rectangle = new Polygon();
        getBitSidesSegments(bit).forEach(rectangle::addEnd);
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

        // 3) add final point (intersection between slice's contour and bit's sides)
        //todo : améliorer gestion erreur, + le fait qu'on ne regarde que sur le 1er contour
        associatedPoints.add(getNextBitStartPoint(bit, boundsList.get(0)));
        this.pointsContenus = (Vector<Vector2>) associatedPoints.clone();
        return associatedPoints;
    }


    public Vector<Vector2> getBitAssociatedPoints(Vector2 startPoint, Vector<Vector2> points) {
        Vector<Vector2> bound = (Vector<Vector2>) points.clone();
        //We search which bound intersects with the bit.
        Polygon rectangle = new Polygon();
        getBitSidesSegments(bit).forEach(rectangle::addEnd);
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
     * Return the Bit2D start point.
     * It is the intersection between the slice and the start side of the Bit2D.
     *
     * @param bit    the current Bit2D (the last placed Bit2D by AI).
     * @param points the points of the bounds on which stands the bit.
     * @return the bit start point. Returns <code>null</code> if none was found.
     */
    public Vector2 getBitStartPoint(Bit2D bit, Vector<Vector2> points) throws AI_Exception {
        //todo ajuster pour que ca marche pour des demi/quarts.. de bits (copier la modif de getNextBitStartPoint probablement)
        this.bit = bit;
        Polygon rectangle = new Polygon();
        getBitSidesSegments(bit).forEach(rectangle::addEnd);
        area = new Area(rectangle.toPath2D());
        points = (Vector<Vector2>) points.clone();
        points = repopulateSection(points);

        //We are first looking for the first point (at index i) contained by the Area of the bit
        int i;
        for (i = 0; i < points.size() - 1; i++) {
            if (area.contains(points.get(i).x, points.get(i).y)) {
                break;
            }
        }

        Segment2D outGoingSegment = new Segment2D(points.get(i - 1), points.get(i));

        //We are looking for the intersection between the outGoingSegment and the bounds of the Slice
        Vector2 intersectionPoint;
        Vector<Segment2D> sides = getBitSidesSegments(bit);
        //this.currentSegToDraw = outGoingSegment; //debugOnly
        // pointsADessiner.clear();//debugOnly

        for (Segment2D bitSides : sides) {
            intersectionPoint = bitSides.intersect(outGoingSegment); //null if parallel

            if (intersectionPoint != null) {
                if (contains(bitSides, intersectionPoint)) {
                    if (contains(outGoingSegment, intersectionPoint)) {
                        //  pointsADessiner.add(intersectionPoint); //debugOnly
                        return intersectionPoint;
                    }
                }
            }
        }
        throw new AI_Exception("The bit start point has not been found.");
    }


}
