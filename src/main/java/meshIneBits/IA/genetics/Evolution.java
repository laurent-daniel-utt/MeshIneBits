package meshIneBits.IA.genetics;

import meshIneBits.util.Vector2;

import java.util.Vector;

public class Evolution {
    /**
     * The number of generations to do.
     */
    public static final int NB_GEN_MAX = 10;
    /**
     * The size of the population of solutions. Has to be greater than 100.
     */
    public static final int POP_SIZE = 150;//todo augmenter et tester
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
    private static final double RANK_REPRODUCTION = 0.4;
    private final Vector<Vector2> pointSection;
    private final Vector2 startPoint;
    private final Vector<Vector2> bound;
    private final Vector<Generation> generations = new Vector<>();
    public Solution bestSolution;
    public double[] scores = new double[POP_SIZE * NB_GEN_MAX + 1]; //debugOnly

    /**
     * An evolution searches the best Solution for a given set of points.
     *
     * @param pointSection the points on which the Bit2D will be placed.
     */
    public Evolution(Vector<Vector2> pointSection, Vector2 startPoint, Vector<Vector2> bound) {
        this.pointSection = (Vector<Vector2>) pointSection.clone();
        this.startPoint = startPoint;
        this.bound = (Vector<Vector2>) bound.clone();
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
                startPoint,
                bound);
        initGeneration.initialize((Vector<Vector2>) pointSection.clone());
        int currentGenerationNumber = 0;
        generations.add(initGeneration);

        while (currentGenerationNumber < NB_GEN_MAX) {
            Generation currentGeneration = new Generation(
                    POP_SIZE,
                    RANK_SELECTION,
                    RANK_REPRODUCTION,
                    PROB_MUTATION,
                    startPoint,
                    bound);
            currentGenerationNumber++;
            generations.add(currentGeneration);

            Vector<Solution> mutatedSolutionsFromLastGen = currentGeneration.mutate((Vector<Solution>) generations.get(currentGenerationNumber - 1).solutions.clone());
            Vector<Solution> sortedSolutions = currentGeneration.select((Vector<Solution>) mutatedSolutionsFromLastGen.clone());
            Vector<Solution> reproducedSolutions = currentGeneration.reproduce((Vector<Solution>) mutatedSolutionsFromLastGen.clone());

            Vector<Solution> newSolutions = new Vector<>();
            newSolutions.addAll(sortedSolutions);
            newSolutions.addAll(reproducedSolutions);
            currentGeneration.initializeWith(newSolutions);
            Vector<Solution> solutionsToCompleteWith = currentGeneration.completeWithNewSolutions((Vector<Vector2>) pointSection.clone());
            currentGeneration.solutions.addAll(solutionsToCompleteWith);

            //evaluates and deletes bad solutions
            Vector<Solution> badSolutions = new Vector<>();
            for (Solution solution : currentGeneration.solutions) {
                if (solution.isBad())
                    badSolutions.add(solution);
            }
            currentGeneration.solutions.removeAll(badSolutions);
            currentGeneration.evaluateGeneration((Vector<Vector2>) pointSection.clone());
            bestSolution = currentGeneration.bestSolution;

            scores[currentGenerationNumber] = currentGeneration.maxScore;
            currentGeneration.sortSolutions(currentGeneration.solutions);
        }
    }
}