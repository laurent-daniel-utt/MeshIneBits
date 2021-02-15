/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2021 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020 CLARIS Etienne & RUSSO André.
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
 */

package meshIneBits.util;

import remixlab.dandelion.geom.Vec;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Vector;

public class CutPathUtil {
    /**
     * This method is used to order the position of the {@link Path2D} in the {@link Vector list}
     * @param cutPaths {@link Vector list} of cut paths
     */
    public static void sortCutPath(Vector<Path2D> cutPaths) {
        cutPaths.sort((path1, path2) -> {
            Path2D path1Clone = (Path2D) path1.clone();
            Point2D currentPoint1Start = path1Clone.getCurrentPoint();
            path1Clone.closePath();
            Point2D currentPoint1End = path1Clone.getCurrentPoint();
            Path2D path2Clone = (Path2D) path2.clone();
            Point2D currentPoint2Start = path2Clone.getCurrentPoint();
            path2Clone.closePath();
            Point2D currentPoint2End = path2Clone.getCurrentPoint();
            if ((currentPoint1Start.getX() < currentPoint2Start.getX() || currentPoint1Start.getX() < currentPoint2End.getX())
                    && (currentPoint1End.getX() < currentPoint2Start.getX() || currentPoint1End.getX() < currentPoint2End.getX())) {
                return -1;
            }else if((currentPoint2Start.getX() < currentPoint1Start.getX() || currentPoint2Start.getX() < currentPoint1End.getX())
                    && (currentPoint2End.getX() < currentPoint1Start.getX() || currentPoint2End.getX() < currentPoint1End.getX())){
                return 1;
            }else return 0;

        });
    }

    public static Path2D OrganizeOrderCutInPath2D(Path2D path2D) {
        PathIterator iterator = path2D.getPathIterator(null);
        Vector<Path2D> listCutPaths = new Vector<>();
        Path2D currentPath = new Path2D.Double();
        for (PathIterator pi = iterator; !pi.isDone(); pi.next()) {
            double[] coord = new double[2];
            int type = pi.currentSegment(coord);
            if (type == PathIterator.SEG_MOVETO) {
                if (currentPath.getCurrentPoint() != null) {

                    currentPath=organizePath(currentPath);
                    listCutPaths.add(currentPath);
                }

                currentPath = new Path2D.Double();
                currentPath.moveTo(coord[0], coord[1]);
            } else if (type == PathIterator.SEG_LINETO) {
                currentPath.lineTo(coord[0], coord[1]);
            }
        }
        if (!listCutPaths.contains(currentPath)) {
            currentPath=organizePath(currentPath);
            listCutPaths.add(currentPath);
        }
        sortCutPath(listCutPaths);
        currentPath = new Path2D.Double();

        for(int i=0;i<listCutPaths.size();i++){
            if(i==0){
                currentPath.append(listCutPaths.get(0), false);
                continue;
            }
            Path2D pathPrevious = listCutPaths.get(i-1);
            Path2D currentPathClone = (Path2D) listCutPaths.get(i).clone();
            currentPathClone.closePath();
            if(pathPrevious.getCurrentPoint().getX()==currentPathClone.getCurrentPoint().getX()
                    &&pathPrevious.getCurrentPoint().getY()==currentPathClone.getCurrentPoint().getY()){
                currentPath.append(listCutPaths.get(i),true);
            }else currentPath.append(listCutPaths.get(i),false);
        }
        return currentPath;

    }
    private static Path2D organizePath(Path2D path){
        int countMoveTo =0;
        Point2D currentPoint = path.getCurrentPoint();
        Path2D result=new Path2D.Double();
        Vector<double[]> list = new Vector<>();
        for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) {
            double[] coord = new double[2];
            int type = pi.currentSegment(coord);
            if (type == PathIterator.SEG_MOVETO) {
                if(currentPoint.getX()>coord[0]) return path;
                countMoveTo++;
            }
            list.add(coord);
            if(countMoveTo>1) throw new IllegalArgumentException("path argument must have just one Move To");
        }
        for(int i=list.size()-1;i>=0;i--){
            if(i==list.size()-1)result.moveTo(list.get(i)[0],list.get(i)[1]);
            else{
                result.lineTo(list.get(i)[0],list.get(i)[1]);
            }
        }
        return result;

    }
    public static  Path2D transformPath2D(Path2D path2D, AffineTransform affineTransform){
        Path2D result = (Path2D) path2D.clone();
        result.transform(affineTransform);
        return OrganizeOrderCutInPath2D(result);
//        for(PathIterator pi = path2D.getPathIterator(null);!pi.isDone();pi.next()){
//            double[] coord = new double[2];
//            int type = pi.currentSegment(coord);
//            if(type==PathIterator.SEG_MOVETO){
//                result.moveTo(-coord[0]);
//            }
//        }

    }
//    private int compare(double[] tab1,double[] tab2){
//        if(tab1.length==2&&tab2.length==2){
//
//        }else{
//            throw new IllegalArgumentException("argument must have 2 element");
//        }
//    }
}
