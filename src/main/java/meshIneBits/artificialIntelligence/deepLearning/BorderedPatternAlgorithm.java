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
import meshIneBits.artificialIntelligence.DebugTools;
import meshIneBits.artificialIntelligence.GeneralTools;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Vector;


public class BorderedPatternAlgorithm {

//    private PlotHelper plotHelper = new PlotHelper();

    /**
     * Checks if check3 is not part of the convex hull
     *
     * @return true if check3 is not part of the Hull
     * @see #QuickHull.getConvexHull(Vector)
     */
    // Taken from : https://www.tutorialcup.com/interview/algorithm/convex-hull-algorithm.htm
    @Deprecated
    private static boolean OrientationMatch(@NotNull Vector2 check1, @NotNull Vector2 check2, @NotNull Vector2 check3) {
        double val = (check2.y - check1.y) * (check3.x - check2.x) - (check2.x - check1.x) * (check3.y - check2.y);

        if (check2.asGoodAsEqual(check3) || check1.asGoodAsEqual(check2) || (Math.abs(val) < Math.pow(10, -5) && check3.sub(check2).dot(check1.sub(check2)) <= 0)) {
            return false;
        }
        return val <= Math.pow(10, -5);
    }

    public static void pointsToFile(@NotNull Vector<Vector2> points) {
        BufferedWriter writer;
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

    /**
     * Compute and return the longest segment of the given list
     *
     * @param segment2DS the segments list
     * @return the longest segment
     */
    private Segment2D getLongestSegment(@NotNull Vector<Segment2D> segment2DS) {
        return segment2DS.stream().max(Comparator.comparing(Segment2D::getLength)).orElseThrow(NoSuchElementException::new);
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
     * Return a vector collinear to the bit to place on the sectionReduced, the vector is orienting toward the end of the bit
     *
     * @param sectionReduced the reduced section of points on which a bit can take place.
     * @return the collinear vector
     */
    private Vector2 getBitCollinearVector(@NotNull Vector<Vector2> sectionReduced) {
        Vector2 startPoint = sectionReduced.firstElement();

        // compute the convex hull's points
//        Vector<Vector2> hullReduced = getConvexHull(sectionReduced);
        QuickHull quickHull = new QuickHull(sectionReduced);
        Vector<Vector2> hullReduced = quickHull.getConvexHull();

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

//    /**
//     * Uses Gift-Wrapping algorithm
//     *
//     * @param points to points to compute the convex Hull on
//     * @return the points of the convex Hull that include the given points
//     */
    // Taken from : https://www.tutorialcup.com/interview/algorithm/convex-hull-algorithm.htm
//    public static Vector<Vector2> getConvexHull(Vector<Vector2> points) {
//        int length = points.size();
//        Vector<Vector2> result = new Vector<>();
//        // find leftMosts
//        Vector<Integer> iLeftMosts = new Vector<>();
//        double xMin = Double.POSITIVE_INFINITY;
//        for (Vector2 point : points) {
//            double x = point.x;
//            if (x <= xMin) {
//                xMin = x;
//                System.out.println("xMin = " + xMin);
//            }
//        }
//        for (int i = 0; i < length; i++) {
//            if (Math.abs(points.get(i).x - xMin) < Math.pow(10, -5))
//                iLeftMosts.add(i);
//        }
//        // find higher of leftMosts
//        int iHigherLeftMost = iLeftMosts.get(0);
//        for (int i = 1; i < iLeftMosts.size(); i++) {
//            if (points.get(iLeftMosts.get(i)).y > points.get(iHigherLeftMost).y)
//                iHigherLeftMost = iLeftMosts.get(i);
//        }
//
//        int p = iHigherLeftMost, pointQ;
//        do {
//            result.add(points.get(p));
//            pointQ = (p + 1) % length;
//            for (int i = 0; i < length; i++) {
//                if (OrientationMatch(points.get(p), points.get(i), points.get(pointQ))) {
//                    pointQ = i;
//                }
//            }
//            p = pointQ;
//        }
//        while (p != iHigherLeftMost);
//
//        result.add(result.firstElement());
//
//        return result;
//    }

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

    //minimum distance of wood needed to be kept when placing, in order to avoid the cut bit
    private @NotNull Vector<Vector2> getSectionReduced(@NotNull Vector<Vector2> sectionPoints, double minWidthToKeep) {
        Vector<Vector2> sectionToReduce = GeneralTools.repopulateWithNewPoints(100, sectionPoints, true);
//        pointsToFile(sectionToReduce);

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
            //                points.set(i, sectionToReduce.get(i));
            Vector<Vector2> points = new Vector<>(sectionToReduce);
//            pointsToFile(points);
            QuickHull quickHull = new QuickHull(points);
            System.out.println("BorderedPatternAlgorithm.getSectionReduced");
            Vector<Vector2> hull = quickHull.getConvexHull();
            Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hull);

            // find the constraint segment, which is the longest segment of the hull // todo, maybe not always the case
            constraintSegment = getLongestSegment(segmentsHull);

            // find the constraint point, which is the convex hull's furthest point from the constraint segment
            furthestPoint = getFurthestPointFromSegment(constraintSegment, hull);

            // calculate distance between constraint point and constraint segment
            double sectionWidth = Vector2.Tools.distanceFromPointToLine(furthestPoint, constraintSegment);
            /*
             If this condition is true, this means the bit can't be placed over all the section's points
             while respecting the minWidthToKeep. In this case the content of the following "if" reduces the section,
             starting by the last point, until the first "cut point" (the constraint point or an end of the
             constraint segment) reached, thus this point is the first one that prevents the section to be thinner.
             */
            if ((!sectionIsClosed && (sectionWidth > CraftConfig.bitWidth - minWidthToKeep)) || (sectionIsClosed && sectionWidth > CraftConfig.bitWidth)) {

                // list the cut points
                Vector<Vector2> cutPoints = new Vector<>();
                cutPoints.add(constraintSegment.start);
                cutPoints.add(constraintSegment.end);
                cutPoints.add(furthestPoint);
                // research of the first "cut point" of the section, starting research by its last point.
                boolean cutPointFound = false;
                int iSection = sectionToReduce.size() - 1;
                while (!cutPointFound) {
                    System.out.println("BorderedPatternAlgorithm.getSectionReduced1");
                    if (cutPoints.contains(sectionToReduce.get(iSection))) {
                        // delete section's points from the cut point at iSection (included) to the last point of the section
                        while (sectionToReduce.size() > iSection) {
                            sectionToReduce.remove(iSection);
                            System.out.println("BorderedPatternAlgorithm.getSectionReduced2");
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
     * @param areaSlice      the area of the Slice
     * @return the normal component of the vector.
     */
    private Vector2 getPositionNormal(@NotNull Vector<Vector2> sectionReduced, Area areaSlice) {
        boolean sectionReducedIsClosed = sectionReduced.firstElement().asGoodAsEqual(sectionReduced.lastElement());
        Vector2 startPoint = sectionReduced.firstElement();
//        Vector<Vector2> hullReduced = getConvexHull(sectionReduced); //computes the convex hull points
        QuickHull quickHull = new QuickHull(sectionReduced);
        Vector<Vector2> hullReduced = quickHull.getConvexHull();

        // compute hull segments
        Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hullReduced);

        /*
        Find the direction of the bit normal vector. Oriented toward the inner
        There are 4 possible cases :
        1 : the segment that connects the end to the start of the hull is IN the Slice
        2 : the segment is OUT of the Slice
        3 : the segment is part of the Slice
        4 : TODO
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

            double normPositionNormal = CraftConfig.bitWidth / 2 - getDistFromFromRefPointViaVector(constraintPoint, midPoint, dirConstraintSegmentNormal);
            positionNormal = dirConstraintSegmentNormal.mul(normPositionNormal);
            System.out.println("cas 1");

        } else { // case 2
            if (dirConstraintSegmentNormal.dot(constraintToMidPoint) > 0)
                dirConstraintSegmentNormal = dirConstraintSegmentNormal.getOpposite();
            double normPositionNormal = CraftConfig.bitWidth / 2 - getDistFromFromRefPointViaVector(midPoint, startPoint, dirConstraintSegmentNormal);
            positionNormal = dirConstraintSegmentNormal.mul(normPositionNormal);
            System.out.println("cas 2, normPositionNormal = " + normPositionNormal);
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
        DebugTools.pointsToDrawBLUE = sectionPoints;
        System.out.println("BorderedPatternAlgorithm.getBitPlacement");
        Vector<Vector2> sectionReduced = getSectionReduced(sectionPoints, minWidthToKeep);
        System.out.println("BorderedPatternAlgorithm.getBitPlacement2");
        Vector2 startPoint = sectionReduced.firstElement();
        pointsToFile(sectionReduced);
        DebugTools.pointsToDrawGREEN = sectionReduced;
        DebugTools.setPaintForDebug(true);
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

        Bit2D bit2D = new Bit2D(bitPosition.x, bitPosition.y, newLengthBit, CraftConfig.bitWidth, bitOrientation.x, bitOrientation.y);


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
//            pointsToFile(bound);
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 nextStartPoint = bound.get(0);

            System.out.println("++++++++++++++ BOUND " + bounds.indexOf(bound) + " ++++++++++++++++");

            Vector<Vector2> sectionPoints;
            int iBit = 0;//TODO DebugOnly
            Placement placement;
            do {
                System.out.println("PLACEMENT BIT " + iBit + "====================");
                sectionPoints = GeneralTools.getSectionPointsFromBound(bound, nextStartPoint);
//                pointsToFile(sectionPoints);
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

            } while (!((listContainsAsGoodAsEqual(veryFirstStartPoint, placement.sectionCovered) && iBit > 1) || listContainsAllAsGoodAsEqual(bound, placement.sectionCovered)) && iBit < numberMaxBits);
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
}