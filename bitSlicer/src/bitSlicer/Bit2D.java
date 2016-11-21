package bitSlicer;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Vector;

import bitSlicer.Slicer.Slice;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.Shape2D;
import bitSlicer.util.Vector2;
import bitSlicer.util.AABBTree;
import bitSlicer.util.AreaTool;
import bitSlicer.util.Polygon;
import bitSlicer.util.Segment2D;;

/**
 * Bit2D represents a bit in 2d space.
 */
public class Bit2D extends Area {
	public Vector2 origin;
	private Vector2 orientation;
	private double length;
	private double width;
	
	/*
	 * originBit and orientation are in the coordinate system of the associated pattern 
	 */
	public Bit2D(Vector2 origin, Vector2 orientation){
		this.origin = origin;
		this.orientation = orientation;
		length = CraftConfig.bitLength;
		width = CraftConfig.bitWidth;
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

		this.add(new Area(path));
	}
	
	/**
	 * Check if the bit is on the outline of the slice
	 */
	public boolean isOnPath(Slice slice) {
		return (AreaTool.getAreaFrom(slice).intersects(this.getBounds()));
	}
	
	/**
	 * Check if the bit is inside boundaries of the slice
	 */
	public boolean isInsideShape(Slice slice, double skirtRadius) {
		if(this.origin.vSize() > skirtRadius)//Optimize the speed of that method by avoiding a contains() if the bit is obviously outside
			return false;
		else 
			return slice.contains(this.origin);
	}
	
	public Area getArea(Slice slice) {
		Area truncatedArea = (Area) this.clone();
		truncatedArea.intersect(AreaTool.getAreaFrom(slice));
		return truncatedArea;
	}
}
