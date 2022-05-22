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

package meshIneBits.artificialIntelligence;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.util.SectionTransformer;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.awt.geom.NoninvertibleTransformException;
import java.util.List;
import java.util.Vector;

public class TangenceAlgo {

    private static final int CONVEX_ERROR = -4;
    private final double MARGIN_EXT = 1;
    /**
     * The minimum segment length to consider when computing the convexity.
     * The convexity is computed only for segments longer than this value.
     */
    private final double MIN_SEGMENT_LENGTH = 3;

    public Vector<Bit2D> getBits(Slice slice, double minWidth, double numberMaxBits) throws NoninvertibleTransformException {
        Vector<Bit2D> bits = new Vector<>();
        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);
        for (Vector<Vector2> bound : bounds) {
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 nextStartPoint = bound.get(0);

            System.out.println("++++++++++++++ BOUND " + bounds.indexOf(bound) + " ++++++++++++++");

            List<Vector2> sectionPoints;
            int iBit = 0;
            do {
                System.out.print("\t " + "PLACEMENT BIT " + iBit + "\t");
                sectionPoints = SectionTransformer.getSectionPointsFromBound(bound, nextStartPoint);


                int convexType = 1000;
                //we first want to know if the beginning of the section is convex or concave
                //we then want to find the max convex or concave section

                Vector2 ORIGIN = new Vector2(0, 0);
                sectionPoints.add(ORIGIN);

                // pour savoir si on commence par une section convexe ou concave, on regarde tous les points
                // qui sont à une distance de BitLength/2 par exemple
                int nbPointsToCheck = 0;
                for (Vector2 point : sectionPoints) {
                    nbPointsToCheck++;
                    if (Vector2.dist(point, nextStartPoint) >= CraftConfig.lengthNormal / 2) {
                        break;
                    }
                }

                List<Vector2> maxConvexSection = new Vector<>();
                if (isConvex(sectionPoints.subList(0, nbPointsToCheck))) {
                    //section convexe
                    convexType = 1;

                    List<Vector2> convexSection = new Vector<>();
                    for (int i = 0; i < nbPointsToCheck; i++) {
                        convexSection.add(sectionPoints.get(i));
                    }
                    convexSection.add(ORIGIN);

                    //on agrandit convexSection tant que la section est convexe
                    int i = nbPointsToCheck;
                    do {
                        convexSection.add(convexSection.size() - 1, sectionPoints.get(i));
                        i++;
                    } while (i < sectionPoints.size() && isConvex(convexSection));
                    maxConvexSection = convexSection;
                    maxConvexSection.remove(ORIGIN);
                }

                if (maxConvexSection.size() > 0) { //on a trouvé une section convexe
                    sectionPoints = maxConvexSection;
                } else { //on a trouvé une section concave
                    convexType = -1;
                    sectionPoints.remove(ORIGIN);
                }

                sectionPoints.remove(ORIGIN);

                Bit2D bit = getBitFromSectionWithTangence(sectionPoints, nextStartPoint, minWidth, convexType);
                if (bit != null) {
                    bits.add(bit);

                    nextStartPoint = GeneralTools.getBitAndContourSecondIntersectionPoint(bit, bound, nextStartPoint);
                    System.out.println("FIN PLACEMENT BIT ");
                } else {
                    throw new RuntimeException("Bit could not be placed !");
                }
                iBit++;


            } while (!((listContainsAsGoodAsEqual(veryFirstStartPoint, sectionPoints) && iBit > 1) || listContainsAllAsGoodAsEqual(bound, sectionPoints)) && iBit < numberMaxBits);
        }


        return bits;
    }

    public Bit2D getBitFromSectionWithTangence(List<Vector2> sectionPoints, Vector2 startPoint, double MinWidth, int convexType) {
        Vector<Segment2D> segmentsSection = GeneralTools.pointsToSegments(sectionPoints);
        Segment2D lastSegmentPossible = null;

        for (Segment2D segment : segmentsSection) {
            //si jamais la longueur du segment est très petite (possible des fois), on passe ce segment.
            if (segment.getLength() < MIN_SEGMENT_LENGTH) continue;

            //pour chaque segment, on calcule la distance entre le début de segment et le startPoint.
            //on projette cette distance vers le vecteur orthogonal au segment.
            //on l'ajoute au startPoint. et ca nous donne le coinHautGauche du bit.
            Vector2 coinBasGauche = getProjStartPoint(startPoint, segment);
            //si la distance entre le point projeté (coinHautGauche) et le startPoint est inférieure à la largeur du bit, on a un bit possible
            //il faut également que l'on soit du bon côté de la section.

            if (convexType == 1 && Vector2.dist(coinBasGauche, startPoint) < CraftConfig.bitWidth + MARGIN_EXT - MinWidth || convexType == -1 && Vector2.dist(coinBasGauche, startPoint) < CraftConfig.bitWidth + MARGIN_EXT - MinWidth) {
                Segment2D segmentDecale = new Segment2D(segment.start.add(segment.getNormal().normal().mul(MARGIN_EXT)).sub(segment.getDirectionalVector().mul(400)), segment.end.add(segment.getNormal().normal().mul(MARGIN_EXT)).add(segment.getDirectionalVector().mul(400)));
                if (getNumberOfIntersection(segmentDecale, sectionPoints) == 0 && convexType == 1) {//todo faire mieux pour ne pas utiliser les intersections
                    lastSegmentPossible = segment;
                } else if (getNumberOfIntersection(segmentDecale, sectionPoints) <= 2 && convexType == -1) {
                    lastSegmentPossible = segment;
                }
            }
        }

        if (lastSegmentPossible == null)
            lastSegmentPossible = segmentsSection.lastElement();//todo marche temporairement

        Vector2 coinBasGauche = getProjStartPoint(startPoint, lastSegmentPossible);

        //on calcule le centre du bit, situé à une distance de CraftConfig.bitWidth/2 et CraftConfig.lengthNormal/2
        // de coinHautGauche, dans les bonnes directions.
        Vector2 vecSegment = lastSegmentPossible.getDirectionalVector().normal();
        Vector2 vecSegmentOrthogonal = vecSegment.getCWAngularRotated().normal();
        Vector2 origin;

        if (convexType == -1) {//concave
            //si le coinBasGauche est situé à l'extérieur de la section, on doit soustraire, et sinon ajouter
            origin = coinBasGauche.sub(new Vector2(CraftConfig.bitWidth / 2, 0).rotate(vecSegmentOrthogonal));
            origin = origin.add(new Vector2(MinWidth, 0).rotate(vecSegmentOrthogonal));

        } else {//convexe
            //si le coinBasGauche est situé à l'extérieur de la section, on doit soustraire, et sinon ajouter
            origin = coinBasGauche.add(new Vector2(CraftConfig.bitWidth / 2, 0).rotate(vecSegmentOrthogonal));
            origin = origin.sub(new Vector2(MARGIN_EXT, 0).rotate(vecSegmentOrthogonal));
            if (coinBasGauche.sub(startPoint).dot(vecSegmentOrthogonal) > 0) {
                origin = origin.sub(new Vector2(Vector2.dist(origin, coinBasGauche), 0).rotate(vecSegmentOrthogonal));
            }
        }

        origin = origin.sub(new Vector2(0, CraftConfig.lengthFull / 2).rotate(vecSegmentOrthogonal));
        return new Bit2D(origin, vecSegment);
    }

    static boolean isConvex(List<Vector2> pts) {
        boolean sign = false;
        int n = pts.size();

        for (int i = 0; i < n; i++) {
            double distX1 = pts.get((i + 2) % n).x - pts.get((i + 1) % n).x;
            double distY1 = pts.get((i + 2) % n).y - pts.get((i + 1) % n).y;
            double distX2 = pts.get(i).x - pts.get((i + 1) % n).x;
            double distY2 = pts.get(i).y - pts.get((i + 1) % n).y;
            double zCrossProduct = distX1 * distY2 - distY1 * distX2;

            //on regarde si le signe est le même pour tous les points avec une marge de précision
            if (i == 0) sign = zCrossProduct > CONVEX_ERROR;
            else if (sign != (zCrossProduct > CONVEX_ERROR)) return false;
        }
        return true;
    }

    private Vector2 getProjStartPoint(Vector2 startPoint, Segment2D segment) {
        Vector2 distance = segment.start.sub(startPoint);
        Vector2 orthogonal = segment.getNormal();
        return startPoint.add(orthogonal.mul(distance.dot(orthogonal))); //le projeté orthogonal
    }//todo traduire tout en anglais

    private int getNumberOfIntersection(Segment2D segmentToTest, List<Vector2> sectionPoints) {
        int nbIntersections = 0;
        Vector<Segment2D> segments = GeneralTools.pointsToSegments(sectionPoints);
        for (Segment2D segment2D : segments) {
            if (Segment2D.doSegmentsIntersect(segmentToTest, segment2D)) {// && segment2D.end != Segment2D.getIntersectionPoint(segmentToTest, segment2D)) {
                nbIntersections++;
            }
        }
        return nbIntersections;
    }


    //todo duplicated code
    private boolean listContainsAsGoodAsEqual(Vector2 point, List<Vector2> points) {
        for (Vector2 p : points) {
            if (point.asGoodAsEqual(p)) {
                return true;
            }
        }
        return false;
    }

    //todo duplicated code
    private boolean listContainsAllAsGoodAsEqual(Vector<Vector2> containedList, List<Vector2> containerList) {
        for (Vector2 p : containedList) {
            if (!listContainsAsGoodAsEqual(p, containerList)) {
                return false;
            }
        }
        return true;
    }
}
