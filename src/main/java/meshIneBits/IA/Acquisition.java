package meshIneBits.IA;

import meshIneBits.Bit2D;
import meshIneBits.IA.IA_util.AI_Exception;
import meshIneBits.IA.IA_util.DataSet;
import meshIneBits.IA.IA_util.DataSetEntry;
import meshIneBits.IA.genetics.Genetic;
import meshIneBits.util.Vector2;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class Acquisition {
    public static boolean storeNewBits = false;
    public static Bit2D lastPlacedBit; //useful for delete last placed bit
    public Vector2 startPoint = new Vector2(0, 0);//debugOnly
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
            DataSetEntry entry = new DataSetEntry(bit, storedExamplesBits.get(bit));
            DataSet.saveEntry(entry);
        }
    }

    public static void addNewExampleBit(Bit2D bit) throws AI_Exception {
        storedExamplesBits.put(bit, AI_Tool.dataPrep.getBitAssociatedPoints(bit));
        lastPlacedBit = bit;

        Genetic genetic = new Genetic(); //debugOnly, on teste l'algo genetique

/*
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
//fin debugOnly*/
    }
}
