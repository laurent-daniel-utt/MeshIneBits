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
 * Copyright (C) 2020 CLARIS Etienne & RUSSO André.
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

package meshIneBits.artificialIntelligence.util;

import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.util.Vector;

/**
 * A Curve is a tool allowing to save some points and perform parametric regressions on them.
 */
public class Curve {
    private final String name;
    private Vector<Vector2> points = new Vector<>();

    /**
     * An undefined curve.
     *
     * @param name the name of the curve.
     */
    public Curve(String name) {
        this.name = name;
    }

    /**
     * Generates the curve with points.
     *
     * @param points the points to add to the curve.
     */
    public void generateCurve(Vector<Vector2> points) {
        this.points = points;
    }

    /**
     * Splits a curve in x(t) and y(t) as in parametric curves.
     *
     * @return the x(t) and y(t) curves.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public Curve @NotNull [] splitCurveInTwo() {

        Vector<Vector2> pointsX = new Vector<>();
        Vector<Vector2> pointsY = new Vector<>();
        double curvilinearAbs = 0;

        pointsX.add(new Vector2(curvilinearAbs, points.get(0).x));
        pointsY.add(new Vector2(curvilinearAbs, points.get(0).y));

        for (int i = 1; i < this.points.size(); i++) {
            curvilinearAbs += Math.sqrt(Math.pow(points.get(i - 1).x - points.get(i).x, 2)
                    + Math.pow(points.get(i - 1).y - points.get(i).y, 2));
            pointsX.add(new Vector2(curvilinearAbs, points.get(i).x));
            pointsY.add(new Vector2(curvilinearAbs, points.get(i).y));
        }

        Curve curveX = new Curve("x(t)");
        Curve curveY = new Curve("y(t)");
        curveX.generateCurve(pointsX);
        curveY.generateCurve(pointsY);

        return new Curve[]{curveX, curveY};
    }


    public Vector<Vector2> getPoints() {
        return points;
    }

    public int getNumberOfPoints() {
        return points.size();
    }

    @Override
    public @NotNull String toString() {
        StringBuilder str = new StringBuilder("name = " + name + "\n");
        for (Vector2 v : points) {
            str.append(v.x).append("\t").append(v.y).append("\n");
        }
        return str.toString();
    }
}