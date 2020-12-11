package meshIneBits.IA.genetics;


import meshIneBits.IA.AI_Tool;
import meshIneBits.util.Vector2;

import java.util.Vector;

public class Evolution {
    public static final int NB_GEN_MAX = 10;
    public static final int POP_SIZE = 2;//todo changer après le debug
    private static final double PROB_MUTATION = 0.1;
    private static final double RANK_SELECTION = 0.3;
    private static final double RANK_REPRODUCTION = 0.6;
    private static final double RANK_NEW = 1 - RANK_REPRODUCTION - RANK_SELECTION;
    private final Vector<Vector2> pointSection;
    public Solution bestSolution;
    public double[] scores = new double[POP_SIZE * NB_GEN_MAX + 1];
    public Generation currentGeneration;
    private int currentGenerationNumber;

    /**
     * An Evolution search the best solution for a given set of points.
     *
     * @param pointSection the points on which the Bit2D will be placed.
     */
    public Evolution(Vector<Vector2> pointSection) {
        this.pointSection = pointSection;
    }

    /**
     * Run the evolution process to find a solution.
     */
    public void run() {
        //first INIT
        currentGeneration = new Generation(
                POP_SIZE,
                NB_GEN_MAX,
                RANK_SELECTION,
                RANK_REPRODUCTION,
                RANK_NEW,
                PROB_MUTATION,
                pointSection.get(0));

        currentGeneration.initialize();
        System.out.println("init(0) gen evaluation...");
        System.out.println("pop after before 0 " + currentGeneration.solutions.size());
        currentGeneration.evaluateGeneration((Vector<Vector2>) pointSection.clone());//debugOnly je crois     //andre : à priori ouais
        System.out.println("pop after eval 0 " + currentGeneration.solutions.size());
        System.out.println("init(0) gen evaluated");
        currentGenerationNumber = 1;
        while (currentGenerationNumber <= NB_GEN_MAX) {
            System.out.println("gen" + currentGenerationNumber + " generation...");
            Generation newGeneration = new Generation(
                    POP_SIZE,
                    NB_GEN_MAX,
                    RANK_SELECTION,
                    RANK_REPRODUCTION,
                    RANK_NEW,
                    PROB_MUTATION,
                    pointSection.get(0));
            System.out.println("gen" + currentGenerationNumber + " generated");
            System.out.println("pop " + currentGeneration.solutions.size());
            Vector<Solution> mutatedSolutions = newGeneration.mutate((Vector<Solution>) currentGeneration.solutions.clone());
            System.out.println("pop " + currentGeneration.solutions.size());
            Vector<Solution> sortedSolutions = newGeneration.select((Vector<Solution>) currentGeneration.solutions.clone());
            System.out.println("pop " + currentGeneration.solutions.size());

            Vector<Solution> reproducedSolutions = newGeneration.reproduce((Vector<Solution>) currentGeneration.solutions.clone());

            Vector<Solution> newSolutions = new Vector<>();
            newSolutions.addAll(mutatedSolutions);
            newSolutions.addAll(sortedSolutions);
            newSolutions.addAll(reproducedSolutions);
            newGeneration.initialize(newSolutions);
            Vector<Solution> solutionsToCompleteWith = newGeneration.addNewSolutions();
            newGeneration.solutions.addAll(solutionsToCompleteWith);

            System.out.println("gen" + currentGenerationNumber + " initialized");


            System.out.println("gen" + currentGenerationNumber + " evaluation...");
            newGeneration.evaluateGeneration((Vector<Vector2>) pointSection.clone());
            System.out.println("gen" + currentGenerationNumber + " evaluated");

            System.out.println("mean score : " + newGeneration.meanScore);
            System.out.println("max score  : " + newGeneration.maxScore);
            System.out.println("population : " + newGeneration.solutions.size() + " individus");

            scores[currentGenerationNumber] = newGeneration.maxScore;
            AI_Tool.dataPrep.scores[currentGenerationNumber] = newGeneration.maxScore;
            currentGenerationNumber++;
            bestSolution = currentGeneration.bestSolution;
        }


    }
}