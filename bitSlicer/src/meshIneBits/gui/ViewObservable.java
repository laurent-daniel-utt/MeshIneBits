package meshIneBits.gui;

import java.util.Observable;
import java.util.Observer;

import meshIneBits.GeneratedPart;
import meshIneBits.util.Vector2;

public class ViewObservable extends Observable implements Observer {

	static private ViewObservable instance;
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

	public static ViewObservable getInstance() {
		if (instance == null) {
			instance = new ViewObservable();
		}

		return instance;
	}

	private ViewObservable() {
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

	public void setPart(GeneratedPart part) {
		if ((this.part == null) && (part != null)) {
			part.addObserver(this);
		}

		this.part = part;

		setLayer(0);
		setSelectedBitKey(null);

		MainWindow.getInstance().refresh();
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
	
	public void toggleShowIrregularBits(boolean selected){
		this.showIrregularBits = selected;
		
		setChanged();
		notifyObservers();
	}
	
}
