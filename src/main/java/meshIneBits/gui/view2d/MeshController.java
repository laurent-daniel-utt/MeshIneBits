/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 Vallon BENJAMIN.
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

package meshIneBits.gui.view2d;

import meshIneBits.*;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.CraftConfigLoader;
import meshIneBits.config.patternParameter.BooleanParam;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.gui.view3d.ProcessingModelView;
import meshIneBits.patterntemplates.PatternTemplate;
import meshIneBits.scheduler.AScheduler;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Logger;
import meshIneBits.util.SimultaneousOperationsException;
import meshIneBits.util.Vector2;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Observes the {@link Mesh} and {@link Layer}s. Observed by {@link MeshWindowCore}
 * and {@link ProcessingModelView}. Controls {@link MeshWindow}.
 */
@SuppressWarnings("WeakerAccess")
public class MeshController extends Observable implements Observer {

    // New bit config
    private final DoubleParam newBitsLengthParam = new DoubleParam(
            "newBitLength",
            "Bit length",
            "Length of bits to add",
            1.0, CraftConfig.bitLength,
            CraftConfig.bitLength, 1.0);
    private final DoubleParam newBitsWidthParam = new DoubleParam(
            "newBitWidth",
            "Bit width",
            "Length of bits to add",
            1.0, CraftConfig.bitWidth,
            CraftConfig.bitWidth, 1.0);
    private final DoubleParam newBitsOrientationParam = new DoubleParam(
            "newBitOrientation",
            "Bit orientation",
            "Angle of bits in respect to that of layer",
            -180.0, 180.0, 0.0, 0.01);
    private final DoubleParam safeguardSpaceParam = new DoubleParam(
            "safeguardSpace",
            "Space around bit",
            "In order to keep bits not overlapping or grazing each other",
            1.0, 10.0, 3.0, 0.01);
    private final BooleanParam autocropParam = new BooleanParam(
            "autocrop",
            "Auto crop",
            "Cut the new bit while preserving space around bits",
            true
    );
    private final MeshWindow meshWindow;
    private Mesh mesh;
    private int layerNumber = 0;
    private Layer currentLayer = null;
    private Set<Vector2> selectedBitKeys = new HashSet<>();
    private double zoom = 1;
    private boolean showSlice = true;
    private boolean showLiftPoints = false;
    private boolean showPreviousLayer = false;
    private boolean showCutPaths = false;
    private boolean showIrregularBits = false;
    private boolean addingBits = false;
    /**
     * In {@link Mesh}'s coordinate system
     */
    private Area availableArea;
    /**
     * In {@link Mesh}'s coordinate system
     */
    private Area bitAreaPreview;
    private boolean selectingRegion;
    private boolean selectedRegion;
    private List<Point2D.Double> regionVertices = new ArrayList<>();
    private Path2D.Double currentSelectedRegion = new Path2D.Double();
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    MeshController(MeshWindow meshWindow) {
        this.meshWindow = meshWindow;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
        mesh.addObserver(this);
    }

    public int getLayerNumber() {
        return layerNumber;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Layer) {
            updateAvailableArea();
            setChanged();
            notifyObservers(arg);
        } else if (arg instanceof MeshEvents)
            switch ((MeshEvents) arg) {
                case READY:
                    break;
                case IMPORT_FAILED:

                    break;
                case IMPORTING:
                    break;
                case IMPORTED:
                    meshWindow.getView3DWindow().setCurrentMesh(mesh);
                    break;
                case SLICING:
                    break;
                case SLICED:
                    Logger.updateStatus("Mesh sliced");
                    setLayer(0);
                    meshWindow.initGadgets();
                    // Notify the core to draw
                    setChanged();
                    notifyObservers(MeshEvents.SLICED);
                    break;
                case PAVING_MESH:
                    Logger.updateStatus("Paving mesh");
                    break;
                case PAVED_MESH:
                    Logger.updateStatus("Mesh paved");
                    checkEmptyLayers();
                    setChanged();
                    notifyObservers(MeshEvents.PAVED_MESH);
                    break;
                case PAVING_LAYER:
                    break;
                case PAVED_LAYER:
                    break;
                case OPTIMIZING_LAYER:
                    break;
                case OPTIMIZED_LAYER:
                    break;
                case OPTIMIZING_MESH:
                    break;
                case OPTIMIZED_MESH:
                    break;
                case GLUING:
                    break;
                case GLUED:
                    break;
                case SCHEDULING:
                    break;
                case SCHEDULED:
                    break;
                case OPENED:
                    meshWindow.getView3DWindow().setCurrentMesh(mesh);
                    setLayer(0);
                    meshWindow.initGadgets();
                    setChanged();
                    notifyObservers(MeshEvents.OPENED);
                    break;
                case OPEN_FAILED:
                    break;
                case SAVED:
                    break;
                case SAVE_FAILED:
                    break;
                case EXPORTING:
                    break;
                case EXPORTED:
                    break;
            }
    }

    public void setLayer(int layerNum) {
        if (mesh == null) {
            return;
        }
        if ((layerNum >= mesh.getLayers().size()) || (layerNum < 0)) {
            return;
        }
        layerNumber = layerNum;
        currentLayer = mesh.getLayers().get(layerNumber);
        currentLayer.addObserver(this);
        updateAvailableArea();
        reset();

        // Notify the core
        setChanged();
        notifyObservers();
    }

    public void checkEmptyLayers() {
        // Check empty layers
        List<Integer> indexesEmptyLayers = mesh.getEmptyLayers();
        if (indexesEmptyLayers.size() > 0) {
            StringBuilder str = new StringBuilder();
            indexesEmptyLayers.forEach(integer -> str.append(integer).append(" "));
            Logger.updateStatus("Some layers are empty: " + str.toString());
        }
    }

    private void updateAvailableArea() {
        availableArea = AreaTool.getAreaFrom(currentLayer.getHorizontalSection());
        Pavement pavement = currentLayer.getFlatPavement();
        if (pavement == null) return; // Empty layer
        pavement.getBitsKeys()
                .forEach(key -> {
                    Bit2D bit2D = pavement.getBit(key);
                    if (bit2D != null) { // for safety
                        availableArea.subtract(
                                AreaTool.expand(
                                        pavement.getBit(key)
                                                .getArea(), // in real
                                        safeguardSpaceParam.getCurrentValue())
                        );
                    }
                });
    }

    public void reset() {
        setSelectedBitKeys(null);
        setAddingBits(false);
        clearSelectingRegion(true);
    }

    private void clearSelectingRegion(boolean fireChangesSelectingRegion) {
        regionVertices.clear();
        currentSelectedRegion = new Path2D.Double();
        selectedRegion = false;
        selectingRegion = false;
        if (fireChangesSelectingRegion)
            changes.firePropertyChange(SELECTING_REGION, !selectingRegion, selectingRegion);
        setChanged();
        notifyObservers();
    }

    public void scaleSelectedBit(double percentageLength, double percentageWidth) {
        if (this.getSelectedBitKeys().isEmpty()) {
            Logger.warning("There is no bit selected");
        } else {
            setSelectedBitKeys(getSelectedBits().stream()
                    .map(bit -> currentLayer.scaleBit(bit, percentageLength, percentageWidth))
                    .collect(Collectors.toSet()));
        }
    }

    public Set<Vector2> getSelectedBitKeys() {
        return selectedBitKeys;
    }

    /**
     * Bulk reset
     *
     * @param newSelectedBitKeys <tt>null</tt> to reset to empty
     */
    public void setSelectedBitKeys(Set<Vector2> newSelectedBitKeys) {
        selectedBitKeys.clear();
        if (newSelectedBitKeys != null) {
            selectedBitKeys.addAll(newSelectedBitKeys);
            selectedBitKeys.removeIf(Objects::isNull);
        }
        // Notify the core to repaint
        setChanged();
        notifyObservers();
    }

    public Set<Bit3D> getSelectedBits() {
        return selectedBitKeys.stream()
                .map(currentLayer::getBit3D)
                .collect(Collectors.toSet());
    }

    /**
     * Restore a mesh into working space
     *
     * @param file location of saved mesh
     */
    public void openMesh(File file) throws SimultaneousOperationsException {
        if (mesh != null && mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        MeshOpener meshOpener = new MeshOpener(file);
        meshOpener.addObserver(this);
        (new Thread(meshOpener)).start();
    }

    /**
     * Save the current mesh on disk
     *
     * @param file location to save
     */
    public void saveMesh(File file) throws Exception {
        if (mesh == null)
            throw new Exception("Mesh not found");
        if (mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        MeshSaver meshSaver = new MeshSaver(file);
        (new Thread(meshSaver)).start();
    }

    public void exportXML(File file) throws Exception {
        if (mesh == null)
            throw new Exception("Mesh not found");
        mesh.export(file);
    }

    public void newMesh(File file) throws SimultaneousOperationsException {
        if (mesh != null && mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        MeshCreator meshCreator = new MeshCreator(file);
        meshCreator.addObserver(this);
        (new Thread(meshCreator)).start();
    }

    public void sliceMesh() throws Exception {
        if (mesh == null) throw new Exception("Mesh not found");
        if (mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        mesh.slice();
    }

    public void paveMesh(PatternTemplate patternTemplate) throws Exception {
        if (mesh == null) throw new Exception("Mesh not found");
        if (mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        if (!mesh.isSliced())
            throw new Exception("Mesh not sliced");
        CraftConfigLoader.saveConfig(null);
        mesh.pave(patternTemplate);
    }

    public void deleteSelectedBits() {
        currentLayer.removeBits(selectedBitKeys);
        selectedBitKeys.clear();
        currentLayer.rebuild();
    }

    boolean isAddingBits() {
        return addingBits;
    }

    public void setAddingBits(boolean b) {
        this.addingBits = b;

        changes.firePropertyChange(ADDING_BITS, !addingBits, addingBits);

        setChanged();
        notifyObservers();
    }

    public void incrementBitsOrientationParamBy(double v) {
        newBitsOrientationParam.incrementBy(v, true);
        setChanged();
        notifyObservers();
    }

    public Layer getCurrentLayer() {
        return currentLayer;
    }

    public boolean showingPreviousLayer() {
        return showPreviousLayer;
    }

    public boolean showingIrregularBits() {
        return showIrregularBits;
    }

    public boolean showingCutPaths() {
        return showCutPaths;
    }

    public boolean showingLiftPoints() {
        return showLiftPoints;
    }

    public boolean showingSlice() {
        return showSlice;
    }

    public void setShowCutPaths(boolean b) {
        showCutPaths = b;

        changes.firePropertyChange(SHOW_CUT_PATHS, !showCutPaths, showCutPaths);

        setChanged();
        notifyObservers();
    }

    public void setShowLiftPoints(boolean b) {
        showLiftPoints = b;

        changes.firePropertyChange(SHOW_LIFT_POINTS, !showLiftPoints, showLiftPoints);

        setChanged();
        notifyObservers();
    }

    public void setShowPreviousLayer(boolean b) {
        showPreviousLayer = b;

        changes.firePropertyChange(SHOW_PREVIOUS_LAYER, !showPreviousLayer, showPreviousLayer);

        setChanged();
        notifyObservers();
    }

    public void setShowSlice(boolean b) {
        showSlice = b;
        System.out.println("showSlice = " + showSlice);

        changes.firePropertyChange(SHOW_SLICE, !showSlice, showSlice);

        setChanged();
        notifyObservers();
    }

    public void setShowIrregularBits(boolean b) {
        showIrregularBits = b;

        changes.firePropertyChange(SHOW_IRREGULAR_BITS, !showIrregularBits, showIrregularBits);

        setChanged();
        notifyObservers();
    }

    public Area getAvailableBitAreaFrom(Shape bitPreviewInReal) {
        Area a = new Area(bitPreviewInReal);
        // Intersect
        a.intersect(availableArea);
        // Cache
        bitAreaPreview = (Area) a.clone();
        return a;
    }

    /**
     * @param position in {@link Mesh}'s coordinate system
     */
    public void addNewBitAt(Point2D.Double position) {
        if (mesh == null
                || currentLayer.getFlatPavement() == null
                || position == null
                || bitAreaPreview.isEmpty())
            return;
        Vector2 lOrientation = Vector2.getEquivalentVector(newBitsOrientationParam.getCurrentValue());
        Vector2 origin = new Vector2(position.x, position.y);
        Bit2D newBit = new Bit2D(origin, lOrientation,
                newBitsLengthParam.getCurrentValue(),
                newBitsWidthParam.getCurrentValue());
        if (autocropParam.getCurrentValue())
            newBit.updateBoundaries(bitAreaPreview);
        currentLayer.addBit(newBit);
    }

    /**
     * @param position in {@link Mesh} coordinate system
     * @return key of bit containing <tt>position</tt>. <tt>null</tt> if not found
     */
    public Vector2 findBitAt(Point2D.Double position) {
        Pavement flatPavement = currentLayer.getFlatPavement();
        for (Vector2 key : flatPavement.getBitsKeys()) {
            if (flatPavement.getBit(key).getArea().contains(position))
                return key;
        }
        return null;
    }

    /**
     * Add new bit key to {@link #selectedBitKeys} and remove if already
     * present
     *
     * @param bitKey in layer's coordinate system
     */
    public void addOrRemoveSelectedBitKeys(Vector2 bitKey) {
        if (mesh == null || !mesh.isPaved() || bitKey == null) {
            return;
        }
        if (!selectedBitKeys.add(bitKey))
            selectedBitKeys.remove(bitKey);
        // Notify the core
        setChanged();
        notifyObservers();
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
        setChanged();
        notifyObservers();
    }

    public DoubleParam getNewBitsLengthParam() {
        return newBitsLengthParam;
    }

    public DoubleParam getNewBitsWidthParam() {
        return newBitsWidthParam;
    }

    public DoubleParam getNewBitsOrientationParam() {
        return newBitsOrientationParam;
    }

    public DoubleParam getSafeguardSpaceParam() {
        return safeguardSpaceParam;
    }

    public BooleanParam getAutocropParam() {
        return autocropParam;
    }

    public void addPropertyChangeListener(String property, PropertyChangeListener l) {
        changes.addPropertyChangeListener(property, l);
    }

    /**
     * Centralized handler of exceptions
     *
     * @param e raised from any mesh execution
     */
    public void handleException(Exception e) {
        e.printStackTrace();
        Logger.error(e.getMessage());
    }

    public void optimizeMesh() throws Exception {
        if (mesh == null)
            throw new Exception("Mesh not found");
        if (!mesh.isPaved())
            throw new Exception("Mesh not paved");
        if (mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        mesh.optimize();
    }

    public void optimizeLayer() throws Exception {
        if (mesh == null)
            throw new Exception("Mesh not found");
        if (mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        if (!currentLayer.isPaved())
            throw new Exception("Layer not paved");
        mesh.optimize(currentLayer);
    }

    public void paveLayer(PatternTemplate patternTemplate) throws Exception {
        if (mesh == null)
            throw new Exception("Mesh not found");
        if (mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        mesh.pave(patternTemplate, currentLayer);
    }

    public void setNewBitSize(int lengthPercentage, int widthPercentage) {
        newBitsLengthParam.setCurrentValue(CraftConfig.bitLength * lengthPercentage / 100);
        newBitsWidthParam.setCurrentValue(CraftConfig.bitWidth * widthPercentage / 100);
        setChanged();
        notifyObservers();
    }

    public void scheduleMesh() throws Exception {
        if (mesh == null)
            throw new Exception("Mesh not found");
        if (!mesh.isPaved())
            throw new Exception("Mesh not paved");
        if (mesh.getScheduler() == null)
            throw new Exception("Scheduler not defined");
        mesh.runScheduler();
    }

    public void setScheduler(AScheduler scheduler) throws Exception {
        if (mesh == null)
            throw new Exception("Mesh not found");
        if (!mesh.isPaved())
            throw new Exception("Mesh not paved");
        mesh.setScheduler(scheduler);
    }

    public void paveSelectedRegion(PatternTemplate patternTemplate) throws Exception {
        if (mesh == null)
            throw new Exception("Mesh not found");
        if (currentLayer == null)
            throw new Exception("Layer not found");
        if (regionVertices.isEmpty())
            throw new Exception("No region vertex found");
        if (!selectedRegion)
            throw new Exception("Region not closed");
        mesh.pave(patternTemplate,
                currentLayer,
                new Area(currentSelectedRegion));
        clearSelectingRegion(true);
    }

    public boolean isSelectingRegion() {
        return selectingRegion;
    }

    public void setSelectingRegion(boolean b) {
        this.selectingRegion = b;
        changes.firePropertyChange(SELECTING_REGION, !selectingRegion, selectingRegion);

        if (!selectingRegion)
            clearSelectingRegion(false);

        setChanged();
        notifyObservers();
    }

    public boolean hasSelectedRegion() {
        return selectedRegion;
    }

    public Path2D.Double getCurrentSelectedRegion() {
        return currentSelectedRegion;
    }

    public List<Point2D.Double> getRegionVertices() {
        return regionVertices;
    }

    public void addNewRegionVertex(Point2D.Double clickSpotInReal) {
        regionVertices.add(clickSpotInReal);
        if (regionVertices.size() == 1) // the first vertex
            currentSelectedRegion.moveTo(clickSpotInReal.x, clickSpotInReal.y);
        else
            currentSelectedRegion.lineTo(clickSpotInReal.x, clickSpotInReal.y);
        setChanged();
        notifyObservers();
    }

    public void closeSelectedRegion() {
        currentSelectedRegion.closePath();
        selectedRegion = true;
        selectingRegion = false;
        setChanged();
        notifyObservers();
    }

    public void paveFill(PatternTemplate patternTemplate) throws Exception {
        if (mesh == null)
            throw new Exception("Mesh not found");
        if (currentLayer == null)
            throw new Exception("Layer not found");
        if (!currentLayer.isPaved())
            throw new Exception("Layer not paved");
        mesh.pave(patternTemplate, currentLayer, availableArea);
    }

    public static final String SHOW_SLICE = "showSlice";
    public static final String SHOW_LIFT_POINTS = "showLiftPoints";
    public static final String SHOW_PREVIOUS_LAYER = "showPreviousLayer";
    public static final String SHOW_CUT_PATHS = "showCutPaths";
    public static final String SHOW_IRREGULAR_BITS = "showIrregularBits";
    public static final String ADDING_BITS = "addingBits";
    public static final String SELECTING_REGION = "selectingRegion";

    public void toggle(String property) {
        switch (property) {
            case SHOW_SLICE:
                setShowSlice(!showingSlice());
                break;
            case SHOW_LIFT_POINTS:
                setShowLiftPoints(!showingLiftPoints());
                break;
            case SHOW_PREVIOUS_LAYER:
                setShowPreviousLayer(!showingPreviousLayer());
                break;
            case SHOW_CUT_PATHS:
                setShowCutPaths(!showingCutPaths());
                break;
            case SHOW_IRREGULAR_BITS:
                setShowIrregularBits(!showingIrregularBits());
                break;
            case ADDING_BITS:
                setAddingBits(!isAddingBits());
                break;
            case SELECTING_REGION:
                setSelectingRegion(!isSelectingRegion());
                break;
        }
    }

    public boolean get(String property) {
        switch (property) {
            case SHOW_SLICE:
                return showingSlice();
            case SHOW_LIFT_POINTS:
                return showingLiftPoints();
            case SHOW_PREVIOUS_LAYER:
                return showingPreviousLayer();
            case SHOW_CUT_PATHS:
                return showingCutPaths();
            case SHOW_IRREGULAR_BITS:
                return showingIrregularBits();
            case ADDING_BITS:
                return isAddingBits();
            case SELECTING_REGION:
                return isSelectingRegion();
        }
        return false;
    }

    /**
     * Convenient class to run async tasks
     */
    private abstract class MeshOperator extends Observable implements Runnable {
        final File file;

        MeshOperator(File file) {
            this.file = file;
        }
    }

    private class MeshCreator extends MeshOperator {

        MeshCreator(File file) {
            super(file);
        }

        @Override
        public void run() {
            setMesh(new Mesh());
            setChanged();
            notifyObservers(MeshEvents.READY);
            String filename = file.toString();
            try {
                mesh.importModel(filename); // sync task
            } catch (Exception e) {
                handleException(e);
                setChanged();
                notifyObservers(MeshEvents.IMPORT_FAILED);
            }
        }
    }

    private class MeshSaver extends MeshOperator {
        MeshSaver(File file) {
            super(file);
        }

        @Override
        public void run() {
            try {
                mesh.saveAs(file);
                Logger.updateStatus("Mesh saved at " + file.getPath());
            } catch (IOException e) {
                handleException(e);
            }
        }
    }

    private class MeshOpener extends MeshOperator {

        MeshOpener(File file) {
            super(file);
        }

        @Override
        public void run() {
            try {
                setMesh(Mesh.open(file));
                Logger.updateStatus("Mesh opened from " + file.getPath());
                // notify main window
                setChanged();
                notifyObservers(MeshEvents.OPENED);
            } catch (ClassNotFoundException | IOException e) {
                handleException(e);
                setChanged();
                notifyObservers(MeshEvents.OPEN_FAILED);
            }
        }
    }
}