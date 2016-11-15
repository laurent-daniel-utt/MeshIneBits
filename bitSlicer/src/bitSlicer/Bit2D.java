package bitSlicer;

import java.util.Vector;

import bitSlicer.Slicer.Slice;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.Shape2D;
import bitSlicer.util.Vector2;
import bitSlicer.util.AABBTree;
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
		
		//We could just do a optimize() but we save a lot of time by doing it like that
		top.setNext(right);
		right.setNext(bottom);
		bottom.setNext(left);
		left.setNext(top);
		
		addModelSegment(top);
		addModelSegment(right);
		addModelSegment(bottom);
		addModelSegment(left);
		
		polygons.add(new Polygon(top));
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
					if (b.getCollisionPoint(s) != null) return true;
				}
			}
		}
		return false;	
	}
	
	public void setNewBoundaries(Shape2D slice){
		Vector<Segment2D> newBoundariesSegment = new Vector<Segment2D>();
		for (Segment2D s : this.trim(slice).getSegmentList()){
			newBoundariesSegment.add(new Segment2D(1, s.start, s.end)); // We can't add directly "s" because we need to erase any prev/next segment previously stored
		}

		polygons.clear();
		segmentList.clear();
		segmentTree = new AABBTree<Segment2D>();
		
		Vector<Segment2D> newSegments = new Vector<Segment2D>();
		newSegments = optimizeDirections(newBoundariesSegment);
		
		for(Segment2D s : newSegments){
			this.addModelSegment(s);
			System.out.println(s);
		}
		
		optimize();
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
	
	/**
	 * WIP - Return the a cut bit without the excess of material outside the slice
	 * TODO update this
	 */
	public Shape2D getTruncatedBitShape(Slice slice) {
		return this.trim(slice);
	}
}
