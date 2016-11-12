package bitSlicer;

import java.util.Vector;

import bitSlicer.Slicer.Slice;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.Shape2D;
import bitSlicer.util.Vector2;
import bitSlicer.util.Polygon;
import bitSlicer.util.Segment2D;;

/**
 * Bit2D represents a bit in 2d space.
 */
public class Bit2D extends Shape2D {
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
		
		Segment2D top = new Segment2D(1, cornerUpLeft, cornerUpRight);
		Segment2D right = new Segment2D(1, cornerUpRight, cornerDownRight);
		Segment2D bottom = new Segment2D(1, cornerDownRight, cornerDownLeft);
		Segment2D left = new Segment2D(1, cornerDownLeft, cornerUpLeft);
		
		top.setNext(right);
		right.setNext(bottom);
		bottom.setNext(left);
		left.setNext(top);
		
		addModelSegment(top);
		addModelSegment(right);
		addModelSegment(bottom);
		addModelSegment(left);
		
		
		// Link up the segments with start/ends, so polygons are created.
		this.optimize();
	}
	
	public void setBitInPatternCooSystem(){
		this.setInLowerCooSystem(orientation, origin);
	}
	
	public void setInThatCooSystem(Vector2 rotation, Vector2 offSet){
		setInLowerCooSystem(rotation, offSet);
		origin = origin.getInLowerCooSystem(rotation, offSet);
	}
	
	/**
	 * Check if the bit is on the outline of the slice
	 */
	public boolean isOnPath(Slice slice) {
		for(Polygon p : this){
			for (Segment2D b : p) {
				for (Segment2D s : slice.getSegmentList()) {
					if (b.getCollisionPoint(s) != null)
						return true;
				}
			}
		}
		return false;	
	}
	
	/**
	 * Check if the bit is inside boundaries of the slice
	 */
	public boolean isInsideShape(Slice slice) {
		return slice.contains(this.origin);
	}
	
	/**
	 * WIP - Return the a cut bit without the excess of material outside the slice
	 * TODO update this
	 */
	public Shape2D getTruncatedBitShape(Slice slice) {
		return this.trim(slice);
	}
}
