package meshIneBits.artificialIntelligence.debug;

import meshIneBits.util.Vector2;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class PlotHelper {
    private Plot plot;
    private JFrame frame = new JFrame();
    JPanel pane;
    public int index = 0;
    int maxImages = 1000;

    public PlotHelper() {
        this.plot = Plot.plot(Plot.plotOpts().
                        title("Plot").
                        legend(Plot.LegendFormat.BOTTOM)).
                xAxis("x", Plot.axisOpts().
                        range(-60, 100)).
                yAxis("y", Plot.axisOpts().
                        range(160, 240));

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(900, 800);


    }

    public void addSeries(String name, Vector<Vector2> series, Plot.Marker marker, Plot.Line line, int size) {
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        for (Vector2 point : series) {
            x.add(point.x);
            y.add(point.y);
        }

        this.plot.series(name, Plot.data().
                                xy(x, y),
                        Plot.seriesOpts().
                                marker(marker).
                                markerColor(Color.GREEN)
                                .line(line)
                                .markerSize(size));
        //this.save();
        //this.drawWindow();
    }

    public void save(){
        if (index < maxImages) {
            try {
                plot.save("src/main/java/meshIneBits/artificialIntelligence/debug/images/" + String.valueOf(index), "png");
                index += 1;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Max d'images enregistrées atteint");
        }
    }

    public void drawWindow() {

        BufferedImage image = this.plot.draw();
        pane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null);
            }
        };

        frame.add(pane);
        frame.setVisible(true);
    }
}
