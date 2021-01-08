package meshIneBits.IA.deeplearning;

import meshIneBits.Bit2D;
import meshIneBits.IA.AI_Tool;
import meshIneBits.IA.DebugTools;
import meshIneBits.IA.GeneralTools;
import meshIneBits.IA.IA_util.DataLog;
import meshIneBits.IA.IA_util.DataLogEntry;
import meshIneBits.IA.IA_util.DataSetGenerator;
import meshIneBits.IA.genetics.Solution;
import meshIneBits.util.Segment2D;
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

        System.out.println("example added");

        /*
        //debugOnly
        Vector<Segment2D> bitSides = GeneralTools.getBitSidesSegments(bit);


        int intersectionCount = 0;

        //DebugTools.pointsADessiner.clear();
        Vector<Vector2> pointsSlice = DataPreparation.getBoundsAndRearrange(AI_Tool.getMeshController().getCurrentLayer().getHorizontalSection()).get(0);
        Vector<Segment2D> segmentsSlice = GeneralTools.getSegment2DS(pointsSlice);

        for (Segment2D segmentSlice : segmentsSlice)
            for (Segment2D bitSide : bitSides)
                if (GeneralTools.doSegmentsIntersect(segmentSlice, bitSide)) {
                    intersectionCount++;
                    Vector2 inter = GeneralTools.getIntersectionPoint(segmentSlice, bitSide);
                    //DebugTools.pointsADessiner.add(inter);
                    System.out.println("pt " + inter.toString());

                }

        for (Segment2D seg : bitSides) {
            //DebugTools.pointsADessiner.add(seg.start);
        }
        System.out.println("INTERSECTIONs : "+intersectionCount);



    }

         */

        Vector<Vector2> pointsSlice = DataPreparation.getBoundsAndRearrange(AI_Tool.getMeshController().getCurrentLayer().getHorizontalSection()).get(0);

        Vector2 nextBitStartPoint = DataPreparation.getBitAndContourSecondIntersectionPoint(bit, pointsSlice);
        DebugTools.pointsADessiner.add(nextBitStartPoint);

    }

}
