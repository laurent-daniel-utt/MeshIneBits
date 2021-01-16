package meshIneBits.artificialIntelligence.deeplearning;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.AI_Tool;
import meshIneBits.artificialIntelligence.DebugTools;
import meshIneBits.artificialIntelligence.util.DataLog;
import meshIneBits.artificialIntelligence.util.DataLogEntry;
import meshIneBits.artificialIntelligence.util.DataSetGenerator;
import meshIneBits.util.Vector2;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class Acquisition {
    public static boolean storeNewBits = false;
    public static Bit2D lastPlacedBit; //useful to delete last placed bit
    private static Map<Bit2D, Vector<Vector2>> storedExamplesBits;

    public static void startStoringBits() {
        storeNewBits = true;
        storedExamplesBits = new LinkedHashMap<>();
    }

    public static void stopStoringBits() throws IOException {
        storeNewBits = false;
        saveExamples();
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
        System.out.println("example added");

        Vector<Vector2> pointsSlice = DataPreparation.getBoundsAndRearrange(AI_Tool.getMeshController().getCurrentLayer().getHorizontalSection()).get(0);

        Vector2 nextBitStartPoint = DataPreparation.getBitAndContourSecondIntersectionPoint(bit, pointsSlice);
        DebugTools.pointsToDrawRED.add(nextBitStartPoint);

    }

}
