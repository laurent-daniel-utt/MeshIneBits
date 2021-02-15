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

import meshIneBits.Bit2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.util.Vector;

/**
 * A DataLogEntry contains the data read or the data to write in the dataSet file.
 * It contains the position and orientation of a bit, and the section of its associated points.
 */
public class DataLogEntry {
    private final Vector2 bitPosition;
    private final Vector2 bitOrientation;
    private final Vector<Vector2> associatedPoints;

    /**
     * A DataLogEntry contains the data read or the data to write in the dataSet file.
     * It contains the position and orientation of a bit, and the section of its associated points.
     *
     * @param bitPosition      the position of the bit.
     * @param bitOrientation   the orientation of the bit.
     * @param associatedPoints the points associated to the bit.
     */
    public DataLogEntry(Vector2 bitPosition, Vector2 bitOrientation, Vector<Vector2> associatedPoints) {
        this.bitPosition = bitPosition;
        this.bitOrientation = bitOrientation;
        this.associatedPoints = associatedPoints;

    }

    /**
     * A DataLogEntry contains the data read or the data to write in the dataSet file.
     * It contains the position and orientation of a bit, and the section of its associated points.
     *
     * @param bit2D            the bit.
     * @param associatedPoints the points associated to the bit.
     */
    public DataLogEntry(@NotNull Bit2D bit2D, Vector<Vector2> associatedPoints) {
        this.bitPosition = bit2D.getOrigin();
        this.bitOrientation = bit2D.getOrientation();
        this.associatedPoints = associatedPoints;
    }

    public Vector<Vector2> getAssociatedPoints() {
        return associatedPoints;
    }

    public Vector2 getBitPosition() {
        return bitPosition;
    }

    public Vector2 getBitOrientation() {
        return bitOrientation;
    }

    @Override
    public @NotNull String toString() {
        return "DataLogEntry{" +
                "bitPosition=" + bitPosition.toString() +
                ", bitOrientation=" + bitOrientation.toString() +
                ", points=" + associatedPoints +
                '}';
    }
}
