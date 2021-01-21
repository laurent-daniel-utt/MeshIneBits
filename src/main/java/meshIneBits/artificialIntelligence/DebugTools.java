package meshIneBits.artificialIntelligence;

import meshIneBits.Bit2D;
import meshIneBits.util.Polygon;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Vector;

/**
 * DebugTools is useful for debugging.
 * Was used with Genetics and NN Pavement.
 * <p>
 * Set its variables depending on what you want to draw, and call DebugTools.setPaintForDebug(true);
 * It will draw on the screen what is stored in the variables of DebugTools.
 * You may have to modifiy the method AIpaintForDebug in MeshWindowsCore to paint exactly what you need.
 *
 * @see meshIneBits.gui.view2d.MeshWindowCore#AIpaintForDebug
 */
@SuppressWarnings("unused")
public final class DebugTools {

    public static @NotNull AffineTransform transformArea = new AffineTransform();

    public static @NotNull Vector<Vector2> pointsToDrawRED = new Vector<>();
    public static @NotNull Vector<Vector2> pointsToDrawGREEN = new Vector<>();
    public static @NotNull Vector<Vector2> pointsToDrawBLUE = new Vector<>();
    public static @NotNull Vector<Vector2> pointsToDrawORANGE = new Vector<>();

    public static @NotNull Path2D cutPathToDraw = new Path2D.Double();

    public static @NotNull Vector<Segment2D> segmentsToDraw = new Vector<>();
    public static @NotNull Segment2D currentSegToDraw = new Segment2D(new Vector2(0, 0), new Vector2(0, 0));
    public static @NotNull Segment2D currentSegToDraw2 = new Segment2D(new Vector2(0, 0), new Vector2(0, 0));

    public static @NotNull Polygon poly = new Polygon();

    public static @NotNull Area area = new Area();
    public static @Nullable Area areaToDraw = null;

    public static @NotNull Vector<Bit2D> Bits = new Vector<>();

    /**
     * Authorize (or not) to paint on the screen what is stored in the variables of DebugTools.
     */
    public static void setPaintForDebug(boolean b) {
        AI_Tool.getMeshController().AI_NeedPaint = b;
    }
}
