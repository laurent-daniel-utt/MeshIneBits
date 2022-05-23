/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
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

import meshIneBits.borderPaver.util.AI_Tool;
import meshIneBits.borderPaver.artificialIntelligence.Acquisition;
import meshIneBits.borderPaver.artificialIntelligence.NNTraining;
import meshIneBits.gui.view2d.MeshController;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class UPPToolsAI extends UtilityParametersPanel {
    private final static String TEXT_TOGGLE_FALSE = "Store new inputs";
    private final static String TEXT_TOGGLE_TRUE = "Recording new inputs..";
    private final JPanel parametersPanel;

    public UPPToolsAI(MeshController meshController) {
        super("AI-Tools");
        // Init components
        AI_Tool.setMeshController(meshController);
        parametersPanel = new JPanel();
        parametersPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        parametersPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        JButton trainButton = new JButton("Train AI");
        trainButton.addActionListener(e -> {
            try {
                NNTraining nnTraining = new NNTraining();
                if (trainButton.getText().equals("Train AI")) {
                    trainButton.setText("Stop training");
                    nnTraining.train(true);
                    nnTraining.evaluateModel();
                    try {
                        nnTraining.save();
                    } catch (IOException eSave) {
                        System.out.println("Neural Network training params could not be saved !");
                    }
                } else {
                    trainButton.setText("Train AI");
                    nnTraining.stop_training();
                }

            } catch (Exception e1) {
                meshController.handleException(e1);
            }


        });

        JButton deleteLastButton = new JButton("Delete last bit placed");
        deleteLastButton.setEnabled(false);
        deleteLastButton.addActionListener(_e -> {
            try {
                Acquisition.deleteLastPlacedBit();
            } catch (Exception _e1) {
                meshController.handleException(_e1);
            }
        });

        JToggleButton storeButton = new JToggleButton(TEXT_TOGGLE_FALSE);
        storeButton.addActionListener(e -> {
            try {
                if (storeButton.getText().equals(TEXT_TOGGLE_FALSE)) {
                    storeButton.setText(TEXT_TOGGLE_TRUE);
                    deleteLastButton.setEnabled(true);
                    Acquisition.startStoringBits();
                    ShowDialogStartStoringBits();
                } else {
                    storeButton.setText(TEXT_TOGGLE_FALSE);
                    deleteLastButton.setEnabled(false);
                    Acquisition.stopStoringBits();
                }
            } catch (Exception e1) {
                meshController.handleException(e1);
            }
        });


        // Layout
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.weighty = 0;
        c.weightx = 0;
        c.anchor = GridBagConstraints.LINE_START;
        add(storeButton, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weighty = 0;
        c.weightx = 0;
        c.anchor = GridBagConstraints.CENTER;
        add(trainButton, c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.weighty = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        add(deleteLastButton, c);
    }

    private void ShowDialogStartStoringBits() {
        JOptionPane pane = new JOptionPane("To correctly train the AI, external surfaces have to be populated turning clockwise, and internal surfaces, anticlockwise." +
                "\nThe shortest sides of the bits have to intersect with the border of the Slice " +
                "\n\n The bit will become yellow if its position is good according to theses criteria.");
        JDialog dialog = pane.createDialog(parametersPanel, "How to store new bits...");
        dialog.setVisible(true);
    }
}