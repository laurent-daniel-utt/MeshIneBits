/**
 * 
 */
package meshIneBits.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Pattern;

/**
 * For automatic optimization. It will observe the changes in layers
 * 
 * @author NHATHAN
 * 
 */
public class Optimizer extends Observable implements Observer, Runnable {

	private Thread threadForAutoOptimizingLayer = null;
	private Thread threadForAutoOptimizingGeneratedPart = null;
	/**
	 * What we will try to solve. Either the generated part or the layer.
	 */
	private Object target;

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
	 * @param generatedPart TODO
	 * 
	 */
	public void automaticallyOptimizeGeneratedPart(GeneratedPart generatedPart) {
		if (threadForAutoOptimizingGeneratedPart == null
				|| (threadForAutoOptimizingGeneratedPart != null & !threadForAutoOptimizingGeneratedPart.isAlive())) {
			target = generatedPart;
			threadForAutoOptimizingGeneratedPart = new Thread(this);
			threadForAutoOptimizingGeneratedPart.start();
		}
	}

	private void optimizeGeneratedPart() {
		int progressGoal = layers.size();
		int irregularitiesRest = 0;
		ArrayList<Integer> unsolvedLayers = new ArrayList<Integer>();
		Logger.updateStatus("Optimizing the generated part.");
		for (int j = 0; j < layers.size(); j++) {
			Logger.setProgress(j + 1, progressGoal);
			int ir = optimizeLayer(layers.get(j));
			if (ir > 0) {
				irregularitiesRest += optimizeLayer(layers.get(j));				
			} else if (ir < 0) {
				unsolvedLayers.add(layers.get(j).getLayerNumber());
			}
		}
		Logger.updateStatus("Auto-optimization complete. Still has " + irregularitiesRest + " not solved yet.");
		if (!unsolvedLayers.isEmpty()){
			StringBuilder str = new StringBuilder();
			for (Integer integer : unsolvedLayers) {
				str.append(integer + " ");
			}
			Logger.updateStatus("Layers which can not be solved:" + str.toString());
		}
	}

	/**
	 * Automatically optimize the given layer
	 * 
	 * @param layer
	 */
	public void automaticallyOptimizeLayer(Layer layer) {
		if (threadForAutoOptimizingLayer == null
				|| (threadForAutoOptimizingLayer != null & !threadForAutoOptimizingLayer.isAlive())) {
			target = layer;
			threadForAutoOptimizingLayer = new Thread(this);
			threadForAutoOptimizingLayer.start();
		}
	}

	/**
	 * Automatically optimize the given layer
	 * 
	 * @param layer
	 * @return the number of irregularities not solved yet
	 */
	private int optimizeLayer(Layer layer) {
		Logger.updateStatus("Auto-optimizing layer " + layer.getLayerNumber());
		int irregularitiesRest = layer.getPatternTemplate().optimize(layer);
		if (irregularitiesRest < 0){
			Logger.updateStatus("Auto-optimization for layer " + layer.getLayerNumber() + " failed.");
		} else {
			Logger.updateStatus("Auto-optimization for layer " + layer.getLayerNumber() + " done. Unsolved bits :" + irregularitiesRest);
		}
		return irregularitiesRest;
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

	@Override
	public void run() {
		if (target != null){
			if (target instanceof Layer){
				optimizeLayer((Layer) target);
			} else if (target instanceof GeneratedPart){
				optimizeGeneratedPart();
			}
			setChanged();
			notifyObservers();
		}
	}
}
