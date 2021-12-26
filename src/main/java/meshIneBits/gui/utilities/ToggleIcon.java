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
 * Copyright (C) 2020 CLAIRIS Etienne & RUSSO André.
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
import meshIneBits.gui.view2d.MeshToggleAction;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ToggleIcon extends JToggleButton implements PropertyChangeListener {
    private final Border onBorder = BorderFactory.createLoweredBevelBorder();
    private final Border offBorder = BorderFactory.createRaisedBevelBorder();
    private String bindProperty;

    ToggleIcon(MeshToggleAction meshToggleAction) {
        super(meshToggleAction);
        bindProperty = meshToggleAction.getBindProperty();

        setHideActionText(true);
        setToolTipText(meshToggleAction.getToolTipText());
        setContentAreaFilled(false);
        meshToggleAction.getMeshController()
                .addPropertyChangeListener(meshToggleAction.getBindProperty(), this);
        setSelected(meshToggleAction.getMeshController().get(bindProperty));
        setFocusPainted(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                setContentAreaFilled(true);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                setContentAreaFilled(false);
            }
        });
    }

    public ToggleIcon(MeshAction meshAction) {
        super(meshAction);
        bindProperty = "";
        setHideActionText(true);
        setToolTipText(meshAction.getToolTipText());
        setContentAreaFilled(false);
        setFocusPainted(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                setContentAreaFilled(true);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                setContentAreaFilled(false);
            }
        });
    }

    private void toggleBorder() {
        if (isSelected())
            setBorder(onBorder);
        else
            setBorder(offBorder);
    }

    @Override
    public void setSelected(boolean b) {
        super.setSelected(b);
        toggleBorder();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(bindProperty))
            setSelected((Boolean) evt.getNewValue());
    }
}