package meshIneBits;

import java.io.IOException;
import java.util.Vector;

import meshIneBits.Config.CraftConfig;
import meshIneBits.Config.CraftConfigLoader;
import meshIneBits.Slicer.Slice;
import meshIneBits.Slicer.SliceTool;
import meshIneBits.gui.MainWindow;
import meshIneBits.util.Logger;

public class MeshIneBitsMain {
	public static void main(String[] args) {
		CraftConfigLoader.loadConfig(null);

		MainWindow.getInstance();
		//		new ConfigWindow();
	}

	public static void sliceModel(String filename) {
		CraftConfig.lastSlicedFile = filename;
		CraftConfigLoader.saveConfig(null);

		Model m;
		try {
			m = new Model(filename);
		} catch (IOException e) {
			e.printStackTrace();
			Logger.error("Failed to load model");
			return;
		}
		m.center();

		GeneratedPart part = new GeneratedPart(m);

		MainWindow.getInstance().setPart(part);
	}
}
