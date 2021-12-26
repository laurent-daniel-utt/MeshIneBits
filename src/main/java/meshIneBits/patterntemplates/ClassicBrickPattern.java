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
 * Copyright (C) 2020 CLAIRIS Etienne & RUSSO André.
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

import meshIneBits.Bit2D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.Pavement;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Vector2;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simplest pattern possible: a grid with a rotation of 90° 1 layer on 2. There
 * is no auto-optimization implemented in this class.
 */

public class ClassicBrickPattern extends PatternTemplate {
    //lengthBit can change
    @Override
    public Pavement pave(Layer layer) {
        Area areaToPave = AreaTool.getAreaFrom(layer.getHorizontalSection());
        Vector2[] p = calculatePatternStartEnd(areaToPave);
        // layer with even index will have a 90° rotation
        Collection<Bit2D> bits;
        if (layer.getLayerNumber() % 2 == 0) {
            bits = paveVertically(p[0], p[1]);
        } else {
            bits = paveHorizontally(p[0], p[1]);
        }
        return new Pavement(bits);
    }

    @Override
    public Pavement pave(Layer layer, Area area) {
        area.intersect(AreaTool.getAreaFrom(layer.getHorizontalSection()));
        Vector2[] p = calculatePatternStartEnd(area);

        // layer with even index will have a 90° rotation
        Collection<Bit2D> bits;
        if (layer.getLayerNumber() % 2 == 0) {
            bits = paveVertically(p[0], p[1]);
        } else {
            bits = paveHorizontally(p[0], p[1]);
        }
        Pavement pavement = new Pavement(bits);
        pavement.computeBits(area);
        return pavement;
    }

    private Vector2[] calculatePatternStartEnd(Area area) {
        Rectangle2D.Double bounds = (Rectangle2D.Double) area.getBounds2D();
        Vector2 patternStart = new Vector2(bounds.x, bounds.y);
        Vector2 patternEnd = new Vector2(bounds.x + bounds.width, bounds.y + bounds.height);
        return new Vector2[]{patternStart, patternEnd};
    }

    /**
     * Pave horizontally. Left to right, top to bottom
     *
     * @param patternStart
     * @param patternEnd
     * @return
     */
    private Collection<Bit2D> paveHorizontally(Vector2 patternStart, Vector2 patternEnd) {
        // Setup parameters
        double bitsOffset = (double) config.get("bitsOffset").getCurrentValue();
        // Start
        List<Bit2D> bits = new ArrayList<>();
        Vector2 coo = patternStart.add(new Vector2(CraftConfig.lengthFull / 2, CraftConfig.bitWidth / 2));
        int column = 0;
        while (coo.x < patternEnd.x + CraftConfig.lengthFull / 2) {
            while (coo.y < patternEnd.y + CraftConfig.bitWidth / 2) {
                // every bits have no rotation in that template
                bits.add(new Bit2D(coo, new Vector2(1, 0)));
                coo = coo.add(new Vector2(0, CraftConfig.bitWidth + bitsOffset));
            }
            column++;
            coo = new Vector2(patternStart.x + CraftConfig.lengthFull / 2 + (CraftConfig.lengthFull + bitsOffset) * column,
                    patternStart.y + CraftConfig.bitWidth / 2);
        }
        return bits;
    }

    /**
     * Pave vertically. Left to right, top to bottom
     *
     * @param patternStart
     * @param patternEnd
     * @return
     */
    private Collection<Bit2D> paveVertically(Vector2 patternStart, Vector2 patternEnd) {
        // Setup parameters
        double bitsOffset = (double) config.get("bitsOffset").getCurrentValue();
        // Start
        List<Bit2D> bits = new ArrayList<>();
        Vector2 coo = patternStart.add(new Vector2(CraftConfig.bitWidth / 2, CraftConfig.lengthFull / 2));
        int line = 0;
        while (coo.y < patternEnd.y + CraftConfig.lengthFull / 2) {
            while (coo.x < patternEnd.x + CraftConfig.bitWidth / 2) {
                bits.add(new Bit2D(coo, new Vector2(0, 1)));
                coo = coo.add(new Vector2(CraftConfig.bitWidth + bitsOffset, 0));
            }
            line++;
            coo = new Vector2(patternStart.x + CraftConfig.bitWidth / 2,
                    patternStart.y + CraftConfig.lengthFull / 2 + (CraftConfig.lengthFull + bitsOffset) * line);
        }
        return bits;
    }

    @Override
    public int optimize(Layer actualState) {
        return -2;
    }

    @Override
    public String getCommonName() {
        return "Classic Brick Pattern";
    }

    @Override
    public String getIconName() {
        return "pattern-classic-brick.png";
    }

    @Override
    public String getDescription() {
        return "The simplest pattern: a grid with a rotation of 90° on odd index layer. "
                + "There is no auto-optimization implemented in this class.";
    }

    @Override
    public String getHowToUse() {
        return "Choose the gap you desire.";
    }

    @Override
    public boolean ready(Mesh mesh) {
        return true;
    }

    @Override
    public void initiateConfig() {
        // This template only need the distance between bits
        config.add(new DoubleParam(
                "bitsOffset",
                "Space between bits",
                "The horizontal and vertical gap in mm",
                1.0,
                100.0,
                3.0,
                1.0));
    }
}
