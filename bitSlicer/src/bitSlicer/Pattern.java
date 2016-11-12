package bitSlicer;

import java.util.Vector;

import bitSlicer.PatternTemplates.PatternTemplate;
import bitSlicer.Slicer.Slice;
import bitSlicer.Slicer.Config.CraftConfig;
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
	
	public void setToLayerCooSystem(){
		for (Bit2D bit : bits){
			bit.setBitInPatternCooSystem(); //Bits' boundaries go from a local coo system to the pattern's one
			bit.setInLowerCooSystem(rotation, new Vector2(0,0)); // Each pattern can have a rotation, usually linked to the layer number
			bit.setInLowerCooSystem(Vector2.getEquivalentVector(CraftConfig.rotation), CraftConfig.offset); //the whole pattern template can have a rotation and an offset regarding the part
			//System.out.println("bit(" + bit.origin.x + ";" + bit.origin.y + ")");
		}
	}
	
	public Vector<Bit2D> getBits(){
		return this.bits;
	}
}
