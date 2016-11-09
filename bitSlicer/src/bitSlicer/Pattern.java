package bitSlicer;

import java.util.Vector;

import bitSlicer.PatternTemplates.PatternTemplate;
import bitSlicer.Slicer.Slice;
import bitSlicer.util.Shape2D;
import bitSlicer.util.Vector2;

public class Pattern {
	
	private Vector<Bit2D> bits;
	private Vector2 rotation;
	
	public Pattern(Vector<Bit2D> bits, Vector2 rotation){

		this.bits = bits;
		this.rotation = rotation;
		
	}
	
	/*
	 * Remove the bits that are outside the part
	 * Compute the cut line for the bits on the boundaries of the part
	 */
	public void computeBits(Slice slice){
		//TODO
	}
	
	public Vector<Shape2D> getBitsShape(){
		//return a vector of the shape2D of the bits in the coo system of the layer which includes this pattern 
		//TODO
		return null;
	}
}
