/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2022 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
 * Copyright (C) 2020-2021 DO Quang Bao.
 * Copyright (C) 2021 VANNIYASINGAM Mithulan.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package meshIneBits.borderPaver.debug;

import meshIneBits.util.Vector2;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class PlotHelper {
    private final JFrame frame = new JFrame();
    public long index = 0;
    JPanel pane;
    final int maxImages = 15000;
    private double yMax;
    private double yMin;
    private double xMax;
    private double xMin;
    private Plot plot;

    public PlotHelper(Vector<Vector2> calibrationSerie) {

        xMin = Double.POSITIVE_INFINITY;
        xMax = Double.NEGATIVE_INFINITY;
        yMin = Double.POSITIVE_INFINITY;
        yMax = Double.NEGATIVE_INFINITY;
        for (Vector2 p : calibrationSerie) {
            if (p.x < xMin) {
                xMin = p.x;
            }
            if (p.x > xMax) {
                xMax = p.x;
            }
            if (p.y < yMin) {
                yMin = p.y;
            }
            if (p.y > yMax) {
                yMax = p.y;
            }
        }
        double margin = (xMax - xMin) / 10;

        this.plot = Plot.plot(Plot.plotOpts().
                        title("Plot " + index).
                        legend(Plot.LegendFormat.BOTTOM)).
                xAxis("x", Plot.axisOpts().
                        range(xMin - margin, xMax + margin)).
                yAxis("y", Plot.axisOpts().
                        range(yMin - margin, yMax + margin));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(900, 800);

    }


    public void addSeries(String name, Vector<Vector2> series, Plot.Marker marker, Plot.Line line, int size, Color color) {
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        for (Vector2 point : series) {
            x.add(point.x);
            y.add(point.y);
        }

        this.plot.series(name, Plot.data().xy(x, y), Plot.seriesOpts().marker(marker).markerColor(color).line(line).markerSize(size));
        //this.save();
        //this.drawWindow();
    }

    public void save() {
        if (index < maxImages) {
            try {
                plot.save("src/main/java/meshIneBits/artificialIntelligence/debug/images/" + index, "png");
                double margin = 0;
                this.plot = Plot.plot(Plot.plotOpts().
                                title("Plot " + index).
                                legend(Plot.LegendFormat.BOTTOM)).
                        xAxis("x", Plot.axisOpts().
                                range(xMin - margin, xMax + margin)).
                        yAxis("y", Plot.axisOpts().
                                range(yMin - margin, yMax + margin));
                index += 1;
                System.out.println("image index = " + index);
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
