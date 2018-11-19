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

import meshIneBits.Mesh;
import meshIneBits.Model;
import meshIneBits.util.Logger;

import java.io.File;

class MeshPropertyPanel extends PropertyPanel {
    static final String MODEL_POSITION = "Model Position";
    static final String MODEL_NAME = "Model Name";
    static final String MODEL_PATH = "Model Path";
    static final String MODEL_NUMBER_OF_TRIANGLES = "Number of Triangles in Model";
    static final String MODEL_SKIRT_RADIUS = "Model Skirt Radius";
    static final String MESH_LAYERS = "Number of Layers";
    static final String MESH_STATE = "State";

    MeshPropertyPanel() {
        super("Current Mesh");
        initTable(getPropertiesOf(null));
    }

    /**
     * Retrieve properties. Each property is a couple of {@link String}: label
     * and value
     *
     * @param mesh target
     * @return list of properties
     */
    private static String[][] getPropertiesOf(Mesh mesh) {
        if (mesh == null)
            return new String[][]{
                    {MODEL_NAME, "UNDEFINED"},
                    {MODEL_PATH, "UNKNOWN"},
                    {MODEL_POSITION, "UNKNOWN"},
                    {MODEL_NUMBER_OF_TRIANGLES, "UNKNOWN"},
                    {MODEL_SKIRT_RADIUS, "UNKNOWN"},
                    {MESH_LAYERS, "UNKNOWN"},
                    {MESH_STATE, "UNKNOWN"}
            };
        else {
            File modelFile = new File(mesh.getModelFile());
            Model model = mesh.getModel();
            return new String[][]{
                    {MODEL_NAME, modelFile.getName()},
                    {MODEL_PATH, modelFile.getPath()},
                    {MODEL_POSITION, model.getPos().toString()},
                    {MODEL_NUMBER_OF_TRIANGLES, String.valueOf(model.getTriangles().size())},
                    {MODEL_SKIRT_RADIUS, String.valueOf(mesh.getSkirtRadius())},
                    {MESH_LAYERS, String.valueOf(mesh.getLayers().size())},
                    {MESH_STATE, String.valueOf(mesh.getState())}
            };
        }
    }

    @Override
    public void updateProperties(Object object) {
        try {
            String[][] properties = getPropertiesOf((Mesh) object);
            for (String[] property : properties) {
                updateProperty(property[0], property[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
        }
    }
}
