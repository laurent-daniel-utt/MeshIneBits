package bitSlicer;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.util.Vector;

import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.AreaTool;
import bitSlicer.util.Vector2;

/**
 * Bit2D represents a bit in 2d space.
 */
public class Bit2D {
	private Vector2 origin;
	private Vector2 orientation;
	private double length;
	private double width;
	private AffineTransform transfoMatrix = new AffineTransform();
	private AffineTransform inverseTransfoMatrix;
	private Vector<Path2D> cutPaths = null;
	private Vector<Area> areas = new Vector<Area>();
	
	/*
	 * originBit and orientation are in the coordinate system of the associated pattern 
	 */
	public Bit2D(Vector2 origin, Vector2 orientation){
		this.origin = origin;
		this.orientation = orientation;
		length = CraftConfig.bitLength;
		width = CraftConfig.bitWidth;
		
		setTransfoMatrix();
		buildBoundaries();
	}
	
	public Bit2D(Bit2D cutBit){
		this.origin = cutBit.origin;
		this.orientation = cutBit.orientation;
		length = CraftConfig.bitLength;
		width = CraftConfig.bitWidth;
		
		setTransfoMatrix();
		buildBoundaries();
	}
	
	public Bit2D(Bit2D cutBit, double percentageLength){
		this.origin = cutBit.origin;
		this.orientation = cutBit.orientation;
		length = CraftConfig.bitLength * percentageLength / 100;
		width = CraftConfig.bitWidth;
		
		setTransfoMatrix();
		buildReducedBoundaries();
	}
	
	private void setTransfoMatrix(){
		transfoMatrix.translate(origin.x, origin.y);
		transfoMatrix.rotate(orientation.x, orientation.y);
		
		try {
			inverseTransfoMatrix = ((AffineTransform) transfoMatrix.clone()).createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
	}
	
	private void buildBoundaries(){
		Vector2 cornerUpLeft = new Vector2(- length/2.0,  - width/2.0);
		Vector2 cornerUpRight = new Vector2(+ length/2.0,  - width/2.0);
		Vector2 cornerDownLeft = new Vector2(- length/2.0,  + width/2.0);
		Vector2 cornerDownRight = new Vector2(+ length/2.0,  + width/2.0);       
		
		Path2D path = new Path2D.Double();
		path.moveTo(cornerUpLeft.x, cornerUpLeft.y);			
		path.lineTo(cornerUpRight.x, cornerUpRight.y);
		path.lineTo(cornerDownRight.x, cornerDownRight.y);
		path.lineTo(cornerDownLeft.x, cornerDownLeft.y);
		path.closePath();

		this.areas.add(new Area(path));
	}
	
	private void buildReducedBoundaries(){
		Vector2 cornerUpLeft = new Vector2(- length/2.0,  - width/2.0);
		Vector2 cornerUpRight = new Vector2(+ length/2.0,  - width/2.0);
		Vector2 cornerDownLeft = new Vector2(- length/2.0,  + width/2.0);
		Vector2 cornerDownRight = new Vector2(+ length/2.0,  + width/2.0);       
		
		Path2D path = new Path2D.Double();
		path.moveTo(cornerUpLeft.x, cornerUpLeft.y);			
		path.lineTo(cornerUpRight.x, cornerUpRight.y);
		path.lineTo(cornerDownRight.x, cornerDownRight.y);
		path.lineTo(cornerDownLeft.x, cornerDownLeft.y);
		path.closePath();

		this.areas.add(new Area(path));
		
		cutPaths = new Vector<Path2D>();
		Path2D cutPath = new Path2D.Double();
		path.moveTo(cornerUpLeft.x, cornerUpLeft.y);			
		path.lineTo(cornerDownLeft.x, cornerDownLeft.y);
		this.cutPaths.add(cutPath);
		System.out.println(cutPaths.get(0));
		
	}
	
	public Area getArea(){
		Area transformedArea = new Area();
		for(Area a : areas)
			transformedArea.add(a);
		transformedArea.transform(transfoMatrix);
		return transformedArea;
	}
	
	public Vector<Area> getAreas(){
		Vector<Area> result = new Vector<Area>();
		for(Area a : areas){
			Area transformedArea = new Area(a);
			transformedArea.transform(transfoMatrix);
			result.add(transformedArea);
		}
		return result;
	}
	
	public Vector2 getOrigin(){
		return origin;
	}
	
	public void updateBoundaries(Area transformedArea){
		areas.clear();
		Area newArea = (Area) transformedArea.clone();
		newArea.transform(inverseTransfoMatrix);
		for(Area a : AreaTool.segregateArea(newArea))
			areas.add(a);
	}
	
	public void setCutPath(Vector<Path2D> paths){
		if(this.cutPaths == null)
			this.cutPaths = new Vector<Path2D>();
		
		for(Path2D p : paths)
			this.cutPaths.add(new Path2D.Double(p, inverseTransfoMatrix));			
	}
	
	public Vector<Path2D> getCutPaths(){
		if (this.cutPaths == null)
			return null;
		else{
			Vector<Path2D> paths = new Vector<Path2D>();
			for(Path2D p : this.cutPaths)
				paths.add(new Path2D.Double(p, transfoMatrix));
			return paths;
		}
	}
}
