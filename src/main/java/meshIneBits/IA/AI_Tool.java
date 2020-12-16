package meshIneBits.IA;

import meshIneBits.IA.IA_util.Tools;
import meshIneBits.IA.genetics.Genetic;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

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


    public void setMeshController(MeshController meshController) {
        this.meshController = meshController;
    }


    /**
     * Pave the whole mesh with AI.
     */
    public static void startAI() { //started when pushing button on UI
        //todo startAI doit permettre de paver entièrement un modèle

        Vector<Slice> slicesList = meshController.getMesh().getSlices();
        for (Slice currentSlice : slicesList) {
            sliceMap.put(
                    currentSlice,
                    (Vector<Segment2D>) currentSlice.getSegmentList().clone()
            );
        }
        meshController.AIneedPaint = true; //debugOnly

        Slice sliceToTest = AI_Tool.getMeshController().getMesh().getSlices().get(0);
        Vector<Vector2> associatedPoints = new Vector<>();

        Vector<Vector<Vector2>> boundsToCheckAssociated = AI_Tool.dataPrep.getBoundsAndRearrange(sliceToTest); //debugonly on fait ici que la premiere slice
        Vector<Vector2> bound1 = boundsToCheckAssociated.get(0);
        for (Vector2 point : bound1) {
            //AI_Tool.dataPrep.pointsADessiner.add(point);
        }
        //placeBitsOnSlices(sliceMap); //todo remettre
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
}