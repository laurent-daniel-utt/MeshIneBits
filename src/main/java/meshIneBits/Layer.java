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
import meshIneBits.util.Vector2;

import java.awt.geom.AffineTransform;
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
    private Vector<Slice> slices;
    private Slice horizontalSection;
    private Pavement flatPavement;
    @Deprecated
    private transient Pavement referentialPavement;
    private PatternTemplate patternTemplate;
    private Vector<Pavement> pavements = new Vector<>();
    private Map<Vector2, Bit3D> mapBits3D;
    private int sliceToSelect;

    /**
     * Constructor for multiple slices
     *
     * @param slices      possibilities of border
     * @param layerNumber index
     * @param mesh        object to print
     * @deprecated
     */
    public Layer(Vector<Slice> slices, int layerNumber, Mesh mesh) {
        this.slices = slices;
        this.layerNumber = layerNumber;
        this.patternTemplate = mesh.getPatternTemplate();
        this.referentialPavement = mesh.getPatternTemplate().pave(this);
        setSliceToSelect(CraftConfig.defaultSliceToSelect);

        rebuild();
    }

    /**
     * Construct new layer given a slice and a parameterized pattern
     *
     * @param layerNumber       index of layer
     * @param horizontalSection projection of mesh onto certain altitude
     * @param patternTemplate   algorithm of flat pavement
     * @since 0.3
     */
    public Layer(int layerNumber, Slice horizontalSection, PatternTemplate patternTemplate) {
        this.layerNumber = layerNumber;
        this.horizontalSection = horizontalSection;
        this.patternTemplate = patternTemplate;
        this.flatPavement = patternTemplate.pave(this);
        this.flatPavement.computeBits(horizontalSection);
        rebuild();
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
            for (Vector2 pos : mapBits3D.get(key).getDepositPoints()) {
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
     * Add a bit to the {@link #flatPavement} and call {@link #rebuild}
     * which will reconstruct all the {@link Pavement} taking in account this new
     * bit.
     *
     * @param bit target
     * @return the key of the newly inserted bit. <tt>null</tt> if out of bound
     */
    public Vector2 addBit(Bit2D bit) {
        Vector2 newKey = flatPavement.addBit(bit);
        rebuild();
        if (this.getBit3D(newKey) == null) {
            return null;
        } else {
            return newKey;
        }
    }

    /**
     * @deprecated
     */
    @SuppressWarnings("unused")
    private void buildPatterns() {
        pavements.clear();
        for (Slice s : slices) {
            pavements.add(referentialPavement.clone());
            pavements.lastElement().computeBits(s);
        }
    }

    /**
     * Computer lift points after extruding 3D bits
     */
    private void computeLiftPoints() {
        for (Vector2 key : getBits3dKeys()) {
            mapBits3D.get(key).computeLiftPoints();
        }
    }

    /**
     * Build the {@link Bit3D} from all the {@link Bit2D} contained in the
     * {@link Pavement}.
     *
     * @deprecated
     */
    @SuppressWarnings("unused")
    private void generateBits3D() {
        mapBits3D = new Hashtable<>();
        Vector<Bit2D> bitsToInclude = new Vector<>();

        for (Vector2 bitKey : referentialPavement.getBitsKeys()) {
            for (Pavement pavement : pavements) {
                if (pavement.getBitsKeys().contains(bitKey)) {
                    bitsToInclude.add(pavement.getBit(bitKey));
                } else {
                    bitsToInclude.add(null);
                }
            }

            for (int i = 0; i < bitsToInclude.size(); i++) {
                if (bitsToInclude.get(i) != null) {
                    Bit3D newBit;
                    try {
                        newBit = new Bit3D(bitsToInclude, bitKey, getNewOrientation(bitsToInclude.get(i)),
                                sliceToSelect);
                        mapBits3D.put(bitKey, newBit);
                    } catch (Exception e) {
                        if ((!e.getMessage().equals("This bit does not contain enough bit 2D in it"))
                                && (!e.getMessage().equals("The slice to select does not exist in that bit"))) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
            }

            bitsToInclude.clear();

        }
    }

    /**
     * Inflate 2D paved bits into 3D shapes
     *
     * @since 0.3
     */
    private void extrudeBitsTo3D() {
        mapBits3D = flatPavement.getBitsKeys().parallelStream()
                .collect(Collectors.toConcurrentMap(key -> key,
                        key -> {
                            Bit2D bit2D = flatPavement.getBit(key);
                            return new Bit3D(bit2D, key,
                                    bit2D.getOrientation().getTransformed(flatPavement.getAffineTransform()));
                        }
                        , (u, v) -> u, ConcurrentHashMap::new));
    }

    /**
     * @param key bit origin in 2D plan
     * @return extruded version of bit 2D
     */
    public Bit3D getBit3D(Vector2 key) {
        return mapBits3D.get(key);
    }

    public Vector<Vector2> getBits3dKeys() {
        return new Vector<>(mapBits3D.keySet());
    }

    /**
     * @return index of layer
     */
    public int getLayerNumber() {
        return layerNumber;
    }

    /**
     * @param bit inside pavement
     * @return orientation in layer's coordinate system
     * @deprecated
     */
    private Vector2 getNewOrientation(Bit2D bit) {
        AffineTransform patternAffTrans = (AffineTransform) referentialPavement.getAffineTransform().clone();
//		patternAffTrans.translate(-CraftConfig.xOffset, -CraftConfig.yOffset);
        return bit.getOrientation().getTransformed(patternAffTrans);
    }

    /**
     * @return all possible pavements basing on boundaries
     * @deprecated
     */
    public Vector<Pavement> getPavements() {
        return this.pavements;
    }

    /**
     * @return all possible boundaries
     * @deprecated
     */
    public Vector<Slice> getSlices() {
        return this.slices;
    }

    /**
     * @return the index of selected slice
     * @deprecated
     */
    public int getSliceToSelect() {
        return sliceToSelect;
    }

    /**
     * @return the selected pattern
     * @deprecated replaced by {@link #getFlatPavement()}
     */
    public Pavement getSelectedPattern() {
        return this.pavements.get(sliceToSelect);
    }

    /**
     * @return current pavement
     * @since 0.3
     */
    public Pavement getFlatPavement() {
        return flatPavement;
    }

    /**
     * @return the slice corresponding to the selected pattern
     * @deprecated replaced by {@link #getHorizontalSection()}
     */
    public Slice getSelectedSlice() {
        return this.slices.elementAt(sliceToSelect);
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
        Vector2 newCoordinate = patternTemplate.moveBit(flatPavement, bitKey, direction);
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
        Set<Vector2> newPositions = bits.stream()
                .map(bit -> patternTemplate.moveBit(flatPavement, bit.getOrigin(), direction))
                .collect(Collectors.toSet());
        rebuild();
        // Some new positions may be out of border
        return newPositions.stream()
                .filter(pos -> this.getBit3D(pos) != null)
                .collect(Collectors.toSet());
    }

    /**
     * Rebuild the whole layer. To be called after every changes made on this
     * layer
     */
    public void rebuild() {
//		buildPatterns();
//      generateBits3D();
        flatPavement.computeBits(horizontalSection);
        extrudeBitsTo3D();
        computeLiftPoints();
        setChanged();
        notifyObservers();
    }

    /**
     * Remove a bit
     *
     * @param key     origin of bit in 2D plan
     * @param rebuild need to rebuild whole layer
     */
    public void removeBit(Vector2 key, boolean rebuild) {
        flatPavement.removeBit(key);
        if (rebuild) {
            rebuild();
        }
    }

    /**
     * Remove multiple bits
     *
     * @param keys    origin of bit in layer coordinate system
     * @param rebuild need to rebuild whole layer
     */
    public void removeBits(Collection<Vector2> keys, boolean rebuild) {
        keys.forEach(flatPavement::removeBit);
        if (rebuild) {
            rebuild();
        }
    }

    /**
     * Scale a bit
     *
     * @param bit              extruded bit
     * @param percentageLength 0 to 100
     * @param percentageWidth  0 to 100
     * @return the key of the replaced bit. If <tt>percentageLength</tt> or
     * <tt>percentageWidth</tt> is 0, the bit will be removed instead.
     */
    public Vector2 scaleBit(Bit3D bit, double percentageLength, double percentageWidth) {
        Bit2D modelBit = bit.getBit2dToExtrude();
        removeBit(bit.getOrigin(), false);
        if (percentageLength != 0 && percentageWidth != 0) {
            Bit2D newBit = new Bit2D(modelBit, percentageLength, percentageWidth);
            return addBit(newBit);
        } else {
            return null;
        }
    }

    /**
     * Only for default slice to select
     *
     * @param percentageOfHeight >0 and <=100
     * @deprecated
     */
    private void setSliceToSelect(double percentageOfHeight) {
        sliceToSelect = (int) Math.round((percentageOfHeight / 100) * (slices.size() - 1));
    }

    /**
     * @param sliceNbr index
     * @deprecated
     */
    public void setSliceToSelect(int sliceNbr) {
        if ((sliceNbr >= 0) && (sliceNbr < slices.size())) {
            sliceToSelect = sliceNbr;
            rebuild();
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
     * @param referentialPavement base pavement
     * @deprecated
     */
    public void setReferentialPavement(Pavement referentialPavement) {
        this.referentialPavement = referentialPavement;
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
     * Rotate multiple bits around their center
     *
     * @param bitKeys in surface
     * @param degrees in degrees
     * @return images after rotation
     */
    public Set<Vector2> rotateBits(Set<Vector2> bitKeys, double degrees) {
        Set<Vector2> newPositions = bitKeys.stream()
                .map(bitKey -> flatPavement.rotateBit(bitKey, degrees))
                .collect(Collectors.toSet());
        rebuild();
        // Some new positions may be out of border
        return newPositions.stream()
                .filter(pos -> this.getBit3D(pos) != null)
                .collect(Collectors.toSet());
    }
}