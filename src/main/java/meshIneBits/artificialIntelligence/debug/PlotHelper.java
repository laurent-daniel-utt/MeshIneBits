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
                        range(-15, 15)).
                yAxis("y", Plot.axisOpts().
                        range(-15, 15));

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
