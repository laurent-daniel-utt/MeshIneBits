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

package meshIneBits.config;

import java.awt.*;

public class WorkspaceConfig {
    /**
     * Increase rotation when scrolling mouse wheel
     */
    public static int rotationSpeed = 5;
    /**
     * Faster/Slower zooming. <tt>1</tt> means to never zoom
     */
    public static double zoomSpeed = 1.25;
    /**
     * Previous layer
     */
    public static Color previousLayerColor = Color.DARK_GRAY;
    /**
     * Realisable bit
     */
    public static Color regularBitColor = new Color(164, 180, 200, 200);
    /**
     * Irrealisable bit
     */
    public static Color irregularBitColor = new Color(255, 0, 0, 100);
    public static Color cutpathColor = Color.BLUE.darker();
    public static Stroke cutpathStroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    public static Color liftpointColor = Color.RED;
    public static Stroke liftpointStroke = new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    public static Color layerBorderColor = Color.BLACK;
    public static Stroke layerBorderStroke = new BasicStroke(1.0f);
    public static Color bitControlColor = new Color(94, 125, 215);
    public static int paddingBitControl = 15;
    public static Color bitPreviewBorderColor = Color.BLUE.darker();
    public static Stroke bitPreviewBorderStroke = new BasicStroke(1.1f);
    public static Color bitPreviewColor = new Color(164, 180, 200, 100);
    public static Color irregularBitPreviewBorderColor = Color.RED.darker();
    public static Stroke irregularBitPreviewBorderStroke = new BasicStroke(1.1f);
    public static Color irregularBitPreviewColor = new Color(250, 0, 100, 100);
    public static Color vertexColor = new Color(255, 0, 100, 200);
    public static int vertexRadius = 10;
    public static Color regionColor = new Color(255, 0, 100, 100);
    public static Stroke regionStroke = new BasicStroke(2f);
    public static Color bulkSelectZoneColor = new Color(200, 180, 255, 250);
    public static Stroke bulkSelectZoneStroke = new BasicStroke(1.2f);
}
