package meshIneBits.util;

import meshIneBits.SubBit2D;
import meshIneBits.config.CraftConfig;

import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.*;
import java.util.stream.Collectors;

import static meshIneBits.config.CraftConfig.precision;

public class TwoDistantPointsCalc {

  public final static TwoDistantPointsCalc instance = new TwoDistantPointsCalc();

  /*public Vector<Vector2> defineTwoPointNearTwoMostDistantPointsInAreaWithRadius(Area area,
      double radius) {
    Vector<Vector2> twoDistantPoints = new Vector<>();
    Vector<Vector2> positionTwoMostDistantPoint = getTwoMostDistantPointFromArea(area);
    if (positionTwoMostDistantPoint == null) {
      return twoDistantPoints;
    }
    Vector2 point1 = positionTwoMostDistantPoint.firstElement();
    Vector2 point2 = positionTwoMostDistantPoint.lastElement();
    Rectangle2D rectangle2D = area.getBounds2D();

    double startX = rectangle2D.getMinX();
    double startY = rectangle2D.getMinY();
    double endX = rectangle2D.getMaxX();
    double endY = rectangle2D.getMaxY();
    boolean foundFirstPoint = false;
    boolean foundSecondPoint = false;
    Vector2 pointResult1 = null;
    Vector2 pointResult2 = null;
    double distant1;
    double distant2;

    //Find a point near first point of two most distant points
    while (radius < 20) {
      for (double x = startX; x < endX; x += 1) {
        for (double y = startY; y < endY; y += 1) {
          if (area.contains(x, y)
              && !(x == CraftConfig.lengthFull / 2 || x == -CraftConfig.lengthFull / 2)
              && !(y == CraftConfig.bitWidth / 2 || y == -CraftConfig.bitWidth / 2)) {
            if (!foundFirstPoint) {
              distant1 = Math.sqrt(
                  ((x - point1.x) * (x - point1.x)) + ((y - point1.y) * (y - point1.y)));
              if (pointResult2 == null || (x != pointResult2.x && y != pointResult2.y)) {
                if (distant1 > radius && distant1 <= radius * 3) {
                  if (checkPointInsideAreaWithRadius(x, y, area, CraftConfig.suckerDiameter / 4)) {
                    pointResult1 = new Vector2(x, y);
                    foundFirstPoint = true;
                  }
                }
              }
            }
            if (!foundSecondPoint) {
              distant2 = Math.sqrt(
                  ((x - point2.x) * (x - point2.x)) + ((y - point2.y) * (y - point2.y)));
              if (pointResult1 == null || (x != pointResult1.x && y != pointResult1.y)) {
                if (distant2 > radius && distant2 <= radius * 3) {
                  if (checkPointInsideAreaWithRadius(x, y, area, CraftConfig.suckerDiameter / 4)) {
                    pointResult2 = new Vector2(x, y);
                    foundSecondPoint = true;
                  }
                }
              }
            }
          }
          if (foundFirstPoint && foundSecondPoint) {
            break;
          }
        }
        if (foundFirstPoint && foundSecondPoint) {
          break;
        }
      }
      if (pointResult1 != null && pointResult2 != null) {
        twoDistantPoints.add(pointResult1);
        twoDistantPoints.add(pointResult2);
        return twoDistantPoints;
      }
      radius++;
    }
    return twoDistantPoints;
  }
*/


//took about 2min53s with precision =0.5
//took about 11 sec with precision=1
    //33 sec classic(donut)

  /**
   *
   * @param area  la surface
   * on crée un tapi de cercle sur tout le rectangle qui cerne la surface rentré en param (list des cercles), ensuite on
   * filtre les cercles en prenant que celles qui sont à l'intérieur de la surface,ensuite on calcule les 2 cercles les plus
   * distants parmi la liste des cercles restantes après filtrage.
   * @precision c'est le paramètre qui détermine les pas de tapiage,exemple:precision=1=>chaque 1mm on va créer une nouvelle
   * cercle horizontalement et verticalement
   * @return un vecteur(collection) contenant les 2 points les plus distants
   *
   */
    public  Vector<Vector2> defineTwoMostDistantPointsInArea(Area area) {
    ArrayList<Circle> circles= new ArrayList<>();



    Double startX=area.getBounds2D().getMinX();
    Double endX=area.getBounds2D().getMaxX();
    Double startY=area.getBounds2D().getMinY();
    Double endY=area.getBounds2D().getMaxY();

//création de la tapi des cercles
    for(double i=startX;i<endX;i+=precision){
      for(double j=startY;j<endY;j=j+precision){
        circles.add(new Circle(new Vector2(i,j), CraftConfig.suckerDiameter/4));

      }

    }
    double margin=0;
//filtrage des cercles
    circles= (ArrayList<Circle>) circles.stream().filter(ci -> (area.contains(ci.getCenter().x+ci.getRadius()+margin,ci.getCenter().y) && area.contains(ci.getCenter().x,ci.getCenter().y+ci.getRadius()+margin)
            && area.contains(ci.getCenter().x,ci.getCenter().y-ci.getRadius()-margin)
            && area.contains(ci.getCenter().x-ci.getRadius()-margin,ci.getCenter().y))).collect(Collectors.toList());


//calcul des distances entre les cercles pour identifier les 2 cercles les plus distants
    Vector<Circle> positionTwoMostDistantCercles=new Vector<>();
    double longestDistance = 0;
    for (Circle cercle:circles){
      for (Circle cercle2:circles){

        if (Circle.CircleDistant(cercle, cercle2) > longestDistance) {
          positionTwoMostDistantCercles.removeAllElements();
          positionTwoMostDistantCercles.add(cercle);
          positionTwoMostDistantCercles.add(cercle2);
          longestDistance = Circle.CircleDistant(cercle, cercle2);
        }


      }
    }
 Vector<Vector2> MostTwoDistantPointsInArea=new Vector<>();
    if (!positionTwoMostDistantCercles.isEmpty()){
    MostTwoDistantPointsInArea.add(positionTwoMostDistantCercles.firstElement().getCenter());
    MostTwoDistantPointsInArea.add(positionTwoMostDistantCercles.lastElement().getCenter());
    }
    return  MostTwoDistantPointsInArea;
  }



//34,62 s classic(donut)
//
/*
    public  Vector<Vector2> defineTwoPointNearTwoMostDistantPointsInAreaWithRadius(Area area,double radius) {
       System.out.println("in the method");
        ArrayList<Circle> circles=new ArrayList<Circle>();

        Rectangle2D rectangle=area.getBounds2D();
        double precision=1;

        Double startX=area.getBounds2D().getMinX();
        Double endX=area.getBounds2D().getMaxX();
        Double startY=area.getBounds2D().getMinY();
        Double endY=area.getBounds2D().getMaxY();
int condition=1;
        double X1=startX,Y1=startY;
        double X2=endX-CraftConfig.suckerDiameter/8,Y2=startY;
        double X3=startX,Y3=startY;
        double X4=startX,Y4=endY-CraftConfig.suckerDiameter/8;
while (condition>0){


            Rectangle2D fromLeft=new Rectangle2D.Double(X1,Y1,CraftConfig.suckerDiameter/8,rectangle.getHeight());
            Rectangle2D fromRight=new Rectangle2D.Double(X2,Y2,CraftConfig.suckerDiameter/8,rectangle.getHeight());
            Rectangle2D fromAbove=new Rectangle2D.Double(X3,Y3,rectangle.getWidth(),CraftConfig.suckerDiameter/8);
            Rectangle2D fromBelow=new Rectangle2D.Double(X4,Y4,rectangle.getWidth(),CraftConfig.suckerDiameter/8);
       condition=0;
    System.out.println("Condition="+condition);
      System.out.println("X1="+X1+" X2="+X2+" X3="+X3+" X4="+X4);
       if(!area.intersects(fromLeft)){condition++;
                X1=X1+fromLeft.getWidth();
            startX=X1;
           System.out.println("in1");
            }

            if(!area.intersects(fromRight)){condition++;
                X2=X2-fromRight.getWidth();
            endX=X2;
                System.out.println("in2");
            }
            if(!area.intersects(fromAbove)){condition++;
                Y3=Y3+fromAbove.getHeight();
            endY=Y3;
                System.out.println("in3");
            }
            if(!area.intersects(fromBelow)){condition++;
                Y4=Y4-fromBelow.getHeight();
            startY=Y4;
                System.out.println("in4");
            }
    System.out.println("X1="+X1+" X2="+X2+" X3="+X3+" X4="+X4);
    System.out.println("stuck in Condition="+condition);
        }

        for(double i=startX;i<endX;i+=precision){
            for(double j=startY;j<endY;j=j+precision){
                circles.add(new Circle(new Vector2(i,j), CraftConfig.suckerDiameter/4));

            }

        }
        double margin=0;
        System.out.println("size before="+circles.size());
        circles= (ArrayList<Circle>) circles.stream().filter(ci -> (area.contains(ci.getCenter().x+ci.getRadius()+margin,ci.getCenter().y) && area.contains(ci.getCenter().x,ci.getCenter().y+ci.getRadius()+margin)
                && area.contains(ci.getCenter().x,ci.getCenter().y-ci.getRadius()-margin)
                && area.contains(ci.getCenter().x-ci.getRadius()-margin,ci.getCenter().y))).collect(Collectors.toList());


        System.out.println("size after="+circles.size());
        Vector<Circle> positionTwoMostDistantCercles=new Vector<>();
        double longestDistance = 0;
        for (Circle cercle:circles){
            for (Circle cercle2:circles){

                if (Circle.CircleDistant(cercle, cercle2) > longestDistance) {
                    positionTwoMostDistantCercles.removeAllElements();
                    positionTwoMostDistantCercles.add(cercle);
                    positionTwoMostDistantCercles.add(cercle2);
                    longestDistance = Circle.CircleDistant(cercle, cercle2);
                }


            }
        }
        Vector<Vector2> MostTwoDistantPointsInArea=new Vector<>();
        if (!positionTwoMostDistantCercles.isEmpty()){
            MostTwoDistantPointsInArea.add(positionTwoMostDistantCercles.firstElement().getCenter());
            MostTwoDistantPointsInArea.add(positionTwoMostDistantCercles.lastElement().getCenter());
        }
        return  MostTwoDistantPointsInArea;
    }
*/








  public Vector<Vector2> getTwoMostDistantPointFromArea(Area area) {

    Vector<Vector2> positionTwoMostDistantPoint = new Vector<>();
    double longestDistance = 0;
    for (PathIterator p1 = area.getPathIterator(null); !p1.isDone(); p1.next()) {
      double[] coord1 = new double[6];

      int type1 = p1.currentSegment(coord1);
      if (type1 == PathIterator.SEG_CLOSE) {
        continue;
      }
      Vector2 v1 = new Vector2(coord1[0], coord1[1]);

      for (PathIterator p2 = area.getPathIterator(null); !p2.isDone(); p2.next()) {
        double[] coord2 = new double[6];
        int type2 = p2.currentSegment(coord2);
        if (type2 == PathIterator.SEG_CLOSE) {
          continue;
        }
        Vector2 v2 = new Vector2(coord2[0], coord2[1]);
        if (Vector2.dist(v1, v2) > longestDistance) {
          positionTwoMostDistantPoint.removeAllElements();
          positionTwoMostDistantPoint.add(v1);
          positionTwoMostDistantPoint.add(v2);
          longestDistance = Vector2.dist(v1, v2);
        }
      }
    }
    return positionTwoMostDistantPoint;
  }


  /**
   *
   * @param area la surface du subbit
   * @param bit le subbit
   * @return les 2 points les plus extremes vers la gauche et vers la droite
   */

  public synchronized Vector<Vector2> getXminXmaxFromArea(Area area, SubBit2D bit) {


    HashMap<Double,Vector2> AllPoints = new HashMap<Double,Vector2>();
    Vector<Vector2> TwoPoints = new Vector<>();

    for (PathIterator p1 = area.getPathIterator(null); !p1.isDone(); p1.next()) {
      double[] coord1 = new double[6];

      int type1 = p1.currentSegment(coord1);
      if (type1 == PathIterator.SEG_CLOSE) {
        continue;
      }
      Vector2 v1 = new Vector2(coord1[0], coord1[1]);
      v1=v1.getTransformed(bit.getParentBit().getTransfoMatrixToCS());


     AllPoints.put(v1.x,v1);


      //System.out.println("v1="+v1.getTransformed(bit.getParentBit().getTransfoMatrixToCS()));
    }
Set<Double> s=  AllPoints.keySet();
    Vector<Double> list=new Vector<>();
    Iterator<Double> its= s.iterator();
    while (its.hasNext()){
      Double d= its.next();
      list.add(d);

    }
    Collections.sort(list);



    TwoPoints.add(AllPoints.get(list.firstElement()));
    TwoPoints.add(AllPoints.get(list.lastElement()));
    return TwoPoints;
  }





  public boolean checkPointInsideAreaWithRadius(double x, double y, Area area,
      double radius) {
    Vector<Vector<Segment2D>> segments = AreaTool.getSegmentsFrom(area);
    Vector2 point = new Vector2(x, y);
    //check if the area contain point and the distant of the point inside with the segment is always smaller the radius
    if (area.contains(point.x, point.y)) {
      for (Vector<Segment2D> polygon : segments) {
        for (Segment2D segment2D : polygon) {
          if (radius > 0 && segment2D.distFromPoint(point) < radius) {
            return false;
          }
        }
      }
    } else {
      return false;
    }

    return true;
  }
}
