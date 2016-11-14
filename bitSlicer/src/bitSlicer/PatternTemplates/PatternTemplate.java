package bitSlicer.PatternTemplates;

import bitSlicer.Pattern;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.Vector2;

public abstract class PatternTemplate {
	
	protected Vector2 patternStart, patternEnd;
	protected double skirtRadius;
	
	public PatternTemplate(double skirtRadius){
		
		//skirtRadius is the radius of the cylinder that fully contains the part.
		//The points patternStart and patternEnd make the rectangle that the method createPattern() will cover.
		patternStart = new Vector2(-skirtRadius - CraftConfig.bitLength, -skirtRadius - CraftConfig.bitLength);
		patternEnd = new Vector2(skirtRadius + CraftConfig.bitLength, skirtRadius + CraftConfig.bitLength);
		
		this.skirtRadius = skirtRadius;
	}
	
	public abstract Pattern createPattern(double layerNumber);
}

