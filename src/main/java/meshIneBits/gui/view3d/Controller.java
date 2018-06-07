package meshIneBits.gui.view3d;

import meshIneBits.GeneratedPart;

import java.util.Observable;
import java.util.Observer;

/**
 * Observe {@link GeneratedPart} and
 */
public class Controller extends Observable implements Observer {

	private GeneratedPart part;

	private static Controller instance;

	public static Controller getInstance() {
		if (instance == null)
			instance = new Controller();
		return instance;
	}

	private Controller() {
	}

	GeneratedPart getCurrentPart() {
		return part;
	}

	/**
	 * @param part <tt>null</tt> to delete the current mesh
	 */
	void setCurrentPart(GeneratedPart part) {
		if (part != null) {
			this.part = part;
			part.addObserver(this);
		} else
			this.part = null;

		setChanged();
		notifyObservers();
	}

	@Override
	public void update(Observable o, Object arg) {

	}

	int getCurrentLayerNumber() {
		// TODO Implement toolbox in 3D view
		return part.getLayers().size() - 1;
	}
}
