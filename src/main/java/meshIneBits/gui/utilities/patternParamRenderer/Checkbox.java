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

package meshIneBits.gui.utilities.patternParamRenderer;

import meshIneBits.config.patternParameter.BooleanParam;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Render {@link BooleanParam}
 *
 * @author Quoc Nhat Han TRAN
 */
public class Checkbox extends Renderer {

    /**
     *
     */
    private static final long serialVersionUID = -6286431984037692066L;

    private JCheckBox checkbox;
    private BooleanParam config;

    /**
     * @param config predefined parameter
     */
    public Checkbox(BooleanParam config) {
        super();
        this.config = config;
        this.initGUI();
    }

    private void initGUI() {
        // Visual options
        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(4, 0, 0, 0));

        // Label
        JLabel lblName = new JLabel(config.getTitle());
        lblName.setToolTipText("<html><div>" + config.getDescription() + "</div></html>");
        this.add(lblName, BorderLayout.WEST);

        // Checkbox
        checkbox = new JCheckBox();
        checkbox.setSelected(config.getCurrentValue());
        checkbox.addActionListener(e -> config.setCurrentValue(checkbox.isSelected()));
        this.add(checkbox, BorderLayout.EAST);
    }
}