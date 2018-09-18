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

package meshIneBits.gui.utilities;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Container for options of a module.
 */
public class OptionsContainer extends JPanel {

    private static final long serialVersionUID = 136154266552080732L;

    public OptionsContainer(String title) {
        // Visual options
        this.setMinimumSize(new Dimension(500, 500));
        this.setLayout(new GridLayout(3, 0, 10, 0));
        this.setBackground(Color.WHITE);

        TitledBorder centerBorder = BorderFactory.createTitledBorder(title);
        centerBorder.setTitleJustification(TitledBorder.CENTER);
        centerBorder.setTitleFont(new Font(this.getFont().toString(), Font.BOLD, 12));
        centerBorder.setTitleColor(Color.gray);
        centerBorder.setBorder(BorderFactory.createEmptyBorder());
        this.setBorder(centerBorder);

    }

    @Override
    public int getBaseline(int width, int height) {
        return 0;
    }

    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior() {
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }
}