package meshIneBits;

import java.io.File;

import meshIneBits.config.CraftConfig;
import meshIneBits.config.CraftConfigLoader;
import meshIneBits.gui.MainWindow;
import meshIneBits.gui.view3d.ProcessingModelView;
import meshIneBits.util.Logger;

/**
 * Main class. Call {@link #main(String[])} to start MeshIneBits. Will first
 * load the configuration and then initialize the GUI. Then wait for
 * {@link #sliceModel(String)} to be called to create a {@link GeneratedPart}.
 * 
 * @see CraftConfigLoader
 * @see GeneratedPart
 */
public class MeshIneBitsMain {
	/**
	 * The Main method is the entry point of the program.
	 * 
	 * @param args
	 *            Program start's arguments, not used yet.
	 */
	public static void main(String[] args) {
		// Load the configuration
		CraftConfigLoader.loadConfig(null);

		// Load the graphical interface
		MainWindow.getInstance();
	}
}