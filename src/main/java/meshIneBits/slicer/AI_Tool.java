package meshIneBits.slicer;

import javafx.util.Pair;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.slicer.IA_util.Tools;
import meshIneBits.util.Polygon;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class AI_Tool {
    Tools tools;
    private MeshController meshController;
    Map<Slice, Vector<Segment2D>> sliceMap = new LinkedHashMap();

    public void startAI(MeshController MC) { //started when pushing button on UI
        this.meshController = MC;

        System.out.println("Starting IA pavement");
        System.out.println("Computing points of each slice");

        Vector<Slice> slicesList = MC.getMesh().getSlices();
        System.out.println(slicesList.size() + " slices to pave");
        tools = new Tools();

        for (Slice currentSlice : slicesList) {
            sliceMap.put(currentSlice, currentSlice.getSegmentList());
        }
        meshController.AIneedPaint = true;
    }

    public Pair<Vector<Polygon>, Vector2> computePoints(Slice currentSlice) {
        Vector<Polygon> polys = new Vector<>();
        Vector<Vector2> pointList;

        //compute points
        Vector<Segment2D> segment2DList = sliceMap.get(currentSlice);
        segment2DList = (Vector<Segment2D>) segment2DList.clone();
        Vector<Vector<Segment2D>> borderList = tools.rearrangeSegments(segment2DList);
        Vector<Vector<Vector2>> sectionsList;
        Vector<Vector<Vector2>> globalList = new Vector<>(); //contains all sectionsList elements
        Vector2 startPoint = new Vector2(0, 0);

        for (Vector<Segment2D> segmentList : borderList) {
            pointList = tools.computePoints(segmentList);
            pointList = tools.rearrangePoints(pointList);
            startPoint = pointList.get(0);

            //todo ANDRE : function in Tools that places points in a local coordinate system

            sectionsList = tools.splitSections(pointList);
            sectionsList.forEach(globalList::add);
        }

        for (Vector<Vector2> section : globalList) { //create polygons for drawing
            polys.add(tools.CreatePolygon(section));
        }

        /*
        //make regression
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(this.degree);
        for (Vector<Vector2> section : sectionsList) {
            WeightedObservedPoints observedPoints = new WeightedObservedPoints();
            for (Vector2 point : section) { observedPoints.add(point.x,point.y); }
            double[] coefficients = tool.reverse(fitter.fit(observedPoints.toList()));
            coefficients = tool.round(coefficients);
            tool.checkCoefficients(coefficients);
            tool.printCoefficients(coefficients);
        }*/ //todo Etienne : regressions
        return new Pair(polys, startPoint);
    }

    public Pair<Vector<Polygon>, Vector2> getactualPolygons_StartPoint(Slice slice) {
        return computePoints(slice);
    }
}
