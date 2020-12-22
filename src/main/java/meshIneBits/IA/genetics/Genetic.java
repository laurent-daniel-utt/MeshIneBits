package meshIneBits.IA.genetics;

import meshIneBits.Bit2D;
import meshIneBits.IA.DataPreparation;
import meshIneBits.Layer;
import meshIneBits.config.CraftConfig;
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
        this.start();
    }

    /**
     * Starts the Genetic pavement and paves the given layer.
     */
    private void start() {
        Slice sliceToTest = layer.getHorizontalSection();

        Vector<Vector<Vector2>> boundsToCheckAssociated = DataPreparation.getBoundsAndRearrange(sliceToTest); //debugOnly on fait ici que la premiere slice
        Vector<Vector2> bound1 = boundsToCheckAssociated.get(0);//debugOnly on fait ici que le premier contour
        Vector2 startPoint = bound1.get(10);//should never start from point 0 or -1
        Vector2 veryFirstStartPoint = startPoint;
        Vector<Vector2> associatedPoints = getAssociatedPoints(bound1, startPoint);


        Bit2D bestBit;
        //todo @Etienne pave each bound
        int nbIterations = 0;
        while (!hasCompletedTheBound(veryFirstStartPoint, associatedPoints)) {
            nbIterations++;
            if (nbIterations > 20)//number max of iterations before stopping
                break;
            System.out.println(nbIterations);
            currentEvolution = new Evolution((Vector<Vector2>) associatedPoints.clone(), startPoint, (Vector<Vector2>) bound1.clone());
            currentEvolution.run();
            bestBit = currentEvolution.bestSolution.bit;
            solutions.add(bestBit);

            System.out.println("best bit " + bestBit);
            try {
                startPoint = DataPreparation.getNextBitStartPoint(bestBit, (Vector<Vector2>) bound1.clone());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("nextBitStartPoint not found (in Genetics)"); //debugOnly
            }
        }
    }

    /**
     * Returns the (associated) points of the Slice in the range of the bitLength.
     *
     * @param bound1     the bound of the Slice on which the associated points will be searched.
     * @param startPoint the point on which a bit will be placed.
     * @return the associated points.
     */
    private Vector<Vector2> getAssociatedPoints(Vector<Vector2> bound1, Vector2 startPoint) {
        Vector<Vector2> associatedPoints = new Vector<>();
        boolean isAfterStartPoint = false;
        Vector<Vector2> bound = (Vector<Vector2>) bound1.clone();
        for (Vector2 point : bound) {
            if (point.equals(startPoint))
                isAfterStartPoint = true;
            if (isAfterStartPoint && Vector2.dist(startPoint, point) <= CraftConfig.bitLength) {
                associatedPoints.add(point);
            }
        }
        return associatedPoints;
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