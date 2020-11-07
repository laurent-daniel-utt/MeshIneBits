package meshIneBits.slicer.IA_util;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

public class Grapheur {

    final XYChart chart = new XYChartBuilder().xAxisTitle("X").yAxisTitle("Y").width(600).height(400).build();

    public Grapheur() {
        new SwingWrapper(chart).displayChart();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
    }

    public void displayGraph(String name, Point[] points) {
        double[] xData = new double[points.length];
        double[] yData = new double[points.length];

        for (int i = 0; i < points.length; i += 1) {
            xData[i] = points[i].getAbs();
            yData[i] = points[i].getOrd();
        }

        chart.addSeries(name, xData, yData);
    }

    public void displayGraph(Curve courbe) {
        double[] xData = new double[courbe.getPoints().length];
        double[] yData = new double[courbe.getPoints().length];

        for (int i = 0; i < courbe.getPoints().length; i += 1) {
            xData[i] = courbe.getPoints()[i].getAbs();
            yData[i] = courbe.getPoints()[i].getOrd();
        }

        chart.addSeries(courbe.getName(), xData, yData);
    }
}