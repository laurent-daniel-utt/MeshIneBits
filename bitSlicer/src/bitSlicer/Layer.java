package bitSlicer;

import bitSlicer.util.Shape2D;
import bitSlicer.Slicer.Slice;

import java.util.Vector;

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
	
	//If it is only used to show it up, maybe it would be more simple to only return the boundaries in the correct coordinate system?
	public Vector<Bit2D> getBits(){
		
		Vector<Bit2D> bitsInLocalCooSystem = new Vector<Bit2D>();
		for (Bit2D bit : pattern.getBits()){
			bitsInLocalCooSystem.add(getInLocalCooSystem(bit));
		}
		
		return bitsInLocalCooSystem; 
	}
	
	private Bit2D getInLocalCooSystem(Bit2D bit){
		//TODO
		return null;
	}
	
	public Slice getModelSlice(){
		return slice;
	}

}
