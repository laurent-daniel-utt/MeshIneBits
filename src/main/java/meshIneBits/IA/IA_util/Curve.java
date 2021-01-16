package meshIneBits.IA.IA_util;

import meshIneBits.util.Vector2;

import java.util.Vector;

public class Curve {
    //todo @Andre : doc et passer tout en anglais
    //todo @Andre on peut se débarasser de cette classe ?
    private final String name;
    private Vector<Vector2> points = new Vector<>();

    //Courbe non définie
    public Curve(String name) {
        this.name = name;
    }

    // générer à partir de points
    public void generateCurve(Vector<Vector2> points) {
        this.points = points;
    }


    public Curve[] splitCurveInTwo() {

        Vector<Vector2> pointsX = new Vector<>();
        Vector<Vector2> pointsY = new Vector<>();
        double absCurv = 0;


        pointsX.add(new Vector2(absCurv, points.get(0).x));
        pointsY.add(new Vector2(absCurv, points.get(0).y));

        for (int i = 1; i < this.points.size(); i++) {
            absCurv += Math.sqrt(Math.pow(points.get(i - 1).x - points.get(i).x, 2)
                    + Math.pow(points.get(i - 1).y - points.get(i).y, 2));
            pointsX.add(new Vector2(absCurv, points.get(i).x));
            pointsY.add(new Vector2(absCurv, points.get(i).y));
        }

        Curve courbeX = new Curve("x(t)");
        Curve courbeY = new Curve("y(t)");
        courbeX.generateCurve(pointsX);
        courbeY.generateCurve(pointsY);

        return new Curve[]{courbeX, courbeY};
    }


    public Vector<Vector2> getPoints() {
        return points;
    }

    public String getName() {
        return name;
    }

    public int getN_points() {
        return points.size();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("name = " + name + "\n");
        for (Vector2 v : points) {
            str.append(v.x).append("\t").append(v.y).append("\n");
        }
        return str.toString();
    }
}