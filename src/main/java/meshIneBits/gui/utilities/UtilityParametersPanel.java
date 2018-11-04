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

public class UtilityParametersPanel extends JPanel {

    public UtilityParametersPanel(String title) {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK),
                title);
        titledBorder.setTitleJustification(TitledBorder.LEFT);
        titledBorder.setTitleFont(new Font(this.getFont().toString(), Font.BOLD, 12));
        titledBorder.setTitleColor(Color.GRAY);
        this.setBorder(titledBorder);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), 100);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(getWidth(), 100);
    }
}
