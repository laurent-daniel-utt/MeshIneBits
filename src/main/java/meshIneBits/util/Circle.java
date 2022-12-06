package meshIneBits.util;

public class Circle {
    private Vector2 center;
    private double radius;

    /**
     * a class representing a circle
     */
    public Circle(Vector2 center,double radius){
        this.center=new Vector2(center.x-radius,center.y-radius);
        this.radius=radius;
    }

    /**
     * @param c1 circle1
     * @param c2 circle2
     * @return distance between 2 circles
     */
    public static double CircleDistant(Circle c1,Circle c2){
      return   Vector2.dist(c1.center,c2.center);


    }

    public Vector2 getCenter(){
        return center;
    }

    public double getRadius() {
        return radius;
    }
}
