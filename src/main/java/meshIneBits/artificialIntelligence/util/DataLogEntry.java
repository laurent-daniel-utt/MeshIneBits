package meshIneBits.artificialIntelligence.util;

import meshIneBits.Bit2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.util.Vector;

/**
 * A DataLogEntry contains the data read or the data to write in the dataSet file.
 * It contains the position and orientation of a bit, and the section of its associated points.
 */
public class DataLogEntry {
    private final Vector2 bitPosition;
    private final Vector2 bitOrientation;
    private final Vector<Vector2> associatedPoints;

    /**
     * A DataLogEntry contains the data read or the data to write in the dataSet file.
     * It contains the position and orientation of a bit, and the section of its associated points.
     *
     * @param bitPosition      the position of the bit.
     * @param bitOrientation   the orientation of the bit.
     * @param associatedPoints the points associated to the bit.
     */
    public DataLogEntry(Vector2 bitPosition, Vector2 bitOrientation, Vector<Vector2> associatedPoints) {
        this.bitPosition = bitPosition;
        this.bitOrientation = bitOrientation;
        this.associatedPoints = associatedPoints;

    }

    /**
     * A DataLogEntry contains the data read or the data to write in the dataSet file.
     * It contains the position and orientation of a bit, and the section of its associated points.
     *
     * @param bit2D            the bit.
     * @param associatedPoints the points associated to the bit.
     */
    public DataLogEntry(@NotNull Bit2D bit2D, Vector<Vector2> associatedPoints) {
        this.bitPosition = bit2D.getOrigin();
        this.bitOrientation = bit2D.getOrientation();
        this.associatedPoints = associatedPoints;
    }

    public Vector<Vector2> getAssociatedPoints() {
        return associatedPoints;
    }

    public Vector2 getBitPosition() {
        return bitPosition;
    }

    public Vector2 getBitOrientation() {
        return bitOrientation;
    }

    @Override
    public @NotNull String toString() {
        return "DataLogEntry{" +
                "bitPosition=" + bitPosition.toString() +
                ", bitOrientation=" + bitOrientation.toString() +
                ", points=" + associatedPoints +
                '}';
    }
}
