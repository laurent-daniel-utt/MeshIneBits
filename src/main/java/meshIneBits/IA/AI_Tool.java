package meshIneBits.IA;

import meshIneBits.IA.IA_util.DataSet;
import meshIneBits.IA.IA_util.Tools;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class AI_Tool {
    private Tools tools;
    private MeshController meshController;
    private Map<Slice, Vector<Segment2D>> sliceMap = new LinkedHashMap();
    public DataPreparation dataPrep;
    public Acquisition acquisition;
    public Training training;
    public Exploitation exploitation;
    public DataSet dataSet;

    /**
     * An AI_Tool lets the user pave the whole mesh, with artificial intelligence.
     * AI_Tool is based on a neural network that learns from how does a human place bits on the bounds of a Slice.
     *
     * @param MC the instance of meshController currently running.
     */
    public AI_Tool(MeshController MC) {
        this.meshController = MC;
        this.acquisition = new Acquisition(this);
        this.training = new Training(this);
        this.exploitation = new Exploitation(this);
        this.dataPrep = new DataPreparation(this);
        this.tools = new Tools();
        this.dataSet = new DataSet();
    }

    /**
     * Pave the whole mesh with AI.
     */
    public void startAI() { //started when pushing button on UI
        //todo startAI doit permettre de paver entièrement un modèle
        Vector<Slice> slicesList = meshController.getMesh().getSlices();
        for (Slice currentSlice : slicesList) {
            sliceMap.put(
                    currentSlice,
                    (Vector<Segment2D>) currentSlice.getSegmentList().clone()
            );
        }

        meshController.AIneedPaint = true; //debugOnly
        //placeBitsOnSlices(sliceMap);
    }

    /**
     * @return the meshController
     */
    public MeshController getMeshController() {
        return meshController;
    }

    /**
     * @return the map of the slices, with Vector of Segment2D associated to each Slice
     */
    public Map<Slice, Vector<Segment2D>> getSliceMap() {
        return sliceMap;
    }
}