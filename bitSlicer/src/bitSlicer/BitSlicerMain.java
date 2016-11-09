package bitSlicer;

import java.io.IOException;
import java.util.Vector;

import bitSlicer.PatternTemplates.PatternTemplate;
import bitSlicer.PatternTemplates.PatternTemplate1;
import bitSlicer.PatternTemplates.PatternTemplate2;
import bitSlicer.Slicer.Slice;
import bitSlicer.Slicer.SliceTool;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.Slicer.Config.CraftConfigLoader;
import bitSlicer.gui.ConfigWindow;
import bitSlicer.util.Logger;
import bitSlicer.util.Segment2D;
import bitSlicer.util.Vector2;
import bitSlicer.gui.PreviewFrame;

public class BitSlicerMain {
	public static void main(String[] args)
	{
		CraftConfigLoader.loadConfig(null);
		new ConfigWindow();
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
		
		//buildLayers(slices);

		new PreviewFrame(slices);
	}
	
	/*
	 * skirtRadius is the radius of the cylinder that fully contains the part.
	 */
	public double getSkirtRadius(Vector<Slice> slices){
		
		double radius = 0;
		
		for (Slice s : slices){
			for (Segment2D segment : s.getSegmentList()){
				if (segment.start.vSize2() > radius)
					radius = segment.start.vSize2();
				if(segment.end.vSize2() > radius)
					radius = segment.end.vSize2();
			}
		}
		
		return Math.sqrt(radius);
	}
	
	public static void setPatternTemplate(int templateNumber, Vector2 rotation, Vector2 offSet, double skirtRadius){
		PatternTemplate patternTemplate;
		switch (templateNumber){
		case 1:
			patternTemplate = new PatternTemplate1(rotation, offSet, skirtRadius);
			break;
		case 2:
			patternTemplate = new PatternTemplate2(rotation, offSet, skirtRadius);
		}
	}
	
	
	public static void buildLayers(Vector<Slice> slices){
		
		Vector<Layer> layers = new Vector<Layer>();
		double bitThickness = CraftConfig.bitThickness;
		double sliceHeight = CraftConfig.sliceHeight;
		double z = CraftConfig.firstSliceHeightPercent * sliceHeight;
		int layerNumber = 1;
		
		while (!slices.isEmpty()){
			Vector<Slice> includedSlices = new Vector<Slice>();
			while ((z < bitThickness * layerNumber) && !slices.isEmpty()){
				includedSlices.add(slices.get(0));
				slices.remove(0);
				z = z + bitThickness;
			}
			layers.add(new Layer(includedSlices, layerNumber));
			layerNumber++;
		}
	}
	
}
