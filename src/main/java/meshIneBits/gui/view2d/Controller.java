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
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;

import java.awt.geom.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The controller, linking 2D views and mesh (so-called {@link Mesh}).
 * It observes the {@link Mesh} and its {@link meshIneBits.Layer
 * Layers}. Observed by {@link Core} and {@link Wrapper}. A singleton.
 */
class Controller extends Observable implements Observer {

    static private Controller instance;
    private Mesh mesh = null;
    private int layerNumber = 0;
    private Layer currentLayer = null;
    private int sliceNumber = 0;
    private Set<Vector2> selectedBitKeys = new HashSet<>();
    private double zoom = 1;
    @Deprecated
    private boolean showSlices = false;
    private boolean showSlice = false;
    private boolean showLiftPoints = false;
    private boolean showPreviousLayer = false;
    private boolean showCutPaths = false;
    private boolean showIrregularBits = false;
    private Model model = null;
    private boolean onAddingBits = false;

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
    private Area availableArea;
    private AffineTransform realToPavement;
    final DoubleParam safeguardSpaceParam = new DoubleParam(
            "safeguardSpace",
            "Space around bit",
            "In order to keep bits not overlapping or grazing each other",
            1.0, 10.0, 3.0, 0.01);

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }

        return instance;
    }

    private Controller() {
    }

    int getCurrentLayerNumber() {
        return layerNumber;
    }

    boolean showSlice() {
        return showSlice;
    }

    Mesh getCurrentMesh() {
        return mesh;
    }

    int getCurrentSliceNumber() {
        return sliceNumber;
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
        setChanged();
        notifyObservers(Component.SELECTED_BIT);
    }

    Set<Bit3D> getSelectedBits() {
        return selectedBitKeys.stream()
                .map(currentLayer::getBit3D)
                .collect(Collectors.toSet());
    }

    double getZoom() {
        return zoom;
    }

    public Model getModel() {
        return model;
    }

    public void setLayer(int nbrLayer) {
        if (mesh == null) {
            return;
        }
        if (!mesh.isPaved()) {
            return;
        }
        if ((nbrLayer >= mesh.getLayers().size()) || (nbrLayer < 0)) {
            return;
        }
        layerNumber = nbrLayer;
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

        setChanged();
        notifyObservers(Component.LAYER);
    }

    public Layer getLayer() {
        return currentLayer;
    }

    /**
     * Put a generated mesh under observation of this controller. Also signify
     * {@link Core} and {@link Wrapper} to repaint.
     *
     * @param mesh <tt>null</tt> to reset
     */
    void setCurrentMesh(Mesh mesh) {
        if (mesh != null) {
            this.mesh = mesh;
            mesh.addObserver(this);

            setLayer(0);
            reset();
        } else
            this.mesh = null;

        setChanged();
        notifyObservers(Component.MESH);
    }

    /**
     * Reset all attributes of chooser
     */
    void reset() {
        setSelectedBitKeys(null);
        setOnAddingBits(false);
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

        setChanged();
        notifyObservers(Component.SELECTED_BIT);
    }

    public void setSlice(int nbrSlice) {
        if (mesh == null) {
            return;
        }
        if ((nbrSlice >= mesh.getSlices().size()) || (nbrSlice < 0)) {
            return;
        }
        sliceNumber = nbrSlice;

        setChanged();
        notifyObservers(Component.SLICE);
    }

    void setZoom(double zoomValue) {
        if (mesh == null) {
            return;
        }
        zoom = zoomValue < Wrapper.MIN_ZOOM_VALUE ? 0.5 : zoomValue;

        setChanged();
        notifyObservers(Component.ZOOM);
    }

    boolean showCutPaths() {
        return showCutPaths;
    }

    boolean showLiftPoints() {
        return showLiftPoints;
    }

    boolean showPreviousLayer() {
        return showPreviousLayer;
    }

    @SuppressWarnings("unused")
    @Deprecated
    boolean showSlices() {
        return showSlices;
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

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    void toggleShowSlices(boolean selected) {
        this.showSlices = selected;

        setChanged();
        notifyObservers();
    }

    void toggleShowSlice(boolean selected) {
        this.showSlice = selected;

        setChanged();
        notifyObservers();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == this.mesh) {
            this.setCurrentMesh(mesh);
        } else if (o == currentLayer) {
            updateAvailableArea();
            setChanged();
            notifyObservers(Component.LAYER);
        }
    }

    private void updateAvailableArea() {
        availableArea = AreaTool.getAreaFrom(currentLayer.getHorizontalSection());
        Pavement pavement = currentLayer.getFlatPavement();
        pavement.getBitsKeys()
                .forEach(key -> availableArea.subtract(
                        AreaTool.expand(pavement.getBit(key).getArea(),
                                safeguardSpaceParam.getCurrentValue())));
    }

    void incrementBitsOrientationParamBy(double notches) {
        newBitsOrientationParam.incrementBy(notches);
        setChanged();
        notifyObservers();
    }

    /**
     * @param notches rotation in degree
     */
    void rotateSelectedBitsBy(double notches) {
        setSelectedBitKeys(currentLayer.rotateBits(selectedBitKeys, notches));
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
        return a;
    }

    public enum Component {
        MESH, LAYER, SELECTED_BIT, ZOOM, SLICE
    }

    boolean showIrregularBits() {
        return showIrregularBits;
    }

    void toggleShowIrregularBits(boolean selected) {
        this.showIrregularBits = selected;

        setChanged();
        notifyObservers();
    }

    boolean isOnAddingBits() {
        return onAddingBits;
    }

    void setOnAddingBits(boolean onAddingBits) {
        this.onAddingBits = onAddingBits;
        setChanged();
        notifyObservers();
    }

    /**
     * @param position real position (not zoomed or translated)
     */
    void addNewBitAt(Point2D.Double position) {
        if (mesh == null || currentLayer.getFlatPavement() == null || position == null)
            return;
        realToPavement.transform(position, position);
        Vector2 lOrientation = Vector2.getEquivalentVector(newBitsOrientationParam.getCurrentValue());
        Vector2 origin = new Vector2(position.x, position.y);
        // Add
        currentLayer.addBit(new Bit2D(origin, lOrientation,
                newBitsLengthParam.getCurrentValue(),
                newBitsWidthParam.getCurrentValue()));
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

    void scaleSelectedBit(double percentageLength, double percentageWidth) {
        if (this.getSelectedBitKeys().isEmpty()) {
            Logger.warning("There is no bit selected");
        } else {
            setSelectedBitKeys(getSelectedBits().stream()
                    .map(bit -> currentLayer.scaleBit(bit, percentageLength, percentageWidth))
                    .collect(Collectors.toSet()));
        }
    }
}