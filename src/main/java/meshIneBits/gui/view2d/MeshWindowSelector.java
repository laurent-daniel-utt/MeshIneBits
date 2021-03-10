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

import meshIneBits.Mesh;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MeshWindowSelector extends JPanel implements PropertyChangeListener {
    private JSlider layerSlider;
    private JSpinner layerSpinner;

    public MeshWindowSelector(MeshController meshController) {
        Mesh mesh = meshController.getMesh();
        meshController.addPropertyChangeListener(MeshController.SETTING_LAYER, this);

        layerSlider = new JSlider(
                SwingConstants.VERTICAL,
                0,
                mesh.getLayers().size() - 1,
                0);
        //layerSlider.setMinimumSize(new Dimension(20,200));
        //layerSlider.setMaximumSize(new Dimension(20,200));
        layerSlider.setFocusable(false);

        layerSpinner = new JSpinner(
                new SpinnerNumberModel(
                        0,
                        0,
                        mesh.getLayers().size() - 1,
                        1));
        layerSpinner.setMaximumSize(new Dimension(100,50));

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(layerSlider);
        add(layerSpinner);
        setBorder(new EmptyBorder(0, 5, 5, 0));

        layerSpinner.addChangeListener(e ->
        {
            meshController.setLayer((int) layerSpinner.getValue());
            layerSlider.setValue((int) layerSpinner.getValue());
        });

        layerSlider.addChangeListener(e ->
        {
            meshController.setLayer(layerSlider.getValue());
            layerSpinner.setValue(layerSlider.getValue());
        });
        setOpaque(false);
        layerSpinner.setOpaque(false);
        layerSpinner.setOpaque(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(MeshController.SETTING_LAYER)
                && ((int) evt.getNewValue() != layerSlider.getValue())) {
            layerSlider.setValue((int) evt.getNewValue());
        }
    }
}
