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

import meshIneBits.artificialIntelligence.util.SectionTransformer;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Vector;

public class Section {
    private final Vector2 startPoint;
    private final Vector<Vector2> points;
    private final Vector<Segment2D> segments;


    public Section(Vector<Vector2> sectionPoints) {
        this.points = sectionPoints;
        this.segments = GeneralTools.pointsToSegments(sectionPoints);
        this.startPoint = sectionPoints.firstElement();
    }

    public Section(Vector<Vector2> sectionPoints, Vector2 startPoint) {
        this.points = sectionPoints;
        this.segments = GeneralTools.pointsToSegments(sectionPoints);
        this.startPoint = startPoint;
    }


    /**
     * Compute and return the longest segment of the given list
     *
     * @param segment2DS the segments list
     * @return the longest segment
     */
    public static Segment2D getLongestSegment(Vector<Segment2D> segment2DS) {
        return segment2DS.stream().max(Comparator.comparing(Segment2D::getLength)).orElseThrow(NoSuchElementException::new);
    }

    /**
     * Compute and return the most distant point to a segment
     *
     * @param segment2D the segment
     * @param points    the list of points
     * @return the furthest point from the segment
     */
    public static Vector2 getFurthestPointFromSegment(Segment2D segment2D, Vector<Vector2> points) {
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
    public static Vector2 getFurthestPointFromRefPointViaVector(Vector2 refPoint, Vector2 directionalVector, Vector<Vector2> points) {
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

    // todo methode en double dans generalTools
    public static boolean listContainsAllAsGoodAsEqual(Vector<Vector2> containedList, Vector<Vector2> containerList) {
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
    public static Vector2 getFurthestPoint(Vector2 refPoint, Vector<Vector2> points) {
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

    public static boolean listContainsAsGoodAsEqual(Vector2 point, Vector<Vector2> points) {
        for (Vector2 p : points) {
            if (point.asGoodAsEqual(p)) {
                return true;
            }
        }
        return false;
    }

    //minimum distance of wood needed to be kept when placing, in order to avoid the cut bit
    public Section getSectionReduced(Vector<Vector2> sectionPoints, double minWidthToKeep) {
        Section sectionToReduce = new Section(SectionTransformer.repopulateWithNewPoints(200, sectionPoints, true));

        boolean sectionReductionCompleted = false;

        Segment2D constraintSegment;
        Vector2 furthestPoint;

        do {

            boolean sectionIsClosed = sectionToReduce.startPoint.asGoodAsEqual(sectionToReduce.points.lastElement());

            // calculates the convex hull of the section's points
            Vector<Vector2> hull = sectionToReduce.getHull();

            Vector<Segment2D> segmentsHull = GeneralTools.pointsToSegments(hull);

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
            if ((!sectionIsClosed && (sectionWidth > CraftConfig.bitWidth - minWidthToKeep)) || (sectionIsClosed && sectionWidth > CraftConfig.bitWidth)) {

                // list the cut points
                Vector<Vector2> cutPoints = new Vector<>();
                cutPoints.add(constraintSegment.start);
                cutPoints.add(constraintSegment.end);
                cutPoints.add(furthestPoint);
                // research of the first "cut point" of the section, starting research by its last point.
                boolean cutPointFound = false;
                int iSection = sectionToReduce.getPoints().size() - 1;
                while (!cutPointFound) {
                    if (cutPoints.contains(sectionToReduce.getPoints().get(iSection))) {
                        // delete section's points from the cut point at iSection (included) to the last point of the section
                        while (sectionToReduce.getPoints().size() > iSection) {
                            sectionToReduce.getPoints().remove(iSection);
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

    public Vector<Vector2> getHull() {
        Vector<Vector2> hull = new Vector<>();

        // find leftMosts
        Vector<Integer> iLeftMosts = new Vector<>();
        double xMin = Double.POSITIVE_INFINITY;
        for (Vector2 point : points) {
            double x = point.x;
            if (x <= xMin) {
                xMin = x;
            }
        }
        for (int i = 0; i < points.size(); i++) {
            if (Math.abs(points.get(i).x - xMin) < Math.pow(10, -5)) iLeftMosts.add(i);
        }
        // find higher of leftMosts
        int iHigherLeftMost = iLeftMosts.get(0);
        for (int i = 1; i < iLeftMosts.size(); i++) {
            if (points.get(iLeftMosts.get(i)).y < points.get(iHigherLeftMost).y) iHigherLeftMost = iLeftMosts.get(i);
        }
        int startIndex = iHigherLeftMost;

        hull.add(points.get(startIndex));


        // en partant de ce point, trouver chaque prochain point dont l'angle est le plus grand
        // jusqu'à ce qu'on retourne au point de départ


        //calcul du second point
        Vector2 previousPoint = points.get(startIndex).sub(new Vector2(-1, 0));
        Vector2 pointMilieu = points.get(startIndex);

        while (hull.size() < 2 || hull.firstElement() != hull.lastElement()) {
            double maxAngle = 0;
            Vector<Vector2> pointsMaxAngles = new Vector<>();
            for (int i = 0; i < points.size(); i++) {
                // le point pivot étant le dernier point ajouté cela ne sert à rien de le tester à nouveau, de plus cela entraine des calculs d'agles erronés
                if (points.get(i) != pointMilieu) {
                    double angle = Vector2.getAngle(previousPoint, pointMilieu, points.get(i));

                    if (angle >= maxAngle - 1e-10 && i != points.indexOf(pointMilieu)) {
                        if (angle > maxAngle + 1e-10) { // - et + ça permet de considérer qu'entre -1e-10 et +1e-10 les points sont alignés
                            pointsMaxAngles.removeAllElements();
                        }
                        maxAngle = angle;
                        pointsMaxAngles.add(points.get(i));
                    }
                }
            }

            if (pointsMaxAngles.contains(hull.firstElement())) {
                /*
                dans une section fermée, le premier et le dernier élément de la section sont les mêmes,
                mais à cause d'imprécisions dans les chiffres décimaux, parfois à la fin du hull c'est un point
                légèrement différent du premier qui est ajouté, ce qui fait que la boucle while tourne à l'infini
                 */
                hull.add(hull.firstElement());
            } else {
                hull.add(getFurthestPoint(pointMilieu, pointsMaxAngles));
            }
            previousPoint = pointMilieu;
            pointMilieu = hull.lastElement();
        }
        return hull;
    }

    //GETTERS
    public Vector2 getStartPoint() {
        return startPoint;
    }

    public Vector<Vector2> getPoints() {
        return points;
    }

    public Vector<Segment2D> getSegments() {
        return segments;
    }


}
