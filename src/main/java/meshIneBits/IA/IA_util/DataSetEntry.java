package meshIneBits.IA.IA_util;

import meshIneBits.Bit2D;
import meshIneBits.util.Vector2;

import java.util.Vector;

public class DataSetEntry {
    private Vector2 bitPosition;
    private Vector2 bitOrientation;
    private Vector<Vector2> points;


    public DataSetEntry(Vector2 bitPosition, Vector2 bitOrientation, Vector<Vector2> points) {
        this.bitPosition = bitPosition;
        this.bitOrientation = bitOrientation;
        this.points = points;

    }

    public DataSetEntry(Bit2D bit2D, Vector<Vector2> points) {
        this.bitPosition = bit2D.getOrigin();
        this.bitOrientation = bit2D.getOrientation();
        this.points = points;
    }

    public Vector<Vector2> getPoints() {
        return points;
    }

    public Vector2 getBitPosition() {
        return bitPosition;
    }

    public Vector2 getBitOrientation() {
        return bitOrientation;
    }

    @Override
    public String toString() {
        return "DataSetEntry{" +
                "bitPosition=" + bitPosition.toString() +
                ", bitOrientation=" + bitOrientation.toString() +
                ", points=" + points +
                '}';
    }
}
