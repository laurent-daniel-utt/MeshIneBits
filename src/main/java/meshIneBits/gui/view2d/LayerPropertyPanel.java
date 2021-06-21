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

import meshIneBits.Layer;
import meshIneBits.util.Logger;

/**
 * LayerPropertyPanel set the informations that will be display when clicking on a Bit2D in 2D View
 * It is used In {@link MeshWindowPropertyPanel}.
 */
public class LayerPropertyPanel extends PropertyPanel {
    private static final String LAYER_INDEX = "Layer Index";
    private static final String LAYER_IS_PAVED = "Is Paved?";
    private static final String LAYER_PATTERN = "Pattern Applied";
    private static final String LAYER_BITS = "Bits";
    private static final String LAYER_IRREGULAR_BITS = "Irregular Bits";
    private static final String LAYER_LOWER_ALTITUDE = "Lower Altitude";
    private static final String LAYER_HIGHER_ALTITUDE = "Higher Altitude";
    private static final String LAYER_SLICE_ALTITUDE = "Slice Altitude";

    /**
     * Construct layerPropertyPanel.
     */
    LayerPropertyPanel() {
        super("Current Layer");
        initTable(getPropertiesOf(null));
    }

    /**
     * Set value of wanted informations as a String.
     *
     * @param layer the layer we want to get informations
     */
    private static String[][] getPropertiesOf(Layer layer) {
        if (layer == null)
            // Default value
            return new String[][]{
                    {LAYER_INDEX, "UNSET"},
                    {LAYER_IS_PAVED, "FALSE"},
                    {LAYER_PATTERN, "NONE"},
                    {LAYER_BITS, "0"},
                    {LAYER_IRREGULAR_BITS, "0"},
                    {LAYER_LOWER_ALTITUDE, "UNKNOWN"},
                    {LAYER_HIGHER_ALTITUDE, "UNKNOWN"},
                    {LAYER_SLICE_ALTITUDE, "UNKNOWN"}
            };
        else
            return new String[][]{
                    {LAYER_INDEX, String.valueOf(layer.getLayerNumber())},
                    {LAYER_IS_PAVED, String.valueOf(layer.isPaved())},
                    {LAYER_PATTERN,
                            layer.getPatternTemplate() == null ? "None" : layer.getPatternTemplate().getCommonName()},
                    {LAYER_BITS,
                            layer.getFlatPavement() == null ? "0" : String.valueOf(layer.getBits3dKeys().size())},
                    {LAYER_IRREGULAR_BITS,
                            layer.getFlatPavement() == null ? "0" : String.valueOf(layer.getKeysOfIrregularBits().size())},
                    {LAYER_LOWER_ALTITUDE, String.valueOf(layer.getLowerAltitude())},
                    {LAYER_HIGHER_ALTITUDE, String.valueOf(layer.getHigherAltitude())},
                    {LAYER_SLICE_ALTITUDE, String.valueOf(layer.getHorizontalSection().getAltitude())}
            };
    }

    @Override
    public void updateProperties(Object object) {
        try {
            String[][] properties = getPropertiesOf((Layer) object);
            for (String[] property : properties) {
                updateProperty(property[0], property[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
        }
    }
}
