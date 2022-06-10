/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
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

import meshIneBits.gui.utilities.patternParamRenderer.Checkbox;
import meshIneBits.gui.utilities.patternParamRenderer.Renderer;

/**
 * @author Quoc Nhat Han TRAN
 */
public class BooleanParam extends PatternParameter {

  /**
   *
   */
  private static final long serialVersionUID = -2259784876889236537L;
  private boolean currentValue;
  private boolean defaultValue;

  /**
   * @param name         Should be unique among parameters of a pattern
   * @param title        to be shown on GUI
   * @param description  to be shown in tooltip on GUI
   * @param defaultValue which value this parameter should hold at first or when meet a wrong
   *                     setting
   */
  public BooleanParam(String name, String title, String description, boolean defaultValue) {
    this.title = title;
    this.codename = name;
    this.description = description;
    this.defaultValue = defaultValue;
    this.currentValue = defaultValue;
  }

  @Override
  public Boolean getCurrentValue() {
    return currentValue;
  }

  @Override
  public void setCurrentValue(Object newValue) {
    if (newValue instanceof Boolean) {
      this.currentValue = (boolean) newValue;
    } else {
      this.currentValue = defaultValue;
    }
  }

  @Override
  public String toString() {
    return "Boolean[name=" + codename + ", title=" + title + ", description=" + description
        + ", defaultValue="
        + defaultValue + ", currentValue=" + currentValue + "]";
  }

  @Override
  public Renderer getRenderer() {
    return new Checkbox(this);
  }
}
