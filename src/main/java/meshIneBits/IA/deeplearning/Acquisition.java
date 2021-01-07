package meshIneBits.IA.deeplearning;

import meshIneBits.Bit2D;
import meshIneBits.IA.DebugTools;
import meshIneBits.IA.IA_util.DataLog;
import meshIneBits.IA.IA_util.DataLogEntry;
import meshIneBits.IA.IA_util.DataSetGenerator;
import meshIneBits.util.Vector2;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class Acquisition {
    public static boolean storeNewBits = false;
    public static Bit2D lastPlacedBit; //useful for delete last placed bit
    private static Map<Bit2D, Vector<Vector2>> storedExamplesBits;

    public static void startStoringBits() {
        storeNewBits = true;
        storedExamplesBits = new LinkedHashMap<>();

    }

    public static void stopStoringBits() throws IOException {
        storeNewBits = false;
        saveExamples();
        DebugTools.scores.clear(); //debugonly
        DebugTools.Bits.clear();//Debugonly
    }

    public static void deleteLastPlacedBit() {
        storedExamplesBits.remove(lastPlacedBit);
    }

    private static void saveExamples() throws IOException {
        for (Bit2D bit : storedExamplesBits.keySet()) {
            DataLogEntry entry = new DataLogEntry(bit, storedExamplesBits.get(bit));
            DataLog.saveEntry(entry);
        }
        DataSetGenerator.generateCsvFile();
    }

    public static void addNewExampleBit(Bit2D bit) throws Exception {


        Vector<Vector2> points = DataPreparation.getCurrentLayerBitAssociatedPoints(bit);
        storedExamplesBits.put(bit, points);
        lastPlacedBit = bit;


        DebugTools.pointsADessiner.clear();
        DebugTools.pointsADessiner.addAll(points);

        System.out.println("example added");

    }

}
