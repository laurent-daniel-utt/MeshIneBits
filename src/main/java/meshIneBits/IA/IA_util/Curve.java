package meshIneBits.IA.IA_util;

import meshIneBits.util.Vector2;

import javax.swing.plaf.ColorUIResource;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

public class Curve {

    private String name;
    private Vector<Vector2> points = new Vector();


    //Courbe non définie
    public Curve(String name) {
        this.name = name;
    }

    public void generateCurve(String filename) throws FileNotFoundException {
        Vector<Vector2> vPoints = new Vector<>();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(filename);

        java.util.Scanner scanner ;
        //java.io.File fichier = new java.io.File(filename);
        scanner = new java.util.Scanner(file);
        while (scanner.hasNext()) {
            String[] line = scanner.nextLine().split(":");
            double x = Double.parseDouble(line[0]);
            double y = Double.parseDouble(line[1]);
            Vector2 point = new Vector2(x,y);
            vPoints.add(point);
        }
        int i=0;
        for (Vector2 point:vPoints) {
            this.points.add(point);
            i++;
        }
        scanner.close();
    }

    public void generateCircle(Vector2 center, double r){

        int nbPoints = 30;
        double sum=0;
        double step = 2*Math.PI/nbPoints;
        for(int i=0; i<nbPoints; i++){
            points.add(new Vector2(center.x + Math.cos(sum+step)*r, center.y + Math.sin(sum+step)*r));
            sum+=step;
        }

    }


    //Génère les points à partir d'une équation
    public void generateCurve(Vector<Double> theta, double xStart, double nbPoints, double step) {
        double x = xStart;
        for (int i = 0; i < nbPoints; i++) {
            double y = 0;
            for (int deg = 0; deg < theta.size(); deg++) {
                y += theta.get(deg) * Math.pow(x, theta.size() - deg - 1);
            }
            this.points.add(new Vector2(x, y));
            x += step;
        }
    }

    public void generateCurve(Vector<Double> theta1, Vector<Double> theta2, double tStart, double nbPoints, double step) {
        double t = tStart;
        for (int i = 0; i < nbPoints; i++) {

            double x = 0;
            for (int deg = 0; deg < theta1.size(); deg++) {
                x += theta1.get(deg) * Math.pow(t, theta1.size() - deg - 1);
            }
            double y = 0;
            for (int deg = 0; deg < theta2.size(); deg++) {
                y += theta2.get(deg) * Math.pow(t, theta2.size() - deg - 1);
            }

            this.points.add(new Vector2(x, y));
            t += step;
        }

    }

    // générer à partir de points
    public void generateCurve(Vector<Vector2> points) {
        this.points = points;
    }


    //générer à partir de 2 courbes (= sorte d'équation paramétrque)
    //attention !!! ne marche qu'avec 2 courbes d'un même nombre de points, avec  des abcisses curvilignes identiques.
    public void generateCurve(Curve courbeX, Curve courbeY){
        for(int i = 0; i< courbeX.getN_points(); i++){
            Vector2 point = new Vector2(courbeX.getPoints().get(i).y, courbeY.getPoints().get(i).y);
            this.points.add(point);
        }
    }



    //génère du bruit sur les points de la courbe
    public void addNoise(float noise) {
        for (int i = 0; i < points.size(); i++) {
            double x = this.points.get(i).x + Math.random();
            double y = this.points.get(i).y + noise * Math.random();
            this.points.set(i, new Vector2(x, y));
        }
    }

    public void shufflePoints() {
        Random r = new Random();
        for (int i = this.points.size()-1; i > 0; i--) {
            int j = r.nextInt(i);
            Vector2 temp = this.points.get(i);
            this.points.set(i, this.points.get(j));
            this.points.set(j, temp);
        }
    }

    public Curve[] splitCurveInTwo() {

        Vector<Vector2> pointsX =  new Vector<>();
        Vector<Vector2> pointsY =  new Vector<>();
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
        String str = "name = " + name + "\n";
        for(Vector2 v : points){
            str += v.x + "\t" + v.y + "\n";
        }
        return str;
    }
}