package meshIneBits.IA.genetics;


import meshIneBits.Bit2D;
import meshIneBits.IA.AI_Tool;
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

    public double bitPos;
    public Vector2 bitAngle;
    public double score = 0;
    public Bit2D bit;
    public Vector2 startPoint;
    private Generation generation;

    private double MUTATION_MAX_STRENGTH = 0.1;
    private double ANGLE_PENALTY_STRENGTH = 0.3; // between 0 to 1 (max)
    private double LENGTH_PENALTY_STRENGTH = 0.3; // between 0 to 1 (max)

    /**
     * A Solution is a Bit2D with position and rotations parameters in a local coordinate system.
     */
    public Solution(double pos, Vector2 bitAngle, Vector2 startPoint, Generation generation) {
        this.bitPos = pos;
        this.bitAngle = bitAngle;
        this.startPoint = startPoint;
        //System.out.println("new solution : "+bitPos +" "+bitAngle);
        this.generation = generation;
    }


    /**
     * Evaluates the current solution according to its lost Area.
     *
     * @return the score of the current solution.
     */
    public double evaluate(Vector<Vector2> pointSection) {
        /*Vector<Double> Xpoints = new Vector<>();
        Vector<Double> Ypoints = new Vector<>();

        // We all calculate in coordinate
        // Reset cut paths
        int npoints=0;
        this.cutPaths = new Vector<>();
        Vector<Vector2> polygon = pointSection;
        // Define 4 corners
        Bit2D bit = getBit(startPoint);


        // Check cut path
        // If and edge lives on sides of the bit
        // We remove it
        for (int i=0;i<polygon.size()-1;i++) {
            Segment2D currentSeg = new Segment2D(polygon.get(i),polygon.get(i+1));
            if (sideTop.contains(currentSeg) || sideRight.contains(currentSeg)
                    || sideBottom.contains(currentSeg) || sideLeft.contains(currentSeg)) {
                polygon.remove(currentSeg);
            }
        }
        // After filter out the edges on sides
        // We form cut paths from these polygons
        Path2D cutPath2D = new Path2D.Double();
        cutPath2D.moveTo(polygon.get(0).x, polygon.get(0).y);
        for (int i = 0; i < polygon.size()-2; i++) {
            cutPath2D.lineTo( polygon.get(i+1).x, polygon.get(i+1).y);
            npoints++;
            Xpoints.add(polygon.get(i+1).x);
            Ypoints.add(polygon.get(i+1).y);
            // Some edges may have been deleted
            // So we check beforehand to skip
            if (i + 1 < polygon.size() && !polygon.contains(polygon.get(i+2))) {
                // If the next edge has been removed
                // We complete the path
                this.cutPaths.add(cutPath2D);
                // Then we create a new one
                // And move to the start of the succeeding edge
                cutPath2D = new Path2D.Double();
                cutPath2D.moveTo(polygon.get(i + 1).x, polygon.get(i + 1).y);
            }
        }

*/

        System.out.println("computing area");
        this.score = computeArea(); //the used area
        System.out.println("area computed. Adding penalty for angle...");

        this.addPenaltyForBitAngle((Vector<Vector2>) pointSection.clone());
        System.out.println("Adding penalty for covered length");
        //this.addPenaltyForSectionCoveredLength((Vector<Vector2>) pointSection.clone());//todo deboguer et remettre
        System.out.println("penalties added");
        return this.score;
    }

    /**
     * Calculates the area of the Bit2D.
     * Here it is the area of the part of the Bit2D inside the Slice.
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

        availableBitArea.intersect(availableArea);//todo @Etienne pb here, area could be null||
        try {
            if (availableBitArea.isEmpty() || DetectorTool.checkIrregular(availableBitArea)) {
                // Outside of border or irregular
                //this.generation.solutions.remove(this); todo @Etienne remettre après debug
            } else {
                bit.updateBoundaries(availableBitArea);
                bit.calcCutPath();

                System.out.println("BIT " + bit.toString());
                //AI_Tool.dataPrep.bit = bit.clone();//debugOnly, et virer tous les ai_tool


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
                AI_Tool.dataPrep.cutPathToDraw = (Path2D) finalCutPath.clone();


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
                    //System.out.println(segment[0] + " " + segment[1]); //todo pourquoi le dernier point c'est 0,0?? l'enlever
                    npoints++;
                    iter.next();
                }

                for (int i = 0; i < npoints; i++) {
                    area += (Xpoints.get(i) * Ypoints.get((i + 1) % npoints)) - (Ypoints.get(i) * Xpoints.get((i + 1) % npoints));
                }

                return Math.abs(area / 2);
            }
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
        return -1;
    }

    /**
     * Mutates parameters of a solution.
     */
    public void mutate() { //todo @all tester
        if (Math.random() < 0.5) { // mutate bitPos
            bitPos += (Math.random() * 2 - 1) * MUTATION_MAX_STRENGTH;
        } else { // mutate bitAngle
            bitAngle = new Vector2(bitAngle.x + (Math.random() * 2 - 1) * MUTATION_MAX_STRENGTH,
                    bitAngle.y + (Math.random() * 2 - 1) * MUTATION_MAX_STRENGTH);
        }
    }

    /**
     * Deletes the solution if it is bad.
     * Bad Solutions criterias :
     * - there is more than one intersection between bit's edges and the section
     * -...
     */
    public void deleteIfBad(Vector<Vector2> sectionPoints) {

        boolean bad = false;

        if (getNumberOfIntersections(sectionPoints) > 1) {//todo @Andre, je crois que toutes les solutions sont dégagées XD
            //bad = true;
        }

        if (bad) {
            generation.solutions.remove(this); //todo vérifier que ca marche
            System.out.println("deleted");
        }
    }

    /**
     * returns the number of intersections between bit's edges and the section.
     *
     * @param sectionPoints
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
                    if (AI_Tool.dataPrep.contains(bitSides, intersectionPoint)) {
                        if (AI_Tool.dataPrep.contains(sectionSegment, intersectionPoint)) {
                            intersectionCount += 1;
                        }
                    }
                }
            }
        return intersectionCount;
    }


    /**
     * get BitD2 based on the values of s
     *
     * @param startPoint the point where the bit's edge should be placed
     * @return the related Bit
     */
    private Bit2D getBit(Vector2 startPoint) {
        AI_Tool.acquisition.startPoint = startPoint;//debugOnly

        //bitAngle = new Vector2(1,0);//debugONLY
        //bitPos = 0;
        Vector2 colinear = this.bitAngle.normal();
        Vector2 orthogonal = colinear
                .rotate(new Vector2(0, 1)); // 90deg anticlockwise rotation
        Vector2 position = startPoint
                .add(orthogonal.mul(this.bitPos))
                .add(colinear.mul(CraftConfig.bitLength / 2))
                .sub(orthogonal.mul(CraftConfig.bitWidth / 2));

        return new Bit2D(position, this.bitAngle);
    }


    private void addPenaltyForBitAngle(Vector<Vector2> sectionPoints) { //todo @all tester

        Bit2D bit2D = getBit(startPoint);

        double angleBit = Math.abs(bit2D.getOrientation().getEquivalentAngle2());

        double angleSection = AI_Tool.dataPrep.getSectionOrientation(sectionPoints) * (180 / Math.PI);

        // recenter points so the first point of the section is on the Oy axis.
        // This way we'll can use arePointsMostlyToTheRight method
        Vector<Vector2> oYRecenteredPoints = new Vector();
        for (int i = 0; i < sectionPoints.size(); i++) {
            oYRecenteredPoints.add(new Vector2(
                    sectionPoints.get(i).x - sectionPoints.firstElement().x,
                    sectionPoints.get(i).y));
        }
        if (!AI_Tool.dataPrep.arePointsMostlyToTheRight(sectionPoints)) {
            angleSection = Math.sin(angleSection) * (-180 + angleSection);
        }

        // now angleBit and angleSection are expressed as values between -180 and 180, so we can compute the difference.
        double difference = Math.abs(angleSection - angleBit);

        // finally we add the angle penalty to the score

        this.score -= ANGLE_PENALTY_STRENGTH * this.score * (difference / 180);
    }


    private void addPenaltyForSectionCoveredLength(Vector<Vector2> sectionPoints) { //todo @all tester

        Bit2D bit2D = getBit(startPoint);
        AI_Tool.dataPrep.bit = bit2D;
        Vector<Segment2D> sectionSegments = new Vector<>();
        for (int i = 0; i < sectionPoints.size() - 1; i++) {
            sectionSegments.add(new Segment2D(
                    sectionPoints.get(i),
                    sectionPoints.get(i + 1)
            ));
        }

        Vector<Segment2D> sides = AI_Tool.dataPrep.getBitSidesSegments(bit2D);

        boolean firstIntersectionFound = false;
        Vector2 firstIntersectionPoint = null;

        int i_segment = 0;
        while (i_segment < sectionSegments.size() && !firstIntersectionFound) {

            int i_bitSide = 0;
            while (i_bitSide < 4 && !firstIntersectionFound) {
                Vector2 intersectionPoint = sides.get(i_bitSide).intersect(sectionSegments.get(i_segment)); //null if parallel
                if (intersectionPoint != null) {
                    if (AI_Tool.dataPrep.contains(sides.get(i_bitSide), intersectionPoint)) {
                        System.out.println("sides contains");
                        if (AI_Tool.dataPrep.contains(sectionSegments.get(i_segment), intersectionPoint)) {
                            System.out.println("section contains");
                            firstIntersectionFound = true;
                            firstIntersectionPoint = intersectionPoint;
                        }
                    }
                }
                AI_Tool.dataPrep.A = intersectionPoint;
                AI_Tool.dataPrep.currentSegToDraw = sides.get(i_bitSide);
                AI_Tool.dataPrep.currentSegToDraw2 = sectionSegments.get(i_segment);

                i_bitSide++;
            }
            i_segment++; //j'ai ajouté ca aussi
        }

        this.score -= LENGTH_PENALTY_STRENGTH * this.score
                * (Vector2.dist(sectionPoints.firstElement(), firstIntersectionPoint) / CraftConfig.bitLength);
    }

    public String toString() {
        return "pos: " + this.bitPos + " , angle: " + this.bitAngle;
    }

    public Solution clone() {
        return new Solution(this.bitPos, this.bitAngle, this.startPoint, this.generation);
    }
}


class SolutionComparator implements Comparator<Solution> {
    public int compare(Solution s1, Solution s2) {
        if (s1.score > s2.score)
            return -1;
        if (s1.score < s2.score) {
            return 1;
        }
        return 0;
    }
}