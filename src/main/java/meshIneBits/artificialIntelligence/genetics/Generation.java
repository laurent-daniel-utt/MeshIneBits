package meshIneBits.artificialIntelligence.genetics;

import meshIneBits.artificialIntelligence.GeneralTools;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Area;
import java.util.*;

/**
 * A Generation is a set of solutions.
 * Provides tools to reproduce solutions between them, select the better ones,
 * and mutate some (has to be used with genetics algorithms).
 *
 * @see Solution
 */
public class Generation {
    /**
     * The max angle between the section and the bit when creating a new Solution.
     */
    private static final double MAX_ANGLE = 89.0;
    public final @NotNull Vector<Solution> solutions;
    private final Vector<Vector2> bound;
    private final Vector2 startPoint;
    private final int popSize;
    private final double rankSelection;
    private final double rankReproduction;
    private final double probMutation;
    private final int ratio;
    private final Area layerAvailableArea;
    public Solution bestSolution;

    /**
     * A Generation is a set of solutions.
     * Implements methods to sort, select, reproduce, mutate and evaluate the solutions.
     *
     * @param popSize          the size of the population.
     * @param rankSelection    the percentage of selected solutions from the previous generation to make the new one.
     * @param rankReproduction the percentage of reproduced solutions from the previous generation to make the new one.
     * @param probMutation     the probability for a solution to mutate by itself at each generation.
     * @param startPoint       the point on which the bit has to be placed
     * @param bound            the bound on which the bit has to be placed
     */
    public Generation(int popSize, double rankSelection, double rankReproduction, double probMutation, int ratio, Area layerAvailableArea, Vector2 startPoint, Vector<Vector2> bound) {
        this.popSize = popSize;
        this.solutions = new Vector<>(popSize);
        this.rankSelection = rankSelection;
        this.rankReproduction = rankReproduction;
        this.probMutation = probMutation;
        this.startPoint = startPoint;
        this.bound = bound;
        this.ratio = ratio;
        this.layerAvailableArea = layerAvailableArea;
    }

    /**
     * Initialize a Generation. Creates its solutions.
     */
    public void initialize(@NotNull Vector<Vector2> pointSection) {
        for (int pop = 0; pop < popSize; pop++) {
            this.solutions.add(createNewSolution(pointSection));
        }
    }

    /**
     * Initialize a Generation with given solutions.
     */
    public void initializeWith(Vector<Solution> solutionVector) {
        solutions.addAll(solutionVector);
    }

    /**
     * Create a new random solution. Takes in account the <code>MAX_ANGLE</code> to generate the random rotation.
     *
     * @return the new random solution.
     */
    private @NotNull Solution createNewSolution(@NotNull Vector<Vector2> pointSection) {
        double position = Math.random() * CraftConfig.bitWidth;

        double angleSection = GeneralTools.
                getSectionOrientation(pointSection);

        if (GeneralTools.arePointsMostlyOrientedToTheLeft(pointSection, pointSection.firstElement())) {
            angleSection = -Math.signum(angleSection) * 180 + angleSection;
        }
        int dir = Math.random() > 0.5 ? 1 : -1;
        double rotation = angleSection + Math.random() * MAX_ANGLE * dir;
        Vector2 rotationVector = Vector2.getEquivalentVector(rotation);
        return new Solution(position, rotationVector, startPoint, this, bound, ratio, layerAvailableArea);
    }

    /**
     * Create a new solution from parameters.
     *
     * @param pos   the position of the solution
     * @param angle the angle of the solution
     * @return the new solution.
     */
    private @NotNull Solution createNewSolution(double pos, Vector2 angle) {
        return new Solution(pos, angle, startPoint, this, bound, ratio, layerAvailableArea);
    }

    /**
     * Evaluates the current generation and stores scores.
     */
    @SuppressWarnings("unchecked")
    public void evaluateGeneration() {
        Vector<Solution> clonedSolutions = (Vector<Solution>) solutions.clone();
        for (Solution solution : clonedSolutions) {
            solution.evaluate();
        }
        List<Solution> solListSorted = sortSolutions(clonedSolutions);
        try {
            bestSolution = solListSorted.get(0);
        } catch (Exception e) {
            System.out.println("Error : not enough solutions in the population. Please increase POPULATION parameter.");
        }
    }

    /**
     * Select the best solutions.
     *
     * @return the n best solutions according to the <code>rankSelection</code>.
     */
    public @NotNull Vector<Solution> select(@NotNull Vector<Solution> solutionsToSelect) {
        List<Solution> solutionList = sortSolutions(solutionsToSelect);
        int nbOfSelecteds = (int) (solutionsToSelect.size() * rankSelection);
        return new Vector<>(solutionList.subList(0, nbOfSelecteds));
    }

    /**
     * Reproduce solutions between.
     *
     * @return the n new children solutions according to the <code>rankReproduction</code>.
     */
    public @NotNull Vector<Solution> reproduce(@NotNull Vector<Solution> solutionsToReproduce) {
        Vector<Solution> reproducedSolutions = new Vector<>();
        int numberOfChilds = (int) (this.popSize * this.rankReproduction);

        for (int i = 0; i < numberOfChilds; i++) {
            Random rand = new Random();
            Solution parent1 = solutionsToReproduce.get(rand.nextInt(solutionsToReproduce.size()));
            Solution parent2 = solutionsToReproduce.get(rand.nextInt(solutionsToReproduce.size()));

            double childPos = (parent1.getBitPos() + parent2.getBitPos()) / 2;
            Vector2 childAngle = (parent1.getBitAngle().add(parent2.getBitAngle())).mul(0.5).normal();
            Solution childSolution = createNewSolution(childPos, childAngle);
            reproducedSolutions.add(childSolution);
        }
        return reproducedSolutions;
    }

    /**
     * Mutate a few solutions to assure diversity.
     * The probability for a Solution to mutate is given by <code>probMutation</code>
     *
     * @return the solutions given with a few mutated solutions.
     */
    public @NotNull Vector<Solution> mutate(@NotNull Vector<Solution> solutionsToMutate) {
        //Vector<Solution> mutatedSolutions = new Vector<>();
        for (Solution solution : solutionsToMutate) {
            if (Math.random() <= this.probMutation) {
                solution.mutate();
                // mutatedSolutions.add(solution);
            }
        }
        return solutionsToMutate;
    }

    /**
     * Completes the current generation with new solutions to get exactly <code>popSize</code> solutions.
     *
     * @return the completely new solutions.
     */
    public @NotNull Vector<Solution> completeWithNewSolutions(@NotNull Vector<Vector2> pointSection) {
        Vector<Solution> newSolutions = new Vector<>();
        int nbOfNewSolutions = this.popSize - this.solutions.size();
        for (int i = 0; i < nbOfNewSolutions; i++) {
            newSolutions.add(createNewSolution(pointSection));
        }
        return newSolutions;
    }

    /**
     * Sorts its solutions in the descending order so that the first solution returned is the better.
     *
     * @param solutions the solutions to sort.
     * @return the sorted solutions.
     */
    public @NotNull List<Solution> sortSolutions(@NotNull Vector<Solution> solutions) {
        List<Solution> solutionList = new ArrayList<>(solutions);
        Comparator<Solution> comparator = new SolutionComparator();
        solutionList.sort(comparator);
        return solutionList;
    }
}
