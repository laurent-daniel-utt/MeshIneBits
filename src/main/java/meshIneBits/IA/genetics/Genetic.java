package meshIneBits.IA.genetics;

import meshIneBits.Bit2D;
import meshIneBits.IA.DataPreparation;
import meshIneBits.Layer;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Vector2;

import java.util.Vector;

public class Genetic {
    private final Layer layer;
    public Evolution currentEvolution;
    private final Vector<Bit2D> solutions = new Vector<>();

    /**
     * The tool for genetic algorithms which performs the pavement on a layer.
     *
     * @param layer the layer to pave.
     */
    public Genetic(Layer layer) {
        System.out.println("On Pave le Layer : " + layer.getLayerNumber());
        this.layer = layer;
        try {
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the Genetic pavement and paves the given layer.
     */
    private void start() throws Exception {
        Slice sliceToTest = layer.getHorizontalSection();

        Vector<Vector<Vector2>> boundsToCheckAssociated = DataPreparation.getBoundsAndRearrange(sliceToTest); //debugOnly on fait ici que la premiere slice
        Vector<Vector2> bound1 = boundsToCheckAssociated.get(0);//debugOnly on fait ici que le premier contour
        Vector2 startPoint = bound1.get(10);//should never start from point 0 or -1
        Vector2 veryFirstStartPoint = startPoint;
        Vector<Vector2> associatedPoints = DataPreparation.getSectionPointsFromBound(bound1, startPoint);//getAssociatedPoints(bound1, startPoint);

        Bit2D bestBit;
        //todo @Etienne pave each bound
        int nbIterations = 0;
        while (!hasCompletedTheBound(veryFirstStartPoint, associatedPoints)) { //todo remetre

            nbIterations++;
            if (nbIterations > 50)//number max of iterations before stopping //todo remettre
                break; //todo enlever je pense ou trouver un nombre correct
            System.out.println(nbIterations);
            currentEvolution = new Evolution((Vector<Vector2>) associatedPoints.clone(), startPoint, (Vector<Vector2>) bound1.clone());
            currentEvolution.run();
            bestBit = currentEvolution.bestSolution.bit;
            solutions.add(bestBit);

            associatedPoints = DataPreparation.getSectionPointsFromBound(bound1, startPoint);
            startPoint = DataPreparation.getNextBitStartPoint(bestBit, bound1);
        }
    }


    /**
     * Check if the bound of the Slice has been entirely paved.
     *
     * @param startPoint       the point of the bound on which the very first bit was placed.
     * @param associatedPoints the points on which a bit has just been placed.
     * @return <code>true</code> if the bound of the Slice has been entirely paved. <code>false</code> otherwise.
     */
    private boolean hasCompletedTheBound(Vector2 startPoint, Vector<Vector2> associatedPoints) {
        //todo @Etienne debug hasCompletedTheBound
        if (associatedPoints.firstElement() == startPoint)
            return false;
        return associatedPoints.contains(startPoint);
    }

    /**
     * @return the best solutions
     */
    public Vector<Bit2D> getSolutions() {
        return solutions;
    }
}