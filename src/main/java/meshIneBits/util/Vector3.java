/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas..
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

package meshIneBits.util;

import java.io.Serializable;

/**
 * Vector3 represents a point in 3D space.
 */
public class Vector3 implements Serializable {
    public double x, y, z;

    public Vector3() {
        // TODO Auto-generated constructor stub
    }

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void addToSelf(Vector3 v) {
        x += v.x;
        y += v.y;
        z += v.z;
    }

    public Vector3 cross(Vector3 v) {
        return new Vector3((y * v.z) - (z * v.y), (z * v.x) - (x * v.z), (x * v.y) - (y * v.x));
    }

    private Vector3 div(double f) {
        return new Vector3(x / f, y / f, z / f);
    }

    /**
     * Return the scalar of two vector
     *
     * @param v other one
     * @return scalar product
     */
    public double dot(Vector3 v) {
        return (x * v.x) + (y * v.y) + (z * v.z);
    }

    public Vector3 normal() {
        return div(vSize());
    }

    public Vector3 sub(Vector3 v) {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }

    /**
     * Return the size (length) of the vector
     */
    private double vSize() {
        return Math.sqrt((x * x) + (y * y) + (z * z));
    }

    /**
     * Return the squared size (length) of the vector
     *
     * @return euclidean norm
     */
    public double vSize2() {
        return (x * x) + (y * y) + (z * z);
    }
}
