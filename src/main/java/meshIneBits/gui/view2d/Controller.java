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

import meshIneBits.Bit2D;
import meshIneBits.Bit3D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.util.Vector2;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The controller, linking 2D views and part (so-called {@link GeneratedPart}).
 * It observes the {@link GeneratedPart} and its {@link meshIneBits.Layer
 * Layers}. Observed by {@link Core} and {@link Wrapper}. A singleton.
 */
class Controller extends Observable implements Observer {

	static private Controller instance;
	private GeneratedPart part = null;
	private int layerNumber = 0;
	private int sliceNumber = 0;
	private Set<Vector2> selectedBitKeys = new HashSet<>();
	private double zoom = 1;
	private boolean showSlices = false;
	private boolean showLiftPoints = false;
	private boolean showPreviousLayer = false;
	private boolean showCutPaths = false;
	private boolean showIrregularBits = false;

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

	GeneratedPart getCurrentPart() {
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

	public void setLayer(int nbrLayer) {
		if (part == null) {
			return;
		}
		if (!part.isGenerated()) {
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
	void setCurrentPart(GeneratedPart part) {
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
		stopSelectingMultiplePoints();
	}

	/**
	 * Add new bit key to {@link #selectedBitKeys} and remove if already present
	 * @param bitKey in layer's coordinate system
	 */
	void addOrRemoveSelectedBitKeys(Vector2 bitKey) {
		if (part == null || !part.isGenerated()) {
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
			reset();

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

	private boolean onSelectingMultiplePoints = false;

	boolean isOnSelectingMultiplePoints() {
		return onSelectingMultiplePoints;
	}

	void startSelectingMultiplePoints() {
		onSelectingMultiplePoints = true;
		selectedPoints = new HashSet<>();
		setChanged();
		notifyObservers();
	}

	void stopSelectingMultiplePoints() {
		onSelectingMultiplePoints = false;
		selectedPoints.clear();
		setChanged();
		notifyObservers();
	}

	/**
	 * Points in coordinate system of layer
	 */
	private Set<Point2D.Double> selectedPoints = new HashSet<>();

	Set<Point2D.Double> getSelectedPoints() {
		return selectedPoints;
	}

	/**
	 * Add a new point to {@link #selectedPoints} or remove if we already have
	 *
	 * @param newPoint in coordinate system of view not zooming
	 */
	void addOrRemovePoint(Point2D.Double newPoint) {
		if (!selectedPoints.add(newPoint))
			selectedPoints.remove(newPoint);
		setChanged();
		notifyObservers();
	}

	/**
	 * Add new bits for each points saved in {@link #selectedPoints}
	 * @param length new bit's
	 * @param width new bit's
	 * @param orientation new bit's
	 */
	void addNewBits(double length, double width, double orientation) {
		if (part == null || !part.isGenerated() || selectedPoints == null) return;
		Vector2 lOrientation = Vector2.getEquivalentVector(orientation);
		Layer l = part.getLayers().get(layerNumber);
		AffineTransform inv = new AffineTransform();
		try {
			 inv = l.getSelectedPattern().getAffineTransform().createInverse();
		} catch (NoninvertibleTransformException e) {
			// Ignore
		}
		final AffineTransform finalInv = inv;
		selectedPoints.forEach((Point2D.Double p) ->
		{
			// Convert coordinates into layer's system
			finalInv.transform(p, p);
			Vector2 origin = new Vector2(p.x, p.y);
			// Add
			l.addBit(new Bit2D(origin, lOrientation, length, width));
		});
	}
}