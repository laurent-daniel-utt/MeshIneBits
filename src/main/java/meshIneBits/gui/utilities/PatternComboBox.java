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

package meshIneBits.gui.utilities;

import meshIneBits.config.patternParameter.PatternParameter;
import meshIneBits.patterntemplates.PatternTemplate;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatternComboBox extends JPanel {
    private List<PatternTemplate> choices;
    private Map<PatternTemplate, ImageIcon> iconMap = new HashMap<>();
    private Map<PatternTemplate, String> descriptionMap = new HashMap<>();
    private JLabel currentChoiceLabel = new JLabel();
    private PatternTemplate currentChoice;
    private JPopupMenu patternsPopupMenu;
    private JButton choosingButton = new JButton(IconLoader.get("angle-down.png", 14, 14)) {
        private final Dimension s = new Dimension(16, 16);

        @Override
        public Dimension getMinimumSize() {
            return s;
        }

        @Override
        public Dimension getPreferredSize() {
            return s;
        }

        @Override
        public Dimension getMaximumSize() {
            return s;
        }
    };
    /**
     * Location to show pattern parameters
     */
    private JPanel parametersPanel;

    public PatternComboBox(List<PatternTemplate> choices, JPanel parametersPanel) {
        this.choices = choices;
        this.parametersPanel = parametersPanel;
        this.setLayout(new FlowLayout(FlowLayout.TRAILING));
        choices.forEach(patternTemplate -> {
            iconMap.put(patternTemplate, IconLoader.get(patternTemplate.getIconName(), 30, 30));
            descriptionMap.put(patternTemplate, getDescriptiveText(patternTemplate));
        });
        this.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        this.add(currentChoiceLabel);
        this.add(choosingButton);
        choosingButton.addActionListener(e ->
                patternsPopupMenu.show(
                        choosingButton,
                        0,
                        choosingButton.getHeight() + 5)); // padding
        this.patternsPopupMenu = new PatternsPopupMenu();
        // Set default choice
        setCurrentChoice(choices.get(0));
    }

    private String getDescriptiveText(PatternTemplate patternTemplate) {
        return "<html><div>" +
                "<p><strong>" + patternTemplate.getCommonName() + "</strong></p>" +
                "<p>" + patternTemplate.getDescription() + "</p>" +
                "<p><strong>How-To-Use</strong><br/>" + patternTemplate.getHowToUse() + "</p>" +
                "</div></html>";
    }

    public PatternTemplate getCurrentChoice() {
        return currentChoice;
    }

    public void setCurrentChoice(PatternTemplate currentChoice) {
        this.currentChoice = currentChoice;
        updateCurrentChoiceLabel();
        updateParametersPanel(currentChoice);
    }

    private void updateParametersPanel(PatternTemplate patternTemplate) {
        parametersPanel.removeAll();
        for (PatternParameter paramConfig : patternTemplate.getPatternConfig().values()) {
            parametersPanel.add(paramConfig.getRenderer());
        }
    }

    private void updateCurrentChoiceLabel() {
        this.currentChoiceLabel.setText(currentChoice.getCommonName());
        this.currentChoiceLabel.setIcon(iconMap.get(currentChoice));
        this.currentChoiceLabel.setToolTipText(descriptionMap.get(currentChoice));
    }

    private class PatternsPopupMenu extends JPopupMenu {
        public PatternsPopupMenu() {
            choices.forEach(this::addChoice);
        }

        private void addChoice(PatternTemplate patternTemplate) {
            JMenuItem newPatternChoice = new JMenuItem(patternTemplate.getCommonName());
            newPatternChoice.setIcon(iconMap.get(patternTemplate));
            add(newPatternChoice);
            newPatternChoice.addActionListener(e -> setCurrentChoice(patternTemplate));
        }
    }
}
