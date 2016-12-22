package meshIneBits.gui;

import java.util.Observable;

import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.util.Vector2;

public class ShowedView extends Observable {
	
	private GeneratedPart part = null;
	private Layer layer = null;
	private Vector2 selectedBitKey = null;
	private double zoom = 1;
	
	public enum Component {
		PART, LAYER, SELECTED_BIT, ZOOM, ME
	}
	
	public ShowedView(){
	
	}
	
	public void letObserversKnowMe(){
		setChanged();
		notifyObservers(Component.ME);
	}
	
	public void setPart(GeneratedPart part){
		this.part = part;
		layer = part.getLayers().get(0);
		selectedBitKey = null;
		setChanged();
		notifyObservers(Component.PART);
	}
	
	public void setLayer(int nbrLayer){
		layer = part.getLayers().get(nbrLayer);
		selectedBitKey = null;
		setChanged();
		notifyObservers(Component.LAYER);
	}
	
	public void setSelectedBitKey(Vector2 bitKey){
		selectedBitKey = bitKey;
		setChanged();
		notifyObservers(Component.SELECTED_BIT);
	}
	
	public void setZoom(double zoomValue){
		zoom = zoomValue;
		setChanged();
		notifyObservers(Component.ZOOM);
	}
	
	public GeneratedPart getCurrentPart(){
		return part;
	}
	
	public Layer getCurrentLayer(){
		return layer;
	}
	
	public Vector2 getSelectedBitKey(){
		return selectedBitKey;
	}
	
	public double getZoom(){
		return zoom;
	}
}
