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

import meshIneBits.artificialIntelligence.AI_Tool;
import meshIneBits.artificialIntelligence.deeplearning.Acquisition;
import meshIneBits.artificialIntelligence.deeplearning.NNTraining;
import meshIneBits.gui.view2d.MeshController;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class UPPToolsIA extends UtilityParametersPanel {
    private final static String TEXT_TOGGLE_FALSE = "Store new inputs";
    private final static String TEXT_TOGGLE_TRUE = "Recording new inputs..";

    public UPPToolsIA(MeshController meshController) {
        super("AI-Tools");
        // Init components
        AI_Tool.setMeshController(meshController);
        JPanel parametersPanel = new JPanel();
        parametersPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        parametersPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);


       /* JButton startButton = new JButton("Start AI Pavement");
        startButton.addActionListener(e -> {
            try {

            } catch (Exception e1) {
                    meshController.handleException(e1);
                }
            });*/ //todo @Etienne @Andre encore besoin de ce bouton ?

        JButton trainButton = new JButton("Train AI");
        trainButton.addActionListener(e -> {
            try {
                NNTraining nnTraining = new NNTraining();
                nnTraining.train(true);
                nnTraining.evaluateModel();
                try {
                    nnTraining.save();
                } catch (IOException eSave) {
                    System.out.println("Neural Network training params could not be saved !");
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
                    if (storeButton.getText()==TEXT_TOGGLE_FALSE) {
                        storeButton.setText(TEXT_TOGGLE_TRUE);
                        deleteLastButton.setEnabled(true);
                        Acquisition.startStoringBits();
                    }
                    else {
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
           /* c.gridx = 0;
            c.gridy = 0;
            c.weighty = 0;
            c.weightx = 0;
            c.anchor = GridBagConstraints.LINE_START;
            add(startButton, c);*/

            c = new GridBagConstraints();
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
    }