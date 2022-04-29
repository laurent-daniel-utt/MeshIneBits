/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas..
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

package patternTemplate;

import java.util.logging.Handler;
import java.util.logging.Level;
import meshIneBits.config.CraftConfig;
import meshIneBits.patterntemplates.UnitSquarePattern;
import org.junit.jupiter.api.BeforeEach;

public class UnitSquarePatternIntegrityTest extends PatternIntegrityTest {

  static {
    logger = meshIneBits.util.Logger.createSimpleInstanceFor(UnitSquarePatternIntegrityTest.class);
  }

  @BeforeEach
  void setUp() {
    pattern = new UnitSquarePattern();
    UnitSquarePattern p = (UnitSquarePattern) pattern;
    logger.info("applyQuickRegroup=true");
    p.setApplyQuickRegroup(true);
    logger.info("limitAction=10000");
    p.setLimitAction(10000);
    logger.info("cutDetails=true");
    p.setCutDetails(true);
    CraftConfig.templateChoice = pattern;
    // Reduce the logger's verbalism
    UnitSquarePattern.LOGGER.setLevel(Level.INFO);
    for (Handler handler : UnitSquarePattern.LOGGER.getHandlers()) {
      handler.setLevel(Level.INFO);
    }
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
