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

package meshIneBits.gui;

import meshIneBits.*;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.patternParameter.BooleanParam;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.slicer.Slice;
import meshIneBits.util.*;

import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Observes the {@link Mesh}. Observed by {@link MeshWindowCore}. Controls
 * {@link MeshWindowCore} and {@link MeshWindow}.
 */
public class MeshController extends Observable implements Observer {

    // New bit config
    final DoubleParam newBitsLengthParam = new DoubleParam(
            "newBitLength",
            "Bit length",
            "Length of bits to add",
            1.0, CraftConfig.bitLength,
            CraftConfig.bitLength, 1.0);
    final DoubleParam newBitsWidthParam = new DoubleParam(
            "newBitWidth",
            "Bit width",
            "Length of bits to add",
            1.0, CraftConfig.bitWidth,
            CraftConfig.bitWidth, 1.0);
    final DoubleParam newBitsOrientationParam = new DoubleParam(
            "newBitOrientation",
            "Bit orientation",
            "Angle of bits in respect to that of layer",
            -180.0, 180.0, 0.0, 0.01);
    final DoubleParam safeguardSpaceParam = new DoubleParam(
            "safeguardSpace",
            "Space around bit",
            "In order to keep bits not overlapping or grazing each other",
            1.0, 10.0, 3.0, 0.01);
    final BooleanParam autocropParam = new BooleanParam(
            "autocrop",
            "Auto crop",
            "Cut the new bit while preserving space around bits",
            true
    );
    private final MeshWindow meshWindow;
    private Mesh mesh;
    private int sliceNumber = 0;
    private int layerNumber = 0;
    private Layer currentLayer = null;
    private Set<Vector2> selectedBitKeys = new HashSet<>();
    private double zoom = 1;
    private boolean showSlice = false;
    private boolean showLiftPoints = false;
    private boolean showPreviousLayer = false;
    private boolean showCutPaths = false;
    private boolean showIrregularBits = false;
    private boolean addingBits = false;
    private AffineTransform realToPavement;
    private Area availableArea;
    private Area bitAreaPreview;

    MeshController(MeshWindow meshWindow) {
        this.meshWindow = meshWindow;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public int getLayerNumber() {
        return layerNumber;
    }

    public void setLayer(int layerNum) {
        if (mesh == null) {
            return;
        }
        if (!mesh.isPaved()) {
            return;
        }
        if ((layerNum >= mesh.getLayers().size()) || (layerNum < 0)) {
            return;
        }
        layerNumber = layerNum;
        currentLayer = mesh.getLayers().get(layerNumber);
        currentLayer.addObserver(this);
        realToPavement = new AffineTransform();
        try {
            realToPavement = currentLayer.getFlatPavement().getAffineTransform().createInverse();
        } catch (NoninvertibleTransformException e) {
            realToPavement = AffineTransform.getScaleInstance(1, 1);
        }
        updateAvailableArea();
        reset();

        // Notify the core
        setChanged();
        notifyObservers();
    }

    private void updateAvailableArea() {
        availableArea = AreaTool.getAreaFrom(currentLayer.getHorizontalSection());
        Pavement pavement = currentLayer.getFlatPavement();
        pavement.getBitsKeys()
                .forEach(key -> availableArea.subtract(
                        AreaTool.expand(pavement.getBit(key).getArea(),
                                safeguardSpaceParam.getCurrentValue())));
    }

    void reset() {
        setSelectedBitKeys(null);
        setAddingBits(false);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof MeshEvents)
            switch ((MeshEvents) arg) {
                case READY:
                    break;
                case IMPORTING:
                    Logger.updateStatus("Importing model");
                    break;
                case IMPORTED:
                    Logger.updateStatus("Model imported. Mesh ready to slice.");
                    meshWindow.getView3DWindow().setCurrentMesh(mesh);
                    break;
                case SLICING:
                    break;
                case SLICED:
                    Logger.updateStatus("Mesh sliced");
                    meshWindow.initSliceView();
                    // Notify the core to draw
                    setChanged();
                    notifyObservers(MeshEvents.SLICED);
                    break;
                case PAVING_MESH:
                    break;
                case PAVED_MESH:
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
                case OPENED:
                    meshWindow.getView3DWindow().setCurrentMesh(mesh);
                    break;
                case OPEN_FAILED:
                    Logger.error("Failed to open the mesh");
                    break;
                case SAVED:
                    break;
                case SAVE_FAILED:
                    Logger.error("Failed to save the mesh");
                    break;
                case EXPORTED:
                    break;
            }
    }

    /**
     * Restore a mesh into working space
     *
     * @param file location of saved mesh
     */
    void openMesh(File file) throws SimultaneousOperationsException {
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
    void saveMesh(File file) throws Exception {
        if (mesh == null)
            throw new Exception("Mesh not found");
        if (mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        MeshSaver meshSaver = new MeshSaver(file);
        meshSaver.addObserver(this);
        (new Thread(meshSaver)).start();
    }

    void exportXML(File file) throws Exception {
        if (mesh == null)
            throw new Exception("Mesh not found");
        if (mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        if (mesh.getState().getCode() < MeshEvents.PAVED_MESH.getCode())
            throw new Exception("Mesh unpaved");
        MeshXMLExporter meshXMLExporter = new MeshXMLExporter(file);
        meshXMLExporter.addObserver(this);
        (new Thread(meshXMLExporter)).start();
    }

    void newMesh(File file) throws SimultaneousOperationsException {
        if (mesh != null && mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        MeshCreator meshCreator = new MeshCreator(file);
        meshCreator.addObserver(this);
        (new Thread(meshCreator)).start();
    }

    void sliceMesh() throws Exception {
        if (mesh == null) throw new Exception("Mesh not found");
        if (mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        MeshSlicer meshSlicer = new MeshSlicer();
        meshSlicer.addObserver(this);
        (new Thread(meshSlicer)).start();
    }

    void deleteSelectedBits() {
        currentLayer.removeBits(selectedBitKeys, false);
        selectedBitKeys.clear();
        currentLayer.rebuild();
    }

    public void setSlice(int sliceNum) {
        if (mesh == null) {
            return;
        }
        if ((sliceNum >= mesh.getSlices().size()) || (sliceNum < 0)) {
            return;
        }
        sliceNumber = sliceNum;

        // Notify the core
        setChanged();
        notifyObservers();
    }

    int getSliceNumber() {
        return sliceNumber;
    }

    boolean isAddingBits() {
        return addingBits;
    }

    void setAddingBits(boolean b) {
        this.addingBits = b;
        setChanged();
        notifyObservers();
    }

    void incrementBitsOrientationParamBy(double v) {
        newBitsOrientationParam.incrementBy(v, true);
        setChanged();
        notifyObservers();
    }

    Set<Vector2> getSelectedBitKeys() {
        return selectedBitKeys;
    }

    /**
     * Bulk reset
     *
     * @param newSelectedBitKeys <tt>null</tt> to reset to empty
     */
    void setSelectedBitKeys(Set<Vector2> newSelectedBitKeys) {
        selectedBitKeys.clear();
        if (newSelectedBitKeys != null) {
            selectedBitKeys.addAll(newSelectedBitKeys);
            selectedBitKeys.removeIf(Objects::isNull);
        }
        // Notify the core to repaint
        setChanged();
        notifyObservers();
    }

    void rotateSelectedBitsBy(double v) {
        setSelectedBitKeys(currentLayer.rotateBits(selectedBitKeys, v));
    }

    Layer getCurrentLayer() {
        return currentLayer;
    }

    Slice getCurrentSlice() {
        return mesh.getSlices().get(sliceNumber);
    }

    boolean showingPreviousLayer() {
        return showPreviousLayer;
    }

    boolean showingIrregularBits() {
        return showIrregularBits;
    }

    boolean showingCutPaths() {
        return showCutPaths;
    }

    boolean showingLiftPoints() {
        return showLiftPoints;
    }

    boolean showingSlice() {
        return showSlice;
    }

    void toggleShowCutPaths(boolean selected) {
        this.showCutPaths = selected;

        setChanged();
        notifyObservers();
    }

    void toggleShowLiftPoints(boolean selected) {
        this.showLiftPoints = selected;

        setChanged();
        notifyObservers();
    }

    void toggleShowPreviousLayer(boolean selected) {
        this.showPreviousLayer = selected;

        setChanged();
        notifyObservers();
    }

    void toggleShowSlice(boolean selected) {
        this.showSlice = selected;

        setChanged();
        notifyObservers();
    }

    void toggleShowIrregularBits(boolean selected) {
        this.showIrregularBits = selected;

        setChanged();
        notifyObservers();
    }

    Set<Bit3D> getSelectedBits() {
        return selectedBitKeys.stream()
                .map(currentLayer::getBit3D)
                .collect(Collectors.toSet());
    }

    Area getAvailableBitAreaAt(Point2D.Double spot) {
        Rectangle2D.Double r = new Rectangle2D.Double(
                -CraftConfig.bitLength / 2,
                -CraftConfig.bitWidth / 2,
                newBitsLengthParam.getCurrentValue(),
                newBitsWidthParam.getCurrentValue());
        Area a = new Area(r);
        // Rotate
        AffineTransform affineTransform = new AffineTransform(realToPavement);
        affineTransform.translate(spot.x, spot.y);
        Vector2 lOrientation = Vector2.getEquivalentVector(
                newBitsOrientationParam.getCurrentValue());
        affineTransform.rotate(lOrientation.x, lOrientation.y);
        a.transform(affineTransform);
        // Intersect
        a.intersect(availableArea);
        // Cache
        bitAreaPreview = (Area) a.clone();
        return a;
    }

    /**
     * @param position real position (not zoomed or translated)
     */
    void addNewBitAt(Point2D.Double position) {
        if (mesh == null || currentLayer.getFlatPavement() == null || position == null)
            return;
        realToPavement.transform(position, position);
        bitAreaPreview.transform(realToPavement);
        Vector2 lOrientation = Vector2.getEquivalentVector(newBitsOrientationParam.getCurrentValue());
        Vector2 origin = new Vector2(position.x, position.y);
        // Add
        Bit2D newBit = new Bit2D(origin, lOrientation,
                newBitsLengthParam.getCurrentValue(),
                newBitsWidthParam.getCurrentValue());
        if (autocropParam.getCurrentValue())
            newBit.updateBoundaries(bitAreaPreview);
        currentLayer.addBit(newBit);
    }

    /**
     * @param position real position (not zoomed or translated)
     * @return key of bit containing <tt>position</tt>. <tt>null</tt> if not found
     */
    Vector2 findBitAt(Point2D.Double position) {
        realToPavement.transform(position, position);
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
    void addOrRemoveSelectedBitKeys(Vector2 bitKey) {
        if (mesh == null || !mesh.isPaved() || bitKey == null) {
            return;
        }
        if (!selectedBitKeys.add(bitKey))
            selectedBitKeys.remove(bitKey);

        // Notify the core
        setChanged();
        notifyObservers();
    }

    double getZoom() {
        return zoom;
    }

    void setZoom(double zoom) {
        this.zoom = zoom;
        setChanged();
        notifyObservers();
    }

    /**
     * Convenient class to run async tasks
     */
    private abstract class MeshOperator extends Observable implements Runnable {
        final File file;

        MeshOperator(File file) {
            this.file = file;
        }

        MeshOperator() {
            file = null;
        }
    }

    private class MeshCreator extends MeshOperator {

        MeshCreator(File file) {
            super(file);
        }

        @Override
        public void run() {
            mesh = new Mesh();
            mesh.addObserver(MeshController.this);
            setChanged();
            notifyObservers(MeshEvents.READY);
            assert file != null;
            String filename = file.toString();
            try {
                mesh.importModel(filename); // sync task
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class MeshSlicer extends MeshOperator {

        MeshSlicer() {
            super();
        }

        @Override
        public void run() {
            try {
                mesh.slice();
            } catch (Exception e) {
                e.printStackTrace();
                Logger.error("Unable to slice mesh. " + e.getMessage());
            }
        }
    }

    private class MeshSaver extends MeshOperator {
        MeshSaver(File file) {
            super(file);
        }

        @Override
        public void run() {
            assert file != null;
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(mesh);
                oos.flush();
                // notify main window
                setChanged();
                notifyObservers(MeshEvents.SAVED);
            } catch (IOException e) {
                e.printStackTrace();
                setChanged();
                notifyObservers(MeshEvents.SAVE_FAILED);
            }
        }
    }

    private class MeshOpener extends MeshOperator {

        MeshOpener(File file) {
            super(file);
        }

        @Override
        public void run() {
            assert file != null;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                setMesh((Mesh) ois.readObject());
                // notify main window
                setChanged();
                notifyObservers(MeshEvents.OPENED);
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
                setChanged();
                notifyObservers(MeshEvents.OPEN_FAILED);
            }
        }
    }

    private class MeshXMLExporter extends MeshOperator {

        MeshXMLExporter(File file) {
            super(file);
        }

        @Override
        public void run() {
            assert file != null;
            XmlTool xt = new XmlTool(mesh, file.toPath());
            xt.writeXmlCode();
            setChanged();
            notifyObservers(MeshEvents.EXPORTED);
        }
    }
}