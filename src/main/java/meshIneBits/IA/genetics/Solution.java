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

    private final int LENGTH_PENALTY_STRENGTH;//50000
    private final Vector<Vector2> bound;
    private final Vector2 startPoint;
    private final Generation generation;
    private boolean hasBeenEvaluated = false;
    private boolean hasBeenCheckedBad = false;
    private boolean bad = false;
    private Bit2D bit;
    private double bitPos;
    private Vector2 bitAngle;
    private double score = 0;


    /**
     * A Solution is a Bit2D with position parameter in a local coordinate system. Its position is measured from its startPoint.
     */
    public Solution(double pos, Vector2 bitAngle, Vector2 startPoint, Generation generation, Vector<Vector2> bound, int length_penalty) {
        this.bitPos = pos;
        this.bitAngle = bitAngle;
        this.startPoint = startPoint;
        this.generation = generation;
        this.bound = bound;
        this.LENGTH_PENALTY_STRENGTH = length_penalty;
    }


    /**
     * Evaluates the current solution according to its lost surface.
     * Applies penalties to the score.
     *
     * @return the score of the current solution.
     */
    public double evaluate() {
        if (hasBeenEvaluated)
            return score;
        score = computeArea(); //the used area
        try {
            addPenaltyForSectionCoveredLength();
            hasBeenEvaluated = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return score;
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


            area = 0;
            for (int i = 0; i < nPoints - 1; i++) {
                int j = i + 1;
                area += XPoints.get(i) * YPoints.get(j);
                area -= YPoints.get(i) * XPoints.get(j);
            }
            return Math.abs(area);
            /*

            for (int i = 0; i < nPoints - 1; i++) {
                area += (XPoints.get(i) * YPoints.get(i + 1)) - (YPoints.get(i) * XPoints.get(i + 1));
            }
            return Math.abs(area / 2);

             */
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
        hasBeenEvaluated = false;
        hasBeenCheckedBad = false;
    }

    public boolean isBad() {
        if (hasBeenCheckedBad)
            return bad;
        try {
            DataPreparation.getNextBitStartPoint(getBit(startPoint), bound);
        } catch (Exception e) {
            bad = true;
        }
        hasBeenCheckedBad = true;
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

        Vector<Segment2D> segmentsSlice = GeneralTools.getSegment2DS(boundPoints);
        Vector<Segment2D> bitSides = GeneralTools.getBitSidesSegments(bit);

        Vector<Vector2> pointsSlice = DataPreparation.getBoundsAndRearrange(AI_Tool.getMeshController().getCurrentLayer().getHorizontalSection()).get(0);
        Vector<Segment2D> segmentsSliceeee = GeneralTools.getSegment2DS(pointsSlice);

        System.out.println();
        //System.out.println(bound.toString());
        System.out.println(pointsSlice.toString());
        System.out.println(segmentsSliceeee.toString());

        //DebugTools.pointsADessiner.clear();

        for (Segment2D seg : bitSides) {
            //DebugTools.pointsADessiner.add(seg.start);
        }


        int intersectionCount = 0;

        Vector2 inter;
        for (Segment2D segmentSlice : segmentsSliceeee)
            for (Segment2D bitSide : bitSides)
                if (GeneralTools.doSegmentsIntersect(segmentSlice, bitSide)) {
                    // fixme
                    inter = GeneralTools.getIntersectionPoint(segmentSlice, bitSide);
                    if (GeneralTools.isPointOnSegment(inter, segmentSlice)) {
                        intersectionCount++;
                        //DebugTools.pointsADessiner.add(inter);
                        System.out.println("pt " + inter.toString());
                    }
                }

        System.out.println("nb inters " + intersectionCount);
        ;
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
     * The less the Bit2D follows the bound of the Slice,
     * the more the score will be decreased.
     * Depends of <code>LENGTH_PENALTY_STRENGTH</code>
     */
    private void addPenaltyForSectionCoveredLength() throws Exception {

        Bit2D bit2D = getBit(startPoint);
        Vector2 nextBitStartPoint = DataPreparation.getNextBitStartPoint(bit2D, bound);
        double coveredDistance = Vector2.dist(startPoint, nextBitStartPoint);

      /*  if (Math.abs(coveredDistance - CraftConfig.bitLength) < 1)
            this.score += LENGTH_PENALTY_STRENGTH*score;
        else
            this.score -= LENGTH_PENALTY_STRENGTH * score / coveredDistance;//debugOnly
        if (Math.abs(coveredDistance - CraftConfig.bitLength) < 1)
            score += LENGTH_PENALTY_STRENGTH*score;
        else*/
        //double maxArea = CraftConfig.bitLength * CraftConfig.bitWidth;
        //score = (0.2 * score / maxArea + 0.85 * coveredDistance / CraftConfig.bitLength);

        score *= coveredDistance / CraftConfig.bitLength;
    }


    /**
     * @return the position and the angle of the solution in a String.
     */
    public String toString() {
        return "pos: " + this.bitPos + " , angle: " + this.bitAngle;
    }

    public Bit2D getBit() {
        return bit;
    }

    public double getBitPos() {
        return bitPos;
    }

    public Vector2 getBitAngle() {
        return bitAngle;
    }

    public double getScore() {
        return score;
    }
}

/**
 * Let the <code>Generation</code> compare two Solution by their scores.
 */
class SolutionComparator implements Comparator<Solution> {
    public int compare(Solution s1, Solution s2) {
        return Double.compare(s2.getScore(), s1.getScore());
    }
}