/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2021 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020 CLAIRIS Etienne & RUSSO André.
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
 */

package meshIneBits.artificialIntelligence.deepLearning;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.GeneralTools;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Area;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Vector;


public class BorderedPatternAlgorithm {
    //private PlotHelper plotHelper = new PlotHelper();

    public static void main(String[] args) {

//        Vector2 a = new Vector2(2, 2);
//        Vector2 b = new Vector2(0, 1);
//        Vector2 c = new Vector2(0, -1);
//        Vector2 d = new Vector2(2, 2);
//        Vector2 r = new Vector2(0, 0);
//        //Segment2D s = new Segment2D(a, b);
//        //Segment2D r = new Segment2D(c, d);
//
//        //System.out.println(Segment2D.getIntersectionPoint(s,r));
//
//        Vector<Vector2> v = new Vector<>();
//        v.add(a);
//        v.add(b);
//        v.add(c);
//        v.add(d);

        //System.out.println(getDistFurthestPointFromRefPoint(r,new Vector2(1, 1), v));
//
//        Vector2 constraintPoint = new Vector2(36.512649343232034, 248.44305388184796);
//        Vector2 midpoint = new Vector2(14.487722601313394, 235.63393999281956);
//        Vector2 dirConstraintSegmentNormal = new Vector2(0.08375151151741546, -0.9964866704168943);
//
//        System.out.println(getDistFromFromRefPointViaVector(constraintPoint, midpoint, dirConstraintSegmentNormal));

//        Vector<Vector2> list = new Vector<>();
//        for (int i=0;i<5000;i++) {
//            Vector2 start = new Vector2(Math.random(),Math.random());
//            list.add(start);
//        }
//        list.add(new Vector2(0,200));
//
//        long start = System.currentTimeMillis();
//        for (int i=0;i<5000;i++) {
//            getDistFurthestPointFromRefPointViaVector2(new Vector2(0,0),new Vector2(100,100),list);
//        }
//        long end = System.currentTimeMillis();
//        System.out.println(end-start);
//        System.out.println(getDistFurthestPointFromRefPointViaVector(new Vector2(0,0),new Vector2(100,100),list));
//        System.out.println(getDistFurthestPointFromRefPointViaVector2(new Vector2(0,0),new Vector2(100,100),list));

        //normale :7220
        //nouvelle:
    }

    /**
     * Compute and return the longest segment of the given list
     *
     * @param segment2DS the segments list
     * @return the longest segment
     */
    private Segment2D getLongestSegment(@NotNull Vector<Segment2D> segment2DS) {
        return segment2DS
                .stream()
                .max(Comparator.comparing(Segment2D::getLength))
                .orElseThrow(NoSuchElementException::new);
    }

    /**
     * Compute and return the most distant point to a segment
     *
     * @param segment2D the segment
     * @param points    the list of points
     * @return the furthest point from the segment
     */
    private Vector2 getFurthestPointFromSegment(Segment2D segment2D, @NotNull Vector<Vector2> points) {
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


    //TODO JAMAIS UTILISEE ?
    private Vector2 getOrthogonalProjection(@NotNull Segment2D AB, Vector2 C) {
        Vector2 vAC = AB.start.sub(C);
        Vector2 vAB = AB.getDirectionalVector().mul(AB.getLength());
        double orthogonalProjectionDist = vAB.dot(vAC) / AB.getLength();
        return AB.start.add(AB.getDirectionalVector().mul(-orthogonalProjectionDist));
    }

    private double getDistFromFromRefPointViaVector(Vector2 refPoint, Vector2 point, Vector2 directionalVector) {
        double dist = 0;
        if (point != refPoint) {
            double angle = Math.toRadians(directionalVector.getEquivalentAngle2() - point.sub(refPoint).getEquivalentAngle2());
            dist = Math.cos(angle) * Vector2.dist(point, refPoint);
        }
        return dist;
    }

    public Placement calculateBitPosition(@NotNull Vector<Vector2> sectionPoints, Area areaSlice, double minWidthToKeep) {

        double distExt = 0.1;//TODO NEVER USED ?

        Vector<Vector2> sectionRepopulated = GeneralTools.repopulateWithNewPoints(100, sectionPoints, true);


        boolean sectionReductionCompleted = false;

        Segment2D constraintSegment;
        Vector2 furthestPoint;

        int cpt = 0;

        do {
//            plotHelper.addSeries("section", sectionRepopulated, Plot.Marker.DIAMOND, Plot.Line.NONE, 6);
//            plotHelper.save();

            Vector<Vector2> hull = convex_hull(sectionRepopulated);
            //DebugTools.pointsToDrawBLUE = (Vector<Vector2>) hull.clone();
            hull.add(hull.firstElement());
            Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hull);

            // trouver le plus long segment
            constraintSegment = getLongestSegment(segmentsHull);

            // trouver le point le plus éloigné du constraint
            furthestPoint = getFurthestPointFromSegment(constraintSegment, hull);
            double maxDistance = Vector2.Tools.distanceFromPointToLine(furthestPoint, constraintSegment);

            if (maxDistance > CraftConfig.bitWidth - minWidthToKeep) { // cas où on ne peut pas couvrir toute la section avec une lamelle
                // recherche du point à pârtir duquel raccourcir la section
                Vector<Vector2> cutPoints = new Vector<>();
                cutPoints.add(constraintSegment.start);
                cutPoints.add(constraintSegment.end);
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

        }
        while (!sectionReductionCompleted);

        /*
        // todo mettre le placement en fonctions
        Vector<Vector2> hull = convex_hull(sectionRepopulated);
        hull.add(hull.firstElement());
        Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hull);

        // trouver le plus long segment
        constraintSegment = getLongestSegment(segmentsHull);
        // trouver le point le plus éloigné du constraint
        furthestPoint = getFurthestPointFromSegment(constraintSegment, hull);


        Vector2 rotation = constraintSegment.getDirectionalVector();


        // projection orthogonale de FurthestPoint sur LongesSegment
        Vector2 ortProjFurthestPoint = getOrthogonalProjection(constraintSegment, furthestPoint);

        Vector2 midPoint = constraintSegment.getMidPoint();
        Vector2 posLigneRoseLocal = ortProjFurthestPoint.sub(furthestPoint).normal().mul(CraftConfig.bitWidth / 2 - distExt);
        Vector2 posLigneRose;
        boolean hasToRotateBit = false;
        if (areaSlice.contains(midPoint.x, midPoint.y)) {//a l'interieur
            posLigneRose = furthestPoint.add(posLigneRoseLocal);
            hasToRotateBit = true;
        } else {//a l'exterieur
            posLigneRose = ortProjFurthestPoint.sub(posLigneRoseLocal);
        }

        //DebugTools.pointsToDrawGREEN.add(posLigneRose);
        //DebugTools.setPaintForDebug(true);


        Segment2D ligneRose = new Segment2D(
                constraintSegment.getDirectionalVector().mul(-10000).add(posLigneRose),
                constraintSegment.getDirectionalVector().mul(10000).add(posLigneRose));
        Segment2D lignePerpendiculaire = new Segment2D(
                sectionRepopulated.firstElement().add(constraintSegment.getNormal().mul(-10000)),
                sectionRepopulated.firstElement().add(constraintSegment.getNormal().mul(10000)));


        Vector2 pointRose = Segment2D.getIntersectionPoint(lignePerpendiculaire, ligneRose);


        // find direction of bit
        Vector2 startPoint = sectionRepopulated.firstElement();
        Vector2 directionalVector;
        if (Vector2.dist2(startPoint, constraintSegment.start) < Vector2.dist2(startPoint, constraintSegment.end)) {
            directionalVector = constraintSegment.getDirectionalVector();
        } else {
            directionalVector = constraintSegment.getDirectionalVector().mul(-1);
        }

//        DebugTools.pointsToDrawRED.add(new Vector2(0, 0));
//        DebugTools.segmentsToDraw.add(new Segment2D(new Vector2(0, 0), directionalVector.mul(50)));


        double longueur = CraftConfig.lengthFull / 2 - Vector2.dist(pointRose, posLigneRose);
        Vector2 position = posLigneRose.add(directionalVector.mul(longueur));

        if (hasToRotateBit) {
            rotation = rotation.getOpposite();
        }
        Bit2D bit = new Bit2D(position, rotation);
        double newLengthBit = getDistFurthestPointFromRefPoint(startPoint, constraintSegment.getDirectionalVector(), hull);
        System.out.println("newLengthBit = " + newLengthBit);
//        DebugTools.pointsToDrawRED.add(new Vector2(0,0));
//        DebugTools.pointsToDrawRED.addAll(hull);
//        DebugTools.setPaintForDebug(true);
        bit.resize(newLengthBit / CraftConfig.lengthFull * 100, 100);


        System.out.println("bit placé = " + bit);

         */

        Bit2D bit = getBitFromSectionReduced(sectionRepopulated, areaSlice);

        return new Placement(bit, sectionRepopulated);
    }

    /**
     * Return a vector collinear to the bit to place on the sectionReduced, the vector is orienting toward the end of the bit
     *
     * @param sectionReduced the reduced section of points
     * @return the collinear vector
     */
    private Vector2 getBitCollinearVector(@NotNull Vector<Vector2> sectionReduced) {
        Vector2 startPoint = sectionReduced.firstElement();

        // calculer les points du hull
        Vector<Vector2> hullReduced = convex_hull(sectionReduced);
        hullReduced.add(hullReduced.firstElement());

        // calculer les segments du hull
        Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hullReduced);


        // trouver le vecteur qui relie le startpoint au point de plus éloigné de la section (hull)
        Vector2 furthestPoint = getFurthestPoint(startPoint, sectionReduced);
        Vector2 startToFurthestPoint = furthestPoint.sub(startPoint);


        // find directional vector of constraint, oriented to the end of the section
        Vector2 dirConstraintSegment = getLongestSegment(segmentsHull).getDirectionalVector();

        if (startToFurthestPoint.dot(dirConstraintSegment) < 0) {
            return dirConstraintSegment.getOpposite();
        }
        return dirConstraintSegment;
    }

    /**
     * retourne le vecteur de position du bit colinéaire à la longueur bit relatif au startpoint.
     * Return the position of the bit //TODO DOC @ANDRE de cette fonction et dans la fonction
     *
     * @param bitCollinearVector
     * @param bitLength
     * @return
     */
    private Vector2 getBitPositionCollinear(@NotNull Vector2 bitCollinearVector, double bitLength) {
        // calculer la composante colinéaire à la longueur du bit, du le vecteur position du bit vis à vis du start point
        return bitCollinearVector.mul(bitLength / 2);
    }

    /**
     * retourne le vecteur de position du bit normale à la longueur bit relatif au startpoint.
     * //TODO DOC @ANDRE de cette fonction et dans la fonction
     *
     * @param sectionReduced
     * @param areaSlice
     * @return
     */
    private Vector2 getPositionNormal(@NotNull Vector<Vector2> sectionReduced, Area areaSlice) {
        Vector2 startPoint = sectionReduced.firstElement();
        // calculer les points du hull
        Vector<Vector2> hullReduced = convex_hull(sectionReduced);
        hullReduced.add(hullReduced.firstElement());
        // calculer les segments du hull
        Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hullReduced);
        /*
         trouver la direction du vecteur normal au bit que l'on veut placer, orienté vers l'intérieur de la forme à paver
        3 cas possibles : voir SCHEMA 2
        */
        Segment2D constraintSegment = getLongestSegment(segmentsHull);
        Vector2 constraintPoint = getFurthestPointFromSegment(constraintSegment, hullReduced);
        Vector2 dirConstraintSegmentNormal = constraintSegment.getNormal();
        Vector2 midPoint = constraintSegment.getMidPoint();
        Vector2 constraintToMidPoint = midPoint.sub(constraintPoint);
        Vector2 positionNormal;
        if (constraintPoint.isOnSegment(constraintSegment)) { // cas 3
            dirConstraintSegmentNormal = getInnerDirectionalVector(constraintSegment, areaSlice);
            positionNormal = dirConstraintSegmentNormal.mul(CraftConfig.bitWidth / 2);

        } else if (areaSlice.contains(midPoint.x, midPoint.y)) { // cas 1 : constraint segment à l'intérieur et il fait inverser la direction de dirConstraintVectorNormal
            if (dirConstraintSegmentNormal.dot(constraintToMidPoint) < 0) // dans le cas où le vecteur est dans le mauvais sens
                dirConstraintSegmentNormal = dirConstraintSegmentNormal.getOpposite();

            double normPositionNormal = CraftConfig.bitWidth / 2
                    - getDistFromFromRefPointViaVector(constraintPoint, midPoint, dirConstraintSegmentNormal);
            positionNormal = dirConstraintSegmentNormal.mul(normPositionNormal);

        } else { // cas 2
            if (dirConstraintSegmentNormal.dot(constraintToMidPoint) > 0)
                dirConstraintSegmentNormal = dirConstraintSegmentNormal.getOpposite();
            double normPositionNormal = CraftConfig.bitWidth / 2
                    - getDistFromFromRefPointViaVector(midPoint, startPoint, dirConstraintSegmentNormal);
            positionNormal = dirConstraintSegmentNormal.mul(normPositionNormal);
        }
        return positionNormal;
    }

    /**
     * Compute the new (reduced) length of the bit considering the section reduced
     *
     * @param sectionReduced     the reduced section of points
     * @param bitCollinearVector the bit collinear vector
     * @return the length of the bit
     * @see #getBitCollinearVector(Vector)
     */
    private double getBitLengthFromSectionReduced(@NotNull Vector<Vector2> sectionReduced, Vector2 bitCollinearVector) {
        Vector2 startPoint = sectionReduced.firstElement();
        return getDistFurthestPointFromRefPointViaVector(startPoint, bitCollinearVector, sectionReduced);
    }

    //TODO DOC @ANDRE de cette fonction et dans la fonction
    private Bit2D getBitFromSectionReduced(Vector<Vector2> sectionReduced, Area areaSlice) {
        Vector2 bitCollinearVector = getBitCollinearVector(sectionReduced);
        double newLengthBit = getBitLengthFromSectionReduced(sectionReduced, bitCollinearVector);
        Vector2 positionCollinear = getBitPositionCollinear(bitCollinearVector, newLengthBit);
        Vector2 positionNormal = getPositionNormal(sectionReduced, areaSlice);

        // point sur lequel se se trouve une des arrêtes courtes du bit à placer
        Vector2 startBit = getFurthestPointFromRefPointViaVector(sectionReduced.firstElement(), bitCollinearVector.getOpposite(), sectionReduced);

        // au cas où startBit et startpoint sont différents
        double compensationDistance = getDistFromFromRefPointViaVector(sectionReduced.firstElement(), startBit, positionNormal.normal());
        positionNormal = positionNormal.sub(positionNormal.normal().mul(compensationDistance));

        Vector2 bitPosition = startBit.add(positionCollinear).add(positionNormal);
        Vector2 bitOrientation = positionCollinear.normal();

        return new Bit2D(bitPosition.x, bitPosition.y,
                newLengthBit, CraftConfig.bitWidth,
                bitOrientation.x, bitOrientation.y);
    }

    //TODO DOC @ANDRE de cette fonction et dans la fonction
    // https://www.tutorialcup.com/interview/algorithm/convex-hull-algorithm.htm
    // return true if check3 isn't part of the hull
    public boolean OrientationMatch(@NotNull Vector2 check1, @NotNull Vector2 check2, @NotNull Vector2 check3) {
        double val = (check2.y - check1.y) * (check3.x - check2.x) - (check2.x - check1.x) * (check3.y - check2.y);
        if (check2.asGoodAsEqual(check3) || check1.asGoodAsEqual(check2) ||
                (Math.abs(val) < Math.pow(10, -CraftConfig.errorAccepted)
                        && check3.sub(check2).dot(check1.sub(check2)) <= 0)) {
            return false;
        }
        return val <= Math.pow(10, -CraftConfig.errorAccepted);
    }

    //TODO DOC @ANDRE de cette fonction et dans la fonction
    // https://www.tutorialcup.com/interview/algorithm/convex-hull-algorithm.htm
    public Vector<Vector2> convex_hull(@NotNull Vector<Vector2> points) {
        int lengths = points.size();
        if (lengths < 3) return null;
        Vector<Vector2> result = new Vector<>();
        int leftmost = 0;
        for (int i = 1; i < lengths; i++)
            if (points.get(i).x < points.get(leftmost).x)
                leftmost = i;
        int p = leftmost, pointq;
        do {
            result.add(points.get(p));
            pointq = (p + 1) % lengths;
            for (int i = 0; i < lengths; i++) {
                if (OrientationMatch(points.get(p), points.get(i), points.get(pointq))) {
                    pointq = i;
                }
            }
            p = pointq;
        }
        while (p != leftmost);

        return result;
    }

    //TODO JAMAIS UTILISEE ?
    public Vector<Bit2D> getBits2(@NotNull Slice slice, double minWidthToKeep) throws Exception {
        System.out.println("PAVING SLICE " + slice.getAltitude());
        Vector<Bit2D> bits = new Vector<>();

        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);

        Vector2 startPoint = new Vector2(86.28867818076299, 236.48951109657744);
        //Vector2 startPoint = bounds.get(0).get(10);

        Vector<Vector2> sectionPoints = GeneralTools.getSectionPointsFromBound(bounds.get(0), startPoint);

        Area areaSlice = AreaTool.getAreaFrom(slice);

//        bits.add(calculateBitPosition(sectionPoints, areaSlice, minWidthToKeep));
//        DebugTools.pointsToDrawGREEN.add(startPoint);

        return bits;
    }

    /**
     * Compute each bit to place on the border of the Slice
     *
     * @param slice          the slice to pave
     * @param minWidthToKeep //TODO @ANDRE
     * @return the list of bits for this Slice
     */
    public Vector<Bit2D> getBits(@NotNull Slice slice, double minWidthToKeep) {
        System.out.println("PAVING SLICE " + slice.getAltitude());
        Vector<Bit2D> bits = new Vector<>();


        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);

        Area areaSlice = AreaTool.getAreaFrom(slice);

        for (Vector<Vector2> bound : bounds) {
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 nextStartPoint = bound.get(0);

            System.out.println("firstStartpoint = " + nextStartPoint);

            Vector<Vector2> sectionPoints;
            int nbMaxBits = 0;
            Placement placement;
            do {
                System.out.println("PLACEMENT BIT " + nbMaxBits + "====================");
                sectionPoints = GeneralTools.getSectionPointsFromBound(bound, nextStartPoint);
                placement = calculateBitPosition(sectionPoints, areaSlice, minWidthToKeep);
                bits.add(placement.bit2D);
                nextStartPoint = placement.end;

                nbMaxBits++;

            }
            while (!placement.sectionCovered.contains(veryFirstStartPoint)); //Add each bit on the bound
        }
        return bits;

    }

    private Vector2 getFurthestPointFromRefPointViaVector(Vector2 refPoint, Vector2 directionalVector, Vector<Vector2> points) {
        directionalVector = directionalVector.normal();
        double maxDist = Double.NEGATIVE_INFINITY;
        Vector2 furthestPoint = null;
        for (Vector2 p : points) {
            double dist = getDistFromFromRefPointViaVector(refPoint, p, directionalVector);
            if (dist > maxDist) {
                maxDist = dist;
                furthestPoint = p;
            }
        }
        return furthestPoint;
    }


    private double getDistFurthestPointFromRefPointViaVector(Vector2 refPoint, Vector2 directionalVector, Vector<Vector2> points) {
        directionalVector = directionalVector.normal();
        double maxDist = Double.NEGATIVE_INFINITY;
        for (Vector2 p : points) {
            double dist = getDistFromFromRefPointViaVector(refPoint, p, directionalVector);
            if (dist > maxDist) {
                maxDist = dist;
            }
        }
        return maxDist;
    }

    /**
     * retourne le point le plus éloigné du refpopint
     *
     * @param refPoint
     * @param points
     * @return
     */
    private Vector2 getFurthestPoint(Vector2 refPoint, Vector<Vector2> points) {
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

    private Vector2 getInnerDirectionalVector(Segment2D s, Area a) {
        double distCkeck = 0.1;
        Vector2 dir = s.getNormal();
        Vector2 vectCheck = s.getMidPoint().add(dir.mul(distCkeck));
        if (!a.contains(vectCheck.x, vectCheck.y)) {
            dir = dir.getOpposite();
        }
        return dir;
    }

    private static class Placement {
        public final Bit2D bit2D;
        public final Vector<Vector2> sectionCovered;
        public final Vector2 end;

        public Placement(Bit2D bit2D, Vector<Vector2> sectionCovered) {
            this.bit2D = bit2D;
            this.sectionCovered = sectionCovered;
            this.end = sectionCovered.lastElement();
        }
    }
}
