package bitSlicer.PatternTemplates;

import java.util.Vector;

import bitSlicer.Bit2D;
import bitSlicer.util.Vector2;

public abstract class PatternTemplate {
	
	protected Vector2 rotation, offSet, patternStart, patternEnd;
	
	public PatternTemplate(Vector2 rotation, Vector2 offSet, double skirtRadius){
		this.rotation = rotation;
		this.offSet = offSet;
		//skirtRadius is the radius of the cylinder that fully contains the part.
		//The points patternStart and patternEnd make the rectangle that the method createPattern() will cover.
		patternStart = new Vector2(-skirtRadius, -skirtRadius);
		patternEnd = new Vector2(skirtRadius, skirtRadius);
	}
	
	public abstract Vector<Bit2D> createPattern(double layerNumber);
}

