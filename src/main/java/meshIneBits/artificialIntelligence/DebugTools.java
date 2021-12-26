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

package meshIneBits.artificialIntelligence;

import meshIneBits.Bit2D;
import meshIneBits.util.Polygon;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Vector;

/**
 * DebugTools is useful for debugging.
 * Was used with Genetics and NN Pavement.
 * <p>
 * Set its variables depending on what you want to draw, and call DebugTools.setPaintForDebug(true);
 * It will draw on the screen what is stored in the variables of DebugTools.
 * You may have to modifiy the method AIpaintForDebug in MeshWindowsCore to paint exactly what you need.
 *
 * @see meshIneBits.gui.view2d.MeshWindowCore#AIpaintForDebug
 */
@SuppressWarnings("unused")
public final class DebugTools {

    public static @NotNull AffineTransform transformArea = new AffineTransform();

    public static @NotNull Vector<Vector2> pointsToDrawRED = new Vector<>();
    public static @NotNull Vector<Vector2> pointsToDrawGREEN = new Vector<>();
    public static @NotNull Vector<Vector2> pointsToDrawBLUE = new Vector<>();
    public static @NotNull Vector<Vector2> pointsToDrawORANGE = new Vector<>();

    public static @NotNull Path2D cutPathToDraw = new Path2D.Double();

    public static @NotNull Vector<Segment2D> segmentsToDraw = new Vector<>();
    public static @NotNull Segment2D currentSegToDraw = new Segment2D(new Vector2(0, 0), new Vector2(0, 0));
    public static @NotNull Segment2D currentSegToDraw2 = new Segment2D(new Vector2(0, 0), new Vector2(0, 0));

    public static @NotNull Polygon poly = new Polygon();

    public static @NotNull Area area = new Area();
    public static @Nullable Area areaToDraw = null;

    public static @NotNull Vector<Bit2D> Bits = new Vector<>();

    /**
     * Authorize (or not) to paint on the screen what is stored in the variables of DebugTools.
     */
    public static void setPaintForDebug(boolean b) {
        AI_Tool.getMeshController().AI_NeedPaint = b;
    }
}
