package meshIneBits.IA;

import meshIneBits.Bit2D;
import meshIneBits.IA.IA_util.AI_Exception;
import meshIneBits.IA.IA_util.DataLog;
import meshIneBits.IA.IA_util.DataLogEntry;
import meshIneBits.IA.genetics.Genetic;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class Acquisition {
    public static boolean storeNewBits = false;
    public static Bit2D lastPlacedBit; //useful for delete last placed bit
    public static Vector2 startPoint = new Vector2(0, 0);//debugOnly
    private static Map<Bit2D, Vector<Vector2>> storedExamplesBits;

    public static void startStoringBits() {
        storeNewBits = true;
        storedExamplesBits = new LinkedHashMap<>();
    }

    public static void stopStoringBits() throws IOException {
        storeNewBits = false;
        saveExamples();
    }

    public static void deleteLastPlacedBit() {
        storedExamplesBits.remove(lastPlacedBit);
    }

    private static void saveExamples() throws IOException {
        for (Bit2D bit : storedExamplesBits.keySet()) {
            DataLogEntry entry = new DataLogEntry(bit, storedExamplesBits.get(bit));
            DataLog.saveEntry(entry);
        }
    }

    public static void addNewExampleBit(Bit2D bit) throws AI_Exception {

        storedExamplesBits.put(bit, AI_Tool.dataPrep.getBitAssociatedPoints(bit));
        lastPlacedBit = bit;

        AI_Tool.dataPrep.pointsADessiner.addAll(AI_Tool.dataPrep.getBitAssociatedPoints(bit)); //debugOnly

//debugOnly, on teste si la recherche du point suivant marche bien
        Vector<Vector2> pointList = new Vector<>();
        Vector<Slice> slicesList =AI_Tool.getMeshController().getMesh().getSlices();
        Vector<Segment2D> segment2DVector = slicesList.get(0).getSegmentList();
        for (Segment2D seg : segment2DVector) {
            pointList.add(new Vector2(seg.end.x, seg.end.y));
        }
        pointList = AI_Tool.dataPrep.getBoundsAndRearrange(AI_Tool.getSliceMap(), AI_Tool.getMeshController().getCurrentLayer().getHorizontalSection()).get(0);
        //todo modifier : ici on fait que du .get(0)

        startPoint = AI_Tool.dataPrep.getNextBitStartPoint(bit, pointList);
//fin debugOnly


        System.out.println("example added");



    }
}
