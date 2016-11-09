package bitSlicer;

import java.util.Vector;

import bitSlicer.Slicer.Slice;
import bitSlicer.util.Shape2D;
import bitSlicer.util.Vector2;
import bitSlicer.util.Segment2D;;

/**
 * Bit2D represents a bit in 2d space.
 */
public class Bit2D extends Shape2D {
	private Vector2 origin;
	private Vector2 orientation;
	
	/*
	 * originBit and orientation are in the coordinate system of the associated pattern 
	 */
	public Bit2D(Vector2 origin, Vector2 orientation){
		this.origin = origin;
		this.orientation = orientation;
	}
	
	/**
	 * Check if the bit is on the outline of the slice
	 */
	public boolean isOnPath(Slice slice) {
		for (Segment2D b : this.getSegmentList()) {
			for (Segment2D s : slice.getSegmentList()) {
				if (b.getCollisionPoint(s) != null)
					return true;
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
