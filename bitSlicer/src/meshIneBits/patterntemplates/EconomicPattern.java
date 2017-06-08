/**
 * 
 */
package meshIneBits.patterntemplates;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.PatternParameterConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.DetectorTool;
import meshIneBits.util.Vector2;

/**
 * A pattern which tries optimization not by displacing paved bits but rather
 * right from the phase of paving. Therefore, it requires auto-optimization task
 * to complete. Note that this pattern does not always return a result because
 * once it can not fill a separated zone of the generated part, that whole layer
 * will end up failed.
 * 
 * @author NHATHAN
 *
 */
public class EconomicPattern extends PatternTemplate {

	/**
	 * Contains all offsets for trying to rotate this layer, in order to not
	 * have the same rotation with the previous layer.
	 */
	private double[] trialRotations;
	/**
	 * Contains all offsets for trying to dislocate the first line following
	 * y-axe.
	 */
	private double[] trialHeightOffsets;
	/**
	 * Contains all offsets for trying to dislocate the first bit of a line
	 * following x-axe.
	 */
	private double[] trialLengthOffsets;
	/**
	 * The gap between 2 bits' width
	 */
	private double bitsWidthSpace;

	/**
	 * The gap between 2 bits' length
	 */
	private double bitsLengthSpace;
	/**
	 * Stocks all rotations' layers.
	 */
	private static Vector<java.lang.Double> layersRotations = new Vector<java.lang.Double>();

	/**
	 * This constructor will only leave a black space. The real job is done in
	 * {@link #optimize(Layer)}
	 */
	@Override
	public Pattern createPattern(int layerNumber) {
		layersRotations.add(layerNumber, null);
		return new Pattern(new Vector<Bit2D>(), new Vector2(1, 0));
	}

	/**
	 * This will try to pave bits into the layer in the best way possible. The
	 * process should be executed consequently.
	 * 
	 * @return -1 if the task failed
	 */
	@Override
	public int optimize(Layer actualState) {
		// Reset the state
		actualState.setReferentialPattern(new Pattern(new Vector<Bit2D>(), new Vector2(1, 0)));
		// Prepare parameters
		this.setupTrialLengthOffsets();
		this.setupTrialHeightOffsets();
		this.setupTrialRotations(actualState.getLayerNumber());
		this.bitsLengthSpace = (double) config.get("bitsLengthSpace").getCurrentValue();
		this.bitsWidthSpace = (double) config.get("bitsWidthSpace").getCurrentValue();
		// Slice in fact is a set of polygons
		Slice selectedBoundary = actualState.getSelectedSlice();
		Vector<Bit2D> overallPavement = new Vector<Bit2D>();
		double thisLayerRotation = 0;
		boolean essay = true;
		// Get all lv0 areas
		Vector<Area> lv0Areas = AreaTool.getLevel0AreasFrom(selectedBoundary);
		for (int idx = 0; idx < trialRotations.length; idx++) {
			thisLayerRotation = trialRotations[idx];
			// System.out.println("Essay of rotation:" + thisLayerRotation);
			// Assuming this rotation of this layer will give us the best answer
			// Note: differential rotation means the difference of directions
			// between this layer and the previous one
			// We will pave the bits for each level-0 area
			// which is a constraint zone not overlapping with another
			//
			// Pave bits into each lv0 area
			// Receive the map of keys
			for (Area area : lv0Areas) {
				// System.out.println("Area:" + area.getBounds2D());
				Rectangle2D.Double bound = (Double) area.getBounds2D();
				double anchorX = bound.x + bound.width / 2, anchorY = bound.y + bound.height / 2;
				Vector2 vectorRotation = Vector2.getEquivalentVector(thisLayerRotation);
				AffineTransform rotate = AffineTransform.getRotateInstance(vectorRotation.x, vectorRotation.y, anchorX,
						anchorY), rotateBack = new AffineTransform();
				try {
					rotateBack = rotate.createInverse();
				} catch (Exception e) {
					e.printStackTrace();
					rotate.setToIdentity();
					rotateBack.setToIdentity();
				}
				// Rotate the local zone
				Area rotatedZone = area.createTransformedArea(rotate);
				Vector<Bit2D> localPavement = null;
				Vector<AffineTransform> possibleFlips = this.calculatePossibleFlips(rotatedZone);
				if (possibleFlips == null) {
					essay = false;
					break;
				}
				// Try all possible flips
				// Including no flip, horizontal flip, vertical flip,
				// center reflect
				for (int i = 0; i < possibleFlips.size(); i++) {
					AffineTransform flip = possibleFlips.get(i);
					localPavement = this.fillZone(rotatedZone.createTransformedArea(flip));
					if (localPavement != null) {
						// Reverse the local pavement
						// In this case, flip(flip(X)) = X
						// And we must notice the first rotate
						flip.preConcatenate(rotateBack);
						localPavement = this.transform(localPavement, flip);
						overallPavement.addAll(localPavement);
						break;
					}
				}
				// Even if we try all flips
				if (localPavement == null) {
					essay = false;
					break;
				}
			}
			if (essay) {
				// If we find at least one fill
				// Or we tried all rotation
				break;
			} else {
				// Clear all to try at new rotation
				overallPavement.clear();
			}
		}
		// Recreate the referential pattern for this layer
		if (!overallPavement.isEmpty()) {
			actualState.setReferentialPattern(new Pattern(overallPavement, new Vector2(1, 0)));
			layersRotations.set(actualState.getLayerNumber(), thisLayerRotation);
			actualState.rebuild();
			return 0;
		} else {
			return -1;
		}
	}

	/**
	 * Trying to fill a given area by bits following the predefined algorithm.
	 * 
	 * Note: Only fill from top to bottom.
	 * 
	 * @param zone
	 *            a constraint surface without any other one inside or outside.
	 * @return <tt>null</tt> if no solution found
	 */
	private Vector<Bit2D> fillZone(Area zone) {
		Vector<Bit2D> zonePavement = new Vector<Bit2D>();
		// The rectangle enclosing the area
		Rectangle2D.Double zoneOuterRect = (Double) zone.getBounds2D();
		for (int idx = 0; idx < trialHeightOffsets.length; idx++) {
			// System.out.println("Zone: W " + zone.getBounds2D().getWidth() + "
			// H " + zone.getBounds2D().getHeight()
			// + " - Essay of height:" + trialHeightOffsets[idx]);
			// Initial parameters
			Area unpavedZone = (Area) zone.clone();
			Area lastState = null;
			Vector<Bit2D> lastBand = null, thisBand = null;
			double thisBandHeight = CraftConfig.bitWidth;
			boolean essay = true;
			// Starting to pave line by line
			while (!unpavedZone.isEmpty()) {
				lastState = (Area) unpavedZone.clone();
				Rectangle2D.Double unpavedZoneRect = (Double) unpavedZone.getBounds2D();
				// Creating a buffering rectangle
				// which will contain a portion of the given area
				// in which we will place the bits
				Rectangle2D.Double unpavedBandRect = new Rectangle2D.Double();
				if (zonePavement.isEmpty()) {
					// If this is the first band,
					// we will push it back a little bit
					unpavedBandRect.setRect(unpavedZoneRect.x, unpavedZoneRect.y - trialHeightOffsets[idx],
							unpavedZoneRect.width, thisBandHeight);
				} else {
					unpavedBandRect.setRect(unpavedZoneRect.x, unpavedZoneRect.y, unpavedZoneRect.width,
							thisBandHeight);
				}
				Area unpavedBand = new Area(unpavedBandRect);
				// Intersecting this band with the initial area
				// gives us real surface in which we will fill bits
				unpavedBand.intersect(zone);
				// Starting to fill bit by bit
				thisBand = this.fillBand(unpavedBand, thisBandHeight);
				// Check out the result
				if (thisBand == null) {
					// It means we failed at this band
					if (lastBand == null) {
						// It means we failed to fill this zone
						// (either we are in the first band
						// or we were trying to rebuild a band)
						// We need to change the height of first line
						essay = false;
						break;
					} else {
						if (lastBand.firstElement().getWidth() == CraftConfig.bitWidth) {
							// If the previous band has not been cut in half
							// we will rebuild it with half of its height
							// First, we need to recover the space
							// taken by last band
							double lastY = lastBand.firstElement().getOrigin().y - CraftConfig.bitWidth / 2
									- bitsLengthSpace;
							Rectangle2D.Double lastlyPavedSpaceRect = new Rectangle2D.Double(zoneOuterRect.x, lastY,
									zoneOuterRect.width, unpavedZoneRect.y - lastY);
							Area lastlyPavedSpace = new Area(lastlyPavedSpaceRect);
							lastlyPavedSpace.intersect(zone);
							// Then add to the unpaved zone
							unpavedZone.add(lastlyPavedSpace);
							// Assuming new height to build
							thisBandHeight = CraftConfig.bitWidth / 2;
							// Remove the old bits from zonePavement
							zonePavement.removeAll(lastBand);
							// Delete the memory
							lastBand = null;
						} else {
							// Although the last line has been rebuilt
							// with half height
							// we still can not find a solution for this line
							// We need to change the height of first line
							essay = false;
							break;
						}
					}
				} else {
					// This line has been filled successfully
					// Let's save this line for later use
					lastBand = thisBand;
					// Reduce the unpaved space
					Rectangle2D.Double pavedZoneRect = new Rectangle2D.Double(zoneOuterRect.x, unpavedZoneRect.y,
							zoneOuterRect.width, thisBandHeight + bitsLengthSpace);
					unpavedZone.subtract(new Area(pavedZoneRect));
					// Assuming the height of the next line
					thisBandHeight = CraftConfig.bitWidth;
					// Collecting the result
					zonePavement.addAll(thisBand);
					// Delete memory
					thisBand = null;
				}
				// Preventing infinite loop
				if (unpavedZone.equals(lastState))
					return null;
			}
			// If we find at least one fill satisfying
			// Or we had tried all possibilities
			if (essay) {
				break;
			} else {
				// Reset
				zonePavement.clear();
			}
		}
		if (!zonePavement.isEmpty()) {
			return zonePavement;
		} else {
			return null;
		}
	}

	/**
	 * Trying to fill a horizontal line by bits.
	 * 
	 * Note: Only fill from left to right.
	 * 
	 * @param band
	 *            whose height is {@link CraftConfig#bitWidth} or a half of it
	 * @param bandHeight
	 *            height of bits to be filled in
	 * @return <tt>null</tt> if no solution found
	 */
	private Vector<Bit2D> fillBand(Area band, double bandHeight) {
		// A little tweak
		if (band.getBounds2D().getHeight() < CraftConfig.suckerDiameter
				|| band.getBounds2D().getWidth() < CraftConfig.suckerDiameter) {
			return null;
		}
		Vector<Bit2D> bandPavement = new Vector<Bit2D>();
		for (int idx = 0; idx < trialLengthOffsets.length; idx++) {
			System.out.println("Band: W " + band.getBounds2D().getWidth() + " H " + band.getBounds2D().getHeight()
					+ " - Height to build: " + bandHeight + " - Essay of length:" + trialLengthOffsets[idx]);
			// Save the rest area
			Area unpavedSpace = (Area) band.clone();
			// Initial parameters
			Rectangle2D.Double bandOuterRect = (Double) band.getBounds2D();
			double originY = bandOuterRect.y + CraftConfig.bitWidth / 2, thisBitLength = CraftConfig.bitLength;
			boolean essay = true;
			// Start
			while (!unpavedSpace.isEmpty()) {
				// Get the boundary
				Rectangle2D.Double unpavedSpaceRect = (Double) unpavedSpace.getBounds2D();
				// Creating a new bit
				// Attention to the case of rebuilding a bit
				// with half of its normal length
				Vector2 origin = new Vector2(unpavedSpaceRect.x + thisBitLength - CraftConfig.bitLength / 2, originY);
				if (bandPavement.isEmpty()) {
					// If this is the first bit
					// we will push it backward a little bit
					origin.sub(new Vector2(trialLengthOffsets[idx], 0));
				}
				Bit2D newBit = new Bit2D(origin, new Vector2(1, 0), thisBitLength, bandHeight);
				// Update the area of the bit
				Area newBitArea = newBit.getArea();
				newBitArea.intersect(unpavedSpace);
				if (newBitArea.isEmpty()) {
					// If no space left,
					// we complete the fill
					break;
				}
				newBit.updateBoundaries(newBitArea);
				// Check if the new bit have sufficient lift points
				// in the unpaved space before the fill
				if (!DetectorTool.checkIrregular(newBit)) {
					// If yes, we retrieve it
					bandPavement.add(newBit);
					// Decrease the unpaved space
					Rectangle2D.Double pavedSpaceRect = new Rectangle2D.Double(unpavedSpaceRect.x, bandOuterRect.y,
							thisBitLength + bitsWidthSpace, bandHeight);
					unpavedSpace.subtract(new Area(pavedSpaceRect));
					// Assuming the next bit will have full length
					thisBitLength = CraftConfig.bitLength;
				} else {
					// If no, it means we failed
					if (thisBitLength == CraftConfig.bitLength) {
						// We were building a new full-length bit
						// So we will retry by rebuilding the last bit
						// with half of its length
						if (bandPavement.isEmpty()) {
							// But if we have no previous bit
							// We change the length offset of first bit
							essay = false;
							break;
						} else {
							Bit2D lastBit = bandPavement.lastElement();
							if (lastBit.getLength() == CraftConfig.bitLength / 2) {
								// If we had rebuilt the last bit
								// and retried this bit but ended up failure
								// That means we fail
								// We should change the length offset
								essay = false;
								break;
							} else {
								// Else, we recover the space taken
								// by the last bit
								// Note: we must include the skipped space
								double lastX = lastBit.getOrigin().x - CraftConfig.bitLength / 2;
								Rectangle2D.Double lastPavedSpaceRect = new Rectangle2D.Double(lastX, bandOuterRect.y,
										unpavedSpaceRect.x - lastX, bandHeight);
								Area lastlyPavedSpace = new Area(lastPavedSpaceRect);
								lastlyPavedSpace.intersect(band);
								unpavedSpace.add(lastlyPavedSpace);
								// Assuming the rebuild
								// with a half normal length
								thisBitLength = CraftConfig.bitLength / 2;
								// We remove the last bit
								bandPavement.remove(lastBit);
							}
						}
					} else {
						// In case we were trying rebuilding
						// We will change the offset
						essay = false;
						break;
					}
				}
			}
			if (essay) {
				// If we find at least one fill satisfying
				// Or we had tried all possibilities
				break;
			} else {
				bandPavement.clear();
			}
		}
		if (!bandPavement.isEmpty()) {
			return bandPavement;
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * meshIneBits.patterntemplates.PatternTemplate#moveBit(meshIneBits.Pattern,
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
	 * meshIneBits.patterntemplates.PatternTemplate#moveBit(meshIneBits.Pattern,
	 * meshIneBits.util.Vector2, meshIneBits.util.Vector2, double)
	 */
	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection, double distance) {
		return actualState.moveBit(bitKey, localDirection, distance);
	}

	@SuppressWarnings("unchecked")
	private void setupTrialLengthOffsets() throws ClassCastException, UnsupportedOperationException {
		List<java.lang.Double> a = (List<java.lang.Double>) config.get("trialLengthRatioOffsets").getCurrentValue();
		// Adapt values
		trialLengthOffsets = new double[a.size()];
		for (int i = 0; i < a.size(); i++) {
			trialLengthOffsets[i] = a.get(i) * CraftConfig.bitLength;
		}
	}

	@SuppressWarnings("unchecked")
	private void setupTrialHeightOffsets() {
		List<java.lang.Double> a = (List<java.lang.Double>) config.get("trialHeightRatioOffsets").getCurrentValue();
		trialHeightOffsets = new double[a.size()];
		for (int i = 0; i < trialHeightOffsets.length; i++) {
			trialHeightOffsets[i] = a.get(i) * CraftConfig.bitWidth;
		}
	}

	@SuppressWarnings("unchecked")
	private void setupTrialRotations(int layerNum) {
		List<java.lang.Double> a = (List<java.lang.Double>) config.get("trialDiffAngles").getCurrentValue();
		if (layerNum == 0 || layersRotations.get(layerNum - 1) == null) {
			trialRotations = new double[a.size() + 1];
			trialRotations[0] = 0;
			for (int i = 0; i < a.size(); i++) {
				trialRotations[i + 1] = a.get(i);
			}
		} else {
			double previousRotation = layersRotations.get(layerNum - 1);
			trialRotations = new double[a.size()];
			for (int i = 0; i < a.size(); i++) {
				trialRotations[i] = previousRotation + a.get(i);
			}
		}
	}

	private Vector<Bit2D> transform(Vector<Bit2D> bits, AffineTransform conservativeTransformation) {
		Vector<Bit2D> result = new Vector<Bit2D>(bits.size());
		bits.forEach(bit -> {
			result.add(bit.createTransformedBit(conservativeTransformation));
		});
		return result;
	}

	/**
	 * @param area
	 * @return the matrix to move the <tt>area</tt> to the origin of coordinate
	 *         system
	 */
	private AffineTransform centerize(Area area) {
		AffineTransform tx = new AffineTransform();
		tx.setToIdentity();
		Rectangle2D.Double rect = (Double) area.getBounds2D();
		double centerX = rect.x + rect.width / 2, centerY = rect.y + rect.height / 2;
		tx.translate(-centerX, -centerY);
		return tx;
	}

	/**
	 * Calculate all possible flips for <tt>area</tt>, keeping the same boundary
	 * 
	 * @param area
	 * @return a table of 4 possible flips
	 */
	private Vector<AffineTransform> calculatePossibleFlips(Area area) {
		AffineTransform t = this.centerize(area);
		Vector<AffineTransform> result = null;
		try {
			AffineTransform goback = t.createInverse();
			Vector<AffineTransform> flips = new Vector<AffineTransform>(4);
			result = new Vector<AffineTransform>(4);
			flips.add(AffineTransform.getScaleInstance(1.0, 1.0));
			flips.add(AffineTransform.getScaleInstance(1.0, -1.0));
			flips.add(AffineTransform.getScaleInstance(-1.0, 1.0));
			flips.add(AffineTransform.getScaleInstance(-1.0, -1.0));
			for (int i = 0; i < flips.size(); i++) {
				AffineTransform c = (AffineTransform) t.clone();
				c.preConcatenate(flips.get(i));
				c.preConcatenate(goback);
				result.add(c);
			}
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String getCommonName() {
		return "Economic Pattern";
	}

	@Override
	public String getIconName() {
		return "p4.png";
	}

	@Override
	public String getDescription() {
		return "A pattern which tries optimization not by displacing paved bits "
				+ "but rather right from the phase of paving.";
	}

	@Override
	public String getHowToUse() {
		return "It requires auto-optimization task to complete. "
				+ "Note that this pattern does not always return a result "
				+ "because once it can not fill a separated zone of the generated part, "
				+ "that whole layer will end up failed.";
	}

	/*
	 * This does nothing in particular.
	 */
	@Override
	public boolean ready(GeneratedPart generatedPart) {
		return true;
	}

	@Override
	public void initiateConfig() {
		// bitsLengthSpace
		config.add(new PatternParameterConfig("bitsLengthSpace", "Space between bits' lengths",
				"The gap between two consecutive bits' lengths (in mm)", 1.0, 100.0, 1.0, 1.0));
		// bitsWidthSpace
		config.add(new PatternParameterConfig("bitsWidthSpace", "Space between bits' widths",
				"The gap between two consecutive bits' widths (in mm)", 1.0, 100.0, 1.0, 1.0));
		// trialLengthRatioOffsets
		double[] x = { 0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0 };
		ArrayList<java.lang.Double> trialLengthRatios = new ArrayList<>(x.length);
		for (int i = 0; i < x.length; i++) {
			trialLengthRatios.add(java.lang.Double.valueOf(x[i]));
		}
		config.add(new PatternParameterConfig("trialLengthRatioOffsets", "Trial length's ratios",
				"This helps us in finding the most suitable length for the first bit of a line."
						+ "\nThese ratios should be distinct between 0 and 1." + "\nOtherwise values will be filtered.",
				0.0, 1.0, trialLengthRatios, 0.001));
		// trialHeightRatioOffsets
		double[] y = { 0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0 };
		ArrayList<java.lang.Double> trialHeightRatios = new ArrayList<>(y.length);
		for (int i = 0; i < y.length; i++) {
			trialHeightRatios.add(java.lang.Double.valueOf(y[i]));
		}
		config.add(new PatternParameterConfig("trialHeightRatioOffsets", "Trial height's ratios",
				"This helps us in finding the most suitable height for the first line of the pavement."
						+ "\nThese ratios should be distinct between 0 and 1." + "\nOtherwise values will be filtered.",
				0.0, 1.0, trialHeightRatios, 0.001));
		// trialDiffAngles
		double[] z = { 90, // 1st level
				45, 135, // 2nd level
				30, 60, 120, 150, // 3rd level
		};
		ArrayList<java.lang.Double> trialDiffAngles = new ArrayList<>(z.length);
		for (int i = 0; i < z.length; i++) {
			trialDiffAngles.add(java.lang.Double.valueOf(z[i]));
		}
		config.add(new PatternParameterConfig("trialDiffAngles", "Trial differential angles",
				"This helps us in finding the most suitable rotation of a layer in comparision with the previous one,"
						+ " in order not to have 2 layers having same rotation."
						+ "\nThese angles should be distinct between 0 and 180."
						+ "\nOtherwise values will be filtered.",
				0.0, 180.0, trialDiffAngles, 0.1));
	}
}
