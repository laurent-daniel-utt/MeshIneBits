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
import java.util.Vector;
import meshIneBits.Bit2D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.Pavement;
import meshIneBits.artificialIntelligence.AI_Tool;
import meshIneBits.artificialIntelligence.GeneralTools;
import meshIneBits.artificialIntelligence.genetics.Evolution;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Vector2;

public class GeneticPavement extends PatternTemplate {

  private Layer layer;
  private Vector<Bit2D> solutions = new Vector<>();
  public Evolution currentEvolution;

  @Override
  protected void initiateConfig() {
    config.add(new DoubleParam(
        "genNumber",
        "Number of generations",
        "The number of generations",
        1.0,
        50.0,
        10.0,
        1.0));

    config.add(new DoubleParam(
        "popSize",
        "Size of the population",
        "The size of population to generate",
        100.0,
        Double.POSITIVE_INFINITY,
        150.0,
        50.0));

    config.add(new DoubleParam(
        "ratio",
        "Length/Area ratio %",
        "Balance between area and length to calculate solutions' score",
        2.0,
        100.0,
        80.0,
        2.0));
    config.add(new DoubleParam(
        "earlyStopping",
        "Maximum number of bits before stopping",
        "Set a max number of bits to avoid infinite loop",
        0.0,
        Double.POSITIVE_INFINITY,
        50.0,
        5.0));
    config.add(AI_Tool.paramSafeguardSpace);
  }

  @Override
  public Pavement pave(Layer layer) {
    this.layer = layer;
    this.solutions = new Vector<>();
    try {
      this.start(
          AreaTool.getAreaFrom(layer.getHorizontalSection()),
          (double) config.get("genNumber")
              .getCurrentValue(),
          (double) config.get("popSize")
              .getCurrentValue(),
          (double) config.get("ratio")
              .getCurrentValue(),
          (double) config.get("earlyStopping")
              .getCurrentValue());
      updateBitAreasWithSpaceAround();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new Pavement(solutions);
  }

  /**
   * Starts the Genetic pavement and paves the given layer.
   */
  private void start(Area layerAvailableArea, double genNumber, double popSize, double ratio,
      double maxBitNumber) throws Exception {
    Slice slice = layer.getHorizontalSection();
    Vector<Vector<Vector2>> boundsToCheckAssociated = new GeneralTools().getBoundsAndRearrange(
        slice);

    for (Vector<Vector2> bound : boundsToCheckAssociated) {
      Vector2 startPoint = bound.get(0);
      Vector2 veryFirstStartPoint = startPoint;
      Vector<Vector2> associatedPoints = GeneralTools.getSectionPointsFromBound(bound, startPoint);

      Bit2D bestBit;
      int bitNumber = 0;
      while (new AI_Tool().hasNotCompletedTheBound(veryFirstStartPoint, startPoint,
          associatedPoints)) { //Add each bit on the bound
        bitNumber++;
        if (bitNumber > maxBitNumber)//number max of bits to place on a bound before stopping
        {
          break;
        }
        printInfos(boundsToCheckAssociated, bound, bitNumber);

        //Find a new Solution
        currentEvolution = new Evolution(layerAvailableArea, associatedPoints, startPoint, bound,
            (int) genNumber, (int) popSize, (int) ratio);
        currentEvolution.run();
        bestBit = currentEvolution.bestSolution.getBit();
        solutions.add(bestBit);

        //Prepare to find the next Solution
        layerAvailableArea.subtract(bestBit.getArea());
        associatedPoints = GeneralTools.getSectionPointsFromBound(bound, startPoint);
        startPoint = new GeneralTools().getNextBitStartPoint(bestBit, bound);
      }
    }
  }

  /**
   * Print infos on the console
   */
  private void printInfos(Vector<Vector<Vector2>> boundsToCheckAssociated, Vector<Vector2> bound,
      int bitNumber) {
    System.out.printf("%-11s", "Layer n " + layer.getLayerNumber());
    System.out.printf("%-14s", "   bound n " + boundsToCheckAssociated.indexOf(bound));
    System.out.printf("%-14s", "   bit n " + bitNumber);
    System.out.println();
  }

  @Override
  public Pavement pave(Layer layer, Area area) {
    System.out.println("Pave layer & area with genetics... Not implemented yet.");
    return null;
  }

  @Override
  public int optimize(Layer actualState) {
    // TODO: 2021-01-17 implement optimization for last bit placement as in classic brick pattern.
    return -2;
  }

  @Override
  public String getCommonName() {
    return "Genetic pavement";
  }

  @Override
  public String getIconName() {
    return "pattern-genetic.png";
  }

  @Override
  public String getDescription() {
    return "Paves the bounds of the slices with genetic algorithms.";
  }

  @Override
  public String getHowToUse() {
    return "Choose your length covered/area ratio and your params. Choose the gap you desire.";
  }

  @Override
  public boolean ready(Mesh mesh) {
    return true;
  }

  private void updateBitAreasWithSpaceAround() {
    Area availableArea = new Area();
    for (Bit2D bit : solutions) {
      availableArea.add(bit.getArea());
    }
    for (Bit2D bit : solutions) {
      if (bit.getArea() == null) {
        continue;
      }
      Area bitArea = bit.getArea();
      bitArea.intersect(availableArea);
      if (!bitArea.isEmpty()) {
        bit.updateBoundaries(bitArea);
        availableArea.subtract(AreaTool.expand(
            bitArea, // in real
            (double) config.get("safeguardSpace")
                .getCurrentValue()));
      }
    }

  }

}
