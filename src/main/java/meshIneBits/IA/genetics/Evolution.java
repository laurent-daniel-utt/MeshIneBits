package meshIneBits.IA.genetics;


import meshIneBits.util.Vector2;

import java.util.Vector;

public class Evolution {
    /**
     * The number of generations to do.
     */
    public static final int NB_GEN_MAX = 1;
    /**
     * The size of the population of solutions.
     */
    public static final int POP_SIZE = 10;
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
    private static final double RANK_REPRODUCTION = 0.6;
    private final Vector<Vector2> pointSection;
    private final Vector2 startPoint;
    private final Vector<Vector2> bound;
    private final Vector<Generation> generations = new Vector<>();
    public Solution bestSolution;
    public double[] scores = new double[POP_SIZE * NB_GEN_MAX + 1];

    /**
     * An Evolution search the best Solution for a given set of points.
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
        //first INIT
        Generation initGeneration = new Generation(
                POP_SIZE,
                NB_GEN_MAX,
                RANK_SELECTION,
                RANK_REPRODUCTION,
                PROB_MUTATION,
                startPoint,
                bound);
        initGeneration.initialize((Vector<Vector2>) pointSection.clone());
        //System.out.println("gen 0 initialized");
        int currentGenerationNumber = 0;
        generations.add(initGeneration);

        while (currentGenerationNumber < NB_GEN_MAX) {
            //System.out.println("gen " + currentGenerationNumber + " generation...");
            Generation currentGeneration = new Generation(
                    POP_SIZE,
                    NB_GEN_MAX,
                    RANK_SELECTION,
                    RANK_REPRODUCTION,
                    PROB_MUTATION,
                    startPoint,
                    bound);
            currentGenerationNumber++;
            generations.add(currentGeneration);
            System.out.println("pop after init : " + currentGeneration.solutions.size());

            //System.out.println("gen " + currentGenerationNumber + " generated");
            Vector<Solution> mutatedSolutionsFromLastGen = currentGeneration.mutate((Vector<Solution>) generations.get(currentGenerationNumber - 1).solutions.clone());
            Vector<Solution> sortedSolutions = currentGeneration.select((Vector<Solution>) mutatedSolutionsFromLastGen.clone());
            Vector<Solution> reproducedSolutions = currentGeneration.reproduce((Vector<Solution>) mutatedSolutionsFromLastGen.clone());
            System.out.println("pop after generating solutions : " + currentGeneration.solutions.size());

            Vector<Solution> newSolutions = new Vector<>();
            newSolutions.addAll(sortedSolutions);
            newSolutions.addAll(reproducedSolutions);
            currentGeneration.initializeWith(newSolutions);
            //System.out.println("new gen individus : "+currentGeneration.solutions.size());
            Vector<Solution> solutionsToCompleteWith = currentGeneration.completeWithNewSolutions((Vector<Vector2>) pointSection.clone());
            currentGeneration.solutions.addAll(solutionsToCompleteWith);
            System.out.println("pop after adding : " + currentGeneration.solutions.size());

            //System.out.println("gen" + currentGenerationNumber + " initialized");
            //System.out.println("population : " + currentGeneration.solutions.size() + " individus");

            //System.out.println("gen" + currentGenerationNumber + " evaluation...");
            //evaluates and delete bad solutions
            currentGeneration.evaluateGeneration((Vector<Vector2>) pointSection.clone());
            System.out.println("\npop after evaluation : " + currentGeneration.solutions.size());
            Vector<Solution> clonedSolutions = (Vector<Solution>) currentGeneration.solutions.clone();
            for (Solution solution : clonedSolutions) {
                solution.deleteIfBad((Vector<Vector2>) pointSection.clone());
            }
            System.out.println("pop after deleting : " + currentGeneration.solutions.size());
            System.out.println("gen" + currentGenerationNumber + " evaluated");

            System.out.println("mean score : " + currentGeneration.meanScore);
            System.out.println("max score  : " + currentGeneration.maxScore + "  " + currentGeneration.bestSolution.toString());
            System.out.println("population : " + currentGeneration.solutions.size() + " individus");

            solutionsToCompleteWith = currentGeneration.completeWithNewSolutions((Vector<Vector2>) pointSection.clone());
            currentGeneration.solutions.addAll(solutionsToCompleteWith);
            scores[currentGenerationNumber] = currentGeneration.maxScore;
            currentGeneration.sortSolutions(currentGeneration.solutions);
            bestSolution = currentGeneration.bestSolution.clone();
            System.out.println("pop after completing : " + currentGeneration.solutions.size());
        }

        //DEBUGONLY
/*
        Vector<Solution> clonedSolutions = (Vector<Solution>) generations.get(currentGenerationNumber).solutions.clone();
        for (Solution solution : clonedSolutions) {
            if (AI_Tool.dataPrep.getNextBitStartPoint(solution.bit, bound) == null) {
                generations.get(currentGenerationNumber).solutions.remove(solution);
            }
        }
        generations.get(currentGenerationNumber).evaluateGeneration(pointSection);
        bestSolution = generations.get(currentGenerationNumber).bestSolution;//devrait prendre la meilleure non supprimée
        //FIXME @Etienne on perd des indivs
*/
    }
}