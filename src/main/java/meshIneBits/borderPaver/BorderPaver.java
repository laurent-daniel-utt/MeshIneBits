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

package meshIneBits.borderPaver;

import meshIneBits.Bit2D;
import meshIneBits.borderPaver.util.GeneralTools;
import meshIneBits.borderPaver.util.Placement;
import meshIneBits.borderPaver.util.Section;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Vector2;

import java.util.Vector;

public class BorderPaver {

    public static Placement getBit(Section sectionPoints, double minWidthToKeep, Slice slice, Vector<Vector2> bound) {
        Vector2 startPoint = sectionPoints.getStartPoint();

        int convexType = sectionPoints.getConvexType(startPoint);

        if (convexType == Section.CONVEX_TYPE_CONVEX) {
            System.out.println("CONVEEEEEEEEEEX");
            return BorderedPatternAlgorithm.getBitPlacement(sectionPoints, AreaTool.getAreaFrom(slice), minWidthToKeep);//auto set the nextStartPoint
        } else {
            System.out.println("CONCAAAAAAAAAVE");//todo harmoniser
            Bit2D bit = TangenceAlgorithm.getBitFromSectionWithTangence(sectionPoints.getPoints(), startPoint, minWidthToKeep, convexType);
            Placement placement = new Placement(bit);
            placement.setNextStartPoint(GeneralTools.getBitAndContourSecondIntersectionPoint(bit, bound, startPoint));
            return placement;
        }
    }
}