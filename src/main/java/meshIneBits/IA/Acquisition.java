package meshIneBits.IA;

import meshIneBits.Bit2D;
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
    public static Vector2 startPoint = new Vector2(0, 0);//debugOnly
    private static Map<Bit2D, Vector<Vector2>> storedExamplesBits;

    public static void startStoringBits() {
        storeNewBits = true;
        storedExamplesBits = new LinkedHashMap<>();
    }

    public static void stopStoringBits() throws IOException {
        storeNewBits = false;
        saveExamples();
        AI_Tool.dataPrep.scores.clear(); //debugonly
        AI_Tool.dataPrep.Bits.clear();//Debugonly
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


        Vector<Vector2> points = AI_Tool.dataPrep.getBitAssociatedPoints(bit);
        storedExamplesBits.put(bit, points);
        lastPlacedBit = bit;

        /*

        // test coordinate system transformations

        Vector2 startPointGlobal = points.firstElement();
        double angleLocalSystem = DataPreparation.getLocalCoordinateSystemAngle(points);

        double edgeAbscissa = DataSetGenerator.getBitEdgeAbscissa(bit.getCenter(), bit.getOrientation(), startPointGlobal);
        double bitOrientationLocal = DataSetGenerator.getBitAngleInLocalSystem(bit.getOrientation(), points);

        System.out.println("bitOrientationLocal " + bitOrientationLocal);


        // reverse
        Vector2 posLocalSystem = startPointGlobal;

        Bit2D bit2 = Exploitation.getBitFromNeuralNetworkOutput(edgeAbscissa, bitOrientationLocal, posLocalSystem, angleLocalSystem);

        // display
        Vector<Segment2D> segs = DataPreparation.getBitSidesSegments(bit2);
        AI_Tool.dataPrep.pointsADessiner.add(segs.get(0).start);
        AI_Tool.dataPrep.pointsADessiner.add(segs.get(1).start);
        AI_Tool.dataPrep.pointsADessiner.add(segs.get(2).start);
        AI_Tool.dataPrep.pointsADessiner.add(segs.get(3).start);


        AI_Tool.dataPrep.pointsADessiner.add(startPointGlobal);
        AI_Tool.dataPrep.pointsADessiner.add(startPointGlobal.add(Vector2.getEquivalentVector(angleLocalSystem).mul(120)));

        System.out.println("edgeAbscissa = " + edgeAbscissa);
        System.out.println("angle Repere local = " + angleLocalSystem);

         */



        /*
                // if bitAngle vector has it's x cooordinate negative, we take the opposite vector because we
        // don't need a -180 to 180 degrees orientation but only a -90 to 90 rotation
        if (bitAngle.x<0){
            bitAngle = new Vector2(-bitAngle.x, -bitAngle.y);
        }
         */


//debugOnly, on teste si la recherche du point suivant marche bien
        /*
        Vector<Vector2> pointList = new Vector<>();
        Vector<Slice> slicesList =AI_Tool.getMeshController().getMesh().getSlices();
        Vector<Segment2D> segment2DVector = slicesList.get(0).getSegmentList();
        for (Segment2D seg : segment2DVector) {
            pointList.add(new Vector2(seg.end.x, seg.end.y));
        }
        pointList = AI_Tool.dataPrep.getBoundsAndRearrange(AI_Tool.getMeshController().getCurrentLayer().getHorizontalSection()).get(0);
        //todo modifier : ici on fait que du .get(0)

        startPoint = AI_Tool.dataPrep.getNextBitStartPoint(bit, pointList);
*/


        //afficher resultats scoring genetics
        /*
        double pos = DataSetGenerator.getBitEdgeAbscissa(bit.getOrigin(), bit.getOrientation(), points.firstElement());

        Generation generation = new Generation(1, 1, 1, 1, 1, new Vector2(1, 1), points);

        Solution solution = new Solution(pos, bit.getOrientation(), points.firstElement(), generation, points);

         */
//fin debugOnly


        System.out.println("example added");

    }
}
