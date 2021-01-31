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
import meshIneBits.patterntemplates.ManualPattern;
import meshIneBits.patterntemplates.PatternTemplate;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.awt.geom.AffineTransform;


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
    private Path2D horizontalAreaConvert;
    private Pavement flatPavement;
    private PatternTemplate patternTemplate;
    private Map<Vector2, Bit3D> mapBits3D;
    private boolean paved = false;
    private Collection<Vector2> irregularBits; // should be concurrent
    private double lowerAltitude;
    private double higherAltitude;


//    public static class AreaSerializable extends Area implements Serializable{
//        private static final long serialVersionUID = -3627137348463415558L;
//        public AreaSerializable(){
//            super();
//        }
//
//        @Override
//        public Object clone() {
//            return new AreaSerializable(this);
//        }
//
//        public AreaSerializable(Shape s) {
//            super(s);
//        }
//        /**
//         * Writes object out to out.
//         * @param out Output
//         * @throws IOException if I/O errors occur while writing to the
//         *  underlying OutputStream
//         */
//        public void writeObject(java.io.ObjectOutputStream out)
//                throws IOException {
//            out.writeObject(AffineTransform.getTranslateInstance(0, 0).
//                    createTransformedShape(this));
//        }
//        /**
//         * Reads object in from in.
//         * @param in Input
//         * @throws IOException if I/O errors occur while writing to the
//         *  underlying OutputStream
//         * @throws ClassNotFoundException if the class of a serialized object
//         *  could not be found.
//         */
//        public void readObject(java.io.ObjectInputStream in)
//                throws IOException, ClassNotFoundException {
//            add(new Area((Shape) in.readObject()));
//        }
//    }
    private void writeObject(ObjectOutputStream oos) throws IOException {
        // Write normal fields
        horizontalAreaConvert=SerializeArea.toPath2D(horizontalArea);
        oos.writeInt(layerNumber);
        oos.writeObject(horizontalSection);
        oos.writeObject(horizontalAreaConvert);
        oos.writeObject(flatPavement);
        oos.writeObject(patternTemplate);
        oos.writeObject(mapBits3D);
        oos.writeObject(irregularBits);
        oos.writeBoolean(paved);
        oos.writeDouble(lowerAltitude);
        oos.writeDouble(higherAltitude);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.layerNumber = ois.readInt();
        this.horizontalSection = (Slice) ois.readObject();
        this.horizontalAreaConvert = (Path2D) ois.readObject();
        this.horizontalArea = SerializeArea.toArea(horizontalAreaConvert);
        //noinspection unchecked
        this.flatPavement = (Pavement) ois.readObject();
        this.patternTemplate = (PatternTemplate) ois.readObject();
        this.mapBits3D = (Map<Vector2, Bit3D>) ois.readObject();
        this.irregularBits = (Collection) ois.readObject();
        this.paved = ois.readBoolean();
        this.lowerAltitude = ois.readDouble();
        this.higherAltitude = ois.readDouble();
    }

    public static class SerializeArea {
    public static class SerializeArea {

        static Path2D.Double toPath2D(final Area a) {
            final PathIterator pi = a.getPathIterator(new AffineTransform());
            final Path2D.Double path = new Path2D.Double();
            switch (pi.getWindingRule()) {
                case PathIterator.WIND_EVEN_ODD:
                    path.setWindingRule(Path2D.WIND_EVEN_ODD);
                    break;
                case PathIterator.WIND_NON_ZERO:
                    path.setWindingRule(Path2D.WIND_NON_ZERO);
                    break;
                default:
                    throw new UnsupportedOperationException("Unimplemented winding rule.");
            }
            path.append(pi, false);
            return path;
        }

        public static Area toArea(final Path2D path) {
            return new Area(path);
        }
    }

    public void convertHorizontalAreatoPath2D() {
        this.horizontalAreaConvert = SerializeArea.toPath2D(this.horizontalArea);
    }

    public void convertPath2DtoHorizontalArea() {
        this.horizontalArea = SerializeArea.toArea(this.horizontalAreaConvert);
    }


    /**
     * Rebuild the whole layer. To be called after overall changes made on this
     * {@link Layer}
     */
    public void rebuild() {
        flatPavement.computeBits(horizontalArea);
        extrudeBitsTo3D();
        findKeysOfIrregularBits();
        setChanged();
        notifyObservers(new M(
                M.LAYER_REBUILT,
                M.map(
                        M.REBUILT_LAYER,
                        this
                )
        ));
    }

    private void findKeysOfIrregularBits() {
        irregularBits = mapBits3D.keySet().parallelStream()
                .filter(key -> mapBits3D.get(key).isIrregular())
                .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }

    /**
     * Inflate 2D paved bits into 3D shapes
     *
     * @since 0.3
     */
    private void extrudeBitsTo3D() {
        mapBits3D = flatPavement.getBitsKeys().parallelStream()
                .collect(Collectors.toConcurrentMap(key -> key,
                        key -> new Bit3D(flatPavement.getBit(key), this),
                        (u, v) -> u, // Preserve the first
                        ConcurrentHashMap::new));
    }

    /**
     * Extrude {@link Bit2D} to {@link Bit3D} and detect irregularities
     *
     * @param key origin of {@link Bit2D}
     */
    private void rebuild(Vector2 key) {
        if (flatPavement.getBit(key) != null) {
            Bit3D bit3D = new Bit3D(flatPavement.getBit(key), this);
            mapBits3D.put(key, bit3D);
            if (bit3D.isIrregular())
                irregularBits.add(key);
        }
    }

    public Vector<Vector2> getBits3dKeys() {
        if(mapBits3D==null)return new Vector<>();
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
//        Area area = AreaTool.getAreaFrom(horizontalSection);
//        AreaSerializable areaSerializable = (AreaSerializable) horizontalArea;
        this.patternTemplate = null;
        this.flatPavement = null;
        this.lowerAltitude = horizontalSection.getAltitude()
                - CraftConfig.firstSliceHeightPercent / 100
                * CraftConfig.bitThickness;
        this.higherAltitude = this.lowerAltitude + CraftConfig.bitThickness;
        this.irregularBits = new ConcurrentLinkedQueue<>();
    }

    /**
     * Sort bits according to their X position
     * Used for XML writing
     *
     * @return bits' list in order
     */
    public Vector<Pair<Bit3D, Vector2>> sortBits() {
        Vector<Pair<Bit3D, Vector2>> keySet = new Vector<>();
        if (mapBits3D == null) {
            return keySet;
        }
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
     * @param b   <tt>true</tt> to notify observers
     * @return the origin of the newly inserted bit in {@link Mesh} coordinate system.
     * <tt>null</tt> if out of bound
     */
    public Vector2 addBit(Bit2D bit, boolean b) {
        Area bitArea = getInteriorArea(bit);
        if (bitArea == null) {
            // Out of bound
            return null;
        } else {
            bit.updateBoundaries(bitArea);
            bit.calcCutPath();
            Vector2 key = flatPavement.addBit(bit);
            rebuild(key);
            if (b) {
                setChanged();
                notifyObservers(new M(
                        M.LAYER_ADDED_BIT,
                        M.map(
                                M.NEW_BIT,
                                getBit3D(key)
                        )
                ));
            }
            return key;
        }
    }

    /**
     * @param bit2D target
     * @return <tt>null</tt> if outside
     */
    private Area getInteriorArea(Bit2D bit2D) {
        Area bitArea = bit2D.getArea();
        if (horizontalArea == null) {
            Logger.message("null valeur");
        }
        bitArea.intersect(horizontalArea);
        if (bitArea.isEmpty()) {
            // Out of bound
            return null;
        } else
            return bitArea;
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
     * Change the whole current pavement. Should call {@link #rebuild()} to extrude
     * full bit
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
     * @param bit3D    target
     * @param direction the direction in local coordinate system of the bit
     * @return the new origin of the moved bit
     */
    public Vector2 moveBit(Bit3D bit3D, Vector2 direction) {
        irregularBits.remove(bit3D.getOrigin());
        double distance = 0;
        if (direction.x == 0) {// up or down
            distance = CraftConfig.bitWidth / 2;
        } else if (direction.y == 0) {// left or right
            distance = CraftConfig.LengthFull / 2;
        }
        Vector2 newCoordinate = flatPavement.moveBit(bit3D.getOrigin(), direction, distance);
        rebuild(newCoordinate);
        setChanged();
        notifyObservers(new M(
                M.LAYER_MOVED_BIT,
                M.map(
                        M.OLD_BIT,
                        bit3D,
                        M.NEW_BIT,
                        getBit3D(newCoordinate)
                )
        ));
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
        // Calculate travel distance
        double distance = 0;
        if (direction.x == 0) {// up or down
            distance = CraftConfig.bitWidth / 2;
        } else if (direction.y == 0) {// left or right
            distance = CraftConfig.LengthFull / 2;
        }

        // Move bits
        final double finalDistance = distance;
        Set<Vector2> newPositions = bits.stream()
                .map(bit -> flatPavement.moveBit(bit.getOrigin(), direction, finalDistance))
                .collect(Collectors.toSet());

        // Remove old bits
        removeBits(
                bits.stream()
                        .map(Bit3D::getOrigin)
                        .collect(Collectors.toList()),
                false
        );

        // Filter new bits 3D
        Collection<Bit3D> newBit3Ds = new ConcurrentLinkedQueue<>();
        for (Vector2 pos : new HashSet<>(newPositions)) {
            Bit2D bit2D = flatPavement.getBit(pos);
            Area a = getInteriorArea(bit2D);
            if (a == null) {
                flatPavement.removeBit(pos);
                newPositions.remove(pos);
            } else {
                bit2D.updateBoundaries(a);
                bit2D.calcCutPath();
                rebuild(pos);
                newBit3Ds.add(getBit3D(pos));
            }
        }

        setChanged();
        notifyObservers(new M(
                M.LAYER_MOVED_BITS,
                M.map(
                        M.OLD_BIT,
                        bits,
                        M.NEW_BIT,
                        newBit3Ds
                )
        ));

        return newPositions;
    }

    /**
     * @param key bit origin in 2D plan
     * @return extruded version of bit 2D
     */
    public Bit3D getBit3D(Vector2 key) {
        return mapBits3D.get(key);
    }

    public List<Bit3D> getAllBit3D() {
        return new ArrayList<>(mapBits3D.values());
    }

    /**
     * Remove a bit
     *
     * @param key origin of bit
     * @param b   notify or not
     */
    public void removeBit(Vector2 key, boolean b) {
        Bit3D oldBit = getBit3D(key);
        flatPavement.removeBit(key);
        mapBits3D.remove(key);
        irregularBits.remove(key);
        if (b) {
            setChanged();
            notifyObservers(new M(
                    M.LAYER_REMOVED_BIT,
                    M.map(
                            M.OLD_BIT,
                            oldBit
                    )
            ));
        }
    }

    /**
     * Remove multiple bits
     *
     * @param keys origin of bit in layer coordinate system
     * @param b    notify or not
     */
    public void removeBits(Collection<Vector2> keys, boolean b) {
        Collection<Bit3D> oldBit3Ds = keys
                .parallelStream()
                .map(this::getBit3D)
                .collect(Collectors
                        .toCollection(ConcurrentLinkedQueue::new));
        keys.forEach(key -> this.removeBit(key, false));
        if (b) {
            setChanged();
            notifyObservers(new M(
                    M.LAYER_REMOVED_BITS,
                    M.map(
                            M.OLD_BIT,
                            oldBit3Ds
                    )
            ));
        }
    }

    /**
     * Scale a bit
     *
     * @param bit              extruded bit
     * @param percentageLength of {@link CraftConfig#LengthFull}
     * @param percentageWidth  of {@link CraftConfig#bitWidth}
     * @return the key of the replaced bit. If <tt>percentageLength</tt> or
     * <tt>percentageWidth</tt> is 0, the bit will be removed instead.
     */
    public Vector2 scaleBit(Bit3D bit, double percentageLength, double percentageWidth) {
        Bit2D modelBit = bit.getBaseBit();
        removeBit(bit.getOrigin(), true);
        if (percentageLength != 0 && percentageWidth != 0) {
            modelBit.resize(percentageLength, percentageWidth);
            return addBit(modelBit, true);
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

    public Collection<Vector2> getKeysOfIrregularBits() {
        return irregularBits;
    }

    public void paveRegion(Area region, PatternTemplate patternTemplate) {
        if (this.flatPavement == null) {
            setPatternTemplate(new ManualPattern());
            setFlatPavement(patternTemplate.pave(this, region));
        } else
            getFlatPavement()
                    .addBits(patternTemplate
                            .pave(this, region)
                            .getBits());
        paved = true;
        rebuild();
    }

    public double getLowerAltitude() {
        return lowerAltitude;
    }

    public double getHigherAltitude() {
        return higherAltitude;
    }

}