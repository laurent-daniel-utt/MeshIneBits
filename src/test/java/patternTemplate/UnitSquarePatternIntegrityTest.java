/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
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

import meshIneBits.Layer;
import meshIneBits.config.CraftConfig;
import meshIneBits.patterntemplates.UnitSquarePattern;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
	}

	@Override
	protected void checkSlicedPart() {
		// Nothing to check
	}

	@Override
	protected void checkGeneratedPart() {
		for (Layer layer : part.getLayers()) {
			// Assure each layer is empty
			assertEquals(0, layer.getSelectedPattern().getBitsKeys().size(),
					"Layer " + layer.getLayerNumber() + " should be empty");
		}
	}
}
