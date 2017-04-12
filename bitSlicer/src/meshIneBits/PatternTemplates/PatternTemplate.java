package meshIneBits.PatternTemplates;

import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.Config.CraftConfig;
import meshIneBits.util.Optimizer;
import meshIneBits.util.Vector2;

public abstract class PatternTemplate {

	protected Vector2 patternStart, patternEnd;
	protected double skirtRadius;

	/**
	 * The common name for the template (not in enumeration)
	 */
	private String name;

	public PatternTemplate(double skirtRadius) {

		// skirtRadius is the radius of the cylinder that fully contains the
		// part.
		// The points patternStart and patternEnd make the rectangle that the
		// method createPattern() will cover.
		double maxiSide = Math.max(CraftConfig.bitLength, CraftConfig.bitWidth);
		patternStart = new Vector2(-skirtRadius - maxiSide, -skirtRadius - maxiSide);
		patternEnd = new Vector2(skirtRadius + maxiSide, skirtRadius + maxiSide);

		this.skirtRadius = skirtRadius;
	}

	/**
	 * Construct the layer based on this pattern
	 * 
	 * @param layerNumber
	 * @return
	 */
	public abstract Pattern createPattern(int layerNumber);

	/**
	 * To be called in auto-optimization. Or can be used to perform for a
	 * specific slice of a layer
	 * 
	 * @param realState
	 */
	public abstract void optimize(Layer realState);

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Calculate automatically the distance if moving a bit in the given
	 * direction. The distance depends on the chosen pattern and it will be the
	 * minimum step. Realize the move on the input pattern
	 * 
	 * @param actualState
	 *            the actual state of layer which is paved by this pattern
	 *            template
	 * @param keyOfBitToMove
	 * @param direction
	 *            the direction in the coordinate system of bit
	 */
	public abstract void moveBit(Pattern actualState, Vector2 keyOfBitToMove, Vector2 direction);
}
