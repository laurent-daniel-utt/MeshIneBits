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

package meshIneBits.artificialIntelligence.util;

import meshIneBits.artificialIntelligence.AI_Tool;
import meshIneBits.artificialIntelligence.GeneralTools;
import meshIneBits.artificialIntelligence.Section;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * Generates a .csv dataset
 * format of a line : edge abscissa, bit's angle, point 0 x, point 0 y, point 1 x, point 1 y,...
 */
public final class DataSetGenerator {

    /**
     * Generates a .csv file that can be used by a neural network
     */
    public static void generateCsvFile() throws IOException {

        long dataLogSize;
        dataLogSize = DataLogger.getNumberOfEntries();
        FileWriter fw = new FileWriter(AI_Tool.DATASET_FILE_PATH, false);

        for (int logLine = 1; logLine <= dataLogSize; logLine++) {

            DataLogEntry entry = DataLogger.getEntryFromFile(logLine);

            // format data for deep learning
            Vector2 startPoint = entry.getAssociatedPoints().firstElement();
//todo pareil créer une Section à chaque fois (3 fois ici)
            Vector<Vector2> transformedPoints = SectionTransformer.getGlobalSectionInLocalCoordinateSystem(
                    entry.getAssociatedPoints(),
                    SectionTransformer.getLocalCoordinateSystemAngle(new Section(entry.getAssociatedPoints())),
                    startPoint); //TODO @Etienne TESTER
            Vector<Vector2> pointsForDl = GeneralTools.getInputPointsForDL(new Section(transformedPoints));

            double edgeAbscissa = getBitEdgeAbscissa(entry.getBitPosition(), entry.getBitOrientation(), startPoint);
            double bitOrientation = getBitAngleInLocalSystem(entry.getBitOrientation(), new Section(entry.getAssociatedPoints()));

            //generates one line (corresponds to the data of one bit)
            StringBuilder csvLine = new StringBuilder(""
                    + edgeAbscissa // adds position
                    + ","
                    + bitOrientation); //adds bitOrientation
            for (Vector2 point : pointsForDl) { // add points
                csvLine.append(",").append(point.x);
                csvLine.append(",").append(point.y);
            }

            fw.write(csvLine + "\n");
        }

        fw.close();
    }


    /**
     * This method convert a bit position (x, y) to another expression of position (called edgeAbscissa) related to
     * a startPoint and an abscissa related to one bit's vertex. This is helpful for automatic placement on
     * slice's borders,because it guarantees that the bit covers the startPoint, as bit's position is related to it.
     *
     * @param bitOrigin  usual (x, y) coordinates of bit's center
     * @param bitAngle   bit's angle as a vector
     * @param startPoint the startPoint on which the new bit's end should be placed : the intersection between the
     *                   slice border and the last placed bit's end edge
     * @return the edgeAbscissa
     */
    public static double getBitEdgeAbscissa(@NotNull Vector2 bitOrigin, @NotNull Vector2 bitAngle, Vector2 startPoint) {

        Vector2 collinear = bitAngle.normal();
        Vector2 orthogonal = collinear.rotate(new Vector2(0, -1)); // 90deg anticlockwise rotation

        //this point is used as a local origin for the new coordinate system
        Vector2 originVertex = bitOrigin.add(orthogonal.mul(CraftConfig.bitWidth / 2))
                .sub(collinear.mul(CraftConfig.lengthFull / 2));

        if (Vector2.dist(originVertex, startPoint) > CraftConfig.bitWidth) {
            //this case is not possible. this means that originVertex should be the opposite point compared to bitOrigin
            originVertex = bitOrigin.sub(orthogonal.mul(CraftConfig.bitWidth / 2))
                    .add(collinear.mul(CraftConfig.lengthFull / 2));
        }

        // edgeAbscissa is the distance between the startPoint and the originVertex
        // the startPoint on which the new bit should be placed : the intersection between the slice border
        // and the last placed bit's end edge

        // todo @Andre: this suppose that the bit's end edge is already placed on the start point. How can we be sure of that ?
        return Vector2.dist(originVertex, startPoint);
    }

    /**
     * @param bitAngle      the angle of the bit in the global coordinate system
     * @param sectionPoints the points saved by DataLogger
     * @return the angle of the bit in the local coordinate system. Note that the angle is expressed
     * between -90 and 90. Otherwise, as the orientation is expressed in regard to the center of the bit,
     * the angles -100 and -10 degrees (for example) would have been equivalent.
     */
    private static double getBitAngleInLocalSystem(@NotNull Vector2 bitAngle, Section sectionPoints) {
        Vector2 localCoordinateSystemAngle = Vector2.getEquivalentVector(SectionTransformer.getLocalCoordinateSystemAngle(sectionPoints));

        System.out.println("localCoordinateSystemAngle = " + localCoordinateSystemAngle.getEquivalentAngle2());

        // = if angle between the vector is more than 90 degrees
        // (the point of rotation is the center of the bit, so both angles are equivalent)
        if (bitAngle.dot(localCoordinateSystemAngle) < 0) {
            bitAngle = new Vector2(-bitAngle.x, -bitAngle.y);
        }

        double x1 = localCoordinateSystemAngle.x;
        double y1 = localCoordinateSystemAngle.y;
        double x2 = bitAngle.x;
        double y2 = bitAngle.y;
        double l1 = localCoordinateSystemAngle.vSize();
        double l2 = bitAngle.vSize();
        double bitAngleLocal = Math.asin((x1 * y2 - y1 * x2) / (l1 * l2));

        return Math.toDegrees(bitAngleLocal);
    }
}
