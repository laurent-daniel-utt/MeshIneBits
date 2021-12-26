package meshIneBits.artificialIntelligence.deepLearning;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.AI_Tool;
import meshIneBits.artificialIntelligence.DebugTools;
import meshIneBits.artificialIntelligence.genetics.Evolution;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.apache.arrow.flatbuf.Null;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Area;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import meshIneBits.artificialIntelligence.GeneralTools;
import remixlab.dandelion.geom.Vec;


public class MagicAlgorithm {
    public Bit2D calculateBitPosition(@NotNull Vector<Vector2> sectionPoints) {

        Vector<Vector2> sectionRepopulated = GeneralTools.repopulateWithNewPoints(100, sectionPoints, true);


        boolean sectionReductionCompleted = false;

        Segment2D longestSegment = null;
        Vector2 furthestPoint = null;

        int cpt = 0;

        do {
            //Vector<Vector2> hull = makeHullPresorted(sectionRepopulated);
            //Vector<Vector2> hull = makeHull(sectionRepopulated);
            Vector<Vector2> hull = convex_hull(sectionRepopulated);
            //DebugTools.pointsToDrawRED = sectionPoints;
            hull.add(hull.firstElement());
            Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hull);

            // trouver le plus long segment
            double maxLength = -1;
            for (Segment2D s : segmentsHull) {
                if (s.getLength() > maxLength) {
                    longestSegment = s;
                    maxLength = s.getLength();
                }
            }

            // trouver le point le plus éloigné du longestSegment
            double maxDistance = -1;
            for (Vector2 point : hull) {
                assert longestSegment != null;
                if (longestSegment.distFromPoint(point) > maxDistance) {
                    furthestPoint = point;
                    maxDistance = longestSegment.distFromPoint(point);
                }
            }

            if (maxDistance > CraftConfig.bitWidth) { // cas où on ne peut pas couvrir toute la section avec une lamelle
                // recherche du point à pârtir duquel raccourcir la section
                Vector<Vector2> cutPoints = new Vector<>();
                cutPoints.add(longestSegment.start);
                cutPoints.add(longestSegment.end);
                cutPoints.add(furthestPoint);
                boolean cutPointFound = false;
                int iSection = sectionRepopulated.size() - 1;
                while (!cutPointFound) {
                    if (cutPoints.contains(sectionRepopulated.get(iSection))) {
                        while (sectionRepopulated.size() > iSection) { // todo
                            sectionRepopulated.remove(iSection);
                        }
                        cutPointFound = true;
                    }
                    iSection--;
                }
            } else {
                sectionReductionCompleted = true;
            }
            cpt++;
            System.out.println("max distance = " + maxDistance);
        }
        while (!sectionReductionCompleted);

        //Vector<Vector2> hull = makeHullPresorted(sectionRepopulated);
        //Vector<Vector2> hull = makeHull(sectionRepopulated);
        Vector<Vector2> hull = convex_hull(sectionRepopulated);
        //DebugTools.pointsToDrawRED = sectionPoints;
        hull.add(hull.firstElement());
        Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hull);

        // trouver le plus long segment
        double maxLength = -1;
        for (Segment2D s : segmentsHull) {
            if (s.getLength() > maxLength) {
                longestSegment = s;
                maxLength = s.getLength();
            }
        }

        // trouver le point le plus éloigné du longestSegment
        double maxDistance = -1;
        for (Vector2 point : hull) {
            assert longestSegment != null;
            if (longestSegment.distFromPoint(point) > maxDistance) {
                furthestPoint = point;
                maxDistance = longestSegment.distFromPoint(point);
            }
        }




        //DebugTools.pointsToDrawBLUE = (Vector<Vector2>) sectionRepopulated.clone();
//        DebugTools.pointsToDrawRED = (Vector<Vector2>) hull.clone();
//        DebugTools.segmentsToDraw = (Vector<Segment2D>) segmentsHull.clone();
//        DebugTools.setPaintForDebug(true);

        // cas ou on a fait des decoupes donc la position du bit est contraite

        Vector2 rotation = longestSegment.getDirectionalVector();


        Vector2 centerSegmentPoint;
        Vector2 centerSegmentPoint1= furthestPoint.sub(longestSegment.getNormal()
                .mul(longestSegment.distFromPoint(furthestPoint)/2));
        Vector2 centerSegmentPoint2= furthestPoint.sub(longestSegment.getNormal()
                .mul(-longestSegment.distFromPoint(furthestPoint)/2));
        if (Vector2.dist2(centerSegmentPoint1, longestSegment.start) < Vector2.dist2(centerSegmentPoint2, longestSegment.start)) {
            centerSegmentPoint = centerSegmentPoint1;
        } else {
            centerSegmentPoint = centerSegmentPoint2;
        }

//        DebugTools.pointsToDrawBLUE.add(centerSegmentPoint);
//        DebugTools.pointsToDrawRED.add(centerSegmentPoint1);
//        DebugTools.pointsToDrawRED.add(centerSegmentPoint2);
//        DebugTools.setPaintForDebug(true);


        Segment2D ligneRose = new Segment2D(
                longestSegment.getDirectionalVector().mul(-10000).add(centerSegmentPoint),
                longestSegment.getDirectionalVector().mul(10000).add(centerSegmentPoint));
        Segment2D lignePerpendiculaire = new Segment2D(
                sectionRepopulated.firstElement().add(longestSegment.getNormal().mul(-10000)),
                sectionRepopulated.firstElement().add(longestSegment.getNormal().mul(10000)));


        Vector2 pointRose = Segment2D.getIntersectionPoint(lignePerpendiculaire,ligneRose);


        // find direction of bit
        Vector2 startPoint = sectionRepopulated.firstElement();
        Vector2 directionalVector;
        if (Vector2.dist2(startPoint, longestSegment.start) < Vector2.dist2(startPoint, longestSegment.end)) {
            directionalVector = longestSegment.getDirectionalVector();
        } else {
            directionalVector = longestSegment.getDirectionalVector().mul(-1);
        }

//        DebugTools.pointsToDrawRED.add(new Vector2(0, 0));
//        DebugTools.segmentsToDraw.add(new Segment2D(new Vector2(0, 0), directionalVector.mul(50)));


        double longueur = CraftConfig.lengthFull/2-Vector2.dist(pointRose,centerSegmentPoint);
        Vector2 position = centerSegmentPoint.add(directionalVector.mul(longueur));

        Bit2D bit = new Bit2D(position, rotation);

        System.out.println("bit place = " + bit.toString());


        return bit;
        //return new Bit2D(new Vector2(1, 1), new Vector2(1, 1));
    }


    public Vector<Bit2D> getBits2(Slice slice) throws Exception {
        System.out.println("PAVING SLICE " + slice.getAltitude());
        Vector<Bit2D> bits = new Vector<>();

        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);

        Vector2 startPoint = new Vector2(29.39577615707399, 168.8854983178101);

        Vector<Vector2> sectionPoints = GeneralTools.getSectionPointsFromBound(bounds.get(0), startPoint);

        bits.add(calculateBitPosition(sectionPoints));

        return bits;
    }


    public Vector<Bit2D> getBits(Slice slice) throws Exception {
        System.out.println("PAVING SLICE " + slice.getAltitude());
        Vector<Bit2D> bits = new Vector<>();

        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);

        for (Vector<Vector2> bound : bounds) {
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 startPoint = bound.get(0);

            System.out.println("startpoint = " + startPoint);

            Vector<Vector2> sectionPoints;
            int nbMaxBits = 0;
            do{
                sectionPoints = GeneralTools.getSectionPointsFromBound(bound, startPoint);
                Bit2D bit = calculateBitPosition(sectionPoints);
                bits.add(bit);
                startPoint = new GeneralTools().getNextBitStartPoint(bit, bound);
                nbMaxBits++;
                System.out.println("startpoint = " + startPoint);
                //DebugTools.pointsToDrawRED = sectionPoints;
                //DebugTools.setPaintForDebug(true);
            }
            while (new AI_Tool().hasNotCompletedTheBound(veryFirstStartPoint, startPoint, sectionPoints) && nbMaxBits < 5); //Add each bit on the bound

        }
        return bits;

    }


    // https://www.tutorialcup.com/interview/algorithm/convex-hull-algorithm.htm
    public int OrientationMatch(Vector2 check1, Vector2 check2, Vector2 check3) {
        double val = (check2.y - check1.y) * (check3.x - check2.x) - (check2.x - check1.x) * (check3.y - check2.y);
        double errorAccepted = 0.05;
        if (Math.abs(val) < errorAccepted)
            return 0;
        return (val > 0) ? 1 : 2;
    }
    public Vector<Vector2> convex_hull(Vector<Vector2> points) {
        int lengths = points.size();
        if (lengths<3) return null;
        Vector<Vector2> result = new Vector<Vector2> ();
        int leftmost = 0;
        for (int i = 1; i<lengths; i++)
            if (points.get(i).x<points.get(leftmost).x)
                leftmost = i;
        int p = leftmost, pointq;
        do {
            result.add(points.get(p));
            pointq = (p + 1) % lengths;
            for (int i = 0; i<lengths; i++) {
                if (OrientationMatch(points.get(p), points.get(i), points.get(pointq)) == 2) {
                    pointq = i;
                }
            }
            p = pointq;
        }
        while (p != leftmost);

        return result;
    }



    // Returns a new list of points representing the convex hull of
    // the given set of points. The convex hull excludes collinear points.
    // This algorithm runs in O(n log n) time.
    public static Vector<Vector2> makeHull(Vector<Vector2> points) {
        Vector<Vector2c> newPoints = new Vector<>();
        for (Vector2 point : points) {
            newPoints.add(new Vector2c(point.x, point.y));
        }
        Collections.sort(newPoints);
        return makeHullPresorted(newPoints);
    }


    private static class Vector2c extends Vector2 implements Comparable<Vector2> {

        public Vector2c(double x, double y) {
            super(x, y);
        }

        @Override
        public int compareTo(@NotNull Vector2 other) {
            if (x != other.x)
                return Double.compare(x, other.x);
            else
                return Double.compare(y, other.y);
        }
    }


    // taken from https://www.nayuki.io/res/convex-hull-algorithm/ConvexHull.java
    // Returns the convex hull, assuming that each points[i] <= points[i + 1]. Runs in O(n) time.
    public static Vector<Vector2> makeHullPresorted(Vector<Vector2c> points) {
        if (points.size() <= 1)
            return new Vector<>(points);

        // Andrew's monotone chain algorithm. Positive y coordinates correspond to "up"
        // as per the mathematical convention, instead of "down" as per the computer
        // graphics convention. This doesn't affect the correctness of the result.

        Vector<Vector2> upperHull = new Vector<>();
        for (Vector2 p : points) {
            while (upperHull.size() >= 2) {
                Vector2 q = upperHull.get(upperHull.size() - 1);
                Vector2 r = upperHull.get(upperHull.size() - 2);
                if ((q.x - r.x) * (p.y - r.y) >= (q.y - r.y) * (p.x - r.x))
                    upperHull.remove(upperHull.size() - 1);
                else
                    break;
            }
            upperHull.add(p);
        }
        upperHull.remove(upperHull.size() - 1);

        Vector<Vector2> lowerHull = new Vector<>();
        for (int i = points.size() - 1; i >= 0; i--) {
            Vector2 p = points.get(i);
            while (lowerHull.size() >= 2) {
                Vector2 q = lowerHull.get(lowerHull.size() - 1);
                Vector2 r = lowerHull.get(lowerHull.size() - 2);
                if ((q.x - r.x) * (p.y - r.y) >= (q.y - r.y) * (p.x - r.x))
                    lowerHull.remove(lowerHull.size() - 1);
                else
                    break;
            }
            lowerHull.add(p);
        }
        lowerHull.remove(lowerHull.size() - 1);

        if (!(upperHull.size() == 1 && upperHull.equals(lowerHull)))
            upperHull.addAll(lowerHull);
        return upperHull;
    }


    private static double getAngle(Vector2 p1, Vector2 p2, Vector2 pRef) {
        return Math.atan2(p1.y - pRef.y, p1.x - pRef.x) - Math.atan2(p2.y - pRef.y, p2.x - pRef.x);
    }

    public static void main(String[] args) {



    }

}
