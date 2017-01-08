package meshIneBits;

import java.awt.geom.AffineTransform;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Vector;

import meshIneBits.Config.CraftConfig;
import meshIneBits.Slicer.Slice;
import meshIneBits.util.Vector2;

public class Layer extends Observable {

	private int layerNumber;
	private Vector<Slice> slices;
	private Pattern referentialPattern;
	private Vector<Pattern> patterns = new Vector<Pattern>();
	private Hashtable<Vector2, Bit3D> mapBits3D;
	private int sliceToSelect;

	public Layer(Vector<Slice> slices, int layerNumber, GeneratedPart generatedPart) {
		this.slices = slices;
		this.layerNumber = layerNumber;
		this.referentialPattern = generatedPart.getPatternTemplate().createPattern(layerNumber);

		setSliceToSelect(CraftConfig.defaultSliceToSelect);

		rebuild();
	}

	public void addBit(Bit2D bit) {
		referentialPattern.addBit(bit);
		rebuild();
	}

	private void buildPatterns() {
		patterns.clear();
		for (Slice s : slices) {
			patterns.add(referentialPattern.clone());
			patterns.lastElement().computeBits(s);
		}
	}

	private void computeLiftPoints() {
		for (Vector2 key : getBits3dKeys()) {
			mapBits3D.get(key).computeLiftPoints();
		}
	}

	private void generateBits3D() {
		mapBits3D = new Hashtable<Vector2, Bit3D>();
		Vector<Bit2D> bitsToInclude = new Vector<Bit2D>();

		for (Vector2 bitKey : referentialPattern.getBitsKeys()) {
			for (Pattern pattern : patterns) {
				if (pattern.getBitsKeys().contains(bitKey)) {
					bitsToInclude.add(pattern.getBit(bitKey));
				} else {
					bitsToInclude.add(null);
				}
			}

			for (int i = 0; i < bitsToInclude.size(); i++) {
				if (bitsToInclude.get(i) != null) {
					Bit3D newBit;
					try {
						newBit = new Bit3D(bitsToInclude, bitKey, getNewOrientation(bitsToInclude.get(i)), sliceToSelect);
						mapBits3D.put(bitKey, newBit);
					} catch (Exception e) {
						if ((e.getMessage() != "This bit does not contain enough bit 2D in it") && (e.getMessage() != "The slice to select does not exist in that bit")) {
							e.printStackTrace();
						}
					}
					break;
				}
			}

			bitsToInclude.clear();
		}
	}

	public Bit3D getBit3D(Vector2 key) {
		return mapBits3D.get(key);
	}

	public Vector<Vector2> getBits3dKeys() {
		return new Vector<Vector2>(mapBits3D.keySet());
	}

	public int getLayerNumber() {
		return layerNumber;
	}

	public Vector2 getNewOrientation(Bit2D bit) {
		AffineTransform patternAffTrans = (AffineTransform) referentialPattern.getAffineTransform().clone();
		patternAffTrans.translate(-CraftConfig.xOffset, -CraftConfig.yOffset);
		return bit.getOrientation().getTransformed(patternAffTrans);
	}

	public Vector<Pattern> getPatterns() {
		return this.patterns;
	}

	public Vector<Slice> getSlices() {
		return this.slices;
	}

	public int getSliceToSelect() {
		return sliceToSelect;
	}

	public void moveBit(Vector2 key, Vector2 direction, double offsetValue) {
		referentialPattern.moveBit(key, direction, offsetValue);
		rebuild();
	}

	public void rebuild() {
		buildPatterns();
		generateBits3D();
		computeLiftPoints();
		setChanged();
		notifyObservers();
	}

	public void removeBit(Vector2 key) {
		referentialPattern.removeBit(key);
		rebuild();
	}

	public void replaceBit(Bit3D bit, double percentageLength, double percentageWidth) {
		Bit2D modelBit = bit.getBit2dToExtrude();
		removeBit(bit.getOrigin());
		addBit(new Bit2D(modelBit, percentageLength, percentageWidth));
	}

	/**
	 * Only for default slice to select
	 * 
	 * @param percentageOfHeight
	 */
	private void setSliceToSelect(double percentageOfHeight) {
		sliceToSelect = (int) Math.round((percentageOfHeight / 100) * (slices.size() - 1));
	}

	public void setSliceToSelect(int sliceNbr) {
		if ((sliceNbr >= 0) && (sliceNbr < slices.size())) {
			sliceToSelect = sliceNbr;
			rebuild();
		}
	}

}
