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

package meshIneBits.gui;

import meshIneBits.Mesh;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

class MeshWindowSliceSelector extends JPanel {
    private JSlider sliceSlider;
    private JSpinner sliceSpinner;
    private Mesh mesh;

    MeshWindowSliceSelector(MeshController meshController) {
        mesh = meshController.getMesh();
        sliceSlider = new JSlider(
                SwingConstants.VERTICAL,
                0,
                mesh.getSlices().size() - 1,
                0);
        sliceSpinner = new JSpinner(
                new SpinnerNumberModel(
                        0,
                        0,
                        mesh.getSlices().size() - 1,
                        1));
        sliceSlider.setMaximumSize(new Dimension(40, 500));
        sliceSlider.setFocusable(false);
        sliceSpinner.setMaximumSize(new Dimension(40, 40));

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(sliceSlider);
        add(sliceSpinner);
        setBorder(new EmptyBorder(0, 5, 5, 0));

        sliceSpinner.addChangeListener(e ->
        {
            meshController.setSlice((Integer) sliceSpinner.getValue());
            sliceSlider.setValue((Integer) sliceSpinner.getValue());
        });

        sliceSlider.addChangeListener(e ->
        {
            meshController.setSlice(sliceSlider.getValue());
            sliceSpinner.setValue(sliceSlider.getValue());
        });
        setVisible(true);
    }

    public Mesh getMesh() {
        return mesh;
    }
}
