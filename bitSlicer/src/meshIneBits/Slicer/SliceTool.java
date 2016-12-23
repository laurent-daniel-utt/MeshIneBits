package meshIneBits.Slicer;

import java.util.Observable;
import java.util.Vector;

import meshIneBits.GeneratedPart;
import meshIneBits.Model;
import meshIneBits.Config.CraftConfig;
import meshIneBits.Slicer.Slice;
import meshIneBits.util.Logger;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Triangle;
import meshIneBits.util.Vector3;

/**
 * The slice tool slices the model into slices, it does so by going trough all model triangles and
 * slice those into 2D lines.
 */
public class SliceTool extends Observable implements Runnable
{
	private Model model;
	private Thread t;
	private GeneratedPart o;
	private Vector<Slice> slices = new Vector<Slice>();
	
	public SliceTool(GeneratedPart o, Model model)
	{
		this.o = o;
		addObserver(o);
		this.model = model;
	}
	
	public Vector<Slice> getSlices() {
		return slices;
	}
	
	public void sliceModel() {
		t = new Thread(this);
		t.start();
	}
	
	public void run()
	{
		double sliceHeight = CraftConfig.sliceHeight;
		Vector3 modelMax = model.getMax();
		double firstSliceHeight = ((double) CraftConfig.firstSliceHeightPercent) / 100.0;
		int sliceCount = (int) (modelMax.z / sliceHeight + firstSliceHeight);
		
		int firstSlice = 0;
		int lastSlice = sliceCount;
		
		Logger.updateStatus("Slicing slices");
		
		for (int i = firstSlice; i < lastSlice; i++)
		{
			slices.add(new Slice());
		}
		int n = 0;
		for (Triangle t : model.getTriangles())
		{
			Logger.setProgress(n++, model.getTriangles().size());
			
			double zMin = t.point[0].z;
			double zMax = t.point[0].z;
			if (t.point[1].z < zMin)
				zMin = t.point[1].z;
			if (t.point[2].z < zMin)
				zMin = t.point[2].z;
			if (t.point[1].z > zMax)
				zMax = t.point[1].z;
			if (t.point[2].z > zMax)
				zMax = t.point[2].z;
			for (int i = (int) (zMin / sliceHeight + firstSliceHeight); i <= (int) (zMax / sliceHeight + firstSliceHeight); i++)
			{
				if (i >= firstSlice && i < lastSlice)
				{
					double sliceZ = (((double) i) + firstSliceHeight) * sliceHeight;
					Segment2D s = t.project2D(sliceZ);
					if (s != null)
						slices.get(i - firstSlice).addModelSegment(s);
				}
			}
		}
		
		Logger.updateStatus("Optimizing slices");
		for (int i = 0; i < slices.size(); i++)
		{
			Logger.setProgress(i, slices.size()-1);
			slices.get(i).optimize();
		}
		
		Logger.updateStatus("Slice count: " + slices.size());
		setChanged();
		notifyObservers(o);
	}
}
