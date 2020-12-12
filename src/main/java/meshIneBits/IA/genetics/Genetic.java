package meshIneBits.IA.genetics;

import meshIneBits.Bit2D;
import meshIneBits.IA.AI_Tool;
import meshIneBits.IA.DataPreparation;
import meshIneBits.IA.IA_util.AI_Exception;
import meshIneBits.Layer;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class Genetic {
    private final Layer layer;
    public Evolution currentEvolution;
    private Vector<Bit2D> solutions = new Vector<>();
    private Map<Slice, Vector<Segment2D>> sliceMap = new LinkedHashMap();

    /**
     * The tool for genetic algorithms for pavement.
     */
    public Genetic(Layer layer) {
        System.out.println("On Pave le Layer : " + layer.getLayerNumber());
        this.layer = layer;
        this.start();
    }

    /**
     * Starts the Genetic pavement and paves the whole mesh.
     */
    private void start() {
        Vector<Slice> slicesList = AI_Tool.getMeshController().getMesh().getSlices();
        slicesList.forEach(currentSlice -> sliceMap.put(
                currentSlice,
                (Vector<Segment2D>) currentSlice.getSegmentList().clone()
        ));

        Slice sliceToTest = layer.getHorizontalSection();

        Vector<Vector<Vector2>> boundsToCheckAssociated = DataPreparation.getBoundsAndRearrange(sliceToTest); //debugonly on fait ici que la premiere slice
        Vector<Vector2> bound1 = boundsToCheckAssociated.get(0);//debugonly on fait ici que le premier contour
        Vector2 startPoint = bound1.get(10);//todo ne jamais partir du point 0 ou -1
        Vector2 veryFirstStartPoint = startPoint;
        Vector<Vector2> associatedPoints = getAssociatedPoints(bound1, startPoint);


        Bit2D bestBit;
        //todo @Etienne paver chaque contour aussi
        int nbIters = 0;
        while (!hasCompletedTheBound(veryFirstStartPoint, associatedPoints)) {
            nbIters++;
            if (nbIters > 40)//nombre max d'iters avant de stopper
                break;
            System.out.println(nbIters);
            currentEvolution = new Evolution((Vector<Vector2>) associatedPoints.clone(), startPoint, (Vector<Vector2>) bound1.clone());
            currentEvolution.run();
            bestBit = currentEvolution.bestSolution.bit;
            solutions.add(bestBit);


            startPoint = AI_Tool.dataPrep.getNextBitStartPoint(bestBit, (Vector<Vector2>) bound1.clone());
            if (startPoint == null) {
                try {
                    throw new AI_Exception("AIE AIE AIE");
                } catch (AI_Exception e) {
                }
            }
            //System.out.println("on a le startPoint : "+startPoint);
            associatedPoints.clear();
            associatedPoints = AI_Tool.dataPrep.getBitAssociatedPoints(startPoint, (Vector<Vector2>) bound1.clone());
            //associatedPoints = getAssociatedPoints(bound1,startPoint);
            /*AI_Tool.dataPrep.A = startPoint;
            isAfterStartPoint = false;
            bound = (Vector<Vector2>) bound1.clone();
            for (int j = 1; j < bound.size(); j++) {
                Vector2 point = bound.get(j);
                if (Vector2.dist(startPoint, point) <= CraftConfig.bitLength) {
                    AI_Tool.dataPrep.pointsContenus.add(point);//debugonly
                }
                if (new Segment2D(point,bound.get(j-1)).contains(startPoint.x,startPoint.y) || point.asGoodAsEqual(startPoint))
                    isAfterStartPoint = true;
                if (isAfterStartPoint && Vector2.dist(startPoint, point) <= CraftConfig.bitLength) {
                    System.out.println("on y est");
                    associatedPoints.add(point);
                }
            }

             */
        }
    }

    private Vector<Vector2> getAssociatedPoints(Vector<Vector2> bound1, Vector2 startPoint) {
        Vector<Vector2> associatedPoints = new Vector<>();
        boolean isAfterStartPoint = false;
        Vector<Vector2> bound = (Vector<Vector2>) bound1.clone();
        for (Vector2 point : bound) {
            if (point.equals(startPoint))
                isAfterStartPoint = true;
            if (isAfterStartPoint && Vector2.dist(startPoint, point) <= CraftConfig.bitLength) {
                associatedPoints.add(point);
            }
        }
        return associatedPoints;
    }

    private boolean hasCompletedTheBound(Vector2 startPoint, Vector<Vector2> associatedPoints) {//todo déboguer hasCompletedTheBound
        if (associatedPoints.firstElement() == startPoint)
            return false;
        return associatedPoints.contains(startPoint);
    }

    public Vector<Bit2D> getSolutions() {
        return solutions;
    }
}