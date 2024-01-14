/*
 * MeshIneBits is a Java software to disintegrate a 3d project (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2022 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
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
 *
 */

package meshIneBits.gui.utilities;

import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view2d.ProjectController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

public class UPPPaveProject extends UtilityParametersPanel {
public static JButton button;
  public UPPPaveProject(ProjectController projectController) {
    super("Pave Project");
    // Init components
    JPanel parametersPanel = new JPanel();
    parametersPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
    parametersPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

    PatternComboBox patternComboBox = new PatternComboBox(
        Arrays.asList(CraftConfig.clonePreloadedPatterns()),
        parametersPanel
    );

    JButton startButton = new JButton();
    //shortcut for project paving : Alt+P
        Action buttonAction = new AbstractAction("Start") {
      @Override public void actionPerformed(ActionEvent evt) {


          startMeshPavement(projectController, patternComboBox);
            }
        };

        String key = "startPavement";
        startButton.setAction(buttonAction);
        buttonAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
        startButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK), key);
    startButton.getActionMap().put(key, buttonAction);
        startButton.addActionListener(e -> startMeshPavement(projectController, patternComboBox));

    // Layout
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.weighty = 0;
    c.weightx = 0;
    c.anchor = GridBagConstraints.LINE_START;
    add(patternComboBox, c);

    c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 0;
    c.weighty = 0;
    c.weightx = 0;
    c.anchor = GridBagConstraints.CENTER;
    add(startButton, c);
button=startButton;
    c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 2;
    c.gridy = 0;
    c.weighty = 1;
    c.weightx = 1;
    c.anchor = GridBagConstraints.LINE_START;
    add(parametersPanel, c);
  }

    private void startMeshPavement(ProjectController projectController, PatternComboBox patternComboBox) {
        try {
            projectController.paveMesh(patternComboBox.getCurrentChoice());
        } catch (Exception e1) {
            projectController.handleException(e1);
        }
    }
}