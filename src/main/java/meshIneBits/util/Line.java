package meshIneBits.util;

public class Line  {
   private Vector2 point1;
    private Vector2 point2;
    //y=ax+b
    public Line(Vector2 point1,Vector2 point2){
        this.point1=point1;
        this.point2=point2;
    }

    public Vector2 getPoint1(){
        return point1;
    }
    public Vector2 getPoint2(){
        return point2;
    }

        public static Vector2 getIntersection(Line lineA, Line lineB)
        {
            final double x1 = lineA.getPoint1().x;
            final double y1 = lineA.getPoint1().y;
            final double x2 = lineA.getPoint2().x;
            final double y2 = lineA.getPoint2().y;

            final double x3 = lineB.getPoint1().x;
            final double y3 = lineB.getPoint1().y;
            final double x4 = lineB.getPoint2().x;
            final double y4 = lineB.getPoint2().y;

            final double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

            if (d != 0)
            {
                final double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
                final double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

                return new Vector2(xi, yi);
            }
            return null;
        }


}
