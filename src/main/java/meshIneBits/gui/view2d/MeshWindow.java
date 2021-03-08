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

package meshIneBits.gui.view2d;

import meshIneBits.config.CraftConfig;
import meshIneBits.config.CraftConfigLoader;
import meshIneBits.gui.utilities.*;
import meshIneBits.gui.view3d.ControllerView3D;
import meshIneBits.gui.view3d.ProcessingModelView;
import meshIneBits.util.Logger;
import meshIneBits.util.SimultaneousOperationsException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Vector;

public class MeshWindow extends JFrame {

    private final GridBagConstraints selectorGBC;
    private final GridBagConstraints utilityParametersPanelGBC;
    private final GridBagConstraints zoomerGBC;
    private final GridBagConstraints propertyPanelGBC;
    private final MeshWindowCore core;
    private final JMenuBar menuBar = new JMenuBar();
    private final MeshActionToolbar toolBar = new MeshActionToolbar();
    private final MeshActionToolbar utilitiesBox = new MeshActionToolbar();
    private final Vector<MeshAction> meshActionList = new Vector<>();
    private final MeshSettingsWindow meshSettingsWindow = new MeshSettingsWindow();
    private final MeshController meshController = new MeshController(this);
    private boolean openedFill= false;
    private ProcessingModelView view3DWindow;
    private ActionMap actionMap;
    private UtilityParametersPanel utilityParametersPanel;
    private MeshWindowZoomer zoomer;
    private MeshWindowSelector selector;
    private MeshWindowPropertyPanel propertyPanel;


    public MeshWindow() throws HeadlessException {

        this.setIconImage(IconLoader.get("icon.png", 0, 0).getImage());

        // Visual options
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
            UIManager.put("Separator.foreground", new Color(10, 10, 10, 50));
            UIManager.put("Button.focus", new ColorUIResource(new Color(0, 0, 0, 0)));
            UIManager.put("Slider.focus", new ColorUIResource(new Color(0, 0, 0, 0)));
            UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Window options
        setTitle("MeshIneBits");
        setSize(1280, 720);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        init();
        setJMenuBar(menuBar);

        // Grid Bag Layout
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        // Toolbar
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.gridwidth = 4;
        c.weightx = 1;
        c.weighty = 0;
        add(toolBar, c);

        // Utilities box
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight = 3;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 1;
        add(utilitiesBox, c);

        // Utility parameter panel
        utilityParametersPanelGBC = new GridBagConstraints();
        utilityParametersPanelGBC.fill = GridBagConstraints.HORIZONTAL;
        utilityParametersPanelGBC.gridx = 1;
        utilityParametersPanelGBC.gridy = 1;
        utilityParametersPanelGBC.gridheight = 1;
        utilityParametersPanelGBC.gridwidth = 3;
        utilityParametersPanelGBC.weightx = 1;
        utilityParametersPanelGBC.weighty = 0;

        // Core
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill=GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        core = new MeshWindowCore(meshController);
        add(core, c);

        // Selector
        selectorGBC = new GridBagConstraints();
        selectorGBC.gridx = 2;
        selectorGBC.gridy = 2;
        selectorGBC.gridwidth = 1;
        selectorGBC.gridheight = 1;
        selectorGBC.weightx = 0;
        selectorGBC.weighty = 1;

        // Zoomer
        zoomerGBC = new GridBagConstraints();
        zoomerGBC.gridx = 1;
        zoomerGBC.gridy = 3;
        zoomerGBC.gridwidth = 2;
        zoomerGBC.gridheight = 1;
        zoomerGBC.fill = GridBagConstraints.HORIZONTAL;
        zoomerGBC.weightx = 1;
        zoomerGBC.weighty = 0;

        // Property panel
        propertyPanelGBC = new GridBagConstraints();
        propertyPanelGBC.fill = GridBagConstraints.BOTH;
        propertyPanelGBC.gridx = 3;
        propertyPanelGBC.gridy = 2;
        propertyPanelGBC.gridwidth = 1;
        propertyPanelGBC.gridheight = 3;
        propertyPanelGBC.weightx = 0.2;
        propertyPanelGBC.weighty = 1;

        // Status bar
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 4;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 0.05;
        add(new StatusBar(), c);

        setVisible(true);

    }

    private void save(){
        final JFileChooser fc = new CustomFileChooser() {
            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        String ext = CraftConfigLoader.MESH_EXTENSION;
        fc.addChoosableFileFilter(new FileNameExtensionFilter(ext.toUpperCase() + " files", ext));
        String dir;
        if (CraftConfig.lastMesh == null || CraftConfig.lastMesh.equals(""))
            dir = System.getProperty("user.home");
        else
            dir = CraftConfig.lastMesh.replace("\n", "\\n");
        fc.setSelectedFile(new File(dir));
        if (fc.showSaveDialog(MeshWindow.this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().endsWith("." + ext)) {
                f = new File(f.getPath() + "." + ext);
            }
            Logger.updateStatus("Saving the mesh at " + f.getName());
            try {
                meshController.saveMesh(f);
                openedFill=false;
            } catch (Exception e1) {
                meshController.handleException(e1);
            }

        }
    }

    private void closeProject(){
        openedFill=false;
        dispose();
        new MeshWindow();
        ControllerView3D.closeInstance();
        ProcessingModelView.closeInstance();
        System.out.println("close project");
    }
    private void init() {
        InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        actionMap = this.getRootPane().getActionMap();

        /* Actions */
        MeshAction newMesh = new MeshAction(
                "newMesh",
                "New project",
                "mesh-new.png",
                "New Mesh",
                "control N",
                () -> {
                    if (openedFill){
                        int answer= JOptionPane.showConfirmDialog(this,"You have already open a project.\n Do you want to save it before opening another one","Asking saving",JOptionPane.YES_NO_OPTION);
                        switch (answer) {
                            case JOptionPane.YES_OPTION:
                                save();
                        }
                        ControllerView3D.closeInstance();
                        ProcessingModelView.closeInstance();
                    }
                    final JFileChooser fc = new CustomFileChooser();
                    fc.addChoosableFileFilter(new FileNameExtensionFilter("STL files", "stl"));
                    String dir;
                    if (CraftConfig.lastModel == null || CraftConfig.lastModel.equals("")) {
                        dir = System.getProperty("user.home");
                    } else
                        dir = CraftConfig.lastModel.replace("\n", "\\n");
                    fc.setSelectedFile(new File(dir));
                    int returnVal = fc.showOpenDialog(MeshWindow.this);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            openedFill=true;
                            meshController.newMesh(fc.getSelectedFile());
                        } catch (SimultaneousOperationsException e1) {
                            meshController.handleException(e1);
                        }
                    }
                });
        meshActionList.add(newMesh);

        MeshAction openMesh = new MeshAction(
                "openMesh",
                "Open project",
                "mesh-open.png",
                "Reload a project into workspace",
                "control O",
                () -> {
                    if (openedFill){
                        int answer= JOptionPane.showConfirmDialog(this,"You have already open a project.\n Do you want to save it before opening another one","Asking saving",JOptionPane.YES_NO_OPTION);
                        switch (answer) {
                            case JOptionPane.YES_OPTION:
                                save();
                        }
                        ControllerView3D.closeInstance();
                        ProcessingModelView.closeInstance();
                    }
                    final JFileChooser fc = new CustomFileChooser();
                    String meshExt = CraftConfigLoader.MESH_EXTENSION;
                    fc.addChoosableFileFilter(new FileNameExtensionFilter(meshExt + " files", meshExt));
                    String dir;
                    if (CraftConfig.lastMesh == null || CraftConfig.lastMesh.equals(""))
                        dir = System.getProperty("user.home");
                    else
                        dir = CraftConfig.lastMesh.replace("\n", "\\n");
                    fc.setSelectedFile(new File(dir));
                    int returnVal = fc.showOpenDialog(MeshWindow.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File f = fc.getSelectedFile();
                        Logger.updateStatus("Opening the mesh " + f.getName());
                        try {
                            openedFill=true;
                            meshController.openMesh(f); // asynchronous task
                        } catch (SimultaneousOperationsException e1) {
                            meshController.handleException(e1);
                        }
                    }
                });
        meshActionList.add(openMesh);

        MeshAction saveMesh = new MeshAction(
                "saveMesh",
                "Save project",
                "mesh-save.png",
                "Save the current project",
                "control S",
                () -> {
                    if (openedFill){
                        save();
                        openedFill=true;
                    }
                });
        meshActionList.add(saveMesh);

        MeshAction closeMesh = new MeshAction("closeProject", "Close Project", "project-close.png", "Close the current project", "control Q", () -> {
            if (openedFill){
                int answer= JOptionPane.showConfirmDialog(this,"you are about to close the opened project.\n Do you want to save it?","Asking saving",JOptionPane.YES_NO_OPTION);
                switch (answer){
                    case JOptionPane.YES_OPTION:
                        save();
                    case JOptionPane.NO_OPTION:
                        closeProject();
                }
            }

        });

        MeshAction configure = new MeshAction(
                "configure",
                "Configuration",
                "gears.png",
                "Configure hyper parameters of printer and workspace",
                "",
                () -> meshSettingsWindow.setVisible(true));
        meshActionList.add(configure);

        MeshAction exit = new MeshAction(
                "exit",
                "Exit",
                "",
                "Exit program",
                "",
                () -> System.exit(0));
        meshActionList.add(exit);

        MeshAction manual = new MeshAction(
                "openManual",
                "Manual",
                "manual.png",
                "Open manual file",
                "",
                () -> {
                    Desktop dt = Desktop.getDesktop();
                    try {
                        dt.open(new File(
                                Objects.requireNonNull(
                                        MeshWindow.this.getClass()
                                                .getClassLoader()
                                                .getResource("resources/help.pdf"))
                                        .getPath()));
                    } catch (IOException e1) {
                        meshController.handleException(e1);
                    }
                });
        meshActionList.add(manual);

        MeshAction about = new MeshAction(
                "about",
                "About",
                "info-circle.png",
                "General information about software",
                "",
                () -> new AboutDialogWindow(
                        MeshWindow.this,
                        "About MeshIneBits",
                        true));
        meshActionList.add(about);

        MeshAction view3D = new MeshAction(
                "view3D",
                "3D View",
                "view-3D.png",
                "Open the 3D view of mesh",
                "alt 3",
                ProcessingModelView::startProcessingModelView);
        meshActionList.add(view3D);

        MeshAction sliceMesh = new MeshAction(
                "sliceMesh",
                "Slice Mesh",
                "mesh-slice.png",
                "Slice the mesh",
                "alt S",
                () -> {
                    try {
                        meshController.sliceMesh();
                    } catch (Exception e) {
                        meshController.handleException(e);
                    }
                });
        meshActionList.add(sliceMesh);

        MeshAction paveMesh = new MeshAction(
                "paveMesh",
                "Pave Mesh",
                "mesh-pave.png",
                "Pave the whole mesh with a pattern",
                "alt P",
                null) {
            final UPPPaveMesh uppPaveMesh = new UPPPaveMesh(meshController);

            @Override
            public void actionPerformed(ActionEvent e) {
                // Check validity
                if (meshController.getMesh() == null
                        || !meshController.getMesh().isSliced()) return;
                toggleUtilityParametersPanel(uppPaveMesh);
            }
        };
        meshActionList.add(paveMesh);

        MeshAction paveMeshAI = new MeshAction(
                "paveMeshAI",
                "AI Tools",
                "ia-light-bulb.png",
                "Access to AI tools",
                "alt A",
                null) {

            final UPPToolsIA uppToolsIA = new UPPToolsIA(meshController);

            @Override
            public void actionPerformed(ActionEvent e) {
                // Check validity
                if (meshController.getMesh() == null
                        || !meshController.getMesh().isSliced()) return;
                toggleUtilityParametersPanel(uppToolsIA);
            }
        };
        meshActionList.add(paveMeshAI);

        MeshAction exportMeshXML = new MeshAction(
                "exportMeshXML",
                "Export XML",
                "mesh-export-xml.png",
                "Export printing instructions",
                "alt E",
                () -> {
                    if (meshController.getMesh() == null) {
                        meshController.handleException(new Exception("Mesh not found"));
                        return;
                    }
                    if (!meshController.getMesh().getScheduler().isScheduled()) {
                        meshController.handleException(new Exception("Scheduling not lauched"));
                        return;
                    }
                    final JFileChooser fc = new CustomFileChooser();
                    fc.addChoosableFileFilter(new FileNameExtensionFilter("XML files", "xml"));
                    int returnVal = fc.showSaveDialog(MeshWindow.this);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        Logger.updateStatus("Exporting XML at " + fc.getSelectedFile().getName());
                        try {
                            meshController.exportXML(fc.getSelectedFile()); // async task
                        } catch (Exception e1) {
                            meshController.handleException(e1);
                        }
                    }
                });
        meshActionList.add(exportMeshXML);

        MeshAction paveLayer = new MeshAction(
                "paveLayer",
                "Pave Layer",
                "layer-pave.png",
                "Pave the current layer",
                "alt shift P",
                null) {
            private final UPPPaveLayer uppPaveLayer = new UPPPaveLayer(meshController);

            @Override
            public void actionPerformed(ActionEvent e) {
                // Check validity
                if (meshController.getMesh() == null
                        || !meshController.getMesh().isSliced()) return;
                toggleUtilityParametersPanel(uppPaveLayer);
            }
        };
        meshActionList.add(paveLayer);

        MeshAction paveRegion = new MeshAction(
                "paveRegion",
                "Pave Region",
                "layer-region.png",
                "Choose and pave a region",
                "",
                null) {
            private final UPPPaveRegion uppPaveRegion = new UPPPaveRegion(meshController);

            @Override
            public void actionPerformed(ActionEvent e) {
                if (meshController.getMesh() == null) {
                    meshController.handleException(new Exception("Mesh not found"));
                    return;
                }
                if (meshController.getCurrentLayer() == null) {
                    meshController.handleException(new Exception("Layer not found"));
                    return;
                }
                toggleUtilityParametersPanel(uppPaveRegion);
            }
        };
        meshActionList.add(paveRegion);

        MeshAction paveFill = new MeshAction(
                "paveFill",
                "Fill",
                "layer-fill.png",
                "Fill the left space with pattern",
                "",
                null) {
            private final UPPPaveFill uppPaveFill = new UPPPaveFill(meshController);

            @Override
            public void actionPerformed(ActionEvent e) {
                if (meshController.getMesh() == null) {
                    meshController.handleException(new Exception("Mesh not found"));
                    return;
                }
                if (meshController.getCurrentLayer() == null) {
                    meshController.handleException(new Exception("Layer not found"));
                    return;
                }
                toggleUtilityParametersPanel(uppPaveFill);
            }
        };
        meshActionList.add(paveFill);

        MeshAction optimizeMesh = new MeshAction(
                "optimizeMesh",
                "Optimize Mesh",
                "mesh-optimize.png",
                "Optimize all layers",
                "alt O",
                null) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    meshController.optimizeMesh();
                } catch (Exception e1) {
                    meshController.handleException(e1);
                }
            }
        };
        meshActionList.add(optimizeMesh);

        MeshAction optimizeLayer = new MeshAction(
                "optimizeLayer",
                "Optimize Layer",
                "layer-optimize.png",
                "Optimize the current layer",
                "alt shift O",
                null) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    meshController.optimizeLayer();
                } catch (Exception e1) {
                    meshController.handleException(e1);
                }
            }
        };
        meshActionList.add(optimizeLayer);

        MeshAction halfLengthBit = new MeshAction(
                "halfLengthBit",
                "Half Length",
                "bit-half-length.png",
                "Cut bit half in length",
                "",
                () -> meshController.scaleSelectedBit(
                        50,
                        100
                ));
        meshActionList.add(halfLengthBit);

        MeshAction halfWidthBit = new MeshAction(
                "halfWidthBit",
                "Half Width",
                "bit-half-width.png",
                "Cut bit half in width",
                "",
                () -> meshController.scaleSelectedBit(
                        100,
                        50
                ));
        meshActionList.add(halfWidthBit);

        MeshAction quartBit = new MeshAction(
                "quartBit",
                "1/4 Bit",
                "bit-quart.png",
                "Cut bit half in width and length",
                "",
                () -> meshController.scaleSelectedBit(
                        50,
                        50
                ));
        meshActionList.add(quartBit);

        MeshAction deleteBit = new MeshAction(
                "deleteBit",
                "Delete Bit",
                "bit-delete.png",
                "Remove chosen bit(s)",
                "DELETE",
                meshController::deleteSelectedBits);
        meshActionList.add(deleteBit);

        MeshAction restoreBit = new MeshAction(
                "restoreBit",
                "Restore Bit",
                "bit-restore.png",
                "Restore to full bit",
                "",
                () -> meshController.scaleSelectedBit(
                        100,
                        100));
        meshActionList.add(restoreBit);

        MeshAction newBit = new MeshAction(
                "newBit",
                "New Bit",
                "bit-new.png",
                "Create new bit",
                "alt N",
                null) {
            private final UPPNewBit uppNewBit = new UPPNewBit(meshController);

            @Override
            public void actionPerformed(ActionEvent e) {
                if (meshController.getMesh() == null) {
                    meshController.handleException(new Exception("Mesh not found"));
                    return;
                }
                if (meshController.getCurrentLayer() == null) {
                    meshController.handleException(new Exception("Layer not found"));
                    return;
                }
                if (!meshController.getCurrentLayer().isPaved()) {
                    meshController.handleException(new Exception("Layer not paved"));
                    return;
                }
                toggleUtilityParametersPanel(uppNewBit);
                meshController.setAddingBits(uppNewBit.isVisible());
            }
        };
        meshActionList.add(newBit);

        MeshToggleAction toggleShowSlice = new MeshToggleAction(
                "toggleShowSlice",
                "Show/Hide Layer Border",
                "layer-border-toggle.png",
                "Show or hide boundary of layer",
                "shift 1",
                meshController,
                MeshController.SHOW_SLICE
        );
        meshActionList.add(toggleShowSlice);

        MeshToggleAction toggleIrregularBit = new MeshToggleAction(
                "toggleIrregularBit",
                "Show/Hide Irregular Bit",
                "bit-irregular-toggle.png",
                "Show or hide non realizable bits",
                "shift 2",
                meshController,
                MeshController.SHOW_IRREGULAR_BITS
        );
        meshActionList.add(toggleIrregularBit);

        MeshToggleAction toggleCutPaths = new MeshToggleAction(
                "toggleCutPaths",
                "Show/Hide Cut Paths",
                "bit-cutpath-toggle.png",
                "Show or hide cut path of bit",
                "shift 3",
                meshController,
                MeshController.SHOW_CUT_PATHS
        );
        meshActionList.add(toggleCutPaths);

        MeshToggleAction toggleLiftPoint = new MeshToggleAction(
                "toggleLiftPoints",
                "Show/Hide Lift Points",
                "bit-liftpoint-toggle.png",
                "Show or hide lift point of bit",
                "shift 4",
                meshController,
                MeshController.SHOW_LIFT_POINTS
        );
        meshActionList.add(toggleLiftPoint);

        MeshToggleAction toggleBitFullLength = new MeshToggleAction(
                "toogleBitsFullLength",
                "Show/Hide bits not full length",
                "icon-full-length.png",
                "Show or hide bits not use with his full length",
                "shift 6",
                meshController,
                MeshController.SHOW_BITS_NOT_FULL_LENGTH);
        meshActionList.add(toggleBitFullLength);

        MeshToggleAction togglePreviousLayer = new MeshToggleAction(
                "togglePreviousLayer",
                "Show/Hide Previous Layer",
                "layer-below.png",
                "Show or hide below pavement",
                "shift 5",
                meshController,
                MeshController.SHOW_PREVIOUS_LAYER
        );
        meshActionList.add(togglePreviousLayer);

        MeshAction scheduleMesh = new MeshAction(
                "scheduleMesh",
                "Bit cut and place scheduling",
                "mesh-schedule.png",
                "Index bits to print",
                "alt I",
                null
        ) {
            private final UPPScheduleMesh uppScheduleMesh = new UPPScheduleMesh(meshController);

            @Override
            public void actionPerformed(ActionEvent e) {
                if (meshController.getMesh() == null) {
                    meshController.handleException(new Exception("Mesh not found"));
                    return;
                }
                if (!meshController.getMesh().isPaved()) {
                    meshController.handleException(new Exception("Mesh not paved"));
                    return;
                }
                toggleUtilityParametersPanel(uppScheduleMesh);
            }
        };
        meshActionList.add(scheduleMesh);

        // Register to global listener
        meshActionList.forEach(meshAction -> {
            inputMap.put(meshAction.acceleratorKey, meshAction.uuid);
            actionMap.put(meshAction.uuid, meshAction);
        });


        /* Menu Bar */
        /* File */
        JMenu fileMenu = new MeshActionMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(newMesh);
        fileMenu.add(openMesh);
        fileMenu.add(saveMesh);
        fileMenu.add(closeMesh);
        fileMenu.addSeparator();
//        fileMenu.add(openPatternConfig);
//        fileMenu.add(savePatternConfig);
//        fileMenu.addSeparator();
        fileMenu.add(configure);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        JMenu helpMenu = new MeshActionMenu("Help");
        menuBar.add(helpMenu);
        helpMenu.add(manual);
        helpMenu.addSeparator();
        helpMenu.add(about);

        /* Toolbar */
        toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.LINE_AXIS));
        toolBar.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK));
        toolBar.setFloatable(false);
        toolBar.add(newMesh);
        toolBar.add(openMesh);
        toolBar.add(saveMesh);
        toolBar.addSeparator();
        toolBar.add(sliceMesh);
        toolBar.add(paveMesh);
        toolBar.add(paveMeshAI);
        toolBar.add(optimizeMesh);
        toolBar.add(scheduleMesh);
        toolBar.add(exportMeshXML);
        toolBar.addSeparator();
//        toolBar.add(openPatternConfig);
//        toolBar.add(savePatternConfig);
        toolBar.add(configure);
        toolBar.addSeparator();
        toolBar.add(view3D);
        toolBar.addSeparator();
        toolBar.addToggleButton(toggleShowSlice);
        toolBar.addToggleButton(toggleIrregularBit);
        toolBar.addToggleButton(toggleCutPaths);
        toolBar.addToggleButton(toggleLiftPoint);
        toolBar.addToggleButton(toggleBitFullLength);
        toolBar.addToggleButton(togglePreviousLayer);

        /* UtilitiesBox */
        utilitiesBox.setLayout(new BoxLayout(utilitiesBox, BoxLayout.PAGE_AXIS));
        utilitiesBox.setFloatable(false);
        utilitiesBox.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));
        utilitiesBox.add(paveLayer);
        utilitiesBox.add(paveRegion);
        utilitiesBox.add(paveFill);
        utilitiesBox.add(optimizeLayer);
        JSeparator horizontalSeparator = new JSeparator(SwingConstants.HORIZONTAL);
        horizontalSeparator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        utilitiesBox.add(horizontalSeparator);
        utilitiesBox.add(newBit);
        utilitiesBox.add(halfLengthBit);
        utilitiesBox.add(halfWidthBit);
        utilitiesBox.add(quartBit);
        utilitiesBox.add(deleteBit);
        utilitiesBox.add(restoreBit);

    }

    private void toggleUtilityParametersPanel(UtilityParametersPanel newUPP) {
        if (newUPP == utilityParametersPanel)
            utilityParametersPanel.setVisible(!utilityParametersPanel.isVisible());
        else {
            if (utilityParametersPanel != null)
                remove(utilityParametersPanel);
            utilityParametersPanel = newUPP;
            add(utilityParametersPanel, utilityParametersPanelGBC);
        }
        meshController.reset();
        revalidate();
        repaint();
    }

    ProcessingModelView getView3DWindow() {
        return view3DWindow;
    }

    void initGadgets() {
        if (zoomer != null)
            remove(zoomer);
        zoomer = new MeshWindowZoomer(meshController);
        add(zoomer, zoomerGBC);

        if (selector != null)
            remove(selector);
        selector = new MeshWindowSelector(meshController);
        add(selector, selectorGBC);

        if (propertyPanel != null)
            remove(propertyPanel);
        propertyPanel = new MeshWindowPropertyPanel(meshController);
        add(propertyPanel, propertyPanelGBC);
    }

    void reset() {
        if (zoomer != null) {
            remove(zoomer);
            zoomer = null;
        }
        if (selector != null) {
            remove(selector);
            selector = null;
        }
        if (propertyPanel != null) {
            remove(propertyPanel);
            meshController.removePropertyChangeListener(propertyPanel);
            propertyPanel = null;
        }
        core.initBackground();
    }
}