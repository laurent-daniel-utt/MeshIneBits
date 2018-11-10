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

package meshIneBits;

import javafx.util.Pair;
import meshIneBits.config.CraftConfig;
import meshIneBits.patterntemplates.PatternTemplate;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Vector2;

import java.awt.geom.Area;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A layer contains all the bit 3D for a given Z. These bits are organized
 * following a {@link PatternTemplate}. In order to build the 3D bits, it has to
 * start by building a {@link Pavement} for each slice. The
 * {@link #flatPavement} determines the position and orientation of the
 * bits. It is then cloned and shaped to fit the linked {@link Slice}.
 */
public class Layer extends Observable implements Serializable {

    private int layerNumber;
    private Slice horizontalSection;
    private transient Area horizontalArea;
    private Pavement flatPavement;
    private PatternTemplate patternTemplate;
    private Map<Vector2, Bit3D> mapBits3D;
    private boolean paved = false;
    private List<Vector2> irregularBits;

    /**
     * Rebuild the whole layer. To be called after overall changes made on this
     * {@link Layer}
     */
    public void rebuild() {
        flatPavement.computeBits(horizontalSection);
        extrudeBitsTo3D();
        findKeysOfIrregularBits();
        setChanged();
        notifyObservers();
    }

    private void findKeysOfIrregularBits() {
        irregularBits = mapBits3D.keySet().stream()
                .filter(key -> mapBits3D.get(key).isIrregular())
                .collect(Collectors.toList());
    }

    /**
     * Inflate 2D paved bits into 3D shapes
     *
     * @since 0.3
     */
    private void extrudeBitsTo3D() {
        mapBits3D = flatPavement.getBitsKeys().parallelStream()
                .collect(Collectors.toConcurrentMap(key -> key,
                        key -> new Bit3D(flatPavement.getBit(key)),
                        (u, v) -> u, // Preserve the first
                        ConcurrentHashMap::new));
    }

    private void extrudeBitTo3D(Vector2 key) {
        if (flatPavement.getBit(key) != null) {
            Bit3D bit3D = new Bit3D(flatPavement.getBit(key));
            mapBits3D.put(key, bit3D);
            if (bit3D.isIrregular())
                irregularBits.add(key);
        }
    }

    public Vector<Vector2> getBits3dKeys() {
        return new Vector<>(mapBits3D.keySet());
    }

    /**
     * Construct new empty layer. No {@link PatternTemplate}, no {@link Pavement}
     *
     * @param layerNumber       index of layer
     * @param horizontalSection projection of mesh onto layer's altitude
     */
    public Layer(int layerNumber, Slice horizontalSection) {
        this.layerNumber = layerNumber;
        this.horizontalSection = horizontalSection;
        this.horizontalArea = AreaTool.getAreaFrom(horizontalSection);
        this.patternTemplate = null;
        this.flatPavement = null;
    }

    /**
     * Sort bits according to their X position
     * Used for XML writing
     *
     * @return bits' list in order
     */
    public Vector<Pair<Bit3D, Vector2>> sortBits() {
        Vector<Pair<Bit3D, Vector2>> keySet = new Vector<>();
        for (Vector2 key : mapBits3D.keySet()) {
            for (Vector2 pos : mapBits3D.get(key).getLiftPoints()) {
                if (pos != null) {
                    keySet.add(new Pair<>(mapBits3D.get(key), pos));
                }
            }
        }
        keySet.sort((v1, v2) -> {
            if (Double.compare(v1.getValue().x, v2.getValue().x) == 0) {
                return Double.compare(v1.getValue().y, v2.getValue().y);
            } else {
                return Double.compare(v1.getValue().x, v2.getValue().x);
            }
        });
        return keySet;
    }

    /**
     * Add a {@link Bit2D} to the {@link #flatPavement}. Recalculate area of
     * {@link Bit2D} and decide to add into {@link #flatPavement} if it is inside
     * border
     *
     * @param bit expressed in {@link Mesh} coordinate system
     * @return the origin of the newly inserted bit in {@link Mesh} coordinate system.
     * <tt>null</tt> if out of bound
     */
    public Vector2 addBit(Bit2D bit) {
        Area bitArea = bit.getArea();
        bitArea.intersect(horizontalArea);
        if (bitArea.isEmpty()) {
            // Out of bound
            return null;
        } else {
            bit.updateBoundaries(bitArea);
            bit.calcCutPath();
            Vector2 key = flatPavement.addBit(bit);
            extrudeBitTo3D(key);
            return key;
        }
    }

    /**
     * @return index of layer
     */
    public int getLayerNumber() {
        return layerNumber;
    }

    /**
     * @return current pavement
     * @since 0.3
     */
    public Pavement getFlatPavement() {
        return flatPavement;
    }

    /**
     * Change the whole current pavement
     *
     * @param newFlatPavement new base pavement
     */
    public void setFlatPavement(Pavement newFlatPavement) {
        this.flatPavement = newFlatPavement;
    }

    /**
     * @return the boundary of layer
     * @since 0.3
     */
    public Slice getHorizontalSection() {
        return horizontalSection;
    }

    /**
     * Move a bit. The distance of displacement will be determined in dependence
     * on the pattern template. Not use in quick succession because after each
     * move, the layer will be recalculated, slowing the process
     *
     * @param bitKey    origin of bit
     * @param direction the direction in local coordinate system of the bit
     * @return the new origin of the moved bit
     */
    public Vector2 moveBit(Vector2 bitKey, Vector2 direction) {
        double distance = 0;
        if (direction.x == 0) {// up or down
            distance = CraftConfig.bitWidth / 2;
        } else if (direction.y == 0) {// left or right
            distance = CraftConfig.bitLength / 2;
        }
        Vector2 newCoordinate = flatPavement.moveBit(bitKey, direction, distance);
        rebuild();
        return newCoordinate;
    }

    /**
     * Move multiple bits at once.
     *
     * @param bits      chosen bits
     * @param direction chosen way
     * @return list of new origins' position
     */
    public Set<Vector2> moveBits(Set<Bit3D> bits, Vector2 direction) {
        double distance = 0;
        if (direction.x == 0) {// up or down
            distance = CraftConfig.bitWidth / 2;
        } else if (direction.y == 0) {// left or right
            distance = CraftConfig.bitLength / 2;
        }

        final double finalDistance = distance;
        Set<Vector2> newPositions = bits.stream()
                .map(bit -> flatPavement.moveBit(bit.getOrigin(), direction, finalDistance))
                .collect(Collectors.toSet());
        rebuild();
        // Some new positions may be out of border
        return newPositions.stream()
                .filter(pos -> this.getBit3D(pos) != null)
                .collect(Collectors.toSet());
    }

    /**
     * @param key bit origin in 2D plan
     * @return extruded version of bit 2D
     */
    public Bit3D getBit3D(Vector2 key) {
        return mapBits3D.get(key);
    }

    /**
     * Remove a bit
     *
     * @param key origin of bit in 2D plan
     */
    public void removeBit(Vector2 key) {
        flatPavement.removeBit(key);
        mapBits3D.remove(key);
    }

    /**
     * Remove multiple bits
     *
     * @param keys origin of bit in layer coordinate system
     */
    public void removeBits(Collection<Vector2> keys) {
        keys.forEach(flatPavement::removeBit);
    }

    /**
     * Scale a bit
     *
     * @param bit              extruded bit
     * @param percentageLength of {@link CraftConfig#bitLength}
     * @param percentageWidth  of {@link CraftConfig#bitWidth}
     * @return the key of the replaced bit. If <tt>percentageLength</tt> or
     * <tt>percentageWidth</tt> is 0, the bit will be removed instead.
     */
    public Vector2 scaleBit(Bit3D bit, double percentageLength, double percentageWidth) {
        Bit2D modelBit = bit.getBit2dToExtrude();
        removeBit(bit.getOrigin());
        if (percentageLength != 0 && percentageWidth != 0) {
            modelBit.resize(percentageLength, percentageWidth);
            return addBit(modelBit);
        } else {
            return null;
        }
    }

    /**
     * @return the patternTemplate
     */
    public PatternTemplate getPatternTemplate() {
        return patternTemplate;
    }

    /**
     * @param patternTemplate the patternTemplate to set
     */
    public void setPatternTemplate(PatternTemplate patternTemplate) {
        this.patternTemplate = patternTemplate;
    }

    /**
     * Start the {@link #patternTemplate}
     */
    public void startPaver() {
        this.flatPavement = this.patternTemplate.pave(this);
        paved = true;
        rebuild();
    }

    public boolean isPaved() {
        return paved;
    }

    public List<Vector2> getKeysOfIrregularBits() {
        return irregularBits;
    }

    public void paveRegion(Area region, PatternTemplate patternTemplate) {
        if (this.flatPavement == null) {
            setPatternTemplate(patternTemplate);
            setFlatPavement(patternTemplate.pave(this, region));
        } else
            getFlatPavement()
                    .addBits(patternTemplate
                            .pave(this, region)
                            .getBits());
        paved = true;
        rebuild();
    }
}