package meshIneBits.IA;

import meshIneBits.Bit2D;
import meshIneBits.IA.IA_util.Tools;
import meshIneBits.IA.genetics.Genetic;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

/**
 * An AI_Tool lets the user pave the whole mesh, with artificial intelligence.
 * AI_Tool is based on a neural network that learns from how does a human place bits on the bounds of a Slice.
 */
public class AI_Tool {
    public static DataPreparation dataPrep = new DataPreparation();
    public static Acquisition acquisition = new Acquisition();
    public static Training training = new Training();
    public static Exploitation exploitation = new Exploitation();
    public static Genetic genetic;
    private static Tools tools = new Tools();
    private static MeshController meshController;
    private static Map<Slice, Vector<Segment2D>> sliceMap = new LinkedHashMap();

    private static Vector<Bit2D> bits = new Vector<>(); //the bits placed by the AI

    public void setMeshController(MeshController meshController) {
        this.meshController = meshController;
    }


    /**
     * Pave the whole mesh with AI.
     */
    public static void startAI(Slice slice) throws IOException, InterruptedException {
        bits.clear();

        Vector<Slice> slicesList = meshController.getMesh().getSlices();
        for (Slice currentSlice : slicesList) {
            sliceMap.put(
                    currentSlice,
                    (Vector<Segment2D>) currentSlice.getSegmentList().clone()
            );
        }
        meshController.AIneedPaint = true; //debugOnly

        //DEBUGONLY todo ici on teste juste en placant le bit 1
        //todo startAI doit permettre de paver entièrement une slice

        Vector<Vector2> associatedPoints = new Vector<>();

        Vector<Vector<Vector2>> bounds = AI_Tool.dataPrep.getBoundsAndRearrange(slice);
        Vector<Vector2> bound1 = bounds.get(0);
        Vector2 startPoint = bound1.get(40);

        Vector<Vector2> sectionPoints = DataPreparation.getSectionPoints(bound1, startPoint);

        double angleLocalSystem = DataPreparation.getLocalCoordinateSystemAngle(sectionPoints);

        Bit2D bit = DeepL.getBitPlacement(sectionPoints, startPoint, angleLocalSystem);

        bits.add(bit);
    }

    /**
     * @return the meshController
     */
    public static MeshController getMeshController() {
        return meshController;
    }

    /**
     * @return the map of the slices, with Vector of Segment2D associated to each Slice
     */
    public static Map<Slice, Vector<Segment2D>> getSliceMap() {
        return sliceMap;
    }

    public static Vector<Bit2D> getBits() {
        return bits;
    }
}