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

import meshIneBits.patterntemplates.PatternTemplate;
import meshIneBits.slicer.Slice;
import meshIneBits.slicer.SliceTool;
import meshIneBits.util.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This object is the equivalent of the piece which will be printed
 */
public class Mesh extends Observable implements Observer {
    private Vector<Layer> layers = new Vector<>();
    private Vector<Slice> slices = new Vector<>();
    @Deprecated
    private PatternTemplate patternTemplate = null;
    private double skirtRadius;
    private SliceTool slicer;
    private boolean sliced = false;
    @Deprecated
    private Optimizer optimizer = null;
    private Model model;
    private MeshEvents state;
    /**
     * Regroup of irregularities by index of layers and keys of bits
     */
    private Map<Layer, List<Vector2>> irregularBits;

    /**
     * Update the current state of mesh and notify observers with that state
     *
     * @param state a value from predefined list
     */
    private void setState(MeshEvents state) {
        this.state = state;
        setChanged();
        notifyObservers(state);
    }

    @Deprecated
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
        if (state.isWorking()) throw new SimultaneousOperationsException();

        setState(MeshEvents.IMPORTING);
        this.model = new Model(filepath);
        this.model.center();
        slicer = new SliceTool(this);
        setState(MeshEvents.IMPORTED);
    }

    /**
     * Start slicing the registered model
     */
    public void slice() throws Exception {
        if (state.isWorking()) throw new SimultaneousOperationsException();

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
    public void pave(PatternTemplate template) throws Exception {
        pavementSafetyCheck();

        setState(MeshEvents.PAVING_MESH);
        // MeshEvents.PAVED_MESH will be sent in update() after receiving
        // enough signals from layers
        Logger.updateStatus("Ready to generate bits");
        template.ready(this);
        // Remove all current layers
        this.layers.clear();
        // New worker
        PavingWorker pavingWorker = new PavingWorker(template);
        pavingWorker.addObserver(this);
        Thread t = new Thread(pavingWorker);
        t.start();
    }

    // TODO pave a certain layer

    // TODO parallel pavement

    private void pavementSafetyCheck() throws Exception {
        if (state.isWorking()) throw new SimultaneousOperationsException();
        if (!isSliced())
            throw new Exception("The mesh cannot be paved until it is sliced");
    }

    /**
     * Start the auto optimizer embedded in each template of each layer
     * if presenting
     */
    public void optimize() throws Exception {
        optimizationSafetyCheck();

        setState(MeshEvents.OPTIMIZING_MESH);
        // MeshEvents.OPTIMIZED_MESH will be sent in update() after receiving
        // enough signals from layers
        MeshOptimizer meshOptimizer = new MeshOptimizer();
        Thread t = new Thread(meshOptimizer);
        t.start();
    }

    /**
     * Run the optimizing algorithm of the layer
     *
     * @param layer layer of optimization
     */
    public void optimize(Layer layer) throws Exception {
        optimizationSafetyCheck();

        setState(MeshEvents.OPTIMIZING_LAYER);
        LayerOptimizer layerOptimizer = new LayerOptimizer(layer);
        // MeshEvents.OPTIMIZED_LAYER will be sent after completed the task
        Thread t = new Thread(layerOptimizer);
        t.start();
    }

    private void optimizationSafetyCheck() throws Exception {
        if (state.isWorking()) throw new SimultaneousOperationsException();
        if (!isPaved())
            throw new Exception("The mesh cannot be auto optimized until it is fully paved.");
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
        if (!isPaved()) {
            try {
                throw new Exception("Mesh not paved!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.layers;
    }

    @Deprecated
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

    public boolean isPaved() {
        // TODO make a full check
        return !layers.isEmpty();
    }

    public boolean isSliced() {
        return sliced;
    }

    private void detectIrregularBits() {
//        optimizer = new Optimizer(layers);
//        optimizer.detectIrregularBits();
        irregularBits = layers.parallelStream().collect(Collectors.toConcurrentMap(
                layer -> layer,
                layer -> DetectorTool.detectIrregularBits(layer.getFlatPavement()),
                (u, v) -> u,
                ConcurrentHashMap::new
        ));
    }

    /**
     * @param layer target
     * @return keys of irregularities in layer
     */
    public List<Vector2> getIrregularBitKeysOf(Layer layer) {
        return irregularBits.get(layer);
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
        if (arg instanceof MeshEvents) {
            switch ((MeshEvents) arg) {
                case READY:
                    break;
                case IMPORTING:
                    break;
                case IMPORTED:
                    break;
                case SLICING:
                    break;
                case SLICED:
                    // Slice job has been done
                    // Slicer sends messages
                    this.slices = slicer.getSlices();
                    sliced = true;
                    setSkirtRadius();
                    setState(MeshEvents.SLICED);
                    break;
                case PAVING_MESH:
                    break;
                case PAVED_MESH:
                    // In sequential pavement, we only need one signal at the end
                    setState(MeshEvents.PAVED_MESH);
                    // TODO In parallel pavement, we need to collect all signals
                    break;
                case OPTIMIZING_LAYER:
                    break;
                case OPTIMIZED_LAYER:
                    setState(MeshEvents.OPTIMIZED_MESH);
                    break;
                case OPTIMIZING_MESH:
                    break;
                case OPTIMIZED_MESH:
                    setState(MeshEvents.OPTIMIZED_MESH);
                    break;
                case GLUING:
                    break;
                case GLUED:
                    break;
            }
        }
    }

    /**
     * @return the optimizer
     * @deprecated
     */
    public Optimizer getOptimizer() {
        return optimizer;
    }

    /**
     * In charge of paving one or multiple layers
     *
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
            buildLayers();
            detectIrregularBits();
            setState(MeshEvents.PAVED_MESH);
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

    /**
     * Managing optimization of a layer, then reporting to {@link MeshOptimizer} or {@link Mesh}
     */
    private class LayerOptimizer extends Observable implements Runnable {

        private Layer layer;
        private int irregularitiesRest;

        LayerOptimizer(Layer layer) {
            this.layer = layer;
        }

        public Layer getLayer() {
            return layer;
        }

        @Override
        public void run() {
            Logger.updateStatus("Auto-optimizing layer " + layer.getLayerNumber());
            irregularitiesRest = layer.getPatternTemplate().optimize(layer);
            if (irregularitiesRest <= 0) {
                switch (irregularitiesRest) {
                    case 0:
                        Logger.updateStatus("Auto-optimization succeeded on layer " + layer.getLayerNumber());
                        break;
                    case -1:
                        Logger.updateStatus("Auto-optimization failed on layer " + layer.getLayerNumber());
                        break;
                    case -2:
                        Logger.updateStatus("No optimizing algorithm implemented on layer " + layer.getLayerNumber());
                        break;
                }
            } else {
                Logger.updateStatus("Auto-optimization for layer " + layer.getLayerNumber() + " done. " + irregularitiesRest + " unsolved irregularities.");
            }
            setChanged();
            notifyObservers(MeshEvents.OPTIMIZED_LAYER);
        }
    }

    /**
     * Managing process of optimizing consequently all layers, then reporting to {@link Mesh}
     */
    private class MeshOptimizer extends Observable implements Runnable {
        @Override
        public void run() {
            int progressGoal = layers.size();
            int irregularitiesRest = 0;
            ArrayList<Integer> unsolvedLayers = new ArrayList<>();
            Logger.updateStatus("Optimizing the current mesh.");
            for (int j = 0; j < layers.size(); j++) {
                Logger.setProgress(j + 1, progressGoal);
                int ir = layers.get(j).getPatternTemplate().optimize(layers.get(j));
                if (ir > 0) {
                    irregularitiesRest += ir;
                } else if (ir < 0) {
                    unsolvedLayers.add(layers.get(j).getLayerNumber());
                }
            }
            Logger.updateStatus("Auto-optimization complete. Still has " + irregularitiesRest + " irregularities not solved yet.");
            if (!unsolvedLayers.isEmpty()) {
                StringBuilder str = new StringBuilder();
                for (Integer integer : unsolvedLayers) {
                    str.append(" ").append(integer);
                }
                Logger.updateStatus("Unsolvable layers: " + str.toString());
            }
            setChanged();
            notifyObservers(MeshEvents.OPTIMIZED_MESH);
        }
    }

    private class SimultaneousOperationsException extends Exception {
        SimultaneousOperationsException() {
            super("The mesh is undergoing an other task. Please wait until that task is done.");
        }
    }
}