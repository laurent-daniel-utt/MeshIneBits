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

import meshIneBits.util.Vector2;

import java.util.Vector;

public class QuickHull {
    public final Vector<Vector2> hull;

    public QuickHull(Vector<Vector2> points) {
        hull = getHull(points);
    }

    public Vector<Vector2> getHull(Vector<Vector2> points) {
        System.out.println("START");

        //trouver le point le plus en bas à gauche
        int minIndex = 0;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).x < points.get(minIndex).x && points.get(i).y < points.get(minIndex).y) minIndex = i;
        }
        hull.add(points.get(minIndex));


        // en partant de ce point, trouver chaque prochain point dont l'angle est le plus grand
        // jusqu'à ce qu'on retourne au point de départ


        //calcul du second point
        Vector2 previousPoint = points.get(minIndex).sub(new Vector2(-1, 0));
        Vector2 pointMilieu = points.get(minIndex);

        int maxIndex = -1;
        while (maxIndex != minIndex) {
            double maxAngle = 0;
            for (int i = 0; i < points.size(); i++) {
                double angle = Vector2.getAngle(previousPoint, pointMilieu, points.get(i));

                if (angle + 0.00001 >= maxAngle && i != points.indexOf(pointMilieu)) {
                    maxAngle = angle;
                    maxIndex = i;
                }
            }

            if (maxIndex != -1) {
                hull.add(points.get(maxIndex));
            }
            previousPoint = pointMilieu;
            pointMilieu = points.get(maxIndex);
        }

        return hull;
    }

    public Vector<Vector2> getConvexHull() {
        return hull;
    }
}