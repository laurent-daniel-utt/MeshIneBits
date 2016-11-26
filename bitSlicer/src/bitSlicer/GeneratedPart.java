package bitSlicer;

import java.util.Hashtable;
import java.util.Vector;

import bitSlicer.PatternTemplates.PatternTemplate;
import bitSlicer.PatternTemplates.PatternTemplate1;
import bitSlicer.PatternTemplates.PatternTemplate2;
import bitSlicer.Slicer.Slice;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.Logger;
import bitSlicer.util.Segment2D;
import bitSlicer.util.Vector2;
import bitSlicer.util.Vector3;

public class GeneratedPart {
	
	private Vector<Slice> slices;
	private double skirtRadius;
	private Vector<Layer> layers = new Vector<Layer>();
	private PatternTemplate patternTemplate;
	private Hashtable<Vector3, Bit3D> map3DBits;
	
	public GeneratedPart(Vector<Slice> slices){
		this.slices = slices;
		setSkirtRadius();
		build();
	}
	
	/*
	 * skirtRadius is the radius of the cylinder that fully contains the part.
	 */
	public void setSkirtRadius(){
		
		double radius = 0;
		
		for (Slice s : slices){
			for (Segment2D segment : s.getSegmentList()){
				if (segment.start.vSize2() > radius)
					radius = segment.start.vSize2();
				if(segment.end.vSize2() > radius)
					radius = segment.end.vSize2();
			}
		}
		
		skirtRadius = Math.sqrt(radius);
		
		System.out.println("Skirt's radius: " + ((int) skirtRadius + 1) + " mm");
	}
	
	public void build(){
		setPatternTemplate(CraftConfig.patternNumber);
		buildLayers();
	}
	
	public void setPatternTemplate(int templateNumber){
		switch (templateNumber){
		case 1:
			patternTemplate = new PatternTemplate1(skirtRadius);
			break;
		case 2:
			patternTemplate = new PatternTemplate2(skirtRadius);
		}
	}
	
	public PatternTemplate getPatternTemplate(){
		return patternTemplate;
	}
	
	public void buildLayers(){
		
		@SuppressWarnings("unchecked")
		Vector<Slice> slicesCopy = (Vector<Slice>) slices.clone();
		double bitThickness = CraftConfig.bitThickness;
		double sliceHeight = CraftConfig.sliceHeight;
		double z = CraftConfig.firstSliceHeightPercent * sliceHeight / 100;
		int layerNumber = 1;
		int progress = 0;
		int progressGoal = slicesCopy.size();
		
		Logger.updateStatus("Generating Layers");
		while (!slicesCopy.isEmpty()){
			Vector<Slice> includedSlices = new Vector<Slice>();
			while ((z < bitThickness * layerNumber) && !slicesCopy.isEmpty()){
				includedSlices.add(slicesCopy.get(0));
				slicesCopy.remove(0);
				z = z + sliceHeight;
				Logger.setProgress(progress++, progressGoal);
			}
			layers.add(new Layer(includedSlices, layerNumber, this));
			layerNumber++;
		}
		System.out.println("Layer count: " + (layerNumber - 1));
	}
	
	public Vector<Layer> getLayers() {
		return this.layers;
	}

}
