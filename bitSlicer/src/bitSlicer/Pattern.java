package bitSlicer;

import java.util.Vector;

import bitSlicer.PatternTemplates.PatternTemplate;
import bitSlicer.Slicer.Slice;

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
		
	}
}
