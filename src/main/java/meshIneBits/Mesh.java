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

package meshIneBits;

import meshIneBits.config.CraftConfig;
import meshIneBits.patterntemplates.PatternTemplate;
import meshIneBits.slicer.Slice;
import meshIneBits.slicer.SliceTool;
import meshIneBits.util.*;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

/**
 * This object is the equivalent of the part which will be printed
 *
 */
public class Mesh extends Observable implements Runnable, Observer {
	private Vector<Layer> layers = new Vector<Layer>();
	private Vector<Slice> slices = new Vector<Slice>();
	private PatternTemplate patternTemplate;
	private double skirtRadius;
	private Thread t = null;
	private SliceTool slicer;
	private boolean sliced = false;
	private Optimizer optimizer;
	private Model model;

	public Mesh(Model model) {
		slicer = new SliceTool(this, model);
		slicer.sliceModel();
		this.model = model;
	}

	/**
	 * Start the process of generating bits
	 */
	public void buildBits2D() {

		if (t == null || !t.isAlive()) {
			setPatternTemplate();

			this.layers.clear();

			t = new Thread(this);
			t.start();
		}
	}


	private void buildLayers() {
		@SuppressWarnings("unchecked")
		Vector<Slice> slicesCopy = (Vector<Slice>) slices.clone();
		double bitThickness = CraftConfig.bitThickness;
		double sliceHeight = CraftConfig.sliceHeight;
		double layersOffSet = CraftConfig.layersOffset;
		double z = (CraftConfig.firstSliceHeightPercent * sliceHeight) / 100;
		int layerNumber = 0;
		int progress = 0;
		int progressGoal = slicesCopy.size();
		double zBitBottom = 0;
		double zBitRoof = bitThickness;

		Logger.updateStatus("Generating Layers");
		while (!slicesCopy.isEmpty()) {
			Vector<Slice> includedSlices = new Vector<Slice>();
			while ((z <= zBitRoof) && !slicesCopy.isEmpty()) {
				if (z >= zBitBottom) {
					includedSlices.add(slicesCopy.get(0));
				}
				slicesCopy.remove(0);
				z = z + sliceHeight;
				progress++;
				Logger.setProgress(progress, progressGoal);
			}
			if (!includedSlices.isEmpty()) {
				layers.add(new Layer(includedSlices, layerNumber, this));
				layerNumber++;
			}
			zBitBottom = zBitRoof + layersOffSet;
			zBitRoof = zBitBottom + bitThickness;
		}
		Logger.updateStatus("Layer count: " + layerNumber);
	}


	public Vector<Layer> getLayers() {
		if (!isGenerated()) {
			try {
				throw new Exception("Part not generated!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.layers;
	}

	public PatternTemplate getPatternTemplate() {
		return patternTemplate;
	}

	public double getSkirtRadius() {
		return skirtRadius;
	}

	public Vector<Slice> getSlices() {
		if (!sliced) {
			try {
				throw new Exception("Part not sliced!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return slices;
	}

	public boolean isGenerated() {
		if (!layers.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isSliced() {
		return sliced;
	}

	@Override
	public void run() {
		buildLayers();
		detectIrregularBits();
		setChanged();
		notifyObservers(Events.PAVED);
	}

	private void detectIrregularBits() {
		optimizer = new Optimizer(layers);
		optimizer.detectIrregularBits();
	}

	private void setPatternTemplate() {
		patternTemplate = CraftConfig.templateChoice;
		patternTemplate.ready(this);
		Logger.updateStatus("Prepared for generating bits.");
	}

	/**
	 * skirtRadius is the radius of the cylinder that fully contains the part.
	 */

	private void setSkirtRadius() {

		double radius = 0;

		for (Slice s : slices) {
			for (Segment2D segment : s.getSegmentList()) {
				if (segment.start.vSize2() > radius) {
					radius = segment.start.vSize2();
				}
				if (segment.end.vSize2() > radius) {
					radius = segment.end.vSize2();
				}
			}
		}
		skirtRadius = Math.sqrt(radius);
		Logger.updateStatus("Skirt's radius: " + ((int) skirtRadius + 1) + " mm");
	}
	/*
	private void setSkirtRadius() {

		double radius = 0;
		Vector2 center = centerOfSkirtRadius();

		for (Slice s : slices) {
			for (Segment2D segment : s.getSegmentList()) {
				Vector2 tmp1 = new Vector2(Math.abs(segment.start.x - center.x), Math.abs(segment.start.y - center.y));
				Vector2 tmp2 = new Vector2(Math.abs(segment.end.x - center.x), Math.abs(segment.end.y - center.y));
				if (tmp1.vSize2() > radius) {
					radius = tmp1.vSize2();
				}
				if (tmp2.vSize2() > radius) {
					radius = tmp2.vSize2();
				}
			}
		}
		skirtRadius = new Pair<Vector2, Double>(center, Math.sqrt(radius));
		Logger.updateStatus("Skirt's radius: " + ((int) (double)skirtRadius.getValue() + 1) + " mm");
	}

	// the center of the skirtradius is the average center of the slices of the model
	private Vector2 centerOfSkirtRadius(){

		Vector<Vector2> centers = new Vector<Vector2>(slices.size());
		int i = 0;
		for (Slice s : slices) {
			double centroidX = 0;
			double centroidY = 0;
			double signedArea = 0.0;
			double x0 = 0.0; // Current vertex X
			double y0 = 0.0; // Current vertex Y
			double x1 = 0.0; // Next vertex X
			double y1 = 0.0; // Next vertex Y
			double a = 0.0; // Partial signed area
			for (Segment2D segment : s.getSegmentList()) {
				x0 = segment.start.x;
				y0 = segment.start.y;
				x1 = segment.end.x;
				y1 = segment.end.y;
				a = (x0 * y1) - (x1 * y0);
				signedArea += a;
				centroidX += (x0 + x1) * a;
				centroidY += (y0 + y1) * a;
			}
			signedArea *= 0.5;

			centroidX /= (6.0 * signedArea);
			centroidY /= (6.0 * signedArea);
			centers.add(i, new Vector2(centroidX, centroidY));
			i++;
		}

		double cX = 0.0;
		double cY = 0.0;
		for (Vector2 v : centers){
			cX += v.x;
			cY += v.y;
		}

		return new Vector2(cX / centers.size(),cY / centers.size());
	}
*/
	public Model getModel(){
		return model;
	}

	@Override
	public void update(Observable o, Object arg) {
		if ((o == slicer) && (arg == this)) {
			this.slices = slicer.getSlices();
			sliced = true;
			setSkirtRadius();
			setChanged();
			notifyObservers(Events.SLICED);
		}
	}

	/**
	 * @return the optimizer
	 */
	public Optimizer getOptimizer() {
		return optimizer;
	}

	public enum Events {
		MODEL_LOADED, SLICED, PAVED, AUTO_OPTIMIZED
	}
}
