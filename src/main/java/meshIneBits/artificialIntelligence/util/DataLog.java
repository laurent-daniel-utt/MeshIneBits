package meshIneBits.artificialIntelligence.util;

import meshIneBits.util.Vector2;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Vector;
import java.util.stream.Stream;

public final class DataLog {
    private final static String dataLogFilePath = "storedBits.csv";

    static public long getNumberOfEntries() throws IOException {
        return Files.lines(Paths.get(dataLogFilePath)).count();
    }


    static public void saveEntry(DataLogEntry dataLogEntry) throws IOException {
        StringBuilder line = new StringBuilder(dataLogEntry.getBitPosition().x + ","
                + dataLogEntry.getBitPosition().y + ","
                + dataLogEntry.getBitOrientation().x + ","
                + dataLogEntry.getBitOrientation().y);

        for (Vector2 point : dataLogEntry.getPoints()) {
            line.append(",").append(point.x).append(",").append(point.y);
        }
        FileWriter fw = new FileWriter(dataLogFilePath, true);
        fw.write(line + "\n");
        fw.close();
    }



    @SuppressWarnings("unused")
    static public void saveAllEntries(Vector<DataLogEntry> dataSetEntries) throws IOException {
        for (DataLogEntry dataLogEntry : dataSetEntries) {
            saveEntry(dataLogEntry);
        }
    }


    static public DataLogEntry getEntryFromFile(long lineNumber) throws IOException {
        Stream<String> lines = Files.lines(Paths.get(dataLogFilePath));
        String line = lines.skip(lineNumber - 1).findFirst().get(); //todo @Andre check with .isPresent() before (cf inspection) --> faire un if .isPresent()
        return decodeLine(line);
    }


    static private DataLogEntry decodeLine(String line) {

        String[] dataStr = line.split(",");
        Vector<String> data = new Vector<>();
        Collections.addAll(data, dataStr);

        double bitPosX = Double.parseDouble(data.remove(0));
        double bitPosY = Double.parseDouble(data.remove(0));
        Vector2 bitPos = new Vector2(bitPosX, bitPosY);

        double bitOrientationX = Double.parseDouble(data.remove(0));
        double bitOrientationY = Double.parseDouble(data.remove(0));
        Vector2 bitOrientation = new Vector2(bitOrientationX, bitOrientationY);

        Vector<Vector2> points = new Vector<>();
        while (! data.isEmpty()) {
            points.add(new Vector2(
                    Double.parseDouble(data.remove(0)),
                    Double.parseDouble(data.remove(0))));
        }

        return new DataLogEntry(bitPos, bitOrientation, points);
    }

}
