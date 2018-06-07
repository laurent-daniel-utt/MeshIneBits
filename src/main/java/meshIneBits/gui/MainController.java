package meshIneBits.gui;

import meshIneBits.GeneratedPart;

import java.util.Observable;
import java.util.Observer;

/**
 * Observe {@link GeneratedPart} and observed by {@link Toolbar}
 */

public class MainController extends Observable implements Observer {

	static private MainController instance;
	private GeneratedPart currentPart;

	private MainController() {

	}

	public static MainController getInstance() {
		if (instance == null)
			instance = new MainController();
		return instance;
	}

	void setCurrentPart(GeneratedPart gp) {
		currentPart = gp;
		if (gp != null)
			currentPart.addObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg == null) return;
		if (o == currentPart) {
			switch ((GeneratedPart.Events) arg) {
				case MODEL_LOADED:
					MainWindow.getInstance().get3DView().setCurrentPart(currentPart);
					break;
				case SLICED:
					MainWindow.getInstance().get2DView().setCurrentPart(currentPart);
					break;
				case PAVED:
					break;
				case AUTO_OPTIMIZED:
					break;
				default:
					MainWindow.getInstance().get2DView().setCurrentPart(null);
					MainWindow.getInstance().get3DView().setCurrentPart(null);
					break;
			}
		}
	}

	public GeneratedPart getCurrentPart() {
		return currentPart;
	}
}
