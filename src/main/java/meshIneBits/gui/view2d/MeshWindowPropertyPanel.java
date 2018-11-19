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
import java.util.List;
import java.util.Vector;

/**
 * Display properties of {@link Mesh}, {@link Layer} and {@link Bit3D}.
 * Observes {@link MeshController}.
 */
public class MeshWindowPropertyPanel extends JPanel implements PropertyChangeListener {
    private JPanel content = new JPanel();
    private GridBagConstraints meshPropertyPanelGBC;
    private GridBagConstraints layerPropertyPanelGBC;
    private MeshPropertyPanel meshPropertyPanel;
    private LayerPropertyPanel layerPropertyPanel;
    private JPanel bitsPropertyPanel;
    private List<Bit3D> bit3Ds = new Vector<>();

    MeshWindowPropertyPanel(MeshController meshController) {
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.BLACK));
        meshController.addPropertyChangeListener("", this);
        this.setLayout(new BorderLayout());
        content.setOpaque(false);
        content.setLayout(new GridBagLayout());

        meshPropertyPanelGBC = new GridBagConstraints();
        meshPropertyPanelGBC.fill = GridBagConstraints.HORIZONTAL;
        meshPropertyPanelGBC.gridx = 0;
        meshPropertyPanelGBC.gridy = 0;
        meshPropertyPanelGBC.weightx = 1;
        meshPropertyPanelGBC.weighty = 0;

        layerPropertyPanelGBC = new GridBagConstraints();
        layerPropertyPanelGBC.fill = GridBagConstraints.HORIZONTAL;
        layerPropertyPanelGBC.gridx = 0;
        layerPropertyPanelGBC.gridy = 1;
        layerPropertyPanelGBC.weightx = 1;
        layerPropertyPanelGBC.weighty = 0;

        bitsPropertyPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.PAGE_START;
        c.gridx = 0;
        c.gridy = 2;
        c.weighty = 1;
        c.weightx = 1;
        content.add(bitsPropertyPanel, c);
        bitsPropertyPanel.setLayout(new GridBagLayout());
        GridBagConstraints bitPropertyPanelGBC = new GridBagConstraints();
        bitPropertyPanelGBC.fill = GridBagConstraints.BOTH;
        bitPropertyPanelGBC.gridx = 0;
        // gridy will be incremented
        bitPropertyPanelGBC.weightx = 1;
        bitPropertyPanelGBC.weighty = 0;

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
                initMeshPropertyPanel((Mesh) val, msg);
                break;
            case MeshController.LAYER_CHOSEN:
                initLayerPropertyPanel((Layer) val, msg);
                break;
            case MeshController.MESH_OPENED:
                initMeshPropertyPanel((Mesh) val, msg);
                break;
            case MeshController.MESH_PAVED:
                meshPropertyPanel.updateProperty(MeshPropertyPanel.MESH_STATE, "PAVED");
                break;
            case MeshController.MESH_OPTIMIZED:
                meshPropertyPanel.updateProperty(MeshPropertyPanel.MESH_STATE, "OPTIMIZED");
                break;
            case MeshController.LAYER_PAVED:
                layerPropertyPanel.updateProperties(val, msg);
                break;
            case MeshController.BIT_SELECTED:
                selectBit((Bit3D) val);
                break;
            case MeshController.BIT_UNSELECTED:
                unselectBit((Bit3D) val);
                break;
            case MeshController.BITS_SELECTED:
                selectBits((Collection<Bit3D>) val);
                break;
        }
        revalidate();
    }

    private void selectBits(Collection<Bit3D> newBit3DS) {
        // TODO
        System.out.println("Bits selected");
    }

    private void unselectBit(Bit3D bit3D) {
        // TODO
        System.out.println("Bit unselected");
    }

    private void selectBit(Bit3D bit3D) {
        // TODO
        System.out.println("Bit selected");
    }

    private void initLayerPropertyPanel(Layer layer, String msg) {
        if (layerPropertyPanel == null) {
            layerPropertyPanel = new LayerPropertyPanel(layer);
            content.add(layerPropertyPanel, layerPropertyPanelGBC);
        } else
            layerPropertyPanel.updateProperties(layer, msg);
    }

    private void initMeshPropertyPanel(Mesh mesh, String msg) {
        if (meshPropertyPanel == null) {
            meshPropertyPanel = new MeshPropertyPanel(mesh);
            content.add(meshPropertyPanel, meshPropertyPanelGBC);
        } else
            meshPropertyPanel.updateProperties(mesh, msg);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, getHeight());
    }
}
