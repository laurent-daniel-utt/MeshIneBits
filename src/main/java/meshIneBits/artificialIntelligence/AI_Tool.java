package meshIneBits.artificialIntelligence;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.deeplearning.NNExploitation;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Vector2;

import java.util.Collection;
import java.util.Vector;

/**
 * An AI_Tool lets the user pave the whole mesh, with artificial intelligence.
 * AI_Tool is based on a neural network that learns from how does a human place bits on the bounds of a Slice.
 */
public abstract class AI_Tool {
    private static MeshController meshController;
    private static final Vector<Bit2D> bits = new Vector<>(); //the bits placed by the AI
    public static final DoubleParam paramSafeguardSpace = new DoubleParam(
            "safeguardSpace",
            "Space around bit",
            "In order to keep bits not overlapping or grazing each other",
            1.0, 10.0, 3.0, 0.01);

    /**
     * Pave the whole mesh with AI.
     */
    public static Collection<Bit2D> startNNPavement(Slice slice) throws Exception {
        bits.clear();
        meshController.AI_NeedPaint = true;

        Vector<Vector<Vector2>> bounds = DataPreparation.getBoundsAndRearrange(slice);

        for (Vector<Vector2> bound : bounds) {
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 startPoint = bound.get(0);
            Vector<Vector2> associatedPoints = DataPreparation.getSectionPointsFromBound(bound, startPoint);

            int nbIterations = 0;
            while (hasNotCompletedTheBound(veryFirstStartPoint, startPoint, associatedPoints)) { //Add each bit on the bound

                nbIterations++;
                if (nbIterations > 50)//number max of bits to place before stopping
                    break;

                Vector<Vector2> sectionPoints = DataPreparation.getSectionPointsFromBound(bound, startPoint);
                double angleLocalSystem = DataPreparation.getLocalCoordinateSystemAngle(sectionPoints);

                NNExploitation nnExploitation = new NNExploitation();
                Vector<Vector2> sectionPointsReg = DataPreparation.getInputPointsForDL(sectionPoints);
                Bit2D bit = nnExploitation.getBit(sectionPointsReg, startPoint, angleLocalSystem);
                bits.add(bit);
                System.out.println("size:" + bits.size());

                startPoint = DataPreparation.getNextBitStartPoint(bit, bound);

            }
        }
        return bits;
    }

    /**
     * Check if the bound of the Slice has been entirely paved.
     *
     * @param veryFirststartPoint the point of the bound on which the very first bit was placed.
     * @param associatedPoints    the points on which a bit has just been placed.
     * @return <code>true</code> if the bound of the Slice has been entirely paved. <code>false</code> otherwise.
     */
    public static boolean hasNotCompletedTheBound(Vector2 veryFirststartPoint, Vector2 startPoint, Vector<Vector2> associatedPoints) {
        if (associatedPoints.firstElement() == veryFirststartPoint) //to avoid returning true on the first placement
            return true;
        if (Vector2.dist(veryFirststartPoint, startPoint) < AI_Tool.paramSafeguardSpace.getCurrentValue()*10) //standard safe distance between two bits
            return false;
        return !associatedPoints.contains(veryFirststartPoint);
    }

    /**
     * @return the meshController
     */
    public static MeshController getMeshController() {
        return meshController;
    }

    public static void setMeshController(MeshController meshController) {
        AI_Tool.meshController = meshController;
    }

}