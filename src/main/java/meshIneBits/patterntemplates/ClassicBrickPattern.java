/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 Vallon BENJAMIN.
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
import meshIneBits.util.Vector2;

import java.util.Vector;

/**
 * Simplest pattern possible: a grid with a rotation of 90° 1 layer on 2. There
 * is no auto-optimization implemented in this class.
 */

public class ClassicBrickPattern extends PatternTemplate {

    private Vector2 patternStart;
    private Vector2 patternEnd;

    @Override
    public Pavement pave(Layer layer) {
        // Setup parameters
        double bitsOffset = (double) config.get("bitsOffset").getCurrentValue();
        // Start
        Vector<Bit2D> bits = new Vector<>();
        Vector2 coo = patternStart;
        int column = 1;
        while (coo.x <= patternEnd.x) {
            while (coo.y <= patternEnd.y) {
                // every bits have no rotation in that template
                bits.add(new Bit2D(coo, new Vector2(1, 0)));
                coo = coo.add(new Vector2(0, CraftConfig.bitWidth + bitsOffset));
            }
            coo = new Vector2(patternStart.x + (CraftConfig.bitLength + bitsOffset) * column, patternStart.y);
            column++;
        }
        // in this pattern 1 layer on 2 has a 90° rotation
        Vector2 rotation = new Vector2(1, 0);
        if (layer.getLayerNumber() % 2 == 0) {
            rotation = new Vector2(0, 1);
        }
        return new Pavement(bits, rotation);
    }

    @Override
    public int optimize(Layer actualState) {
        return -2;
    }

    @Override
    public Vector2 moveBit(Pavement actualState, Vector2 bitKey, Vector2 localDirection) {
        double distance = 0;
        if (localDirection.x == 0) {// up or down
            distance = CraftConfig.bitWidth / 2;
        } else if (localDirection.y == 0) {// left or right
            distance = CraftConfig.bitLength / 2;
        }
        return this.moveBit(actualState, bitKey, localDirection, distance);
    }

    @Override
    public Vector2 moveBit(Pavement actualState, Vector2 bitKey, Vector2 localDirection, double distance) {
        return actualState.moveBit(bitKey, localDirection, distance);
    }

    @Override
    public String getCommonName() {
        return "Classic Brick Pattern";
    }

    @Override
    public String getIconName() {
        return "p1.png";
    }

    @Override
    public String getDescription() {
        return "The simplest pattern: a grid with a rotation of 90° 1 layer on 2. "
                + "There is no auto-optimization implemented in this class.";
    }

    @Override
    public String getHowToUse() {
        return "Choose the gap you desire.";
    }

    @Override
    public boolean ready(Mesh mesh) {
        // Setting the skirtRadius and starting/ending points
        double skirtRadius = mesh.getSkirtRadius();
        double maxiSide = Math.max(CraftConfig.bitLength, CraftConfig.bitWidth);
        this.patternStart = new Vector2(-skirtRadius - maxiSide, -skirtRadius - maxiSide);
        this.patternEnd = new Vector2(skirtRadius + maxiSide, skirtRadius + maxiSide);
        return true;
    }

    @Override
    public void initiateConfig() {
        // This template only need the distance between bits
        config.add(new DoubleParam("bitsOffset", "Space between bits", "The horizontal and vertical gap in mm", 1.0,
                100.0, 3.0, 1.0));
    }
}
