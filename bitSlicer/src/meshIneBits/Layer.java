package meshIneBits;

import java.awt.geom.AffineTransform;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Vector;

import meshIneBits.Config.CraftConfig;
import meshIneBits.PatternTemplates.PatternTemplate;
import meshIneBits.Slicer.Slice;
import meshIneBits.util.Vector2;

/**
 * A layer contains all the bit 3D for a given Z. These bits are organized
 * following a {@link PatternTemplate}. In order to build the 3D bits, it has to
 * start by building a {@link Pattern} for each slice. The
 * {@link #referentialPattern} determines the position and orientation of the
 * bits. It is then cloned and shaped to fit the linked {@link Slice}.
 */
public class Layer extends Observable {

	private int layerNumber;
	private Vector<Slice> slices;
	private Pattern referentialPattern;
	private PatternTemplate patternTemplate;
	private Vector<Pattern> patterns = new Vector<Pattern>();
	private Hashtable<Vector2, Bit3D> mapBits3D;
	private int sliceToSelect;

	public Layer(Vector<Slice> slices, int layerNumber, GeneratedPart generatedPart) {
		this.slices = slices;
		this.layerNumber = layerNumber;
		this.patternTemplate = generatedPart.getPatternTemplate();
		this.referentialPattern = generatedPart.getPatternTemplate().createPattern(layerNumber);
		setSliceToSelect(CraftConfig.defaultSliceToSelect);

		rebuild();
	}

	/**
	 * Add a bit to the {@link #referentialPattern} and call {@link #rebuild}
	 * which will rebuild all the {@link #Pattern} taking in account this new
	 * bit.
	 * 
	 * @param bit
	 */
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

	/**
	 * Build the {@link Bit3D} from all the {@link Bit2D} contained in the
	 * {@link Pattern}.
	 */
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
						newBit = new Bit3D(bitsToInclude, bitKey, getNewOrientation(bitsToInclude.get(i)),
								sliceToSelect);
						mapBits3D.put(bitKey, newBit);
					} catch (Exception e) {
						if ((e.getMessage() != "This bit does not contain enough bit 2D in it")
								&& (e.getMessage() != "The slice to select does not exist in that bit")) {
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

	/**
	 * @return the index of selected slice
	 */
	public int getSliceToSelect() {
		return sliceToSelect;
	}

	/**
	 * @return the selected pattern
	 */
	public Pattern getSelectedPattern() {
		return this.patterns.get(sliceToSelect);
	}
	
	/**
	 * @return the slice corresponding to the selected pattern
	 */
	public Slice getSelectedSlice(){
		return this.slices.elementAt(sliceToSelect);
	}

	/**
	 * Move a bit. The distance of displacement will be determined in dependence
	 * on the pattern template.
	 * 
	 * @param bitKey
	 * @param direction
	 *            the direction in local coordinate system of the bit
	 * @return the new origin of the moved bit
	 */
	public Vector2 moveBit(Vector2 bitKey, Vector2 direction) {
		Vector2 newCoor = patternTemplate.moveBit(referentialPattern, bitKey, direction);
		rebuild();
		return newCoor;
	}

	/**
	 * Rebuild the whole layer. To be called after every changes made on this
	 * layer
	 */
	public void rebuild() {
		buildPatterns();
		generateBits3D();
		computeLiftPoints();
		setChanged();
		notifyObservers();
	}

	/**
	 * Remove a bit
	 * 
	 * @param key
	 */
	public void removeBit(Vector2 key) {
		referentialPattern.removeBit(key);
		rebuild();
	}

	/**
	 * Scale a bit
	 * 
	 * @param bit
	 * @param percentageLength
	 * @param percentageWidth
	 */
	public void replaceBit(Bit3D bit, double percentageLength, double percentageWidth) {
		Bit2D modelBit = bit.getBit2dToExtrude();
		removeBit(bit.getOrigin());
		Bit2D newBit = new Bit2D(modelBit, percentageLength, percentageWidth);
		addBit(newBit);
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

	/**
	 * @return the patternTemplate
	 */
	public PatternTemplate getPatternTemplate() {
		return patternTemplate;
	}

	/**
	 * @param patternTemplate
	 *            the patternTemplate to set
	 */
	public void setPatternTemplate(PatternTemplate patternTemplate) {
		this.patternTemplate = patternTemplate;
	}

}
