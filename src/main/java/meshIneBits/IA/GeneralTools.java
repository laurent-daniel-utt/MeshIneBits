package meshIneBits.IA;

import meshIneBits.Bit2D;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.util.Vector;

public final class GeneralTools {

    public static Vector2 getIntersectionPoint(Segment2D AB, Segment2D CD) {
        // points
        Vector2 A = AB.start;
        Vector2 B = AB.end;
        Vector2 C = CD.start;
        Vector2 D = CD.end;

        // particular cases
        if (A.asGoodAsEqual(C) || A.asGoodAsEqual(D)) {
            return A;
        }
        if (B.asGoodAsEqual(C) || B.asGoodAsEqual(D)) {
            return B;
        }

        if (isPointOnSegment(A, CD)){
            return A;
        }
        if (isPointOnSegment(B, CD)){
            return B;
        }
        if (isPointOnSegment(C, AB)){
            return C;
        }
        if (isPointOnSegment(D, AB)){
            return D;
        }

        if (doSegmentsIntersect(AB, CD)) {

            double AD = Vector2.dist(A, D);
            double AID = 180 - GeneralTools.getAngle(D, A, B) - GeneralTools.getAngle(C, D, A);

            double IA = (AD / Math.sin(Math.toRadians(AID))) * Math.sin(Math.toRadians(GeneralTools.getAngle(A, D, C)));

            return A.add(B.sub(A).normal().mul(IA));
        }
        return null;
    }


    /**
     * Checks if two segments intersects
     * @param AB  the first segment
     * @param CD  the second segment
     * @return  true if the two segments intersects. false otherwise
     */
    public static boolean doSegmentsIntersect(Segment2D AB, Segment2D CD) {

        // points
        Vector2 A = AB.start;
        Vector2 B = AB.end;
        Vector2 C = CD.start;
        Vector2 D = CD.end;

        // evident situations
        if (A.asGoodAsEqual(C) || A.asGoodAsEqual(D)) {
            return true;
        }
        if (B.asGoodAsEqual(C) || B.asGoodAsEqual(D)) {
            return true;
        }
        if (isPointOnSegment(A, CD)){
            return true;
        }
        if (isPointOnSegment(B, CD)){
            return true;
        }
        if (isPointOnSegment(C, AB)){
            return true;
        }
        if (isPointOnSegment(D, AB)){
            return true;
        }

        double DAC = GeneralTools.getAngle(D, A, C);
        double ACB = GeneralTools.getAngle(A, C, B);
        double CBD = GeneralTools.getAngle(C, B, D);
        double BDA = GeneralTools.getAngle(B, D, A);


        double sum = Math.abs(DAC) + Math.abs(ACB) + Math.abs(CBD) + Math.abs(BDA);

        // if sum is not 2pi, then ABCD is a complex quadrilateral (2 edges cross themselves).
        // This means that segments intersect
        double errorThreshold = 0.1;
        return Math.abs(360 - sum) < errorThreshold;
    }



    public static double getAngle(Vector2 A, Vector2 B, Vector2 C) {
        double angle = Math.acos((Vector2.dist2(B, A) + Vector2.dist2(B, C) - Vector2.dist2(A, C))
                / (2 * (Vector2.dist(B, A) * Vector2.dist(B, C))));
        return Math.toDegrees(angle);
    }


    /**
     * similar to isOnSegment() of Vector2, but more reliable
     *
     * @param v a point
     * @param s a segment
     * @return true if the point is on the segment
     */
    public static boolean isPointOnSegment(Vector2 v, Segment2D s){
        double errorAccepted = Math.pow(10, -5);

        return Math.abs(Vector2.dist(s.start, v) + Vector2.dist(s.end, v) - s.getLength()) < errorAccepted;
    }


    /**
     * Find an approximation of the intersection point between a segment and a circle
     * If there is more than one intersection, this method will return the point that is
     * the closest to the end of the segment.
     * Initial condition : an intersection should exist
     *
     * @param center center of the circle
     * @param radius radius of the circle
     * @param seg    segment
     * @return an approximation of the intersection point between the segment and the circle
     */
    public static Vector2 circleAndSegmentIntersection(Vector2 center, double radius, Segment2D seg) {

        double step = 0.01;

        double t = 1;
        double x = seg.end.x;
        double y = seg.end.y;
        double dist = Vector2.dist(center, new Vector2(x, y));

        while (dist > radius) {
            t = t - step;
            x = seg.start.x + t * (seg.end.x - seg.start.x);
            y = seg.start.y + t * (seg.end.y - seg.start.y);
            dist = Vector2.dist(center, new Vector2(x, y));
        }

        return new Vector2(x, y);
    }


    /**
     * Returns the four segments of a Bit2D (a Bit2D not cut by cut paths)
     *
     * @param bit the Bit2D
     * @return a Vector of the four segments.
     */ // l'originale //todo @Andre supprimer? comme t'en as créé une autre
    @SuppressWarnings("unused")
    public static Vector<Segment2D> getBitSidesSegments1(Bit2D bit) {

        Vector<Segment2D> sides = new Vector<>();
        //generates the 4 points which makes the rectangular bit
        Vector2 bitOrigin = bit.getOrigin();
        Vector2 A = new Vector2(
                bit.getLength() / 2,
                bit.getWidth() / 2)
                .rotate(bit.getOrientation())
                .add(new Vector2(
                        bitOrigin.x,
                        bitOrigin.y
                ));
        Vector2 B = new Vector2(
                bit.getLength() / 2,
                -bit.getWidth() / 2)
                .rotate(bit.getOrientation())
                .add(new Vector2(
                        bitOrigin.x,
                        bitOrigin.y
                ));

        Vector2 C = new Vector2(
                -bit.getLength() / 2,
                -bit.getWidth() / 2)
                .rotate(bit.getOrientation())
                .add(new Vector2(
                        bitOrigin.x,
                        bitOrigin.y
                ));

        Vector2 D = new Vector2(
                -bit.getLength() / 2,
                bit.getWidth() / 2)
                .rotate(bit.getOrientation())
                .add(new Vector2(
                        bitOrigin.x,
                        bitOrigin.y
                ));

        sides.add(new Segment2D(A, B));
        sides.add(new Segment2D(B, C));
        sides.add(new Segment2D(C, D));
        sides.add(new Segment2D(D, A));

        return sides;
    }

    // l'originale debuguée
    public static Vector<Segment2D> getBitSidesSegments(Bit2D bit) {

        Vector<Segment2D> sides = new Vector<>();
        //generates the 4 points which makes the rectangular bit
        Vector2 bitOrigin = bit.getOrigin();
        Vector2 A = new Vector2(
                bit.getLength() / 2,
                bit.getWidth() / 2)
                .rotate(bit.getOrientation().normal())
                .add(new Vector2(
                        bitOrigin.x,
                        bitOrigin.y
                ));


        Vector2 B = new Vector2(
                bit.getLength() / 2,
                -bit.getWidth() / 2)
                .rotate(bit.getOrientation().normal())
                .add(new Vector2(
                        bitOrigin.x,
                        bitOrigin.y
                ));

        Vector2 C = new Vector2(
                -bit.getLength() / 2,
                -bit.getWidth() / 2)
                .rotate(bit.getOrientation().normal())
                .add(new Vector2(
                        bitOrigin.x,
                        bitOrigin.y
                ));

        Vector2 D = new Vector2(
                -bit.getLength() / 2,
                bit.getWidth() / 2)
                .rotate(bit.getOrientation().normal())
                .add(new Vector2(
                        bitOrigin.x,
                        bitOrigin.y
                ));

        sides.add(new Segment2D(A, B));
        sides.add(new Segment2D(B, C));
        sides.add(new Segment2D(C, D));
        sides.add(new Segment2D(D, A));

        return sides;
    }


    // celle que j'ai créée
    //todo @Andre beh du coup c'est celle là ou celle d'avant à utiliser ??
    @SuppressWarnings("unused")
    public static Vector<Segment2D> getBitSidesSegmentsNouvelle(Bit2D bit) {

        // bit's colinear and orthogonal unit vectors computation
        Vector2 colinear = bit.getOrientation().normal();
        Vector2 orthogonal = colinear.rotate(new Vector2(0, -1).normal()); // 90deg anticlockwise rotation

        Vector2 A = bit.getOrigin()
                .add(colinear.mul(CraftConfig.bitLength/2))
                .add(orthogonal.mul(CraftConfig.bitWidth/2));

        Vector2 B = bit.getOrigin()
                .sub(colinear.mul(CraftConfig.bitLength/2))
                .add(orthogonal.mul(CraftConfig.bitWidth/2));

        Vector2 C = bit.getOrigin()
                .sub(colinear.mul(CraftConfig.bitLength/2))
                .sub(orthogonal.mul(CraftConfig.bitWidth/2));

        Vector2 D = bit.getOrigin()
                .add(colinear.mul(CraftConfig.bitLength/2))
                .sub(orthogonal.mul(CraftConfig.bitWidth/2));

        Vector<Segment2D> sides = new Vector<>();

        sides.add(new Segment2D(A, B));
        sides.add(new Segment2D(B, C));
        sides.add(new Segment2D(C, D));
        sides.add(new Segment2D(D, A));

        return sides;
    }




    @NotNull
    public static Vector<Segment2D> getSegment2DS(Vector<Vector2> sectionPoints) {
        Vector<Segment2D> sectionSegments = new Vector<>();
        for (int i = 0; i < sectionPoints.size() - 1; i++) {
            sectionSegments.add(new Segment2D(
                    sectionPoints.get(i),
                    sectionPoints.get(i + 1)
            ));
        }
        return sectionSegments;
    }

}
