package meshIneBits.IA.genetics;


import meshIneBits.Bit2D;
import meshIneBits.IA.AI_Tool;
import meshIneBits.IA.DataPreparation;
import meshIneBits.IA.IA_util.AI_Exception;
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

    private final Vector<Vector2> bound;
    public double bitPos;
    public Vector2 bitAngle;
    public double score = 0;
    public Bit2D bit;
    private final Vector2 startPoint;
    private final Generation generation;

    /**
     * The coefficient associated to the mutation.
     */
    private final double MUTATION_MAX_STRENGTH = 0.2;
    /**
     * The coefficient associated to the angle penalty. Must be between 0 and 1.
     */
    private final double ANGLE_PENALTY_STRENGTH = 0; // between 0 to 1 (max)
    /**
     * The coefficient associated to the length penalty. Must be between 0 and 1.
     */
    private final double LENGTH_PENALTY_STRENGTH = 0; // between 0 to 1 (max)

    /**
     * A Solution is a Bit2D with position parameter in a local coordinate system. Its position is measured from its startPoint.
     */
    public Solution(double pos, Vector2 bitAngle, Vector2 startPoint, Generation generation, Vector<Vector2> bound) {
        this.bitPos = pos;
        this.bitAngle = bitAngle;
        this.startPoint = startPoint;
        this.generation = generation;
        this.bound = bound;
    }


    /**
     * Evaluates the current solution according to its lost Area.
     * Applies penalties to the score.
     *
     * @return the score of the current solution.
     */
    public double evaluate(Vector<Vector2> pointSection) {
        this.score = computeArea(); //the used area
        this.addPenaltyForBitAngle((Vector<Vector2>) pointSection.clone());
        this.addPenaltyForSectionCoveredLength((Vector<Vector2>) pointSection.clone());//todo deboguer et remettre
        return this.score;
    }

    /**
     * Calculates the area of the Bit2D.
     * It is the area of the part of the Bit2D inside the Slice.
     *
     * @return the area.
     */
    private double computeArea() {

        //en gros pour évaluer le score, on créé un nouveau bit avec getBit()
        //ensuite on affecte des cutsPaths à ce bit, on le découpe quoi
        //on obtient l'aire découpée, dont on calcule la surface
        //après le score c'est l'aire du bit découpé

        bit = getBit(startPoint);
        Area availableBitArea = bit.getArea();
        Area availableArea = AI_Tool.getMeshController().availableArea;

        AI_Tool.dataPrep.hasNewBitToDraw = true;//debugonly
        //AI_Tool.dataPrep.areaToDraw = (Area) availableArea.clone();

        availableBitArea.intersect(availableArea);//todo @Etienne pb here, area could be null

        if (availableBitArea.isEmpty() || DetectorTool.checkIrregular(availableBitArea)) { // Outside of border or irregular
            this.generation.solutions.remove(this);
            return -1;
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

            int npoints = 0;
            Vector<Double> Xpoints = new Vector<>();
            Vector<Double> Ypoints = new Vector<>();
            double area = 0;
            PathIterator iter = finalCutPath.getPathIterator(null);
            while (!iter.isDone()) {
                double[] segment = new double[6];
                iter.currentSegment(segment);
                Xpoints.add(segment[0]);
                Ypoints.add(segment[1]);
                //System.out.println(segment[0] + " " + segment[1]);
                // todo pourquoi le dernier point c'est 0,0?? l'enlever
                npoints++;
                iter.next();
            }


            Xpoints.remove(Xpoints.lastElement());
            Ypoints.remove(Ypoints.lastElement());
            npoints = Xpoints.size();
            for (int i = 0; i < npoints; i++) {
                System.out.println(Xpoints.get(i) + " : " + Ypoints.get(i));
            }
            for (int i = 0; i < npoints - 1; i++) {
                area += (Xpoints.get(i) * Ypoints.get(i + 1)) - (Ypoints.get(i) * Xpoints.get(i + 1));
            }
            System.out.println("area = " + Math.abs(area / 2));
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

    /**
     * Deletes the solution if it is bad.
     * Bad Solutions criterias :
     * - there is more than one intersection between bit's edges and the section
     * -...todo completer doc
     */
    public void deleteIfBad(Vector<Vector2> sectionPoints) {

        boolean bad = false;
        if (getNumberOfIntersections(sectionPoints) > 1) {//todo @Andre, je crois que toutes les solutions sont dégagées XD
            bad = true;
        }
        try {
            if (AI_Tool.dataPrep.getNextBitStartPoint(getBit(startPoint), bound) == null) {
                bad = true;
            }
        } catch (AI_Exception e) {
            e.printStackTrace();
            System.out.println("nextBitStartPoint non trouvé dans Solution");
        }

        if (bad) generation.solutions.remove(this);
    }

    /**
     * returns the number of intersections between bit's edges and the section.
     *
     * @param sectionPoints the section of the Slice
     * @return the number of intersections
     */

    private int getNumberOfIntersections(Vector<Vector2> sectionPoints) {

        Bit2D bit = getBit(startPoint);

        Vector<Segment2D> sectionSegments = new Vector<>();
        for (int i = 0; i < sectionPoints.size() - 1; i++) {
            sectionSegments.add(new Segment2D(
                    sectionPoints.get(i),
                    sectionPoints.get(i + 1)
            ));
        }

        Vector<Segment2D> sides = AI_Tool.dataPrep.getBitSidesSegments(bit);

        int intersectionCount = 0;

        for (Segment2D sectionSegment : sectionSegments)
            for (Segment2D bitSides : sides) {
                Vector2 intersectionPoint = bitSides.intersect(sectionSegment); //null if parallel
                if (intersectionPoint != null) {
                    if (intersectionPoint.isOnSegment(bitSides)) {
                        if (intersectionPoint.isOnSegment(sectionSegment)) {
                            intersectionCount += 1;
                        }
                    }
                }
            }
        return intersectionCount;
    }


    /**
     * get the Solution's Bit2D according to the startPoint.
     *
     * @param startPoint the point where the bit's edge should be placed
     * @return the related Bit
     */
    private Bit2D getBit(Vector2 startPoint) {
        Vector2 colinear = this.bitAngle.normal();
        Vector2 orthogonal = colinear
                .rotate(new Vector2(0, 1)); // 90deg anticlockwise rotation
        Vector2 position = startPoint
                .add(orthogonal.mul(this.bitPos))
                .add(colinear.mul(CraftConfig.bitLength / 2))
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
    private void addPenaltyForBitAngle(Vector<Vector2> sectionPoints) { //todo @all tester

        Bit2D bit2D = getBit(startPoint);

        double angleBit = Math.abs(bit2D.getOrientation().getEquivalentAngle2());
        double angleSection = DataPreparation.getSectionOrientation(sectionPoints);

        // recenter points so the first point of the section is on the Oy axis.
        // This way we'll can use arePointsMostlyToTheRight method
        Vector<Vector2> oYRecenteredPoints = new Vector<>();
        for (Vector2 sectionPoint : sectionPoints) {
            oYRecenteredPoints.add(new Vector2(
                    sectionPoint.x - sectionPoints.get(0).x,
                    sectionPoint.y));
        }
        if (!DataPreparation.arePointsMostlyToTheRight(oYRecenteredPoints)) {
            angleSection = -Math.signum(angleSection) * 180 + angleSection;
        }

        // now angleBit and angleSection are expressed as values between -180 and 180, so we can compute the difference.
        double difference = Math.abs(angleSection - angleBit);

        // finally we add the angle penalty to the score
        this.score -= ANGLE_PENALTY_STRENGTH * this.score * (difference / 180);
    }

    /**
     * Add a penalty to the score.
     * The less the Bit2D follows the bound of the Slice,
     * the more the score will be decreased.
     * Depends of <code>LENGTH_PENALTY_STRENGTH</code>
     *
     * @param sectionPoints the section of the Slice
     */
    private void addPenaltyForSectionCoveredLength(Vector<Vector2> sectionPoints) { //todo @all tester

        Bit2D bit2D = getBit(startPoint);
        AI_Tool.dataPrep.hasNewBitToDraw = true;
        Vector<Segment2D> sectionSegments = new Vector<>();
        for (int i = 0; i < sectionPoints.size() - 1; i++) {
            sectionSegments.add(new Segment2D(
                    sectionPoints.get(i),
                    sectionPoints.get(i + 1)
            ));
        }

        Vector<Segment2D> sides = AI_Tool.dataPrep.getBitSidesSegments(bit2D);
        Vector2 firstIntersectionPoint = null;
        double coveredDistance = -1;

        int i_segment = 0;
        while (i_segment < sectionSegments.size() && firstIntersectionPoint == null) {

            for (int i_bitSide = 0; i_bitSide < 4; i_bitSide++) {
                Vector2 intersectionPoint = sides.get(i_bitSide).intersect(sectionSegments.get(i_segment)); //null if parallel

                if (intersectionPoint != null) {
                    if (intersectionPoint.isOnSegment(sides.get(i_bitSide))) {
                        if (intersectionPoint.isOnSegment(sectionSegments.get(i_segment))) {
                            coveredDistance = Vector2.dist(startPoint, intersectionPoint);
                            if (coveredDistance != 0.0) {
                                firstIntersectionPoint = intersectionPoint;
                                break;
                            }
                        }
                    }
                }
            }
            i_segment++;
        }
        if (firstIntersectionPoint == null) {
            generation.solutions.remove(this);
            System.out.print("    deleted because firstIntersectionPoint is null");
            return;
        }

        this.score -= LENGTH_PENALTY_STRENGTH * this.score * coveredDistance / CraftConfig.bitLength;
    }

    /**
     * @return the position and the angle of the solution in a String.
     */
    public String toString() {
        return "pos: " + this.bitPos + " , angle: " + this.bitAngle;
    }

    /**
     * @return the cloned Solution.
     */
    public Solution clone() {
        return new Solution(this.bitPos, this.bitAngle, this.startPoint, this.generation, bound);
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