package meshIneBits.IA;

import meshIneBits.IA.IA_util.DataSet;
import meshIneBits.IA.IA_util.DataSetEntry;
import meshIneBits.util.Vector2;

import java.util.Vector;

public class Training {
    private AI_Tool ai_tool;

    public Training(AI_Tool ai_tool) {
        this.ai_tool = ai_tool;
    }

    /**
     * Train the neural network with the data from the txt file.
     */
    public void trainNN() {
        DataSet dataset = ai_tool.dataSet;
        Vector<DataSetEntry> dataSetEntries = dataset.getAllEntriesFromFile();

        for (DataSetEntry entry : dataSetEntries) {
            Vector<Vector2> pointsInLocalCS = ai_tool.dataPrep.getSectionInLocalCoordinateSystem(entry.getPoints());
            Vector2 position = entry.getBitPosition();
            Vector2 orientation = entry.getBitOrientation();

            //todo train NN with data
        }
    }

}
