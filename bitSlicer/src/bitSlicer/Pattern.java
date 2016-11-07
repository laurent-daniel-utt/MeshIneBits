package bitSlicer;

import java.util.Vector;

import bitSlicer.PatternTemplates.PatternTemplate;
import bitSlicer.Slicer.Slice;
import bitSlicer.Bit2D;

public class Pattern {
	
	private Vector<Bit2D> bits;
	
	public Pattern(Slice slice, PatternTemplate patternTemplate, int layerNumber){
		
		bits = patternTemplate.createPattern(layerNumber);

		computeBits(slice);
		
	}
	
	/*
	 * Remove the bits that are outside the part
	 * Compute the cut line for the bits on the boundaries of the part
	 */
	private void computeBits(Slice slice){
		//TODO
	}
	
	//If it is only used to show it up, maybe it would be more simple to only return the boundaries in the correct coordinate system?
	public Vector<Bit2D> getBits(){
		
		Vector<Bit2D> bitsInLocalCooSystem = new Vector<Bit2D>();
		for (Bit2D bit : bits){
			bitsInLocalCooSystem.add(getInLocalCooSystem(bit));
		}
		
		return bitsInLocalCooSystem; 
	}
	
	private Bit2D getInLocalCooSystem(Bit2D bit){
		//TODO
		return null;
	}
}
