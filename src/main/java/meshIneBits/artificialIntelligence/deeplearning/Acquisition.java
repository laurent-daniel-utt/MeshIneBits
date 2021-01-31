package meshIneBits.artificialIntelligence.deepLearning;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.AI_Tool;
import meshIneBits.artificialIntelligence.DebugTools;
import meshIneBits.artificialIntelligence.GeneralTools;
import meshIneBits.artificialIntelligence.util.DataLogEntry;
import meshIneBits.artificialIntelligence.util.DataLogger;
import meshIneBits.artificialIntelligence.util.DataSetGenerator;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Let the user save new inputs to store in the dataSet to later train the neural network.
 */
public class Acquisition {
    /**
     * If true, the bits placed manually by the user will be stored.
     */
    private static boolean storeNewBits = false;
    private static Bit2D lastPlacedBit; //useful to delete last placed bit
    private static Map<Bit2D, Vector<Vector2>> storedExamplesBits;

    /**
     * The bits placed manually by the user will be stored.
     */
    public static void startStoringBits() {
        storeNewBits = true;
        storedExamplesBits = new LinkedHashMap<>();
    }

    /**
     * The bits placed manually by the user will not be stored.
     */
    public static void stopStoringBits() throws IOException {
        storeNewBits = false;
        saveExamples();
    }

    /**
     * Delete the last manually placed bit by the user from the dataSet.
     */
    public static void deleteLastPlacedBit() {
        storedExamplesBits.remove(lastPlacedBit);
        lastPlacedBit.setUsedForNN(false);
    }

    /**
     * Save all examples in a file.
     */
    private static void saveExamples() throws IOException {
        for (Bit2D bit : storedExamplesBits.keySet()) {
            DataLogEntry entry = new DataLogEntry(bit, storedExamplesBits.get(bit));
            DataLogger.saveEntry(entry);
        }
        DataSetGenerator.generateCsvFile();
    }

    /**
     * Add a new example bit in the dataSet.
     *
     * @param bit the example to be added.
     */
    public static void addNewExampleBit(@NotNull Bit2D bit) throws Exception {
        if (isIrregular(bit)) {
            System.out.println("Example not added !");
            throw new Exception();
        }

        Vector<Vector2> points = new GeneralTools().getCurrentLayerBitAssociatedPoints(bit);
        storedExamplesBits.put(bit, points);
        lastPlacedBit = bit;
        System.out.println("Example added.");
        bit.setUsedForNN(true);
    }


    /**
     * checks if a bit can be used to train the neural net.
     * A bit can be used by the neural net only if the first intersection between an edge of the bit and the bound
     * (scanning it in the direction of the increasing indices) is made by a short edge of the bit.
     *
     * @param bit a {@link Bit2D}
     * @return true if the bit can not be used by the neural net.
     */
    private static boolean isIrregular(@NotNull Bit2D bit) {
        Vector<Segment2D> bitEdges = bit.getBitSidesSegments();

        Slice slice = AI_Tool.getMeshController().getCurrentLayer().getHorizontalSection();
        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);

        for (Vector<Vector2> bound : bounds) {
            for (int i = 0; i < bound.size() - 1; i++) {
                Segment2D boundSegment = new Segment2D(bound.get(i), bound.get(i + 1));

                Segment2D firstIntersectingEdge = null;
                double maxDistance = Double.POSITIVE_INFINITY;

                for (Segment2D bitEdge : bitEdges) {

                    // finds the edge of the bit that intersects the first
                    if (Segment2D.doSegmentsIntersect(bitEdge, boundSegment)
                            && Vector2.dist(bound.get(i), Segment2D.getIntersectionPoint(bitEdge, boundSegment)) < maxDistance) {
                        maxDistance = Vector2.dist(bound.get(i), Segment2D.getIntersectionPoint(bitEdge, boundSegment));
                        firstIntersectingEdge = bitEdge;
                    }

                    // check if the position of the bit is irregular
                    if (firstIntersectingEdge != null) {
                        if (Math.abs(firstIntersectingEdge.getLength() - CraftConfig.bitWidth) < Math.pow(10, -CraftConfig.errorAccepted))
                            return false; // the first intersection is a short edge of the bit
                    }
                }
            }
        }
        // only reached if the bit doesn't intersect with a bound
        return true;
    }

    public static boolean isStoreNewBits() {
        return storeNewBits;
    }
}
