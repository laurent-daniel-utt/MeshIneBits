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
import meshIneBits.artificialIntelligence.AI_Tool;
import meshIneBits.artificialIntelligence.GeneralTools;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Area;
import java.util.Vector;


public class BorderedPatternAlgorithm {
    //private PlotHelper plotHelper = new PlotHelper();

    public Bit2D calculateBitPosition(@NotNull Vector<Vector2> sectionPoints, Area areaSlice, double minWidthToKeep) {

        double distExt = 0.1;

        Vector<Vector2> sectionRepopulated = GeneralTools.repopulateWithNewPoints(100, sectionPoints, true);


        boolean sectionReductionCompleted = false;

        Segment2D longestSegment;
        Vector2 furthestPoint;

        int cpt = 0;

        do {
//            plotHelper.addSeries("section", sectionRepopulated, Plot.Marker.DIAMOND, Plot.Line.NONE, 6);
//            plotHelper.save();

            Vector<Vector2> hull = convex_hull(sectionRepopulated);
            //DebugTools.pointsToDrawBLUE = (Vector<Vector2>) hull.clone();
            hull.add(hull.firstElement());
            Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hull);

//            DebugTools.pointsToDrawBLUE = (Vector<Vector2>) hull.clone();
//            DebugTools.segmentsToDraw = (Vector<Segment2D>) segmentsHull.clone();
//            DebugTools.pointsToDrawRED = (Vector<Vector2>) sectionRepopulated.clone();
//            DebugTools.setPaintForDebug(true);


//            plotHelper.addSeries("hull", hull, Plot.Marker.CIRCLE, Plot.Line.SOLID, 5);
//            plotHelper.save();

//            DebugTools.pointsToDrawRED = sectionPoints;
//            DebugTools.segmentsToDraw = segmentsHull;
//            DebugTools.setPaintForDebug(true);

            // trouver le plus long segment
            longestSegment = getLongestSegment(segmentsHull);

            // trouver le point le plus éloigné du longestSegment
            furthestPoint = getFurthestPointFromSegment(longestSegment, hull);
            double maxDistance = Vector2.Tools.distanceFromPointToLine(furthestPoint, longestSegment);

            if (maxDistance > CraftConfig.bitWidth - minWidthToKeep) { // cas où on ne peut pas couvrir toute la section avec une lamelle
//                System.out.println("superieur a largeur bit");
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
//                            System.out.println("aaaaaaaaa");
                        }
                        cutPointFound = true;
                    }
                    iSection--;
                }
            } else {
                sectionReductionCompleted = true;
            }
            cpt++;
//            System.out.println("max distance = " + maxDistance + " and recuction completed = " + sectionReductionCompleted);

        }
        while (!sectionReductionCompleted);
        //System.out.println("cpt = " + cpt);

        Vector<Vector2> hull = convex_hull(sectionRepopulated);

        //DebugTools.pointsToDrawRED = sectionPoints;
        hull.add(hull.firstElement());
        Vector<Segment2D> segmentsHull = GeneralTools.getSegment2DS(hull);

        // trouver le plus long segment
        longestSegment = getLongestSegment(segmentsHull);

        // trouver le point le plus éloigné du longestSegment
        furthestPoint = getFurthestPointFromSegment(longestSegment, hull);


        // cas ou on a fait des decoupes donc la position du bit est contraite

        Vector2 rotation = longestSegment.getDirectionalVector();


        // projection orthogonale de FurthestPoint sur LongesSegment
        Vector2 ortProjFurthestPoint = getOrthogonalProjection(longestSegment, furthestPoint);


//        DebugTools.pointsToDrawRED.add(ortProjFurthestPoint);
//        DebugTools.pointsToDrawGREEN.add(furthestPoint);
//        DebugTools.segmentsToDraw.add(longestSegment);


//        DebugTools.pointsToDrawRED = sectionPoints;
//        DebugTools.setPaintForDebug(true);


        Vector2 midPoint = longestSegment.getMidPoint();
        Vector2 posLigneRoseLocal = ortProjFurthestPoint.sub(furthestPoint).normal().mul(CraftConfig.bitWidth/2-distExt);
        Vector2 posLigneRose;
        if (areaSlice.contains(midPoint.x, midPoint.y)) {
            posLigneRose = furthestPoint.add(posLigneRoseLocal);
//            System.out.println("a l'interieur");
        } else {
            posLigneRose = ortProjFurthestPoint.sub(posLigneRoseLocal);
//            System.out.println("a l'extérieur");
        }

        //DebugTools.pointsToDrawGREEN.add(posLigneRose);
        //DebugTools.setPaintForDebug(true);


        Segment2D ligneRose = new Segment2D(
                longestSegment.getDirectionalVector().mul(-10000).add(posLigneRose),
                longestSegment.getDirectionalVector().mul(10000).add(posLigneRose));
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


        double longueur = CraftConfig.lengthFull/2-Vector2.dist(pointRose,posLigneRose);
        Vector2 position = posLigneRose.add(directionalVector.mul(longueur));

        Bit2D bit = new Bit2D(position, rotation);

        System.out.println("bit place = " + bit);

        //DebugTools.pointsToDrawGREEN.add(position);


        return bit;
        //return new Bit2D(new Vector2(1, 1), new Vector2(1, 1));
    }


    private Segment2D getLongestSegment(Vector<Segment2D> segments) {
        Segment2D longestSegment = null;
        double maxLength = -1;
        for (Segment2D s : segments) {
            if (s.getLength() > maxLength) {
                longestSegment = s;
                maxLength = s.getLength();
            }
        }
        return longestSegment;
    }

    private Vector2 getFurthestPointFromSegment(Segment2D s, Vector<Vector2> points) {
        Vector2 furthestPoint = null;
        double maxDistance = -1;
        for (Vector2 p : points) {
            if (Vector2.Tools.distanceFromPointToLine(p, s) > maxDistance) {
                furthestPoint = p;
                maxDistance = Vector2.Tools.distanceFromPointToLine(p, s);
            }
        }
        return furthestPoint;
    }


    private Vector2 getOrthogonalProjection(Segment2D AB, Vector2 C) {
        Vector2 vAC = AB.start.sub(C);
        Vector2 vAB = AB.getDirectionalVector().mul(AB.getLength());
        double orthogonalProjectionDist = vAB.dot(vAC) / AB.getLength();
        return AB.start.add(AB.getDirectionalVector().mul(-orthogonalProjectionDist));
    }


    public Vector<Bit2D> getBits2(Slice slice, double minWidthToKeep) throws Exception {
        System.out.println("PAVING SLICE " + slice.getAltitude());
        Vector<Bit2D> bits = new Vector<>();

        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);

        Vector2 startPoint = new Vector2(86.28867818076299, 236.48951109657744);
        //Vector2 startPoint = bounds.get(0).get(10);

        Vector<Vector2> sectionPoints = GeneralTools.getSectionPointsFromBound(bounds.get(0), startPoint);

        Area areaSlice = AreaTool.getAreaFrom(slice);

        bits.add(calculateBitPosition(sectionPoints, areaSlice,minWidthToKeep));
//        DebugTools.pointsToDrawGREEN.add(startPoint);

        return bits;
    }


    public Vector<Bit2D> getBits(Slice slice, double minWidthToKeep) throws Exception {
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
            do{
                sectionPoints = GeneralTools.getSectionPointsFromBound(bound, nextStartPoint);
                Bit2D bit = calculateBitPosition(sectionPoints, areaSlice, minWidthToKeep);
                bits.add(bit);
                nextStartPoint = GeneralTools.getBitAndContourSecondIntersectionPoint(bit, bound,false,null);

//                nextStartPoint = new GeneralTools().getNextBitStartPoint(bit, bound);
                nbMaxBits++;
                System.out.println("nextStartPoint = " + nextStartPoint);
//                DebugTools.pointsToDrawGREEN.removeAllElements();
//                DebugTools.pointsToDrawGREEN.add(nextStartPoint);
//                DebugTools.setPaintForDebug(true);
//                DebugTools.pointsToDrawBLUE.removeAllElements();
//                DebugTools.pointsToDrawBLUE.addAll(sectionPoints);
//                DebugTools.pointsToDrawRED.add(veryFirstStartPoint);
                //DebugTools.pointsToDrawRED = sectionPoints;
                //DebugTools.setPaintForDebug(true);
            }
//            while (nextStartPoint!=null && nbMaxBits < 30); //Add each bit on the bound
            while (!new AI_Tool().hasCompletedTheBound(veryFirstStartPoint, nextStartPoint, bits.lastElement(), bound) && nbMaxBits < 30); //Add each bit on the bound
//            while (new AI_Tool().hasNotCompletedTheBound(veryFirstStartPoint, startPoint, sectionPoints) && nbMaxBits < 3000); //Add each bit on the bound

        }
        return bits;

    }

    // https://www.tutorialcup.com/interview/algorithm/convex-hull-algorithm.htm
    // return true if check3 isn't part of the hull
    public boolean OrientationMatch(Vector2 check1, Vector2 check2, Vector2 check3) {

//        Vector<Vector2> p1 = new Vector<>();
//        Vector<Vector2> p2 = new Vector<>();
//        Vector<Vector2> p3 = new Vector<>();
//        p1.add(check1);
//        p2.add(check2);
//        p3.add(check3);
//        plotHelper.addSeries("p1", p1, Plot.Marker.CIRCLE, Plot.Line.NONE, 30);
//        plotHelper.addSeries("p2", p2, Plot.Marker.DIAMOND, Plot.Line.NONE, 20);
//        plotHelper.addSeries("p3", p3, Plot.Marker.SQUARE, Plot.Line.NONE, 10);
//        plotHelper.save();

//        System.out.println(plotHelper.index + " :    " + check2.isOnSegment(new Segment2D(check1, check3)));

        double val = (check2.y - check1.y) * (check3.x - check2.x) - (check2.x - check1.x) * (check3.y - check2.y);
        if (check2.asGoodAsEqual(check3) || check1.asGoodAsEqual(check2) ||
                (Math.abs(val)<Math.pow(10, -CraftConfig.errorAccepted)
                && check3.sub(check2).dot(check1.sub(check2))<=0)) {
            return false;
        }
        return val <= Math.pow(10, -CraftConfig.errorAccepted);
    }

    // https://www.tutorialcup.com/interview/algorithm/convex-hull-algorithm.htm
    public Vector<Vector2> convex_hull(Vector<Vector2> points) {
        int lengths = points.size();
        if (lengths<3) return null;
        Vector<Vector2> result = new Vector<>();
        int leftmost = 0;
        for (int i = 1; i<lengths; i++)
            if (points.get(i).x<points.get(leftmost).x)
                leftmost = i;
        int p = leftmost, pointq;
        do {
            result.add(points.get(p));
            pointq = (p + 1) % lengths;
            for (int i = 0; i<lengths; i++) {
                if(OrientationMatch(points.get(p), points.get(i), points.get(pointq))) {
                    pointq = i;
                }
            }
            p = pointq;
        }
        while (p != leftmost);

        return result;
    }



    public static void main(String[] args) {

        Vector2 a = new  Vector2(0, 0);
        Vector2 b = new Vector2(0, 1);
        Vector2 c = new Vector2(0, -1);
        Vector2 d = new Vector2(0, 2);
        Segment2D s = new Segment2D(a, b);
        Segment2D r = new Segment2D(c,d);

        System.out.println(Segment2D.getIntersectionPoint(s,r));


    }

}
