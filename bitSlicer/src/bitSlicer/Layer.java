package bitSlicer;

import bitSlicer.util.Shape2D;
import bitSlicer.Slicer.Slice;
import bitSlicer.Pattern;
import bitSlicer.PatternTemplates.PatternTemplate;

public class Layer extends Shape2D {
	
	private int layerNumber;
	private Slice slice;
	private Pattern pattern;
	
	public Layer(Slice slice, PatternTemplate patternTemplate,  int layerNumber){
		this.slice = slice;
		this.layerNumber = layerNumber;
		
		pattern = new Pattern(slice, patternTemplate, layerNumber);
	}

}
