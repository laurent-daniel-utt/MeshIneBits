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

import meshIneBits.borderPaver.util.SectionTransformer;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

public class Section {
    private final Vector2 startPoint;
    private final Vector<Vector2> points;
    private final Vector<Segment2D> segments;


    public Section(Vector<Vector2> sectionPoints) {
        this.points = sectionPoints;
        this.segments = pointsToSegments(sectionPoints);
        this.startPoint = sectionPoints.firstElement();
    }

    /**
     * Computes the distance between a startPoint and an endPoint. But not a direct distance.
     * The distance is calculated passing by the segments of the bound
     *
     * @param startPoint a point which is on a segment
     * @param endPoint   the end of a segment
     * @param bound      the Slice
     * @return the distance
     */
    public static double getDistViaSegments(Vector2 startPoint, Vector2 endPoint, Vector<Segment2D> bound) {
        //finds the nearest point from startPoint and which is after the startPoint
        Segment2D minSeg = null;
        Segment2D startSeg = null;
        for (Segment2D segment2D : bound) {
            if (startPoint.isOnSegment(segment2D)) {
                startSeg = segment2D;
                minSeg = new Segment2D(segment2D.end, startPoint);
                break;
            }
        }

        //distance between the startPoint and the nearest segment
        //then we loop through the segments, starting from minPoint to end
        double totalDist = minSeg.getLength();//+sum of segments

        for (Segment2D segment2D : bound.subList(bound.indexOf(startSeg), bound.size())) {
            if (endPoint.isOnSegment(segment2D)) {
                return totalDist + new Segment2D(endPoint, segment2D.start).getLength();
            }
            totalDist += segment2D.getLength();
        }
        return totalDist;
    }

    /**
     * Checks if the given point A is located before the given point B on the given polygon.
     * @param A a {@link Vector2}.
     * @param B a {@link Vector2}.
     * @param polygon a {@link Vector} of {@link Vector2}.
     * @return true if A is located before B on the polygon, false otherwise.
     */
    static boolean isABeforeBOnPolygon(Vector2 A, Vector2 B, Vector<Segment2D> polygon) {
        // on parcourt le polygon, et on regarde si on trouve le point A avant le point B
        for (Segment2D segment : polygon) {
            if (A.isOnSegment(segment) && B.isOnSegment(segment)) {
                return Vector2.dist(segment.start, A) < Vector2.dist(segment.start, B);
            }
            if (A.isOnSegment(segment)) return true;
            if (B.isOnSegment(segment)) return false;
        }
        throw new RuntimeException("Points not found on polygon");
    }

    /**
     * Checks if most of the points are located at the left of a reference point
     *
     * @return true if most of the points are at the left of the reference point.
     */
    public boolean arePointsMostlyOrientedToTheLeft() {
        int leftPoints = 0;
        int rightPoints = 0;
        for (Vector2 point : points) {
            if (point.x < startPoint.x) {
                leftPoints++;
            } else {
                rightPoints++;
            }
        }
        return leftPoints >= rightPoints;
    }


    /**
     * Compute and return the longest segment of the given list
     *
     * @return the longest segment
     */
    public Segment2D getLongestSegment() {
        return segments.stream().max(Comparator.comparing(Segment2D::getLength)).orElseThrow(NoSuchElementException::new);
    }

    /**
     * Compute and return the most distant point to a segment
     *
     * @param segment2D the segment
     * @return the furthest point from the segment
     */
    public Vector2 getFurthestPointFromSegment(Segment2D segment2D) {
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
     * Search for the furthest point from ref point via a vector by computing a projection along the vector.
     *
     * @param refPoint          the first point
     * @param directionalVector the second point
     * @return the furthest point
     */
    public Vector2 getFurthestPointFromRefPointViaVector(Vector2 refPoint, Vector2 directionalVector) {
        directionalVector = directionalVector.normal();
        double maxDist = Double.NEGATIVE_INFINITY;
        Vector2 furthestPoint = null;
        for (Vector2 p : points) {
            double dist = BorderedPatternAlgorithm.getDistFromFromRefPointViaVector(refPoint, p, directionalVector);
            if (dist > maxDist) {
                maxDist = dist;
                furthestPoint = p;
            }
        }
        return furthestPoint;
    }


    /**
     * Finds the furthest point among a list of points from a reference point (refPoint), calculating direct distances
     * between them.
     * @param refPoint the point from which the method calculates the distances
     * @return the furthest point from refPoint among the list  of points
     */
    public Vector2 getFurthestPoint(Vector2 refPoint) {
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

    //minimum distance of wood needed to be kept when placing, in order to avoid the cut bit
    public Section getSectionReduced(Section sectionPoints, double minWidthToKeep) {
        Section sectionToReduce = new Section(SectionTransformer.repopulateWithNewPoints(200, sectionPoints, true));

        boolean sectionReductionCompleted = false;

        Segment2D constraintSegment;
        Vector2 furthestPoint;

        do {

            boolean sectionIsClosed = sectionToReduce.startPoint.asGoodAsEqual(sectionToReduce.points.lastElement());

            // calculates the convex hull of the section's points
            Section hull = sectionToReduce.getHull();

            // find the constraint segment, which is the longest segment of the hull // todo, maybe not always the case
            constraintSegment = hull.getLongestSegment();

            // find the constraint point, which is the convex hull's furthest point from the constraint segment
            furthestPoint = hull.getFurthestPointFromSegment(constraintSegment);

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

    public Section getHull() {
        Vector<Vector2> hull = new Vector<>();

        // find leftMost
        Vector<Integer> iLeftMost = new Vector<>();
        double xMin = Double.POSITIVE_INFINITY;
        for (Vector2 point : points) {
            double x = point.x;
            if (x <= xMin) {
                xMin = x;
            }
        }
        for (int i = 0; i < points.size(); i++) {
            if (Math.abs(points.get(i).x - xMin) < Math.pow(10, -5)) iLeftMost.add(i);
        }
        // find higher of leftMost
        int iHigherLeftMost = iLeftMost.get(0);
        for (int i = 1; i < iLeftMost.size(); i++) {
            if (points.get(iLeftMost.get(i)).y < points.get(iHigherLeftMost).y) iHigherLeftMost = iLeftMost.get(i);
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
                // le point pivot étant le dernier point ajouté cela ne sert à rien de le tester à nouveau, de plus cela entraine des calculs d'angles erronés
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
                Section maxAnglesSection = new Section(pointsMaxAngles);
                hull.add(maxAnglesSection.getFurthestPoint(pointMilieu));
            }
            previousPoint = pointMilieu;
            pointMilieu = hull.lastElement();
        }
        return new Section(hull);
    }


    /**
     * Converts a list of {@link Vector2} to a list of {@link Vector} that would connect each point to the other,
     * following the order of the list of points given as entry. Deletes the points that are duplicated.
     *
     * @param points the points requiring to be converted into segments.
     * @return the segments resulting from the conversion.
     */
    @NotNull
    public static Vector<Segment2D> pointsToSegments(List<Vector2> points) {
        //remove the duplicated points
        Vector<Vector2> pointsNoDuplicates = new Vector<>();
        for (Vector2 point : points) {
            if (!pointsNoDuplicates.contains(point)) {
                pointsNoDuplicates.add(point);
            }
        }
        Vector<Segment2D> sectionSegments = new Vector<>();
        for (int i = 0; i < pointsNoDuplicates.size() - 1; i++) {
            sectionSegments.add(new Segment2D(pointsNoDuplicates.get(i), pointsNoDuplicates.get(i + 1)));
        }
        return sectionSegments;
    }

    /**
     * Returns a point list from a segment list.
     * If the first and last segment are connected, the first point is not added.
     *
     * @param segmentList the segment list
     * @return the list of point computed from the segment list
     */
    static @NotNull Vector<Vector2> segmentsToPoints(@NotNull Vector<Segment2D> segmentList) {
        Vector<Vector2> pointsList = new Vector<>();
        for (Segment2D segment : segmentList) {
            pointsList.add(new Vector2(segment.start.x, segment.start.y));
        }
        if (pointsList.firstElement().asGoodAsEqual(pointsList.lastElement())) pointsList.remove(0);
        return pointsList;
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
