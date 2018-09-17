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

import meshIneBits.config.CraftConfig;
import meshIneBits.patterntemplates.ClassicBrickPattern;
import org.junit.jupiter.api.BeforeEach;


public class ClassicBrickPatternIntegrityTest extends PatternIntegrityTest {

    static {
        logger = meshIneBits.util.Logger.createSimpleInstanceFor(ClassicBrickPatternIntegrityTest.class);
    }

    @BeforeEach
    void setUp() {
        pattern = new ClassicBrickPattern();
        CraftConfig.templateChoice = pattern;
    }

    @Override
    protected void checkSlicedPart() {
        // Nothing to check
    }

    @Override
    protected void checkPavedMesh() {
    }

}