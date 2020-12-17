package meshIneBits.IA.IA_util;

import meshIneBits.IA.DataPreparation;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Vector2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * generates a .csv dataset
 * format of a line : edge abscissa, bit's angle, point 0 x, point 0 y, point 1 x, point 1 y,...
 */
public final class DataSetGenerator { // todo il y a sans doute des méthodes de DataPreparation qui auraient leur place ds cette classe
    final static String dataSetFilePath = "dataSet.csv";

    /**
     * generates a .csv file that can be used by a neural network
     *
     * @return todo @andre, donner le cas -1 aussi
     */
    public static int generateCsvFile() {

        long dataLogSize;
        try {
            dataLogSize = DataLog.getNumberOfEntries();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }


        for (int logLine = 1; logLine <= dataLogSize; logLine++) {

            DataLogEntry entry = DataLog.getEntryFromFile(logLine); //todo : gérer le cas ou ça return null

            // format data for dl
            Vector2 startPoint = entry.getPoints().firstElement();
            Vector<Vector2> transformedPoints = DataPreparation.getSectionInLocalCoordinateSystem(entry.getPoints());
            Vector<Vector2> pointsForDl = DataPreparation.getInputPointsForDL(transformedPoints);
            double edgeAbscissa = getBitEdgeAbscissa(entry.getBitPosition(), entry.getBitOrientation(), startPoint);
            double orientation = entry.getBitOrientation().getEquivalentAngle2();

            //generates one line (corresponds to the data of one bit)
            String csvLine = ""
                    + edgeAbscissa // adds position
                    + ","
                    + orientation; //adds orientation
            for (Vector2 point : pointsForDl) { // add points
                csvLine += "," + point.x;
                csvLine += "," + point.y;
            }

            try {
                FileWriter fw = new FileWriter(dataSetFilePath, true);
                fw.write(csvLine + "\n");
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }


    /**
     * This method convert a bit position (x, y) to another expression of position (called edgeAbscissa) related to
     * a startPoint and an abscissa related to one bit's vertex. This is helpful for automatic placement on
     * slice's borders,because it guarantees that the bit covers the startPoint, as bit's position is related to it.
     *
     * @param bitPos     usual (x, y) coordinates of bit's center
     * @param bitAngle   bit's angle as a vector
     * @param startPoint the startPoint on which the new bit's end should be placed : the intersection between the
     *                   slice border and the last placed bit's end edge
     * @return the edgeAbscissa
     */
    public static double getBitEdgeAbscissa(Vector2 bitPos, Vector2 bitAngle, Vector2 startPoint) {
        //todo repasser privé et non static
        // bit's colinear and orthogonal unit vectors computation
        Vector2 colinear = bitAngle.normal();
        Vector2 orthogonal = colinear.rotate(new Vector2(0, 1)); // 90deg anticlockwise rotation

        //this point is used as a local origin for the new coordinate system
        Vector2 originVertex = bitPos.add(orthogonal.mul(CraftConfig.bitWidth / 2))
                .sub(colinear.mul(CraftConfig.bitLength / 2));

        // edgeAbscissa is the distance between the startPoint and the originVertex
        // the startPoint on which the new bit should be placed : the intersection between the slice border
        // and the last placed bit's end edge
        // todo: this suppose that the bit's end edge is already placed on the start point. How can we be sure of that ?
        double edgeAbscissa = Vector2.dist(originVertex, startPoint);

        return edgeAbscissa;
    }

    public static void main(String[] args) {
        DataSetGenerator.generateCsvFile();
    }


}