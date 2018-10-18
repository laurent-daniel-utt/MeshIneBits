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

package meshIneBits.gui.utilities;

import meshIneBits.config.CraftConfig;
import meshIneBits.patterntemplates.PatternTemplate;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Vector;

public class GalleryContainer extends OptionsContainer {

    private static final long serialVersionUID = 5081506030712556983L;

    /**
     * For the display
     */
    private JLabel chosenTemplateLabel = new JLabel();
    private JPopupMenu templatesMenu;
    private PatternParametersContainer parameterPanel;
    private PatternTemplate chosenTemplate;

    public GalleryContainer(String title) {
        super(title);
        this.setLayout(new BorderLayout());
        // For the chosen template
        setChosenTemplate(CraftConfig.templateChoice);
        setChosenTemplateLabel(chosenTemplate);
        this.add(chosenTemplateLabel, BorderLayout.CENTER);
        // For the menu
        ImageIcon image = new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/" + "angle-down.png")));
        ImageIcon icon = new ImageIcon(image.getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
        JButton choosingTemplateBtn = new JButton(icon);
        this.add(choosingTemplateBtn, BorderLayout.SOUTH);
        choosingTemplateBtn.addActionListener(e -> templatesMenu.show(choosingTemplateBtn, 0, choosingTemplateBtn.getHeight()));
        this.templatesMenu = new TemplatesMenu();
    }

    public PatternTemplate getChosenTemplate() {
        return chosenTemplate;
    }

    public void setChosenTemplate(PatternTemplate chosenTemplate) {
        this.chosenTemplate = chosenTemplate;
    }

    private String descriptiveText(PatternTemplate template) {
        return "<html><div>" +
                "<p><strong>" + template.getCommonName() + "</strong></p>" +
                "<p>" + template.getDescription() + "</p>" +
                "<p><strong>How-To-Use</strong><br/>" + template.getHowToUse() + "</p>" +
                "</div></html>";
    }

    void setParameterPanel(PatternParametersContainer patternParametersContainer) {
        parameterPanel = patternParametersContainer;
    }

    private class TemplatesMenu extends JPopupMenu {

        /**
         *
         */
        private static final long serialVersionUID = 4906068175556528411L;

        TemplatesMenu() {
            super("...");
            CraftConfig.templatesLoaded = new Vector<>(
                    Arrays.asList(CraftConfig.templatesPreloaded));
            for (PatternTemplate template : CraftConfig.templatesLoaded) {
                this.addChoice(template);
            }
        }

        void addChoice(PatternTemplate template) {
            JMenuItem newChoice = new JMenuItem(template.getCommonName());
            ImageIcon image = new ImageIcon(
                    Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/" + template.getIconName())));
            ImageIcon icon = new ImageIcon(image.getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
            newChoice.setIcon(icon);
            this.add(newChoice);
            newChoice.addActionListener(e -> {
                setChosenTemplate(template);
                setChosenTemplateLabel(chosenTemplate);
                parameterPanel.setupPatternParameters();
            });
        }
    }

    private void setChosenTemplateLabel(PatternTemplate template) {
        this.chosenTemplateLabel.setText(template.getCommonName());
        ImageIcon image = new ImageIcon(
                Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/" + template.getIconName())));
        ImageIcon icon = new ImageIcon(image.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
        this.chosenTemplateLabel.setIcon(icon);
        this.chosenTemplateLabel.setToolTipText(descriptiveText(template));
    }
}