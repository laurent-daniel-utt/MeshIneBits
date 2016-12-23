package meshIneBits.gui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Icon;

import meshIneBits.GeneratedPart;
import meshIneBits.util.Vector2;

public class ViewObservable extends Observable implements Observer{
	
	private GeneratedPart part = null;
	private int layerNumber = 0;
	private int sliceNumber = 0;
	private Vector2 selectedBitKey = null;
	private double zoom = 0.8;
	private boolean showSlices = true;
	private boolean showLiftPoints = false;
	static private ViewObservable instance;
	
	public enum Component {
		PART, LAYER, SELECTED_BIT, ZOOM, SLICE
	}
	
	private ViewObservable(){
		
	}
	
	public static ViewObservable getInstance(){
		if(instance == null)
			instance = new ViewObservable();
		
		return instance;
	}
	
//	public void letObserversKnowMe(){
//		setChanged();
//		notifyObservers(Component.ME);
//	}
	
	public void setPart(GeneratedPart part){
		if (this.part == null && part != null) {
			part.addObserver(this);
		}
		
		this.part = part;
		
		layerNumber = 0;
		selectedBitKey = null;
		
//		if(part != null && part.isSliced()) {
			MainWindow.getInstance().refresh();
			setChanged();
			notifyObservers(Component.PART);
//		}
	}
	
	public void setLayer(int nbrLayer){
		if(part == null)
			return;
		if(!part.isGenerated())
			return;
		if(nbrLayer >= part.getLayers().size() || nbrLayer < 0)
			return;
		layerNumber = nbrLayer;
		selectedBitKey = null;
		setChanged();
		notifyObservers(Component.LAYER);
	}
	
	public void setSlice(int nbrSlice){
		if(part == null)
			return;
		if(nbrSlice >= part.getSlices().size() || nbrSlice < 0)
			return;
		sliceNumber = nbrSlice;
		setChanged();
		notifyObservers(Component.SLICE);
	}
	
	public void setSelectedBitKey(Vector2 bitKey){
		if(part == null)
			return;
		selectedBitKey = bitKey;
		setChanged();
		notifyObservers(Component.SELECTED_BIT);
	}
	
	public void setZoom(double zoomValue){
		if(part == null)
			return;
		zoom = zoomValue;
		if (zoom < 0.2)
			zoom = 0.2;
		setChanged();
		notifyObservers(Component.ZOOM);
	}
	
	public GeneratedPart getCurrentPart(){
		return part;
	}
	
	public int getCurrentLayerNumber(){
		return layerNumber;
	}
	
	public int getCurrentSliceNumber(){
		return sliceNumber;
	}
	
	public Vector2 getSelectedBitKey(){
		return selectedBitKey;
	}
	
	public double getZoom(){
		return zoom;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == this.part)
			this.setPart(part);
	}

	public void toggleShowSlice(boolean selected) {
		this.showSlices = selected;
		setChanged();
		notifyObservers();
	}
	
	public boolean showSlices() {
		return showSlices;
	}
	
	public void toggleShowLiftPoints(boolean selected) {
		this.showLiftPoints = selected;
		setChanged();
		notifyObservers();
	}

	public boolean showLiftPoints() {
		return showLiftPoints ;
	}
}
