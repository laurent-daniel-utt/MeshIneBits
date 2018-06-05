package meshIneBits.gui.view2d;

import meshIneBits.Bit2D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.util.Vector2;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * The controller, linking 2D views and part (so-called {@link GeneratedPart}).
 * It observes the {@link GeneratedPart} and its {@link meshIneBits.Layer
 * Layers}. Observed by {@link Core} and {@link Wrapper}. A singleton.
 */
public class Controller extends Observable implements Observer {

	static private Controller instance;
	private GeneratedPart part = null;
	private int layerNumber = 0;
	private int sliceNumber = 0;
	private Vector2 selectedBitKey = null;
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

	public int getCurrentLayerNumber() {
		return layerNumber;
	}

	public GeneratedPart getCurrentPart() {
		return part;
	}

	public int getCurrentSliceNumber() {
		return sliceNumber;
	}

	public Vector2 getSelectedBitKey() {
		return selectedBitKey;
	}

	public double getZoom() {
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
		setSelectedBitKey(null);

		setChanged();
		notifyObservers(Component.LAYER);
	}

	/**
	 * Put a generated part under observation of this controller. Also signify
	 * {@link Core} and {@link Wrapper} to repaint.
	 *
	 * @param part should not be null
	 */
	public void setPart(GeneratedPart part) {
		if ((this.part == null) && (part != null)) {
			part.addObserver(this);
		}

		this.part = part;

		setLayer(0);
		setSelectedBitKey(null);

		setChanged();
		notifyObservers(Component.PART);
	}

	public void setSelectedBitKey(Vector2 bitKey) {
		if (part == null) {
			return;
		}
		selectedBitKey = bitKey;

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

	public void setZoom(double zoomValue) {
		if (part == null) {
			return;
		}
		zoom = zoomValue;
		if (zoom < 0.5) {
			zoom = 0.5;
		}

		setChanged();
		notifyObservers(Component.ZOOM);
	}

	public boolean showCutPaths() {
		return showCutPaths;
	}

	public boolean showLiftPoints() {
		return showLiftPoints;
	}

	public boolean showPreviousLayer() {
		return showPreviousLayer;
	}

	public boolean showSlices() {
		return showSlices;
	}

	public void toggleShowCutPaths(boolean selected) {
		this.showCutPaths = selected;

		setChanged();
		notifyObservers();
	}

	public void toggleShowLiftPoints(boolean selected) {
		this.showLiftPoints = selected;

		setChanged();
		notifyObservers();
	}

	public void toggleShowPreviousLayer(boolean selected) {
		this.showPreviousLayer = selected;

		setChanged();
		notifyObservers();
	}

	public void toggleShowSlice(boolean selected) {
		this.showSlices = selected;

		setChanged();
		notifyObservers();
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == this.part) {
			this.setPart(part);
		} else if (o == this.part.getLayers().get(layerNumber)) {
			setSelectedBitKey(null);

			setChanged();
			notifyObservers(Component.LAYER);
		}
	}


	public enum Component {
		PART, LAYER, SELECTED_BIT, ZOOM, SLICE
	}

	public boolean showIrregularBits() {
		return showIrregularBits;
	}

	public void toggleShowIrregularBits(boolean selected) {
		this.showIrregularBits = selected;

		setChanged();
		notifyObservers();
	}

	private boolean onSelectingMultiplePoints = false;

	boolean isOnSelectingMultiplePoints() {
		return onSelectingMultiplePoints;
	}

	public void startSelectingMultiplePoints() {
		onSelectingMultiplePoints = true;
		selectedPoints = new HashSet<>();
		setChanged();
		notifyObservers();
	}

	public void stopSelectingMultiplePoints() {
		onSelectingMultiplePoints = false;
		selectedPoints = null;
		setChanged();
		notifyObservers();
	}

	/**
	 * Points in coordinate system of layer
	 */
	private Set<Point2D.Double> selectedPoints = null;

	public Set<Point2D.Double> getSelectedPoints() {
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

	public void addNewBits(double length, double width, double orientation) {
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