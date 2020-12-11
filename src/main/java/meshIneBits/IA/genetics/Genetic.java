package meshIneBits.IA.genetics;

import meshIneBits.Bit2D;
import meshIneBits.IA.AI_Tool;
import meshIneBits.IA.IA_util.AI_Exception;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.util.Vector;

public class Genetic {
    public Evolution currentEvolution;
    private Vector<Bit2D> solutions = new Vector<>();

    /**
     * The tool for genetic algorithms for pavement.
     */
    public Genetic() {
        try {
            this.start();
        } catch (AI_Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the Genetic pavement and paves the whole mesh.
     */
    private void start() throws AI_Exception {
        //!! currently, just paves one Bit

        //Gets the associated points to the Bit2D to place, and creates an Evolution process
        /*Vector<Vector<Vector2>> sectionPoints = AI_Tool.dataPrep.getBoundsAndRearrange(AI_Tool.getSliceMap(), sliceToTest);
        Vector<Vector2> associatedPoints = AI_Tool.dataPrep.getBitAssociatedPoints(AI_Tool.acquisition.lastPlacedBit);
        AI_Tool.dataPrep.pointsADessiner = associatedPoints;

        AI_Tool.dataPrep.A = associatedPoints.get(0); //debugOnly, c'est le startPoint
*/

        Slice sliceToTest = AI_Tool.getMeshController().getMesh().getSlices().get(0);
        Vector<Vector2> associatedPoints = new Vector<>();
        Vector2 startPoint = sliceToTest.getSegmentList().get(0).start;
        Vector<Segment2D> segmentsToCheckAssociated = sliceToTest.getSegmentList();
        Vector<Vector2> points = new Vector<>();
        for (Segment2D seg : segmentsToCheckAssociated) {
            points.add(seg.start);
        }
        AI_Tool.dataPrep.A = startPoint;
        boolean isAfterStartPoint = false;
        for (Segment2D seg : segmentsToCheckAssociated) {//juste un test.. chepa si ca marche todo
            if (seg.start == startPoint)
                isAfterStartPoint = true;
            if (isAfterStartPoint && AI_Tool.dataPrep.getDistance(startPoint, seg.start) <= CraftConfig.bitLength) {//todo prendre en compte les quarts/demis bits
                System.out.println("on y est");
                associatedPoints.add(seg.start);
                AI_Tool.dataPrep.pointsContenus.add(seg.start);
            }
        }

        for (int i = 0; i < 1; i++) {
            currentEvolution = new Evolution(associatedPoints);
            System.out.println("launch evolution");
            currentEvolution.run();
            Bit2D bestBit = currentEvolution.bestSolution.bit;
            System.out.println("best : " + bestBit.toString());
            solutions.add(bestBit);

            startPoint = AI_Tool.dataPrep.getNextBitStartPoint(bestBit, points);
            associatedPoints.clear();
            isAfterStartPoint = false;
            for (Segment2D seg : segmentsToCheckAssociated) {//juste un test.. chepa si ca marche todo
                if (seg.start.equals(startPoint)) {
                    isAfterStartPoint = true;
                    System.out.println("on y est");
                }
                if (isAfterStartPoint && AI_Tool.dataPrep.getDistance(startPoint, seg.start) <= CraftConfig.bitLength) {//todo prendre en compte les quarts/demis bits
                    associatedPoints.add(seg.start);
                    //AI_Tool.dataPrep.pointsContenus.add(seg.start);
                }
            }

        }
    }

    public Vector<Bit2D> getSolutions() {
        return solutions;
    }
}