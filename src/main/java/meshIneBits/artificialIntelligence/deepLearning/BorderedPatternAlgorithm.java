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

    //minimum distance of wood needed to be kept when placing, in order to avoid the cut bit
    private @NotNull Vector<Vector2> getSectionReduced(@NotNull Vector<Vector2> sectionPoints, double minWidthToKeep) {
        Vector<Vector2> sectionRepopulated = GeneralTools.repopulateWithNewPoints(100, sectionPoints, true);

        boolean sectionReductionCompleted = false;

        Segment2D constraintSegment;
        Vector2 furthestPoint;

        do {
            // calculates the convex hull of the section's points
            Vector<Vector2> hull = getConvexHull(sectionRepopulated);
            hull.add(hull.firstElement());
            Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hull);

            // find the constraint segment, which is the longest segment of the hull // todo, peut être pas toujours le cas
            constraintSegment = getLongestSegment(segmentsHull);

            // find the constraint point, which is the convex hull's furthest point from the constraint segment
            furthestPoint = getFurthestPointFromSegment(constraintSegment, hull);

            // calculate distance between constraint point and constraint segment
            double maxDistance = Vector2.Tools.distanceFromPointToLine(furthestPoint, constraintSegment);

            /*
             If this condition is true is executed, this means the bit can't be placed over all the section's points
             while respecting the minWidthToKeep. In this case the content of the following "if" reduces the section,
             starting by the last point, until the first "cut point" (the constraint point or an end of the
             constraint segment) reached, thus this point is the fist one that prevents the section to be thinner.
             */
            if (maxDistance > CraftConfig.bitWidth - minWidthToKeep) {
                // list the cut points
                Vector<Vector2> cutPoints = new Vector<>();
                cutPoints.add(constraintSegment.start);
                cutPoints.add(constraintSegment.end);
                cutPoints.add(furthestPoint);

                // research of the first "cut point" of the section, starting research by its last point.
                boolean cutPointFound = false;
                int iSection = sectionRepopulated.size() - 1;
                while (!cutPointFound) {
                    if (cutPoints.contains(sectionRepopulated.get(iSection))) {
                        // delete section's points from the cut point at iSection (included) to the last point of the section
                        while (sectionRepopulated.size() > iSection) {
                            sectionRepopulated.remove(iSection);
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

        return sectionRepopulated;
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
        hullReduced.add(hullReduced.firstElement());

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
     * @param bitLength the length of the Bit placed.
     * @return the collinear component of the vector.
     */
    private Vector2 getBitPositionCollinear(@NotNull Vector2 bitCollinearVector, double bitLength) {
        return bitCollinearVector.mul(bitLength / 2);
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
        Vector2 startPoint = sectionReduced.firstElement();
        Vector<Vector2> hullReduced = getConvexHull(sectionReduced); //computes the convex hull points
        hullReduced.add(hullReduced.firstElement());
        // calculer les segments du hull
        Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hullReduced); //computes the
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
        Vector2 furthestPoint = getFurthestPointFromRefPointViaVector(startPoint, bitCollinearVector, sectionReduced);
        return getDistFromFromRefPointViaVector(startPoint, furthestPoint, bitCollinearVector);


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
     * Checks if check3 is not part of the convex hull
     *
     * @return true if check3 is not part of the Hull
     * @see #getConvexHull(Vector)
     */
    // Taken from : https://www.tutorialcup.com/interview/algorithm/convex-hull-algorithm.htm
    private boolean OrientationMatch(@NotNull Vector2 check1, @NotNull Vector2 check2, @NotNull Vector2 check3) {
        double val = (check2.y - check1.y) * (check3.x - check2.x) - (check2.x - check1.x) * (check3.y - check2.y);
        if (check2.asGoodAsEqual(check3) || check1.asGoodAsEqual(check2) ||
                (Math.abs(val) < Math.pow(10, -CraftConfig.errorAccepted)
                        && check3.sub(check2).dot(check1.sub(check2)) <= 0)) {
            return false;
        }
        return val <= Math.pow(10, -CraftConfig.errorAccepted);
    }

    /**
     * Uses Gift-Wrapping algorithm
     *
     * @param points to points to compute the convex Hull on
     * @return the points of the convex Hull that include the given points
     */
    // Taken from : https://www.tutorialcup.com/interview/algorithm/convex-hull-algorithm.htm
    public Vector<Vector2> getConvexHull(@NotNull Vector<Vector2> points) {
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

    /**
     * Compute each bit to place on the border of the Slice using BorderedPattern algorithms
     *
     * @param slice          the slice to pave
     * @param minWidthToKeep minimum distance of wood needed to be kept when placing, in order to avoid the cut bit to be too fragile
     * @return the list of bits for this Slice
     */
    public Vector<Bit2D> getBits(@NotNull Slice slice, double minWidthToKeep) {
        Vector<Bit2D> bits = new Vector<>();

        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);
        Area areaSlice = AreaTool.getAreaFrom(slice);

        for (Vector<Vector2> bound : bounds) {
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 nextStartPoint = bound.get(0);

            Vector<Vector2> sectionPoints;
            int iBit = 0;//TODO DEBUGONLY
            Placement placement;
            do {
                System.out.println("PLACEMENT BIT " + iBit + "====================");
                sectionPoints = GeneralTools.getSectionPointsFromBound(bound, nextStartPoint);
                placement = getBitPlacement(sectionPoints, areaSlice, minWidthToKeep);
                bits.add(placement.bit2D);
                nextStartPoint = placement.end;

                iBit++;

            }
            while (!placement.sectionCovered.contains(veryFirstStartPoint)); //Add each bit on the bound
        }
        return bits;

    }

    /**
     * Finds the furthest point among a list of points from a reference point (refPoint), calculating direct distances
     * between them.
     * @param refPoint the point from which the method calculates the distances
     * @param points the list of points among which the methods search the furthest point from the refPoint
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

    //TODO DEBUGONLY

    /**
     * Debug uniquement : peut être utilisée pour placer un seul bit à un endroit utilisé en choisissant les coodronnées
     * du startPoint dans le code
     *
     * @param slice          the slice to pave
     * @param minWidthToKeep minimum distance of wood needed to be kept when placing, in order to avoid the cut bit
     *                       to be too fragile
     * @return the list of bits for this Slice
     */
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
