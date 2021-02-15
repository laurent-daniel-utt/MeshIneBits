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

package meshIneBits.gui.utilities;

import meshIneBits.gui.view2d.MeshAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ButtonIcon extends JButton {
    private static final long serialVersionUID = 4439705350058229259L;

    public ButtonIcon(String label, String iconName) {
        this(label, iconName, false);
    }

    public ButtonIcon(String label, String iconName, boolean onlyIcon) {
        this(label, iconName, onlyIcon, 22, 22);
    }

    public ButtonIcon(String label, String iconName, boolean onlyIcon, int width, int height) {
        super((label.isEmpty() ? "" : " ") + label);
        this.setHorizontalAlignment(LEFT);
        this.setMargin(new Insets(0, 0, 0, 2));
        this.setIcon(IconLoader.get(iconName, width, height));

        if (onlyIcon) {
            setContentAreaFilled(false);
            setBorder(new EmptyBorder(3, 3, 3, 3));

            // Actions listener
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    setContentAreaFilled(true);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    setContentAreaFilled(false);
                }
            });
        }
    }

    public ButtonIcon(MeshAction meshAction) {
        super(meshAction);
        setHideActionText(true);
        setToolTipText(meshAction.getToolTipText());
        setContentAreaFilled(true);
        setBorder(new EmptyBorder(3, 3, 3, 3));
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setContentAreaFilled(true);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setContentAreaFilled(false);
            }
        });
    }
}