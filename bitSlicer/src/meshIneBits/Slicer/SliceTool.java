package meshIneBits.Slicer;

import java.util.Observable;
import java.util.Vector;

import meshIneBits.GeneratedPart;
import meshIneBits.Model;
import meshIneBits.Config.CraftConfig;
import meshIneBits.util.Logger;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Triangle;
import meshIneBits.util.Vector3;

/**
 * The slice tool slices the model into slices, it does so by going trough all
 * model triangles and slice those into 2D lines. <br>
 * <img src="./doc-files/slices.png"> <br>
 * For each slice, it go through all the triangles of the model. If the z of the
 * slice is between the z_min and the z_max of the triangle, then it call the
 * method {@link Triangle#project2D(double)}, which returns a
 * {@link Segment2D} (class representing in a plane since it admits only Coordinates in
 * x and y). Each segment is then recorded in the {@link Slice} object. At the end of
 * {@link #sliceModel()} we get a collection of slices composed of segments that form
 * the outline of the part.
 */
public class SliceTool extends Observable implements Runnable {
	private Model model;
	private Thread t;
	private GeneratedPart part;
	private Vector<Slice> slices = new Vector<Slice>();

	/**
	 * SliceTool register itself to a {@link GeneratedPart}, which is udpated when the slicing is finished.
	 * @param part {@link GeneratedPart} that will collect the slices when slicing is finished.
	 * @param model {@link Model} to slice.
	 */
	public SliceTool(GeneratedPart part, Model model) {
		this.part = part;
		addObserver(part);
		this.model = model;
	}

	/**
	 * @return {@link Vector} of {@link Slice}
	 */
	public Vector<Slice> getSlices() {
		return slices;
	}

	/**
	 * Slicing algorithm.
	 */
	@Override
	public void run() {
		double sliceHeight = CraftConfig.sliceHeight;
		Vector3 modelMax = model.getMax();
		double firstSliceHeight = (CraftConfig.firstSliceHeightPercent) / 100.0;
		int sliceCount = (int) ((modelMax.z / sliceHeight) + firstSliceHeight);

		int firstSlice = 0;
		int lastSlice = sliceCount;

		Logger.updateStatus("Slicing slices");

		for (int i = firstSlice; i < lastSlice; i++) {
			slices.add(new Slice());
		}
		int n = 0;
		for (Triangle t : model.getTriangles()) {
			Logger.setProgress(n++, model.getTriangles().size());

			double zMin = t.point[0].z;
			double zMax = t.point[0].z;
			if (t.point[1].z < zMin) {
				zMin = t.point[1].z;
			}
			if (t.point[2].z < zMin) {
				zMin = t.point[2].z;
			}
			if (t.point[1].z > zMax) {
				zMax = t.point[1].z;
			}
			if (t.point[2].z > zMax) {
				zMax = t.point[2].z;
			}
			for (int i = (int) ((zMin / sliceHeight) + firstSliceHeight); i <= (int) ((zMax / sliceHeight) + firstSliceHeight); i++) {
				if ((i >= firstSlice) && (i < lastSlice)) {
					double sliceZ = ((i) + firstSliceHeight) * sliceHeight;
					Segment2D s = t.project2D(sliceZ);
					if (s != null) {
						slices.get(i - firstSlice).addModelSegment(s);
					}
				}
			}
		}

		Logger.updateStatus("Optimizing slices");
		for (int i = 0; i < slices.size(); i++) {
			Logger.setProgress(i, slices.size() - 1);
			slices.get(i).optimize();
		}

		Logger.updateStatus("Slice count: " + slices.size());
		setChanged();
		notifyObservers(part);
	}

	/**
	 * Start the slicing in a thread.
	 */
	public void sliceModel() {
		t = new Thread(this);
		t.start();
	}
}
