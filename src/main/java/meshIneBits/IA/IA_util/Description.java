package meshIneBits.IA.IA_util;

import meshIneBits.Bit2D;
import meshIneBits.util.Vector2;

import java.util.Vector;

public class Description {
    Vector<Vector2> points;
    Vector<Double> coeffs;

    /**
     * Represent the description of the part of the slice concerned by a Bit2D
     *
     * @param points the points of the Slice contained in a Bit3D
     * @param coeffs the coeffs from the model (which fits the points)
     */
    public Description(Vector<Vector2> points, Vector<Double> coeffs) {
        this.points = points;
        this.coeffs = coeffs;
    }

    /**
     * Returns the Description of a Bit2D.
     * The Description is composed by the points of the section associated with the bit, and the coefficients that fit the section.
     *
     * @param bit the Bit2D.
     * @return the Description of the Bit2D.
     */
    public static Description getDescription(Bit2D bit) {
        //todo get la liste des points concernés
        //todo get la liste des coefficients du modèle des points concernés
        Vector<Vector2> points = new Vector<>();
        Vector<Double> coeffs = new Vector<>();
        Description description = new Description(points, coeffs);
        return description;
    }
}
