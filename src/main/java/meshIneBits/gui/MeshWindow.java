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

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MeshWindow extends JFrame {

    private JMenuBar menuBar = new JMenuBar();
    private JToolBar toolBar = new JToolBar();
    private InputMap inputMap;
    private ActionMap actionMap;

    public MeshWindow() throws HeadlessException {
        this.setIconImage(IconLoader.get("resources/icon.png").getImage());

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

        initActions();

        initMenuBar();
        setJMenuBar(menuBar);

        initToolbar();
        add(toolBar, BorderLayout.NORTH);

        setVisible(true);
    }

    private void initActions() {
        inputMap = this.getRootPane().getInputMap();
        actionMap = this.getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("control O"), "openMesh");
        actionMap.put("openMesh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Open mesh
                System.out.println("Opening mesh");
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("control S"), "saveMesh");
        actionMap.put("saveMesh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Save mesh
                System.out.println("Saving mesh");
            }
        });
    }

    private void initMenuBar() {
        /* File */
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        addActionMenuItem(fileMenu,
                "New Mesh",
                "newMesh",
                "control N",
                "resources/mesh-new.png");
        addActionMenuItem(fileMenu,
                "Open Mesh",
                "openMesh",
                "control O",
                "resources/mesh-open.png");
        addActionMenuItem(fileMenu,
                "Save Mesh",
                "saveMesh",
                "control S",
                "resources/mesh-save.png");
        fileMenu.addSeparator();
        addActionMenuItem(fileMenu,
                "Open Pattern Config",
                "openPatternConfig",
                "",
                "resources/pconf-open.png");
        addActionMenuItem(fileMenu,
                "Save Pattern Config",
                "savePatternConfig",
                "",
                "resources/pconf-save.png");
        fileMenu.addSeparator();
        addActionMenuItem(fileMenu,
                "Hyper Parameters",
                "openHyperParameter",
                "",
                "resources/gears.png");
        fileMenu.addSeparator();
        addActionMenuItem(fileMenu,
                "Exit",
                "exit",
                "",
                "");

        /* Help */
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        addActionMenuItem(helpMenu,
                "Manual",
                "openManual",
                "",
                "resources/manual.png");
        addActionMenuItem(helpMenu,
                "Website",
                "openWebsite",
                "",
                "resources/website.png");
        addActionMenuItem(helpMenu,
                "About",
                "about",
                "",
                "resources/info-circle.png");
    }

    private void addActionMenuItem(JMenu menu, String text,
                                   String actionText,
                                   String keyCombo,
                                   String iconFilepath) {
        JMenuItem jMenuItem = new JMenuItem(actionMap.get(actionText));
        jMenuItem.setMargin(new Insets(1, 1, 1, 2));
        menu.add(jMenuItem);
        if (keyCombo != null && !keyCombo.equals(""))
            jMenuItem.setAccelerator(KeyStroke.getKeyStroke(keyCombo));
        jMenuItem.setText(text);
        if (iconFilepath != null && !iconFilepath.equals(""))
            jMenuItem.setIcon(IconLoader.get(iconFilepath, 22, 22));
    }

    private void initToolbar() {

    }
}
