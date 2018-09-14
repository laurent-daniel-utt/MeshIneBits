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

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Hashtable;
import java.util.Vector;

import meshIneBits.patterntemplates.PatternTemplate;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;

/**
 * Build by a {@link PatternTemplate} or manually.
 * Contains a set of {@link Bit2D}.
 */
public class Pavement implements Cloneable {
    private Vector2 rotation;
    /**
     * The key is the origin of bit in coordinate system of global object.
     */
    private Hashtable<Vector2, Bit2D> mapBits;
    private AffineTransform transformMatrix = new AffineTransform();
    private AffineTransform inverseTransformMatrix;

    private Pavement(Hashtable<Vector2, Bit2D> mapBits, Vector2 rotation, AffineTransform transformMatrix,
                     AffineTransform inverseTransformMatrix) {
        this.rotation = rotation;
        this.mapBits = mapBits;
        this.transformMatrix = transformMatrix;
        this.inverseTransformMatrix = inverseTransformMatrix;
    }

    /**
     * Construct pavement out of bits and chosen rotation
     *
     * @param bits     polygons in 2D plan
     * @param rotation specific to layer
     */

    public Pavement(Vector<Bit2D> bits, Vector2 rotation) {

        this.rotation = rotation;

        // Each pavement can have a rotation, usually linked to the layer number
        transformMatrix.rotate(rotation.x, rotation.y);

        try {
            inverseTransformMatrix = ((AffineTransform) transformMatrix.clone()).createInverse();
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }

        // Set up map of bits
        mapBits = new Hashtable<>();
        for (Bit2D bit : bits) {
            addBit(bit);
        }
    }

    /**
     * @param bit what to incorporate into pavement
     * @return the key of inserted bit in this pavement
     */
    public Vector2 addBit(Bit2D bit) {
        // the key of each bit is its origin's coordinates in the general coo
        // system
        Vector2 bitKey = bit.getOrigin().getTransformed(transformMatrix);
        // We check that there is not already a bit at this place
        for (Vector2 key : getBitsKeys()) {
            if (bitKey.asGoodAsEqual(key)) {
                Logger.warning(
                        "A bit already exists at these coordinates: " + key
                                + ", it has been replaced by the new one.");
                removeBit(key);
            }

        }
        mapBits.put(bitKey, bit);
        return bitKey;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Pavement clone() {
        Hashtable<Vector2, Bit2D> clonedMapBits = new Hashtable<>();
        for (Vector2 key : this.getBitsKeys()) {
            clonedMapBits.put(key, mapBits.get(key).clone());
        }

        return new Pavement(clonedMapBits, rotation, transformMatrix, inverseTransformMatrix);
    }

    /**
     * Removes the {@link Bit2D} that are outside the boundaries of the
     * {@link Slice} and cut at right shape the ones that intercepts the boundaries.
     *
     * @param slice boundary
     */
    public void computeBits(Slice slice) {
        Area sliceArea = AreaTool.getAreaFrom(slice);
        sliceArea.transform(inverseTransformMatrix);
        Vector<Vector2> keys = new Vector<>(mapBits.keySet());
        for (Vector2 key : keys) {
            Bit2D bit = mapBits.get(key);
            Area bitArea = new Area();
            bitArea.add(bit.getArea());
            bitArea.intersect(sliceArea);
            if (bitArea.isEmpty()) {
                mapBits.remove(key);
            } else {
                bit.updateBoundaries(bitArea);
                bit.calcCutPath();
            }
        }
    }

    public AffineTransform getAffineTransform() {
        return transformMatrix;
    }

    public Bit2D getBit(Vector2 key) {
        return mapBits.get(key);
    }

    public Vector<Vector2> getBitsKeys() {
        return new Vector<>(mapBits.keySet());
    }

    /**
     * Move the chosen bit in the wanted direction. Note: not exactly "moving", but
     * rather "removing" then "adding" new one
     *
     * @param key         the key of the bit we want to move
     * @param direction   in the local coordinate system of the bit
     * @param offsetValue the distance of displacement
     * @return the key of the newly added bit
     */
    public Vector2 moveBit(Vector2 key, Vector2 direction, double offsetValue) {
        Bit2D bitToMove = mapBits.get(key);
        removeBit(key);
        Vector2 localDirection = bitToMove.getOrientation();
        AffineTransform rotateMatrix = new AffineTransform();
        rotateMatrix.rotate(direction.x, direction.y);
        localDirection = localDirection.getTransformed(rotateMatrix);
        localDirection = localDirection.normal();
        Vector2 newCenter = new Vector2(bitToMove.getOrigin().x + (localDirection.x * offsetValue),
                bitToMove.getOrigin().y + (localDirection.y * offsetValue));
        return addBit(new Bit2D(newCenter, bitToMove.getOrientation(), bitToMove.getLength(), bitToMove.getWidth()));
    }

    public void removeBit(Vector2 key) {
        mapBits.remove(key);
    }
}
