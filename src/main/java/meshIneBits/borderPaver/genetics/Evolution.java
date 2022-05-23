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

package meshIneBits.borderPaver.genetics;

import meshIneBits.borderPaver.util.Section;
import meshIneBits.util.Vector2;

import java.awt.geom.Area;
import java.util.Vector;

/**
 * An Evolution is the process of generating generations and applying Darwin's principles to them.
 * The Evolution will find the best solution for the given bit placement with genetic algorithms.
 *
 * @see Generation
 * @see Solution
 */
public class Evolution {
    /**
     * The probability for a Solution to mutate.
     */
    private static final double PROB_MUTATION = 0.1;
    /**
     * The percentage of selected Solutions to give to the new Generation.
     */
    private static final double RANK_SELECTION = 0.2;
    /**
     * The percentage of reproduced Solutions to give to the new Generation.
     */
    private static final double RANK_REPRODUCTION = 0.5;
    /**
     * The ratio between the area and the length, used when calculating the score.
     */
    private final int RATIO;
    /**
     * The number of generations to do.
     */
    private final int NB_OF_GENERATIONS;
    /**
     * The size of the population to keep alive.
     */
    private final int POP_SIZE;
    private final Vector<Vector2> pointSection;
    private final Vector2 startPoint;
    private final Vector<Vector2> bound;
    private final Vector<Generation> generations = new Vector<>();
    private final Area layerAvailableArea;
    private final Section section;
    public Solution bestSolution;

    /**
     * An evolution searches the best Solution for a given set of points.
     *
     * @param layerAvailableArea the available Area of the layer.
     * @param section            the section to place the bit on.
     * @param bound              the bound where the bit has to be placed.
     * @param nbOfGenerations    the number of generations to generate.
     * @param popSize            the size of the population to keep alive during the process.
     * @param RATIO              the ratio between the area and the length, used when calculating the score.
     */
    public Evolution(Area layerAvailableArea, Section section, Vector<Vector2> bound, int nbOfGenerations, int popSize, int RATIO) {
        this.layerAvailableArea = layerAvailableArea;
        this.pointSection = section.getPoints();
        this.startPoint = section.getStartPoint();
        this.bound = bound;
        this.NB_OF_GENERATIONS = nbOfGenerations;
        this.POP_SIZE = popSize;
        this.RATIO = RATIO;
        this.section = section;
    }

    /**
     * Runs the evolution process to find a solution.
     */
    public void run() {
        //INIT
        Generation initGeneration = new Generation(
                POP_SIZE,
                RANK_SELECTION,
                RANK_REPRODUCTION,
                PROB_MUTATION,
                RATIO,
                layerAvailableArea,
                startPoint,
                bound
        );
        initGeneration.initialize(section);
        int currentGenerationNumber = 0;
        generations.add(initGeneration);

        while (currentGenerationNumber < NB_OF_GENERATIONS) {
            Generation currentGeneration = new Generation(
                    POP_SIZE,
                    RANK_SELECTION,
                    RANK_REPRODUCTION,
                    PROB_MUTATION,
                    RATIO,
                    layerAvailableArea,
                    startPoint,
                    bound
            );
            currentGenerationNumber++;
            generations.add(currentGeneration);

            Vector<Solution> mutatedSolutionsFromLastGen = currentGeneration.mutate(generations.get(currentGenerationNumber - 1).solutions);
            Vector<Solution> sortedSolutions = currentGeneration.select(mutatedSolutionsFromLastGen);
            Vector<Solution> reproducedSolutions = currentGeneration.reproduce(mutatedSolutionsFromLastGen);

            Vector<Solution> newSolutions = new Vector<>();
            newSolutions.addAll(sortedSolutions);
            newSolutions.addAll(reproducedSolutions);
            currentGeneration.initializeWith(newSolutions);
            Vector<Solution> solutionsToCompleteWith = currentGeneration.completeWithNewSolutions(section);
            currentGeneration.solutions.addAll(solutionsToCompleteWith);

            //evaluates and deletes bad solutions
            Vector<Solution> badSolutions = new Vector<>();
            for (Solution solution : currentGeneration.solutions) {
                if (solution.isBad())
                    badSolutions.add(solution);
            }
            System.out.println("bad sol in gen " + currentGenerationNumber + " : " + badSolutions.size() + "/" + currentGeneration.solutions.size());//todo debugOnly
            currentGeneration.solutions.removeAll(badSolutions);
            currentGeneration.evaluateGeneration();
            bestSolution = currentGeneration.bestSolution;

            currentGeneration.sortSolutions(currentGeneration.solutions);
        }
    }
}