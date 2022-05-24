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

package meshIneBits.borderPaver.util;

import meshIneBits.Bit2D;
import meshIneBits.util.Vector2;

/**
 * The description of the placement of a {@link Bit2D} : the bit2D, the section covered by it
 * and the last point of the section.
 */
public class Placement {
    public final Bit2D bit2D;
    public Section sectionCovered;
    public Vector2 nextStartPoint;

    public Placement(Bit2D bit2D) {
        this.bit2D = bit2D;
    }

    public void setNextStartPoint(Vector2 nextStartPoint) {
        this.nextStartPoint = nextStartPoint;
    }

    public void setSectionCovered(Section sectionCovered) {
        this.sectionCovered = sectionCovered;
    }
}
