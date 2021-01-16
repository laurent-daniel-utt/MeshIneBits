package meshIneBits.artificialIntelligence.genetics;

import meshIneBits.util.Vector2;

import java.awt.geom.Area;
import java.util.Vector;

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
    private final int LENGTH_COEFF;
    private final int NB_GEN_MAX;
    private final int POP_SIZE;
    private final Vector<Vector2> pointSection;
    private final Vector2 startPoint;
    private final Vector<Vector2> bound;
    private final Vector<Generation> generations = new Vector<>();
    private final Area layerAvailableArea;
    public Solution bestSolution;

    /**
     * An evolution searches the best Solution for a given set of points.
     *
     * @param pointSection the points on which the Bit2D will be placed.
     */
    public Evolution(Area layerAvailableArea, Vector<Vector2> pointSection, Vector2 startPoint, Vector<Vector2> bound, int genNumber, int popSize, int LENGTH_COEFF) {
        this.layerAvailableArea = layerAvailableArea;
        this.pointSection = pointSection;
        this.startPoint = startPoint;
        this.bound = bound;
        this.NB_GEN_MAX = genNumber;
        this.POP_SIZE = popSize;
        this.LENGTH_COEFF = LENGTH_COEFF;
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
                LENGTH_COEFF,
                layerAvailableArea,
                startPoint,
                bound
        );
        initGeneration.initialize(pointSection);
        int currentGenerationNumber = 0;
        generations.add(initGeneration);

        while (currentGenerationNumber < NB_GEN_MAX) {
            Generation currentGeneration = new Generation(
                    POP_SIZE,
                    RANK_SELECTION,
                    RANK_REPRODUCTION,
                    PROB_MUTATION,
                    LENGTH_COEFF,
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
            Vector<Solution> solutionsToCompleteWith = currentGeneration.completeWithNewSolutions(pointSection);
            currentGeneration.solutions.addAll(solutionsToCompleteWith);

            //evaluates and deletes bad solutions
            Vector<Solution> badSolutions = new Vector<>();
            for (Solution solution : currentGeneration.solutions) {
                if (solution.isBad())
                    badSolutions.add(solution);
            }
            currentGeneration.solutions.removeAll(badSolutions);
            currentGeneration.evaluateGeneration();
            bestSolution = currentGeneration.bestSolution;

            currentGeneration.sortSolutions(currentGeneration.solutions);
        }
    }
}