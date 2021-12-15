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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import meshIneBits.gui.utilities.patternParamRenderer.LabeledSpinner;
import meshIneBits.gui.view2d.MeshAction;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.gui.view2d.MeshToggleAction;

public class UPPNewBit extends UtilityParametersPanel {

  public UPPNewBit(MeshController meshController) {
    super("Add new bit");
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_START;
    c.weightx = 0;
    c.weighty = 0;
    c.insets = new Insets(0, 0, 0, 5);
    c.gridheight = 1;
    c.gridwidth = 1;
    c.gridy = 0;
    c.gridx = -1;

    MeshToggleAction toggleAddingBits = new MeshToggleAction(
        "addBits",
        "Toggle select positions",
        "selection-tool.png",
        "Enable/Disable bit adding mode",
        "",
        meshController,
        MeshController.ADDING_BITS
    );
    ToggleIcon addingBitsBtn = new ToggleIcon(toggleAddingBits);
    c.gridx++;
    add(addingBitsBtn, c);

    LabeledSpinner newBitsOrientationSpinner = new LabeledSpinner(
        meshController.getNewBitsOrientationParam());
    c.gridx++;
    add(newBitsOrientationSpinner, c);

    c.gridx++;
    add(meshController.getSafeguardSpaceParam()
        .getRenderer(), c);

    c.gridx++;
    add(meshController.getAutocropParam()
        .getRenderer(), c);

    c.gridx++;
    add(meshController.getProhibitAddingIrregularBitParam()
        .getRenderer(), c);

    JLabel bitLabel = new JLabel("Bit types: ");
    c.gridx++;
    add(bitLabel, c);

    ButtonGroup bitTypes = new ButtonGroup();

    ToggleIcon fullbit = new ToggleIcon(new MeshAction(
        "addFullBit",
        "Add a full bit",
        "bit-restore.png",
        "New bit to add will have full width and height",
        "",
        () -> meshController.setNewBitSize(100, 100)
    ));
    c.gridx++;
    add(fullbit, c);
    bitTypes.add(fullbit);

    ToggleIcon halfwidthbit = new ToggleIcon(new MeshAction(
        "addHalfWidthBit",
        "Add a bit with half width",
        "bit-half-width.png",
        "New bit to add will have half width and full length",
        "",
        () -> meshController.setNewBitSize(100, 50)
    ));
    c.gridx++;
    add(halfwidthbit, c);
    bitTypes.add(halfwidthbit);

    ToggleIcon halflengthbit = new ToggleIcon(new MeshAction(
        "addHalfLengthBit",
        "Add a bit with half length",
        "bit-half-length.png",
        "New bit to add will have half length and full width",
        "",
        () -> meshController.setNewBitSize(50, 100)
    ));
    c.gridx++;
    add(halflengthbit, c);
    bitTypes.add(halflengthbit);

    ToggleIcon quartbit = new ToggleIcon(new MeshAction(
        "addQuartBit",
        "Add a quart of bit",
        "bit-quart.png",
        "New bit to add will have half length and half width",
        "",
        () -> meshController.setNewBitSize(50, 50)
    ));
    c.gridx++;
    add(quartbit, c);
    bitTypes.add(quartbit);

    // Dummy panel
    JPanel dummy = new JPanel();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridx++;
    add(dummy, c);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(getWidth(), 70);
  }
}