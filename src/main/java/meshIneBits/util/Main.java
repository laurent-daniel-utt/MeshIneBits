package meshIneBits.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class Main extends JPanel {

    public void paint(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;

        Ellipse2D e1 = new Ellipse2D.Double (60.0, 50.0, 40.0, 70.0);
        Ellipse2D e2 = new Ellipse2D.Double (20.0, 70.0, 40.0, 40.0);

        Area a1 = new Area (e1);
        Area a2 = new Area (e2);

        //a1.subtract (a2);

        g2.setColor (Color.GREEN);
        g2.fill (a2);
        g2.fill (a1);
        g2.setColor (Color.black);
        g2.drawString ("subtract", 20, 140);
Graphics2D g2d;

Ellipse2D circle=new Ellipse2D.Double(70.0, 60.0, 25, 40);

        Area a3=new Area(circle);
        g2.setColor(Color.BLACK);
        g2.fill(circle);
        System.out.println(a1.getBounds());
        System.out.println(e1.getMaxX());
        System.out.println(a1.contains(70.0, 60.0, 25, 40));
        Rectangle2D r= a3.getBounds2D();
//a1.contains(70.0, 60.0, 25, 40);
      //  g2.setColor(Color.BLUE);
    //g2.fill(r);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();

        frame.getContentPane().add(new Main());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,400);
        frame.setVisible(true);
    }
}