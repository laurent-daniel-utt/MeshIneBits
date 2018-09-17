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

import meshIneBits.gui.view2d.Window;
import meshIneBits.gui.view3d.ProcessingModelView;
import meshIneBits.gui.view3d.ProcessingView;
import meshIneBits.gui.view3d.demoView;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.util.Objects;

/**
 * Main window should only contain menu bar, toolbar, log and status bar. Every
 * graphic shall be executed in {@link SubWindow}, which can be toggled by menu
 * bar.
 */
public class MainWindow extends JFrame {
    private static MainWindow instance = null;
    private static final long serialVersionUID = -74349571204769732L;
    private Container content;
    private Toolbar toolbar;
    private SubWindow view2DWindow;
    private SubWindow view3DWindow;
    private SubWindow modelWindow;
    private SubWindow demoWindow;

    /**
     *
     * @return the main GUI window
     */
    public static MainWindow getInstance() {
        if (instance == null) {
            instance = new MainWindow();
        }
        return instance;
    }

    private MainWindow() {
        this.setIconImage(new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/icon.png"))).getImage());

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
        setSize(1280, 500);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Menu with the tabs
        toolbar = new Toolbar();

        // Preview of the generated part & controls
        view2DWindow = new Window();
        view3DWindow = new ProcessingView();
        modelWindow = new ProcessingModelView();
        demoWindow = new demoView();

        content = getContentPane();
        content.setLayout(new BorderLayout());
        content.add(toolbar, BorderLayout.NORTH);
        // TODO
        // A text log here
        content.add(new StatusBar(), BorderLayout.SOUTH);

        // Show the frames
        setVisible(true);
    }

    SubWindow get2DView() {
        return view2DWindow;
    }

    SubWindow get3DView() {
        return view3DWindow;
    }

    SubWindow getModelView() {
        return modelWindow;
    }

    SubWindow getDemoView() {
        return demoWindow;
    }

}
