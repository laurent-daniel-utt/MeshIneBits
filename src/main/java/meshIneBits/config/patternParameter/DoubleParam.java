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

package meshIneBits.config.patternParameter;

import java.util.List;
import meshIneBits.gui.utilities.patternParamRenderer.LabeledSpinner;
import meshIneBits.gui.utilities.patternParamRenderer.Renderer;

/**
 * To be used in couple with {@link LabeledSpinner}
 *
 * @author Quoc Nhat Han TRAN
 */
public class DoubleParam extends PatternParameter {

  /**
   *
   */
  private static final long serialVersionUID = 9178756965959306841L;
  private Double minValue;
  private Double maxValue;
  private Double defaultValue;
  private Double step;
  private Double currentValue;

  /**
   * This will validate each input. If one does not satisfy, it will bring the default value. Be
   * careful of {@link List}. Should use a real provided class implementing {@link List}, not by
   * wrapping.
   *
   * @param name         Should be unique among parameters of a pattern
   * @param title        no constraint
   * @param description  no constraint
   * @param minValue     defines minimum of range of selection. Default = {@link Double#MIN_VALUE}
   * @param maxValue     defines maximum of range of selection. Default = {@link Double#MAX_VALUE}
   * @param defaultValue Round up by {@link #step}
   *                     <ul>
   *                     <li><tt>0.0</tt> if {@link Double#isInfinite()} or
   *                     {@link Double#isNaN()} or <tt>null</tt></li>
   *                     <li>{@link #minValue} if lower than {@link #minValue}</li>
   *                     <li>{@link #maxValue} if higher than {@link #maxValue}</li>
   *                     </ul>
   * @param step         if {@link Double#isInfinite()} or {@link Double#isNaN()} or
   *                     <tt>null</tt> or <tt>0</tt>, {@link #step} will be set to
   *                     <tt>1.0</tt><br>
   *                     if negative, {@link #step} will be the opposite value
   */
  public DoubleParam(String name, String title, String description, Double minValue,
      Double maxValue,
      Double defaultValue, Double step) {
    this.codename = name;
    this.title = title;
    this.description = description;
    setMin(minValue);
    setMax(maxValue);
    checkMinMax();
    setStep(step);
    setDefault(defaultValue);
    // Setup temporary currentValue
    this.currentValue = this.defaultValue;
  }

  /**
   * @param d default value
   * @see #filter(Double)
   */
  private void setDefault(Double d) {
    this.defaultValue = filter(d);
  }

  /**
   * @param s if {@link Double#isInfinite()} or {@link Double#isNaN()} or
   *          <tt>null</tt> or <tt>0</tt>, {@link #step} will be set to
   *          <tt>1.0</tt><br>
   *          if negative, {@link #step} will be the opposite value
   */
  private void setStep(Double s) {
    if (Double.isInfinite(s) || Double.isNaN(s) || s == 0) {
      this.step = 1.0;
    } else {
      this.step = (s < 0 ? -s : s);
    }
  }

  /**
   * @param minValue if {@link Double#isInfinite()} or {@link Double#isNaN()} holds true, the {@link
   *                 #minValue} will be set to {@link Double#MIN_VALUE}
   */
  private void setMin(Double minValue) {
    if (Double.isInfinite(minValue) || Double.isNaN(minValue)) {
      this.minValue = Double.MIN_VALUE;
    } else {
      this.minValue = minValue;
    }
  }

  /**
   * @param maxValue if {@link Double#isInfinite()} or {@link Double#isNaN()} holds true, the {@link
   *                 #maxValue} will be set to {@link Double#MAX_VALUE}
   */
  private void setMax(Double maxValue) {
    if (Double.isInfinite(maxValue) || Double.isNaN(maxValue)) {
      this.maxValue = Double.MAX_VALUE;
    } else {
      this.maxValue = maxValue;
    }
  }

  /**
   * Swap min and max if min > max
   */
  private void checkMinMax() {
    if (minValue > maxValue) {
      double c = minValue;
      minValue = maxValue;
      maxValue = c;
    }
  }

  /**
   * Filter an input value after setting up {@link #minValue}, {@link #maxValue}, {@link #step}
   *
   * @param d input value
   * @return Round up by {@link #step}
   * <ul>
   * <li><tt>0.0</tt> if {@link Double#isInfinite()} or
   * {@link Double#isNaN()} or <tt>null</tt></li>
   * <li>{@link #minValue} if lower than {@link #minValue}</li>
   * <li>{@link #maxValue} if higher than {@link #maxValue}</li>
   * </ul>
   */
  private double filter(Double d) {
    // Check extreme
    if (Double.isInfinite(d) || Double.isNaN(d)) {
      return 0.0;
    }
    // Check in range
    if (d < this.minValue) {
      return this.minValue;
    }
    if (d > this.maxValue) {
      return this.maxValue;
    }
    // Round up
    double sgn = Math.signum(d);
    if (sgn == 0) {
      return d;
    }
    if (sgn < 0) {
      d = d * sgn;
    }
    double residu = d - ((int) (d / step)) * step;
    if (residu < 0.5 * step) {
      d = (d - residu) * sgn;
    } else {
      d = (d - residu + step) * sgn;
    }
    return d;
  }

  /**
   * @param newCurrentValue will be filtered before affecting, else <tt>defaultValue</tt>
   */
  public void setCurrentValue(Object newCurrentValue) {
    if (!(newCurrentValue instanceof Double)) {
      this.currentValue = this.defaultValue;
    } else {
      this.currentValue = filter((Double) newCurrentValue);
    }
  }

  @Override
  public String toString() {
    return "Double[name=" + codename + ", title=" + title + ", description=" + description
        + ", minValue="
        + minValue + ", maxValue=" + maxValue + ", defaultValue=" + defaultValue + ", step=" + step
        + ", currentValue=" + currentValue + "]";
  }

  public Double getDefaultValue() {
    return defaultValue;
  }

  @Override
  public Double getCurrentValue() {
    return currentValue;
  }

  @Override
  public String getCodename() {
    return codename;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public Double getMinValue() {
    return minValue;
  }

  public Double getMaxValue() {
    return maxValue;
  }

  public Double getStep() {
    return step;
  }

  @Override
  public Renderer getRenderer() {
    return new LabeledSpinner(this);
  }

  /**
   * @param amount positive to increment {@link #currentValue} or negative to decrement. Can not
   *               trespass {@link #maxValue} and {@link #minValue}
   * @param cyclic <tt>true</tt> to recount from {@link #minValue} when
   *               greater than {@link #maxValue}
   */
  public void incrementBy(double amount, boolean cyclic) {
    double oldValue = currentValue;
    if (cyclic) {
      double newValue = currentValue + amount;
      if (newValue > maxValue) // Positive amount
      {
        currentValue = minValue
            + newValue - maxValue
            - Math.floor((newValue - maxValue) / (maxValue - minValue)) * (maxValue - minValue);
      } else if (newValue < minValue) // Negative amount
      {
        currentValue = maxValue
            - ((minValue - newValue)
            - Math.floor((minValue - newValue) / (maxValue - minValue)) * (maxValue - minValue));
      } else {
        currentValue = newValue;
      }
    } else {
      currentValue = filter(currentValue + amount);
    }
    changes.firePropertyChange("currentValue", oldValue, currentValue);
  }
}
