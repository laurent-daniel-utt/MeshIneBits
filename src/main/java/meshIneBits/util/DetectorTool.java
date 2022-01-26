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

package meshIneBits.util;

import meshIneBits.Bit2D;
import meshIneBits.Bit3D;
import meshIneBits.Pavement;
import meshIneBits.config.CraftConfig;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

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
    public static List<Vector2> detectIrregularBits(Pavement pavement) {
        List<Vector2> result = new ArrayList<>();
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
     * <li>Can have multiple separated areas.</li>
     * <li>Each area is large enough to contain one lift point</li>
     * </ol>
     *
     * @param bit target
     * @return <tt>true</tt> if this bit is irregular, <tt>false</tt> otherwise.
     */
    public static boolean checkIrregular(Bit2D bit) {
        return checkIrregular(bit.getArea());
    }

    public static boolean checkIrregular(Bit3D bit3D) {
        return bit3D.isIrregular();
    }

    /**
     * Check if an area is irregular
     *
     * @param area closed zone
     * @return <tt>true</tt> if a sub surface has no lift point
     * or <tt>area</tt> is <tt>null</tt> or empty
     */
    public static boolean checkIrregular(Area area) {
        if (area == null || area.isEmpty()) return true;
        return AreaTool.segregateArea(area)
                .stream()
                .anyMatch(subarea ->
                        AreaTool.getLiftPoint(
                                area,
                                CraftConfig.suckerDiameter / 2) == null);
    }
}