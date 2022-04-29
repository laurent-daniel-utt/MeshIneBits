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
package meshIneBits.gui.utilities.patternParamRenderer;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import meshIneBits.config.DoubleSetting;
import meshIneBits.config.FloatSetting;
import meshIneBits.config.IntegerSetting;
import meshIneBits.config.patternParameter.DoubleParam;

public class LabeledSpinner extends Renderer implements PropertyChangeListener {

  private static final long serialVersionUID = 6726754934854914029L;

  private FixedWidthSpinner spinner;

  private JLabel lblName;

  public void setEnabled(boolean enabled) {
    spinner.setEnabled(enabled);
    lblName.setEnabled(enabled);
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
      spinner = new FieldSpinner(
          new SpinnerNumberModel(
              field.getDouble(null),
              setting.minValue(),
              setting.maxValue(),
              setting.step()),
          setting.defaultValue());
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
      spinner = new FieldSpinner(
          new SpinnerNumberModel(
              (Number) field.getFloat(null),
              setting.minValue(),
              setting.maxValue(),
              setting.step()),
          setting.defaultValue());
      spinner.addChangeListener(e -> {
        try {
          field.setFloat(null, (Float) spinner.getValue());
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
      spinner = new FieldSpinner(
          new SpinnerNumberModel(
              field.getInt(null),
              setting.minValue(),
              setting.maxValue(),
              setting.step()),
          setting.defaultValue());
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

    spinner = new ParamSpinner(
        new SpinnerNumberModel(
            config.getCurrentValue(),
            config.getMinValue(),
            config.getMaxValue(),
            config.getStep()),
        config.getDefaultValue());
    spinner.addChangeListener(e -> config.setCurrentValue(spinner.getValue()));
    this.add(spinner, BorderLayout.EAST);
    config.addPropertyChangeListener(this);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName()
        .equals("currentValue")) {
      spinner.setValue(evt.getNewValue());
    }
  }

  public void reset() {
    try {
      spinner.reset();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private interface Resetable {

    void reset() throws IllegalAccessException;
  }

  private abstract class FixedWidthSpinner extends JSpinner implements Resetable {

    static final int MAX_CHAR_WIDTH = 6;

    FixedWidthSpinner(SpinnerModel model) {
      super(model);
      ((JSpinner.DefaultEditor) this.getEditor()).getTextField()
          .setColumns(MAX_CHAR_WIDTH);
    }
  }

  private class ParamSpinner extends FixedWidthSpinner {

    private final Object defaultValue;

    ParamSpinner(SpinnerModel model, Object defaultValue) {
      super(model);
      this.defaultValue = defaultValue;
    }

    @Override
    public void reset() {
      spinner.setValue(defaultValue);
    }
  }

  private class FieldSpinner extends FixedWidthSpinner {

    private final Object defaultValue;

    FieldSpinner(SpinnerModel model, Object defaultValue) {
      super(model);
      this.defaultValue = defaultValue;
    }

    @Override
    public void reset() {
      spinner.setValue(defaultValue); // static field
    }
  }
}