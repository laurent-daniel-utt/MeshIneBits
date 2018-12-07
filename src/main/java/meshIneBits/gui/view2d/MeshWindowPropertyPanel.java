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

import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Mesh;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

/**
 * Display properties of {@link Mesh}, {@link Layer} and {@link Bit3D}.
 * Observes {@link MeshController}.
 */
public class MeshWindowPropertyPanel extends JPanel implements PropertyChangeListener {
    private MeshPropertyPanel meshPropertyPanel = new MeshPropertyPanel();
    private LayerPropertyPanel layerPropertyPanel = new LayerPropertyPanel();
    private BitsPropertyPanel bitsPropertyPanel;

    MeshWindowPropertyPanel(MeshController meshController) {
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.BLACK));
        meshController.addPropertyChangeListener("", this);
        this.setLayout(new BorderLayout());
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new GridBagLayout());

        this.bitsPropertyPanel = new BitsPropertyPanel(meshController.getMesh());

        GridBagConstraints meshPropertyPanelGBC = new GridBagConstraints();
        meshPropertyPanelGBC.fill = GridBagConstraints.HORIZONTAL;
        meshPropertyPanelGBC.gridx = 0;
        meshPropertyPanelGBC.gridy = 0;
        meshPropertyPanelGBC.weightx = 1;
        meshPropertyPanelGBC.weighty = 0;
        content.add(meshPropertyPanel, meshPropertyPanelGBC);

        GridBagConstraints layerPropertyPanelGBC = new GridBagConstraints();
        layerPropertyPanelGBC.fill = GridBagConstraints.HORIZONTAL;
        layerPropertyPanelGBC.gridx = 0;
        layerPropertyPanelGBC.gridy = 1;
        layerPropertyPanelGBC.weightx = 1;
        layerPropertyPanelGBC.weighty = 0;
        content.add(layerPropertyPanel, layerPropertyPanelGBC);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.PAGE_START;
        c.gridx = 0;
        c.gridy = 2;
        c.weighty = 1;
        c.weightx = 1;
        content.add(bitsPropertyPanel, c);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollPane, BorderLayout.CENTER);
        this.setOpaque(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String msg = evt.getPropertyName();
        Object val = evt.getNewValue();
        switch (msg) {
            case MeshController.MESH_SLICED:
                meshPropertyPanel.updateProperties(val);
                break;
            case MeshController.LAYER_CHOSEN:
                layerPropertyPanel.updateProperties(val);
                break;
            case MeshController.MESH_OPENED:
                meshPropertyPanel.updateProperties(val);
                break;
            case MeshController.MESH_PAVED:
                meshPropertyPanel.updateProperty(MeshPropertyPanel.MESH_STATE, "PAVED");
                break;
            case MeshController.MESH_OPTIMIZED:
                meshPropertyPanel.updateProperty(MeshPropertyPanel.MESH_STATE, "OPTIMIZED");
                break;
            case MeshController.LAYER_PAVED:
                layerPropertyPanel.updateProperties(val);
                break;
            case MeshController.BIT_SELECTED:
                bitsPropertyPanel.selectBit((Bit3D) val);
                break;
            case MeshController.BIT_UNSELECTED:
                bitsPropertyPanel.unselectBit((Bit3D) val);
                break;
            case MeshController.BITS_SELECTED:
                bitsPropertyPanel.selectBits((Collection<Bit3D>) val);
                break;
            case MeshController.DELETING_BITS:
                bitsPropertyPanel.unselectBits((Collection<Bit3D>) val);
        }
        revalidate();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, getHeight());
    }
}
