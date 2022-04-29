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

package meshIneBits.patterntemplates;

import java.awt.geom.Area;
import java.util.ArrayList;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.Pavement;
import meshIneBits.config.PatternConfig;

/**
 * This pattern does not do anything in process. In fact, it allows user to freely construct a layer
 * with other provided tools
 */
public class ManualPattern extends PatternTemplate {

  @Override
  protected void initiateConfig() {
    // Nothing
  }

  @Override
  public boolean ready(Mesh mesh) {
    return true;
  }

  @Override
  public Pavement pave(Layer layer) {
    return new Pavement(new ArrayList<>());
  }

  @Override
  public Pavement pave(Layer layer, Area area) {
    return new Pavement(new ArrayList<>());
  }

  @Override
  public int optimize(Layer actualState) {
    return -2;
  }

  /**
   * @return the full name of icon representation the template
   */
  public String getIconName() {
    return "pattern-manual.png";
  }

  /**
   * @return the common name of the template
   */
  public String getCommonName() {
    return "Manual Pattern";
  }

  /**
   * @return a block of text of description about this template
   */
  public String getDescription() {
    return "A white paper in which you can draw as you like. "
        + "No optimizing or paving algorithm is implemented.";
  }

  /**
   * @return a block of text about how to use this template
   */
  public String getHowToUse() {
    return "Use the provided tools to pave bits into layer.";
  }

  public PatternConfig getPatternConfig() {
    return config;
  }
}
