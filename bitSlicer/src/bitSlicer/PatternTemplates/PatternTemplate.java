package bitSlicer.PatternTemplates;

import java.util.Vector;

import bitSlicer.Bit2D;
import bitSlicer.util.Vector2;

abstract class PatternTemplate {
	
	protected double bitWidth, bitLength;	
	protected Vector2 rotation, offSet, patternStart, patternEnd;
	
	public PatternTemplate(double bitWidth, double bitLength, Vector2 rotation, Vector2 offSet, double skirtRadius){
		this.bitWidth = bitWidth;
		this.bitLength = bitLength;
		this.rotation = rotation;
		this.offSet = offSet;
		setBoundaries(skirtRadius);
	}

	/*
	 * setBoundaries will set the patternStart and patternEnd points according to the rotation, offset and skirtRadius
	 * skirtRadius is the radius of the cylinder that fully contains the part.
	 * The points patternStart and patternEnd make the rectangle that the method createPattern() will cover.
	 */
	public void setBoundaries(double skirtRadius){
		//TODO
	}
	
	abstract Vector<Bit2D> createPattern(double layerNumber);
}

