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

import meshIneBits.gui.utilities.ButtonIcon;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Vector;

public class MeshWindow extends JFrame {

    private JMenuBar menuBar = new JMenuBar();
    private MeshActionToolbar toolBar = new MeshActionToolbar();
    private ActionMap actionMap;

    public MeshWindow() throws HeadlessException {
        this.setIconImage(IconLoader.get("icon.png").getImage());

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
        setLayout(new BorderLayout());

        init();
        setJMenuBar(menuBar);
        add(toolBar, BorderLayout.NORTH);

        setVisible(true);
    }

    private void init() {
        InputMap inputMap = this.getRootPane().getInputMap();
        actionMap = this.getRootPane().getActionMap();
        Vector<MeshAction> meshActionList = new Vector<>();

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
                // TODO New mesh
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
                // TODO Open mesh
                System.out.println("Opening mesh");
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
                // TODO
            }
        };
        meshActionList.add(saveMesh);

        MeshAction openPatternConfig = new MeshAction(
                "openPatternConfig",
                "Open Pattern Config",
                "pconf-open.png",
                "Open a set of predefined pattern parameters",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(openPatternConfig);

        MeshAction savePatternConfig = new MeshAction(
                "savePatternConfig",
                "Save Pattern Config",
                "pconf-save.png",
                "Save the current set of pattern parameters",
                ""
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        meshActionList.add(savePatternConfig);

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
                // TODO
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
                // TODO
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
                // TODO
            }
        };
        meshActionList.add(about);

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
        fileMenu.add(openPatternConfig);
        fileMenu.add(savePatternConfig);
        fileMenu.addSeparator();
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
        toolBar.setFloatable(false);
        toolBar.add(newMesh);
        toolBar.add(openMesh);
        toolBar.add(saveMesh);
        toolBar.addSeparator();
        toolBar.add(openPatternConfig);
        toolBar.add(savePatternConfig);
        toolBar.addSeparator();
        toolBar.add(configure);
    }

    private class MeshActionMenuItem extends JMenuItem {
        MeshActionMenuItem(Action a) {
            super(a);
            setMargin(new Insets(1, 1, 1, 2));
        }
    }

    private class MeshActionMenu extends JMenu {
        public MeshActionMenu(String name) {
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
    }
}