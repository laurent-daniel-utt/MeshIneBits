package meshIneBits;

import java.io.File;

import meshIneBits.config.CraftConfig;
import meshIneBits.config.CraftConfigLoader;
import meshIneBits.gui.MainWindow;
import meshIneBits.util.Logger;

/**
 * Main class. Call {@link #main(String[])} to start MeshIneBits.
 * Will first load the configuration and then initialize the GUI. Then wait for {@link #sliceModel(String)} to be called to create a {@link GeneratedPart}.
 * @see CraftConfigLoader
 * @see GeneratedPart
 */
public class MeshIneBitsMain {
	/**
	 * The Main method is the entry point of the program.
	 * @param args
	 * 	Program start's arguments, not used yet.
	 */
	public static void main(String[] args) {
		// Load the configuration
		CraftConfigLoader.loadConfig(null);

		// Load the graphical interface
		MainWindow.getInstance();
	}

	/**
	 * Call this method to slice the model, create a {@link GeneratedPart} and be able to generate a pattern.
	 * @param filename
	 * 	{@link File#toString()}
	 * @see GeneratedPart
	 */
	public static void sliceModel(String filename) {
		CraftConfig.lastSlicedFile = filename;
		CraftConfigLoader.saveConfig(null);

		Model m;
		try {
			m = new Model(filename);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("Failed to load model");
			return;
		}
		m.center();

		GeneratedPart part = new GeneratedPart(m);

		MainWindow.getInstance().setPart(part);
	}
}
