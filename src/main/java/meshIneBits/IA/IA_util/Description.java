package meshIneBits.IA.IA_util;

import meshIneBits.util.Vector2;

import java.util.Vector;

public class Description {
    Vector<Vector2> points;
    Vector<Double> coeffs;

    /**
     * Represent the description of the part of the slice concerned by a Bit2D
     * @param points the points of the Slice contained in a Bit3D
     * @param coeffs the coeffs from the model (which fits the points)
     */
    public Description(Vector<Vector2> points, Vector<Double> coeffs) {
        this.points = points;
        this.coeffs = coeffs;
    }
}
