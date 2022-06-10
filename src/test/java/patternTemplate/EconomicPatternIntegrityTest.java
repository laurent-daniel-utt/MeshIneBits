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

package patternTemplate;

import meshIneBits.config.CraftConfig;
import meshIneBits.patterntemplates.EconomicPattern;
import org.junit.jupiter.api.BeforeEach;


public class EconomicPatternIntegrityTest extends PatternIntegrityTest {

  static {
    logger = meshIneBits.util.Logger.createSimpleInstanceFor(EconomicPatternIntegrityTest.class);
  }

  @BeforeEach
  void setUp() {
    pattern = new EconomicPattern();
    CraftConfig.templateChoice = pattern;
  }

  @Override
  protected void checkSlicedPart() {
    // Nothing to check
  }

  @Override
  protected void checkPavedMesh() {
//        for (Layer layer : mesh.getLayers()) {
//            // Assure each layer is empty
//            assertEquals(0, layer.getFlatPavement().getBitsKeys().size(),
//                    "Layer " + layer.getLayerNumber() + " should be empty");
//        }
    // Nothing to check
  }

}
