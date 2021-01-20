package meshIneBits.artificialIntelligence;

import meshIneBits.Bit2D;
import meshIneBits.util.Polygon;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Vector;

/**
 * DebugTools is useful for debugging.
 * Was used with Genetics and NN Pavement.
 *
 * Set its variables depending on what you want to draw, and call DebugTools.setPaintForDebug(true);
 * It will draw on the screen what is stored in the variables of DebugTools.
 * You may have to modifiy the method AIpaintForDebug in MeshWindowsCore to paint exactly what you need.
 * @see meshIneBits.gui.view2d.MeshWindowCore#AIpaintForDebug
 */
@SuppressWarnings("unused")
public final class DebugTools {

    public static AffineTransform transformArea = new AffineTransform();

    public static Vector<Vector2> pointsToDrawRED = new Vector<>();
    public static Vector<Vector2> pointsToDrawGREEN = new Vector<>();
    public static Vector<Vector2> pointsToDrawBLUE = new Vector<>();
    public static Vector<Vector2> pointsToDrawORANGE = new Vector<>();

    public static Path2D cutPathToDraw = new Path2D.Double();

    public static Vector<Segment2D> segmentsToDraw = new Vector<>();
    public static Segment2D currentSegToDraw = new Segment2D(new Vector2(0,0), new Vector2(0,0));
    public static Segment2D currentSegToDraw2 = new Segment2D(new Vector2(0,0), new Vector2(0,0));

    public static Polygon poly = new Polygon();

    public static Area area = new Area();
    public static Area areaToDraw = null;

    public static Vector<Bit2D> Bits = new Vector<>();

    /**
     * Authorize (or not) to paint on the screen what is stored in the variables of DebugTools.
     */
    public static void setPaintForDebug(boolean b) {
        AI_Tool.getMeshController().AI_NeedPaint = b;
    }
}
