package meshIneBits.artificialIntelligence;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.deepLearning.NNExploitation;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Vector;

/**
 * An AI_Tool lets the user pave the whole mesh, with artificial intelligence.
 * AI_Tool is based on a neural network that learns from how does a human place bits on the bounds of a Slice.
 */
public class AI_Tool {
    /**
     * The name and location of the trained model saved.
     */
    public static final String MODEL_PATH = "src/main/java/meshIneBits/artificialIntelligence/deepLearning/resources/trained_model.zip";
    /**
     * The name and path of the normalizer saved.
     */
    public static final String NORMALIZER_PATH = "src/main/java/meshIneBits/artificialIntelligence/deepLearning/resources/normalizer_saved.bin";
    /**
     * The name and location of the csv file which contains the raw data for the DataSet.
     */
    public final static String DATA_LOG_FILE_PATH = "src/main/java/meshIneBits/artificialIntelligence/deepLearning/resources/storedBits.csv";
    /**
     * The name and location of the csv file which contains the dataSet.
     */
    public static final String DATASET_FILE_PATH = "src/main/java/meshIneBits/artificialIntelligence/deepLearning/resources/dataSet.csv";
    /**
     * Correct the position of the bits placed by the Neural Network.
     * May be useless when the NN will be correctly trained.
     */
    public static final DoubleParam paramPosCorrection = new DoubleParam(
            "posCorrection",
            "Position correction",
            "",
            -10.0, 10.0, 5.0, 0.1);
    public static final DoubleParam paramSafeguardSpace = new DoubleParam(
            "safeguardSpace",
            "Space around bit",
            "In order to keep bits not overlapping or grazing each other",
            1.0, 10.0, 3.0, 0.01);
    public static final DoubleParam paramEarlyStopping = new DoubleParam(
            "earlyStopping",
            "Maximum number of bits before stopping",
            "Set a max number of bits to avoid infinite loop",
            0.0,
            Double.POSITIVE_INFINITY,
            50.0,
            10.0);
    private static MeshController meshController;
    public static MeshController getMeshController() {
        return meshController;
    }

    public static void setMeshController(MeshController meshController) {
        AI_Tool.meshController = meshController;
    }

    /**
     * Pave the whole mesh with AI.
     */
    public @NotNull Collection<Bit2D> startNNPavement(@NotNull Slice slice) throws Exception {
        Vector<Bit2D> bits = new Vector<>();
        System.out.println("PAVING SLICE " + slice.getAltitude());

        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);
        NNExploitation nnExploitation = new NNExploitation();

        for (Vector<Vector2> bound : bounds) {
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 startPoint = bound.get(0);
            Vector<Vector2> associatedPoints = new GeneralTools().getSectionPointsFromBound(bound, startPoint);

            int nbIterations = 0;
            while (hasNotCompletedTheBound(veryFirstStartPoint, startPoint, associatedPoints)) { //Add each bit on the bound

                nbIterations++;
                if (nbIterations > AI_Tool.paramEarlyStopping.getCurrentValue())//number max of bits to place before stopping
                    break;

                Vector<Vector2> sectionPoints = new GeneralTools().getSectionPointsFromBound(bound, startPoint);
                double angleLocalSystem = new GeneralTools().getLocalCoordinateSystemAngle(sectionPoints);

                Vector<Vector2> sectionPointsReg = new GeneralTools().getInputPointsForDL(sectionPoints);
                Bit2D bit = nnExploitation.getBit(sectionPointsReg, startPoint, angleLocalSystem);
                bits.add(bit);

                startPoint = new GeneralTools().getNextBitStartPoint(bit, bound);
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
    public boolean hasNotCompletedTheBound(Vector2 veryFirststartPoint, Vector2 startPoint, @NotNull Vector<Vector2> associatedPoints) {
        if (associatedPoints.firstElement() == veryFirststartPoint) //to avoid returning false on the first placement
            return true;
        if (Vector2.dist(veryFirststartPoint, startPoint) < AI_Tool.paramSafeguardSpace.getCurrentValue() * 10) //standard safe distance between two bits
            return false;
        return !associatedPoints.contains(veryFirststartPoint);
    }

}