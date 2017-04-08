package meshIneBits.PatternTemplates;

import meshIneBits.Pattern;
import meshIneBits.Config.CraftConfig;
import meshIneBits.util.Vector2;

public abstract class PatternTemplate {
	
	protected Vector2 patternStart, patternEnd;
	protected double skirtRadius;
	
	private String name;
	
	public PatternTemplate(double skirtRadius){
		
		//skirtRadius is the radius of the cylinder that fully contains the part.
		//The points patternStart and patternEnd make the rectangle that the method createPattern() will cover.
		double maxiSide = Math.max(CraftConfig.bitLength, CraftConfig.bitWidth);
		patternStart = new Vector2(-skirtRadius - maxiSide, -skirtRadius - maxiSide);
		patternEnd = new Vector2(skirtRadius + maxiSide, skirtRadius + maxiSide);
		
		this.skirtRadius = skirtRadius;
	}
	
	public abstract Pattern createPattern(int layerNumber);

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}

