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
 */
public class Mesh extends Observable implements Observer {
    private Vector<Layer> layers = new Vector<>();
    private Vector<Slice> slices = new Vector<>();
    private PatternTemplate patternTemplate;
    private double skirtRadius;
    private Thread t = null;
    private SliceTool slicer;
    private boolean sliced = false;
    private Optimizer optimizer;
    private Model model;

    /**
     * Update the current state of mesh and notify observers with that state
     *
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
     *
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
     * Given a certain template, pave the whole mesh sequentially
     *
     * @param template an automatic builder
     */
    public void pave(PatternTemplate template) {
        // TODO pave mesh
        if (t == null || !t.isAlive()) {
            // MeshEvents.PAVED will be sent in update() after receiving
            // enough signals from layers
            Logger.updateStatus("Ready to generate bits");
            template.ready(this);
            // Remove all current layers
            this.layers.clear();
            // New worker
            PavingWorker pavingWorker = new PavingWorker(template);
            pavingWorker.addObserver(this);
            t = new Thread(pavingWorker);
            t.start();
        }
    }

    // TODO parallel pavement

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

    PatternTemplate getPatternTemplate() {
        return patternTemplate;
    }

    public double getSkirtRadius() {
        return skirtRadius;
    }

    public Vector<Slice> getSlices() {
        if (!sliced) {
            try {
                throw new Exception("Mesh not sliced!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return slices;
    }

    public boolean isGenerated() {
        return !layers.isEmpty();
    }

    public boolean isSliced() {
        return sliced;
    }

    private void detectIrregularBits() {
        optimizer = new Optimizer(layers);
        optimizer.detectIrregularBits();
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

    public Model getModel() {
        return model;
    }

    @Override
    public void update(Observable o, Object arg) {
        if ((o == slicer) && (arg == MeshEvents.SLICED)) {
            // Slice job has been done
            this.slices = slicer.getSlices();
            sliced = true;
            setSkirtRadius();
            setState(MeshEvents.SLICED);
        }
        if (arg == MeshEvents.PAVED) {
            // In sequential pavement, we only need one signal at the end
            setState(MeshEvents.PAVED);
            // TODO In parallel pavement, we need to collect all signals
        }
    }

    /**
     * @return the optimizer
     */
    public Optimizer getOptimizer() {
        return optimizer;
    }

    /**
     * In charge of paving one or multiple layers
     * @since 0.3
     */
    private class PavingWorker extends Observable implements Runnable {

        private PatternTemplate patternTemplate;

        /**
         * Pave the layer following a certain template
         *
         * @param patternTemplate how we want to pave a layer
         */
        PavingWorker(PatternTemplate patternTemplate) {
            this.patternTemplate = patternTemplate;
        }

        @Override
        public void run() {
            setChanged();
            notifyObservers(MeshEvents.PAVING);
            buildLayers();
            detectIrregularBits();
            setChanged();
            notifyObservers(MeshEvents.PAVED);
        }

        /**
         * Construct layers from slices then pave them
         */
        private void buildLayers() {
            Logger.updateStatus("Generating Layers");
            for (int i = 0; i < slices.size(); i++) {
                // TODO where is the altitude of layer?
                layers.add(new Layer(i, slices.get(i), patternTemplate));
            }
            Logger.updateStatus(layers.size() + " layers have been generated and paved");
        }
    }
}
