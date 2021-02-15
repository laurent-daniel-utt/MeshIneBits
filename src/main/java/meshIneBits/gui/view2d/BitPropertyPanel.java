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

package meshIneBits.gui.view2d;

import meshIneBits.Bit3D;
import meshIneBits.Mesh;
import meshIneBits.util.Logger;

public class BitPropertyPanel extends PropertyPanel {
    private static final String ORIGIN = "Origin";
    private static final String ORIENTATION = "Orientation";
    private static final String LIFT_POINTS = "Lift points";
    private static final String IS_IRREGULAR = "Is irregular";
    private static final String BATCH_ID = "Batch id";
    private static final String PLATE_ID = "Plate id";
    private static final String BIT_NUMBER = "Bit id";
    private static  Mesh mesh = null;

    BitPropertyPanel(Bit3D bit3D) {
        super("Bit3D@" + bit3D.hashCode());
        initTable(getPropertiesOf(bit3D));
    }

    BitPropertyPanel(Bit3D bit3D, Mesh m) {
        super("Bit3D@" + bit3D.hashCode());
        mesh = m;
        initTable(getPropertiesOf(bit3D));
    }

    private static String[][] getPropertiesOf(Bit3D bit3D) {
        if (bit3D == null)
            return new String[][]{
                    {ORIGIN, "UNKNOWN"},
                    {ORIENTATION, "UNKNOWN"},
                    {LIFT_POINTS, "UNKNOWN"},
                    {IS_IRREGULAR, "UNKNOWN"},
                    {PLATE_ID, "UNKNOWN"},
                    {BIT_NUMBER, "UNKNOWN"},
                    {BATCH_ID, "UNKNOWN"}
            };
        else if (mesh.getScheduler() != null && !mesh.getScheduler().getSortedBits().isEmpty())
        {
            return new String[][]{
                    {ORIGIN, bit3D.getOrigin().toString()},
                    {ORIENTATION, String.valueOf(bit3D.getOrientation().getEquivalentAngle2())},
                    {LIFT_POINTS, bit3D.getLiftPoints().toString()},
                    {IS_IRREGULAR, String.valueOf(bit3D.isIrregular())},
                    {BIT_NUMBER, String.valueOf(mesh.getScheduler().getBitIndex(bit3D))},
                    {PLATE_ID,  String.valueOf(mesh.getScheduler().getSubBitPlate(bit3D))},
                    {BATCH_ID, String.valueOf(mesh.getScheduler().getSubBitBatch(bit3D))}
            };
        }
        else
            return new String[][]{
                    {ORIGIN, bit3D.getOrigin().toString()},
                    {ORIENTATION, String.valueOf(bit3D.getOrientation().getEquivalentAngle2())},
                    {LIFT_POINTS, bit3D.getLiftPoints().toString()},
                    {IS_IRREGULAR, String.valueOf(bit3D.isIrregular())},
                    {PLATE_ID, "UNKNOWN"},
                    {BIT_NUMBER, "UNKNOWN"},
                    {BATCH_ID, "UNKNOWN"}

            };
    }

    @Override
    public void updateProperties(Object object) {
        try {
            String[][] properties = getPropertiesOf((Bit3D) object);
            for (String[] property : properties) {
                updateProperty(property[0], property[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
        }
    }
}
