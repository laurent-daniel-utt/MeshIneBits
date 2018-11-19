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

package meshIneBits.gui.view2d;

import meshIneBits.Bit3D;
import meshIneBits.util.Logger;

public class BitPropertyPanel extends PropertyPanel {
    private static final String ORIGIN = "Origin";
    private static final String ORIENTATION = "Orientation";
    private static final String LIFT_POINTS = "Lift points";
    private static final String IS_IRREGULAR = "Is irregular";

    BitPropertyPanel(Bit3D bit3D) {
        super("Bit3D@" + bit3D.hashCode());
        initTable(getPropertiesOf(bit3D));
    }

    private static String[][] getPropertiesOf(Bit3D bit3D) {
        if (bit3D == null)
            return new String[][]{
                    {ORIGIN, "UNKNOWN"},
                    {ORIENTATION, "UNKNOWN"},
                    {LIFT_POINTS, "UNKNOWN"},
                    {IS_IRREGULAR, "UNKNOWN"}
            };
        else
            return new String[][]{
                    {ORIGIN, bit3D.getOrigin().toString()},
                    {ORIENTATION, String.valueOf(bit3D.getOrientation().getEquivalentAngle2())},
                    {LIFT_POINTS, bit3D.getLiftPoints().toString()},
                    {IS_IRREGULAR, String.valueOf(bit3D.isIrregular())}
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
