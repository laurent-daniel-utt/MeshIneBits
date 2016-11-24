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
	public final Vector2 origin;
	@SuppressWarnings("unused")
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
		
		transfoMatrix.translate(origin.x, origin.y);
		transfoMatrix.rotate(orientation.x, orientation.y);
		
		try {
			inverseTransfoMatrix = ((AffineTransform) transfoMatrix.clone()).createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		
		buildBoundaries();
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
