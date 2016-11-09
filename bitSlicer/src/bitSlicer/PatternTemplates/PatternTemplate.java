package bitSlicer.PatternTemplates;

import bitSlicer.Pattern;
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
		
		//We apply the offset by moving the pattern's start point
		//That's a shitty solution tho, it only works if it makes the point roll away from the center
		patternStart.add(offSet);
	}
	
	public abstract Pattern createPattern(double layerNumber);
}

