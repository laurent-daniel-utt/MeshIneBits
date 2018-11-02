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

import meshIneBits.Mesh;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.CraftConfigLoader;
import meshIneBits.config.PatternConfig;
import meshIneBits.config.Setting;
import meshIneBits.gui.utilities.*;
import meshIneBits.gui.utilities.patternParamRenderer.LabeledSpinner;
import meshIneBits.gui.view3d.ProcessingView;
import meshIneBits.patterntemplates.PatternTemplate;
import meshIneBits.scheduler.AScheduler;
import meshIneBits.util.Logger;
import meshIneBits.util.XmlTool;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.*;

/**
 * All tasks are placed here.
 */
class Toolbar extends JTabbedPane implements Observer {
    private static final long serialVersionUID = -1759701286071368808L;
    private MainController controller;
    private File file = null;
    private File patternConfigFile = null;
    private SlicerTab SlicerTab;
    private TemplateTab TemplateTab;
    private HashMap<String, Setting> setupAnnotations = loadAnnotations();

    Toolbar() {
        // Add the tab
        JPanel fileTab = new JPanel();
        SlicerTab = new SlicerTab();
        TemplateTab = new TemplateTab();
        Toolbar.ReviewTab reviewTab = new ReviewTab();
        addTab("File", fileTab);
        addTab("Slicer", new JScrollPane(SlicerTab));
        addTab("Template", new JScrollPane(TemplateTab));
        addTab("Review", new JScrollPane(reviewTab));

        // Add the menu button
        FileMenuButton fileMenuBtn = new FileMenuButton();
        Toolbar.this.setTabComponentAt(0, fileMenuBtn);

        Toolbar.this.setSelectedIndex(indexOfTab("Slicer"));

        // Disabling the tabs that are useless before slicing
        // setEnabledAt(indexOfTab("Review"), false);
        setEnabledAt(indexOfTab("File"), false); // Actually this is a fake tab

        // Visual options
        setFont(new Font(this.getFont().toString(), Font.PLAIN, 15));

        // Main controller
        controller = MainController.getInstance();
        controller.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        revalidate();
    }

    /**
     * To show the pop-up menu for loading model, etc.
     */
    private class FileMenuButton extends JToggleButton {
        private static final long serialVersionUID = 5613899244422633632L;
        private FileMenuPopUp filePopup;

        FileMenuButton() {
            // Visual options
            this.setFocusable(false);
            this.setBorder(null);
            this.setContentAreaFilled(false);
            // Setting up
            ImageIcon icon = new ImageIcon(
                    new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/" + "bars.png"))).getImage()
                            .getScaledInstance(24, 24, Image.SCALE_REPLICATE));
            this.setIcon(icon);

            ImageIcon selectedIcon = new ImageIcon(
                    new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/" + "blue-bars.png")))
                            .getImage().getScaledInstance(24, 24, Image.SCALE_REPLICATE));
            filePopup = new FileMenuPopUp();

            // Actions listener
            this.addActionListener(ev -> {
                JToggleButton b = FileMenuButton.this;
                if (b.isSelected()) {
                    filePopup.show(b, b.getMousePosition().x, b.getMousePosition().y);
                    setIcon(selectedIcon);
                } else {
                    filePopup.setVisible(false);
                    setIcon(icon);
                }
            });

            filePopup.addPopupMenuListener(new PopupMenuListener() {

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    FileMenuButton.this.setSelected(false);
                    setIcon(icon);
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    FileMenuButton.this.setSelected(false);
                    setIcon(icon);
                }

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    FileMenuButton.this.setSelected(true);
                    setIcon(icon);
                }

            });
        }

        private class FileMenuPopUp extends JPopupMenu {
            private static final long serialVersionUID = 3631645660924751860L;

            FileMenuPopUp() {
                // Setting up
                JMenuItem openModelMenu = new FileMenuItem("Open Model", "file-o.png");
                JMenuItem saveMeshMenu = new FileMenuItem("Save Mesh", "model-save.png");
                JMenuItem openMeshMenu = new FileMenuItem("Open Mesh", "model-open.png");
                JMenuItem openPatternConfigMenu = new FileMenuItem("Load pattern configuration", "conf-o.png");
                JMenuItem savePatternConfigMenu = new FileMenuItem("Save pattern configuration", "conf-save.png");
                JMenuItem exportMenu = new FileMenuItem("Export XML", "file-code-o.png");
                JMenuItem aboutMenu = new FileMenuItem("About", "info-circle.png");

                add(openModelMenu);
                add(saveMeshMenu);
                add(openMeshMenu);
                addSeparator();
                add(openPatternConfigMenu);
                add(savePatternConfigMenu);
                add(exportMenu);
                addSeparator();
                add(aboutMenu);

                openModelMenu.setRolloverEnabled(true);

                // Actions listener
                openModelMenu.addActionListener(e -> {
                    final JFileChooser fc = new CustomFileChooser();
                    fc.addChoosableFileFilter(new FileNameExtensionFilter("STL files", "stl"));
                    fc.setSelectedFile(new File(CraftConfig.lastSlicedFile.replace("\n", "\\n")));
                    int returnVal = fc.showOpenDialog(null);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        file = fc.getSelectedFile();
                        String filename = file.toString();
                        Mesh mesh = new Mesh();
                        controller.setCurrentMesh(mesh); // reset
                        try {
                            mesh.importModel(filename);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            Logger.error("Failed to load model");
                            return;
                        }
                        controller.setModel();
                        MainWindow.getInstance().getModelView().toggle();
                        Logger.updateStatus("Ready to slice " + file.getName());
                        Toolbar.this.SlicerTab.setReadyToSlice();
                    }
                });

                saveMeshMenu.addActionListener(e -> {
                    final JFileChooser fc = new CustomFileChooser();
                    String ext = CraftConfigLoader.MESH_EXTENSION;
                    fc.addChoosableFileFilter(new FileNameExtensionFilter(ext.toUpperCase() + " files", ext));
                    if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File f = fc.getSelectedFile();
                        if (!f.getName().endsWith("." + ext)) {
                            f = new File(f.getPath() + "." + ext);
                        }
                        try {
                            controller.saveMesh(f);
                            Logger.updateStatus("Saved the mesh at " + f.getName());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            Logger.error("Failed to save the mesh");
                        }
                    }
                });

                openMeshMenu.addActionListener(e -> {
                    final JFileChooser fc = new CustomFileChooser();
                    String meshExt = CraftConfigLoader.MESH_EXTENSION;
                    fc.addChoosableFileFilter(new FileNameExtensionFilter(meshExt + " files", meshExt));
                    fc.setSelectedFile(new File(CraftConfig.lastSlicedFile.replace("\n", "\\n")));
                    int returnVal = fc.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File f = fc.getSelectedFile();
                        try {
                            Logger.updateStatus("Opening the mesh");
                            controller.openMesh(f); // asynchronous task
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            Logger.error("Failed to load mesh");
                        }
                    }
                });

                openPatternConfigMenu.addActionListener(e -> {
                    final JFileChooser fc = new CustomFileChooser();
                    String ext = CraftConfigLoader.PATTERN_CONFIG_EXTENSION;
                    fc.addChoosableFileFilter(new FileNameExtensionFilter(ext.toUpperCase() + " files", ext));
                    String filePath = CraftConfig.lastPatternConfigFile.replace("\n", "\\n");
                    fc.setSelectedFile(new File(filePath));
                    if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        patternConfigFile = fc.getSelectedFile();
                        CraftConfig.lastPatternConfigFile = patternConfigFile.getAbsolutePath();
                        PatternConfig loadedConf = CraftConfigLoader.loadPatternConfig(patternConfigFile);
                        if (loadedConf != null) {
                            TemplateTab.patternParametersContainer.setupPatternParameters(loadedConf);
                            Logger.updateStatus("Pattern configuration loaded.");
                        }
                    }
                });

                savePatternConfigMenu.addActionListener(e -> {
                    final JFileChooser fc = new CustomFileChooser();
                    String ext = CraftConfigLoader.PATTERN_CONFIG_EXTENSION;
                    fc.addChoosableFileFilter(new FileNameExtensionFilter(ext.toUpperCase() + " files", ext));
                    if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File f = fc.getSelectedFile();
                        if (!f.getName().endsWith("." + ext)) {
                            f = new File(f.getPath() + "." + ext);
                        }
                        CraftConfigLoader.savePatternConfig(f);
                    }
                });

                exportMenu.addActionListener(e -> {
                    final JFileChooser fc = new JFileChooser();
                    fc.addChoosableFileFilter(new FileNameExtensionFilter("XML files", "xml"));
                    int returnVal = fc.showSaveDialog(null);

                    Mesh part = controller.getCurrentMesh();
                    if ((returnVal == JFileChooser.APPROVE_OPTION) && (part != null) && part.isPaved()) {
                        XmlTool xt = new XmlTool(part, Paths.get(fc.getSelectedFile().getPath()));
                        xt.writeXmlCode();
                    } else {
                        Logger.error("The XML file has not been generated");
                    }
                });

                aboutMenu.addActionListener(e -> new AboutDialogWindow(null, "About MeshIneBits", true));
            }

            private class FileMenuItem extends JMenuItem {

                private static final long serialVersionUID = 3576752233844578812L;

                FileMenuItem(String label, String iconName) {
                    super(label);

                    // Visual options
                    setRolloverEnabled(true);
                    this.setHorizontalAlignment(LEFT);
                    this.setMargin(new Insets(0, 0, 0, 2));

                    // Setting up
                    try {
                        ImageIcon icon = new ImageIcon(
                                new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/" + iconName)))
                                        .getImage().getScaledInstance(22, 22, Image.SCALE_DEFAULT));
                        this.setIcon(icon);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Actions listener
                    addMouseListener(new MouseListener() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {
                            setArmed(true);
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            setArmed(false);
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                        }
                    });
                }
            }

        }

    }

    /**
     * For slicing object into multiple slices
     */
    private class SlicerTab extends RibbonTab {

        private static final long serialVersionUID = -2435250564072409684L;
        private JLabel fileSelectedLabel;
        private JButton computeSlicesBtn;

        void setReadyToSlice() {
            fileSelectedLabel.setText(file.getName());
            computeSlicesBtn.setEnabled(true);
        }

        SlicerTab() {
            super();

            // Slicer section
            OptionsContainer slicerCont = new OptionsContainer("Slicer options");
            // LabeledSpinner sliceHeightSpinner = new LabeledSpinner("sliceHeight", setupAnnotations.get("sliceHeight"));
            // slicerCont.add(sliceHeightSpinner);
            LabeledSpinner layersOffsetSpinner = new LabeledSpinner("layersOffset",
                    setupAnnotations.get("layersOffset"));
            slicerCont.add(layersOffsetSpinner);
            LabeledSpinner firstSliceHeightPercentSpinner = new LabeledSpinner("firstSliceHeightPercent",
                    setupAnnotations.get("firstSliceHeightPercent"));
            slicerCont.add(firstSliceHeightPercentSpinner);

            add(slicerCont);

            // Compute section
            OptionsContainer computeCont = new OptionsContainer("Compute");
            computeSlicesBtn = new ButtonIcon("Slice model", "gears.png");
            computeSlicesBtn.setHorizontalAlignment(SwingConstants.CENTER);
            fileSelectedLabel = new JLabel("No file selected");
            computeCont.add(fileSelectedLabel);
            computeCont.add(computeSlicesBtn);

            add(new TabContainerSeparator());
            add(computeCont);

            // Actions listener

            computeSlicesBtn.addActionListener(e -> {
                if (controller.getCurrentMesh() == null) {
                    Logger.error("No mesh found in workspace");
                    return;
                }

                if (file != null) {
                    // Disable button to prevent further clicking
                    computeSlicesBtn.setEnabled(false);
                    try {
                        controller.getCurrentMesh().slice();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        StringBuilder sb = new StringBuilder();
                        sb.append(e1.toString());
                        sb.append("\n");
                        for (StackTraceElement el : e1.getStackTrace()) {
                            sb.append(el.toString());
                            sb.append("\n");
                        }
                        JOptionPane.showMessageDialog(null, sb, "Exception", JOptionPane.ERROR_MESSAGE);
                    }
                    // Re-enable the button
                    computeSlicesBtn.setEnabled(true);
                }
            });

            // View option
            // TODO
            // Should move this into menu bar

            OptionsContainer viewsCont = new OptionsContainer("Views");
            add(viewsCont);
            ButtonIcon _2D = new ButtonIcon("Toggle 2D view", "gears.png");
            ButtonIcon _3D = new ButtonIcon("Toggle 3D view", "gears.png");
            ButtonIcon _demo = new ButtonIcon("Demo", "gears.png");
            ButtonIcon _model = new ButtonIcon("Model View", "gears.png");
            viewsCont.add(_2D);
            viewsCont.add(_3D);
            viewsCont.add(_demo);
            viewsCont.add(_model);

            _2D.addActionListener(e -> MainWindow.getInstance().get2DView().toggle());

            _3D.addActionListener(e -> MainWindow.getInstance().get3DView().toggle());

            _demo.addActionListener(e -> MainWindow.getInstance().getDemoView().toggle());

            _model.addActionListener(e -> MainWindow.getInstance().getModelView().toggle());
        }
    }

    /**
     * For customizing parameters of the chosen pattern
     */
    private class TemplateTab extends RibbonTab {

        private static final long serialVersionUID = -2963705108403089250L;

        private JButton computeTemplateBtn;
        private PatternParametersContainer patternParametersContainer;

        TemplateTab() {
            super();

            // Setting up
            // Bits options
            OptionsContainer bitsCont = new OptionsContainer("Bits options");
            LabeledSpinner bitThicknessSpinner = new LabeledSpinner("bitThickness",
                    setupAnnotations.get("bitThickness"));
            LabeledSpinner bitWidthSpinner = new LabeledSpinner("bitWidth", setupAnnotations.get("bitWidth"));
            LabeledSpinner bitLengthSpinner = new LabeledSpinner("bitLength", setupAnnotations.get("bitLength"));
            bitsCont.add(bitThicknessSpinner);
            bitsCont.add(bitWidthSpinner);
            bitsCont.add(bitLengthSpinner);

            // Pattern choice
            GalleryContainer patternGallery = new GalleryContainer("Pattern");

            // Template options
            patternParametersContainer = new PatternParametersContainer("Pattern parameters", patternGallery);

            // Computing options
            OptionsContainer computeCont = new OptionsContainer("Compute");
            computeCont.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            computeTemplateBtn = new ButtonIcon("Generate layers", "cog.png");
            computeTemplateBtn.setHorizontalAlignment(SwingConstants.CENTER);

            LabeledSpinner suctionCupDiameter = new LabeledSpinner("suckerDiameter",
                    setupAnnotations.get("suckerDiameter"));
            computeCont.add(suctionCupDiameter);
            computeCont.add(new JLabel()); // dummy
            computeCont.add(computeTemplateBtn);

            // Overall
            add(bitsCont);
            add(new TabContainerSeparator());
            add(patternGallery);
            add(new TabContainerSeparator());
            // add(patternParameters);
            add(patternParametersContainer);
            add(new TabContainerSeparator());
            add(computeCont);

            // Actions listener

            computeTemplateBtn.addActionListener(e -> {
                computeTemplateBtn.setEnabled(false);
                CraftConfigLoader.saveConfig(null);
                try {
                    controller.getCurrentMesh().pave(CraftConfig.templateChoice);
                } catch (Exception e1) {
                    Logger.error(e1.getMessage());
                }
                computeTemplateBtn.setEnabled(true);
            });
        }

    }

    /**
     * For reviewing result and auto-optimizing
     */
    private class ReviewTab extends RibbonTab {

        private static final long serialVersionUID = -6062849183461607573L;

        ReviewTab() {
            super();

            // For auto-optimizing the whole mesh
            OptionsContainer optimizeCont = new OptionsContainer("Optimizing pavement");
            JButton optimizeMeshBtn = new ButtonIcon("Optimize this mesh", "cog.png");
            optimizeMeshBtn.setToolTipText("Try the best to eliminate all irregular bits in the current mesh");
            optimizeCont.add(optimizeMeshBtn);

            add(optimizeCont);

            // For 3d view
            add(new TabContainerSeparator());
            OptionsContainer processingViewCont = new OptionsContainer("3D view");
            ButtonIcon processingViewBtn = new ButtonIcon("Open 3D view", "3D.png");
            processingViewCont.add(processingViewBtn);
            add(processingViewCont);

            // For Scheduling
            add(new TabContainerSeparator());
            OptionsContainer processingViewScheduler = new OptionsContainer("Scheduling");
            JComboBox schedulerChoice = new JComboBox(CraftConfig.schedulerPreloaded);
            ButtonIcon processingViewSchedulerBtn = new ButtonIcon("Run bits scheduling", "cog.png");
            processingViewScheduler.add(schedulerChoice);
            processingViewScheduler.add(processingViewSchedulerBtn);
            add(processingViewScheduler);

            /////////////////////////////////////////////
            // Actions listener

            // For auto-optimizing

            optimizeMeshBtn.addActionListener(e -> {
                optimizeMeshBtn.setEnabled(false);
                Mesh currentMesh = controller.getCurrentMesh();
                try {
                    currentMesh.optimize();
                } catch (Exception e1) {
                    Logger.error(e1.getMessage());
                }
                optimizeMeshBtn.setEnabled(true);
            });

            processingViewSchedulerBtn.addActionListener(e -> {
                processingViewSchedulerBtn.setEnabled(false);
                Mesh currentMesh = controller.getCurrentMesh();
                try {
                    currentMesh.runScheduler();
                } catch (Exception e1) {
                    Logger.error(e1.getMessage());
                }
                processingViewSchedulerBtn.setEnabled(true);
            });

            schedulerChoice.addActionListener(e -> {
                JComboBox sC = (JComboBox)e.getSource();
                Mesh currentMesh = controller.getCurrentMesh();
                try {
                    currentMesh.setScheduler((AScheduler) sC.getSelectedItem());
                } catch (Exception e1) {
                    Logger.error(e1.getMessage());
                }
            });

            // For 3D view
            processingViewBtn.addActionListener(e -> ProcessingView.startProcessingView());
        }
    }


    /**
     * Get annotations for fields from {@link CraftConfig}
     *
     * @return names of attributes associated by their annotations
     */
    private HashMap<String, Setting> loadAnnotations() {
        HashMap<String, Setting> result = new HashMap<>();
        try {
            // Get all declared attributes
            Field[] fieldList = Class.forName("meshIneBits.config.CraftConfig").getDeclaredFields();
            for (Field field : fieldList) {
                result.put(field.getName(), field.getAnnotation(Setting.class));
            }
        } catch (SecurityException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

}