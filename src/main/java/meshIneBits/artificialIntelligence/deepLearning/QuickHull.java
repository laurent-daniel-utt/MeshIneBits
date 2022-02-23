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

import meshIneBits.util.Vector2;

import java.util.Vector;

public class QuickHull {
    public Vector<Vector2> hull = new Vector<>();

//    public static void main(String[] args) {
//        Vector<Vector2> list = new Vector<>();
//        list.add(new Vector2(0,0));
//        list.add(new Vector2(0,10));
//        list.add(new Vector2(10,10));
//        list.add(new Vector2(10,0));
//
//
//        System.out.println("initial list = "+list);
//        Vector<Vector2> hull = printHull(list);
//        hull = removeDuplicates(hull);
//        System.out.println("hull = " + hull);
//
//    }

    public QuickHull(Vector<Vector2> points) {
//        Vector<Vector2> hull1 = printHull(points);
//        hull1 = removeDuplicates(hull1);
//        hull1 = reordonnatePoints(hull1, points);
//        hull1.add(hull1.firstElement());//TODO j'ai mis ca mais ??
//        hull = hull1;

//        WrappingConvexHull wrappingConvexHull = new WrappingConvexHull(points);

//        hull = wrappingConvexHull.hull; // to close the hull
//        hull.add(hull.firstElement());
        hull = getHull(points);
    }

    public Vector<Vector2> getHull(Vector<Vector2> points) {
        System.out.println("START");

        //trouver le point le plus en bas à gauche
        int minIndex = 0;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).x < points.get(minIndex).x
                    && points.get(i).y < points.get(minIndex).y)
                minIndex = i;
        }
        hull.add(points.get(minIndex));


        // en partant de ce point, trouver chaque prochain point dont l'angle est le plus grand
        // jusqu'à ce qu'on retourne au point de départ


        //calcul du second point
        Vector2 previousPoint = points.get(minIndex).sub(new Vector2(-1, 0));
        Vector2 pointMilieu = points.get(minIndex);
        //points.add(points.get(minIndex));//on fait ca pour que l'optimisation du i= dans le for repasse par le premier point

        int stop =0;
        int maxIndex = -1;
        while (maxIndex!=minIndex) {
            stop++;
//            if (stop>20)
//                break;
            double maxAngle = 0;
//            for (int i = points.indexOf(pointMilieu); i < points.size(); i++) {
            for (int i = 0; i < points.size(); i++) {
                double angle = Vector2.getAngle(previousPoint, pointMilieu, points.get(i));

//                if (angle >= maxAngle && i!=points.indexOf(pointMilieu)) {
                if (angle + 0.00001>=maxAngle && i!=points.indexOf(pointMilieu)) {
                    maxAngle = angle;
                    maxIndex = i;
                }
            }
//            System.out.println("max = "+maxIndex+"  min = "+minIndex);

//            System.out.println("maxAngle = "+maxAngle);

            if (maxIndex != -1) {
                hull.add(points.get(maxIndex));
            }
            System.out.println("minIndex = " + minIndex + " maxIndex = " + maxIndex);
            previousPoint = pointMilieu;
            pointMilieu = points.get(maxIndex);
        }
        System.out.println("END");


        return hull;
    }

    public Vector<Vector2> getConvexHull() {
        return hull;
    }
//
//    private static Vector<Vector2> reordonnatePoints(Vector<Vector2> messyList, Vector<Vector2> boundPoints) {
//        Vector<Vector2> reorderedPoints = new Vector<>();
//        for (Vector2 boundPoint : boundPoints) {
//            if (messyList.contains(boundPoint)) reorderedPoints.add(boundPoint);
//        }
//        return reorderedPoints;
//    }
//
//    public Vector<Vector2> getConvexHull() {
//        return hull;
//    }
//
//    private Vector<Vector2> removeDuplicates(Vector<Vector2> list) {
//        Vector<Vector2> hull2 = new Vector<>();
//        for (Vector2 vector2 : list) {
//            if (!hull2.contains(vector2)) {
//                hull2.add(vector2);
//            }
//        }
//        return hull2;
//    }
//
//    /**
//     * Returns the side of the given point respect to line joining points p1 and p2.
//     *
//     * @param p1 the first point of the line
//     * @param p2 the second point of the line
//     * @param p  the given point
//     * @return the side of the given point
//     */
//    double findSide(Vector2 p1, Vector2 p2, Vector2 p) {
//        //todo ca serait pas juste notre méthode qui regarde si un point est d'un côté ou d'un autre d'un segment?
//        // cette méthode est meilleure ou on garde la notre ?
//        double val = (p.y - p1.y) * (p2.x - p1.x) - (p2.y - p1.y) * (p.x - p1.x);
//        return Double.compare(val, 0);
//    }
//
//    /**
//     * Computes the distance between the point p and a line formed by the points p1 and p2
//     *
//     * @param p1 the first point of the line
//     * @param p2 the second point of the line
//     * @param p  the given point
//     * @return the distance between the point and the line
//     */
//    double lineDist(Vector2 p1, Vector2 p2, Vector2 p) {
//        //todo pareil on a pas déjà cette méthode ?
//        return Math.abs((p.y - p1.y) * (p2.x - p1.x) - (p2.y - p1.y) * (p.x - p1.x));
//    }
//
//
//    /**
//     * QuickHull recursive algorithm core.
//     * End points of line L are p1 and p2.
//     *
//     * @param hull the hull which is passed through the recursive algorithm
//     * @param list the list of points
//     * @param n    the size of the list
//     * @param p1   the first point of the line
//     * @param p2   the second point of the line
//     * @param side the side on which stands a point. -1 or 1 specifying each of the parts made by the line
//     */
//    void quickHull(Vector<Vector2> hull, Vector<Vector2> list, int n, Vector2 p1, Vector2 p2, double side) {
//        int index = -1;
//        double max_dist = 0;
//
//        // finding the point with maximum distance from L and also on the specified side of L.
//        for (int i = 0; i < n; i++) {
//            double dist = lineDist(p1, p2, list.get(i));
//            if (findSide(p1, p2, list.get(i)) == side && dist > max_dist) {
//                index = i;
//                max_dist = dist;
//            }
//        }
//
//        // If no point is found, add the end points of L to the convex hull.
//        if (index == -1) {
//            hull.add(p1);
//            hull.add(p2);
//            return;
//        }
//
//        if (hull.contains(list.get(index)))
//            return;//TODO @Etienne, c'est un quick fix, a voir si on le garde
//
//        // Recur for the two parts divided by list[index]
//        quickHull(hull, list, n, list.get(index), p1, -findSide(list.get(index), p1, p2));
//        quickHull(hull, list, n, list.get(index), p2, -findSide(list.get(index), p2, p1));
//    }
//
//    /**
//     * Find the convex Hull of a given set of points.
//     *
//     * @param list the set of points
//     * @return the computed convex hull
//     */
//    Vector<Vector2> printHull(Vector<Vector2> list) {
//        int n = list.size();
//        if (n <= 3) return list; // if we have only 3 points, we can already return them
//
//        // Finding the point with minimum and maximum x-coordinate
//        int min_x = 0, max_x = 0;
//
//        for (int i = 0; i < n; i++) {
//            if (list.get(i).x < list.get(min_x).x) min_x = i;
//            if (list.get(i).x > list.get(max_x).x) max_x = i;
//        }
//
//
//        Vector<Vector2> hull = new Vector<>();
//        // Recursively find convex hull points on one side of line joining list[min_x] and list[max_x]
//        quickHull(hull, list, n, list.get(min_x), list.get(max_x), 1);
//        // Recursively find convex hull points on other side of line joining list[min_x] and list[max_x]
//        quickHull(hull, list, n, list.get(min_x), list.get(max_x), -1);
//
//        return hull;
//    }
}