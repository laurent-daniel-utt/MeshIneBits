package meshIneBits.IA;


import meshIneBits.IA.IA_util.Curve;
import meshIneBits.util.Vector2;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.util.Vector;

public class Grapheur {

    final XYChart chart = new XYChartBuilder().xAxisTitle("X").yAxisTitle("Y").width(600).height(400).build();

    public Grapheur() {
        new SwingWrapper(chart).displayChart();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        //chart.getStyler().setYAxisMin(-10.0);
        //chart.getStyler().setYAxisMax(10.0);
    }

    public void displayGraph(String name, Vector<Vector2> points) {
        double[] xData = new double[points.size()];
        double[] yData = new double[points.size()];

        for (int i = 0; i < points.size(); i += 1) {
            xData[i] = points.get(i).x;
            yData[i] = points.get(i).y;
        }

        chart.addSeries(name, xData, yData);
    }

    public void displayGraph(Curve curve) {
        double[] xData = new double[curve.getPoints().size()];
        double[] yData = new double[curve.getPoints().size()];

        for (int i = 0; i < curve.getPoints().size(); i += 1) {
            xData[i] = curve.getPoints().get(i).x;
            yData[i] = curve.getPoints().get(i).y;
        }

        chart.addSeries(curve.getName(), xData, yData);
    }

    public void clear(String name) {
        chart.removeSeries(name);
    }
}