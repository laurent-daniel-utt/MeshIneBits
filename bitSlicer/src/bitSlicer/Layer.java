package bitSlicer;

import bitSlicer.util.Logger;
import bitSlicer.util.Shape2D;
import bitSlicer.Slicer.Slice;

import java.util.Vector;

import bitSlicer.Pattern;

public class Layer extends Shape2D {
	
	private int layerNumber;
	private Vector<Slice> slices;
	private Vector<Pattern> patterns = new Vector<Pattern>();
	
	public Layer(Vector<Slice> slices, int layerNumber, GeneratedPart generatedPart){
		this.slices = slices;
		this.layerNumber = layerNumber;
		
		Logger.updateStatus("Generating patterns");
		Pattern pattern = generatedPart.getPatternTemplate().createPattern(layerNumber);
		
		Logger.updateStatus("Adding pattern to slices");
		for (Slice s : slices){
			patterns.add(pattern);
			patterns.lastElement().computeBits(s);
		}
	}

	public Vector<Slice> getSlices() {
		return this.slices;
	}
	
	public Vector<Pattern> getPatterns() {
		return this.patterns;
	}
}
