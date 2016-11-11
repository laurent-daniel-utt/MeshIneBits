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

		Pattern pattern = generatedPart.getPatternTemplate().createPattern(layerNumber);
		
		for (Slice s : slices){
			patterns.add(pattern);
			//patterns.lastElement().computeBits(s);
		}
	}

	public Vector<Slice> getSlices() {
		return this.slices;
	}
	
	public Vector<Shape2D> getPatterns() {
		Vector<Shape2D> result = new Vector<Shape2D>();
		for (Shape2D s : patterns.get(0).getBitsShape()){
			result.add(s.getInLowerCooSystem(CraftConfig.rotation, new Vector2(0,0)));
		}
		return result;
	}
}
