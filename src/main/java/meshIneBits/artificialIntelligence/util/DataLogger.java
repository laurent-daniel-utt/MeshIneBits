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

package meshIneBits.artificialIntelligence.util;

import meshIneBits.artificialIntelligence.AI_Tool;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Stream;

/**
 * Provides tools to save and read data from the csv DataSet file.
 */
public final class DataLogger {

    /**
     * Returns the number of line entries in the file.
     *
     * @return the number of entries.
     */
    static public long getNumberOfEntries() throws IOException {
        return Files.lines(Paths.get(AI_Tool.DATA_LOG_FILE_PATH)).count();
    }

    /**
     * Saves one entry in the dataSet file.
     *
     * @param dataLogEntry the entry to save.
     */
    static public void saveEntry(@NotNull DataLogEntry dataLogEntry) throws IOException {
        StringBuilder line = new StringBuilder(dataLogEntry.getBitPosition().x + ","
                + dataLogEntry.getBitPosition().y + ","
                + dataLogEntry.getBitOrientation().x + ","
                + dataLogEntry.getBitOrientation().y);

        for (Vector2 point : dataLogEntry.getAssociatedPoints()) {
            line.append(",").append(point.x).append(",").append(point.y);
        }
        FileWriter fw = new FileWriter(AI_Tool.DATA_LOG_FILE_PATH, true);
        fw.write(line + "\n");
        fw.close();
    }


    /**
     * Saves all entries in the dataSet file.
     *
     * @param dataSetEntries the list of entries to save.
     */
    @SuppressWarnings("unused")
    static public void saveAllEntries(@NotNull Vector<DataLogEntry> dataSetEntries) throws IOException {
        for (DataLogEntry dataLogEntry : dataSetEntries) {
            saveEntry(dataLogEntry);
        }
    }

    /**
     * Returns the entry at the given line in the dataSet file.
     *
     * @param lineNumber the line where to get the entry.
     * @return the DataLogEntry read.
     */
    static public @NotNull DataLogEntry getEntryFromFile(long lineNumber) throws IOException {
        Stream<String> lines = Files.lines(Paths.get(AI_Tool.DATA_LOG_FILE_PATH));
        Optional<String> str = lines.skip(lineNumber - 1).findFirst();
        String line = null;
        if (str.isPresent()) {
            line = str.get();
        }
        assert line != null;
        return decodeLine(line);
    }

    /**
     * Transforms a line in a DataLogEntry.
     *
     * @param line the line to be transformed.
     * @return the dataLogEntry.
     */
    static private @NotNull DataLogEntry decodeLine(@NotNull String line) {

        String[] dataStr = line.split(",");
        Vector<String> data = new Vector<>();
        Collections.addAll(data, dataStr);

        double bitPosX = Double.parseDouble(data.remove(0));
        double bitPosY = Double.parseDouble(data.remove(0));
        Vector2 bitPos = new Vector2(bitPosX, bitPosY);

        double bitOrientationX = Double.parseDouble(data.remove(0));
        double bitOrientationY = Double.parseDouble(data.remove(0));
        Vector2 bitOrientation = new Vector2(bitOrientationX, bitOrientationY);

        Vector<Vector2> points = new Vector<>();
        while (!data.isEmpty()) {
            points.add(new Vector2(
                    Double.parseDouble(data.remove(0)),
                    Double.parseDouble(data.remove(0))));
        }

        return new DataLogEntry(bitPos, bitOrientation, points);
    }

}
