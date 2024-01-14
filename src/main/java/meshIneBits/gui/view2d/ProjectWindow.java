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

package meshIneBits.gui.view2d;

import meshIneBits.config.CraftConfig;
import meshIneBits.config.CraftConfigLoader;
import meshIneBits.gui.utilities.*;
import meshIneBits.gui.view3d.provider.ProjectProvider;
import meshIneBits.gui.view3d.view.BaseVisualization3DView;
import meshIneBits.util.Logger;
import meshIneBits.util.SimultaneousOperationsException;
import meshIneBits.webUI.WebLauncher;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Vector;



public class ProjectWindow extends JFrame {

  private final GridBagConstraints selectorGBC;
  private final GridBagConstraints utilityParametersPanelGBC;
  private final GridBagConstraints zoomerGBC;
  private final GridBagConstraints propertyPanelGBC;
  private final ProjectWindowCore core;
  private final JMenuBar menuBar = new JMenuBar();
  private final ProjectActionToolbar toolBar = new ProjectActionToolbar();
  private final ProjectActionToolbar utilitiesBox = new ProjectActionToolbar();
  private final Vector<ProjectAction> projectActionList = new Vector<>();
  private final ProjectSettingsWindow projectSettingsWindow = new ProjectSettingsWindow();
  private final ProjectController projectController = new ProjectController(this);
  private ActionMap actionMap;
  private UtilityParametersPanel utilityParametersPanel;
  private ProjectWindowZoomer zoomer;
  private ProjectWindowSelector selector;
  private ProjectWindowPropertyPanel propertyPanel;

  private BaseVisualization3DView baseVisualization3DView=new BaseVisualization3DView();
  public ProjectWindow() throws HeadlessException {

    this.setIconImage(IconLoader.get("icon.png", 0, 0)
        .getImage());

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
    Dimension dim = Toolkit.getDefaultToolkit()
        .getScreenSize();
    setTitle("MeshIneBits");
    setSize(dim.width, dim.height - 100);
    setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       // setLocation(dim.width/2-this.getSize().width/2, 0);
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
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 1;
    core = new ProjectWindowCore(projectController);
    add(core, c);

    // Selector
    selectorGBC = new GridBagConstraints();
    selectorGBC.gridx = 2;
    selectorGBC.gridy = 2;
    selectorGBC.gridwidth = 1;
    selectorGBC.gridheight = 1;
    selectorGBC.fill = GridBagConstraints.VERTICAL;
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
    propertyPanelGBC.weightx = 0;
    propertyPanelGBC.weighty = 1;

    // Status bar
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 4;
    c.gridheight = 1;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 0;
    add(new StatusBar(), c);

    setVisible(true);

  }

  private void newFile() {
    final JFileChooser fc = new CustomFileChooser();
    fc.addChoosableFileFilter(new FileNameExtensionFilter("STL files", "stl"));
    String dir;
    if (CraftConfig.lastModel == null || CraftConfig.lastModel.equals("")) {
      dir = System.getProperty("user.home");
    } else {
      dir = CraftConfig.lastModel.replace("\n", "\\n");
    }
    fc.setSelectedFile(new File(dir));
    int returnVal = fc.showOpenDialog(ProjectWindow.this);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
      try {
        projectController.newMesh(fc.getSelectedFile());
      } catch (SimultaneousOperationsException e1) {
        projectController.handleException(e1);
      }
    }
  }

  private void openFile() {
    final JFileChooser fc = new CustomFileChooser();
    String meshExt = CraftConfigLoader.MESH_EXTENSION;
    fc.addChoosableFileFilter(new FileNameExtensionFilter(meshExt + " files", meshExt));
    String dir;
    if (CraftConfig.lastMesh == null || CraftConfig.lastMesh.equals("")) {
      dir = System.getProperty("user.home");
    } else {
      dir = CraftConfig.lastMesh.replace("\n", "\\n");
    }
    fc.setSelectedFile(new File(dir));
    int returnVal = fc.showOpenDialog(ProjectWindow.this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File f = fc.getSelectedFile();
      Logger.updateStatus("Opening the project " + f.getName());
      try {
        projectController.openMesh(f); // asynchronous task
      } catch (SimultaneousOperationsException e1) {
        projectController.handleException(e1);
      }
    }
  }

  private void save() {
    final JFileChooser fc = new CustomFileChooser() {
      @Override
      public void approveSelection() {
        File f = getSelectedFile();
        if (f.exists() && getDialogType() == SAVE_DIALOG) {
          int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?",
              "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
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
    if (CraftConfig.lastMesh == null || CraftConfig.lastMesh.equals("")) {
      dir = System.getProperty("user.home");
    } else {
      dir = CraftConfig.lastMesh.replace("\n", "\\n");
    }
    fc.setSelectedFile(new File(dir));
    if (fc.showSaveDialog(ProjectWindow.this) == JFileChooser.APPROVE_OPTION) {
      File f = fc.getSelectedFile();
      if (!f.getName()
          .endsWith("." + ext)) {
        f = new File(f.getPath() + "." + ext);
      }
      Logger.updateStatus("Saving the project at " + f.getName());
      try {
        projectController.saveMesh(f);
      } catch (Exception e1) {
        projectController.handleException(e1);
      }

    }
  }

  private void closeProject() {
    ProjectProvider.closeInstance();
    projectController.resetAll();
    this.reset();
    System.out .println("close project");
  }

  private void init() {
    InputMap inputMap = this.getRootPane()
        .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    actionMap = this.getRootPane()
        .getActionMap();

    /* Actions */
    ProjectAction newMesh = new ProjectAction(
        "newMesh",
        "New project",
        "mesh-new.png",
        "New Project",
        "control N",
        () -> {
          if (projectController.getMesh() != null) {
            int answer = JOptionPane.showConfirmDialog(this,
                "You have already open a project.\n Do you want to save it before opening another one",
                "Asking saving", JOptionPane.YES_NO_OPTION);
            switch (answer) {
              case JOptionPane.YES_OPTION:
                save();
                ProjectProvider.closeInstance();
                newFile();
                return;
              case JOptionPane.NO_OPTION:
                ProjectProvider.closeInstance();
                newFile();
                return;
              case JOptionPane.CLOSED_OPTION:
                return;
            }
          }
          newFile();
        });
    projectActionList.add(newMesh);

    ProjectAction openMesh = new ProjectAction(
        "openMesh",
        "Open project",
        "mesh-open.png",
        "Reload a project into workspace",
        "control O",
        () -> {
          if (projectController.getMesh() != null) {
            int answer = JOptionPane.showConfirmDialog(this,
                "You have already open a project.\n Do you want to save it before opening another one",
                "Asking saving", JOptionPane.YES_NO_OPTION);
            switch (answer) {
              case JOptionPane.YES_OPTION:
                save();
                openFile();
                return;
              case JOptionPane.NO_OPTION:
                openFile();
                return;
              case JOptionPane.CLOSED_OPTION:
                return;
            }
          }
          openFile();
        });
    projectActionList.add(openMesh);

    ProjectAction saveMesh = new ProjectAction(
        "saveMesh",
        "Save project",
        "mesh-save.png",
        "Save the current project",
        "control S",
        () -> {
          if (projectController.getMesh() != null) {
            save();
          }
        });
    projectActionList.add(saveMesh);

    ProjectAction closeMesh = new ProjectAction("closeProject", "Close Project", "project-close.png",
        "Close the current project", "control Q", () -> {
      if (projectController.getMesh() != null) {
        int answer = JOptionPane.showConfirmDialog(this,
            "you are about to close the opened project.\n Do you want to save it?", "Asking saving",
            JOptionPane.YES_NO_OPTION);
        switch (answer) {
          case JOptionPane.YES_OPTION:
            save();
            closeProject();
            return;
          case JOptionPane.NO_OPTION:
            closeProject();
            return;
          case JOptionPane.CLOSED_OPTION:
        }
      }

    });

    ProjectAction configure = new ProjectAction(
        "configure",
        "Configuration",
        "gears.png",
        "Configure hyper parameters of printer and workspace",
        "",
        () -> projectSettingsWindow.setVisible(true));
    projectActionList.add(configure);

    ProjectAction exit = new ProjectAction(
        "exit",
        "Exit",
        "",
        "Exit program",
        "",
        () -> System.exit(0));
    projectActionList.add(exit);

    ProjectAction manual = new ProjectAction(
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
                        ProjectWindow.this.getClass()
                            .getClassLoader()
                            .getResource("resources/help.pdf"))
                    .getPath()));
          } catch (IOException e1) {
            projectController.handleException(e1);
          }
        });
    projectActionList.add(manual);

    ProjectAction about = new ProjectAction(
        "about",
        "About",
        "info-circle.png",
        "General information about software",
        "",
        () -> new AboutDialogWindow(
            ProjectWindow.this,
            "About MeshIneBits",
            true));
    projectActionList.add(about);

    ProjectAction view3D = new ProjectAction(
        "view3D",
        "3D View",
        "view-3D.png",
        "Open the 3D view of project",
        "alt 3",

            baseVisualization3DView::startProcessingModelView);
    projectActionList.add(view3D);

    ProjectAction view3DBrowser = new ProjectAction(
            "view3DBrowser",
            "3D View Browser",
            "view-3D.png",
            "Open the 3D view of project",
            "alt 4",
            WebLauncher::launchBrowser
            );
    projectActionList.add(view3DBrowser);

    ProjectAction sliceMesh = new ProjectAction(
        "sliceMesh",
        "Slice Project",
        "mesh-slice.png",
        "Slice the project",
        "alt S",
        () -> {
          try {
            projectController.sliceMesh();
          } catch (Exception e) {
            projectController.handleException(e);
          }
        });
    projectActionList.add(sliceMesh);

    ProjectAction paveMesh = new ProjectAction(
        "paveMesh",
        "Pave Project",
        "mesh-pave.png",
        "Pave the whole project with a pattern",
        "alt P",
        null) {
      final UPPPaveProject uppPaveMesh = new UPPPaveProject(projectController);

      @Override
      public void actionPerformed(ActionEvent e) {
        // Check validity
        if (projectController.getMesh() == null
            || !projectController.getMesh()
            .isSliced()) {
          return;
        }
        toggleUtilityParametersPanel(uppPaveMesh);
      }
    };
    projectActionList.add(paveMesh);

    ProjectAction paveMeshAI = new ProjectAction(
        "paveMeshAI",
        "AI Tools",
        "ia-light-bulb.png",
        "Access to AI tools",
        "alt A",
        null) {

      final UPPToolsAI uppToolsIA = new UPPToolsAI(projectController);

      @Override
      public void actionPerformed(ActionEvent e) {
        // Check validity
        if (projectController.getMesh() == null
            || !projectController.getMesh()
            .isSliced()) {
          return;
        }
        toggleUtilityParametersPanel(uppToolsIA);
      }
    };
    projectActionList.add(paveMeshAI);

    ProjectAction exportMeshXML = new ProjectAction(
        "exportMeshXML",
        "Export XML",
        "mesh-export-xml.png",
        "Export printing instructions",
        "alt E",
        () -> {
          if (projectController.getMesh() == null) {
            projectController.handleException(new Exception("Project not found"));
            return;
          }
          if (!projectController.getMesh()
              .getScheduler()
              .isScheduled()) {
            projectController.handleException(new Exception("Scheduling not lauched"));
            return;
          }
          final JFileChooser fc = new CustomFileChooser();
          fc.setCurrentDirectory(new File("."));
          fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          // disable the "All files" option.
          fc.setAcceptAllFileFilterUsed(false);

          int returnVal = fc.showSaveDialog(ProjectWindow.this);

          if (returnVal == JFileChooser.APPROVE_OPTION) {
            Logger.updateStatus("Exporting XML at " + fc.getSelectedFile()
                .getName());
            try {
              projectController.exportXML(fc.getSelectedFile()); // async task
            } catch (Exception e1) {
              projectController.handleException(e1);
            }
          }
        });
    projectActionList.add(exportMeshXML);

    ProjectAction paveLayer = new ProjectAction(
        "paveLayer",
        "Pave Layer",
        "layer-pave.png",
        "Pave the current layer",
        "alt shift P",
        null) {
      private final UPPPaveLayer uppPaveLayer = new UPPPaveLayer(projectController);

      @Override
      public void actionPerformed(ActionEvent e) {
        // Check validity
        if (projectController.getMesh() == null
            || !projectController.getMesh()
            .isSliced()) {
          return;
        }
        toggleUtilityParametersPanel(uppPaveLayer);
      }
    };
    projectActionList.add(paveLayer);

    ProjectAction paveRegion = new ProjectAction(
        "paveRegion",
        "Pave Region",
        "layer-region.png",
        "Choose and pave a region",
        "",
        null) {
      private final UPPPaveRegion uppPaveRegion = new UPPPaveRegion(projectController);

      @Override
      public void actionPerformed(ActionEvent e) {
        if (projectController.getMesh() == null) {
          projectController.handleException(new Exception("Project not found"));
          return;
        }
        if (projectController.getCurrentLayer() == null) {
          projectController.handleException(new Exception("Layer not found"));
          return;
        }
        toggleUtilityParametersPanel(uppPaveRegion);
      }
    };
    projectActionList.add(paveRegion);

    ProjectAction paveFill = new ProjectAction(
        "paveFill",
        "Fill",
        "layer-fill.png",
        "Fill the left space with pattern",
        "",
        null) {
      private final UPPPaveFill uppPaveFill = new UPPPaveFill(projectController);

      @Override
      public void actionPerformed(ActionEvent e) {
        if (projectController.getMesh() == null) {
          projectController.handleException(new Exception("Project not found"));
          return;
        }
        if (projectController.getCurrentLayer() == null) {
          projectController.handleException(new Exception("Layer not found"));
          return;
        }
        toggleUtilityParametersPanel(uppPaveFill);
      }
    };
    projectActionList.add(paveFill);

    ProjectAction optimizeMesh = new ProjectAction(
        "optimizeMesh",
        "Optimize Project",
        "mesh-optimize.png",
        "Optimize all layers",
        "alt O",
        null) {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          projectController.optimizeMesh();
        } catch (Exception e1) {
          projectController.handleException(e1);
        }
      }
    };
    projectActionList.add(optimizeMesh);

    ProjectAction optimizeLayer = new ProjectAction(
        "optimizeLayer",
        "Optimize Layer",
        "layer-optimize.png",
        "Optimize the current layer",
        "alt shift O",
        null) {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          projectController.optimizeLayer();
        } catch (Exception e1) {
          projectController.handleException(e1);
        }
      }
    };
    projectActionList.add(optimizeLayer);

    ProjectAction halfLengthBit = new ProjectAction(
        "halfLengthBit",
        "Half Length",
        "bit-half-length.png",
        "Cut bit half in length",
        "",
        () -> projectController.scaleSelectedBit(
            50,
            100
        ));
    projectActionList.add(halfLengthBit);

    ProjectAction halfWidthBit = new ProjectAction(
        "halfWidthBit",
        "Half Width",
        "bit-half-width.png",
        "Cut bit half in width",
        "",
        () -> projectController.scaleSelectedBit(
            100,
            50
        ));
    projectActionList.add(halfWidthBit);

    ProjectAction quartBit = new ProjectAction(
        "quartBit",
        "1/4 Bit",
        "bit-quart.png",
        "Cut bit half in width and length",
        "",
        () -> projectController.scaleSelectedBit(
            50,
            50
        ));
    projectActionList.add(quartBit);

    ProjectAction deleteBit = new ProjectAction(
        "deleteBit",
        "Delete Bit",
        "bit-delete.png",
        "Remove chosen bit(s)",
        "DELETE",
        projectController::deleteSelectedBits);

    projectActionList.add(deleteBit);

    ProjectAction restoreBit = new ProjectAction(
        "restoreBit",
        "Restore Bit",
        "bit-restore.png",
        "Restore to full bit",
        "",
        () -> projectController.scaleSelectedBit(
            100,
            100));
    projectActionList.add(restoreBit);

    ProjectAction newBit = new ProjectAction(
        "newBit",
        "New Bit",
        "bit-new.png",
        "Create new bit",
        "alt N",
        null) {
      private final UPPNewBit uppNewBit = new UPPNewBit(projectController);

      @Override
      public void actionPerformed(ActionEvent e) {
        if (projectController.getMesh() == null) {
          projectController.handleException(new Exception("Project not found"));
          return;
        }
        if (projectController.getCurrentLayer() == null) {
          projectController.handleException(new Exception("Layer not found"));
          return;
        }
        if (!projectController.getCurrentLayer()
            .isPaved()) {
          projectController.handleException(new Exception("Layer not paved"));
          return;
        }
        toggleUtilityParametersPanel(uppNewBit);
        projectController.setAddingBits(uppNewBit.isVisible());
      }
    };
    projectActionList.add(newBit);




    ProjectToggleAction toggleShowSlice = new ProjectToggleAction(
        "toggleShowSlice",
        "Show/Hide Layer Border",
        "layer-border-toggle.png",
        "Show or hide boundary of layer",
        "shift 1",
            projectController,
        ProjectController.SHOW_SLICE
    );
    projectActionList.add(toggleShowSlice);

    ProjectToggleAction toggleIrregularBit = new ProjectToggleAction(
        "toggleIrregularBit",
        "Show/Hide Irregular Bit",
        "bit-irregular-toggle.png",
        "Show or hide non realizable bits",
        "shift 2",
            projectController,
        ProjectController.SHOW_IRREGULAR_BITS
    );
    projectActionList.add(toggleIrregularBit);

    ProjectToggleAction toggleCutPaths = new ProjectToggleAction(
        "toggleCutPaths",
        "Show/Hide Cut Paths",
        "bit-cutpath-toggle.png",
        "Show or hide cut path of bit",
        "shift 3",
            projectController,
        ProjectController.SHOW_CUT_PATHS
    );
    projectActionList.add(toggleCutPaths);

    ProjectToggleAction toggleLiftPoint = new ProjectToggleAction(
        "toggleLiftPoints",
        "Show/Hide Lift Points",
        "bit-liftpoint-toggle.png",
        "Show or hide lift point of bit",
        "shift 4",
            projectController,
        ProjectController.SHOW_LIFT_POINTS
    );
    projectActionList.add(toggleLiftPoint);

    ProjectToggleAction toggleBitFullLength = new ProjectToggleAction(
        "toogleBitsFullLength",
        "Show/Hide bits not full length",
        "icon-full-length.png",
        "Show or hide bits not use with his full length",
        "shift 6",
            projectController,
        ProjectController.SHOW_BITS_NOT_FULL_LENGTH);
    projectActionList.add(toggleBitFullLength);

    ProjectToggleAction togglePreviousLayer = new ProjectToggleAction(
        "togglePreviousLayer",
        "Show/Hide Previous Layer",
        "layer-below.png",
        "Show or hide below pavement",
        "shift 5",
            projectController,
        ProjectController.SHOW_PREVIOUS_LAYER
    );
    projectActionList.add(togglePreviousLayer);


    ProjectToggleAction manipulateBit = new ProjectToggleAction(
            "manipulateBit",
            "manipulate bit",
           // "bit-new.png",
            "manipulate_Bit.png",
            "manipulate the selected bit",
            "shift 7",
            projectController,
            ProjectController.MANIPULATNG_BIT
    );
    projectActionList.add(manipulateBit);


    ProjectAction scheduleMesh = new ProjectAction(
        "scheduleMesh",
        "Bit cut and place scheduling",
        "mesh-schedule.png",
        "Index bits to print",
        "alt I",
        null
    ) {
      private final UPPScheduleProject uppScheduleMesh = new UPPScheduleProject(projectController);

      @Override
      public void actionPerformed(ActionEvent e) {
        if (projectController.getMesh() == null) {
          projectController.handleException(new Exception("Project not found"));
          return;
        }
        if (!projectController.getMesh()
            .isPaved()) {
          projectController.handleException(new Exception("Project not paved"));
          return;
        }
        toggleUtilityParametersPanel(uppScheduleMesh);
      }
    };
    projectActionList.add(scheduleMesh);

    // Register to global listener
    projectActionList.forEach(meshAction -> {
      inputMap.put(meshAction.acceleratorKey, meshAction.uuid);
      actionMap.put(meshAction.uuid, meshAction);
    });


    /* Menu Bar */
    /* File */
    JMenu fileMenu = new ProjectActionMenu("File");
    menuBar.add(fileMenu);
    fileMenu.add(newMesh);
    fileMenu.add(openMesh);
    fileMenu.add(saveMesh);
    fileMenu.add(closeMesh);
    fileMenu.addSeparator();
    fileMenu.add(configure);
    fileMenu.addSeparator();
    fileMenu.add(exit);

    JMenu helpMenu = new ProjectActionMenu("Help");
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
    toolBar.add(configure);
    toolBar.addSeparator();
    toolBar.add(view3D);
    toolBar.add(view3DBrowser);
    toolBar.addSeparator();
    toolBar.addToggleButton(toggleShowSlice);
    toolBar.addToggleButton(toggleIrregularBit);
    toolBar.addToggleButton(toggleCutPaths);
    toolBar.addToggleButton(toggleLiftPoint);
    toolBar.addToggleButton(toggleBitFullLength);
    toolBar.addToggleButton(togglePreviousLayer);
    toolBar.addToggleButton(manipulateBit);
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
    if (newUPP == utilityParametersPanel) {
      utilityParametersPanel.setVisible(!utilityParametersPanel.isVisible());
    } else {
      if (utilityParametersPanel != null) {
        remove(utilityParametersPanel);
      }
      utilityParametersPanel = newUPP;
      add(utilityParametersPanel, utilityParametersPanelGBC);
    }
    projectController.reset();
    revalidate();
    repaint();

  }

  void initGadgets() {
    if (zoomer != null) {
      remove(zoomer);
    }
    zoomer = new ProjectWindowZoomer(projectController, core);
    add(zoomer, zoomerGBC);

    if (selector != null) {
      remove(selector);
    }
    selector = new ProjectWindowSelector(projectController);
    add(selector, selectorGBC);

    if (propertyPanel != null) {
      remove(propertyPanel);
    }
    propertyPanel = new ProjectWindowPropertyPanel(projectController);
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
      projectController.removePropertyChangeListener(propertyPanel);
      propertyPanel = null;
    }
    if(utilityParametersPanel!=null){
      remove(utilityParametersPanel);
      utilityParametersPanel=null;
    }
    core.initBackground();
    this.validate();
    this.repaint();
  }

  public ProjectController getMeshController() {
    return projectController;
  }
}