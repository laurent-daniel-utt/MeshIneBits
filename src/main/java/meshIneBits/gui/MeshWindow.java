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

package meshIneBits.gui;

import meshIneBits.config.CraftConfig;
import meshIneBits.config.CraftConfigLoader;
import meshIneBits.gui.utilities.AboutDialogWindow;
import meshIneBits.gui.utilities.ButtonIcon;
import meshIneBits.gui.utilities.CustomFileChooser;
import meshIneBits.gui.utilities.ToggleIcon;
import meshIneBits.gui.view3d.ProcessingModelView;
import meshIneBits.util.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Vector;

public class MeshWindow extends JFrame {

    private JMenuBar menuBar = new JMenuBar();
    private MeshActionToolbar toolBar = new MeshActionToolbar();
    private ActionMap actionMap;
    private MeshActionToolbar utilitiesBox = new MeshActionToolbar();
    private Vector<MeshAction> meshActionList = new Vector<>();
    private JPanel utilityParametersPanel = new JPanel();
    private JPanel core = new JPanel();
    private JPanel zoomer = new JPanel();
    private JPanel selector = new JPanel();
    private MeshController meshController = new MeshController(this);

    private ProcessingModelView view3DWindow = new ProcessingModelView();

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
        c.gridwidth = 3;
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
        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 1;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0;
        add(utilityParametersPanel, c);

        // Core
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        add(core, c);

        // Selector
        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 1;
        add(selector, c);

        // Zoomer
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 0;
        add(zoomer, c);

        // Status bar
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 3;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 0;
        add(new StatusBar(), c);

        setVisible(true);
    }

    private void init() {
        InputMap inputMap = this.getRootPane().getInputMap();
        actionMap = this.getRootPane().getActionMap();

        /* Actions */
        MeshAction newMesh = new MeshAction(
                "newMesh",
                "New Mesh",
                "mesh-new.png",
                "New Mesh",
                "control N"
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new CustomFileChooser();
                fc.addChoosableFileFilter(new FileNameExtensionFilter("STL files", "stl"));
                fc.setSelectedFile(new File(CraftConfig.lastSlicedFile.replace("\n", "\\n")));
                int returnVal = fc.showOpenDialog(MeshWindow.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        meshController.newMesh(fc.getSelectedFile());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        Logger.error("Failed to create new mesh. " + e1.getMessage());
                    }
                }
            }
        };
        meshActionList.add(newMesh);

        MeshAction openMesh = new MeshAction(
                "openMesh",
                "Open Mesh",
                "mesh-open.png",
                "Reload a project into workspace",
                "control O"
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new CustomFileChooser();
                String meshExt = CraftConfigLoader.MESH_EXTENSION;
                fc.addChoosableFileFilter(new FileNameExtensionFilter(meshExt + " files", meshExt));
                fc.setSelectedFile(new File(CraftConfig.lastSlicedFile.replace("\n", "\\n")));
                int returnVal = fc.showOpenDialog(MeshWindow.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    try {
                        Logger.updateStatus("Opening the mesh " + f.getName());
                        meshController.openMesh(f); // asynchronous task
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        Logger.error("Failed to load mesh. " + e1.getMessage());
                    }
                }
            }
        };
        meshActionList.add(openMesh);

        MeshAction saveMesh = new MeshAction(
                "saveMesh",
                "Save Mesh",
                "mesh-save.png",
                "Save the current project",
                "control S"
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new CustomFileChooser();
                String ext = CraftConfigLoader.MESH_EXTENSION;
                fc.addChoosableFileFilter(new FileNameExtensionFilter(ext.toUpperCase() + " files", ext));
                if (fc.showSaveDialog(MeshWindow.this) == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    if (!f.getName().endsWith("." + ext)) {
                        f = new File(f.getPath() + "." + ext);
                    }
                    try {
                        Logger.updateStatus("Saving the mesh at " + f.getName());
                        meshController.saveMesh(f);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        Logger.error("Failed to save mesh. " + e1.getMessage());
                    }
                }
            }
        };
        meshActionList.add(saveMesh);

//        MeshAction openPatternConfig = new MeshAction(
//                "openPatternConfig",
//                "Open Pattern Config",
//                "pconf-open.png",
//                "Open a set of predefined pattern parameters",
//                ""
//        ) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                final JFileChooser fc = new CustomFileChooser();
//                String ext = CraftConfigLoader.PATTERN_CONFIG_EXTENSION;
//                fc.addChoosableFileFilter(new FileNameExtensionFilter(ext.toUpperCase() + " files", ext));
//                String filePath = CraftConfig.lastPatternConfigFile.replace("\n", "\\n");
//                fc.setSelectedFile(new File(filePath));
//                if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//                    File patternConfigFile = fc.getSelectedFile();
//                    CraftConfig.lastPatternConfigFile = patternConfigFile.getAbsolutePath();
//                    PatternConfig loadedConf = CraftConfigLoader.loadPatternConfig(patternConfigFile);
//                    if (loadedConf != null) {
//                        Toolbar.TemplateTab.patternParametersContainer.setupPatternParameters(loadedConf);
//                        Logger.updateStatus("Pattern configuration loaded.");
//                    }
//                }
//            }
//        };
//        meshActionList.add(openPatternConfig);
//
//        MeshAction savePatternConfig = new MeshAction(
//                "savePatternConfig",
//                "Save Pattern Config",
//                "pconf-save.png",
//                "Save the current set of pattern parameters",
//                ""
//        ) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // TODO
//            }
//        };
//        meshActionList.add(savePatternConfig);

        MeshAction configure = new MeshAction(
                "configure",
                "Configuration",
                "gears.png",
                "Configure hyper parameters of printer and workspace",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(configure);

        MeshAction exit = new MeshAction(
                "exit",
                "Exit",
                "",
                "Exit program",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        meshActionList.add(exit);

        MeshAction manual = new MeshAction(
                "openManual",
                "Manual",
                "manual.png",
                "Open manual file",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Desktop dt = Desktop.getDesktop();
                try {
                    dt.open(new File(
                            Objects.requireNonNull(
                                    MeshWindow.this.getClass()
                                            .getClassLoader()
                                            .getResource("resources/help.pdf"))
                                    .getPath()));
                } catch (IOException e1) {
                    Logger.error("Failed to load help file");
                }
            }
        };
        meshActionList.add(manual);

        MeshAction about = new MeshAction(
                "about",
                "About",
                "info-circle.png",
                "General information about software",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutDialogWindow(MeshWindow.this, "About MeshIneBits", true);
            }
        };
        meshActionList.add(about);

        MeshAction view3D = new MeshAction(
                "view3D",
                "3D View",
                "view-3D.png",
                "Open the 3D view of mesh",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(view3D);

        MeshAction sliceMesh = new MeshAction(
                "sliceMesh",
                "Slice Mesh",
                "mesh-slice.png",
                "Slice the mesh",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(sliceMesh);

        MeshAction paveMesh = new MeshAction(
                "paveMesh",
                "Pave Mesh",
                "mesh-pave.png",
                "Pave the whole mesh with an unique pattern",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(paveMesh);

        MeshAction exportMeshXML = new MeshAction(
                "exportMeshXML",
                "Export XML",
                "mesh-export-xml.png",
                "Export printing instructions",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new CustomFileChooser();
                fc.addChoosableFileFilter(new FileNameExtensionFilter("XML files", "xml"));
                int returnVal = fc.showSaveDialog(MeshWindow.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    Logger.updateStatus("Exporting XML at " + fc.getSelectedFile().getName());
                    try {
                        meshController.exportXML(fc.getSelectedFile()); // async task
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };
        meshActionList.add(exportMeshXML);

        MeshAction paveLayer = new MeshAction(
                "paveLayer",
                "Pave Layer",
                "layer-pave.png",
                "Pave the current layer",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(paveLayer);

        MeshAction selectRegion = new MeshAction(
                "selectRegion",
                "Select Region",
                "region-select.png",
                "Select a region",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(selectRegion);

        MeshAction paveRegion = new MeshAction(
                "paveRegion",
                "Pave Region",
                "layer-region.png",
                "Choose and pave a region",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(paveRegion);

        MeshAction paveFill = new MeshAction(
                "paveFill",
                "Fill",
                "layer-fill.png",
                "Fill the left space with pattern",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(paveFill);

        MeshAction optimizeMesh = new MeshAction(
                "optimizeMesh",
                "Optimize Mesh",
                "mesh-optimize.png",
                "Optimize all layers",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(optimizeMesh);

        MeshAction optimizeLayer = new MeshAction(
                "optimizeLayer",
                "Optimize Layer",
                "layer-optimize.png",
                "Optimize the current layer",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(optimizeLayer);

        MeshAction halfLengthBit = new MeshAction(
                "halfLengthBit",
                "Half Width",
                "bit-half-length.png",
                "Cut bit half in length",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(halfLengthBit);

        MeshAction halfWidthBit = new MeshAction(
                "halfWidthBit",
                "Half Length",
                "bit-half-width.png",
                "Cut bit half in width",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(halfWidthBit);

        MeshAction quartBit = new MeshAction(
                "quartBit",
                "1/4 Bit",
                "bit-quart.png",
                "Cut bit half in width and length",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(quartBit);

        MeshAction deleteBit = new MeshAction(
                "deleteBit",
                "Delete Bit",
                "bit-delete.png",
                "Remove chosen bit(s)",
                "DELETE"
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(deleteBit);

        MeshAction restoreBit = new MeshAction(
                "restoreBit",
                "Restore Bit",
                "bit-restore.png",
                "Restore to full bit",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(restoreBit);

        MeshAction newBit = new MeshAction(
                "newBit",
                "New Bit",
                "bit-new.png",
                "Create new bit",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(newBit);

        MeshAction toggleLayerBorder = new MeshAction(
                "toggleLayerBorder",
                "Show/Hide Layer Border",
                "layer-border-toggle.png",
                "Show or hide boundary of layer",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(toggleLayerBorder);

        MeshAction toggleIrregularBit = new MeshAction(
                "toggleIrregularBit",
                "Show/Hide Irregular Bit",
                "bit-irregular-toggle.png",
                "Show or hide non realizable bits",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(toggleIrregularBit);

        MeshAction toggleCutPaths = new MeshAction(
                "toggleCutPaths",
                "Show/Hide Cut Paths",
                "bit-cutpath-toggle.png",
                "Show or hide cut path of bit",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(toggleCutPaths);

        MeshAction toggleLiftPoint = new MeshAction(
                "toggleLiftPoints",
                "Show/Hide Lift Points",
                "bit-liftpoint-toggle.png",
                "Show or hide lift point of bit",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(toggleLiftPoint);

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
        toolBar.add(optimizeMesh);
        toolBar.add(exportMeshXML);
        toolBar.addSeparator();
//        toolBar.add(openPatternConfig);
//        toolBar.add(savePatternConfig);
        toolBar.add(configure);
        toolBar.addSeparator();
        toolBar.add(view3D);
        toolBar.addSeparator();
        toolBar.addToggleButton(toggleLayerBorder);
        toolBar.addToggleButton(toggleLiftPoint);
        toolBar.addToggleButton(toggleCutPaths);
        toolBar.addToggleButton(toggleIrregularBit);

        /* UtilitiesBox */
        utilitiesBox.setLayout(new BoxLayout(utilitiesBox, BoxLayout.PAGE_AXIS));
        utilitiesBox.setFloatable(false);
        utilitiesBox.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));
        utilitiesBox.add(paveLayer);
        utilitiesBox.add(selectRegion);
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

    public ProcessingModelView getView3DWindow() {
        return view3DWindow;
    }

    private class MeshActionMenuItem extends JMenuItem {

        MeshActionMenuItem(Action a) {
            super(a);
            setMargin(new Insets(1, 1, 1, 2));
        }
    }

    private class MeshActionMenu extends JMenu {
        MeshActionMenu(String name) {
            super(name);
        }

        @Override
        public JMenuItem add(Action a) {
            return super.add(new MeshActionMenuItem(a));
        }
    }

    private class MeshActionToolbar extends JToolBar {
        public void add(MeshAction a) {
            add(new ButtonIcon(a));
        }

        public void addToggleButton(MeshAction meshAction) {
            add(new ToggleIcon(meshAction));
        }
    }
}