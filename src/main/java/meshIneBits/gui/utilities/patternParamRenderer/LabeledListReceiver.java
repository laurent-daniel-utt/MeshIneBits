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
 * Copyright (C) 2020 CLAIRIS Etienne & RUSSO André.
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

package meshIneBits.gui.utilities.patternParamRenderer;

import meshIneBits.config.patternParameter.DoubleListParam;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains a label and a text area, which receive multiple input.<br>
 * Renders {@link DoubleListParam}
 *
 * @author NHATHAN
 */
public class LabeledListReceiver extends Renderer {

    private JLabel btnName;
    private DoubleListParam dlconfig;
    private static String delimiter = ";";
    private static String msgInstruction = "<html>" + "<p>Please enter a new array of double values</p>"
            + "<p><small>Use " + delimiter + " to separate values and . to mark decimal point.</small></p>" + "</html>";

    /**
     *
     */
    private static final long serialVersionUID = 5463905865176363388L;

    /**
     * Render {@link DoubleListParam}
     *
     * @param config predefined parameter
     */
    public LabeledListReceiver(DoubleListParam config) {
        this.dlconfig = config;
        // Visual options
        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(4, 0, 0, 0));

        // Setting up
        btnName = new JLabel(config.getTitle());
        this.add(btnName, BorderLayout.WEST);
        btnName.setToolTipText(generateToolTipText());
        JButton btnChangeValue = new JButton("Modify");
        this.add(btnChangeValue, BorderLayout.EAST);
        btnChangeValue.addActionListener(e -> {
            String s = (String) JOptionPane.showInputDialog(null, msgInstruction, convertToString());
            if (s != null) {
                // Just change the value in case
                // user hits Ok button
                LabeledListReceiver.this.dlconfig.setCurrentValue(parseToList(s));
                btnName.setToolTipText(generateToolTipText());
            }
        });
    }

    private String generateToolTipText() {
        StringBuilder str = new StringBuilder();
        str.append("<html><div>");
        str.append("<p>" + dlconfig.getDescription() + "</p>");
        str.append("<p><strong>Current Value</strong><br/>" + convertToString() + "</p>");
        str.append("</div></html>");
        return str.toString();
    }

    private String convertToString() {
        List<Double> l = dlconfig.getCurrentValue();
        StringBuilder str = new StringBuilder();
        if (!l.isEmpty()) {
            for (Iterator<Double> iterator = l.iterator(); iterator.hasNext(); ) {
                Double obj = iterator.next();
                if (iterator.hasNext()) {
                    str.append(obj + " ; ");
                } else {
                    str.append(obj);
                }
            }
        }
        return str.toString();
    }

    private List<Double> parseToList(String s) {
        if (s == null)
            return new ArrayList<>();
        String[] values = s.split(delimiter, 0);
        ArrayList<Double> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            try {
                list.add(Double.valueOf(values[i]));
            } catch (Exception e) {
                continue;
            }
        }
        return list;
    }
}
