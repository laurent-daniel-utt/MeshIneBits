package meshIneBits;

import meshIneBits.util.Circle;
import meshIneBits.util.Vector2;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Vector;
import java.util.stream.Collectors;

public class Test extends JPanel {

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        Ellipse2D e1 = new Ellipse2D.Double (getWidth()/2, getHeight()/2, 110, 100);
        Ellipse2D e2 = new Ellipse2D.Double (20.0, 70.0, 40.0, 40.0);

        Area a1 = new Area (e1);
        Area a2 = new Area (e2);
System.out.println("a1:"+a1);
       // a1.subtract (a2);

        g2.setColor (Color.orange);
        g2.fill (a1);
       // g2.fill (a2);
        g2.setColor (Color.black);
        g2.drawString ("subtract", 20, 140);
        System.out.println(a1.getBounds());
g2.setColor(Color.cyan);
        g2.fill(a1.getBounds2D());
        g2.setColor(Color.black);
        g2.fill(new Ellipse2D.Double (a1.getBounds2D().getMaxX(), a1.getBounds2D().getMinY(), 5, 5));
        System.out.println(a1.contains(19,20+35));
        Ellipse2D c = new Ellipse2D.Double (19.0, 20.0+35, 5, 5);
      g2.setColor(Color.GREEN);


        ArrayList<Circle> circles=new ArrayList<Circle>();

        Rectangle2D rectangle=a1.getBounds2D();
        Double startX=a1.getBounds2D().getMinX();
        Double endX=a1.getBounds2D().getMaxX();
        Double startY=a1.getBounds2D().getMinY();
        Double endY=a1.getBounds2D().getMaxY();


        for(double i=startX;i<endX;i+=0.5){
            for(double j=startY;j<endY;j=j+0.5){
                circles.add(new Circle(new Vector2(i,j), 2.5));

            }

        }
        double margin=2;
        System.out.println("size before="+circles.size());
      circles= (ArrayList<Circle>) circles.stream().filter(ci -> (a1.contains(ci.getCenter().x+ci.getRadius()+margin,ci.getCenter().y) && a1.contains(ci.getCenter().x,ci.getCenter().y+ci.getRadius()+margin)
              && a1.contains(ci.getCenter().x,ci.getCenter().y-ci.getRadius()-margin) && a1.contains(ci.getCenter().x-ci.getRadius()-margin,ci.getCenter().y))).collect(Collectors.toList());


        System.out.println("size after="+circles.size());
        Vector<Circle> positionTwoMostDistantCercles=new Vector<>();
        double longestDistance = 0;
        for (Circle cercle:circles){
            for (Circle cercle2:circles){

                if (Circle.CircleDistant(cercle, cercle2) > longestDistance) {
                    positionTwoMostDistantCercles.removeAllElements();
                    positionTwoMostDistantCercles.add(cercle);
                    positionTwoMostDistantCercles.add(cercle2);
                    longestDistance = Circle.CircleDistant(cercle, cercle2);
                }


            }
        }
g2.setColor(Color.BLACK);

        g2.fill(new Ellipse2D.Double(positionTwoMostDistantCercles.get(0).getCenter().x,positionTwoMostDistantCercles.get(0).getCenter().y,5,5));
        g2.fill(new Ellipse2D.Double(positionTwoMostDistantCercles.get(1).getCenter().x,positionTwoMostDistantCercles.get(1).getCenter().y,5,5));




    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();

        frame.getContentPane().add(new Test());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1500,1500);
        frame.setVisible(true);


    }
}
