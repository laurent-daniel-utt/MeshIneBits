package meshIneBits.IA.genetics;

import meshIneBits.IA.AI_Tool;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.util.*;

public class Generation {
    private static final double MAX_ANGLE = 0;//todo mettre 30 je pense
    private final Vector<Vector2> bound;
    public Vector2 startPoint;
    public double meanScore = -1;
    public double maxScore = -1;
    public Solution bestSolution;
    public Vector<Solution> solutions;
    private int popSize;
    private int nbGensMax;
    private double rankSelection;
    private double rankReproduction;
    private double rankNew;
    private double probMutation;

    /**
     * A Generation is a set of solutions.
     *
     * @param popSize          the size of the population.
     * @param nbGensMax        the maximum number of generations.
     * @param rankSelection    the percentage of selected solutions from the previous generation to make the new one.
     * @param rankReproduction the percentage of reproduced solutions from the previous generation to make the new one.
     * @param probMutation     the probability for a solution to mutate by itself at each generation.
     * @param bound
     */
    public Generation(int popSize, int nbGensMax, double rankSelection, double rankReproduction, double probMutation, Vector2 startPoint, Vector<Vector2> bound) {
        this.popSize = popSize;
        this.solutions = new Vector<>(popSize);
        this.nbGensMax = nbGensMax;
        this.rankSelection = rankSelection;
        this.rankReproduction = rankReproduction;
        this.probMutation = probMutation;
        this.startPoint = startPoint;
        this.bound = bound;
    }

    /**
     * Initialize a Generation. Creates its solutions.
     */
    public void initialize(Vector<Vector2> pointSection) {
        for (int pop = 0; pop < popSize; pop++) {
            this.solutions.add(createNewSolution((Vector<Vector2>) pointSection.clone()));
        }
    }

    /**
     * Initialize a Generation. Appends its solutions.
     */
    public void initializeWith(Vector<Solution> solutionVector) {
        solutions.addAll(solutionVector);
    }

    /**
     * Create a new random solution.
     *
     * @return the new random solution.
     */
    private Solution createNewSolution(Vector<Vector2> pointSection) {
        //todo l'user doit pouvoir choisir son +-30° et l'enregistrer dans la config de l'app
        double position = Math.random() * CraftConfig.bitWidth;
        position = 0;
        double angleSection = AI_Tool.dataPrep.
                getSectionOrientation((Vector<Vector2>) pointSection.clone()) * (180 / Math.PI);
        AI_Tool.dataPrep.pointsADessiner.clear();//debugOnly
        // recenter points so the first point of the section is on the Oy axis.
        // This way we'll be able to use arePointsMostlyToTheRight method
        Vector<Vector2> oYRecenteredPoints = new Vector();
        Vector<Vector2> clonedPointSection = (Vector<Vector2>) pointSection.clone();
        for (int i = 0; i < clonedPointSection.size(); i++) {
            oYRecenteredPoints.add(new Vector2(
                    clonedPointSection.get(i).x - clonedPointSection.get(0).x,
                    clonedPointSection.get(i).y));
        }

        if (!AI_Tool.dataPrep.arePointsMostlyToTheRight(oYRecenteredPoints)) {
            angleSection = -Math.signum(angleSection) * 180 + angleSection;
        }
        int dir = Math.random() > 0.5 ? 1 : -1;
        double rotation = angleSection + Math.random() * MAX_ANGLE * dir; //plus ou moins 30°
        Vector2 rotationVector = Vector2.getEquivalentVector(rotation);
        AI_Tool.dataPrep.currentSegToDraw2 = new Segment2D(startPoint, startPoint.add(Vector2.getEquivalentVector(angleSection).mul(100))); //debugOnly
        return new Solution(position, rotationVector, startPoint, this, bound);
    }




    /**
     * Create a new solution from parameters.
     *
     * @param pos   the position of the solution
     * @param angle the angle of the solution
     * @return the new solution.
     */
    private Solution createNewSolution(double pos, Vector2 angle) {
        return new Solution(pos, angle, startPoint, this, bound);
    }

    /**
     * Evaluates the current generation and stores scores.
     */
    public void evaluateGeneration(Vector<Vector2> sectionPoints) {
        this.meanScore = 0;
        this.maxScore = 0;

        Vector<Solution> clonedSolutions = (Vector<Solution>) solutions.clone();

        for (Solution solution : clonedSolutions) {
            //System.out.println("evaluating solution : " + solution.toString() + "...");
            double score = solution.evaluate((Vector<Vector2>) sectionPoints.clone());
            //System.out.println("score de la solution " + score + " --> " + solution.toString());
            if (score > this.maxScore) {
                maxScore = score;
                bestSolution = solution;
            }

            bestSolution = solution;//DEBUGONLY //FIXME A ENLEVER
            this.meanScore += score;
        }

        this.meanScore /= this.popSize;
    }

    /**
     * Select the best solutions.
     *
     * @return the n best solutions.
     */
    public Vector<Solution> select(Vector<Solution> solutionsToSelect) {
        List<Solution> solutionList = new ArrayList<>(solutionsToSelect);

        Comparator<Solution> comparator = new SolutionComparator();
        solutionList.sort(comparator);

        int nbOfSelecteds = (int) (this.popSize * this.rankSelection);
        return new Vector<>(solutionList.subList(0, nbOfSelecteds));
    }

    /**
     * Reproduce solutions between.
     *
     * @return the n new children solutions.
     */
    public Vector<Solution> reproduce(Vector<Solution> solutionsToReproduce) {
        Vector<Solution> reproducedSolutions = new Vector<>();
        int numberOfChilds = (int) (this.popSize * this.rankReproduction);

        for (int i = 0; i < numberOfChilds; i++) {
            Random rand = new Random();
            Solution parent1 = solutionsToReproduce.get(rand.nextInt(solutionsToReproduce.size()));
            Solution parent2 = solutionsToReproduce.get(rand.nextInt(solutionsToReproduce.size()));

            double childPos = (parent1.bitPos + parent2.bitPos) / 2;
            Vector2 childAngle = (parent1.bitAngle.add(parent2.bitAngle)).mul(0.5);
            Solution childSolution = createNewSolution(childPos, childAngle);
            reproducedSolutions.add(childSolution);
        }
        return reproducedSolutions;
    }

    /**
     * Mutate a few solutions to assure diversity.
     *
     * @return the solutions given with a few mutated solutions.
     */
    public Vector<Solution> mutate(Vector<Solution> solutionsToMutate) {
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
     * Completes the current generation with new solutions.
     *
     * @return the completely new solutions.
     */
    public Vector<Solution> completeWithNewSolutions(Vector<Vector2> pointSection) {
        Vector<Solution> newSolutions = new Vector<>();
        int nbOfNewSolutions = this.popSize - this.solutions.size();
        for (int i = 0; i < nbOfNewSolutions; i++) {
            newSolutions.add(createNewSolution(pointSection));
        }
        return newSolutions;
    }


}

