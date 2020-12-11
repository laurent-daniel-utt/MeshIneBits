package meshIneBits.IA;

import meshIneBits.IA.IA_util.Tools;
import meshIneBits.IA.genetics.Genetic;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class AI_Tool {
    public static DataPreparation dataPrep;
    public static Acquisition acquisition;
    public static Training training;
    public static Exploitation exploitation;
    public static Genetic genetic;
    private static Tools tools;
    private static MeshController meshController;
    private static Map<Slice, Vector<Segment2D>> sliceMap = new LinkedHashMap();

    /**
     * An AI_Tool lets the user pave the whole mesh, with artificial intelligence.
     * AI_Tool is based on a neural network that learns from how does a human place bits on the bounds of a Slice.
     *
     * @param MC the instance of meshController currently running.
     */
    public AI_Tool(MeshController MC) {
        meshController = MC;
        acquisition = new Acquisition();
        training = new Training();
        exploitation = new Exploitation();
        dataPrep = new DataPreparation();
        tools = new Tools();
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