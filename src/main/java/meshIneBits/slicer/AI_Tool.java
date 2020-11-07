package meshIneBits.slicer;

import javafx.util.Pair;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.slicer.IA_util.Tools;
import meshIneBits.util.Polygon;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.util.Vector;

public class AI_Tool {
    Tools tools;
    private MeshController meshController;
    private Vector<Segment2D> segment2DList;

    public void startAI(MeshController MC) {
        this.meshController = MC;

        System.out.println("Starting IA pavement");
        System.out.println("Computing points of each slice");

        Vector<Slice> slicesList = MC.getMesh().getSlices();
        System.out.println(slicesList.size() + " slices to pave");
        tools = new Tools();

        for (Slice currentSlice : slicesList) {
            //computePoints(currentSlice);
        }
        meshController.AIneedPaint = true;
    }

    public Pair<Vector<Polygon>, Vector2> computePoints(Slice currentSlice) {
        Vector<Polygon> polys = new Vector<>();
        Vector<Vector2> pointList;

        //compute points
        this.segment2DList = currentSlice.getSegmentList(); //todo enregistrer les slices en this. pour pas devoir les rappeller
        Vector<Vector<Segment2D>> borderList = tools.rearrangeSegments(this.segment2DList);
        Vector<Vector<Vector2>> areaList;
        Vector<Vector<Vector2>> globalList = new Vector<>(); //contains all areaList elements
        Vector2 startPoint = new Vector2(0, 0);

        for (Vector<Segment2D> segmentList : borderList) {
            pointList = tools.computePoints(segmentList);
            pointList = tools.rearrangePoints(pointList);
            startPoint = pointList.get(0);

            //todo place points in a local coordinate system

            areaList = tools.splitAreas(pointList); //todo refactor "area" name
            areaList.forEach(globalList::add);
        }

        for (Vector<Vector2> area : globalList) { //create polygons for drawing
            polys.add(tools.CreatePolygon(area));
        }

        /*
        //make regression
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(this.degree);
        for (Vector<Vector2> area : areaList) {
            WeightedObservedPoints observedPoints = new WeightedObservedPoints();
            for (Vector2 point : area) { observedPoints.add(point.x,point.y); }
            double[] coefficients = tool.reverse(fitter.fit(observedPoints.toList()));
            coefficients = tool.round(coefficients);
            tool.checkCoefficients(coefficients);
            tool.printCoefficients(coefficients);
        }*/ //todo regressions
        return new Pair(polys, startPoint);
    }

    public Pair<Vector<Polygon>, Vector2> getactualPolygons_StartPoint(Slice slice) {
        return computePoints(slice);
    }
}
