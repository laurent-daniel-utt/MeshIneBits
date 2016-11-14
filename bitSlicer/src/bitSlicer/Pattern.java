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
	private double skirtRadius;
	
	public Pattern(Vector<Bit2D> bits, Vector2 rotation, double skirtRadius){

		this.bits = bits;
		this.rotation = rotation;
		this.skirtRadius = skirtRadius;
		}
	
	/*
	 * Remove the bits that are outside the part
	 * Compute the cut line for the bits on the boundaries of the part
	 */
	public void computeBits(Slice slice){
		Vector<Bit2D> bitsToKeep = new Vector<Bit2D>();
		for(Bit2D bit : bits){
			if(bit.isOnPath(slice)){
				bitsToKeep.add(bit);
			}
			else if (bit.isInsideShape(slice, skirtRadius)){
				bitsToKeep.add(bit);
			}
		}
		bits = bitsToKeep;
	}
	
	public void setToLayerCooSystem(){
		for (Bit2D bit : bits){
			bit.setBitInPatternCooSystem(); //Bits' boundaries go from a local coo system to the pattern's one
			bit.setInThatCooSystem(rotation, new Vector2(0,0)); // Each pattern can have a rotation, usually linked to the layer number
			bit.setInThatCooSystem(Vector2.getEquivalentVector(CraftConfig.rotation), new Vector2(CraftConfig.xOffset, CraftConfig.yOffset)); //the whole pattern template can have a rotation and an offset regarding the part
		}
	}
	
	public Vector<Bit2D> getBits(){
		return this.bits;
	}
}
