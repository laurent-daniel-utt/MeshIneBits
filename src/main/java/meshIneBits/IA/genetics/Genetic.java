package meshIneBits.IA.genetics;

import meshIneBits.Bit2D;
import meshIneBits.IA.GeneralTools;
import meshIneBits.IA.AI_Tool;
import meshIneBits.IA.deeplearning.DataPreparation;
import meshIneBits.IA.DebugTools;
import meshIneBits.Layer;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

public class Genetic {
    private final Layer layer;
    public Evolution currentEvolution;
    private final Vector<Bit2D> solutions = new Vector<>();

    /**
     * The tool for genetic algorithms which performs the pavement on a layer.
     *
     * @param layer the layer to pave.
     */
    public Genetic(Layer layer, double genNumber, double popSize, double lengthPenalty) {
        System.out.println("On Pave le Layer : " + layer.getLayerNumber());
        this.layer = layer;
        try {
            this.start((int)genNumber, (int)popSize, (int)lengthPenalty);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the Genetic pavement and paves the given layer.
     */
    private void start(int genNumber, int popSize, int lengthPenalty) throws Exception {
        Slice sliceToTest = layer.getHorizontalSection();

        Vector<Vector<Vector2>> boundsToCheckAssociated = DataPreparation.getBoundsAndRearrange(sliceToTest); //debugOnly on fait ici que la premiere slice
        Vector<Vector2> bound1 = boundsToCheckAssociated.get(0);//debugOnly on fait ici que le premier contour
        Vector2 startPoint = bound1.get(0);
        Vector2 veryFirstStartPoint = startPoint;
        Vector<Vector2> associatedPoints = DataPreparation.getSectionPointsFromBound(bound1, startPoint);//getAssociatedPoints(bound1, startPoint);

        Bit2D bestBit;
        //todo @Etienne pave each bound
        int nbIterations = 0;
        while (!hasCompletedTheBound(veryFirstStartPoint, associatedPoints)) { //Add each bit on the bound

            nbIterations++;
            if (nbIterations > 50)//number max of bits to place before stopping
                break; //todo @Andre@Etienne enlever je pense ou trouver un nombre correct
            System.out.println("bit n°"+nbIterations);
            currentEvolution = new Evolution(associatedPoints, startPoint, bound1, genNumber, popSize, lengthPenalty);
            currentEvolution.run();
            bestBit = currentEvolution.bestSolution.getBit();
            solutions.add(bestBit);

            associatedPoints = DataPreparation.getSectionPointsFromBound(bound1, startPoint);
            startPoint = DataPreparation.getNextBitStartPoint(bestBit, bound1);
            for (double score : currentEvolution.scores) {
                System.out.print(score+" : ");
            }
            System.out.println();

            DebugTools.pointsADessinerRouges.add(startPoint);
            System.out.println("le start point est : " + startPoint.toString());
            System.out.println();
        }
    }

    private void start1 (int genNumber, int popSize, int lengthPenalty) throws Exception {


        Vector<Vector2> boundList = DataPreparation.getBoundsAndRearrange(AI_Tool.getMeshController().getCurrentLayer().getHorizontalSection()).get(0);


        Slice sliceToTest = layer.getHorizontalSection();

        Vector<Vector<Vector2>> boundsToCheckAssociated = DataPreparation.getBoundsAndRearrange(sliceToTest); //debugOnly on fait ici que la premiere slice

        Vector2 startPoint = boundList.get(0);

        Vector<Vector2> associatedPoints = DataPreparation.getSectionPointsFromBound(boundList, startPoint);//getAssociatedPoints(bound1, startPoint);

        Bit2D bestBit;

        System.out.println("placement du but");
        currentEvolution = new Evolution(associatedPoints, startPoint, boundList, genNumber, popSize, lengthPenalty);
        currentEvolution.run();
        bestBit = currentEvolution.bestSolution.getBit();
        solutions.add(bestBit);

        startPoint = DataPreparation.getNextBitStartPoint(bestBit, boundList);

        DebugTools.pointsADessinerRouges.add(startPoint);
        System.out.println("le start point est : " + startPoint.toString());
        System.out.println();


        Vector<Segment2D> bitSidesSegments = GeneralTools.getBitSidesSegments(bestBit);
        for (Segment2D segment : bitSidesSegments) {
            DebugTools.pointsADessinerRouges.add(segment.start);
            System.out.println("longueur segment = " + segment.getLength());
        }


        File fichier =  new File("bitSavedDebug.ser") ;
        // ouverture d'un flux sur un fichier
        ObjectOutputStream oos =  new ObjectOutputStream(new FileOutputStream(fichier)) ;
        // sérialization de l'objet
        oos.writeObject(bestBit) ;
        oos.close();


    }


    /**
     * Check if the bound of the Slice has been entirely paved.
     *
     * @param startPoint       the point of the bound on which the very first bit was placed.
     * @param associatedPoints the points on which a bit has just been placed.
     * @return <code>true</code> if the bound of the Slice has been entirely paved. <code>false</code> otherwise.
     */
    private boolean hasCompletedTheBound(Vector2 startPoint, Vector<Vector2> associatedPoints) {
        //todo @Etienne debug hasCompletedTheBound
        if (associatedPoints.firstElement() == startPoint)
            return false;
        return associatedPoints.contains(startPoint);
    }

    /**
     * @return the best solutions
     */
    public Vector<Bit2D> getSolutions() {
        return solutions;
    }
}