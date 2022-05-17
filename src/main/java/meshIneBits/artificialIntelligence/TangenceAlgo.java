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
import meshIneBits.artificialIntelligence.debug.DebugTools;
import meshIneBits.artificialIntelligence.util.SectionTransformer;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.NoninvertibleTransformException;
import java.util.List;
import java.util.Vector;

public class TangenceAlgo {

    private static final int CONVEX_ERROR = -4;
    private static final int CONCAVE_ERROR = 0;
    private final double MARGIN_EXT = 1;

    public Vector<Bit2D> getBits(Slice slice, double minWidth, double numberMaxBits) throws NoninvertibleTransformException {
        Vector<Bit2D> bits = new Vector<>();

        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);
        for (Vector<Vector2> bound : bounds) {
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 nextStartPoint = bound.get(0);

            System.out.println("++++++++++++++ BOUND " + bounds.indexOf(bound) + " ++++++++++++++++");

            List<Vector2> sectionPoints;
            int iBit = 0;//TODO DebugOnly
            do {
                System.out.println("PLACEMENT BIT " + iBit + "====================");
                System.out.println("NEXT START POINT : " + nextStartPoint);
                sectionPoints = SectionTransformer.getSectionPointsFromBound(bound, nextStartPoint);


                int convexType = 1000;
                //we first want to know if the beginning of the section is convex or concave
                //we then want to find the max convex or concave section
                List<Vector2> maxConvexSection = new Vector<>();
                List<Vector2> maxConcaveSection = new Vector<>();
                List<Vector2> maxLineSection = new Vector<>();

                Vector2 ORIGIN = new Vector2(0, 0);
                sectionPoints.add(ORIGIN);

                // pour savoir si on commence par une section convexe ou concave, on regarde tous les points
                // qui sont à une distance de BitLength/2 par exemple
                int nbPointsToCheck=0;
                for (Vector2 point : sectionPoints) {
                    nbPointsToCheck++;
                    if (Vector2.dist(point, nextStartPoint) >= CraftConfig.lengthNormal / 2) {
                        break;
                    }
                }
//                if (isConcave(sectionPoints.subList(0, 4))) {
//                    System.out.println("3 PREMIERS CONCAVE");
//                    //section concave
//                    convexType = -1;
//                    List<Vector2> concaveSection = new Vector<>();
//                    concaveSection.add(sectionPoints.get(0));
//                    concaveSection.add(sectionPoints.get(1));
//                    concaveSection.add(sectionPoints.get(2));
//                    concaveSection.add(sectionPoints.get(3));
//                    concaveSection.add(ORIGIN);
//
//                    //on agrandit concaveSection tant que la section est concave
//                    int i = 3;
//                    do {
//                        concaveSection.add(concaveSection.size() - 1, sectionPoints.get(i));
//                        i++;
//                    } while (i < sectionPoints.size() && isConcave(concaveSection));
//                    maxConcaveSection = concaveSection;
//                    maxConcaveSection.remove(ORIGIN);
//                    System.out.println("CONCAVE SECTION ");
//
//                }
                if (isConvex(sectionPoints.subList(0, nbPointsToCheck))) {
                    //section convexe
                    convexType = 1;
                    System.out.println("a l'air d'être convexe");

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
//                        System.out.println("isConvex(" + convexSection + ") = " + isConvex(convexSection));
                    } while (i < sectionPoints.size() && isConvex(convexSection));
                    maxConvexSection = convexSection;
                    maxConvexSection.remove(ORIGIN);
                    System.out.println("CONVEX SECTION ");
                }
                if (true) {
//                    sectionPoints.remove(ORIGIN);
//                    if (sectionPoints.size() < 3) {
//                        //on a forcément une ligne
//                        continue;
//                    }
//                    //ligne droite
//                    convexType = 0;
//                    Vector<Vector2> lineSection = new Vector<>();
//                    //on agrandit lineSection tant que la section est une ligne droite
//                    int i = 0;
//                    do {
//                        lineSection.add(sectionPoints.get(i));
//                        i++;
//                    } while (i < sectionPoints.size() && isLine(lineSection));
//                    maxLineSection = lineSection;
//                    System.out.println("LINE SECTION : " + sectionPoints);
                }
                System.out.println("maxConvexSection = " + maxConvexSection.size());
                System.out.println("maxConcaveSection = " + maxConcaveSection.size());
                System.out.println("maxLineSection = " + maxLineSection.size());

                if (maxConvexSection.size() > maxConcaveSection.size() && maxConvexSection.size() > maxLineSection.size()) {
                    //on a trouvé une section convexe
                    System.out.println("FINAL CONVEX SECTION " + maxConvexSection);
                    sectionPoints = maxConvexSection;
                    convexType = 1;
                }
//                if (maxConcaveSection.size() > maxConvexSection.size() && maxConcaveSection.size() > maxLineSection.size()) {
                else {
                    //on a trouvé une section concave
                    System.out.println("FINAL CONCAVE SECTION " + maxConcaveSection);
//                    sectionPoints = maxConcaveSection;
                    convexType = -1;
                    sectionPoints.remove(ORIGIN);
                }
//               if (maxLineSection.size() > maxConcaveSection.size() && maxLineSection.size() > maxConvexSection.size()) {
//                    //on a trouvé une section de ligne
//                    System.out.println("FINAL LINE SECTION " + maxLineSection);
//                    sectionPoints = maxLineSection;
//                    convexType = 0;
//               }


                sectionPoints.remove(ORIGIN);
                System.out.println("CONVEX TYPE : " + convexType);
                System.out.println("sectionPoints = " + sectionPoints);
                DebugTools.setPaintForDebug(true);
                DebugTools.segmentsToDrawBlue.clear();
                DebugTools.segmentsToDrawBlue.addAll(GeneralTools.pointsToSegments(sectionPoints));

                Bit2D bit = getBitFromSectionWithTangence(sectionPoints, nextStartPoint, minWidth, convexType);
//                Bit2D bit = null;
                if (bit != null) {
                    bits.add(bit);

                    nextStartPoint = GeneralTools.getBitAndContourSecondIntersectionPoint(bit, bound, false, nextStartPoint);//TODO DEBUG si c'est une ligne droite
                    System.out.println("nextStartPoint = " + nextStartPoint);
                    DebugTools.pointsToDrawORANGE.add(nextStartPoint);
                    System.out.println("FIN PLACEMENT BIT " + iBit + "====================");
                } else {
                    System.out.println("BIT " + iBit + " NON PLACÉ");
                }
                iBit++;


            } while (!((listContainsAsGoodAsEqual(veryFirstStartPoint, sectionPoints) && iBit > 1) || listContainsAllAsGoodAsEqual(bound, sectionPoints)) && iBit < numberMaxBits);//todo max bits
            //while (!listContainsAsGoodAsEqual(veryFirstStartPoint, placement.sectionCovered.subList(1, placement.sectionCovered.size())) && iBit<40); //Add each bit on the bound


        }


        return bits;
    }

    private Vector<Vector2> FindMaxSectionConvexOrConcave(Vector<Vector2> sectionPoints) {
        //we first want to know if the beginning of the section is convex or concave
        //we then want to find the max convex or concave section
        if (isConvex((Vector<Vector2>) sectionPoints.subList(0, 3))) {
            //section convexe
            Vector<Vector2> convexSection = new Vector<>();
            //on agrandit convexSection tant que la section est convexe
            int i = 0;
            while (isConvex((Vector<Vector2>) sectionPoints.subList(i, i + 3))) {
                convexSection.add(sectionPoints.get(i));
                i++;
            }
            return convexSection;
        } else if (isConcave((Vector<Vector2>) sectionPoints.subList(0, 3))) {
            //section concave
            Vector<Vector2> concaveSection = new Vector<>();
            //on agrandit concaveSection tant que la section est concave
            int i = 0;
            while (isConcave((Vector<Vector2>) sectionPoints.subList(i, i + 3))) {
                concaveSection.add(sectionPoints.get(i));
                i++;
            }
            return concaveSection;
        } else {
            //ligne droite
            Vector<Vector2> lineSection = new Vector<>();
            //on agrandit lineSection tant que la section est une ligne droite
            int i = 0;
            while (isLine((Vector<Vector2>) sectionPoints.subList(i, i + 3))) {
                lineSection.add(sectionPoints.get(i));
                i++;
            }
            return lineSection;
        }
    }

    public Bit2D getBitFromSectionWithTangence(List<Vector2> sectionPoints, Vector2 startPoint, double MinWidth, int convexType) {
        DebugTools.setPaintForDebug(true);
//        DebugTools.segmentsToDrawBlue.clear();
        DebugTools.pointsToDrawBLUE.clear();
        DebugTools.pointsToDrawBLACK.clear();
        DebugTools.pointsToDrawGREEN.clear();

        Vector<Segment2D> segmentsSection = GeneralTools.pointsToSegments(sectionPoints);
        Segment2D lastSegmentPossible = null;

        for (Segment2D segment : segmentsSection) {
            if (segment.start==segment.end) {
                System.out.println(segmentsSection);
                throw new RuntimeException("segment.start==segment.end");
            }
        }

        for (Segment2D segment : segmentsSection) {
            //si jamais la longueur du segment est très petite (possible des fois), on passe ce segment.
            if (segment.getLength() < 3) continue;//todo mettre une val au début

            //pour chaque segment, on calcule la distance entre le début de segment et le startPoint.
            //on projette cette distance vers le vecteur orthogonal au segment.
            //on l'ajoute au startPoint. et ca nous donne le coinHautGauche du bit.
            Vector2 coinBasGauche = getProjStartPoint(startPoint, segment);
            //si la distance entre le point projeté (coinHautGauche) et le startPoint est inférieure à la largeur du bit, on a un bit possible
            //il faut également que l'on soit du bon côté de la section.
            // todo on peut calculer la distance entre chaque segment et le segment que l'on vient de calculer, et il faut que la distance soit supérieure à celle la
//            double distAvecTousLesSegments = getDistWithMaxAllSegments(segment, segmentsSection, coinBasGauche, startPoint);
//            System.out.println("dist = " + Vector2.dist(coinBasGauche, startPoint));
//            System.out.println("distAvecTousLesSegments = " + distAvecTousLesSegments);
//            boolean aucunPointDuMauvaisCote = verifyNoPointOnBadSide(segment, segmentsSection);
            if (convexType == 1 && Vector2.dist(coinBasGauche, startPoint) < CraftConfig.bitWidth + MARGIN_EXT - MinWidth
                    || convexType == -1 && Vector2.dist(coinBasGauche, startPoint) < CraftConfig.bitWidth + MARGIN_EXT - MinWidth) {
//                if (distAvecTousLesSegments > 0) {
                Segment2D segmentDecale = new Segment2D(
                        segment.start.add(segment.getNormal().normal().mul(MARGIN_EXT)).sub(segment.getDirectionalVector().mul(400)),
                        segment.end.add(segment.getNormal().normal().mul(MARGIN_EXT)).add(segment.getDirectionalVector().mul(400)));
                if (getNumberOfIntersection(segmentDecale, sectionPoints) == 0 && convexType == 1) {//todo faire mieux pour ne pas utiliser les intersections
                    lastSegmentPossible = segment;
                } else if (getNumberOfIntersection(segmentDecale, sectionPoints) <= 2 && convexType == -1) {
                    lastSegmentPossible = segment;
                }
            }
        }

        if (lastSegmentPossible==null)
            lastSegmentPossible=segmentsSection.lastElement();//todo marche temporairement
        if (lastSegmentPossible.start==lastSegmentPossible.end) //todo debugonly
            throw new RuntimeException("lastSegmentPossible.start==lastSegmentPossible.end");

        Vector2 coinBasGauche = getProjStartPoint(startPoint, lastSegmentPossible);

        DebugTools.pointsToDrawORANGE.add(startPoint);
        DebugTools.pointsToDrawGREEN.add(coinBasGauche);
//        DebugTools.currentSegToDraw = lastSegmentPossible;
//        DebugTools.currentSegToDraw2 = new Segment2D(startPoint, coinBasGauche);
//        DebugTools.segmentsToDrawBlue.add(new Segment2D(lastSegmentPossible.start, coinBasGauche));

        //on calcule le centre du bit, situé à une distance de CraftConfig.bitWidth/2 et CraftConfig.lengthNormal/2
        // de coinHautGauche, dans les bonnes directions.
        Vector2 vecSegment = lastSegmentPossible.getDirectionalVector().normal();
        Vector2 vecSegmentOrthogonal = vecSegment.getCWAngularRotated().normal();
        Vector2 origin;
        DebugTools.currentSegToDraw = lastSegmentPossible;

        if (convexType == -1) {//concave
            System.out.println("PLACEMENT CONCAVE");
            double distAvecTousLesSegments = getDistWithMaxAllSegments(lastSegmentPossible, segmentsSection, coinBasGauche, startPoint);
            //si le coinBasGauche est situé à l'extérieur de la section, on doit soustraire, et sinon ajouter
            System.out.println("\tcoinBasGauche.sub(startPoint).dot(vecSegmentOrthogonal) = " + coinBasGauche.sub(startPoint).dot(vecSegmentOrthogonal));
            if (coinBasGauche.sub(startPoint).dot(vecSegmentOrthogonal) <= 0) {
                System.out.println("\tCAS <= 0");
                origin = coinBasGauche.sub(new Vector2(CraftConfig.bitWidth/2, 0).rotate(vecSegmentOrthogonal));//todo tout mettre en un
//                origin = origin.add(new Vector2(distAvecTousLesSegments, 0).rotate(vecSegmentOrthogonal));
                origin = origin.add(new Vector2(MinWidth, 0).rotate(vecSegmentOrthogonal));
            } else {
                System.out.println("\tCAS > 0");// le coinBasGauche est à l'intérieur
                origin = coinBasGauche.sub(new Vector2(CraftConfig.bitWidth/2, 0).rotate(vecSegmentOrthogonal));//todo tout mettre en un
                origin = origin.add(new Vector2(MinWidth, 0).rotate(vecSegmentOrthogonal));
            }
//            origin = origin.add(new Vector2(MARGIN_EXT, 0).rotate(vecSegmentOrthogonal));

        } else {//convexe
            System.out.println("PLACEMENT CONVEXE");
            //si le coinBasGauche est situé à l'extérieur de la section, on doit soustraire, et sinon ajouter
            System.out.println("\tcoinBasGauche.sub(startPoint).dot(vecSegmentOrthogonal) = " + coinBasGauche.sub(startPoint).dot(vecSegmentOrthogonal));
            if (coinBasGauche.sub(startPoint).dot(vecSegmentOrthogonal) <= 0) {
                System.out.println("\tCAS <= 0");
                System.out.println("\t" + coinBasGauche + vecSegmentOrthogonal + vecSegment + lastSegmentPossible + lastSegmentPossible.getDirectionalVector() + lastSegmentPossible.getNormal());
                origin = coinBasGauche.add(new Vector2(CraftConfig.bitWidth / 2, 0).rotate(vecSegmentOrthogonal));
                origin = origin.sub(new Vector2(MARGIN_EXT, 0).rotate(vecSegmentOrthogonal));
            }
            else {
                System.out.println("\tCAS > 0");// le coinBasGauche est à l'intérieur
                origin = coinBasGauche.add(new Vector2(CraftConfig.bitWidth / 2, 0).rotate(vecSegmentOrthogonal));
                origin = origin.sub(new Vector2(MARGIN_EXT, 0).rotate(vecSegmentOrthogonal));
                origin = origin.sub(new Vector2(Vector2.dist(origin, coinBasGauche), 0).rotate(vecSegmentOrthogonal));
            }
        }

        origin = origin.sub(new Vector2(0, CraftConfig.lengthFull / 2).rotate(vecSegmentOrthogonal));
        Bit2D bit = new Bit2D(origin, vecSegment);
        System.out.println("BIT FINAL :"+bit);
        DebugTools.segmentsToDrawBlue.addAll(bit.getBitSidesSegments());
        DebugTools.pointsToDrawBLACK.add(bit.getOrigin());
        return bit;
    }

    static boolean isConvex(List<Vector2> pts) {
        System.out.println("TEST isConvex");
        if (pts.size() < 4) {
            System.out.println("\tisConvex : pts.size() < 4");
//            return true;
        }

        boolean sign = false;
        int n = pts.size();

        for (int i = 0; i < n; i++) {
            double dx1 = pts.get((i + 2) % n).x - pts.get((i + 1) % n).x;
            double dy1 = pts.get((i + 2) % n).y - pts.get((i + 1) % n).y;
            double dx2 = pts.get(i).x - pts.get((i + 1) % n).x;
            double dy2 = pts.get(i).y - pts.get((i + 1) % n).y;
            double zCrossProduct = dx1 * dy2 - dy1 * dx2;
            System.out.println("\tzCrossProduct : " + zCrossProduct + sign);

            //on regarde si le signe est le même pour tous les points avec une marge de précision
            if (i == 0) sign = zCrossProduct > CONVEX_ERROR;
            else if (sign != (zCrossProduct > CONVEX_ERROR)) return false;
        }

        return true;
    }

    static boolean isConcave(List<Vector2> pts) {
        System.out.println("TEST isConcave");
        if (pts.size() < 4) {
            System.out.println("\tisConcave : pts.size() < 4");
//            return false;
        }
        boolean sign = false;
        int n = pts.size();

        for (int i = 0; i < n; i++) {
            double dx1 = pts.get((i + 2) % n).x - pts.get((i + 1) % n).x;
            double dy1 = pts.get((i + 2) % n).y - pts.get((i + 1) % n).y;
            double dx2 = pts.get(i).x - pts.get((i + 1) % n).x;
            double dy2 = pts.get(i).y - pts.get((i + 1) % n).y;
            double zCrossProduct = dx1 * dy2 - dy1 * dx2;
            System.out.println("\tzCrossProduct : " + zCrossProduct + sign);

            if (i == 0) sign = zCrossProduct > CONCAVE_ERROR;
            else if (sign != (zCrossProduct > CONCAVE_ERROR)) {
                System.out.println("\tisConcave : true");
                return true;
            }
        }

        return false;
    }

    static boolean isLine(Vector<Vector2> section) {
        // on vérifie que tous les points sont alignés
        for (int i = 1; i < section.size() - 1; i++) {//todo peut etre aussi introduire une erreur acceptée
            if (!section.get(i).isOnSegment(new Segment2D(section.get(i - 1), section.get(i + 1)))) return false;
        }
        return true;
    }

    private boolean verifyNoPointOnBadSide(Segment2D current, Vector<Segment2D> segmentsSection) {
        for (Segment2D segment : segmentsSection) {
            if (segment == current) continue;
            if (Vector2.Tools.checkOnDifferentSides(segment.getMidPoint().add(segment.getNormal()), current.getMidPoint().add(current.getNormal()), segment)) {
                return false;
            }
        }
        return true;
    }

    private double getDistWithMaxAllSegments(Segment2D segment, Vector<Segment2D> segmentsSection, Vector2 coinBasGauche, Vector2 startPoint) {
        //on agrandit le segment pour les intersections //todo enlever ?
        segment = new Segment2D(segment.start.sub(segment.getDirectionalVector().mul(300)), segment.end.add(segment.getDirectionalVector().mul(300)));
//        DebugTools.segmentsToDrawBlue.add(segment);
        double dist = 0;
        Segment2D bestSegment = null;
        Vector2 bestIntersection = null;

        //on calcule le maximum de la distance entre le segment et les autres segments (distance orthogonale)
        for (Segment2D currentSeg : segmentsSection) {
            //on calcule le segmentnormal du segment courant et du currentSeg
            Segment2D currentSegNormal = new Segment2D(currentSeg.start.add(segment.getNormal().mul(100)), currentSeg.start.add(segment.getNormal().mul(-100)));
            //on cherche l'intersection entre les deux segments
            Vector2 intersection = segment.intersect(currentSegNormal);
            //si il y a intersection, on calcule la distance entre le segment et l'intersection
            if (intersection != null) {//si null, alors ils sont parallèles
                double distTemp = Vector2.dist(currentSeg.start, intersection);
//                System.out.println("distTemp = " + distTemp);
                if (distTemp >= dist) {
//                    DebugTools.pointsToDrawRED.add(intersection);
//                    DebugTools.segmentsToDrawBlue.add(new Segment2D(currentSeg.getMidPoint(), intersection));
                    dist = distTemp;
                    bestSegment = currentSegNormal;
                    bestIntersection = intersection;
                }
            }

            DebugTools.pointsToDrawRED.add(bestIntersection);
//            DebugTools.segmentsToDrawBlue.add(bestSegment);
//            if (segment.start.sub(currentSeg.start).dot(currentSeg.getNormal())> dist) {
//                System.out.println("distSeg = " + dist);
//                DebugTools.segmentsToDrawBlue.add(new Segment2D(currentSeg.getMidPoint(), currentSeg.getMidPoint().add(currentSeg.getNormal().mul(-segment.start.sub(currentSeg.start).dot(currentSeg.getNormal())))));
//            }
//            dist = Math.max(dist, segment.start.sub(currentSeg.start).dot(currentSeg.getNormal()));
        }

        //on doit retourner la distance dans le sens du vecteur du segment vers le currentSegment
//        double dir = segment.getNormal().dot(segment.getMidPoint().sub(startPoint));
////        double dir = coinBasGauche.sub(startPoint).dot(bestSegment.getDirectionalVector());
//        if (dir > 0) {
//            System.out.println("dir = " + dir);
//            return dist;
//        }
//        System.out.println("dir = " + dir);
//        return -dist;
        DebugTools.segmentsToDrawBlue.add(segment);
        DebugTools.segmentsToDrawBlue.add(new Segment2D(bestIntersection,bestIntersection.add(segment.getNormal().mul(dist))));
//        if (Vector2.Tools.checkOnDifferentSides(segment.getMidPoint().add(segment.getNormal()), bestSegment.getMidPoint(), segment)) {
//            return -dist;
//        }
        return dist;
    }

    private Vector2 getProjStartPoint(Vector2 startPoint, Segment2D segment) {
//        Vector2 vecSegment = segment.getDirectionalVector();
//        Vector2 vecSegmentOrthogonal = vecSegment.getCWAngularRotated().getOpposite();
//        Vector2 vecStartPoint = startPoint.sub(segment.start);
//        double dist = vecStartPoint.dot(vecSegmentOrthogonal);
//        return segment.start.add(vecSegmentOrthogonal.mul(dist));


        Vector2 distance = segment.start.sub(startPoint);
        Vector2 orthogonal = segment.getNormal();
//        DebugTools.segmentsToDrawBlue.add(new Segment2D(segment.getMidPoint(), segment.getMidPoint().add(orthogonal.mul(10))));
        Vector2 proj = orthogonal.mul(distance.dot(orthogonal));
        return startPoint.add(proj);
    }

    public Bit2D getBitFromSectionWithTangence2(Vector<Vector2> sectionPoints, Vector2 startPoint) {
        DebugTools.setPaintForDebug(true);
        DebugTools.segmentsToDrawBlue.clear();
        DebugTools.pointsToDrawBLUE.clear();
        DebugTools.pointsToDrawRED.clear();
        DebugTools.pointsToDrawGREEN.clear();

        Vector<Segment2D> segmentsSection = GeneralTools.pointsToSegments(sectionPoints);
        Segment2D lastSegmentPossible = null;
        Segment2D segmentExterieur = null;
        Segment2D segmentInterieur = null;

        //pour chaque segment de la section, on colle le segment long extérieur du bit au segment et on regarde comme dans la V1
        for (Segment2D segment : segmentsSection) {
            System.out.println("\tsegment = " + segment);
            Vector2 vecSegmentActuel = segment.getDirectionalVector();
            Vector2 vecSegmentActuelOrtho = vecSegmentActuel.getCWAngularRotated();
            segmentExterieur = new Segment2D(segment.start.add(vecSegmentActuel.getCWAngularRotated().normal().mul(30).add(vecSegmentActuel.normal().mul(1000))), segment.end.add(vecSegmentActuel.getCWAngularRotated().normal().mul(30)).add(vecSegmentActuel.normal().mul(-1000)));//agrandissement du segment
//            segmentExterieur = new Segment2D(
//                    segment.start.add(vecSegmentActuel.normal().mul(1000)).sub(new Vector2(MARGIN, MARGIN).rotate(vecSegmentActuelOrtho.normal())),
//                    segment.end.add(vecSegmentActuel.normal().mul(-1000)).sub(new Vector2(MARGIN, MARGIN).rotate(vecSegmentActuelOrtho.normal())));//agrandissement du segment
//            segmentInterieur = new Segment2D(
//                    segmentExterieur.start.add(vecSegmentActuel.getCWAngularRotated().normal().mul(CraftConfig.bitWidth)),
//                    segmentExterieur.end.add(vecSegmentActuel.getCWAngularRotated().normal().mul(CraftConfig.bitWidth)));

            //on refait les calculs pour le lastSegmentPossible
            Bit2D bit = getBitWithTangents(sectionPoints, startPoint, segmentExterieur, vecSegmentActuelOrtho);
            Segment2D coteBit = bit.getBitSidesSegments().get(3); // le coté court en haut du bit
//            DebugTools.segmentsToDrawBlue.clear();
            DebugTools.segmentsToDrawBlue.add(coteBit);
            DebugTools.segmentsToDrawBlue.add(segment);
            DebugTools.segmentsToDrawBlue.addAll(bit.getBitSidesSegments());

            if (startPoint.isOnSegment(coteBit)) {// || Segment2D.doSegmentsIntersect(coteBit, segmentsSection.firstElement())) {
                System.out.println("\t\tstartPoint is on coteBit=" + startPoint.isOnSegment(coteBit) + " or doSegmentsIntersect=" + Segment2D.doSegmentsIntersect(coteBit, segmentsSection.firstElement()));
                int nbIntersectionsExterieur = getNumberOfIntersection(segmentExterieur, sectionPoints);//TODO FAUX ? RESTE UNE ERREUR ICI BIT6 SLICE1
                //int nbIntersectionsInterieur = getNumberOfIntersection(segmentInterieur, sectionPoints
//            System.out.println("\t\tnbIntersectionsInterieur = " + nbIntersectionsInterieur);
//            System.out.println("\t\tnbIntersectionsExterieur = " + nbIntersectionsExterieur);
                if (nbIntersectionsExterieur <= 1) {
                    lastSegmentPossible = segment;
                    System.out.println("\t\t\tlastSegmentPossible = " + lastSegmentPossible);
                }
            }
        }
        System.out.println();
        System.out.println("segmentInterieur = " + segmentInterieur);
        System.out.println("segmentExterieur = " + segmentExterieur);
        System.out.println("lastSegmentPossible = " + lastSegmentPossible);
        System.out.println("inters: " + getNumberOfIntersection(segmentExterieur, sectionPoints));
        DebugTools.currentSegToDraw = segmentExterieur;

        if (lastSegmentPossible == null) {
            throw new IllegalStateException("lastSegmentPossible == null");
//            lastSegmentPossible=segmentsSection.firstElement();
        }

        //on refait les calculs pour le lastSegmentPossible
        Vector2 vecSegmentActuel = lastSegmentPossible.getDirectionalVector();
        Vector2 vecSegmentActuelOrtho = vecSegmentActuel.getCWAngularRotated();
//        segmentExterieur = new Segment2D(
//                lastSegmentPossible.start.add(vecSegmentActuel.normal().mul(1000).add(new Vector2(0, MARGIN))),
//                lastSegmentPossible.end.add(vecSegmentActuel.normal().mul(-1000).add(new Vector2(0, MARGIN))));//agrandissement du segment
//        segmentInterieur = new Segment2D(segmentExterieur.start.add(vecSegmentActuel.getCWAngularRotated().normal().mul(CraftConfig.bitWidth)), segmentExterieur.end.add(vecSegmentActuel.getCWAngularRotated().normal().mul(CraftConfig.bitWidth)));
//        DebugTools.currentSegToDraw = segmentExterieur;
//
//        Segment2D segmentStartPointVersCoinBasGauche = new Segment2D(startPoint.sub(vecSegmentActuelOrtho.mul(100)), startPoint.add(vecSegmentActuelOrtho.mul(100)));//todo améliorer
//        Vector2 coinBasGauche = segmentStartPointVersCoinBasGauche.intersect(segmentExterieur);
////        Vector2 origin = coinBasGauche.sub(new Vector2(CraftConfig.lengthNormal / 2, CraftConfig.bitWidth / 2).rotate(vecSegmentActuelOrtho.normal().getCWAngularRotated()));//todo bizarre
//        Vector2 origin = coinBasGauche.add(new Vector2(CraftConfig.bitWidth / 2, 0).rotate(vecSegmentActuelOrtho.normal()));
//        origin = origin.sub(new Vector2(0, CraftConfig.lengthFull / 2).rotate(vecSegmentActuelOrtho.normal()));
//        origin = origin.sub(new Vector2(MARGIN, 0).rotate(vecSegmentActuelOrtho.normal()));//test
//
////        DebugTools.currentSegToDraw = segmentInterieur;
////        DebugTools.currentSegToDraw2 = segmentExterieur;
////        DebugTools.segmentsToDrawBlue.add(segmentStartPointVersCoinBasGauche);
//        DebugTools.pointsToDrawRED.addAll(sectionPoints);
//
//        Bit2D bit = new Bit2D(origin, vecSegmentActuelOrtho.normal().getCWAngularRotated().getOpposite());
        Bit2D bit = getBitWithTangents(sectionPoints, startPoint, segmentExterieur, vecSegmentActuelOrtho);
        DebugTools.segmentsToDrawBlue.add(bit.getBitSidesSegments().get(3));
        return bit;
    }

    @NotNull
    private Bit2D getBitWithTangents(Vector<Vector2> sectionPoints, Vector2 startPoint, Segment2D segmentExterieur, Vector2 vecSegmentActuelOrtho) {
        Segment2D segmentStartPointVersCoinBasGauche = new Segment2D(startPoint.sub(vecSegmentActuelOrtho.mul(100)), startPoint.add(vecSegmentActuelOrtho.mul(100)));//todo améliorer
        Vector2 coinBasGauche = segmentStartPointVersCoinBasGauche.intersect(segmentExterieur);
//            Vector2 origin = coinBasGauche.add(
//                    new Vector2(CraftConfig.lengthNormal / 2, CraftConfig.bitWidth / 2));
//                            .rotate(vecSegmentActuel.normal()));//todo bizarre
//            origin = origin.add(new Vector2(MARGIN, 0));//test
        Vector2 origin = coinBasGauche.add(new Vector2(CraftConfig.bitWidth / 2, 0).rotate(vecSegmentActuelOrtho.normal()));
        origin = origin.sub(new Vector2(0, CraftConfig.lengthNormal / 2).rotate(vecSegmentActuelOrtho.normal()));
        origin = origin.sub(new Vector2(MARGIN_EXT, 0).rotate(vecSegmentActuelOrtho.normal()));

//            DebugTools.pointsToDrawGREEN.add(origin);
        DebugTools.currentSegToDraw2 = segmentStartPointVersCoinBasGauche;
        DebugTools.pointsToDrawRED.addAll(sectionPoints);

        Bit2D bit = new Bit2D(origin, vecSegmentActuelOrtho.normal().getCWAngularRotated());
        return bit;
    }

    public Bit2D getBitFromSectionWithTangence1(Vector<Vector2> sectionPoints, Vector2 startPoint) {
        DebugTools.setPaintForDebug(true);
        DebugTools.segmentsToDrawBlue.clear();
        DebugTools.segmentsToDrawRed.clear();
        DebugTools.pointsToDrawBLUE.clear();
        DebugTools.pointsToDrawRED.clear();
        DebugTools.pointsToDrawGREEN.clear();
        DebugTools.pointsToDrawORANGE.clear();

        //todo il faudra peut-etre repeupler

        // on calcule le centre
        //pour chaque point on calcule le vecteur/segment vert                          : SegmentCentreVersPoint
        //puis le vecteur/segment orthogonal                                            : SegmentCentreVersPointOrtho
        //on vérifie si le segment ortho intersecte 0 ou 1 fois avec la section
        //on vérifie si le segment ortho du bas intersecte 0 ou 1 fois avec la section
        //Si oui, ce point devient le dernier point possible
        //fin pour
        //
        //On place par rapport au dernier point

        //calcul du barycentre de la section des points
        Vector2 centre = new Vector2(0, 0);
//        for (Vector2 point : sectionPoints) {
//            centre = centre.add(point);
//        }
//        centre = centre.div(sectionPoints.size());
        //finalement le centre est calculé en prenant l'inter de deux segments partant du début et la fin de la section
        // ces deux segments sont orthogonaux à la section à l'endroit d'ou ils partent
        Vector2 vecSectionFirstVersSecond = sectionPoints.get(1).sub(sectionPoints.get(0));
        Vector2 vecSectionLastVersBeforeLast = sectionPoints.get(sectionPoints.size() - 1).sub(sectionPoints.get(sectionPoints.size() - 2));
        Vector2 vecPremierPointversCentre = sectionPoints.firstElement().add(vecSectionFirstVersSecond.getCWAngularRotated().normal().mul(1000));//todo améliorer
        Vector2 vecDernierPointversCentre = sectionPoints.lastElement().add(vecSectionLastVersBeforeLast.getCWAngularRotated().normal().mul(1000));//todo améliorer

        Segment2D segment1 = new Segment2D(sectionPoints.firstElement(), vecPremierPointversCentre);
        Segment2D segment2 = new Segment2D(sectionPoints.lastElement(), vecDernierPointversCentre);
        centre = Segment2D.getIntersectionPoint(segment1, segment2);
        DebugTools.segmentsToDrawRed.add(segment1);
        DebugTools.segmentsToDrawRed.add(segment2);
        DebugTools.pointsToDrawRED.add(centre);

        if (centre == null) {
            throw new RuntimeException("centre null");
        }


        Vector2 vecteurCentreVersPoint;
        Vector2 vecteurCentreVersPointOrtho;
        Vector2 lastPointPossible = null;
        Segment2D segmentCentreVersPoint;
        Segment2D segmentCentreVersPointOrtho;
        Segment2D segmentCentreVersPointOrthoDuBas;

        for (Vector2 point : sectionPoints) {
            vecteurCentreVersPoint = point.sub(centre);
            vecteurCentreVersPointOrtho = vecteurCentreVersPoint.getCWAngularRotated();
            segmentCentreVersPoint = new Segment2D(centre, centre.add(vecteurCentreVersPoint.mul(10)));//pour assurer l'intersection
            segmentCentreVersPointOrtho = new Segment2D(point.sub(vecteurCentreVersPointOrtho.mul(100)), point.add(vecteurCentreVersPointOrtho.mul(100)));//pour assurer l'intersection todo améliorer
            segmentCentreVersPointOrthoDuBas = new Segment2D(point.sub(vecteurCentreVersPoint.normal().mul(CraftConfig.bitWidth / 2)).sub(vecteurCentreVersPointOrtho.mul(100)), point.sub(vecteurCentreVersPoint.normal().mul(CraftConfig.bitWidth / 2)).add(vecteurCentreVersPointOrtho.mul(100)));// x100 pour assurer l'intersection todo améliorer
            DebugTools.segmentsToDrawBlue.add(segmentCentreVersPointOrtho);
            DebugTools.segmentsToDrawBlue.add(segmentCentreVersPointOrthoDuBas);
            int nbIntersectionsAvecVecteur = getNumberOfIntersection(segmentCentreVersPointOrtho, sectionPoints);//TODO FAUX
            int nbIntersectionsAvecVecteurOrtho = getNumberOfIntersection(segmentCentreVersPointOrthoDuBas, sectionPoints);//TODO FAUX
            System.out.println("nbIntersectionsAvecVecteurOrtho = " + nbIntersectionsAvecVecteurOrtho);
            System.out.println("nbIntersectionsAvecVecteur = " + nbIntersectionsAvecVecteur);
            if (nbIntersectionsAvecVecteur <= 1 && nbIntersectionsAvecVecteurOrtho <= 1) {
                lastPointPossible = point;
            }
        }

        //on refait les calculs pour le lastPointPossible
        vecteurCentreVersPoint = lastPointPossible.sub(centre);
        vecteurCentreVersPointOrtho = vecteurCentreVersPoint.getCWAngularRotated();
        segmentCentreVersPoint = new Segment2D(centre, centre.add(vecteurCentreVersPoint.mul(10)));//pour assurer l'intersection
        segmentCentreVersPointOrtho = new Segment2D(lastPointPossible.sub(vecteurCentreVersPointOrtho.mul(100)), lastPointPossible.add(vecteurCentreVersPointOrtho.mul(100)));//pour assurer l'intersection todo améliorer


        Segment2D segmentStartPointVersCoinBasGauche = new Segment2D(startPoint.sub(vecteurCentreVersPoint.mul(100)), startPoint.add(vecteurCentreVersPoint.mul(100)));//todo améliorer
        Vector2 coinBasGauche = segmentStartPointVersCoinBasGauche.intersect(segmentCentreVersPointOrtho);
        Vector2 origin = coinBasGauche.sub(new Vector2(-CraftConfig.lengthNormal / 2, -CraftConfig.bitWidth / 2 + MARGIN_EXT).rotate(vecteurCentreVersPoint.normal().getOpposite()));//todo bizarre

        DebugTools.currentSegToDraw = segmentCentreVersPointOrtho;//orange
        DebugTools.currentSegToDraw2 = segmentStartPointVersCoinBasGauche;//rouge
        DebugTools.pointsToDrawBLUE.addAll(sectionPoints);
        DebugTools.pointsToDrawRED.add(lastPointPossible);

//        DebugTools.segmentsToDrawBlue.addAll(bit.getBitSidesSegments());
        DebugTools.pointsToDrawORANGE.add(centre);
        DebugTools.segmentsToDrawRed.addAll(GeneralTools.pointsToSegments(sectionPoints));
        Bit2D bit = new Bit2D(origin, vecteurCentreVersPoint);

        return bit;
    }

    private int getNumberOfIntersection(Segment2D segmentToTest, List<Vector2> sectionPoints) {
        int nbIntersections = 0;
        Vector<Segment2D> segments = GeneralTools.pointsToSegments(sectionPoints);
        for (Segment2D segment2D : segments) {
            if (Segment2D.doSegmentsIntersect(segmentToTest, segment2D)) {// && segment2D.end != Segment2D.getIntersectionPoint(segmentToTest, segment2D)) {
                nbIntersections++;
//                DebugTools.pointsToDrawGREEN.add(Segment2D.getIntersectionPoint(segmentToTest, segment2D));
//                System.out.println("intersection en : " + Segment2D.getIntersectionPoint(segmentToTest, segment2D));
//                DebugTools.segmentsToDraw.add(segmentToTest);
//                DebugTools.segmentsToDraw.add(segment2D);
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
