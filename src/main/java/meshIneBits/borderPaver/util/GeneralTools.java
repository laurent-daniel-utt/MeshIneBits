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

package meshIneBits.borderPaver.util;

import meshIneBits.Bit2D;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Polygon;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Area;
import java.util.Vector;


/**
 * GeneralTools groups together the methods used to prepare the data for algorithms that pave the border of the Slice.
 * It also provides methods to perform intersection point search.
 */
public class GeneralTools {

    /**
     * Looks for an intersection point between a side of a bit and a closed contour (bound). The point returned
     * is the first intersection between the bit and the contour, while scanning the contour in the direction of
     * increasing indices of the points of the contour.
     *
     * @param bit         a {@link Bit2D}.
     * @param boundPoints the points of the contour.
     * @return the first intersection between the bit and the closed contour.
     */
    /*
    4 different possibilities :
        - 0 or 1 intersections --> exception, the bit is not well-placed
        - 2 intersections --> the bit is well-placed. Case typical, we return the second point
        - 3 intersections --> the bit is well-placed. Case segment orthogonal, we return the second point, because the third is after the second
        - 3 intersections --> the bit is well-placed. Case segment orthogonal, we return the third point, because the first is before the startPoint
     */
    public static Vector2 getBitAndContourSecondIntersectionPoint(@NotNull Bit2D bit, @NotNull Vector<Vector2> boundPoints, Vector2 startPoint) {
        Vector<Segment2D> bitSidesSegments = bit.getBitSidesSegments();
        Vector<Segment2D> contourSegments = Section.pointsToSegments(boundPoints);
        //We add the last segment to be able to calculate the intersections. We remove it at the end
        contourSegments.add(new Segment2D(boundPoints.get(boundPoints.size() - 2), boundPoints.get(0)));

        Vector<Vector2> intersections = new Vector<>();

        for (Segment2D contourSegment : contourSegments) {
            for (Segment2D bitSideSegment : bitSidesSegments) {
                Vector2 intersection = Segment2D.getIntersectionPoint(bitSideSegment, contourSegment);
                if (intersection != null && !intersections.contains(intersection)) {
                    intersections.add(intersection);
                }
            }
        }

        if (intersections.size() == 0) {
            throw new RuntimeException("No intersection found");
        }

        contourSegments.remove(contourSegments.size() - 1);

        // if the first intersection is the startPoint, we take the third intersection
        if (intersections.size() == 3 && Section.isABeforeBOnPolygon(intersections.get(0), startPoint, contourSegments)) {
            return intersections.get(2);
        }

        return intersections.get(1);
    }


    /**
     * Returns the first intersection point between the bound and bit's edges
     *
     * @param bit         a bit
     * @param boundPoints the points of the bound
     * @return the first intersection point between the bound and bit's edges
     */
    public static @Nullable Vector2 getBitAndContourFirstIntersectionPoint(@NotNull Bit2D bit, @NotNull Vector<Vector2> boundPoints) {

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

        // get sides of the bit as Segment2Ds (will be used later)
        Vector<Segment2D> bitSides = bit.getBitSidesSegments();

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
     * Returns the points of each bound of a given Slice
     * The points are rearranged to be in the correct order.
     *
     * @param currentSlice the slice to get the bounds.
     * @return the bounds of the given slice, once rearranged.
     * @see SectionTransformer#rearrangeSegments
     * @see Section#rearrangePoints
     */
    @SuppressWarnings("unchecked")
    public static @NotNull Vector<Vector<Vector2>> getBoundsAndRearrange(@NotNull Slice currentSlice) {
        Vector<Vector<Vector2>> boundsList = new Vector<>();
        Vector<Vector<Segment2D>> borderList = SectionTransformer.rearrangeSegments((Vector<Segment2D>) currentSlice.getSegmentList().clone());

        for (Vector<Segment2D> border : borderList) {
            Vector<Vector2> unorderedPoints = Section.segmentsToPoints(border);
            boundsList.add(Section.rearrangePoints(unorderedPoints));
        }
        return boundsList;
    }

    /**
     * Return the next Bit2D start point.
     * It is the intersection between the slice and the end side of the Bit2D.
     *
     * @param bit         the current Bit2D (the last placed Bit2D by AI).
     * @param boundPoints the points of the bounds on which stands the bit.
     * @return the next bit start point. Returns <code>null</code> if none was found.
     */
    public Vector2 getNextBitStartPoint(@NotNull Bit2D bit, @NotNull Vector<Vector2> boundPoints, Vector2 startPoint) throws Exception {
        Vector2 nextBitStartPoint = getBitAndContourSecondIntersectionPoint(bit, boundPoints, startPoint);

        if (nextBitStartPoint != null) {
            return nextBitStartPoint;
        } else {
            throw new Exception("The bit start point has not been found.");
        }
    }

    /**
     * Return the next Bit2D start point.
     * It is the intersection between the slice and the end side of the Bit2D.
     *
     * @param bit         the current Bit2D (the last placed Bit2D by AI).
     * @param boundPoints the points of the bounds on which stands the bit.
     * @return the next bit start point. Returns <code>null</code> if none was found.
     */
    public static Vector2 getNextBitStartPoint(@NotNull Bit2D bit, @NotNull Vector<Vector2> boundPoints) throws Exception {
        return getNextBitStartPoint(bit,
                                    boundPoints,
                                    null);
    }
}