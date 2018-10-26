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

import meshIneBits.gui.MeshAction;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.MouseEvent;

public class ToggleIcon extends JToggleButton {
    private boolean on = false;
    private final Border onBorder = BorderFactory.createLoweredBevelBorder();
    private final Border offBorder = BorderFactory.createRaisedBevelBorder();

    public ToggleIcon(MeshAction meshAction) {
        super(meshAction);
        setHideActionText(true);
        setToolTipText((String) meshAction.getValue(Action.NAME));
        setContentAreaFilled(false);
        setBorder(offBorder);
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setContentAreaFilled(true);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setContentAreaFilled(false);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                setFocusPainted(false);
                if (on) {
                    on = false;
                    setBorder(offBorder);
                } else {
                    on = true;
                    setBorder(onBorder);
                }
            }
        });
    }
}
