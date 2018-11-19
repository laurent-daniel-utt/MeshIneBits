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

import meshIneBits.Layer;
import meshIneBits.util.Logger;

public class LayerPropertyPanel extends PropertyPanel {
    static final String LAYER_INDEX = "Layer Index";
    static final String LAYER_IS_PAVED = "Is Paved?";
    static final String LAYER_PATTERN = "Pattern Applied";
    static final String LAYER_BITS = "Bits";
    static final String LAYER_IRREGULAR_BITS = "Irregular Bits";

    LayerPropertyPanel(Layer layer) {
        super("Current Layer", getPropertiesOf(layer));
    }

    private static String[][] getPropertiesOf(Layer layer) {
        return new String[][]{
                {LAYER_INDEX, String.valueOf(layer.getLayerNumber())},
                {LAYER_IS_PAVED, String.valueOf(layer.isPaved())},
                {LAYER_PATTERN,
                        layer.getPatternTemplate() == null ? "None" : layer.getPatternTemplate().getCommonName()},
                {LAYER_BITS,
                        layer.getFlatPavement() == null ? "0" : String.valueOf(layer.getBits3dKeys().size())},
                {LAYER_IRREGULAR_BITS,
                        layer.getFlatPavement() == null ? "0" : String.valueOf(layer.getKeysOfIrregularBits().size())}
        };
    }

    @Override
    public void updateProperties(Object object, String msg) {
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
