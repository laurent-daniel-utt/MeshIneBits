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
import meshIneBits.util.AreaTool;
import meshIneBits.util.Vector2;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

/**
 *
 */
public class DiagonalHerringbonePattern extends PatternTemplate {

    private Vector2 patternStart;
    private Vector2 patternEnd;

    @Override
    public Pavement pave(Layer layer) {
        Vector<Bit2D> bits = pave(layer.getLayerNumber(), patternStart, patternEnd);
        return new Pavement(bits, new Vector2(1, 0)); // every pattern have no rotation in that template
    }

    @Override
    public Pavement pave(Layer layer, Area area) {
        // Start
        area.intersect(AreaTool.getAreaFrom(layer.getHorizontalSection()));
        Rectangle2D.Double bounds = (Rectangle2D.Double) area.getBounds2D();
        Vector2 patternStart = new Vector2(bounds.x, bounds.y);
        Vector2 patternEnd = new Vector2(bounds.x + bounds.width, bounds.y + bounds.height);
        Vector<Bit2D> bits = pave(layer.getLayerNumber(), patternStart, patternEnd);
        Pavement pavement = new Pavement(bits, new Vector2(1, 0));
        pavement.computeBits(area);
        return pavement;
    }

    private Vector<Bit2D> pave(int layerNumber, Vector2 patternStart, Vector2 patternEnd) {
        // Setup parameters
        double bitsOffset = (double) config.get("bitsOffset").getCurrentValue();
        // Start
        Vector<Bit2D> bits = new Vector<>();
        double xOffSet = Math.sqrt(2.0) / 2.0 * CraftConfig.bitLength + bitsOffset;
        double yOffSet = Math.sqrt(2.0) / 2.0 * CraftConfig.bitWidth + bitsOffset;
        for (double i = patternStart.x; i <= patternEnd.x; i = i + 2 * xOffSet) {
            for (double j = patternStart.y; j <= patternEnd.y; j = j + 2 * yOffSet) {
                Vector2 originBit;
                Vector2 orientationBit;
                double layerOffSet = 0; // In this pattern we apply an offset on 1 layer on 2
                if (layerNumber % 2 == 0) {
                    layerOffSet = yOffSet;
                }
                originBit = new Vector2(i, j + layerOffSet);
                orientationBit = new Vector2(1, 1);
                bits.add(new Bit2D(originBit, orientationBit));
            }
        }
        for (double i = patternStart.x + xOffSet; i <= patternEnd.x; i = i + 2 * xOffSet) {
            for (double j = patternStart.y + yOffSet; j <= patternEnd.y; j = j + 2 * yOffSet) {
                Vector2 originBit;
                Vector2 orientationBit;
                double layerOffSet = 0; // In this pattern we apply an offset on 1 layer on 2
                if (layerNumber % 2 == 0) {
                    layerOffSet = yOffSet;
                }
                originBit = new Vector2(i, j + layerOffSet);
                orientationBit = new Vector2(-1, 1);
                bits.add(new Bit2D(originBit, orientationBit));
            }
        }
        return bits;
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
        return "Diagonal Herringbone Pattern";
    }

    @Override
    public String getIconName() {
        return "pattern-diagonal-herringbone.png";
    }

    @Override
    public String getDescription() {
        return "A rather usual pattern. No auto-optimization implemented yet.";
    }

    @Override
    public String getHowToUse() {
        return "Choose the gap you desired.";
    }

    @Override
    public void initiateConfig() {
        config.add(new DoubleParam("bitsOffset", "Space between bits", "The horizontal and vertical gap in mm", 1.0,
                100.0, 3.0, 1.0));
    }

    @Override
    public boolean ready(Mesh mesh) {
        double skirtRadius = mesh.getSkirtRadius();
        double maxiSide = Math.max(CraftConfig.bitLength, CraftConfig.bitWidth);
        this.patternStart = new Vector2(-skirtRadius - maxiSide, -skirtRadius - maxiSide);
        this.patternEnd = new Vector2(skirtRadius + maxiSide, skirtRadius + maxiSide);
        return true;
    }
}