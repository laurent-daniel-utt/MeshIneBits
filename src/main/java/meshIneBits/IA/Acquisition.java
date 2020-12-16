package meshIneBits.IA;

import meshIneBits.Bit2D;
import meshIneBits.IA.IA_util.AI_Exception;
import meshIneBits.IA.IA_util.DataLog;
import meshIneBits.IA.IA_util.DataLogEntry;
import meshIneBits.IA.IA_util.DataSetGenerator;
import meshIneBits.IA.genetics.Generation;
import meshIneBits.IA.genetics.Solution;
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
    }

    public static void addNewExampleBit(Bit2D bit) throws AI_Exception {
        Vector<Vector2> points = AI_Tool.dataPrep.getBitAssociatedPoints(bit);
        storedExamplesBits.put(bit, points);
        lastPlacedBit = bit;
        System.out.println("bit : " + bit);

        AI_Tool.dataPrep.pointsADessiner.addAll(points); //debugOnly


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
        double pos = DataSetGenerator.getBitEdgeAbscissa(bit.getOrigin(), bit.getOrientation(), points.firstElement());

        Generation generation = new Generation(1, 1, 1, 1, 1, new Vector2(1, 1), points);

        Solution solution = new Solution(pos, bit.getOrientation(), points.firstElement(), generation, points);

        System.out.println("===========DEBUG SOLUTION================");
        solution.evaluate(points);
        double scoreInit = solution.score;
        AI_Tool.dataPrep.scores.add(String.valueOf(scoreInit));
        //System.out.println("score init " + scoreInit);
        //solution.addPenaltyForBitAngle(points);
        //System.out.println("penalite angle" +  solution.score);
        //solution.addPenaltyForSectionCoveredLength(points);
        //System.out.println("penalite lengthy" + solution.score);
        System.out.println("=========================================");
//fin debugOnly


        System.out.println("example added");



    }
}
