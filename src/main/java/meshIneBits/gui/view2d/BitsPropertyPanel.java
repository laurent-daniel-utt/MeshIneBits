/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas..
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

import meshIneBits.Bit3D;
import meshIneBits.Mesh;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

class BitsPropertyPanel extends JPanel {
    private LinkedList<Bit3D> bit3Ds = new LinkedList<>();
    private Map<Bit3D, BitPropertyPanel> bitPropertyPanelsMap = new HashMap<>();
    private GridBagConstraints defaultGBC = new GridBagConstraints();
    private TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            TITLE_PREFIX + bit3Ds.size(),
            TitledBorder.CENTER,
            TitledBorder.TOP);
    private static final String TITLE_PREFIX = "Selected bits: ";
    private GridBagLayout gbc = new GridBagLayout();
    private Mesh mesh;


    BitsPropertyPanel(Mesh m) {
        setBorder(titledBorder);
        this.setLayout(gbc);
        defaultGBC.fill = GridBagConstraints.HORIZONTAL;
        defaultGBC.weightx = 1;
        defaultGBC.weighty = 0;
        defaultGBC.gridx = 0;
        defaultGBC.gridy = 1000;
        this.setOpaque(false);
        this.mesh = m;
    }

    void selectBits(Collection<Bit3D> newBit3Ds) {
        // Bulk reset
        bit3Ds.clear();
        bitPropertyPanelsMap.clear();
        defaultGBC.gridy = 1000;
        removeAll();
        // Add
        newBit3Ds.forEach(this::selectBit);
    }


    void unselectBits(Collection<Bit3D> bit3Ds) {
        this.bit3Ds.removeAll(bit3Ds);
        this.bitPropertyPanelsMap.forEach((bit3D, bitPropertyPanel) -> remove(bitPropertyPanel));
        bit3Ds.forEach(this.bitPropertyPanelsMap::remove);
        updateTitle();
    }

    void unselectBit(Bit3D bit3D) {
        bit3Ds.remove(bit3D);
        BitPropertyPanel bitPropertyPanel = bitPropertyPanelsMap.get(bit3D);
        if (bitPropertyPanel != null)
            remove(bitPropertyPanel);
        updateTitle();
    }

    void selectBit(Bit3D bit3D) {
        bit3Ds.addFirst(bit3D);
        BitPropertyPanel bitPropertyPanel = new BitPropertyPanel(bit3D, mesh);
        bitPropertyPanelsMap.put(bit3D, bitPropertyPanel);
        // Add into layout
        if (defaultGBC.gridy == 0)
            shiftPanels();
        defaultGBC.gridy--;
        add(bitPropertyPanel, defaultGBC);
        updateTitle();
    }

    private void shiftPanels() {
        for (Component panel : getComponents()) {
            GridBagConstraints c = gbc.getConstraints(panel);
            c.gridy += 1000;
            gbc.setConstraints(panel, c);
        }
        defaultGBC.gridy += 1000;
    }

    private void updateTitle() {
        titledBorder.setTitle(TITLE_PREFIX + bit3Ds.size());
    }
}
