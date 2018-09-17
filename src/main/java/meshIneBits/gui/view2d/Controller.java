/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package meshIneBits.gui.view2d;

import meshIneBits.*;
import meshIneBits.Mesh;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.util.Vector2;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The controller, linking 2D views and part (so-called {@link Mesh}).
 * It observes the {@link Mesh} and its {@link meshIneBits.Layer
 * Layers}. Observed by {@link Core} and {@link Wrapper}. A singleton.
 */
class Controller extends Observable implements Observer {

	static private Controller instance;
	private Mesh part = null;
	private int layerNumber = 0;
	private int sliceNumber = 0;
	private Set<Vector2> selectedBitKeys = new HashSet<>();
	private double zoom = 1;
	private boolean showSlices = false;
	private boolean showLiftPoints = false;
	private boolean showPreviousLayer = false;
	private boolean showCutPaths = false;
	private boolean showIrregularBits = false;
	private Model model = null;

	public static Controller getInstance() {
		if (instance == null) {
			instance = new Controller();
		}

		return instance;
	}

	private Controller() {
	}

	int getCurrentLayerNumber() {
		return layerNumber;
	}

    Mesh getCurrentMesh() {
		return part;
	}

	int getCurrentSliceNumber() {
		return sliceNumber;
	}

	Set<Vector2> getSelectedBitKeys() {
		return selectedBitKeys;
	}

	/**
	 * Bulk reset
	 *
	 * @param newSelectedBitKeys <tt>null</tt> to reset to empty
	 */
	void setSelectedBitKeys(Set<Vector2> newSelectedBitKeys) {
		selectedBitKeys.clear();
		if (newSelectedBitKeys != null) {
			selectedBitKeys.addAll(newSelectedBitKeys);
			selectedBitKeys.removeIf(Objects::isNull);
		}
		setChanged();
		notifyObservers(Component.SELECTED_BIT);
	}

	Set<Bit3D> getSelectedBits() {
		Layer currentLayer = part.getLayers().get(layerNumber);
		return selectedBitKeys.stream()
				.map(currentLayer::getBit3D)
				.collect(Collectors.toSet());
	}

	double getZoom() {
		return zoom;
	}

	public Model getModel(){ return model; }

	public void setLayer(int nbrLayer) {
		if (part == null) {
			return;
		}
        if (!part.isPaved()) {
			return;
		}
		if ((nbrLayer >= part.getLayers().size()) || (nbrLayer < 0)) {
			return;
		}
		layerNumber = nbrLayer;
		part.getLayers().get(layerNumber).addObserver(this);
		reset();

		setChanged();
		notifyObservers(Component.LAYER);
	}

	/**
	 * Put a generated part under observation of this controller. Also signify
	 * {@link Core} and {@link Wrapper} to repaint.
	 *
	 * @param part <tt>null</tt> to reset
	 */
	void setCurrentPart(Mesh part) {
		if (part != null) {
			this.part = part;
			part.addObserver(this);

			setLayer(0);
			reset();
		} else
			this.part = null;

		setChanged();
		notifyObservers(Component.PART);
	}

	/**
	 * Reset all attributes of chooser
	 */
	void reset() {
		setSelectedBitKeys(null);
		setOnAddingBits(false);
	}

	/**
	 * Add new bit key to {@link #selectedBitKeys} and remove if already
	 * present
	 *
	 * @param bitKey in layer's coordinate system
	 */
	void addOrRemoveSelectedBitKeys(Vector2 bitKey) {
        if (part == null || !part.isPaved()) {
			return;
		}
		if (!selectedBitKeys.add(bitKey))
			selectedBitKeys.remove(bitKey);

		setChanged();
		notifyObservers(Component.SELECTED_BIT);
	}

	public void setSlice(int nbrSlice) {
		if (part == null) {
			return;
		}
		if ((nbrSlice >= part.getSlices().size()) || (nbrSlice < 0)) {
			return;
		}
		sliceNumber = nbrSlice;

		setChanged();
		notifyObservers(Component.SLICE);
	}

	void setZoom(double zoomValue) {
		if (part == null) {
			return;
		}
		zoom = zoomValue < Wrapper.MIN_ZOOM_VALUE ? 0.5 : zoomValue;

		setChanged();
		notifyObservers(Component.ZOOM);
	}

	boolean showCutPaths() {
		return showCutPaths;
	}

	boolean showLiftPoints() {
		return showLiftPoints;
	}

	boolean showPreviousLayer() {
		return showPreviousLayer;
	}

	boolean showSlices() {
		return showSlices;
	}

	void toggleShowCutPaths(boolean selected) {
		this.showCutPaths = selected;

		setChanged();
		notifyObservers();
	}

	void toggleShowLiftPoints(boolean selected) {
		this.showLiftPoints = selected;

		setChanged();
		notifyObservers();
	}

	void toggleShowPreviousLayer(boolean selected) {
		this.showPreviousLayer = selected;

		setChanged();
		notifyObservers();
	}

	void toggleShowSlice(boolean selected) {
		this.showSlices = selected;

		setChanged();
		notifyObservers();
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == this.part) {
			this.setCurrentPart(part);
		} else if (o == this.part.getLayers().get(layerNumber)) {
			setChanged();
			notifyObservers(Component.LAYER);
		}
	}


	public enum Component {
		PART, LAYER, SELECTED_BIT, ZOOM, SLICE
	}

	boolean showIrregularBits() {
		return showIrregularBits;
	}

	void toggleShowIrregularBits(boolean selected) {
		this.showIrregularBits = selected;

		setChanged();
		notifyObservers();
	}

	private boolean onAddingBits = false;

	boolean isOnAddingBits() {
		return onAddingBits;
	}

	void setOnAddingBits(boolean onAddingBits) {
		this.onAddingBits = onAddingBits;
		setChanged();
		notifyObservers();
	}

	// New bit config
	final DoubleParam newBitsLengthParam = new DoubleParam(
			"newBitLength",
			"Bit length",
			"Length of bits to add",
			1.0, CraftConfig.bitLength,
			CraftConfig.bitLength, 1.0);
	final DoubleParam newBitsWidthParam = new DoubleParam(
			"newBitWidth",
			"Bit width",
			"Length of bits to add",
			1.0, CraftConfig.bitWidth,
			CraftConfig.bitWidth, 1.0);
	final DoubleParam newBitsOrientationParam = new DoubleParam(
			"newBitOrientation",
			"Bit orientation",
			"Angle of bits in respect to that of layer",
			0.0, 360.0, 0.0, 0.01);

	void addNewBits(Point2D.Double position) {
        if (part == null || !part.isPaved() || position == null) return;
		Vector2 lOrientation = Vector2.getEquivalentVector(newBitsOrientationParam.getCurrentValue());
		Layer l = part.getLayers().get(layerNumber);
		AffineTransform inv = new AffineTransform();
		try {
            inv = l.getFlatPavement().getAffineTransform().createInverse();
		} catch (NoninvertibleTransformException e) {
			// Ignore
		}
		final AffineTransform finalInv = inv;
		finalInv.transform(position, position);
		Vector2 origin = new Vector2(position.x, position.y);
		// Add
		l.addBit(new Bit2D(origin, lOrientation,
				newBitsLengthParam.getCurrentValue(),
				newBitsWidthParam.getCurrentValue()));
	}
}