package bitSlicer;

import bitSlicer.util.Shape2D;
import bitSlicer.Slicer.Slice;

import java.util.Vector;

import bitSlicer.Pattern;

public class Layer extends Shape2D {
	
	private int layerNumber;
	private Vector<Slice> slices;
	private Vector<Pattern> patterns = new Vector<Pattern>();
	
	public Layer(Vector<Slice> slices, int layerNumber){
		this.slices = slices;
		this.layerNumber = layerNumber;
		
		for (Slice s : slices){
			patterns.add(new Pattern(s, layerNumber));
		}
	}

}
