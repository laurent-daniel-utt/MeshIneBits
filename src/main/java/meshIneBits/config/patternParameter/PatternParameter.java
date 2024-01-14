/*
 * MeshIneBits is a Java software to disintegrate a 3d project (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2022 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
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
 *
 */

package meshIneBits.config.patternParameter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import meshIneBits.gui.utilities.patternParamRenderer.Renderer;

/**
 * Describe a parameter of pattern <br>
 *
 * @author NHATHAN
 */
public abstract class PatternParameter implements Serializable {

  /**
   *
   */
  static final long serialVersionUID = -1032848466522000185L;
  String title;
  String codename;
  String description;
  transient PropertyChangeSupport changes = new PropertyChangeSupport(this);

  /**
   * Type of current value depends on sub class
   *
   * @return predictable type
   */
  public abstract Object getCurrentValue();

  /**
   * This method will be use by interfaces on each change event
   *
   * @param newValue will be filtered before affecting, in the same way of
   *                 <tt>defaultValue</tt>
   */
  public abstract void setCurrentValue(Object newValue);

  /**
   * @return name of parameters. Should be different among parameters.
   */
  public String getCodename() {
    return codename;
  }

  /**
   * @return human-readable name
   */
  public String getTitle() {
    return title;
  }

  /**
   * @return what it is and how to use
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return encoded string
   */
  public abstract String toString();

  /**
   * @return gui of parameter
   */
  public abstract Renderer getRenderer();

  public void addPropertyChangeListener(PropertyChangeListener l) {
    changes.addPropertyChangeListener(l);
  }

  public void removePropertyChangeListener(PropertyChangeListener l) {
    changes.removePropertyChangeListener(l);
  }
}
