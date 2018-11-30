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

package meshIneBits.gui.view2d;

import meshIneBits.config.*;
import meshIneBits.gui.utilities.IconLoader;
import meshIneBits.gui.utilities.patternParamRenderer.LabeledSpinner;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;

class MeshSettingsWindow extends JFrame {
    MeshSettingsWindow() throws HeadlessException {
        this.setIconImage(IconLoader.get("gears.png", 0, 0).getImage());
        setTitle("Settings");
        setSize(440, 220);
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        add(tabbedPane, BorderLayout.CENTER);

        // Add tabs of settings
        tabbedPane.addTab("Bit", initSettingsPanel(CraftConfig.bitSettings));
        tabbedPane.addTab("Slicer", initSettingsPanel(CraftConfig.slicerSettings));
        tabbedPane.addTab("Assembler", initSettingsPanel(CraftConfig.assemblerSettings));
        tabbedPane.addTab("XML", initSettingsPanel(CraftConfig.xmlSettings));
        tabbedPane.addTab("Scheduler", initSettingsPanel(CraftConfig.schedulerSettings));
        tabbedPane.addTab("Printer", initSettingsPanel(CraftConfig.printerSettings));

        setLocationRelativeTo(null);
        setVisible(false);
    }

    private JPanel initSettingsPanel(List<Field> fieldList) {
        TabContentPanel panel = new TabContentPanel();
        for (Field field : fieldList) {
            if (field.isAnnotationPresent(DoubleSetting.class))
                panel.add(new LabeledSpinner(field, field.getAnnotation(DoubleSetting.class)));
            else if (field.isAnnotationPresent(FloatSetting.class))
                panel.add(new LabeledSpinner(field, field.getAnnotation(FloatSetting.class)));
            else if (field.isAnnotationPresent(IntegerSetting.class))
                panel.add(new LabeledSpinner(field, field.getAnnotation(IntegerSetting.class)));
            else if (field.isAnnotationPresent(StringSetting.class)) {
                // TODO renderer of string setting
            }
        }
        return panel;
    }

    private class TabContentPanel extends JPanel {
        TabContentPanel() {
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setLayout(new GridLayout(3, 0, 5, 5));
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        }
    }
}
