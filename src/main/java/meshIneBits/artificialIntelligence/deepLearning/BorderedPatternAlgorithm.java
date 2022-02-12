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

package meshIneBits.artificialIntelligence.deepLearning;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.GeneralTools;
import meshIneBits.artificialIntelligence.debug.PlotHelper;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.io.*;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Vector;


public class BorderedPatternAlgorithm {

    private PlotHelper plotHelper = new PlotHelper();

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

    /**
     * Return the distance between two points via a vector by computing a projection along the vector.
     *
     * @param refPoint          the first point
     * @param point             the second point
     * @param directionalVector the directional vector
     * @return the projected distance
     */
    private double getDistFromFromRefPointViaVector(Vector2 refPoint, Vector2 point, Vector2 directionalVector) {
        if (point != refPoint) {
            double angle = Math.toRadians(directionalVector.getEquivalentAngle2() - point.sub(refPoint).getEquivalentAngle2());
            return Math.cos(angle) * Vector2.dist(point, refPoint);
        }
        return 0;
    }

    /**
     * Search for the furthest point from ref point via a vector by computing a projection along the vector.
     *
     * @param refPoint          the first point
     * @param directionalVector the second point
     * @param points            the points of the section
     * @return the furthest point
     */
    private Vector2 getFurthestPointFromRefPointViaVector(Vector2 refPoint, Vector2 directionalVector, @NotNull Vector<Vector2> points) {
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

    /**
     * Checks if check3 is not part of the convex hull
     *
     * @return true if check3 is not part of the Hull
     * @see #getConvexHull(Vector)
     */
    // Taken from : https://www.tutorialcup.com/interview/algorithm/convex-hull-algorithm.htm
    private static boolean OrientationMatch(@NotNull Vector2 check1, @NotNull Vector2 check2, @NotNull Vector2 check3) {
        double val = (check2.y - check1.y) * (check3.x - check2.x) - (check2.x - check1.x) * (check3.y - check2.y);

        if (check2.asGoodAsEqual(check3) || check1.asGoodAsEqual(check2) ||
                (Math.abs(val) < Math.pow(10, -5)
                        && check3.sub(check2).dot(check1.sub(check2)) <= 0)) {
            return false;
        }
        return val <= Math.pow(10, -5);
    }


    /**
     * Return a vector collinear to the bit to place on the sectionReduced, the vector is orienting toward the end of the bit
     *
     * @param sectionReduced the reduced section of points on which a bit can take place.
     * @return the collinear vector
     */
    private Vector2 getBitCollinearVector(@NotNull Vector<Vector2> sectionReduced) {
        Vector2 startPoint = sectionReduced.firstElement();

        // compute the convex hull's points
        Vector<Vector2> hullReduced = getConvexHull(sectionReduced);

        // compute hull's segments
        Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hullReduced);

        // find the vector which connect the startPoint to the furthest (from the startPoint) point of the section
        Vector2 furthestPoint = getFurthestPoint(startPoint, sectionReduced);
        Vector2 startToFurthestPoint = furthestPoint.sub(startPoint);

        // find directional vector of constraint, oriented to the end of the section
        Vector2 dirConstraintSegment = getLongestSegment(segmentsHull).getDirectionalVector();

        if (startToFurthestPoint.dot(dirConstraintSegment) < 0) { //if the dirConstraintSegment is not pointing to the end of the section
            return dirConstraintSegment.getOpposite();
        }
        return dirConstraintSegment;
    }

    /**
     * The position du bit is given by a vector that connects startPoint to the center of the bit.
     * This vector has to components : Collinear and Normal to the bit.
     *
     * @param bitCollinearVector the vector given by {@link BorderedPatternAlgorithm#getBitCollinearVector(Vector)}
     * @param bitLength          the length of the Bit placed.
     * @return the collinear component of the vector.
     */
    private Vector2 getBitPositionCollinear(@NotNull Vector2 bitCollinearVector, double bitLength) {
        return bitCollinearVector.mul(bitLength / 2);
    }

    /**
     * Uses Gift-Wrapping algorithm
     *
     * @param points to points to compute the convex Hull on
     * @return the points of the convex Hull that include the given points
     */
    // Taken from : https://www.tutorialcup.com/interview/algorithm/convex-hull-algorithm.htm
    public static Vector<Vector2> getConvexHull(Vector<Vector2> points) {
        int length = points.size();
        Vector<Vector2> result = new Vector<>();
        // find leftMosts
        Vector<Integer> iLeftMosts = new Vector<>();
        double xMin = Double.POSITIVE_INFINITY;
        for (int i = 0; i < length; i++) {
            double x = points.get(i).x;
            if (x <= xMin) {
                xMin = x;
                System.out.println("xMin = " + xMin);
            }
        }
        for (int i = 0; i < length; i++) {
            if (Math.abs(points.get(i).x - xMin) < Math.pow(10, -5))
                iLeftMosts.add(i);
        }
        // find higher of leftMosts
        int iHigherLeftMost = iLeftMosts.get(0);
        for (int i = 1; i < iLeftMosts.size(); i++) {
            if (points.get(iLeftMosts.get(i)).y > points.get(iHigherLeftMost).y)
                iHigherLeftMost = iLeftMosts.get(i);
        }

        int p = iHigherLeftMost, pointQ;
        do {
            result.add(points.get(p));
            pointQ = (p + 1) % length;
            for (int i = 0; i < length; i++) {
                if (OrientationMatch(points.get(p), points.get(i), points.get(pointQ))) {
                    pointQ = i;
                }
            }
            p = pointQ;
        }
        while (p != iHigherLeftMost);

        result.add(result.firstElement());

        return result;
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
        Vector2 furthestPoint = getFurthestPointFromRefPointViaVector(startPoint, bitCollinearVector, sectionReduced);
        return getDistFromFromRefPointViaVector(startPoint, furthestPoint, bitCollinearVector);
    }

    public static void pointsToFile(Vector<Vector2> points) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("src/main/java/meshIneBits/artificialIntelligence/debug/points.txt", false));
            for (Vector2 point : points) {
                String line = point.x + "," + point.y + "\n";
                writer.append(line);
            }


            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //minimum distance of wood needed to be kept when placing, in order to avoid the cut bit
    private @NotNull Vector<Vector2> getSectionReduced(@NotNull Vector<Vector2> sectionPoints, double minWidthToKeep) {
        Vector<Vector2> sectionToReduce = GeneralTools.repopulateWithNewPoints(100, sectionPoints, true);
        pointsToFile(sectionToReduce);

//        System.out.println("sectionIsClosed = " + sectionIsClosed);
//        System.out.println("sectionPoints = " + sectionPoints);

        boolean sectionReductionCompleted = false;

        Segment2D constraintSegment;
        Vector2 furthestPoint;

        do {

            boolean sectionIsClosed = sectionToReduce.firstElement().asGoodAsEqual(sectionToReduce.lastElement());

            // calculates the convex hull of the section's points
            //todo
            //sectionToReduce.remove(sectionToReduce.size() - 1);
            //pointsToFile(sectionToReduce);
            //System.out.println("heyy");
            //Vector<Vector2> hull = getConvexHull(sectionToReduce);
            Vector2[] pointsArray = new Vector2[sectionToReduce.size()];
            for (int i = 0; i < sectionToReduce.size(); i++) {
                pointsArray[i] = sectionToReduce.get(i);
            }
            QuickHull quickHull = new QuickHull(pointsArray);
            Vector<Vector2> hull = quickHull.hullPoints;

            Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hull);

            // find the constraint segment, which is the longest segment of the hull // todo, maybe not always the case
            constraintSegment = getLongestSegment(segmentsHull);

            // find the constraint point, which is the convex hull's furthest point from the constraint segment
            furthestPoint = getFurthestPointFromSegment(constraintSegment, hull);

            // calculate distance between constraint point and constraint segment
            double sectionWidth = Vector2.Tools.distanceFromPointToLine(furthestPoint, constraintSegment);

            /*
             If this condition is true is executed, this means the bit can't be placed over all the section's points
             while respecting the minWidthToKeep. In this case the content of the following "if" reduces the section,
             starting by the last point, until the first "cut point" (the constraint point or an end of the
             constraint segment) reached, thus this point is the fist one that prevents the section to be thinner.
             */
            if ((!sectionIsClosed && (sectionWidth > CraftConfig.bitWidth - minWidthToKeep))
                    || (sectionIsClosed && sectionWidth > CraftConfig.bitWidth)) {

                // list the cut points
                Vector<Vector2> cutPoints = new Vector<>();
                cutPoints.add(constraintSegment.start);
                cutPoints.add(constraintSegment.end);
                cutPoints.add(furthestPoint);
                // research of the first "cut point" of the section, starting research by its last point.
                boolean cutPointFound = false;
                int iSection = sectionToReduce.size() - 1;
                while (!cutPointFound) {
                    if (cutPoints.contains(sectionToReduce.get(iSection))) {
                        // delete section's points from the cut point at iSection (included) to the last point of the section
                        while (sectionToReduce.size() > iSection) {
                            sectionToReduce.remove(iSection);
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
     * The position du bit is given by a vector that connects startPoint to the center of the bit.
     * This vector has to components : Collinear and Normal to the bit.
     *
     * @param sectionReduced the reduced section of points on which a bit can take place.
     * @param areaSlice the area of the Slice
     * @return the normal component of the vector.
     */
    private Vector2 getPositionNormal(@NotNull Vector<Vector2> sectionReduced, Area areaSlice) {
        boolean sectionReducedIsClosed = sectionReduced.firstElement().asGoodAsEqual(sectionReduced.lastElement());
        Vector2 startPoint = sectionReduced.firstElement();
        Vector<Vector2> hullReduced = getConvexHull(sectionReduced); //computes the convex hull points
        // compute hull segments
        Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hullReduced); //computes the

        /*
        Find the direction of the bit normal vector. Oriented toward the inner
        There are 3 possible cases :
        1 : the segment that connects the end to the start of the hull is IN the Slice
        2 : the segment is OUT of the Slice
        3 : the segment is part of the Slice
         */
        Segment2D constraintSegment = getLongestSegment(segmentsHull);
        Vector2 constraintPoint = getFurthestPointFromSegment(constraintSegment, hullReduced);
        Vector2 dirConstraintSegmentNormal = constraintSegment.getNormal();
        Vector2 midPoint = constraintSegment.getMidPoint();
        Vector2 constraintToMidPoint = midPoint.sub(constraintPoint);
        Vector2 positionNormal;
        if (constraintPoint.isOnSegment(constraintSegment)) { // case 3
            dirConstraintSegmentNormal = getInnerDirectionalVector(constraintSegment, areaSlice);
            positionNormal = dirConstraintSegmentNormal.mul(CraftConfig.bitWidth / 2);
            System.out.println("cas 3");

        } else if (sectionReducedIsClosed) { // case 4 : covered section closed
            double lenPositionNormal = CraftConfig.bitWidth / 2;
            // on veut que le vecteur parte du start point pour aller vers le constraint segment ou le constraint point en fonction duquel est à l'opposé de lui
            //todo le faire
            positionNormal = dirConstraintSegmentNormal.mul(lenPositionNormal);
            System.out.println("cas 4");

        } else if (areaSlice.contains(midPoint.x, midPoint.y)) { // case 1 : constraint segment is in, so we have to inverse the direction of dirConstraintVectorNormal
            if (dirConstraintSegmentNormal.dot(constraintToMidPoint) < 0) // In the case of the vector is in the bad direction
                dirConstraintSegmentNormal = dirConstraintSegmentNormal.getOpposite();

            double normPositionNormal = CraftConfig.bitWidth / 2
                    - getDistFromFromRefPointViaVector(constraintPoint, midPoint, dirConstraintSegmentNormal);
            positionNormal = dirConstraintSegmentNormal.mul(normPositionNormal);
            System.out.println("cas 1");

        } else { // case 2
            if (dirConstraintSegmentNormal.dot(constraintToMidPoint) > 0)
                dirConstraintSegmentNormal = dirConstraintSegmentNormal.getOpposite();
            double normPositionNormal = CraftConfig.bitWidth / 2
                    - getDistFromFromRefPointViaVector(midPoint, startPoint, dirConstraintSegmentNormal);
            positionNormal = dirConstraintSegmentNormal.mul(normPositionNormal);
            System.out.println("cas 2");
        }

//        DebugTools.pointsToDrawRED.add(constraintPoint);
//        DebugTools.segmentsToDraw.add(constraintSegment);
//        DebugTools.pointsToDrawBLUE.add(positionNormal);
//        DebugTools.setPaintForDebug(true);


        return positionNormal;
    }

    /**
     * Returns the placed bit and the section of the Slice covered by it
     *
     * @param sectionPoints  the points of the section
     * @param areaSlice      the Area of the Slice
     * @param minWidthToKeep minimum distance of wood needed to be kept when placing, in order to avoid the cut bit
     * @return the Placement object of the Bit
     * @see Placement
     */
    private Placement getBitPlacement(@NotNull Vector<Vector2> sectionPoints, Area areaSlice, double minWidthToKeep) {

        Vector<Vector2> sectionReduced = getSectionReduced(sectionPoints, minWidthToKeep);
        Vector2 startPoint = sectionReduced.firstElement();

        Vector2 bitCollinearVector = getBitCollinearVector(sectionReduced);
        double newLengthBit = getBitLengthFromSectionReduced(sectionReduced, bitCollinearVector);
        Vector2 positionCollinear = getBitPositionCollinear(bitCollinearVector, newLengthBit);
        Vector2 positionNormal = getPositionNormal(sectionReduced, areaSlice);

        // the point on which we can place the start of the Bit
        Vector2 startBit = getFurthestPointFromRefPointViaVector(startPoint, bitCollinearVector.getOpposite(), sectionReduced);

        // the case when the startBit and the startPoint are different
        double compensationDistance = getDistFromFromRefPointViaVector(startPoint, startBit, positionNormal.normal());
        positionNormal = positionNormal.sub(positionNormal.normal().mul(compensationDistance));

        Vector2 bitPosition = startBit.add(positionCollinear).add(positionNormal);
        Vector2 bitOrientation = positionCollinear.normal();

        Bit2D bit2D = new Bit2D(bitPosition.x, bitPosition.y,
                newLengthBit, CraftConfig.bitWidth,
                bitOrientation.x, bitOrientation.y);


        return new Placement(bit2D, sectionReduced);
    }

    /**
     * Compute each bit to place on the border of the Slice using BorderedPattern algorithms
     *
     * @param slice          the slice to pave
     * @param minWidthToKeep minimum distance of wood needed to be kept when placing, in order to avoid the cut bit to be too fragile
     * @param numberMaxBits  the maximum number of bits to place on each border
     * @return the list of bits for this Slice
     */
    public Vector<Bit2D> getBits(@NotNull Slice slice, double minWidthToKeep, double numberMaxBits) throws NoninvertibleTransformException {
        Vector<Bit2D> bits = new Vector<>();

        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);
        Area areaSlice = AreaTool.getAreaFrom(slice);


        for (Vector<Vector2> bound : bounds) {
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 nextStartPoint = bound.get(0);

            System.out.println("++++++++++++++ BOUND " + bounds.indexOf(bound) + " ++++++++++++++++");

            Vector<Vector2> sectionPoints;
            int iBit = 0;//TODO DebugOnly
            Placement placement;
            do {
                System.out.println("PLACEMENT BIT " + iBit + "====================");
                sectionPoints = GeneralTools.getSectionPointsFromBound(bound, nextStartPoint);

//                System.out.println("dist = " + Vector2.dist(sectionPoints.lastElement(), nextStartPoint));

                placement = getBitPlacement(sectionPoints, areaSlice, minWidthToKeep);
                bits.add(placement.bit2D);
                nextStartPoint = placement.end;
//                System.out.println("veryFirstStartPoint = " + veryFirstStartPoint);
//                System.out.println("section covered = " + placement.sectionCovered);


//                DebugTools.pointsToDrawBLUE.addAll(sectionPoints);
//                DebugTools.pointsToDrawRED.addAll(placement.sectionCovered);
//                DebugTools.setPaintForDebug(true);
//                System.out.println("sectionPoints.lastElement() = " + sectionPoints.lastElement());
//                System.out.println("placement.sectionCovered.lastElement() = " + placement.sectionCovered.lastElement());


                System.out.println("FIN PLACEMENT BIT " + iBit + "====================");

                iBit++;

            }
            while (!((listContainsAsGoodAsEqual(veryFirstStartPoint, placement.sectionCovered) && iBit > 1) || listContainsAllAsGoodAsEqual(bound, placement.sectionCovered)) && iBit < 100);
            //while (!listContainsAsGoodAsEqual(veryFirstStartPoint, placement.sectionCovered.subList(1, placement.sectionCovered.size())) && iBit<40); //Add each bit on the bound
        }
        return bits;

    }

    private boolean listContainsAllAsGoodAsEqual(Vector<Vector2> containedList, Vector<Vector2> containerList) {
        for (Vector2 p : containedList) {
            if (!listContainsAsGoodAsEqual(p, containerList)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the furthest point among a list of points from a reference point (refPoint), calculating direct distances
     * between them.
     *
     * @param refPoint the point from which the method calculates the distances
     * @param points   the list of points among which the methods search the furthest point from the refPoint
     * @return the furthest point from refPoint among the list  of points
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

    /**
     * A {@link Segment2D} is part of the boundary of an {@link Area}. This method finds a {@link Vector2}, normal
     * to the segment, oriented toward the inside of the area.
     *
     * @param segment2D the segment, part of the boundary of the {@link Area} given as second parameter
     * @param area      the area
     * @return returns a {@link Vector2}, normal to the segment, oriented toward the inside of the area.
     */
    private Vector2 getInnerDirectionalVector(Segment2D segment2D, Area area) {
        double distCheck = 0.1;
        Vector2 dir = segment2D.getNormal();
        Vector2 vectorCheck = segment2D.getMidPoint().add(dir.mul(distCheck));
        if (!area.contains(vectorCheck.x, vectorCheck.y)) {
            dir = dir.getOpposite();
        }
        return dir;
    }

//    private Segment2D getConstraintSegment(Vector<Vector2> section, Vector<Vector2> hull, Area sliceArea) {
//        double threshold = 40; //todo
//        if(Vector2.dist(section..get(0), section.get(1))>threshold)
//    }

    private boolean listContainsAsGoodAsEqual(Vector2 point, Vector<Vector2> points) {
        for (Vector2 p : points) {
            if (point.asGoodAsEqual(p)) {
                return true;
            }
        }
        return false;
    }


    //TODO DebugOnly

    private boolean areHullAndAreaOnSameSide(Segment2D segment2D, Area sliceArea, Vector<Vector2> hull) {
        double distCheck = 0.1;
        Vector2 dir = segment2D.getNormal();
        Vector2 pointCheck = segment2D.getMidPoint().add(dir.mul(distCheck));
        if (!sliceArea.contains(pointCheck.x, pointCheck.y)) {
            pointCheck = pointCheck.getOpposite();
        }
        return !Vector2.Tools.checkOnDifferentSides(pointCheck, getFurthestPointFromSegment(segment2D, hull), segment2D);

    }


    /**
     * The description of the placement of a {@link Bit2D} : the bit2D, the section covered by it
     * and the last point of the section.
     */
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

    /**
     * Debug Only : can be used to place only one bit and to choose its position by passing the startPoint coordinates in the code
     *
     * @param slice          the slice to pave
     * @param minWidthToKeep minimum distance of wood needed to be kept when placing, in order to avoid the cut bit
     *                       to be too fragile
     * @return the list of bits for this Slice
     */
    public Vector<Bit2D> getBits2(@NotNull Slice slice, double minWidthToKeep) throws NoninvertibleTransformException {
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
     * class that implements QuickHull algorithm to construct convex hull polygon<br>
     * usage example:
     * <blockquote><pre>
     * int nDots = 500;
     * int offset = 50;
     * int sizeX = 400;
     * int sizeY = 400;
     * Point []dots = new Point[nDots];
     * for(int i =0; i < dots.length; i++){
     *     int px = (int)Math.round(offset + (sizeX - 2*offset)*Math.random());
     *     int py = (int)Math.round(offset + (sizeY - 2*offset)*Math.random());
     *     dots[i] = new Point(px,py);
     * }
     * qh = new QuickHull(dots);
     * Point []dots = qh.getOriginalPoints();
     * Vector outPoints = qh.getHullPointsAsVector();
     * </pre></blockquote>
     */


    public class QuickHull {
        Vector2[] originalPoints;
        int fullSteps = 0;
        Vector hullPoints = new Vector();

        /*
         * constructor for <code>QuickHull</code> class
         * @param originalPoints {@link Point}[] initial points
         */
        public QuickHull(Vector2[] originalPoints) {
            this.originalPoints = originalPoints;
            qhull(originalPoints, 0, 0);
            reorderPoints(hullPoints);
        }

        /**
         * Returns original {@link Vector2} array.
         *
         * @return original {@link Vector2} array
         */
        public Vector2[] getOriginalPoints() {
            return originalPoints;
        }

        /**
         * Returns convex hull points as {@link Vector}.
         *
         * @return convex hull points as {@link Vector}.
         */
        public Vector getHullPointsAsVector() {
            return (Vector) hullPoints.clone();
        }

        /**
         * Returns convex hull points as {@link Vector2}[].
         *
         * @return convex hull points as {@link Vector2}[].
         */
        public Vector2[] getHullPointsAsArray() {
            if (hullPoints == null) return null;
            Vector2[] hulldots = new Vector2[hullPoints.size()];
            for (int i = 0; i < hulldots.length; i++) {
                hulldots[i] = (Vector2) hullPoints.elementAt(i);
            }
            return hulldots;
        }


        void reorderPoints(Vector v) {
            AngleWrapper[] angleWrappers = new AngleWrapper[v.size()];
            double xc = 0;
            double yc = 0;
            for (int i = 0; i < v.size(); i++) {
                Vector2 pt = (Vector2) v.elementAt(i);
                xc += pt.x;
                yc += pt.y;
            }

            xc /= v.size();
            yc /= v.size();

            for (int i = 0; i < angleWrappers.length; i++) {
                angleWrappers[i] = createAngleWrapper((Vector2) v.elementAt(i), xc, yc);
            }
            java.util.Arrays.sort(angleWrappers, new AngleComparator());
            v.removeAllElements();
            for (int i = 0; i < angleWrappers.length; i++) {
                v.add(angleWrappers[i].pt);
            }
        }

        void qhull(Object[] dots0, int up, int step) {
            fullSteps++;
            if (dots0 == null || dots0.length < 1 || step > 200) return;
            if (dots0.length < 2) {
                addHullPoint((Vector2) dots0[0]);
                return;
            }
            try {
                int leftIndex = 0;
                int rightIndex = 0;
                for (int i = 1; i < dots0.length; i++) {
                    if (((Vector2) dots0[i]).x < ((Vector2) dots0[leftIndex]).x) {
                        leftIndex = i;
                    }
                    if (((Vector2) dots0[i]).x > ((Vector2) dots0[rightIndex]).x) {
                        rightIndex = i;
                    }
                }
                Vector2 leftPoint = (Vector2) dots0[leftIndex];
                Vector2 rightPoint = (Vector2) dots0[rightIndex];
                addHullPoint(leftPoint);
                addHullPoint(rightPoint);
                if (dots0.length == 3) {
                    int middlePoint = -1;
                    for (int i = 0; i < dots0.length; i++) {
                        if (i == leftIndex || i == rightIndex) continue;
                        middlePoint = i;
                        break;
                    }
                    addHullPoint((Vector2) dots0[middlePoint]);
                } else if (dots0.length > 3) {
                    Vector vIn = new Vector();
                    Vector vOut = new Vector();
                    if (up >= 0) {
                        int upIndex = selectPoints(dots0, leftPoint, rightPoint, true, vIn);
                        if (upIndex >= 0 && vIn.size() > 0) {
                            Vector2 upPoint = (Vector2) vIn.elementAt(upIndex);
                            vOut.removeAllElements();
                            selectPoints(vIn, leftPoint, upPoint, true, vOut);
                            qhull(vOut.toArray(), 1, step + 1);
                            vOut.removeAllElements();
                            selectPoints(vIn, upPoint, rightPoint, true, vOut);
                            qhull(vOut.toArray(), 1, step + 1);
                        }
                    }
                    if (up <= 0) {
                        vIn.removeAllElements();
                        int downIndex = selectPoints(dots0, rightPoint, leftPoint, false, vIn);
                        if (downIndex >= 0 && vIn.size() > 0) {
                            Vector2 downPoint = (Vector2) vIn.elementAt(downIndex);
                            vOut.removeAllElements();
                            selectPoints(vIn, rightPoint, downPoint, false, vOut);
                            qhull(vOut.toArray(), -1, step + 1);
                            vOut.removeAllElements();
                            selectPoints(vIn, downPoint, leftPoint, false, vOut);
                            qhull(vOut.toArray(), -1, step + 1);
                        }
                    }
                }
            } catch (Throwable t) {
            }
        }

        void addHullPoint(Vector2 pt) {
            if (!hullPoints.contains(pt)) hullPoints.add(pt);
        }

        int selectPoints(Object[] pIn, Vector2 pLeft, Vector2 pRight, boolean up, Vector vOut) {
            int retValue = -1;
            if (pIn == null || vOut == null) return retValue;
            double k = (double) (pRight.y - pLeft.y) / (double) (pRight.x - pLeft.x);
            double A = -k;
            double B = 1;
            double C = k * pLeft.x - pLeft.y;
            double dup = 0;
            for (int i = 0; i < pIn.length; i++) {
                Vector2 pt = (Vector2) pIn[i];
                if (pt.equals(pLeft) || pt.equals(pRight)) continue;
                double px = pt.x;
                double py = pt.y;
                double y = pLeft.y + k * (px - pLeft.x);
                if ((!up && y < py) || (up && y > py)) {
                    vOut.add(pt);
                    double d = (A * px + B * py + C);
                    if (d < 0) d = -d;
                    if (d > dup) {
                        dup = d;
                        retValue = vOut.size() - 1;
                    }
                }
            }
            vOut.add(pLeft);
            vOut.add(pRight);
            return retValue;
        }

        int selectPoints(Vector vIn, Vector2 pLeft, Vector2 pRight, boolean up, Vector vOut) {
            int retValue = -1;
            if (vIn == null || vOut == null) return retValue;
            double k = (double) (pRight.y - pLeft.y) / (double) (pRight.x - pLeft.x);
            double A = -k;
            double B = 1;
            double C = k * pLeft.x - pLeft.y;
            double dup = 0;
            for (int i = 0; i < vIn.size(); i++) {
                Vector2 pt = (Vector2) vIn.elementAt(i);
                if (pt.equals(pLeft) || pt.equals(pRight)) continue;
                double px = pt.x;
                double py = pt.y;
                double y = pLeft.y + k * (px - pLeft.x);
                if ((!up && y < py) || (up && y > py)) {
                    vOut.add(pt);
                    double d = (A * px + B * py + C);
                    if (d < 0) d = -d;
                    if (d > dup) {
                        dup = d;
                        retValue = vOut.size() - 1;
                    }
                }
            }
            vOut.add(pLeft);
            vOut.add(pRight);
            return retValue;
        }

        AngleWrapper createAngleWrapper(Vector2 pt, double xc, double yc) {
            double angle = Math.atan2(pt.y - yc, pt.x - xc);
            if (angle < 0) angle += 2 * Math.PI;
            return new AngleWrapper(angle, pt.clone());
        }

        private Vector2[] readPointsFromFile() {
            Vector<Vector2> points = new Vector<>();
            try {
                Scanner scanner = new Scanner(new File("src/points.txt"));
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] vals = line.split(",");
                    points.add(new Vector2(Double.parseDouble(vals[0]), Double.parseDouble(vals[1])));
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Vector2[] pointsArray = new Vector2[points.size()];
            for (int i = 0; i < points.size(); i++) {
                pointsArray[i] = points.get(i);
            }

            return pointsArray;
        }

        class AngleComparator implements java.util.Comparator {
            public int compare(Object obj1, Object obj2) {
                if (!(obj1 instanceof AngleWrapper) || !(obj2 instanceof AngleWrapper)) return 0;
                AngleWrapper ac1 = (AngleWrapper) obj1;
                AngleWrapper ac2 = (AngleWrapper) obj2;
                return (ac1.angle < ac2.angle) ? -1 : 1;
            }
        }

        class AngleWrapper implements Comparable {
            double angle;
            Vector2 pt;

            AngleWrapper(double angle, Vector2 pt) {
                this.angle = angle;
                this.pt = pt;
            }

            public int compareTo(Object obj) {
                if (!(obj instanceof AngleWrapper)) return 0;
                AngleWrapper ac = (AngleWrapper) obj;
                return (ac.angle < angle) ? -1 : 1;
            }
        }

    }
}
