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
public class Bit2D implements Cloneable{
	private Vector2 origin;// in the pattern coordinate system
	private Vector2 orientation;// around the bit origin
	private double length;
	private double width;
	private AffineTransform transfoMatrix = new AffineTransform();
	private AffineTransform inverseTransfoMatrix;
	private Vector<Path2D> cutPaths = null;
	private Vector<Area> areas = new Vector<Area>();
	
	@Override
	public Bit2D clone(){		
		return new Bit2D(origin, orientation);
	}
	
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
	
	public Bit2D(Vector2 origin, Vector2 orientation, double percentageLength, double percentageWidth){
		this.origin = origin;
		this.orientation = orientation;
		length = CraftConfig.bitLength * percentageLength / 100;
		width = CraftConfig.bitWidth * percentageWidth / 100;
		
		setTransfoMatrix();
		buildBoundaries();
	}
	
	public Bit2D(Bit2D modelBit){
		this.origin = modelBit.origin;
		this.orientation = modelBit.orientation;
		length = CraftConfig.bitLength;
		width = CraftConfig.bitWidth;
		
		setTransfoMatrix();
		buildBoundaries();
	}
	
	public Bit2D(Bit2D modelBit, double percentageLength, double percentageWidth){
		this.origin = modelBit.origin;
		this.orientation = modelBit.orientation;
		length = CraftConfig.bitLength * percentageLength / 100;
		width = CraftConfig.bitWidth * percentageWidth / 100;
		
		setTransfoMatrix();
		buildBoundaries();
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
		Vector2 cornerUpRight = new Vector2(+ CraftConfig.bitLength/2.0,  - CraftConfig.bitWidth/2.0);
		Vector2 cornerDownRight = new Vector2(cornerUpRight.x, cornerUpRight.y + width);
		Vector2 cornerUpLeft = new Vector2(cornerUpRight.x - length, cornerUpRight.y);
		Vector2 cornerDownLeft = new Vector2(cornerDownRight.x - length, cornerDownRight.y);
		       
		
		Path2D path = new Path2D.Double();
		path.moveTo(cornerUpLeft.x, cornerUpLeft.y);			
		path.lineTo(cornerUpRight.x, cornerUpRight.y);
		path.lineTo(cornerDownRight.x, cornerDownRight.y);
		path.lineTo(cornerDownLeft.x, cornerDownLeft.y);
		path.closePath();

		this.areas.add(new Area(path));
		
		if(length != CraftConfig.bitLength){
			cutPaths = new Vector<Path2D>();
			Path2D.Double cutPath = new Path2D.Double();
			cutPath.moveTo(cornerUpLeft.x, cornerUpLeft.y);			
			cutPath.lineTo(cornerDownLeft.x, cornerDownLeft.y);
			this.cutPaths.add(cutPath);	
		}
		
		if(width != CraftConfig.bitWidth){
			if(cutPaths == null)
				cutPaths = new Vector<Path2D>();
			Path2D.Double cutPath = new Path2D.Double();
			cutPath.moveTo(cornerUpLeft.x, cornerUpLeft.y);			
			cutPath.lineTo(cornerUpRight.x, cornerUpRight.y);
			this.cutPaths.add(cutPath);	
		}
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
	
	public Vector<Path2D> getRawCutPaths(){
		return cutPaths;
	}
	
	public Vector2 getOrientation(){
		return orientation;
	}
	
	public Area getRawArea(){
		Area area = new Area();
		for(Area a : areas)
			area.add(a);
		return area;
	}
	
	public Vector<Area> getRawAreas(){
		return areas;
	}
}
