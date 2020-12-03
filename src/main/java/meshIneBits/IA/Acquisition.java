package meshIneBits.IA;

import meshIneBits.Bit2D;
import meshIneBits.IA.IA_util.AI_Exception;
import meshIneBits.IA.IA_util.Description;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class Acquisition {
    public boolean storeNewBits = false;
    private Map<Bit2D, Description> storedExamplesBits;
    private AI_Tool ai_tool;
    public Vector2 startPoint = new Vector2(0, 0);


    public Acquisition(AI_Tool ai_tool) {
        this.ai_tool = ai_tool;
    }

    public void startStoringBits() {
        this.storeNewBits = true;
        this.storedExamplesBits = new LinkedHashMap<>();
    }

    public void stopStoringBits() {
        this.storeNewBits = false;
        saveExamples();
    }

    private void saveExamples() {
        try {
            /*
            On enregistre dans notre fichier sous la forme :
            positionX , positionY , orientationX, orientationY //todo enregistrer aussi les données de la courbe
             */

            String filename = "storedBits.txt"; //todo enregistrer le fichier dans le package IA plutot
            FileWriter fw = new FileWriter(filename, true);
            for (Bit2D bit : this.storedExamplesBits.keySet()) {
                String text = "";
                text += bit.getOrigin().x + ","
                        + bit.getOrigin().y + ","
                        + bit.getOrientation().x + ","
                        + bit.getOrientation().y;
                fw.write(text + "\n");
            }
            fw.close();
        } catch (IOException ioe) {
            System.err.println("IOException:" + ioe.getMessage());
        }
    }

    public void storeNewExampleBit(Bit2D bit) throws AI_Exception {
        this.storedExamplesBits.put(bit, getDescription(bit));
        Vector<Vector2> pointList = new Vector<>();//debugOnly, on teste si la recherche du point suivant marche bien
        Vector<Slice> slicesList = ai_tool.getMeshController().getMesh().getSlices();
        Vector<Segment2D> segment2DVector = slicesList.get(0).getSegmentList();
        for (Segment2D seg : segment2DVector) {
            pointList.add(new Vector2(seg.end.x, seg.end.y));
        }
        pointList = ai_tool.dataPrep.getContours(ai_tool.getSliceMap(), ai_tool.getMeshController().getCurrentLayer().getHorizontalSection()).get(0);
        //todo modifier : ici on fait que du .get(0)

        startPoint = ai_tool.dataPrep.getNextBitStartPoint(bit, pointList);
    }

    private Description getDescription(Bit2D bit) {
        //todo get la liste des points concernés
        //todo get la liste des coefficients du modèle des points concernés
        Vector<Vector2> points = new Vector<>();
        Vector<Double> coeffs = new Vector<>();
        Description description = new Description(points, coeffs);
        return description;
    }
}
