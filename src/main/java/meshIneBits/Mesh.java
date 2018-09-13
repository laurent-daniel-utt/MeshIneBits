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
 * This object is the equivalent of the piece which will be printed
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

	/**
	 * Update the current state of mesh and notify observers with that state
	 * @param state a value from predefined list
	 */
	private void setState(MeshEvents state) {
		setChanged();
		notifyObservers(state);
	}

	public Mesh(Model model) {
		slicer = new SliceTool(this);
		slicer.sliceModel();
		this.model = model;
	}

	/**
	 * Set the new mesh to ready
	 */
	public Mesh() {
		setState(MeshEvents.READY);
	}

	/**
	 * Register a model given a path
	 * @param filepath model file to load
	 */
	public void importModel(String filepath) throws Exception {
		this.model = new Model(filepath);
		this.model.center();
		setState(MeshEvents.IMPORTED);
		slicer = new SliceTool(this);
	}

	/**
	 * Start slicing the registered model
	 */
	public void slice() {
		setState(MeshEvents.SLICING);
		double zMin = this.model.getMin().z;
		if (zMin != 0) this.model.center(); // recenter before slicing
		slicer.sliceModel();
		// MeshEvents.SLICED will be sent in update() after receiving
		// signal from slicer
	}

	/**
	 * Given a certain template, pave the whole mesh
	 * @param template an automatic builder
	 */
	public void pave(PatternTemplate template) {
		setState(MeshEvents.PAVING);
		// TODO pave mesh
		// MeshEvents.PAVED will be sent in update() after receiving
		// enough signals from layers
	}

	/**
	 * Start the auto optimizer embedded in each template of each layer
	 * if presenting
	 */
	public void optimize() {
		setState(MeshEvents.OPTIMIZING);
		// TODO call optimizer of each layer
		// MeshEvents.OPTIMIZED will be sent in update() after receiving
		// enough signals from layers
	}

	/**
	 * Calculate glue points and/or areas between layers
	 */
	public void glue() {
		setState(MeshEvents.GLUING);
		// TODO run glue inserting in each layer
		// MeshEvents.GLUED will be sent in update() after receiving
		// enough signals from layers
	}

	public void export() {
		// TODO export instructions
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
		notifyObservers(MeshEvents.PAVED);
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
	public Model getModel(){
		return model;
	}

	@Override
	public void update(Observable o, Object arg) {
		if ((o == slicer) && (arg == MeshEvents.SLICED)) {
			// Slice job has been done
			this.slices = slicer.getSlices();
			sliced = true;
			setSkirtRadius();
			setChanged();
			notifyObservers(MeshEvents.SLICED);
		}
	}

	/**
	 * @return the optimizer
	 */
	public Optimizer getOptimizer() {
		return optimizer;
	}

}
