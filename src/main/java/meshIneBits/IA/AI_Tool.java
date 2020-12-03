package meshIneBits.IA;

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

    //todo startAI doit permettre de paver entièrement un modèle
    public void startAI(MeshController MC) { //started when pushing button on UI
        this.meshController = MC;
        this.acquisition = new Acquisition(this);
        this.training = new Training(this);
        this.exploitation = new Exploitation(this);

        System.out.println("Starting IA pavement");
        System.out.println("Computing points of each slice");

        Vector<Slice> slicesList = MC.getMesh().getSlices();
        System.out.println(slicesList.size() + " slices to pave");

        for (Slice currentSlice : slicesList) {
            sliceMap.put(currentSlice, (Vector<Segment2D>) currentSlice.getSegmentList().clone());
        }

        meshController.AIneedPaint = true; //debugOnly
        this.tools = new Tools();
        this.dataPrep = new DataPreparation(this);
        //placeBitsOnSlices(sliceMap);

        //debugOnly pour vérifier que la fonction qui obtient le prochain Bit fonctionne
        //   Bit2D bitTest = new Bit2D(new Vector2(0,245), new Vector2(0,0));

    }

    public MeshController getMeshController() {
        return meshController;
    }
    public Map<Slice, Vector<Segment2D>> getSliceMap() {
        return sliceMap;
    }
}
