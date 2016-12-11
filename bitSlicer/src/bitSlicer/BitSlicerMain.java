package bitSlicer;

import java.io.IOException;
import java.util.Vector;

import bitSlicer.Slicer.Slice;
import bitSlicer.Slicer.SliceTool;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.Slicer.Config.CraftConfigLoader;
import bitSlicer.gui.ConfigWindow;
import bitSlicer.gui.MainWindow;
import bitSlicer.util.Logger;
import bitSlicer.gui.PreviewFrame;
import bitSlicer.gui.ProcessingWindow;

public class BitSlicerMain {
	public static void main(String[] args)
	{
		CraftConfigLoader.loadConfig(null);
		
		MainWindow.getInstance();
//		new ConfigWindow();
	}
	
	public static void sliceModel(String filename)
	{
		CraftConfig.lastSlicedFile = filename;
		CraftConfigLoader.saveConfig(null);

		Model m;
		try
		{
			m = new Model(filename);
		} catch (IOException e)
		{
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
}
