/*
 * MeshIneBits is a Java software to disintegrate a 3d project (model in .stl)
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

package meshIneBits.borderPaver;

import meshIneBits.Bit2D;
import meshIneBits.NewBit2D;
import meshIneBits.borderPaver.util.GeneralTools;
import meshIneBits.borderPaver.util.Placement;
import meshIneBits.borderPaver.util.Section;
import meshIneBits.borderPaver.util.SectionTransformer;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Vector;


public class ConvexBorderAlgorithm {
    /**
     * Return the distance between two points via a vector by computing a projection along the vector.
     *
     * @param refPoint          the first point
     * @param point             the second point
     * @param directionalVector the directional vector
     * @return the projected distance
     */
    public static double getDistFromFromRefPointViaVector(Vector2 refPoint, Vector2 point, Vector2 directionalVector) {
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
    private static Vector2 getFurthestPointFromRefPointViaVector(Vector2 refPoint, Vector2 directionalVector, Section points) {
        directionalVector = directionalVector.normal();
        double maxDist = Double.NEGATIVE_INFINITY;
        Vector2 furthestPoint = null;
        for (Vector2 p : points.getPoints()) {
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
    private static Vector2 getBitCollinearVector(Section sectionReduced) {
        Vector2 startPoint = sectionReduced.getStartPoint();

        // compute the convex hull's points
        Section hullReduced = sectionReduced.getHull();

        // find the vector which connect the startPoint to the furthest (from the startPoint) point of the section
        Vector2 furthestPoint = sectionReduced.getFurthestPoint(startPoint);
        Vector2 startToFurthestPoint = furthestPoint.sub(startPoint);

        // find directional vector of constraint, oriented to the end of the section
        Vector2 dirConstraintSegment = hullReduced.getLongestSegment().getDirectionalVector();

        if (startToFurthestPoint.dot(dirConstraintSegment) < 0) { //if the dirConstraintSegment is not pointing to the end of the section
            return dirConstraintSegment.getOpposite();
        }
        return dirConstraintSegment;
    }

    /**
     * The position of the bit is given by a vector that connects startPoint to the center of the bit.
     * This vector has to components : Collinear and Normal to the bit.
     *
     * @param bitCollinearVector the vector given by
     * @param bitLength          the length of the Bit placed.
     * @return the collinear component of the vector.
     */
    private static Vector2 getBitPositionCollinear(Vector2 bitCollinearVector, double bitLength) {
        return bitCollinearVector.mul(bitLength / 2);
    }

    /**
     * Compute the new (reduced) length of the bit considering the section reduced
     *
     * @param sectionReduced     the reduced section of points
     * @param bitCollinearVector the bit collinear vector
     * @return the length of the bit
     */
    private static double getBitLengthFromSectionReduced(Section sectionReduced, Vector2 bitCollinearVector) {
        Vector2 startPoint = sectionReduced.getStartPoint();
        Vector2 furthestPoint = sectionReduced.getFurthestPointFromRefPointViaVector(startPoint, bitCollinearVector);
        return getDistFromFromRefPointViaVector(startPoint, furthestPoint, bitCollinearVector);
    }

    /**
     * The position of the bit is given by a vector that connects startPoint to the center of the bit.
     * This vector has to components : Collinear and Normal to the bit.
     *
     * @param startBit       the start bit
     * @param sectionReduced the reduced section of points on which a bit can take place.
     * @param areaSlice      the area of the Slice
     * @return the normal component of the vector.
     */
    private static Vector2 getPositionNormal(Vector2 startBit, Section sectionReduced, Area areaSlice) {
        boolean sectionReducedIsClosed = sectionReduced.getStartPoint().asGoodAsEqual(sectionReduced.getPoints().lastElement());
        Section hullReduced = sectionReduced.getHull(); //computes the convex hull points
        // compute hull segments
        Vector<Segment2D> segmentsHull = hullReduced.getSegments();

        /*
        Find the direction of the bit normal vector. Oriented toward the inner
        There are 3 possible cases :
        1 : the constraint segment is IN the Slice
        2 : the segment is OUT of the Slice
        3 : the segment is part of the Slice
        4 : the section is closed and can be overlapped by a bit
         */
        Segment2D constraintSegment = hullReduced.getLongestSegment();
        Vector2 constraintPoint = hullReduced.getFurthestPointFromSegment(constraintSegment);
        Vector2 dirConstraintSegmentNormal = constraintSegment.getNormal();
        Vector2 midPoint = constraintSegment.getMidPoint();
        Vector2 constraintToMidPoint = midPoint.sub(constraintPoint);
        Vector2 check = constraintPoint.add(constraintToMidPoint).add(constraintToMidPoint.normal().mul(1e-5));
        Vector2 positionNormal;

        if (constraintPoint.isOnSegment(constraintSegment)) { // case 3 : the hull is a straight line
            dirConstraintSegmentNormal = getInnerDirectionalVector(constraintSegment, areaSlice);
            positionNormal = dirConstraintSegmentNormal.mul(CraftConfig.bitWidth / 2);

        } else if (sectionReducedIsClosed) { // case 4 : covered section closed
            double lenPositionNormal = CraftConfig.bitWidth / 2 - Vector2.Tools.distanceFromPointToLine(startBit, constraintSegment);
            positionNormal = dirConstraintSegmentNormal.mul(lenPositionNormal);

        } else if (areaSlice.contains(check.x, check.y)) { // case 1 : constraint segment is in, so we have to inverse the direction of dirConstraintVectorNormal
            //Here we want the bit to be placed parallel to slice (as in Tour.stl), but currently doesn't work.

            // research of the longest segment
            double lengthMax = 0;
            for (Segment2D segment2D : segmentsHull.subList(0,
                                                            segmentsHull.size() - 1)) {//the last one, is the one that closes the hull
                if (segment2D.getLength() > lengthMax) {
                    constraintSegment = segment2D;
                    lengthMax = segment2D.getLength();
                }
            }
            dirConstraintSegmentNormal = constraintSegment.getNormal();


            if (dirConstraintSegmentNormal.dot(constraintToMidPoint) < 0) // In the case of the vector is in the bad direction
                dirConstraintSegmentNormal = dirConstraintSegmentNormal.getOpposite();
            double normPositionNormal = CraftConfig.bitWidth / 2 - getDistFromFromRefPointViaVector(constraintPoint, startBit, dirConstraintSegmentNormal);
            positionNormal = dirConstraintSegmentNormal.mul(normPositionNormal);

        } else { // case 2
            if (dirConstraintSegmentNormal.dot(constraintToMidPoint) > 0)
                dirConstraintSegmentNormal = dirConstraintSegmentNormal.getOpposite();
            double normPositionNormal = CraftConfig.bitWidth / 2 - getDistFromFromRefPointViaVector(midPoint, startBit, dirConstraintSegmentNormal);
            positionNormal = dirConstraintSegmentNormal.mul(normPositionNormal);
        }

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
    public static Placement getBitPlacement(Section sectionPoints, Area areaSlice, double minWidthToKeep) {

        Section sectionReduced = sectionPoints.getSectionReduced(sectionPoints, minWidthToKeep);
        Vector2 startPoint = sectionReduced.getStartPoint();

        Vector2 bitCollinearVector = getBitCollinearVector(sectionReduced);

        // the point on which we can place the start of the Bit
        Vector2 startBit = getFurthestPointFromRefPointViaVector(startPoint, bitCollinearVector.getOpposite(), sectionReduced);

        double newLengthBit = getBitLengthFromSectionReduced(sectionReduced, bitCollinearVector) + minWidthToKeep; //minWidthToKeep let the bit cover totally the section

        Vector2 positionCollinear = getBitPositionCollinear(bitCollinearVector, newLengthBit);
        Vector2 positionNormal = getPositionNormal(startBit, sectionReduced, areaSlice);

        Vector2 bitPosition = startBit.add(positionCollinear).add(positionNormal);
        Vector2 bitOrientation = positionCollinear.normal();

        Bit2D bit2D = new NewBit2D(bitPosition.x, bitPosition.y, newLengthBit, CraftConfig.bitWidth, bitOrientation.x, bitOrientation.y);
        Placement placement = new Placement(bit2D);
        placement.setSectionCovered(sectionReduced);
        placement.setNextStartPoint(sectionReduced.getPoints().lastElement());
        return placement;
    }

    /**
     * A {@link Segment2D} is part of the boundary of an {@link Area}. This method finds a {@link Vector2}, normal
     * to the segment, oriented toward the inside of the area.
     *
     * @param segment2D the segment, part of the boundary of the {@link Area} given as second parameter
     * @param area      the area
     * @return returns a {@link Vector2}, normal to the segment, oriented toward the inside of the area.
     */
    private static Vector2 getInnerDirectionalVector(Segment2D segment2D, Area area) {
        double distCheck = 0.1;
        Vector2 dir = segment2D.getNormal();
        Vector2 vectorCheck = segment2D.getMidPoint().add(dir.mul(distCheck));
        if (!area.contains(vectorCheck.x, vectorCheck.y)) {
            dir = dir.getOpposite();
        }
        return dir;
    }


    /**
     * Compute each bit to place on the border of the Slice using BorderedPattern algorithms
     *
     * @param slice          the slice to pave
     * @param minWidthToKeep minimum distance of wood needed to be kept when placing, in order to avoid the cut bit to be too fragile
     * @param numberMaxBits  the maximum number of bits to place on each border
     * @return the list of bits for this Slice
     */
    public static Vector<Bit2D> getBits(Slice slice, double minWidthToKeep, double numberMaxBits) throws NoninvertibleTransformException {
        Vector<Bit2D> bits = new Vector<>();

        Vector<Vector<Vector2>> bounds = GeneralTools.getBoundsAndRearrange(slice);
        Area areaSlice = AreaTool.getAreaFrom(slice);


        for (Vector<Vector2> bound : bounds) {
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 nextStartPoint = bound.get(0);

            System.out.println("\033[0;34m" + "++++++++++++++++ BOUND " + bounds.indexOf(bound) + " ++++++++++++++++++" + "\033[0m");

            int iBit = 0;
            Placement placement;
            do {
                System.out.print("\033[0;32m" +"\t " + "BIT PLACEMENT"+"\tCONVEXE\t"+" : " + iBit + "\033[0m");

                Section sectionPoints = SectionTransformer.getSectionFromBound(bound, nextStartPoint);
                placement = ConvexBorderAlgorithm.getBitPlacement(sectionPoints, areaSlice, minWidthToKeep);
                bits.add(placement.bit2D);
                nextStartPoint = placement.nextStartPoint;

                System.out.println("\033[0;32m" +"\t: END" + "\033[0m");
                iBit++;

            } while (!((Section.listContainsAsGoodAsEqual(veryFirstStartPoint, placement.sectionCovered.getPoints()) && iBit > 1) || Section.listContainsAllAsGoodAsEqual(bound, placement.sectionCovered.getPoints())) && iBit < numberMaxBits);
        }
        return bits;

    }
}
