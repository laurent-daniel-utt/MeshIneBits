package meshIneBits.artificialIntelligence.genetics;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.deeplearning.DataPreparation;
import meshIneBits.Layer;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Vector2;

import java.awt.geom.Area;
import java.util.Vector;

public class Genetic {
    private final Layer layer;
    private final Area layerAvailableArea;
    private final Vector<Bit2D> solutions = new Vector<>();
    public Evolution currentEvolution;

    /**
     * The tool for genetic algorithms which performs the pavement on a layer.
     *
     * @param layer the layer to pave.
     */
    public Genetic(Layer layer, double genNumber, double popSize, double LENGTH_COEFF, double maxBitNumber) {
        this.layer = layer;
        this.layerAvailableArea = AreaTool.getAreaFrom(layer.getHorizontalSection());
        try {
            this.start(layer, layerAvailableArea, (int) genNumber, (int) popSize, (int) LENGTH_COEFF, (int) maxBitNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the Genetic pavement and paves the given layer.
     */
    private void start(Layer layer, Area layerAvailableArea, int genNumber, int popSize, int LENGTH_COEFF, int maxBitNumber) throws Exception {
        Slice slice = layer.getHorizontalSection();
        Vector<Vector<Vector2>> boundsToCheckAssociated = DataPreparation.getBoundsAndRearrange(slice);

        for (Vector<Vector2> bound : boundsToCheckAssociated) {
            Vector2 startPoint = bound.get(0);
            Vector2 veryFirstStartPoint = startPoint;
            Vector<Vector2> associatedPoints = DataPreparation.getSectionPointsFromBound(bound, startPoint);

            Bit2D bestBit;
            int bitNumber = 0;
            while (!hasCompletedTheBound(veryFirstStartPoint, associatedPoints)) { //Add each bit on the bound
                bitNumber++;
                if (bitNumber > maxBitNumber)//number max of bits to place on a bound before stopping
                    break;
                printInfos(boundsToCheckAssociated, bound, bitNumber);

                //Find a new Solution
                currentEvolution = new Evolution(layerAvailableArea, associatedPoints, startPoint, bound, genNumber, popSize, LENGTH_COEFF);
                currentEvolution.run();
                bestBit = currentEvolution.bestSolution.getBit();
                solutions.add(bestBit);

                //Prepare to find the next Solution
                layerAvailableArea.subtract(bestBit.getArea());
                associatedPoints = DataPreparation.getSectionPointsFromBound(bound, startPoint);
                startPoint = DataPreparation.getNextBitStartPoint(bestBit, bound);
            }
        }
    }

    /**
     * Print infos on the console
     */
    private void printInfos(Vector<Vector<Vector2>> boundsToCheckAssociated, Vector<Vector2> bound, int bitNumber) {
        System.out.printf("%-11s", "Layer n°" + layer.getLayerNumber());
        System.out.printf("%-14s", "   bound n°" + boundsToCheckAssociated.indexOf(bound));
        System.out.printf("%-14s", "   bit n°" + bitNumber);
        System.out.println();
    }


    /**
     * Check if the bound of the Slice has been entirely paved.
     *
     * @param veryFirststartPoint the point of the bound on which the very first bit was placed.
     * @param associatedPoints    the points on which a bit has just been placed.
     * @return <code>true</code> if the bound of the Slice has been entirely paved. <code>false</code> otherwise.
     */
    private boolean hasCompletedTheBound(Vector2 veryFirststartPoint, Vector<Vector2> associatedPoints) {
        //todo @Etienne ca marche pas toujours comme voulu
        // faire que si on est trop proche pour poser un bit, on s'arrete
        if (associatedPoints.firstElement() == veryFirststartPoint)
            return false;
        if (Vector2.dist(veryFirststartPoint, associatedPoints.firstElement()) < CraftConfig.errorAccepted)
            return true;
        return associatedPoints.contains(veryFirststartPoint);
    }

    /**
     * @return the best solutions
     */
    public Vector<Bit2D> getSolutions() {
        return solutions;
    }
}