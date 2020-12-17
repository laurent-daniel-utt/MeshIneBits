package meshIneBits.IA.IA_util;

import meshIneBits.util.Polygon;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.awt.geom.Area;
import java.util.Arrays;
import java.util.Vector;
import java.util.stream.IntStream;

public class Tools {

    //compute a pointList from a segmentList
    public Vector<Vector2> computePoints(Vector<Segment2D> segmentList) {
        Vector<Vector2> pointList = new Vector<>();
        for (Segment2D segment : segmentList) {
            pointList.add(new Vector2(segment.start.x, segment.start.y));
        }
        pointList.remove(0);
        return pointList;
    }


    //divide a pointlist and return a vector of pointlist
    public Vector<Vector<Vector2>> splitInSections(Vector<Vector2> pointList) {
        Vector<Vector<Vector2>> areaList = new Vector<>(); //contains different pointLists

        int PointsIndex;
        int areasIndex = 0;

        areaList.add(new Vector<>());
        areaList.get(0).addElement(pointList.get(0));
        areaList.get(0).addElement(pointList.get(1));//add the two first segments

        for (PointsIndex = 2; PointsIndex < pointList.size(); PointsIndex++) { // check for each point if it is OK, else cut list at this point
            Vector2 actualPoint = pointList.get(PointsIndex);
            Vector2 lastPoint = pointList.get(PointsIndex - 1);
            Vector2 beforeLastPoint = pointList.get(PointsIndex - 2);
            if (isPointOK(actualPoint, lastPoint, beforeLastPoint)) {
                areaList.get(areasIndex).addElement(actualPoint);
            } else {
                if (PointsIndex != pointList.size()) {
                    areaList.get(areasIndex).addElement(lastPoint);
                    areaList.add(new Vector<>());
                    areasIndex++;
                    areaList.get(areasIndex).addElement(lastPoint);
                    areaList.get(areasIndex).addElement(actualPoint);
                }
            }
        }
        areaList.get(areasIndex).addElement(pointList.get(pointList.size() - 1));

        /*for (Vector<Vector2> area:areaList) {
            if (area.size() == 0) {
                System.out.println(area.size());
                areaList.remove(area);
            }
        }*/ //never used

        return areaList;
    }


    private boolean isPointOK(Vector2 actualPoint, Vector2 lastPoint, Vector2 beforeLastPoint) { //in order from list : beforeLastPoint, lastPoint, actualPoint
        int direction = beforeLastPoint.x < lastPoint.x ? 1 : -1;
        if (direction == 1 && lastPoint.x >= actualPoint.x || direction == -1 && lastPoint.x <= actualPoint.x) {
            return false;
        }
        return true;
    }

    public Polygon CreatePolygon(Vector<Vector2> area) {
        Polygon poly = new Polygon();
        for (int i = 1; i < area.size(); i++) {
            Vector2 start = area.get(i - 1);
            Vector2 end = area.get(i);
            Segment2D segment = new Segment2D(start, end);
            poly.addEnd(segment);
        }
        return poly;
    }

    public double[] reverse(double[] listA) {
        return IntStream.range(0, listA.length).mapToDouble(i -> listA[listA.length - i - 1]).toArray();
    }

    public void printCoefficients(double[] coefficients) {
        System.out.println("résultats :");
        Arrays.stream(coefficients).forEachOrdered(System.out::println);
        System.out.println("\n");
    }

    public double[] round(double[] list) {
        for (int index = 0; index < list.length; index++) {
            if (Math.abs(list[index]) < Math.pow(10, -10)) { //todo E^(-10) or more ?
                list[index] = 0;
            }
        }
        return list;
    }

    public void checkCoefficients(double[] coefficients) throws Exception {
        for (double coeff : coefficients) {
            if (Double.isNaN(coeff) || Double.isInfinite(coeff))
                throw new Exception("A coefficient returned is NaN or Infinite.");
        }
    }

    /**
     * Permet de savoir si poly1 est dans poly2
     * @param poly1 Un Polygon
     * @param poly2 Un Polygon
     * @return      poly1 est dans poly2
     */
    public boolean isPolygon1InPolygon2(Polygon poly1, Polygon poly2) {
        Area area1 = new Area(poly1.toPath2D());
        Area area2 = new Area(poly2.toPath2D());
        Area intersection = (Area) area1.clone();
        intersection.intersect(area2);
        return intersection.equals(area1);
    }
}