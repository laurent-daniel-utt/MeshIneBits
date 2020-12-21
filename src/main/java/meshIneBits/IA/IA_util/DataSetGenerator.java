package meshIneBits.IA.IA_util;

import meshIneBits.IA.AI_Tool;
import meshIneBits.IA.DataPreparation;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Vector2;
import org.opencv.core.Mat;

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
    public static void generateCsvFile() throws IOException {

        long dataLogSize;

        dataLogSize = DataLog.getNumberOfEntries();

        FileWriter fw = new FileWriter(dataSetFilePath, false);

        for (int logLine = 1; logLine <= dataLogSize; logLine++) {

            DataLogEntry entry = DataLog.getEntryFromFile(logLine); //todo : gérer le cas ou ça return null

            // format data for dl
            Vector2 startPoint = entry.getPoints().firstElement();

            Vector<Vector2> transformedPoints = DataPreparation.getSectionInLocalCoordinateSystem(entry.getPoints());
            Vector<Vector2> pointsForDl = DataPreparation.getInputPointsForDL(transformedPoints);

            double edgeAbscissa = getBitEdgeAbscissa(entry.getBitPosition(), entry.getBitOrientation(), startPoint);
            double bitOrientation = getBitAngleInLocalSystem(entry.getBitOrientation(), entry.getPoints());

            //generates one line (corresponds to the data of one bit)
            String csvLine = ""
                    + edgeAbscissa // adds position
                    + ","
                    + bitOrientation; //adds bitOrientation
            for (Vector2 point : pointsForDl) { // add points
                csvLine += "," + point.x;
                csvLine += "," + point.y;
            }



            fw.write(csvLine + "\n");


        }

        fw.close();
    }


    /**
     * This method convert a bit position (x, y) to another expression of position (called edgeAbscissa) related to
     * a startPoint and an abscissa related to one bit's vertex. This is helpful for automatic placement on
     * slice's borders,because it guarantees that the bit covers the startPoint, as bit's position is related to it.
     *
     * @param bitOrigin     usual (x, y) coordinates of bit's center
     * @param bitAngle   bit's angle as a vector
     * @param startPoint the startPoint on which the new bit's end should be placed : the intersection between the
     *                   slice border and the last placed bit's end edge
     * @return the edgeAbscissa
     */
    public static double getBitEdgeAbscissa(Vector2 bitOrigin, Vector2 bitAngle, Vector2 startPoint) {

        Vector2 colinear = bitAngle.normal();
        Vector2 orthogonal = colinear.rotate(new Vector2(0, -1)); // 90deg anticlockwise rotation

        //this point is used as a local origin for the new coordinate system
        Vector2 originVertex = bitOrigin.add(orthogonal.mul(CraftConfig.bitWidth / 2))
                .sub(colinear.mul(CraftConfig.bitLength / 2));


        if(Vector2.dist(originVertex, startPoint)>CraftConfig.bitWidth){
            //this case is not possible. this means that originVertex should be the opposite point compared to bitOrigin
            originVertex = bitOrigin.sub(orthogonal.mul(CraftConfig.bitWidth / 2))
                    .add(colinear.mul(CraftConfig.bitLength / 2));
        }


        // edgeAbscissa is the distance between the startPoint and the originVertex
        // the startPoint on which the new bit should be placed : the intersection between the slice border
        // and the last placed bit's end edge
        // todo: this suppose that the bit's end edge is already placed on the start point. How can we be sure of that ?
        double edgeAbscissa = Vector2.dist(originVertex, startPoint);


        return edgeAbscissa;
    }

    /**
     *
     * @param bitAngle the angle of the bit in the global coordinate system
     * @param sectionPoints the points saved by DataLog
     * @return the angle of the bit in the local coordinate system. Note that the angle is expressed
     * between -90 and 90. Otherwise, as the orientation is expressed in regards to the center of the bit,
     * the angles -100 and -10 degrees (for example) would have been equivalent.
     */
    public static double getBitAngleInLocalSystem(Vector2 bitAngle, Vector<Vector2> sectionPoints){


        Vector2 localCoordinateSystemAngle = Vector2.getEquivalentVector(DataPreparation.getLocalCoordinateSystemAngle(sectionPoints));

        System.out.println("localCoordinateSystemAngle = " + localCoordinateSystemAngle.getEquivalentAngle2());


        AI_Tool.dataPrep.pointsADessiner.add(localCoordinateSystemAngle.mul((50)));
        AI_Tool.dataPrep.pointsADessiner.add(bitAngle.mul((100)));



        // = if angle between the vector is more than 90 degrees
        // (the point of rotation is the center of the bit, so both angles are equivalent)
        if (bitAngle.dot(localCoordinateSystemAngle)<0){
            bitAngle = new Vector2(-bitAngle.x, -bitAngle.y);
        }



        double x1 = localCoordinateSystemAngle.x;
        double y1 = localCoordinateSystemAngle.y;
        double x2 = bitAngle.x;
        double y2 = bitAngle.y;
        double l1 = localCoordinateSystemAngle.vSize();
        double l2 = bitAngle.vSize();

        //double bitAngleLocal = Math.atan2(x1*y2-y1*x2,
                //x1*x2+y1*y2);



        double bitAngleLocal = Math.asin ( (x1*y2-y1*x2) / (l1*l2) );

        return Math.toDegrees(bitAngleLocal);
    }


    public static void main(String[] args) {
        DataSetGenerator dataSetGenerator = new DataSetGenerator();
        try {
            dataSetGenerator.generateCsvFile();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("pb generation dataset");
        }
    }




}
