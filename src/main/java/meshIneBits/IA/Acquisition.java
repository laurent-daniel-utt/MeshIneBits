package meshIneBits.IA;

import meshIneBits.Bit2D;
import meshIneBits.IA.IA_util.AI_Exception;
import meshIneBits.IA.IA_util.DataSetEntry;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class Acquisition {
    public boolean storeNewBits = false;
    private Map<Bit2D, Vector<Vector2>> storedExamplesBits;
    private AI_Tool ai_tool;
    public Vector2 startPoint = new Vector2(0, 0);//debugOnly
    private Bit2D lastPlacedBit; //useful for delete last placed bit


    public Acquisition(AI_Tool ai_tool) {
        this.ai_tool = ai_tool;
    }

    public void startStoringBits() {
        storeNewBits = true;
        storedExamplesBits = new LinkedHashMap<>();
    }

    public void stopStoringBits() throws IOException {
        storeNewBits = false;
        saveExamples();
    }

    public void deleteLastPlacedBit() {
        storedExamplesBits.remove(lastPlacedBit);
    }

    private void saveExamples() throws IOException {
        for (Bit2D bit : storedExamplesBits.keySet()) {
            DataSetEntry entry = new DataSetEntry(bit, storedExamplesBits.get(bit));
            ai_tool.dataSet.saveEntry(entry);
        }
    }

    public void addNewExampleBit(Bit2D bit) throws AI_Exception {
        storedExamplesBits.put(bit, ai_tool.dataPrep.getBitAssociatedPoints(bit));
        lastPlacedBit = bit;

//debugOnly, on teste si la recherche du point suivant marche bien
        Vector<Vector2> pointList = new Vector<>();
        Vector<Slice> slicesList = ai_tool.getMeshController().getMesh().getSlices();
        Vector<Segment2D> segment2DVector = slicesList.get(0).getSegmentList();
        for (Segment2D seg : segment2DVector) {
            pointList.add(new Vector2(seg.end.x, seg.end.y));
        }
        pointList = ai_tool.dataPrep.getBoundsAndRearrange(ai_tool.getSliceMap(), ai_tool.getMeshController().getCurrentLayer().getHorizontalSection()).get(0);
        //todo modifier : ici on fait que du .get(0)

        startPoint = ai_tool.dataPrep.getNextBitStartPoint(bit, pointList);
//fin debugOnly
    }
}
