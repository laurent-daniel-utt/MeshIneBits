package bitSlicer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.SwingUtilities;

import bitSlicer.Slicer.Config.CraftConfigLoader;
import bitSlicer.gui.ConfigWindow;
import bitSlicer.Layer;
import bitSlicer.Model;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.Slicer.Slice;
import bitSlicer.Slicer.SliceTool;
import bitSlicer.gui.PreviewFrame;
import bitSlicer.util.Logger;
import bitSlicer.util.Vector2;

public class BitSlicerMain {
	public static void main(String[] args)
	{
		CraftConfigLoader.loadConfig(null);
		
		new ConfigWindow();
		
		CraftConfigLoader.saveConfig(null);
	}
	
	public static void sliceModel(String filename)
	{
		long startTime = System.currentTimeMillis();
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
	}
}
