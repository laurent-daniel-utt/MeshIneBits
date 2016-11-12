package bitSlicer;

import bitSlicer.util.Logger;
import bitSlicer.util.Shape2D;
import bitSlicer.util.Vector2;
import bitSlicer.Slicer.Slice;
import bitSlicer.Slicer.Config.CraftConfig;

import java.util.Vector;

import bitSlicer.Pattern;

public class Layer extends Shape2D {
	
	private int layerNumber;
	private Vector<Slice> slices;
	private Vector<Pattern> patterns = new Vector<Pattern>();
	
	public Layer(Vector<Slice> slices, int layerNumber, GeneratedPart generatedPart){
		this.slices = slices;
		this.layerNumber = layerNumber;
		
		for (Slice s : slices){
			patterns.add(generatedPart.getPatternTemplate().createPattern(layerNumber));
			patterns.lastElement().setToLayerCooSystem();
			patterns.lastElement().computeBits(s);
			//System.out.println("Layer " + layerNumber + " : + 1 slice");
		}
	}

	public Vector<Slice> getSlices() {
		return this.slices;
	}
	
	public Vector<Pattern> getPatterns(){
		return this.patterns;
	}
}
