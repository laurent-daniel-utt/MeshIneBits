package meshIneBits.gui;

import java.util.Observable;
import java.util.Observer;

import meshIneBits.GeneratedPart;
import meshIneBits.util.Vector2;

public class ViewObservable extends Observable implements Observer{
	
	private GeneratedPart part = null;
	private int layerNumber = 0;
	private Vector2 selectedBitKey = null;
	private double zoom = 0.8;
	
	public enum Component {
		PART, LAYER, SELECTED_BIT, ZOOM, ME
	}
	
	public ViewObservable(){
		
	}
	
	public void letObserversKnowMe(){
		setChanged();
		notifyObservers(Component.ME);
	}
	
	public void setPart(GeneratedPart part){
		this.part = part;
		this.part.addObserver(this);
		layerNumber = 0;
		selectedBitKey = null;
		
		if(part != null && part.isSliced()) {
			setChanged();
			notifyObservers(Component.PART);
		}
	}
	
	public void setLayer(int nbrLayer){
		if(part == null)
			return;
		layerNumber = nbrLayer;
		selectedBitKey = null;
		setChanged();
		notifyObservers(Component.LAYER);
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
}
