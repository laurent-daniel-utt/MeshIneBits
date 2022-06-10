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

package meshIneBits.patterntemplates;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Vector;
import java.util.stream.Collectors;
import meshIneBits.Bit2D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.NewBit2D;
import meshIneBits.Pavement;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Vector2;

/**
 *
 */
public class DiagonalHerringbonePattern extends PatternTemplate {

  @Override
  public Pavement pave(Layer layer) {
    Area area = AreaTool.getAreaFrom(layer.getHorizontalSection());
    // Rotate area
    double diffRotation = Math.toRadians(
        ((Double) config.get("diffRotation")
            .getCurrentValue()) * layer.getLayerNumber());
    AffineTransform t1 = AffineTransform.getRotateInstance(-diffRotation),
        t2 = AffineTransform.getRotateInstance(diffRotation);
    area.transform(t1);
    // Pave
    Vector2[] p = calculatePatternStartEnd(area);
    Collection<Bit2D> bits = pave(layer.getLayerNumber(), p[0], p[1]).stream()
        .map(bit2D -> bit2D.createTransformedBit(t2))
        .collect(Collectors.toList());
    return new Pavement(bits);
  }

  @Override
  public Pavement pave(Layer layer, Area area) {
    // Start
    area.intersect(AreaTool.getAreaFrom(layer.getHorizontalSection()));
    // Rotate area
    double diffRotation = Math.toRadians(
        ((Double) config.get("diffRotation")
            .getCurrentValue()) * layer.getLayerNumber());
    AffineTransform t1 = AffineTransform.getRotateInstance(-diffRotation),
        t2 = AffineTransform.getRotateInstance(diffRotation);
    area.transform(t1);
    // Pave
    Vector2[] p = calculatePatternStartEnd(area);
    Collection<Bit2D> bits = pave(layer.getLayerNumber(), p[0], p[1]).stream()
        .map(bit2D -> bit2D.createTransformedBit(t2))
        .collect(Collectors.toList());
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

  private Collection<Bit2D> pave(int layerNumber, Vector2 patternStart, Vector2 patternEnd) {
    // Setup parameters
    double bitsOffset = (double) config.get("bitsOffset")
        .getCurrentValue();
    double paddle = Math.max(CraftConfig.lengthFull, CraftConfig.bitWidth);
    // Start
    Vector<Bit2D> bits = new Vector<>();
    double xOffSet = Math.sqrt(2.0) / 2.0 * CraftConfig.lengthFull + bitsOffset;
    double yOffSet = Math.sqrt(2.0) / 2.0 * CraftConfig.bitWidth + bitsOffset;
    for (double i = patternStart.x - paddle; i <= patternEnd.x + paddle; i = i + 2 * xOffSet) {
      for (double j = patternStart.y - paddle; j <= patternEnd.y + paddle; j = j + 2 * yOffSet) {
        Vector2 originBit;
        Vector2 orientationBit;
        double layerOffSet = 0; // In this pattern we apply an offset on 1 layer on 2
        if (layerNumber % 2 == 0) {
          layerOffSet = yOffSet;
        }
        originBit = new Vector2(i, j + layerOffSet);
        orientationBit = new Vector2(1, 1);
        bits.add(new NewBit2D(originBit, orientationBit));
      }
    }
    for (double i = patternStart.x + xOffSet - paddle; i <= patternEnd.x + paddle;
        i = i + 2 * xOffSet) {
      for (double j = patternStart.y + yOffSet - paddle; j <= patternEnd.y + paddle;
          j = j + 2 * yOffSet) {
        Vector2 originBit;
        Vector2 orientationBit;
        double layerOffSet = 0; // In this pattern we apply an offset on 1 layer on 2
        if (layerNumber % 2 == 0) {
          layerOffSet = yOffSet;
        }
        originBit = new Vector2(i, j + layerOffSet);
        orientationBit = new Vector2(-1, 1);
        bits.add(new NewBit2D(originBit, orientationBit));
      }
    }
    return bits;
  }

  @Override
  public int optimize(Layer actualState) {
    return -2;
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
    return "A rather usual pattern. No rotation between layer. No auto-optimization implemented.";
  }

  @Override
  public String getHowToUse() {
    return "Choose the gap you desired.";
  }

  @Override
  public void initiateConfig() {
    config.add(new DoubleParam(
        "bitsOffset",
        "Space between bits",
        "The horizontal and vertical gap in mm",
        1.0,
        100.0,
        3.0,
        1.0));
    config.add(new DoubleParam(
        "diffRotation",
        "Differential rotation",
        "Determine the rotation (in deg) of n-th layer",
        -180.0,
        180.0,
        0.0,
        0.01
    ));
  }

  @Override
  public boolean ready(Mesh mesh) {
    return true;
  }
}