/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 Vallon BENJAMIN.
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

import meshIneBits.Bit2D;
import meshIneBits.Pavement;
import meshIneBits.config.CraftConfig;

import java.awt.geom.Area;
import java.util.Vector;

/**
 * To determine if a bit is not regular (not having a lift point)
 *
 * @author NHATHAN
 */
public class DetectorTool {

    /**
     * @param pavement container of all bits in a slice, already computed (limited by
     *                 boundary)
     * @return all irregular bits in the given layer
     */
    public static Vector<Vector2> detectIrregularBits(Pavement pavement) {
        Vector<Vector2> result = new Vector<>();
        for (Vector2 bitKey : pavement.getBitsKeys()) {
            Bit2D bit = pavement.getBit(bitKey);
            if (checkIrregular(bit)) {
                result.add(bitKey);
            }
        }
        return result;
    }

    /**
     * Check if a bit is irregular.
     * <ol>
     * <li>Only one area per bit</li>
     * <li>Area is large enough to contain one lift point</li>
     * </ol>
     *
     * @param bit target
     * @return <tt>true</tt> if this bit is irregular, <tt>false</tt> otherwise.
     */
    public static boolean checkIrregular(Bit2D bit) {
        // int numLiftPoints = bit.computeLiftPoints().size(), numLevel0Areas =
        // bit.getRawAreas().size();
        // return (numLiftPoints != numLevel0Areas);
        if (bit.getRawAreas().size() != 1)
            return true;
        return bit.computeLiftPoint() == null;
    }

    /**
     * Check if an area is irregular
     *
     * @param area closed zone
     * @return <tt>true</tt> if the surface has more than 1 continuous surface,
     * or no possible lift point or <tt>area</tt> is <tt>null</tt> or empty
     */
    public static boolean checkIrregular(Area area) {
        if (area == null || area.isEmpty()) return true;
        if (AreaTool.segregateArea(area).size() > 1) return true;
        return AreaTool.getLiftPoint(area, CraftConfig.suckerDiameter / 2) == null;
    }
}