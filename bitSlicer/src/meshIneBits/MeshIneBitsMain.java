package meshIneBits;

import java.io.IOException;
import java.util.Vector;

import meshIneBits.Slicer.Slice;
import meshIneBits.Slicer.SliceTool;
import meshIneBits.Slicer.Config.CraftConfig;
import meshIneBits.Slicer.Config.CraftConfigLoader;
import meshIneBits.gui.MainWindow;
import meshIneBits.util.Logger;

public class MeshIneBitsMain {
	public static void main(String[] args) {
		CraftConfigLoader.loadConfig(null);

		MainWindow.getInstance();
		//		new ConfigWindow();
	}

	public static void sliceModel(String filename) {
		new Thread(new Runnable() {
			@Override
			public void run() {
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

				SliceTool slicer = new SliceTool(m);
				final Vector<Slice> slices = slicer.sliceModel();

				GeneratedPart part = new GeneratedPart(slices);
				
				MainWindow.getInstance().setPart(part);
				
				//		new PreviewFrame(part);
				//		new MainWindow(part);
			}
		}).start();
	}
}
