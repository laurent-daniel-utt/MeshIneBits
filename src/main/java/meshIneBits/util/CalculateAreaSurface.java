package meshIneBits.util;

import static java.lang.Double.NaN;

import java.awt.geom.*;

public abstract class CalculateAreaSurface {
    public static double approxArea(Area area, double flatness, int limit) {
        PathIterator i =
                new FlatteningPathIterator(area.getPathIterator(identity),
                        flatness,
                        limit);
        return approxArea(i);
    }

    public static double approxArea(Area area, double flatness) {
        PathIterator i = area.getPathIterator(identity, flatness);
        return approxArea(i);
    }

    public static void main(String[] args) {
        Area area1 = new Area(new Rectangle2D.Double(0,0,10,10));
        Area area2 = new Area(new Rectangle2D.Double(1,1,5,5));
        area1.intersect(area2);
        System.out.println(approxArea(area1,0));
    }

    public static double approxArea(PathIterator i) {
        double a = 0.0;
        double[] coords = new double[6];
        double startX = NaN, startY = NaN;
        Line2D segment = new Line2D.Double(NaN, NaN, NaN, NaN);
        while (! i.isDone()) {
            int segType = i.currentSegment(coords);
            double x = coords[0], y = coords[1];
            switch (segType) {
                case PathIterator.SEG_CLOSE:
                    segment.setLine(segment.getX2(), segment.getY2(), startX, startY);
                    a += hexArea(segment);
                    startX = startY = NaN;
                    segment.setLine(NaN, NaN, NaN, NaN);
                    break;
                case PathIterator.SEG_LINETO:
                    segment.setLine(segment.getX2(), segment.getY2(), x, y);
                    a += hexArea(segment);
                    break;
                case PathIterator.SEG_MOVETO:
                    startX = x;
                    startY = y;
                    segment.setLine(NaN, NaN, x, y);
                    break;
                default:
                    throw new IllegalArgumentException("PathIterator contains curved segments");
            }
            i.next();
        }
        if (Double.isNaN(a)) {
            throw new IllegalArgumentException("PathIterator contains an open path");
        } else {
            return 0.5 * Math.abs(a);
        }
    }

    private static double hexArea(Line2D seg) {
        return seg.getX1() * seg.getY2() - seg.getX2() * seg.getY1();
    }

    private static final AffineTransform identity =
            AffineTransform.getQuadrantRotateInstance(0);
}
