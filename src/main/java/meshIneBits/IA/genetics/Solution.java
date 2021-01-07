package meshIneBits.IA.genetics;


import meshIneBits.Bit2D;
import meshIneBits.IA.AI_Tool;
import meshIneBits.IA.deeplearning.DataPreparation;
import meshIneBits.IA.GeneralTools;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.DetectorTool;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Comparator;
import java.util.Vector;

public class Solution {

    /**
     * The coefficient associated to the mutation.
     */
    private static final double MUTATION_MAX_STRENGTH = 0.2;
    /**
     * The coefficient associated to the angle penalty.
     */
    private static final int ANGLE_PENALTY_STRENGTH = 5000;
    /**
     * The coefficient associated to the length penalty.
     */
    private static final int LENGTH_PENALTY_STRENGTH = 50000;
    private final Vector<Vector2> bound;
    private final Vector2 startPoint;
    private final Generation generation;
    public double bitPos;
    public Vector2 bitAngle;
    public double score = 0;
    public Bit2D bit;

    /**
     * A Solution is a Bit2D with position parameter in a local coordinate system. Its position is measured from its startPoint.
     */
    public Solution(double pos, Vector2 bitAngle, Vector2 startPoint, Generation generation, Vector<Vector2> bound) {
        this.bitPos = pos;
        this.bitAngle = bitAngle;
        this.startPoint = startPoint;
        this.generation = generation;
        this.bound = (Vector<Vector2>) bound.clone();
    }


    /**
     * Evaluates the current solution according to its lost Area.
     * Applies penalties to the score.
     *
     * @return the score of the current solution.
     */
    public double evaluate(Vector<Vector2> pointSection) {
        this.score = computeArea(); //the used area
        //this.addPenaltyForBitAngle((Vector<Vector2>) pointSection.clone());
        try {
            this.addPenaltyForSectionCoveredLength();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.score;
    }

    /**
     * Calculates the area of the Bit2D.
     * It is the area of the part of the Bit2D inside the Slice.
     *
     * @return the area.
     */
    private double computeArea() {
        /*
        To evaluate the score, we create a new bit with getBit()
        We cut it with cutPaths, we next get the cut Area and we compute its surface
        The score is the computed surface of the cut Bit.
         */

        bit = getBit(startPoint);
        Area availableBitArea = bit.getArea();
        Area availableArea = AI_Tool.getMeshController().getAvailableArea();

        availableBitArea.intersect(availableArea);//todo @Etienne pb here, area could be null

        if (availableBitArea.isEmpty() || DetectorTool.checkIrregular(availableBitArea)) { // Outside of border or irregular
            this.generation.solutions.remove(this);
            return 0;
        } else {

            bit.updateBoundaries(availableBitArea);
            bit.calcCutPath();

            Path2D finalCutPath = new Path2D.Double();
            boolean isFirstPoint = true;
            PathIterator pathIter = availableBitArea.getPathIterator(null);
            while (!pathIter.isDone()) {
                final double[] segment = new double[6];
                pathIter.currentSegment(segment);
                if (isFirstPoint) {
                    isFirstPoint = false;
                    finalCutPath.moveTo(segment[0], segment[1]);
                } else {
                    if (segment[0] != 0.0 && segment[1] != 0.0)
                        finalCutPath.lineTo(segment[0], segment[1]);
                }
                pathIter.next();
            }
            finalCutPath.closePath();

            int nPoints = 0;
            Vector<Double> XPoints = new Vector<>();
            Vector<Double> YPoints = new Vector<>();
            double area = 0;
            PathIterator iter = finalCutPath.getPathIterator(null);
            while (!iter.isDone()) {
                double[] segment = new double[6];
                iter.currentSegment(segment);
                XPoints.add(segment[0]);
                YPoints.add(segment[1]);
                //System.out.println(segment[0] + " " + segment[1]);
                //todo @all why is the last point (0,0) ?? remove it
                nPoints++;
                iter.next();
            }


            XPoints.remove(XPoints.lastElement());
            YPoints.remove(YPoints.lastElement());
            nPoints = XPoints.size();

            for (int i = 0; i < nPoints - 1; i++) {
                area += (XPoints.get(i) * YPoints.get(i + 1)) - (YPoints.get(i) * XPoints.get(i + 1));
            }
            //System.out.println("area = " + Math.abs(area / 2));
            return Math.abs(area / 2);
        }
    }

    /**
     * Mutates parameters of the solution.
     */
    public void mutate() { //todo @all tester
        if (Math.random() < 0.5) { // mutate bitPos
            bitPos += (Math.random() * 2 - 1) * MUTATION_MAX_STRENGTH;
            if (bitPos < 0)
                bitPos = 0;
            else if (bitPos > CraftConfig.bitWidth)
                bitPos = CraftConfig.bitWidth;
        } else { // mutate bitAngle
            bitAngle = new Vector2(bitAngle.x + (Math.random() * 2 - 1) * MUTATION_MAX_STRENGTH,
                    bitAngle.y + (Math.random() * 2 - 1) * MUTATION_MAX_STRENGTH)
                    .normal();
        }
    }

    public boolean isBad() {

        for (Vector2 pt : bound){
            if (Double.isNaN(pt.x) || Double.isNaN(pt.y)){
                System.out.println("==================== QQC EST NaN ====================");
            }
        }


        boolean bad = false;
        if (getNumberOfIntersections((Vector<Vector2>) bound.clone()) != 2) { //FIXME @Andre
            bad = true;
        }
        try {
            DataPreparation.getNextBitStartPoint(getBit(startPoint), (Vector<Vector2>) bound.clone());
        } catch (Exception e) {
            bad = true;
        }
        return bad;
    }

    /**
     * Returns the number of intersections between bit's edges and the given bound.
     *
     * @param boundPoints the bound of the Slice
     * @return the number of intersections
     */
    private int getNumberOfIntersections(Vector<Vector2> boundPoints) {

        Bit2D bit = getBit(startPoint);

        Vector<Segment2D> sectionSegments = GeneralTools.getSegment2DS(boundPoints);
        Vector<Segment2D> sides = GeneralTools.getBitSidesSegments(bit);

        int intersectionCount = 0;

        for (Segment2D sectionSegment : sectionSegments)
            for (Segment2D bitSides : sides) {
                if (GeneralTools.doesIntersect(sectionSegment, bitSides)) {
                    intersectionCount++;
                }
            }
        //System.out.println("intersectioncout " + intersectionCount);
        return intersectionCount;
    }


    /**
     * get the Solution's Bit2D according to the startPoint.
     *
     * @param startPoint the point where the bit's edge should be placed
     * @return the related Bit
     */
    private Bit2D getBit(Vector2 startPoint) {
        Vector2 collinear = this.bitAngle.normal();
        Vector2 orthogonal = collinear
                .rotate(new Vector2(0, -1)); // 90deg anticlockwise rotation
        Vector2 position = startPoint
                .add(orthogonal.mul(this.bitPos))
                .add(collinear.mul(CraftConfig.bitLength / 2))
                .sub(orthogonal.mul(CraftConfig.bitWidth / 2));


        return new Bit2D(position, this.bitAngle);
    }


    /**
     * Add a penalty to the score.
     * The more the angle of the bit is far away from the mean angle of the section on which it is placed,
     * the more the score will be decreased.
     * Depends of <code>ANGLE_PENALTY_STRENGTH</code>
     *
     * @param sectionPoints the section of the Slice
     */
    private void addPenaltyForBitAngle(Vector<Vector2> sectionPoints) { //todo @all enlever ???

        Bit2D bit2D = getBit(startPoint);

        double angleBit = Math.abs(bit2D.getOrientation().getEquivalentAngle2());
        double angleSection = DataPreparation.getSectionOrientation(sectionPoints);

        if (!DataPreparation.arePointsMostlyOrientedToTheRight(sectionPoints, sectionPoints.firstElement())) {
            angleSection = -Math.signum(angleSection) * 180 + angleSection;
        }

        // now angleBit and angleSection are expressed as values between -180 and 180, so we can compute the difference.
        double difference = Math.abs(angleSection - angleBit);

        // finally we add the angle penalty to the score
        //this.score -= ANGLE_PENALTY_STRENGTH * this.score * (difference / 180);
        this.score -= ANGLE_PENALTY_STRENGTH * difference / 180;
    }

    /**
     * Add a penalty to the score.
     * The less the Bit2D follows the bound of the Slice,
     * the more the score will be decreased.
     * Depends of <code>LENGTH_PENALTY_STRENGTH</code>
     */
    private void addPenaltyForSectionCoveredLength() throws Exception {

        Bit2D bit2D = getBit(startPoint);
        Vector2 nextBitStartPoint = DataPreparation.getNextBitStartPoint(bit2D, (Vector<Vector2>) bound.clone());
        double coveredDistance = Vector2.dist(startPoint, nextBitStartPoint);

        //this.score -= LENGTH_PENALTY_STRENGTH * this.score * CraftConfig.bitLength / coveredDistance;
        this.score -= LENGTH_PENALTY_STRENGTH / coveredDistance;
        if (Math.abs(coveredDistance - CraftConfig.bitLength) < 1) //todo inferieur à quoi ?
            this.score += LENGTH_PENALTY_STRENGTH;
    }


    /**
     * @return the position and the angle of the solution in a String.
     */
    public String toString() {
        return "pos: " + this.bitPos + " , angle: " + this.bitAngle;
    }

}

/**
 * Let the <code>Generation</code> compare two Solution by their scores.
 */
class SolutionComparator implements Comparator<Solution> {
    public int compare(Solution s1, Solution s2) {
        return Double.compare(s2.score, s1.score);
    }
}