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

package meshIneBits.gui.utilities.patternParamRenderer;

import meshIneBits.config.DoubleSetting;
import meshIneBits.config.FloatSetting;
import meshIneBits.config.IntegerSetting;
import meshIneBits.config.patternParameter.DoubleParam;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;

public class LabeledSpinner extends Renderer implements PropertyChangeListener {

    private static final long serialVersionUID = 6726754934854914029L;

    private JSpinner spinner;

    private JLabel lblName;

    private JSpinner getSpinner() {
        return spinner;
    }

    private JLabel getTitle() {
        return lblName;
    }

    public void setEnabled(boolean enabled) {
        getSpinner().setEnabled(enabled);
        getTitle().setEnabled(enabled);
    }

    public LabeledSpinner(Field field, DoubleSetting setting) {
        // Visual options
        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(4, 0, 0, 0));

        // Setting up
        lblName = new JLabel(setting.title());
        lblName.setToolTipText(setting.description());
        this.add(lblName, BorderLayout.WEST);
        try {
            spinner = new FixedWidthSpinner(
                    new SpinnerNumberModel(
                            field.getDouble(null),
                            setting.minValue(),
                            setting.maxValue(),
                            setting.step()));
            spinner.addChangeListener(e -> {
                try {
                    field.setDouble(null, (double) spinner.getValue());
                } catch (IllegalArgumentException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            });
            this.add(spinner, BorderLayout.EAST);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public LabeledSpinner(Field field, FloatSetting setting) {
        // Visual options
        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(4, 0, 0, 0));

        // Setting up
        lblName = new JLabel(setting.title());
        lblName.setToolTipText(setting.description());
        this.add(lblName, BorderLayout.WEST);
        try {
            spinner = new FixedWidthSpinner(
                    new SpinnerNumberModel(
                            field.getFloat(null),
                            setting.minValue(),
                            setting.maxValue(),
                            setting.step()));
            spinner.addChangeListener(e -> {
                try {
                    field.setFloat(null, (float) spinner.getValue());
                } catch (IllegalArgumentException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            });
            this.add(spinner, BorderLayout.EAST);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public LabeledSpinner(Field field, IntegerSetting setting) {
        // Visual options
        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(4, 0, 0, 0));

        // Setting up
        lblName = new JLabel(setting.title());
        lblName.setToolTipText(setting.description());
        this.add(lblName, BorderLayout.WEST);
        try {
            spinner = new FixedWidthSpinner(
                    new SpinnerNumberModel(
                            field.getInt(null),
                            setting.minValue(),
                            setting.maxValue(),
                            setting.step()));
            spinner.addChangeListener(e -> {
                try {
                    field.setInt(null, (int) spinner.getValue());
                } catch (IllegalArgumentException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            });
            this.add(spinner, BorderLayout.EAST);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * This constructor is to render {@link DoubleParam}
     *
     * @param config predefined parameter
     */
    public LabeledSpinner(DoubleParam config) {
        // Visual options
        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(4, 0, 0, 0));

        // Setting up
        lblName = new JLabel(config.getTitle());
        lblName.setToolTipText("<html><div>" + config.getDescription() + "</div></html>");
        this.add(lblName, BorderLayout.WEST);

        spinner = new FixedWidthSpinner(new SpinnerNumberModel(
                config.getCurrentValue(),
                config.getMinValue(),
                config.getMaxValue(),
                config.getStep()));
        spinner.addChangeListener(e -> config.setCurrentValue(spinner.getValue()));
        this.add(spinner, BorderLayout.EAST);
        config.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("currentValue")) {
            spinner.setValue(evt.getNewValue());
        }
    }

    private class FixedWidthSpinner extends JSpinner {
        static final int MAX_CHAR_WIDTH = 6;

        FixedWidthSpinner(SpinnerModel model) {
            super(model);
            ((JSpinner.DefaultEditor) this.getEditor()).getTextField().setColumns(MAX_CHAR_WIDTH);
        }
    }
}