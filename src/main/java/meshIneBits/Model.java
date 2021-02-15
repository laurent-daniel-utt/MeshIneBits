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

package meshIneBits;

import meshIneBits.util.Logger;
import meshIneBits.util.Triangle;
import meshIneBits.util.Vector3;
import remixlab.dandelion.geom.Rotation;
import remixlab.dandelion.geom.Vec;

import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.Vector;


/**
 * A model is the full set of triangle of the existing 3D mesh to pattern. Model
 * is created from a
 * <a href="https://en.wikipedia.org/wiki/STL_(file_format)">STL file</a>.
 */
public class Model implements Serializable {
    private Vector<Triangle> triangles = new Vector<>();
    private Vector3 position = new Vector3(0, 0, 0);

    /**
     * Read all the triangles of the STL file, whether it's an Ascii or a Binary
     * STL.
     *
     * @param filename The STL file path ({@link File#toString()}).
     * @throws Exception If the file is not a STL.
     */
    public Model(String filename) throws Exception {
        Logger.updateStatus("Loading: " + filename);

        if (filename.toLowerCase().endsWith(".stl")) {
            char[] buf = new char[5];
            BufferedReader br = new BufferedReader(new FileReader(filename));
            br.mark(5);
            //noinspection ResultOfMethodCallIgnored
            br.read(buf);
            br.close();
            String header = new String(buf);

            if (header.equals("solid")) {
                this.triangles.addAll(readAsciiSTL(filename));
                //this.triangles.addAll(readBinarySTL(filename));
                if(triangles.size()==0)this.triangles.addAll(readBinarySTL(filename));
            } else {
                this.triangles.addAll(readBinarySTL(filename));
            }
        } else {
            Logger.error("Unknown model format: " + filename);
            throw new Exception();
        }
        Logger.message("Triangle count: " + triangles.size());
    }

    /**
     * Find the centre of the mesh and translate triangle so the centre is the
     * origin of the basis. To find the centre, it search the distance between
     * maximum and minimum of X and Y and divide it by two. The centre is always
     * placed at the lower Z.
     * <br><img src="./doc-files/center.png" alt="">
     */
    public void center() {
        Vector3 min = getMin();
        Vector3 max = getMax();
        Vector3 translate = new Vector3();
        translate.z = -min.z;
        translate.x = -(max.x + min.x) / 2;
        translate.y = -(max.y + min.y) / 2;

        move(translate);
    }

    /**
     * Get the max of X, Y and Z.
     *
     * @return {@link Vector3}
     */
    public Vector3 getMax() {
        Vector3 ret = new Vector3();
        ret.x = Double.MIN_VALUE;
        ret.y = Double.MIN_VALUE;
        ret.z = Double.MIN_VALUE;
        for (Triangle t : triangles) {
            for (int i = 0; i < 3; i++) {
                if (ret.x < t.point[i].x) {
                    ret.x = t.point[i].x;
                }
                if (ret.y < t.point[i].y) {
                    ret.y = t.point[i].y;
                }
                if (ret.z < t.point[i].z) {
                    ret.z = t.point[i].z;
                }
            }
        }
        return ret;
    }

    /**
     * Get the min of X, Y and Z.
     *
     * @return {@link Vector3}
     */
    public Vector3 getMin() {
        Vector3 ret = new Vector3();
        ret.x = Double.MAX_VALUE;
        ret.y = Double.MAX_VALUE;
        ret.z = Double.MAX_VALUE;
        for (Triangle t : triangles) {
            for (int i = 0; i < 3; i++) {
                if (ret.x > t.point[i].x) {
                    ret.x = t.point[i].x;
                }
                if (ret.y > t.point[i].y) {
                    ret.y = t.point[i].y;
                }
                if (ret.z > t.point[i].z) {
                    ret.z = t.point[i].z;
                }
            }
        }
        return ret;
    }


    public Vector<Triangle> getTriangles() {
        return triangles;
    }

    /**
     * Translate all the triangle adding value of a {@link Vector3}
     *
     * @param translate {@link Vector3}
     */
    public void move(Vector3 translate) {
        for (Triangle t : triangles) {
            for (int i = 0; i < 3; i++) {
                t.point[i].addToSelf(translate);
            }
        }
    }

    /**
     * Convert ascii STL file to a {@link Vector} of {@link Triangle}.
     *
     * @param filename path of input file
     * @return {@link Vector} of {@link Triangle}
     * @throws IOException when bad input
     */
    private Vector<Triangle> readAsciiSTL(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        int i = 0;
        Vector3 normal = null;
        Triangle nextTri = new Triangle();
        Vector<Triangle> resultTriangles = new Vector<>();
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("facet normal")) {
                String[] parts = line.split(" +");
                normal = new Vector3(Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Double.parseDouble(parts[4]));
            }
            if (line.startsWith("vertex")) {
                String[] parts = line.split(" +");
                nextTri.point[i] = new Vector3(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
                i++;
                if (i == 3) {
                    //noinspection ConstantConditions
                    if ((normal.vSize2() > 0.1)) {
                        nextTri.getNormal().dot(normal);
                    }
                    // Triangle winding order and normal don't point in the same direction...
                    // Flip the triangle?
                    resultTriangles.add(nextTri);
                    nextTri = new Triangle();
                    i = 0;
                }
            }
        }
        br.close();
        return resultTriangles;
    }

    /**
     * Convert binary STL file to a {@link Vector} of {@link Triangle}.
     *
     * @param filename path of input file
     * @return {@link Vector} of {@link Triangle}
     * @throws IOException when bad input
     */
    private Vector<Triangle> readBinarySTL(String filename) throws IOException {
        Logger.message("ReadBinarySTL starts");
        RandomAccessFile raf = new RandomAccessFile(filename, "r");
        byte[] header = new byte[80];
        raf.read(header);
        int triangleCount = Integer.reverseBytes(raf.readInt());
        Vector<Triangle> resultTriangles = new Vector<>();
        for (int i = 0; i < triangleCount; i++) {
            Logger.setProgress(i, triangleCount - 1);
            for (int j = 0; j < 3; j++) {
                raf.readFloat();
            }

            Triangle t = new Triangle();
            float x = Float.intBitsToFloat(Integer.reverseBytes(raf.readInt()));
            float y = Float.intBitsToFloat(Integer.reverseBytes(raf.readInt()));
            float z = Float.intBitsToFloat(Integer.reverseBytes(raf.readInt()));
            t.point[0] = new Vector3(x, y, z);
            x = Float.intBitsToFloat(Integer.reverseBytes(raf.readInt()));
            y = Float.intBitsToFloat(Integer.reverseBytes(raf.readInt()));
            z = Float.intBitsToFloat(Integer.reverseBytes(raf.readInt()));
            t.point[1] = new Vector3(x, y, z);
            x = Float.intBitsToFloat(Integer.reverseBytes(raf.readInt()));
            y = Float.intBitsToFloat(Integer.reverseBytes(raf.readInt()));
            z = Float.intBitsToFloat(Integer.reverseBytes(raf.readInt()));
            t.point[2] = new Vector3(x, y, z);
            raf.readShort();// flags
            resultTriangles.add(t);
        }
        raf.close();
        return resultTriangles;
    }

    /**
     * Rotate all triangles
     *
     * @param r angle
     */
    public void rotate(Rotation r) {
        for (Triangle t : triangles) {
            rotateTriangle(t, r);
        }
    }

    private void rotateTriangle(Triangle t, Rotation rot) {
        for (Vector3 p : t.point) {
            Vec v = rot.rotate(new Vec((float) p.x, (float) p.y, (float) p.z));
            p.x = v.x();
            p.y = v.y();
            p.z = v.z();
        }
    }

    public void translate(Vector3 trans) {
        position.x += trans.x;
        position.y += trans.y;
        position.z += trans.z;
    }

    public Vector3 getPos() {
        return position;
    }

    public void setPos(Vector3 t) {
        position = t;
    }

    public void applyScale(float scaling) {
        for (Triangle t : triangles) {
            scaleTriangle(t, scaling);
        }
    }

    private void scaleTriangle(Triangle t, float scaling) {
        for (Vector3 p : t.point) {
            p.addToSelf(new Vector3(p.x*scaling-p.x,p.y*scaling-p.y,p.z*scaling-p.z));
        }
    }
}
