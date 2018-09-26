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

import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.Pavement;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.PatternConfig;
import meshIneBits.util.Vector2;

import java.util.Vector;

/**
 * This pattern does not do anything in process. In fact, it allows user to
 * freely construct a layer with other provided tools
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
        return new Pavement(new Vector<>(), new Vector2(1, 0));
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

    /**
     * @return the full name of icon representation the template
     */
    public String getIconName() {
        return "default-template-icon.png";
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
