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

package meshIneBits.config;
/**
 * Contain Xml tags for object {@link meshIneBits.Mesh}
 */
public class MeshTagXML {

    public static final String MESH_START = "mesh";
    public static final String MESH_NAME = "name";
    public static final String DATE = "date";

    public static final String MESH_CONFIG = "config";
    public static final String BIT_DIMENSION = "dimension-bit";
    public static final String BIT_HEIGHT = "height";
    public static final String BIT_WIDTH = "width";
    public static final String BIT_LENGTH = "length";

    public static final String PART_SKIRT = "part-skirt";
    public static final String PART_SKIRT_HEIGHT = "height";
    public static final String PART_SKIRT_RADIUS = "radius";


    public static final String LAYER = "layer";
    public static final String LAYER_HEIGHT = "layer-height";
    public static final String MOVE_WORKING_SPACE = "move-working-space";
    public static final String GO_TO = "go-to";
    public static final String RETURN = "return";

    public static final String BIT = "bit";
    public static final String BIT_ID = "id-bit";
    public static final String CUT_BIT = "cutting";
    public static final String NO_CUT_BIT = "no-cutting";
    public static final String CUT_PATHS = "cut-paths";
    public static final String MOVE_TO_POSITION = "move-to";
    public static final String CUT_TO_POSITION = "cut-to";

    public static final String SUB_BIT = "sub-bit";
    public static final String SUB_BIT_ID = "id";
    public static final String BATCH = "batch";
    public static final String BATCH_NUMBER = "batch-number";
    public static final String PLATE = "plate";
    public static final String POSITION_BIT_COORDINATE = "position-in-bit";
    public static final String POSITION_MESH_COORDINATE = "position-in-layer";
    public static final String ROTATION_SUB_BIT = "rotation";
    public static final String POINT ="point";
    public static final String POINT_ID ="id";
    public static final String ROTATION_SUB_BIT_SECOND ="rotation-2";

    public static final String COORDINATE_X ="x";
    public static final String COORDINATE_Y ="y";

    public static final String FALL_TYPE ="fall-type";
    public static final String CHUTE_TYPE ="chute";
    public static final String DROP="drop";


}
