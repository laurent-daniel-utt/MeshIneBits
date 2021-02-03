/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas..
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
import meshIneBits.config.DoubleSetting;
import meshIneBits.config.FloatSetting;
import meshIneBits.config.IntegerSetting;
import meshIneBits.gui.utilities.IconLoader;
import meshIneBits.gui.utilities.patternParamRenderer.LabeledSpinner;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;

class MeshSettingsWindow extends JFrame {
    private final TabContentPanel bitSettingsPanel;
    private final TabContentPanel slicerSettingsPanel;
    private final TabContentPanel assemblerSettingsPanel;
    private final TabContentPanel xmlSettingsPanel;
    private final TabContentPanel printerSettingsPanel;

    MeshSettingsWindow() throws HeadlessException {
        this.setIconImage(IconLoader.get("gears.png", 0, 0).getImage());
        setTitle("Settings");
        setSize(440, 220);
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        add(tabbedPane, BorderLayout.CENTER);

        // Add tabs of settings
        bitSettingsPanel = new TabContentPanel(CraftConfig.bitSettings);
        tabbedPane.addTab("Bit", new JScrollPane(bitSettingsPanel));
        slicerSettingsPanel = new TabContentPanel(CraftConfig.slicerSettings);
        tabbedPane.addTab("Slicer", new JScrollPane(slicerSettingsPanel));
        assemblerSettingsPanel = new TabContentPanel(CraftConfig.assemblerSettings);
        tabbedPane.addTab("Assembler", new JScrollPane(assemblerSettingsPanel));
        xmlSettingsPanel = new TabContentPanel(CraftConfig.xmlSettings);
        tabbedPane.addTab("XML", new JScrollPane(xmlSettingsPanel));
        printerSettingsPanel = new TabContentPanel(CraftConfig.printerSettings);
        tabbedPane.addTab("Printer", new JScrollPane(printerSettingsPanel));

        // Default button
        JPanel dummy = new JPanel();
        add(dummy, BorderLayout.SOUTH);
        dummy.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JButton resetDefault = new JButton("Reset to default value");
        resetDefault.addActionListener(e -> reset());
        dummy.add(resetDefault);

        JButton okButton = new JButton("Ok");
        okButton.addActionListener(e -> {
            JComponent comp = (JComponent) e.getSource();
            Window win = SwingUtilities.getWindowAncestor(comp);
            win.dispose();
        });
        dummy.add(okButton);

        setLocationRelativeTo(null);
        setVisible(false);
    }

    private void reset() {
        bitSettingsPanel.reset();
        slicerSettingsPanel.reset();
        assemblerSettingsPanel.reset();
        xmlSettingsPanel.reset();
        printerSettingsPanel.reset();
    }

    private class TabContentPanel extends JPanel {
        TabContentPanel(List<Field> fieldList) {
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setLayout(new GridLayout(0, 1, 5, 5));
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

            for (Field field : fieldList) {
                if (field.isAnnotationPresent(DoubleSetting.class))
                    add(new LabeledSpinner(field, field.getAnnotation(DoubleSetting.class)));
                else if (field.isAnnotationPresent(FloatSetting.class))
                    add(new LabeledSpinner(field, field.getAnnotation(FloatSetting.class)));
                else if (field.isAnnotationPresent(IntegerSetting.class))
                    add(new LabeledSpinner(field, field.getAnnotation(IntegerSetting.class)));
                // TODO renderer of string setting
            }
        }

        void reset() {
            for (Component component : getComponents()) {
                if (component instanceof LabeledSpinner)
                    ((LabeledSpinner) component).reset();
            }
        }
    }
}
