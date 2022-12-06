package meshIneBits.util.supportImportFile;

import javafx.util.Pair;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.*;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Vector;

public class Reconstitute  {
    private ArrayList<ArrayList<Pair<FallType, Path2D.Double>>>cutpaths;
    private ArrayList<Area>fallenAreas=new ArrayList<>();
    private static int currentDecoupBatchNum=0;
    private static int currentDeposeBatchNum=0;
    public static final CustomLogger logger = new CustomLogger(Reconstitute.class);
    Area subBitArea=new Area();
    ArrayList<Area>remainings=new ArrayList<Area>();
    private Rectangle2D.Double bit_complet=new Rectangle2D.Double(-CraftConfig.lengthFull/2,-CraftConfig.bitWidth/2,
            CraftConfig.lengthFull,CraftConfig.bitWidth);
    private ArrayList<Pair<FallType,ArrayList<ArrayList<Vector2>>>>sub_paths_points=new ArrayList<>();
    private ArrayList<Pair<FallType,ArrayList<Path2D.Double>>>sub_paths=new ArrayList<>();
    private ArrayList<Pair<FallType, Path2D.Double>> Node_cutpath=new ArrayList<>();

    public static void setCurrentDecoupBatchNum(int currentDecoupBatchNumI) {
        currentDecoupBatchNum = currentDecoupBatchNumI;
    }

    public static void setCurrentDeposeBatchNum(int currentDeposeBatchNumI) {
        currentDeposeBatchNum = currentDeposeBatchNumI;
    }

    public  Area recreateArea(ArrayList<ArrayList<Pair<FallType, Path2D.Double>>> cutpaths, int id, boolean isDecoup){
        this.cutpaths=cutpaths;
        setNode_cutpath(id, isDecoup);
        build_subpaths();
        subBitArea=reconstruct();
        System.out.println("2");
        return subBitArea;
    }

public static Reconstitute getInstance(){
        return new Reconstitute();
}
    /**
     * we precise from what cut-paths Node i.e(what subbit with no-cutting cases included) we want to reconstruct the area of the
     * subbit i.e wich cut path we need to follow to get the right subbit
     * @param id the absolute bit id value
     * id=5 ==>the fifth subbit==>the fifth element of the collection
     */
       public void setNode_cutpath(int id,boolean isDecoup){
       if(isDecoup) id=id-CraftConfig.nbBitesBatch*currentDecoupBatchNum;
       else id=id-CraftConfig.nbBitesBatch*currentDeposeBatchNum;
        Node_cutpath=cutpaths.get(id);
       }

    /**
     * method that build subaths
     * see Majed_Documents>Reconstitue>New_Architecture.png And Majed_Documents>Reconstitue>Creating_Area.docx
     */
    public void build_subpaths(){
        ArrayList<ArrayList<Vector2>> points_of_subpath = new ArrayList<ArrayList<Vector2>>();
        ArrayList<ArrayList<Vector2>> clone = new ArrayList<ArrayList<Vector2>>();
        for (Pair<FallType, Path2D.Double> pair : Node_cutpath) {
            FallType type = pair.getKey();
            if (pair.getKey() == FallType.Subbit || pair.getKey() == FallType.Chute) {
                decompose(pair.getValue()).forEach(sub_paths -> {
                    /**on créer une liste,chaque case de la liste est l'ensemble des points d'un subpath*/
                    points_of_subpath.add(TwoDistantPointsCalc.instance.getPointsFromPath(sub_paths));
                });
                System.out.println("points_of_subpath size:" + points_of_subpath.size());
                /**on créer une liste,chaque case de la liste est un pair de fallType(key) et la décompositon du path du fallType*/
                clone = (ArrayList<ArrayList<Vector2>>) points_of_subpath.clone();
                sub_paths_points.add(new Pair<>(type, clone));
                sub_paths.add(new Pair<>(type, decompose(pair.getValue())));
                points_of_subpath.clear();
            } else if (type == FallType.Sub_Drop) {
                sub_paths_points.add(new Pair<>(type, new ArrayList<>()));
            }
        }
    }



    /**
     * method to reconstruct the subbit Area
     * @return the Area of the subbit
     * see Majed_Documents>Reconstitue>Creating_Area.docx
     */
    public Area reconstruct(){
        Area remainingArea=new Area(bit_complet);
        Area returnedArea=new Area();

        if(Node_cutpath.get(0).getKey()==FallType.Nocutting) {System.out.println("in no cutting condition");
            return new Area(bit_complet);}

        for (Pair<FallType, ArrayList<ArrayList<Vector2>>> fallTypeArrayListPointsPair : sub_paths_points) {

            FallType type = fallTypeArrayListPointsPair.getKey();
            ArrayList<ArrayList<Vector2>> listOfPoints = fallTypeArrayListPointsPair.getValue();
            if (listOfPoints.isEmpty())System.out.println("listOfPoints is empty");
            if(type==FallType.Sub_Drop) {System.out.println("in drop condition");
                returnedArea=remainingArea;
                return returnedArea;
            }
            for (ArrayList<Vector2> points : listOfPoints) {
                int indexofFallPair = sub_paths_points.indexOf(fallTypeArrayListPointsPair);
                int indexOfSubPath = listOfPoints.indexOf(points);
                /** si le premier et dernier point de découpe sont sur la meme ligne du bit*/
                if ( samePlacement(points.get(0),points.get(points.size() - 1))  ) {System.out.println("in same placement");
                    Area fallenArea=new Area(sub_paths.get(indexofFallPair).getValue().get(indexOfSubPath));
                    /**si ces points sont les derniers dans le falltype c.à.d la decoupe par ce fallType est finit*/
                    if (listOfPoints.indexOf(points) == listOfPoints.size() - 1) {
                        if (type == FallType.Subbit) {
                            returnedArea = fallenArea;
                            return returnedArea;
                            // continue;
                        } else if (type==FallType.Chute) {
                            remainingArea=updateArea(remainingArea,fallenArea);
                        }
                    }/**s'il y a des chutes au milieu de découpe, avant que la découpe soit finit
                     */
                    else {remainingArea=updateArea(remainingArea,fallenArea);
                    }
                    /** si le premier et dernier point de découpe sont sur des lignes parallèles (opposite sides of the bit)*/
                } else if (oppositePlacement(points.get(0),points.get(points.size()-1))) {System.out.println("in opp placement");
                    Area fallenArea= calculateFallenArea((Area) remainingArea.clone(),sub_paths.get(indexofFallPair).getValue().get(indexOfSubPath),true);
                    remainingArea.subtract(fallenArea);
                    Area a= (Area) remainingArea.clone();
                    remainings.add(a);
                    /**si ces points sont les derniers dans le falltype c.à.d la decoupe par ce fallType est finit*/
                    if (listOfPoints.indexOf(points) == listOfPoints.size() - 1) {

                        if (type == FallType.Subbit) {
                            returnedArea = fallenArea;
                            return returnedArea;
                        }
                    }/**si le premier et le dernier point sont sur les extrémités perpendiculaire (longueur et largeur)*/
                }else {System.out.println("in 3d case placement");
                    Area fallenArea= calculateFallenArea((Area) remainingArea.clone(),sub_paths.get(indexofFallPair).getValue().get(indexOfSubPath),false);
                    remainingArea=updateArea(remainingArea,fallenArea);
                    /**si ces points sont les derniers dans le falltype c.à.d la decoupe par ce fallType est finit*/
                    if (listOfPoints.indexOf(points) == listOfPoints.size() - 1) {

                        if (type == FallType.Subbit) {

                            returnedArea = fallenArea;
                            return returnedArea;
                            // continue;
                        }
                    }
                }
            }
        }
        return returnedArea;
    }




    /**
     * @param path falltype path
     * @return list of subpaths (method used to decompse a falltype path to sub paths)
     * see Majed_Documents>Reconstitue>Creating_Area.docx
     */
    public ArrayList<Path2D.Double> decompose(Path2D.Double path){
        ArrayList<Vector2>points= TwoDistantPointsCalc.instance.getPointsFromPath(path);
//if(path.getPathIterator(null).isDone())System.out.println("path empty");
        ArrayList<Path2D.Double>sub_paths=new ArrayList<>();
        Path2D.Double p=new Path2D.Double();
        p.moveTo(points.get(0).x,points.get(0).y);
        for(int i=1;i<points.size();i++){
            if(!checkpointOnExtremity(points.get(i))) {//System.out.println("is in if ?"+" p:"+points.get(i)+" placement:"+placement(points.get(i)));
                p.lineTo(points.get(i).x,points.get(i).y); }
            else {//System.out.println("is in else ?"+" p:"+points.get(i));
                p.lineTo(points.get(i).x,points.get(i).y);
                p.closePath();
                sub_paths.add(p);
                p=new Path2D.Double();
                p.moveTo(points.get(i).x,points.get(i).y);
            }
        }
        System.out.println("end of decompose sub_paths:"+sub_paths.size());
        return sub_paths;
    }



    /**
     *
     * @param remainingArea the remaining Area of the bit
     * @param subpath the subpath
     * @param opp ture if subpath of opposite sides(starts at a side and ends on the opposite side),
     * false if subpath of perpendicular sides(starts at a side and ends on its perpendicular side)
     * @return the fallen Area
     * see Majed_Documents>Reconstitue>Creating_Area.docx
     */
    private Area calculateFallenArea(Area remainingArea,Path2D.Double subpath,boolean opp){
        Area fallenArea=new Area();
        /**when the path is a strict line we adjust it by adding an insignificant point so we can create an area of it
         since we cant create an area from a line
         */
        ArrayList<Vector2> points_initial=TwoDistantPointsCalc.instance.getPointsFromPath(subpath);
        if(points_initial.size()==2){
            Vector2 adjustingpoint=new Vector2(points_initial.get(0).x+0.005,points_initial.get(0).y+0.005);
            points_initial.add(1,adjustingpoint);
            subpath=new Path2D.Double();
            subpath.moveTo(points_initial.get(0).x,points_initial.get(0).y);
            subpath.lineTo(points_initial.get(1).x,points_initial.get(1).y);
            subpath.lineTo(points_initial.get(2).x,points_initial.get(2).y);
            System.out.println("ADJUSTED");
        }

        remainingArea.subtract(new Area(subpath));
        //normalement doit etre de taille 2
        Vector<Area> areas= AreaTool.segregateArea(remainingArea);

        for(Area area:areas){
            /** l'area qui tombe est celui qui n'est pas tenu par le robot de découpe et donc qui ne contient aucun point
             de la zone tenu par le robot le -5 c'est just pour etre sur qu'on est dans la zone on peut le modifier par
             d'autre valeur tant qu'on sort pas de la Holding zone
             */
            if(!area.contains(CraftConfig.lengthFull/2-5,CraftConfig.bitWidth/2-5)&& !area.isEmpty()){
                fallenArea=area;
                System.out.println("fallenArea identified as:"+fallenArea.getBounds2D());
            }
        }
        Vector<Area> areasOfcutpath= AreaTool.segregateArea(new Area(subpath));
        if (!areasOfcutpath.isEmpty()){
            for (Area areaOfcutpath : areasOfcutpath) {
                //creating the path of the area
                Path2D.Double pa = new Path2D.Double(areaOfcutpath);
                ArrayList<Vector2> points = TwoDistantPointsCalc.instance.getPointsFromPath(pa);
                points=removeredundants(points);
                points=reOrderList(points);
                if (areaAddedtoFallen(points,opp)) fallenArea.add(areaOfcutpath);
                fallenAreas.add(fallenArea);
            }
        }
        return fallenArea;
    }


    /**
     * a method that removes redundant points, because creating a path from an area creates redundant points
     * @param points extracted points from the area
     * @return a new list with no redundant points
     */
  private ArrayList<Vector2> removeredundants(ArrayList<Vector2> points){
        ArrayList<Vector2>newPoints=new ArrayList<>();
     for (Vector2 p:points){
      if (!containsPoint(newPoints,p))newPoints.add(p);
     }
        return newPoints;
  }


    /**
     * @param list the new points list
     * @param point the next point
     * @return true if the new list already contains the next point, and false if not
     */
    private boolean containsPoint(ArrayList<Vector2> list,Vector2 point){
    //0.0005 is just random insignificant numbre i chose to remove redundant points with insignifcant diffrences
        for (Vector2 p : list) {
            if (Math.abs(p.x - point.x)<0.0005 && Math.abs(p.y - point.y)<0.0005) return true;
        }
        return false;
    }



    /**
     * method used to know if the subpath Area should be added to the fallen Area or not
     * @param points list of the subpath points
     * @param opp ture in the case of a subpath on oppsite sides,false in the case of perpendicular sies
     * @return true if the subpath are should be added to fallen area,false if not
     * see Majed_Documents>Reconstitue>Creating_Area.docx
     */
    private boolean areaAddedtoFallen(ArrayList<Vector2> points,boolean opp){
        if (opp){System.out.println("in addedtoFallen if");
            Vector2 pointFromHoldingZOne=new Vector2(CraftConfig.lengthFull/2-5,0);
            Line lineEndings=new Line(points.get(0),points.get(points.size()-1));
            Line lineFromHoldingzone=new Line(points.get(1),pointFromHoldingZOne);
            Vector2 intersection=  Line.getIntersection(lineEndings,lineFromHoldingzone);
            if(Vector2.dist(intersection,pointFromHoldingZOne)>Vector2.dist(points.get(1),pointFromHoldingZOne)){ System.out.println("added to fallen");
                return true;}
        }
        else{
            System.out.println("in addedtoFallen else");
            double x1=points.get(0).x;
            double x2=points.get(points.size()-1).x;
            double y1=points.get(0).y;
            double y2=points.get(points.size()-1).y;

            double ycorner, xcorner ;

            double maxY=Math.max(Math.abs(y1),Math.abs(y2));
            double maxX=  Math.max(Math.abs(x1),Math.abs(x2));

            if (maxX == Math.abs(x1)) xcorner = x1 ;
            else xcorner = x2;
            if (maxY == Math.abs(y1)) ycorner = y1 ;
            else ycorner = y2;

            Vector2 corner=new Vector2(xcorner,ycorner);
            Line lineEndings=new Line(points.get(0),points.get(points.size()-1));
            Line lineToCorner=new Line(corner,points.get(1));
            Vector2 intersection=Line.getIntersection(lineEndings,lineToCorner);
            if(Vector2.dist(intersection,corner)<Vector2.dist(points.get(1),corner)){ System.out.println("added to fallen");
                return true;}
        }
        return false;
    }


    private ArrayList<Vector2> reOrderList(ArrayList<Vector2> points){
        int i=0;
        boolean firstreplaced=false;
        while(i<points.size()){
            if((points.get(i).x<=-CraftConfig.lengthFull/2 || points.get(i).y<=-CraftConfig.bitWidth/2
                    ||points.get(i).y>=CraftConfig.bitWidth/2)&&!firstreplaced){
                Vector2 item=points.get(i);
                points.remove(i);
                points.add(0,item);
                firstreplaced=true;
            } else if ((points.get(i).x<=-CraftConfig.lengthFull/2 || points.get(i).y<=-CraftConfig.bitWidth/2
                    ||points.get(i).y>=CraftConfig.bitWidth/2)&&firstreplaced) {
                Vector2 item=points.get(i);
                points.remove(i);
                points.add(points.size(),item);
            }
            i++;
        }
        return points;
    }
    private boolean TopCornerCutting(Vector2 p1,Vector2 p2){
        if(placement(p1)==PlacementOnBitExtremity.Top || placement(p2)==PlacementOnBitExtremity.Top ) return true;

        return false;
    }
    private boolean BottomCornerCutting(Vector2 p1,Vector2 p2){
        if(placement(p1)==PlacementOnBitExtremity.Bottom || placement(p2)==PlacementOnBitExtremity.Bottom ) return true;
        return false;
    }

    private Area updateArea(Area remainingArea,Area fallArea){
        remainingArea.subtract(fallArea);
        return remainingArea;
    }


    /**
     * checks if a point is on the edge of the bit
     * @param point
     * @return true if it's the case false if not
     */
  private boolean checkpointOnExtremity(Vector2 point){
     if(point.x<=-CraftConfig.lengthFull/2 || point.y>=CraftConfig.bitWidth/2
             || point.y<=-CraftConfig.bitWidth/2)  return true;
     else { return false;}
  }

    /**
     * @param p1 first point
     * @param p2 last point
     *  this method checks if the first and last point are on the opposite parallel side of the bit(
     *  exemple:one point on top and the other on the bottom).
     * @return true if on opposite sides, false if not
     */
    private boolean oppositePlacement(Vector2 p1,Vector2 p2){
       if((placement(p1)==PlacementOnBitExtremity.Bottom && (placement(p2)==PlacementOnBitExtremity.Top ||placement(p2)==PlacementOnBitExtremity.Topcorner) )
               ||(placement(p1)==PlacementOnBitExtremity.Top && (placement(p2)==PlacementOnBitExtremity.Bottom || placement(p2)==PlacementOnBitExtremity.Bottomcorner) )) return true;

        return false;
}

    /**
     * checks if 2 points are on the same edge of the bit
     * @param p1 point1
     * @param p2 point2
     * @return true if it's the case, false if not
     */
    private boolean samePlacement(Vector2 p1,Vector2 p2){
        if(   placement(p1)==placement(p2) || (placement(p1)==PlacementOnBitExtremity.Topcorner &&( placement(p2) ==PlacementOnBitExtremity.Bottomcorner
                ||placement(p2) ==PlacementOnBitExtremity.Top || placement(p2) ==PlacementOnBitExtremity.Left ))
                || (placement(p1)==PlacementOnBitExtremity.Bottomcorner && (placement(p2) ==PlacementOnBitExtremity.Topcorner
                ||placement(p2) ==PlacementOnBitExtremity.Bottom ||placement(p2) ==PlacementOnBitExtremity.Left )) ) return true;
        return false;
    }


    /**
     * @param point
     * @return the placement of the point on the bit
     */
    private PlacementOnBitExtremity placement(Vector2 point){
      if(point.y>=CraftConfig.bitWidth/2 && point.x<=-CraftConfig.lengthFull/2 )  return PlacementOnBitExtremity.Bottomcorner;
          else if(point.y<=-CraftConfig.bitWidth/2 && point.x<=-CraftConfig.lengthFull/2  )  return PlacementOnBitExtremity.Topcorner;
      
     else if (point.y>=CraftConfig.bitWidth/2) return PlacementOnBitExtremity.Bottom;
        else if (point.y<=-CraftConfig.bitWidth/2) {return PlacementOnBitExtremity.Top;

        }else if (point.x<=-CraftConfig.lengthFull/2){return PlacementOnBitExtremity.Left;

        }
  return null;
    }

}
