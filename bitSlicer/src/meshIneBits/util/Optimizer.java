/**
 * 
 */
package meshIneBits.util;

import java.util.HashMap;
import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Pattern;

/**
 * @author NHATHAN For automatic optimization
 */
public class Optimizer {

	/**
	 * All the constructed layers
	 */
	private Vector<Layer> layers;

	/**
	 * Collection of bits who do not have lift point in selected slices. The key
	 * is the number of layer and the corresponding value is all irregular bits
	 * in the layer's selected slice.
	 */
	private HashMap<Integer, Vector<Vector2>> irregularBits = new HashMap<Integer, Vector<Vector2>>();

	/**
	 * Basic constructor, used in the running of {@link GeneratedPart}
	 * 
	 * @param layers
	 */
	public Optimizer(Vector<Layer> layers) {
		this.setLayers(layers);
	}

	/**
	 * Automatically optimize all the irregular bits by replacing or displacing,
	 * depending on the pattern
	 * 
	 * @param slice
	 * @param pattern
	 */
	public void automaticallyOptimize(Vector<Bit2D> slice, Pattern pattern) {

	}

	/**
	 * Construct {@link irregularBits}. Considerate only the selected slices
	 */
	public void detectIrregularBits() {
		for (Layer layer : layers) {
			Pattern pattern = layer.getPatterns().get(layer.getSliceToSelect());
			Vector<Vector2> irregularBitsInThisPattern = detectIrregularBits(pattern);
			irregularBits.put(layer.getLayerNumber(), irregularBitsInThisPattern);
		}
	}

	/**
	 * @param pattern
	 *            container of all bits in a slice
	 * @return all irregular bits in the given slice
	 */
	public Vector<Vector2> detectIrregularBits(Pattern pattern) {
		Vector<Vector2> result = new Vector<Vector2>();
		for (Vector2 bitKey : pattern.getBitsKeys()) {
			Bit2D bit = pattern.getBit(bitKey);
			if (bit.computeLiftPoint() == null) {
				bit.setNeedsToBeOptimized(true);
				result.add(bitKey);
			}
		}
		return result;
	}

	/**
	 * @param layerNum
	 * @return all the irregular bit in the given layer
	 */
	public Vector<Vector2> getIrregularBitKeysAtLayer(int layerNum) {
		return irregularBits.get(layerNum);
	}

	/**
	 * @return Map of irregular bits. The key is the number of layer and the
	 *         corresponding value is all irregular bits in the layer's selected
	 *         slice.
	 */
	public HashMap<Integer, Vector<Vector2>> getIrregularBits() {
		return irregularBits;
	}

	/**
	 * @param irregularBits
	 *            the irregularBits to set
	 */
	public void setIrregularBits(HashMap<Integer, Vector<Vector2>> irregularBits) {
		this.irregularBits = irregularBits;
	}

	/**
	 * @return the layers
	 */
	public Vector<Layer> getLayers() {
		return layers;
	}

	/**
	 * @param layers
	 *            the layers to set
	 */
	public void setLayers(Vector<Layer> layers) {
		this.layers = layers;
	}

}
