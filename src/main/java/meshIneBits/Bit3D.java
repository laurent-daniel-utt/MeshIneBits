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
 * Copyright (C) 2020 CLAIRIS Etienne & RUSSO André.
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

package meshIneBits;

import meshIneBits.config.CraftConfig;
import meshIneBits.util.AreaTool;
import meshIneBits.util.CutPathUtil;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A bit 3D is the equivalent of a real wood bit. The 3D shape is determined by
 * extrusion of a {@link Bit2D}
 */
public class Bit3D implements Serializable, Cloneable {
    private static final String TAG = "Bit3D";
    /**
     * In {@link #bit2dToExtrude} coordinate system
     */
    private Vector<Path2D> rawCutPaths;
    private LinkedList<Vector<Path2D>> rawCutPathsSeparate;
    /**
     * In {@link Mesh} coordinate system
     */
    private Vector2 origin;
    /**
     * In {@link Mesh} coordinate system
     */
    private Vector2 orientation;
    private Bit2D bit2dToExtrude;
    /**
     * In {@link #bit2dToExtrude} coordinate system
     */
    private Vector<Vector2> rawLiftPoints = new Vector<>();
    /**
     * In {@link Mesh} coordinate system
     */
    private Vector<Vector2> liftPoints = new Vector<>();
    /**
     * List two points correspond  areas in key
     * In {@link #bit2dToExtrude} coordinate system
     */
    private Vector<Vector<Vector2>>listTwoDistantPoints =new Vector<>();

    public Vector<Double> getListAngles() {
//        if(listAngles.size()==0) throw new NullPointerException("List of angles is not calculated");
        return listAngles;
    }

    /**
     * contain angles made by two distants points
     */
    private Vector<Double> listAngles=new Vector<>();

    private boolean irregular = false;
    private double lowerAltitude;
    private double higherAltitude;

    private boolean reverseInCut;

    public boolean isReverseInCut() {
        return reverseInCut;
    }

    /**
     * Construct bit 3D from horizontal section and calculate lift points
     *
     * @param baseBit horizontal cut
     * @param layer in which this bit resides
     */
    Bit3D(Bit2D baseBit, Layer layer) {
        bit2dToExtrude = baseBit;
        origin = baseBit.getOrigin();
        orientation = baseBit.getOrientation();
        rawCutPaths = baseBit.getRawCutPaths();
        reverseInCut=baseBit.getInverseInCut();
        computeLiftPoints();
        computeTwoPointNearTwoPointMostDistantOnBit();
        lowerAltitude = layer.getLowerAltitude();
        higherAltitude = layer.getHigherAltitude();
    }
    Bit3D(Bit3D bit3D){
        bit2dToExtrude = bit3D.getBaseBit();
        origin = bit3D.getOrigin();
        orientation = bit3D.getOrientation();
        rawCutPaths = (Vector<Path2D>) bit3D.getRawCutPaths();
        computeLiftPoints();
        computeTwoPointNearTwoPointMostDistantOnBit();
        lowerAltitude = bit3D.getLowerAltitude();
        higherAltitude = bit3D.getHigherAltitude();
        reverseInCut=bit3D.isReverseInCut();
    }

    private Vector2 computeLiftPoint(Area subBit) {
//        System.out.println(TAG+"computeLiftPoint");
        return AreaTool.getLiftPoint(subBit, CraftConfig.suckerDiameter / 2);
    }

    /**
     * Calculate the lift point, which is the best position to grip
     * the bit by vacuuming.
     */
    private void computeLiftPoints() {
//        System.out.println(TAG+"computeLiftPoint void");
        for (Area subBit : bit2dToExtrude.getRawAreas()) {
            Vector2 liftPoint = computeLiftPoint(subBit);
            if (liftPoint != null) {
                rawLiftPoints.add(liftPoint);
                liftPoints.add(liftPoint.getTransformed(bit2dToExtrude.getTransfoMatrix()));
            } else {
                irregular = true;
            }
        }
    }

    public List<Path2D> getRawCutPaths() {
        return rawCutPaths;
    }

    public List<Vector2> getLiftPoints() {
        return liftPoints;
    }

    public List<Vector2> getRawLiftPoints() {
        return rawLiftPoints;
    }

    public Vector2 getOrientation() {
        return orientation;
    }

    public Vector2 getOrigin() {
        return origin;
    }

    public Area getRawArea() {
        return bit2dToExtrude.getRawArea();
    }
    public List<Area> getRawAreas() {
        return bit2dToExtrude.getRawAreas();
    }

    public Bit2D getBaseBit() {
        return bit2dToExtrude;
    }

    public boolean isIrregular() {
        return irregular;
    }

    @Override
    public String toString() {
        Rectangle2D bound = bit2dToExtrude.getArea().getBounds2D();
        return "Bit3D[" +
                "origin=" + origin +
                ", orientation=" + orientation +
                ", width=" + bit2dToExtrude.getLength() +
                ", height=" + bit2dToExtrude.getWidth() +
                ", liftPoints=" + liftPoints +
                ", irregular=" + irregular +
                ", areaBound=" +
                "[x=" + bound.getX() +
                ", y=" + bound.getY() +
                ", w=" + bound.getWidth() +
                ", h=" + bound.getHeight() +
                "]" +
                ']';
    }
    public boolean isCutable() {
        return this.getRawLiftPoints().size() > 0;
    }

    public void computeTwoPointNearTwoPointMostDistantOnBit(){
        for (Area area : bit2dToExtrude.getRawAreas()){
            Vector<Vector2> listPoint = AreaTool.defineTwoPointNearTwoMostDistantPointsInAreaWithRadius(area,CraftConfig.suckerDiameter/4);
            if (listPoint != null) {
                listTwoDistantPoints.add(listPoint);
            }
        }
    }

    public double getLowerAltitude() {
        return lowerAltitude;
    }

    public double getHigherAltitude() {
        return higherAltitude;
    }
    @SuppressWarnings("super")
    public Bit3D clone() {
        return new Bit3D(this);
    }

    public LinkedList<Vector<Path2D>> getRawCutPathsSeparate() {
        return rawCutPathsSeparate;
    }

    /**
     * Return list of distant points in {@link Mesh} coordinate.
     * @return list of distant points in {@link Mesh} coordinate
     */
    public Vector<Vector2> getTwoDistantPointsInMeshCoordinate() {
        Vector<Vector2> listPoints = new Vector<>();
        listTwoDistantPoints.forEach(list -> list.forEach(ele->listPoints.add(ele.getTransformed(bit2dToExtrude.getTransfoMatrix()))));
        return listPoints;
    }
    public Vector<Vector2> getTwoDistantPoints() {
        Vector<Vector2> listPoints = new Vector<>();
        listTwoDistantPoints.forEach(list -> list.forEach(ele->listPoints.add(ele)));
        return listPoints;
    }
    private void calcAngles(){
        if(listTwoDistantPoints.size()>0)
        listTwoDistantPoints.forEach(twoDistantPoints->{
                    if(twoDistantPoints.size()>0){
                        listAngles.add(
                                calculateAngleOfTwoPoint(twoDistantPoints.firstElement(),twoDistantPoints.lastElement()));
                    }else listAngles.add(null);
                }
            );
    }
    public Vector<Vector<Vector2>> getListTwoDistantPoints(){
        return listTwoDistantPoints;
    }

    public void prepareBitToExport(){
//        Bit3D bit3D = this;
//        bit3D.computeTwoPointNearTwoPointMostDistantOnBit();
        calcAngles();
        if(reverseInCut) this.inverse();

    }

    private void inverse() {
        inverseCutPath();
        inverseLiftPoint();
        inverseDistantPoints();
    }

    private void inverseLiftPoint() {
        AffineTransform matrixReverseBit = new AffineTransform();
        matrixReverseBit.rotate(Math.PI);
        Vector<Vector2> rawLiftPoints = new Vector<>(this.rawLiftPoints);
        this.rawLiftPoints.clear();
        Vector<Vector2> liftPoints = new Vector<>(this.liftPoints);
        this.liftPoints.clear();
        for(int i=rawLiftPoints.size()-1;i>=0;i--){
            this.rawLiftPoints.add(rawLiftPoints.get(i).getTransformed(matrixReverseBit));
            this.liftPoints.add(liftPoints.get(i));
        }
    }


    private void inverseDistantPoints(){
        AffineTransform matrixReverseBit = new AffineTransform();
        matrixReverseBit.rotate(Math.PI);
        //
        Vector<Vector<Vector2>> listTwoDistantPoints = new Vector<>(this.listTwoDistantPoints);
        //reverse angle array to correspond with two distants points array
        Collections.reverse(this.listAngles);
        this.listTwoDistantPoints.clear();
        for(int i = listTwoDistantPoints.size()-1;i>=0;i--){
//            System.out.println(listTwoDistantPoints.get(i).firstElement().toString()+"------"+listTwoDistantPoints.get(i).lastElement());
            List<Vector2> list=listTwoDistantPoints.get(i).stream().map(ele->ele.getTransformed(matrixReverseBit)).collect(Collectors.toList());
//            System.out.println(list.get(0).toString()+"------"+list.get(1).toString());
            this.listTwoDistantPoints.add(new Vector<>(list));


        }
    }

    public void inverseCutPath(){
        AffineTransform matrixReverseBit = new AffineTransform();
        matrixReverseBit.rotate(Math.PI);
        Vector<Path2D> cutPaths = new Vector<>(this.getRawCutPaths());
        this.getRawCutPaths().clear();
        for(int i = cutPaths.size()-1;i>=0;i--){
            this.getRawCutPaths().add(CutPathUtil.transformPath2D(cutPaths.get(i),matrixReverseBit));
        }
    }
    public boolean isHoldedInCUt(){
        Area bitArea = this.getRawArea();
        Vector<Rectangle2D> twoSide = Bit2D.getTwoSideOfBit(CraftConfig.incertitude);
        return reverseInCut ? bitArea.contains(twoSide.firstElement()) : bitArea.contains(twoSide.lastElement());
    }
    public boolean checkIfLastCutPath(@NotNull Path2D path2D){
        return rawCutPaths.lastElement()==path2D;
    }
    /**
     * This method return the angle of the line connecting two point and the X axis (0; 1)
     * Only use to calculate angle between 2 point for file exported
     * @param point1
     * @param point2
     * @return
     */
    private static double calculateAngleOfTwoPoint(Vector2 point1,Vector2 point2){
        Vector2 vectorResult = new Vector2(point2.x-point1.x,point2.y-point1.y);
        return Vector2.calcAngleBetweenVectorAndAxeX(vectorResult);
    }


    /**
     *
     */
    public ArrayList<Double> getMinAndMaxXDistantPoint(){
        Vector<Vector2> allDistancePoints = this.getTwoDistantPointsInMeshCoordinate();

        ArrayList<Double> xPositions= new ArrayList<Double>();

        if (!allDistancePoints.isEmpty()){
            double minXDistancePoint=allDistancePoints.get(0).x;
            double maxXDistancePoint=allDistancePoints.get(0).x;
            for (int i=0; i< allDistancePoints.size();i++){
                if (allDistancePoints.get(i).x<minXDistancePoint){
                    minXDistancePoint=allDistancePoints.get(i).x;
                }
                if (allDistancePoints.get(i).x>maxXDistancePoint){
                    maxXDistancePoint=allDistancePoints.get(i).x;
                }
            }
            xPositions.add(minXDistancePoint);
            xPositions.add(maxXDistancePoint);
        }
        return xPositions;
    }
}
