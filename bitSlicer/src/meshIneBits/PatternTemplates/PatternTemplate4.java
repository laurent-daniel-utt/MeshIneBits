/**
 * 
 */
package meshIneBits.PatternTemplates;

import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.Config.CraftConfig;
import meshIneBits.util.Vector2;

/**
 * @author NHATHAN
 *
 */
public class PatternTemplate4 extends PatternTemplate {

	/**
	 * @param skirtRadius
	 */
	public PatternTemplate4(double skirtRadius) {
		super(skirtRadius);
	}

	/**
	 * This constructor will only leave a black space. The real job is done in
	 * auto-optimization.
	 */
	@Override
	public Pattern createPattern(int layerNumber) {
		Vector2 customizedRotation = Vector2.getEquivalentVector((CraftConfig.diffRotation * layerNumber) % 360);
		return new Pattern(null, customizedRotation, skirtRadius);
	}


	/**
	 * This will try to pave bits into the layer in the best way possible.
	 * 
	 * @return -1 if the task failed
	 */
	@Override
	public int optimize(Layer realState) {
		// TODO Auto-generated method stub
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * meshIneBits.PatternTemplates.PatternTemplate#moveBit(meshIneBits.Pattern,
	 * meshIneBits.util.Vector2, meshIneBits.util.Vector2)
	 */
	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection) {
		double distance = 0;
		if (localDirection.x == 0) {// up or down
			distance = CraftConfig.bitWidth / 2;
		} else if (localDirection.y == 0) {// left or right
			distance = CraftConfig.bitLength / 2;
		}
		return this.moveBit(actualState, bitKey, localDirection, distance);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * meshIneBits.PatternTemplates.PatternTemplate#moveBit(meshIneBits.Pattern,
	 * meshIneBits.util.Vector2, meshIneBits.util.Vector2, double)
	 */
	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection, double distance) {
		return actualState.moveBit(bitKey, localDirection, distance);
	}

}
