package bitSlicer.Slicer;

import java.util.Vector;

import bitSlicer.Slicer.Slice;
import bitSlicer.Model;
import bitSlicer.util.Segment2D;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.Logger;
import bitSlicer.util.Triangle;
import bitSlicer.util.Vector3;

/**
 * The slice tool slices the model into slices, it does so by going trough all model triangles and
 * slice those into 2D lines.
 */
public class SliceTool
{
	private Model model;
	
	public SliceTool(Model model)
	{
		this.model = model;
	}
	
	public Vector<Slice> sliceModel()
	{
		Vector<Slice> slices = new Vector<Slice>();
		
		double sliceHeight = CraftConfig.sliceHeight;
		Vector3 modelMax = model.getMax();
		double firstSliceHeight = ((double) CraftConfig.firstSliceHeightPercent) / 100.0;
		int sliceCount = (int) (modelMax.z / sliceHeight + firstSliceHeight);
		
		int firstSlice = 0;
		int lastSlice = sliceCount;
		
		Logger.updateStatus("Slicing slices");
		Logger.message("Slicing " + (lastSlice - firstSlice) + " slices");
		for (int i = firstSlice; i < lastSlice; i++)
		{
			slices.add(new Slice());
		}
		int n = 0;
		for (Triangle t : model.triangles)
		{
			Logger.setProgress(n++, model.triangles.size());
			
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
			Logger.setProgress(i, slices.size());
			slices.get(i).optimize();
		}
		return slices;
	}
}
