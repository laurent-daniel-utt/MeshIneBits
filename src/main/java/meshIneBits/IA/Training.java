package meshIneBits.IA;

import meshIneBits.IA.IA_util.DataLog;
import meshIneBits.IA.IA_util.DataLogEntry;
import meshIneBits.util.Vector2;

import java.util.Vector;

public class Training {

    /**
     * Train the neural network with the data from the txt file.
     */
    public void trainNN() {
        Vector<DataLogEntry> dataSetEntries = DataLog.getAllEntriesFromFile();

        for (DataLogEntry entry : dataSetEntries) {
            Vector<Vector2> pointsInLocalCS = AI_Tool.dataPrep.getSectionInLocalCoordinateSystem(entry.getPoints());
            Vector2 position = entry.getBitPosition();
            Vector2 orientation = entry.getBitOrientation();

            //todo train NN with data
        }
    }

}
