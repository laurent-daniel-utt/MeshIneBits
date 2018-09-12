/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package meshIneBits.slicer;

import java.util.Observable;
import java.util.Vector;

import meshIneBits.Mesh;
import meshIneBits.Model;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Logger;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Triangle;
import meshIneBits.util.Vector3;

/**
 * The slice tool slices the model into slices, it does so by going trough all
 * model triangles and slice those into 2D lines. <br>
 * <img src="./doc-files/slices.png" alt=""> <br>
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
	private Mesh part;
	private Vector<Slice> slices = new Vector<Slice>();

	/**
	 * SliceTool register itself to a {@link Mesh}, which is udpated when the slicing is finished.
	 * @param part {@link Mesh} that will collect the slices when slicing is finished.
	 * @param model {@link Model} to slice.
	 */
	public SliceTool(Mesh part, Model model) {
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
