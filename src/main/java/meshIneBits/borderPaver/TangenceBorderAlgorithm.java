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

package meshIneBits.borderPaver;

import meshIneBits.Bit2D;
import meshIneBits.borderPaver.util.GeneralTools;
import meshIneBits.borderPaver.util.Section;
import meshIneBits.borderPaver.util.SectionTransformer;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.awt.geom.NoninvertibleTransformException;
import java.util.List;
import java.util.Vector;

import static meshIneBits.borderPaver.util.Section.*;

public class TangenceBorderAlgorithm {

    /**
     * The minimum distance to keep between the bound and the bit in order to avoid the bit to be placed exactly
     * on the bound. Which causes the intersection computations to fail.
     */
    static final double MARGIN_EXT = 1;
    /**
     * The minimum segment length to consider when computing the convexity.
     * The convexity is computed only for segments longer than this value.
     */
    static final double MIN_SEGMENT_LENGTH = 3;

    /**
     * Computes the placement of a single Bit from a section, using the convexity type of the section.
     *
     * @param sectionPoints the section points.
     * @param startPoint    the start point of the section to place a bit.
     * @param MinWidth      the minimum bit width to keep.
     * @param convexType    the convexity type of the section (1 for convex, -1 for concave).
     * @return the placed bit.
     */
    public static Bit2D getBitFromSectionWithTangence(List<Vector2> sectionPoints, Vector2 startPoint, double MinWidth, int convexType) {
        Vector<Segment2D> segmentsSection = Section.pointsToSegments(sectionPoints);
        Segment2D lastPossibleSegment = null;

        for (Segment2D segment : segmentsSection) {
            // if the segment is too short, we skip it.
            if (segment.getLength() < MIN_SEGMENT_LENGTH) continue;
            //computes the position of the bottom-left edge of the bit
            Vector2 bottomLeftEdge = getProjStartPoint(startPoint, segment);

            //si la distance entre le point projeté (coinHautGauche) et le startPoint est inférieure à la largeur du bit, on a un bit possible
            //il faut également que l'on soit du bon côté de la section.

            if (convexType != CONVEX_TYPE_UNDEFINED && Vector2.dist(bottomLeftEdge, startPoint) < CraftConfig.bitWidth + MARGIN_EXT - MinWidth) {
                Segment2D offsetSegment = new Segment2D(segment.start.add(segment.getNormal().normal().mul(MARGIN_EXT)).sub(segment.getDirectionalVector().mul(400)), segment.end.add(segment.getNormal().normal().mul(MARGIN_EXT)).add(segment.getDirectionalVector().mul(400)));//todo faire une operation plus simple
                if (Section.getNumberOfIntersection(offsetSegment, sectionPoints) == 0 && convexType == CONVEX_TYPE_CONVEX) {//todo faire mieux pour ne pas utiliser les intersections
                    lastPossibleSegment = segment;
                } else if (Section.getNumberOfIntersection(offsetSegment, sectionPoints) <= 2 && convexType == CONVEX_TYPE_CONCAVE) {
                    lastPossibleSegment = segment;
                }
            }
        }

        if (lastPossibleSegment == null)
            lastPossibleSegment = segmentsSection.lastElement();//todo marche temporairement

        Vector2 bottomLeftEdge = getProjStartPoint(startPoint, lastPossibleSegment);

        //Computes the center of the bit
        Vector2 vecSegment = lastPossibleSegment.getDirectionalVector().normal();
        Vector2 vecSegmentOrthogonal = vecSegment.getCWAngularRotated().normal();
        Vector2 origin;

        if (convexType == -1) {//concave
            origin = bottomLeftEdge.sub(new Vector2(CraftConfig.bitWidth / 2, 0).rotate(vecSegmentOrthogonal));
            origin = origin.add(new Vector2(MinWidth, 0).rotate(vecSegmentOrthogonal));

        } else {//convex
            origin = bottomLeftEdge.add(new Vector2(CraftConfig.bitWidth / 2, 0).rotate(vecSegmentOrthogonal));
            origin = origin.sub(new Vector2(MARGIN_EXT, 0).rotate(vecSegmentOrthogonal));
            if (bottomLeftEdge.sub(startPoint).dot(vecSegmentOrthogonal) > 0) {
                origin = origin.sub(new Vector2(Vector2.dist(origin, bottomLeftEdge), 0).rotate(vecSegmentOrthogonal));
            }
        }

        origin = origin.sub(new Vector2(0, CraftConfig.lengthFull / 2).rotate(vecSegmentOrthogonal));
        return new Bit2D(origin, vecSegment);
    }

    /**
     * Computes the projection of a point on a segment, orthogonal to the segment.
     *
     * @param startPoint the point to project.
     * @param segment    the segment to project on.
     * @return the projection of the point on the segment.
     */
    public static Vector2 getProjStartPoint(Vector2 startPoint, Segment2D segment) {
        Vector2 distance = segment.start.sub(startPoint);
        Vector2 orthogonal = segment.getNormal();
        return startPoint.add(orthogonal.mul(distance.dot(orthogonal))); // the orthogonal projection
    }


    /**
     * Places all the bits on the bound of the given Slice.
     *
     * @param slice         the slice to pave.
     * @param minWidth      the minimum bit width to keep
     * @param numberMaxBits the maximum number of bits to place on a bound
     * @return the list of placed bits.
     */
    public static Vector<Bit2D> getBits(Slice slice, double minWidth, double numberMaxBits) throws NoninvertibleTransformException {
        Vector<Bit2D> bits = new Vector<>();
        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);
        for (Vector<Vector2> bound : bounds) {
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 nextStartPoint = bound.get(0);

            System.out.println("++++++++++++++ BOUND " + bounds.indexOf(bound) + " ++++++++++++++");

            List<Vector2> sectionPoints;
            Section section;
            int iBit = 0;
            do {
                System.out.print("\t " + "BIT PLACEMENT : " + iBit + "\t");
                section = SectionTransformer.getSectionFromBound(bound, nextStartPoint);
                sectionPoints = section.getPoints();

                //We first want to know if the beginning of the section is convex or concave
                //we then want to find the max convex or concave section
                int convexType = section.getConvexType(nextStartPoint);

                Bit2D bit = TangenceBorderAlgorithm.getBitFromSectionWithTangence(sectionPoints, nextStartPoint, minWidth, convexType);
                bits.add(bit);

                nextStartPoint = GeneralTools.getBitAndContourSecondIntersectionPoint(bit, bound, nextStartPoint);
                System.out.println("END BIT PLACEMENT");
                iBit++;

            } while (!((Section.listContainsAsGoodAsEqual(veryFirstStartPoint, sectionPoints) && iBit > 1) || Section.listContainsAllAsGoodAsEqual(bound, sectionPoints)) && iBit < numberMaxBits);
        }


        return bits;
    }
}
