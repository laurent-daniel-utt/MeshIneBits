package meshIneBits.slicer.IA_util;

import java.io.File;
import java.util.Random;
import java.util.Vector;

public class Curve {

    private Point[] points;
    private String name;
    private int n_points;


    //Courbe non définie
    public Curve(int n_points, String name) {
        this.name = name;
        this.n_points = n_points;
        this.points = new Point[n_points];
    }

    public Curve(String filename) throws java.io.IOException {
        Vector<Point> vpoints = new Vector<>();
        ClassLoader classLoader = getClass().getClassLoader();
        File fichier = new File(classLoader.getResource(filename).getFile());

        java.util.Scanner lecteur;
        //java.io.File fichier = new java.io.File(filename);
        lecteur = new java.util.Scanner(fichier);
        while (lecteur.hasNext()) {
            String[] line = lecteur.nextLine().split(":");
            double x = Double.parseDouble(line[0]);
            double y = Double.parseDouble(line[1]);
            Point p = new Point(x, y);
            vpoints.add(p);
        }
        this.points = new Point[vpoints.size()];
        int i = 0;
        for (Point p : vpoints) {
            this.points[i] = p;
            i++;
        }
    }


    //Génère les points à partir d'une équation
    public void generateCurve(double[] theta, double xStart, double step) {
        double x = xStart;
        for (int i = 0; i < n_points; i++) {
            double y = 0;
            for (int deg = 0; deg < theta.length; deg++) {
                y += theta[deg] * Math.pow(x, theta.length - deg - 1);
            }
            this.points[i] = new Point(x, y);
            x += step;
        }
    }

    public void generateCurve(double[] theta1, double[] theta2, double tStart, double step) {
        double t = tStart;
        for (int i = 0; i < n_points; i++) {

            double x = 0;
            for (int deg = 0; deg < theta1.length; deg++) {
                x += theta1[deg] * Math.pow(t, theta1.length - deg - 1);
            }
            double y = 0;
            for (int deg = 0; deg < theta2.length; deg++) {
                y += theta2[deg] * Math.pow(t, theta2.length - deg - 1);
            }

            this.points[i] = new Point(x, y);
            t += step;
        }

    }

    // générer à partir de points
    public void generateCurve(Point[] points) {
        this.points = points;
    }

    //générer à partir de 2 courbes (= sorte d'équation paramétrque)
    //attention !!! ne marche qu'avec 2 courbes d'un même nombre de points, avec  des abcisses curvilignes identiques.
    public void generateCurve(Curve courbeX, Curve courbeY) {
        for (int i = 0; i < courbeX.getN_points(); i++) {
            this.points[i] = new Point(courbeX.getPoints()[i].getOrd(), courbeY.getPoints()[i].getOrd());
        }
    }


    /*
    //Génération à partir d'une liste d'ordonnées
    public void generateCurve(double[] list, double xStart, double step) {
        for (int x=0;x<list.length;x++) {
            this.points[x] = new Point(x,list[x]);
        }
    }
    */


    //génère du bruit sur les points de la courbe
    public void addNoise(float noise) {
        for (int i = 0; i < n_points; i++) {
            this.points[i].setAbs(this.points[i].getAbs() + Math.random());
            this.points[i].setOrd(this.points[i].getOrd() + noise * Math.random());
        }
    }

    public void shufflePoints() {
        Random r = new Random();
        for (int i = this.points.length - 1; i > 0; i--) {
            int j = r.nextInt(i);
            Point temp = this.points[i];
            this.points[i] = this.points[j];
            this.points[j] = temp;
        }
    }

    public Curve[] splitCurveInTwo() {

        Point[] pointsX = new Point[this.n_points];
        Point[] pointsY = new Point[this.n_points];
        double absCurv = 0;

        pointsX[0] = new Point(absCurv, points[0].getAbs());
        pointsY[0] = new Point(absCurv, points[0].getOrd());

        for (int i = 1; i < this.n_points; i++) {
            absCurv += Math.sqrt(Math.pow(points[i - 1].getAbs() - points[i].getAbs(), 2)
                    + Math.pow(points[i - 1].getOrd() - points[i].getOrd(), 2));
            pointsX[i] = new Point(absCurv, points[i].getAbs());
            pointsY[i] = new Point(absCurv, points[i].getOrd());
        }

        Curve courbeX = new Curve(this.n_points, "x(t)");
        Curve courbeY = new Curve(this.n_points, "y(t)");
        courbeX.generateCurve(pointsX);
        courbeY.generateCurve(pointsY);

        return new Curve[]{courbeX, courbeY};
    }

    public Point[] getPoints() {
        return points;
    }

    public String getName() {
        return name;
    }

    public int getN_points() {
        return n_points;
    }
}