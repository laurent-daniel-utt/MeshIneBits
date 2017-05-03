/**
 * 
 */
package meshIneBits.util;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import meshIneBits.Layer;
import meshIneBits.Pattern;

/**
 * For automatic optimization. It will observe the changes in layers
 * 
 * @author NHATHAN
 * 
 */
public class Optimizer implements Observer {

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
	 * Basic constructor, used in the running of
	 * {@link meshIneBits.GeneratedPart GeneratedPart}
	 * 
	 * @param layers
	 */
	public Optimizer(Vector<Layer> layers) {
		this.layers = layers;
		for (Layer layer : layers) {
			layer.addObserver(this);
		}
	}

	/**
	 * Automatically optimize all whole generated part
	 * 
	 */
	public void automaticallyOptimizeGeneratedPart() {
		int[] layersHavingIrregularities = new int[irregularBits.keySet().size()];
		int i = 0;
		for (Integer integer : irregularBits.keySet()) {
			layersHavingIrregularities[i] = integer.intValue();
			i++;
		}
		for (int j = 0; j < layersHavingIrregularities.length; j++) {
			automaticallyOptimizeLayer(layers.get(j));
		}
	}

	/**
	 * Automatically optimize the given layer
	 * 
	 * @param layer
	 */
	public void automaticallyOptimizeLayer(Layer layer) {
		layer.getPatternTemplate().optimize(layer);
	}

	/**
	 * Construct the set of all irregular bits. Considerate only ones of the
	 * selected slices.
	 */
	public void detectIrregularBits() {
		for (Layer layer : layers) {
			Pattern pattern = layer.getSelectedPattern();
			Vector<Vector2> irregularBitsInThisPattern = DetectorTool.detectIrregularBits(pattern);
			irregularBits.put(layer.getLayerNumber(), irregularBitsInThisPattern);
		}
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
	 * Re-determine all irregular bits
	 * 
	 * @param o
	 *            the layer which has been changed
	 * @param arg
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof Layer) {
			Layer layer = (Layer) o;
			this.irregularBits.remove(layer.getLayerNumber());
			this.irregularBits.put(layer.getLayerNumber(),
					DetectorTool.detectIrregularBits(layer.getSelectedPattern()));
		}
	}
}
