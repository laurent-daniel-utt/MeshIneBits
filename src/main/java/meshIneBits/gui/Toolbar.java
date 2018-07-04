/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
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

import meshIneBits.GeneratedPart;
import meshIneBits.Model;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.CraftConfigLoader;
import meshIneBits.config.PatternConfig;
import meshIneBits.config.Setting;
import meshIneBits.config.patternParameter.PatternParameter;
import meshIneBits.gui.utilities.*;
import meshIneBits.gui.utilities.patternParamRenderer.LabeledSpinner;
import meshIneBits.gui.view3d.ProcessingView;
import meshIneBits.patterntemplates.PatternTemplate;
import meshIneBits.util.Logger;
import meshIneBits.util.XmlTool;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class Toolbar extends JTabbedPane implements Observer {
    private static final long serialVersionUID = -1759701286071368808L;
    private MainController controller;
    private File file = null;
    private File patternConfigFile = null;
    private JPanel FileTab;
    private SlicerTab SlicerTab;
    private TemplateTab TemplateTab;
    private ReviewTab ReviewTab;
    private HashMap<String, Setting> setupAnnotations = loadAnnotations();

    public HashMap<String, Setting> getSetupAnnotations() {
        return setupAnnotations;
    }

    public Toolbar() {
        // Add the tab
        FileTab = new JPanel();
        SlicerTab = new SlicerTab();
        TemplateTab = new TemplateTab();
        ReviewTab = new ReviewTab();
        addTab("File", FileTab);
        addTab("Slicer", new JScrollPane(SlicerTab));
        addTab("Template", new JScrollPane(TemplateTab));
        addTab("Review", new JScrollPane(ReviewTab));

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

        public FileMenuButton() {
            // Visual options
            this.setFocusable(false);
            this.setBorder(null);
            this.setContentAreaFilled(false);
            // Setting up
            ImageIcon icon = new ImageIcon(
                    new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + "bars.png")).getImage()
                            .getScaledInstance(24, 24, Image.SCALE_REPLICATE));
            this.setIcon(icon);

            ImageIcon selectedIcon = new ImageIcon(
                    new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + "blue-bars.png"))
                            .getImage().getScaledInstance(24, 24, Image.SCALE_REPLICATE));
            filePopup = new FileMenuPopUp();

            // Actions listener
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    JToggleButton b = FileMenuButton.this;
                    if (b.isSelected()) {
                        filePopup.show(b, b.getLocationOnScreen().x, b.getLocationOnScreen().y);
                        setIcon(selectedIcon);
                    } else {
                        filePopup.setVisible(false);
                        setIcon(icon);
                    }
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

            public FileMenuPopUp() {
                // Setting up
                JMenuItem openMenu = new FileMenuItem("Open Model", "file-o.png");
                JMenuItem openPatternConfigMenu = new FileMenuItem("Load pattern configuration", "conf-o.png");
                JMenuItem savePatternConfigMenu = new FileMenuItem("Save pattern configuration", "conf-save.png");
                JMenuItem exportMenu = new FileMenuItem("Export XML", "file-code-o.png");
                JMenuItem aboutMenu = new FileMenuItem("About", "info-circle.png");

                add(openMenu);
                addSeparator();
                add(openPatternConfigMenu);
                add(savePatternConfigMenu);
                add(exportMenu);
                addSeparator();
                add(aboutMenu);

                openMenu.setRolloverEnabled(true);

                // Actions listener
                openMenu.addActionListener(e -> {
                    final JFileChooser fc = new CustomFileChooser();
                    fc.addChoosableFileFilter(new FileNameExtensionFilter("STL files", "stl"));
                    fc.setSelectedFile(new File(CraftConfig.lastSlicedFile.replace("\n", "\\n")));
                    int returnVal = fc.showOpenDialog(null);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        file = fc.getSelectedFile();
                        String filename = file.toString();
                        Model m;
                        try {
                            m = new Model(filename);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            Logger.error("Failed to load model");
                            return;
                        }
                        controller.setModel(m);
                        MainWindow.getInstance().getModelView().toggle();
                        Logger.updateStatus("Ready to slice " + file.getName());
                        Toolbar.this.SlicerTab.setReadyToSlice(true);
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

                    GeneratedPart part = controller.getCurrentPart();
                    if ((returnVal == JFileChooser.APPROVE_OPTION) && (part != null) && part.isGenerated()) {
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

                public FileMenuItem(String label, String iconName) {
                    super(label);

                    // Visual options
                    setRolloverEnabled(true);
                    this.setHorizontalAlignment(LEFT);
                    this.setMargin(new Insets(0, 0, 0, 2));

                    // Setting up
                    try {
                        ImageIcon icon = new ImageIcon(
                                new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + iconName))
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

                        ;

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

                        ;
                    });
                }
            }

            private class AboutDialogWindow extends JDialog {
                private static final long serialVersionUID = -3389839563563221684L;

                public AboutDialogWindow(JFrame parent, String title, boolean modal) {
                    super(parent, title, modal);

                    // Visual options
                    Image windowIcon = new ImageIcon(this.getClass().getClassLoader().getResource("resources/icon.png"))
                            .getImage();
                    this.setIconImage(windowIcon);
                    this.setSize(350, 160);
                    this.setLocationRelativeTo(null);
                    this.setResizable(false);

                    // Setting up the dialog
                    JPanel jp = new JPanel();
                    jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));

                    JLabel bg = new JLabel("");
                    ImageIcon icon = new ImageIcon(
                            new ImageIcon(this.getClass().getClassLoader().getResource("resources/MeshIneBits.png"))
                                    .getImage().getScaledInstance(248, 42, Image.SCALE_SMOOTH));
                    bg.setIcon(icon);
                    bg.setFont(new Font(null, Font.BOLD | Font.ITALIC, 120));
                    bg.setForeground(new Color(0, 0, 0, 8));
                    bg.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JLabel copyrightLabel = new JLabel("Copyright© 2016 Thibault Cassard & Nicolas Gouju.");
                    copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JButton helpFileBtn = new JButton("Open help file (PDF format)");
                    helpFileBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

                    jp.add(new JLabel(" "));
                    jp.add(bg);
                    jp.add(copyrightLabel);
                    jp.add(new JLabel(" "));
                    jp.add(helpFileBtn);
                    AboutDialogWindow.this.getContentPane().add(jp, BorderLayout.CENTER);

                    // Actions listener
                    helpFileBtn.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            AboutDialogWindow.this.dispose();
                            Desktop dt = Desktop.getDesktop();
                            try {
                                dt.open(new File(
                                        this.getClass().getClassLoader().getResource("resources/help.pdf").getPath()));
                            } catch (IOException e1) {
                                Logger.error("Failed to load help file");
                            }
                        }
                    });

                    this.setVisible(true);
                }
            }

        }

    }

    /**
     * For reviewing result and auto-optimizing
     */
    private class ReviewTab extends RibbonTab {

        private static final long serialVersionUID = -6062849183461607573L;

        public ReviewTab() {
            super();

            // For auto-optimizing
            OptionsContainer autoOptimizeCont = new OptionsContainer("Auto-optimizing bits' distribution");
            JButton autoOptimizeGPartBtn = new ButtonIcon("Auto-optimize this generated part", "cog.png");
            autoOptimizeGPartBtn.setToolTipText(
                    "Try the best to eliminate all irregular bits in the current mesh");
            autoOptimizeCont.add(autoOptimizeGPartBtn);

            add(autoOptimizeCont);

            // For 3d view
            add(new TabContainerSeparator());
            OptionsContainer processingViewCont = new OptionsContainer("3D view");
            ButtonIcon processingViewBtn = new ButtonIcon("Open 3D view", "3D.png");
            processingViewCont.add(processingViewBtn);
            add(processingViewCont);

            /////////////////////////////////////////////
            // Actions listener

            // For auto-optimizing

            autoOptimizeGPartBtn.addActionListener(e -> {
                autoOptimizeGPartBtn.setEnabled(true);
                GeneratedPart currentPart = controller.getCurrentPart();
                currentPart.getOptimizer().automaticallyOptimizeGeneratedPart(currentPart);
                autoOptimizeGPartBtn.setEnabled(true);
            });

            // For 3D view
            processingViewBtn.addActionListener(e -> ProcessingView.startProcessingView(null));
        }
    }

    /**
     * For slicing object into multiple slices
     */
    private class SlicerTab extends RibbonTab {

        private static final long serialVersionUID = -2435250564072409684L;
        private JLabel fileSelectedLabel;
        private JButton computeSlicesBtn;

        public void setReadyToSlice(boolean ready) {
            fileSelectedLabel.setText(file.getName());
            computeSlicesBtn.setEnabled(true);
        }

        public SlicerTab() {
            super();

            // Slicer section
            OptionsContainer slicerCont = new OptionsContainer("Slicer options");
            LabeledSpinner sliceHeightSpinner = new LabeledSpinner("sliceHeight", setupAnnotations.get("sliceHeight"));
            LabeledSpinner firstSliceHeightPercentSpinner = new LabeledSpinner("firstSliceHeightPercent",
                    setupAnnotations.get("firstSliceHeightPercent"));
            slicerCont.add(sliceHeightSpinner);
            slicerCont.add(firstSliceHeightPercentSpinner);

            add(slicerCont);

            // Compute section
            OptionsContainer computeCont = new OptionsContainer("Compute");
            computeSlicesBtn = new ButtonIcon("Slice model", "gears.png");
            computeSlicesBtn.setHorizontalAlignment(SwingConstants.CENTER);
            // computeSlicesBtn.setEnabled(false);
            fileSelectedLabel = new JLabel("No file selected");
            computeCont.add(fileSelectedLabel);
            computeCont.add(computeSlicesBtn);

            add(new TabContainerSeparator());
            add(computeCont);

            // Actions listener

            computeSlicesBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    if (controller.getCurrentPart() != null) {
                        controller.setCurrentPart(null);
                    }

                    if (file != null) {
                        // Disable button to prevent further clicking
                        computeSlicesBtn.setEnabled(false);
                        try {
                            Model m;
                            try {
                                m = controller.getModel();
                                m.center();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                                Logger.error("Failed to load model");
                                return;
                            }
                            GeneratedPart part = new GeneratedPart(m);
                            controller.setCurrentPart(part);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            StringBuilder sb = new StringBuilder();
                            sb.append(e1.toString());
                            sb.append("\n");
                            for (StackTraceElement el: e1.getStackTrace()) {
                                sb.append(el.toString());
                                sb.append("\n");
                            }
                            JOptionPane.showMessageDialog(null, sb, "Exception", JOptionPane.ERROR_MESSAGE);
                        }
                        // Re-enable the button
                        computeSlicesBtn.setEnabled(true);
                    }
                }
            });

            // View option
            // TODO
            // Should move this into menu bar

            OptionsContainer viewsCont = new OptionsContainer("Views");
            add(viewsCont);
            ButtonIcon _2D = new ButtonIcon("Toggle 2D view", "gears.png");
            ButtonIcon _3D = new ButtonIcon("Toggle 3D view", "gears.png");
            ButtonIcon _demo = new ButtonIcon("Toggle demo", "gears.png");
            viewsCont.add(_2D);
            viewsCont.add(_3D);
            viewsCont.add(_demo);

            _2D.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // Show up the 2D view
                    MainWindow.getInstance().get2DView().toggle();
                }
            });

            _3D.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // Show up the 3D view
                    MainWindow.getInstance().get3DView().toggle();
                }
            });

            _demo.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // Show up the demo
                    MainWindow.getInstance().getDemoView().toggle();
                }
            });
        }
    }

    /**
     * For customizing parameters of the chosen pattern
     */
    private class TemplateTab extends RibbonTab {

        private static final long serialVersionUID = -2963705108403089250L;

        private JButton computeTemplateBtn;
        private PatternParametersContainer patternParametersContainer;

        public JButton getComputeTemplateBtn() {
            return computeTemplateBtn;
        }

        public TemplateTab() {
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
            patternParametersContainer = new PatternParametersContainer("Pattern parameters");
            patternParametersContainer.setupPatternParameters();

            // Computing options
            OptionsContainer computeCont = new OptionsContainer("Compute");
            computeCont.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            computeTemplateBtn = new ButtonIcon("Generate layers", "cog.png");
            computeTemplateBtn.setHorizontalAlignment(SwingConstants.CENTER);
            LabeledSpinner layersOffsetSpinner = new LabeledSpinner("layersOffset",
                    setupAnnotations.get("layersOffset"));
            LabeledSpinner minPercentageOfSlicesSpinner = new LabeledSpinner("minPercentageOfSlices",
                    setupAnnotations.get("minPercentageOfSlices"));
            LabeledSpinner defaultSliceToSelectSpinner = new LabeledSpinner("defaultSliceToSelect",
                    setupAnnotations.get("defaultSliceToSelect"));
            LabeledSpinner suctionCupDiameter = new LabeledSpinner("suckerDiameter",
                    setupAnnotations.get("suckerDiameter"));
            computeCont.add(layersOffsetSpinner);
            computeCont.add(minPercentageOfSlicesSpinner);
            computeCont.add(defaultSliceToSelectSpinner);
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

            computeTemplateBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    computeTemplateBtn.setEnabled(false);
                    controller.getCurrentPart().buildBits2D();
                    CraftConfigLoader.saveConfig(null);
                    computeTemplateBtn.setEnabled(true);
                }
            });
        }

        private class GalleryContainer extends OptionsContainer {

            private static final long serialVersionUID = 5081506030712556983L;

            /**
             * For the display
             */
            private JLabel templateChosen;
            private JPopupMenu templatesMenu;

            public GalleryContainer(String title) {
                super(title);
                // this.setLayout(new GridLayout(1, 2, 3, 3));
                this.setLayout(new BorderLayout());
                // For the chosen template
                PatternTemplate defaultTemplate = CraftConfig.templateChoice;
                ImageIcon image = new ImageIcon(
                        this.getClass().getClassLoader().getResource("resources/" + defaultTemplate.getIconName()));
                ImageIcon icon = new ImageIcon(image.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
                this.templateChosen = new JLabel(defaultTemplate.getCommonName(), icon, SwingConstants.CENTER);
                this.templateChosen.setPreferredSize(new Dimension(150, 50));
                this.templateChosen.setToolTipText(descriptiveText(defaultTemplate));
                this.add(templateChosen, BorderLayout.CENTER);
                // For the menu
                image = new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + "angle-down.png"));
                icon = new ImageIcon(image.getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
                JButton choosingTemplateBtn = new JButton(icon);
                this.add(choosingTemplateBtn, BorderLayout.SOUTH);
                choosingTemplateBtn.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        templatesMenu.show(choosingTemplateBtn, 0, choosingTemplateBtn.getHeight());
                    }
                });
                this.templatesMenu = new TemplatesMenu(this);
            }

            protected String descriptiveText(PatternTemplate template) {
                StringBuilder str = new StringBuilder();
                str.append("<html><div>");
                str.append("<p><strong>" + template.getCommonName() + "</strong></p>");
                str.append("<p>" + template.getDescription() + "</p>");
                str.append("<p><strong>How-To-Use</strong><br/>" + template.getHowToUse() + "</p>");
                str.append("</div></html>");
                return str.toString();
            }

            /**
             * All the available templates for choosing
             *
             * @author NHATHAN
             */
            private class TemplatesMenu extends JPopupMenu {

                /**
                 *
                 */
                private static final long serialVersionUID = 4906068175556528411L;
                private GalleryContainer parent;

                public TemplatesMenu(GalleryContainer galleryContainer) {
                    super("...");
                    this.parent = galleryContainer;
                    // Load all templates we have
                    // TODO
                    // Load all saved templates
                    CraftConfig.templatesLoaded = new Vector<PatternTemplate>(
                            Arrays.asList(CraftConfig.templatesPreloaded));
                    for (PatternTemplate template: CraftConfig.templatesLoaded) {
                        this.addNewTemplate(template);
                    }
                    // A button to load external template
                    this.addSeparator();
                    JMenuItem loadExternalTemplateBtn = new JMenuItem("More...");
                    this.add(loadExternalTemplateBtn);
                    // TODO
                    // Implement function "add external pattern"
                }

                public void addNewTemplate(PatternTemplate template) {
                    JMenuItem newTemplate = new JMenuItem(template.getCommonName());
                    ImageIcon image = new ImageIcon(
                            this.getClass().getClassLoader().getResource("resources/" + template.getIconName()));
                    ImageIcon icon = new ImageIcon(image.getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
                    newTemplate.setIcon(icon);
                    this.add(newTemplate);
                    newTemplate.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            parent.refreshTemplateChosen(template);
                            CraftConfig.templateChoice = template;
                            patternParametersContainer.setupPatternParameters();
                        }
                    });
                }
            }

            public void refreshTemplateChosen(PatternTemplate template) {
                this.templateChosen.setText(template.getCommonName());
                ImageIcon image = new ImageIcon(
                        this.getClass().getClassLoader().getResource("resources/" + template.getIconName()));
                ImageIcon icon = new ImageIcon(image.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
                this.templateChosen.setIcon(icon);
                this.templateChosen.setToolTipText(descriptiveText(template));
            }
        }

        /**
         * Contains specialized parameters for the chosen pattern
         */
        private class PatternParametersContainer extends OptionsContainer {

            /**
             *
             */
            private static final long serialVersionUID = -5486094986597798629L;

            public PatternParametersContainer(String title) {
                super(title);
            }

            /**
             * Remove all loaded components in containers then load new
             * parameters from the currently chosen pattern.
             */
            public void setupPatternParameters() {
                this.removeAll();
                for (PatternParameter paramConfig: CraftConfig.templateChoice.getPatternConfig().values()) {
                    this.add(paramConfig.getRenderer());
                }
            }

            /**
             * Remove all loaded components in containers then load new
             * parameters from the given <tt>config</tt> (input will be filtered
             * by attribute's name, type)
             *
             * @param config
             */
            public void setupPatternParameters(PatternConfig config) {
                this.removeAll();
                for (PatternParameter param: CraftConfig.templateChoice.getPatternConfig().values()) {
                    PatternParameter importParam = config.get(param.getCodename());
                    // Update current value
                    if (importParam != null) {
                        param.setCurrentValue(importParam.getCurrentValue());
                    }
                    // Then show
                    this.add(param.getRenderer());
                }
            }

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
            for (Field field: fieldList) {
                result.put(field.getName(), field.getAnnotation(Setting.class));
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

}