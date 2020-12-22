package meshIneBits.IA;

import meshIneBits.Bit2D;
import meshIneBits.util.Polygon;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Vector;

public final class DebugTools {

    public static Vector2 A = new Vector2(0, 0);
    public static Vector2 B = new Vector2(0, 0);
    public static Vector2 C = new Vector2(0, 0);
    public static Vector2 D = new Vector2(0, 0);
    public static AffineTransform transformArea = new AffineTransform();

    public static Vector<Vector2> pointsContenus = new Vector<>();
    public static Vector<Vector2> pointsADessiner = new Vector<>();
    public static Path2D cutPathToDraw = new Path2D.Double();

    public static Segment2D currentSegToDraw = new Segment2D(A, B);
    public static Segment2D currentSegToDraw2 = new Segment2D(A, B);

    public static Polygon poly = new Polygon();

    public static Area area = new Area();
    public static Area areaToDraw = null;

    public static Vector<Bit2D> Bits = new Vector<>();
    public static Vector<String> scores = new Vector<>();
    public static boolean hasNewBitToDraw;
}
