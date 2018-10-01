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
import meshIneBits.util.Vector2;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
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
        Layer currentLayer = mesh.getLayers().get(layerNumber);
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
        mesh.getLayers().get(layerNumber).addObserver(this);
        reset();

        setChanged();
        notifyObservers(Component.LAYER);
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
        if (mesh == null || !mesh.isPaved()) {
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
        } else if (o == this.mesh.getLayers().get(layerNumber)) {
            setChanged();
            notifyObservers(Component.LAYER);
        }
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

    private boolean onAddingBits = false;

    boolean isOnAddingBits() {
        return onAddingBits;
    }

    void setOnAddingBits(boolean onAddingBits) {
        this.onAddingBits = onAddingBits;
        setChanged();
        notifyObservers();
    }

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
            0.0, 360.0, 0.0, 0.01);

    void addNewBits(Point2D.Double position) {
        Layer l = mesh.getLayers().get(layerNumber);
        if (mesh == null || l.getFlatPavement() == null || position == null)
            return;
        Vector2 lOrientation = Vector2.getEquivalentVector(newBitsOrientationParam.getCurrentValue());
        AffineTransform inv = new AffineTransform();
        try {
            inv = l.getFlatPavement().getAffineTransform().createInverse();
        } catch (NoninvertibleTransformException e) {
            // Ignore
        }
        final AffineTransform finalInv = inv;
        finalInv.transform(position, position);
        Vector2 origin = new Vector2(position.x, position.y);
        // Add
        l.addBit(new Bit2D(origin, lOrientation,
                newBitsLengthParam.getCurrentValue(),
                newBitsWidthParam.getCurrentValue()));
    }
}