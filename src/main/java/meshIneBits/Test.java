package meshIneBits;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

public class Test extends JPanel {

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        Ellipse2D e1 = new Ellipse2D.Double (20.0, 20.0, 80.0, 70.0);
        Ellipse2D e2 = new Ellipse2D.Double (20.0, 70.0, 40.0, 40.0);

        Area a1 = new Area (e1);
        Area a2 = new Area (e2);

        //a1.intersect (a2);

        g2.setColor (Color.orange);
       // g2.fill (a2);

        a1.subtract (a2);
        g2.fill (a1);
        //g2.setColor(Color.RED);
        //g2.fill (a1);
        g2.setColor (Color.black);
        g2.drawString ("intersect", 20, 140);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new Test());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(200,200);
        frame.setVisible(true);
    }
}