/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2022 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
 * Copyright (C) 2020-2021 DO Quang Bao.
 * Copyright (C) 2021 VANNIYASINGAM Mithulan.
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
 *
 */

package meshIneBits.patterntemplates;

import meshIneBits.Bit2D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.Pavement;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.DetectorTool;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Pattern improved from {@link ClassicBrickPattern}.
 *
 * @author NHATHAN
 */
public class ImprovedBrickPattern extends PatternTemplate {

    private Vector2 patternStart;
    private Vector2 patternEnd;
    private double bitsWidthSpace;
    private double bitsLengthSpace;

    @Override
    public Pavement pave(Layer layer) {
        int layerNumber = layer.getLayerNumber();
        double diffRotation = (double) config.get("diffRotation").getCurrentValue();
        Vector2 customizedRotation = Vector2.getEquivalentVector((diffRotation * layerNumber) % 360);

        // Recalculate patternStart/End
        Rectangle2D.Double r = new Rectangle2D.Double(
                patternStart.x,
                patternStart.y,
                patternEnd.x - patternStart.x,
                patternEnd.y - patternStart.y
        );
        AffineTransform t1 = new AffineTransform(), t2 = new AffineTransform();
        t1.rotate(customizedRotation.x, -customizedRotation.y); // Rotate backward
        t2.rotate(customizedRotation.x, customizedRotation.y); // Rotate forward
        Shape r2 = t1.createTransformedShape(r);
        Rectangle2D bound = r2.getBounds2D();
        Vector2 newPatternStart = new Vector2(bound.getX(), bound.getY()),
                newPatternEnd = new Vector2(bound.getMaxX(), bound.getMaxY());

        Collection<Bit2D> bits =
                pave(layerNumber, newPatternStart, newPatternEnd).stream()
                        .map(bit2D -> bit2D.createTransformedBit(t2)) // Rotate forward
                        .collect(Collectors.toList());

        return new Pavement(bits);
    }

    @Override
    public Pavement pave(Layer layer, Area area) {
        int layerNumber = layer.getLayerNumber();
        double diffRotation = (double) config.get("diffRotation").getCurrentValue();
        Vector2 customizedRotation = Vector2.getEquivalentVector((diffRotation * layerNumber) % 360);

        AffineTransform t1 = new AffineTransform(), t2 = new AffineTransform();
        t1.rotate(customizedRotation.x, -customizedRotation.y); // Rotate backward
        t2.rotate(customizedRotation.x, customizedRotation.y); // Rotate forward
        area.intersect(AreaTool.getAreaFrom(layer.getHorizontalSection())); // what we need to pave
        Shape r2 = t1.createTransformedShape(area);
        Rectangle2D bound = r2.getBounds2D();
        Vector2 newPatternStart = new Vector2(bound.getX(), bound.getY()),
                newPatternEnd = new Vector2(bound.getMaxX(), bound.getMaxY());
        Collection<Bit2D> bits =
                pave(layerNumber, newPatternStart, newPatternEnd).stream()
                        .map(bit2D -> bit2D.createTransformedBit(t2))
                        .collect(Collectors.toList());

        Pavement pavement = new Pavement(bits);
        pavement.computeBits(area);
        return pavement;
    }

    private Collection<Bit2D> pave(int layerNumber, Vector2 patternStart, Vector2 patternEnd) {
        Vector<Bit2D> bits = new Vector<>();
        // Setup parameters
        bitsWidthSpace = (double) config.get("bitsWidthSpace").getCurrentValue();
        bitsLengthSpace = (double) config.get("bitsLengthSpace").getCurrentValue();
        double diffxOffset = (double) config.get("diffxOffset").getCurrentValue();
        double diffyOffset = (double) config.get("diffyOffset").getCurrentValue();
        // The first bit is displaced by multiples of diffxOffset and
        // diffyOffset
        Vector2 _1stBit = new Vector2(diffxOffset * layerNumber % CraftConfig.lengthFull,
                diffyOffset * layerNumber % CraftConfig.bitWidth);
        // Fill out the square
        int lineNum = 0;// Initialize
        // Vertically downward
        while (_1stBit.y - CraftConfig.bitWidth / 2
                + lineNum * (CraftConfig.bitWidth + bitsLengthSpace) <= patternEnd.y + CraftConfig.bitWidth / 2) {
            // Horizontally
            if (lineNum % 2 == 0) {
                fillHorizontally(
                        new Vector2(_1stBit.x,
                                _1stBit.y + lineNum * (CraftConfig.bitWidth + bitsLengthSpace)),
                        bits,
                        patternStart,
                        patternEnd);
            } else {
                fillHorizontally(
                        new Vector2(_1stBit.x + CraftConfig.lengthFull / 2 + bitsWidthSpace / 2,
                                _1stBit.y + lineNum * (CraftConfig.bitWidth + bitsLengthSpace)),
                        bits,
                        patternStart,
                        patternEnd);
            }
            lineNum++;
        }
        // Vertically upward
        lineNum = 1; // Reinitialize
        while (_1stBit.y + CraftConfig.bitWidth / 2
                - lineNum * (CraftConfig.bitWidth + bitsLengthSpace) >= patternStart.y) {
            // Horizontally
            if (lineNum % 2 == 0) {
                fillHorizontally(
                        new Vector2(_1stBit.x,
                                _1stBit.y - lineNum * (CraftConfig.bitWidth + bitsLengthSpace)),
                        bits,
                        patternStart,
                        patternEnd);
            } else {
                fillHorizontally(
                        new Vector2(_1stBit.x + CraftConfig.lengthFull / 2 + bitsWidthSpace / 2,
                                _1stBit.y - lineNum * (CraftConfig.bitWidth + bitsLengthSpace)),
                        bits,
                        patternStart,
                        patternEnd);
            }
            lineNum++;
        }
        return bits;
    }

    /**
     * Fill a line of bits into set of bits, given the origin of the first bit.
     *
     * @param _1stBitOrigin origin of departure
     * @param bits          set of bits of this layer
     * @param patternStart  limit to the left
     * @param patternEnd    limit to the right
     */
    private void fillHorizontally(Vector2 _1stBitOrigin, Vector<Bit2D> bits, Vector2 patternStart, Vector2 patternEnd) {
        double L = CraftConfig.lengthFull;
        double f = bitsWidthSpace;
        // To the right
        int colNum = 0; // Initialize
        while (_1stBitOrigin.x - L / 2 + colNum * (L + f) <= patternEnd.x + CraftConfig.lengthFull / 2) {
            bits.add(new Bit2D(new Vector2(_1stBitOrigin.x + colNum * (L + f), _1stBitOrigin.y), new Vector2(1, 0)));
            colNum++;
        }
        // To the left
        colNum = 1; // Reinitialize
        while (_1stBitOrigin.x + L / 2 - colNum * (L + f) >= patternStart.x - CraftConfig.lengthFull / 2) {
            bits.add(new Bit2D(new Vector2(_1stBitOrigin.x - colNum * (L + f), _1stBitOrigin.y), new Vector2(1, 0)));
            colNum++;
        }
    }

    @Override
    public int optimize(Layer actualState) {
        Logger.updateStatus("Optimizing layer " + actualState.getLayerNumber());
        // this boolean to check that if we'd tried all possibilities
        boolean allFail = false;
        Pavement currentPavement = actualState.getFlatPavement();
        List<Vector2> irregularBitKeys = new Vector<>();
        // Sort irregular bit key in y & x order to stabilize process
        Comparator<Vector2> irBitKeysSorter = (Vector2 k1, Vector2 k2) -> {
            if (k1.y != k2.y) return k1.y < k2.y ? -1 : 1;
            else
                return k1.x < k2.x ? -1 : 1;
        };
        while (!allFail) {
            Slice boundary = actualState.getHorizontalSection();
            Vector2 localDirectionToMove = null, irBitKeyToMove = null;
            irregularBitKeys = DetectorTool.detectIrregularBits(currentPavement);
            irregularBitKeys.sort(irBitKeysSorter);
            if (irregularBitKeys.isEmpty()) break;
            // We will find the first irregular bit that we can resolve
            for (Vector2 irBitKey : irregularBitKeys) {
                // We try to move this irregular bit in 4 directions, starting
                // with height's sides.
                // If there is at least one way to reduce the number of
                // irregular bits in the pattern,
                // we choose that direction and apply on the pattern
                localDirectionToMove = attemptToSolve(currentPavement, boundary, irBitKey, irregularBitKeys);
                if (localDirectionToMove != null) {
                    irBitKeyToMove = irBitKey;
                    break;
                }
            }
            // If we have at least one chance to move
            if (localDirectionToMove != null && irBitKeyToMove != null) {
                Bit2D initialStateOfBitToMove = currentPavement.getBit(irBitKeyToMove);
                Vector2 newPos = this.pushBit(currentPavement, boundary, irBitKeyToMove, localDirectionToMove);
                Logger.updateStatus("Moved bit at " + irBitKeyToMove + " in direction "
                        + localDirectionToMove.rotate(initialStateOfBitToMove.getOrientation())
                        + " to " + newPos);

                // We cover behind
                this.cover(currentPavement, boundary, initialStateOfBitToMove, localDirectionToMove);
            } else {
                // Else if we don't have anyway to solve
                // We stop the process of resolution
                allFail = true;
            }
        }
        // Apply the changes on whole layer
        actualState.setFlatPavement(currentPavement);
        actualState.rebuild();
        return irregularBitKeys.size();
    }

    /**
     * Attempt to resolve by moving the bit in 4 directions.
     * <p>
     * Prioritizing the height's sides. If the obtained state has less irregular
     * bits, we will follow that way. Note: we also try to cover what we left behind
     * by a full bit.
     *
     * @param pavement       the selected pavement in the layer. This method will work on its
     *                       clone
     * @param boundary       used to re-validate the attempt
     * @param irBitKey       the key of the bit to try
     * @param irregularities keys of current irregular bits
     * @return the first direction which reduce the total number of irregular bits
     * in the pavement. <tt>Null</tt> if no way to get better state. Calculated in
     * local coordinate system of input bit
     */
    private Vector2 attemptToSolve(Pavement pavement,
                                   Slice boundary,
                                   Vector2 irBitKey,
                                   List<Vector2> irregularities) {
        // Initial number of irregularities
        int initialIrregularities = irregularities.size();
        Vector2[] localDirectionsForTrying = {new Vector2(1, 0), // right
                new Vector2(-1, 0), // left
                new Vector2(0, 1), // up
                new Vector2(0, -1) // down
        };
        for (Vector2 localDirectionToTry : localDirectionsForTrying) {
            // We need to conserve pavement
            // So we work on a clone
            Pavement clonedPavement = pavement.clone();
            Bit2D initialBit = clonedPavement.getBit(irBitKey);
            Vector2 newOrigin = this.pushBit(clonedPavement, boundary, irBitKey, localDirectionToTry);
            // Check that we did not push the bit into the air
            if (newOrigin == null) {
                continue;
            }
            // We cover what we left behind
            // We do not need to cover if the bit is not full
            this.cover(clonedPavement, boundary, initialBit, localDirectionToTry);
            // Re-validate
            if (initialIrregularities > DetectorTool.detectIrregularBits(clonedPavement).size())
                return localDirectionToTry;
        }
        return null;
    }

    /**
     * Cover the gap left behind after moving a bit forward.
     *
     * @param actualState          current situation
     * @param boundary             to determine the necessity of cover
     * @param movedBit             the initial state. Full bit
     * @param localDirectionToMove in which we move the bit. In the local coordinate
     * @return origin of cover bit. <tt>Null</tt> if no need to cover
     */
    private Vector2 cover(Pavement actualState,
                          Slice boundary,
                          Bit2D movedBit,
                          Vector2 localDirectionToMove) {
        if ((movedBit.getLength() == CraftConfig.lengthFull
                && localDirectionToMove.y == 0) // right or left
                || (movedBit.getWidth() == CraftConfig.bitWidth
                && localDirectionToMove.x == 0)) { // up or down
            Vector2 initialCenter = movedBit.getCenter();
            Vector2 coveringBitKey = actualState.addBit(
                    new Bit2D(
                            initialCenter.x,
                            initialCenter.y,
                            movedBit.getLength(),
                            movedBit.getWidth(),
                            movedBit.getOrientation().x,
                            movedBit.getOrientation().y
                    )
            );
            return this.pushBit(actualState, boundary, coveringBitKey, localDirectionToMove.getOpposite());
        }
        return null;
    }

    /**
     * To push forward a bit into one direction.
     * <p>
     * Automatically reduce back every bit in front of it. The step of push is
     * determined by direction, either a half of {@link CraftConfig#lengthFull} or
     * {@link CraftConfig#bitWidth}
     *
     * @param actualState          current situation
     * @param boundary             anticipate cases of pushing into thin air
     * @param keyOfBitToMove       key of the bitToMove
     * @param localDirectionToPush in the coordinate system of the bitToMove. Either (1, 0), (-1, 0),
     *                             (0, 1), (0, -1).
     * @return new origin of bit after being pushed into the given direction.
     * <tt>Null</tt> if we push bit out of <tt>boundary</tt>
     */
    private Vector2 pushBit(Pavement actualState,
                            Slice boundary,
                            Vector2 keyOfBitToMove,
                            Vector2 localDirectionToPush) {
        Bit2D bitToPush = actualState.getBit(keyOfBitToMove);
        // Calculating the distance to push
        double verticalDisplacement,
                // these 2 are for calculating origins of covering bits
                horizontalDisplacement,
                // to move the bits in front a little more
                // in order to have space between bits
                additionalVerticalDisplacement,
                additionalHorizontalDisplacement,
                // quart bit to cover
                coveringBitLength = CraftConfig.lengthFull / 2,
                coveringBitWidth = CraftConfig.bitWidth / 2;
        if (localDirectionToPush.x == 0) {
            // If we push up and down
            verticalDisplacement = CraftConfig.bitWidth / 2;
            horizontalDisplacement = CraftConfig.lengthFull / 2;
            additionalVerticalDisplacement = bitsLengthSpace / 2;
            additionalHorizontalDisplacement = bitsWidthSpace / 2;
        } else {
            // If we push right or left
            verticalDisplacement = CraftConfig.lengthFull / 2;
            horizontalDisplacement = CraftConfig.bitWidth / 2;
            additionalVerticalDisplacement = bitsWidthSpace / 2;
            additionalHorizontalDisplacement = bitsLengthSpace / 2;
        }

        // Note that, we move by a distance
        // equal to what we reduce the bits in front of us
        Vector2 newOrigin = this.moveBitWithoutKeepingCutpaths(
                actualState,
                keyOfBitToMove,
                localDirectionToPush,
                verticalDisplacement);
        actualState.computeBits(boundary);
        // Check if we move into thin air
        // If yes, we skip the push
        // If no, we reduce bits in front
        if (actualState.getBit(newOrigin) == null) return null;

        // Find all the bits in front of bitToMove.
        // And classify them into 2 groups:
        // One consisting of bits whose centers are in front of bitToMove,
        // Other consisting of bits whose centers are not.
        // The way we treat these 2 groups are different
        Vector<Vector2> bitEntirelyInFrontOfBitToMove = new Vector<>();
        Vector<Vector2> bitPartiallyInFrontOfBitToMove = new Vector<>();
        for (Vector2 bitKey : actualState.getBitsKeys()) {
            if (bitKey.equals(keyOfBitToMove) || bitKey.equals(newOrigin))
                continue;
            Bit2D bitToCompare = actualState.getBit(bitKey);
            if (checkAdjacence(bitToPush, bitToCompare)) {
                if (checkInFront(bitToPush, bitToCompare, localDirectionToPush)) {
                    if (checkEntirelyInFront(bitToPush, bitToCompare, localDirectionToPush)) {
                        bitEntirelyInFrontOfBitToMove.add(bitKey);
                    } else {
                        bitPartiallyInFrontOfBitToMove.add(bitKey);
                    }
                }
            }
        }

        // Treating the group bitEntirelyInFrontOfBitToMove.
        // We just reducing them back.
        // We also add a little space between
        // the bit we are going to move
        // and the ones in front of its
        for (Vector2 bitKey : bitEntirelyInFrontOfBitToMove) {
            this.reduceBit(bitKey, actualState, localDirectionToPush, verticalDisplacement);
        }

        // Treating the group bitPartiallyInFrontOfBitToMove
        // We need to recover the space left behind
        // after reducing them back.
        // We try the simplest way of covering.
        for (Vector2 bitKey : bitPartiallyInFrontOfBitToMove) {
            // Save the initial state of the actual bit
            Bit2D bitToReduce = actualState.getBit(bitKey).clone();
            // Reduce the actual bit
            this.reduceBit(bitKey, actualState, localDirectionToPush, verticalDisplacement);
            // The covering bit will always be a quart of a full one.
            // We define its center instead of origin
            Vector2 coveringCenter,
                    initialOrientation = bitToReduce.getOrientation(),
                    initialCenter = bitToReduce.getCenter();
            Vector2 centrifugalVector = Vector2.Tools.getCentrifugalVector(
                    bitToPush.getCenter(),
                    localDirectionToPush.rotate(initialOrientation), // in Mesh coordinate system
                    bitToReduce.getCenter());
            if (bitToReduce.getLength() == CraftConfig.lengthFull
                    && bitToReduce.getWidth() == CraftConfig.bitWidth)
                // If the initial bit is full
                coveringCenter = initialCenter.add(
                        centrifugalVector.mul(
                                horizontalDisplacement / 2
                                        + additionalHorizontalDisplacement
                        ) // horizontal displacement
                ).sub(
                        localDirectionToPush
                                .rotate(initialOrientation)
                                .mul(verticalDisplacement / 2 + additionalVerticalDisplacement) // vertical displacement
                );
            else
                // The actually considered bit has been modified
                // (not in full form)
                // Add the "petit" covering bit
                // First, we add the clone of the actual bit,
                // which has just been reduced to none
                coveringCenter = initialCenter.add(
                        centrifugalVector.mul(
                                additionalHorizontalDisplacement / 2
                                        + additionalHorizontalDisplacement
                        ) // horizontal displacement
                );
            Bit2D coveringBit = new Bit2D(
                    coveringCenter.x,
                    coveringCenter.y,
                    coveringBitLength,
                    coveringBitWidth,
                    initialOrientation.x,
                    initialOrientation.y
            );
            actualState.addBit(coveringBit);
        }

        return newOrigin;
    }

    /**
     * Cut a bit and push it into the given local direction. Remove bit if
     * <tt>lengthToReduce</tt> is larger than bit's length (if push left or right)
     * or bit's width (if push up or down)
     *
     * @param bitKey                 origin of the bit in coordinate system of this layer
     * @param actualState            the selected pattern
     * @param localDirectionToReduce in the coordinate system of bit. Should be either (0, 1), (0, -1),
     *                               (1, 0), (-1, 0).
     * @param lengthToReduce         in millimeter. If greater than sides, the bit will be removed.
     *                               Half of {@link CraftConfig#lengthFull} or {@link CraftConfig#bitWidth}
     */
    private void reduceBit(Vector2 bitKey,
                           Pavement actualState,
                           Vector2 localDirectionToReduce,
                           double lengthToReduce) {
        Bit2D initialBit = actualState.getBit(bitKey);
        double initialLength = initialBit.getLength(),
                initialWidth = initialBit.getWidth();
        Vector2 initialCenter = initialBit.getCenter(),
                initialRotation = initialBit.getOrientation();
        actualState.removeBit(bitKey); // Remove old one
        if (localDirectionToReduce.x == 0) {
            // Push up or down
            if (initialWidth > lengthToReduce) {
                // Add new replacement
                Vector2 newCenter = initialCenter.add(
                        localDirectionToReduce
                                .rotate(initialRotation)
                                .mul((initialWidth - lengthToReduce) / 2)
                );
                actualState.addBit(new Bit2D(
                        newCenter.x,
                        newCenter.y,
                        initialLength,
                        initialWidth - lengthToReduce,
                        initialRotation.x,
                        initialRotation.y
                ));
            }
        } else {
            // Push left or right
            if (initialLength > lengthToReduce) {
                // Add new replacement
                Vector2 newCenter = initialCenter.add(
                        localDirectionToReduce
                                .rotate(initialRotation)
                                .mul((initialLength - lengthToReduce) / 2)
                );
                actualState.addBit(new Bit2D(
                        newCenter.x,
                        newCenter.y,
                        initialLength - lengthToReduce,
                        initialWidth,
                        initialRotation.x,
                        initialRotation.y
                ));
            }
        }
    }

    /**
     * Check if these 2 bits are adjacent in the reality
     *
     * @param bit1 the first
     * @param bit2 the second
     * @return <tt>true</tt> if they are close enough
     */
    private boolean checkAdjacence(Bit2D bit1, Bit2D bit2) {
        // The 2 bits always have same rotation
        // The orientation is parallel to the length side
        Vector2 x = bit1.getOrientation().normal(),
                y = x.getCWAngularRotated(),
                dist = bit2.getCenter().sub(bit1.getCenter());
        double distX = Math.abs(dist.dot(x)),
                distY = Math.abs(dist.dot(y));
        double length1 = bit1.getLength(),
                width1 = bit1.getWidth(),
                length2 = bit2.getLength(),
                width2 = bit2.getWidth();

        // Firstly, we check if they do not overlap.
        // Even if they have only one common point,
        // (not on the border)
        // we will consider it as overlapped.
        // Horizontally && vertically
        if (distX < (length1 + length2) / 2
                && distY < (width1 + width2) / 2) {
            return false;
        }

        // Secondly, we check if they are not too far
        if (distX < (length1 + length2) / 2 + CraftConfig.lengthFull / 2
                && distY < (width1 + width2) / 2) {
            return true;
        }
        return distY < (width1 + width2) / 2 + CraftConfig.bitWidth / 2
                && distX < (length1 + length2) / 2;
    }

    /**
     * To check if the <tt>bit2</tt>is directly in front of <tt>bit1</tt>.
     * <p>
     * Ensure to use {@link #checkAdjacence(Bit2D, Bit2D) checkAdjacence}
     * before this.
     *
     * @param bit1           the first input bit (reference bit)
     * @param bit2           the second input bit (bit to check)
     * @param localDirection in the local coordinate system of bit1.
     *                       Either (0, 1), (1, 0), (0, -1), (-1, 0)
     * @return <tt>true</tt> if half of sum of 2 bits' sides is greater than
     * distance between center; anh <tt>bit2</tt> faces in <tt>localDirection</tt>
     * of <tt>bit1</tt>.
     */
    private boolean checkInFront(Bit2D bit1, Bit2D bit2, Vector2 localDirection) {
        // By default, these 2 bits have the same orientation
        Vector2 dist = bit2.getCenter().sub(bit1.getCenter());
        Vector2 realDirection = localDirection.rotate(bit1.getOrientation());
        double h1, h2;// Horizontal measures
        if (localDirection.x == 0) {
            // If we check with length sides
            h1 = bit1.getLength();
            h2 = bit2.getLength();
        } else {
            // If we check with width sides
            h1 = bit1.getWidth();
            h2 = bit2.getWidth();
        }
        return (Math.abs(dist.dot(realDirection.getCWAngularRotated())) < (h1 + h2) / 2
                && dist.dot(realDirection) > 0);
    }

    /**
     * Check if the <tt>bit2</tt>'s center is in front of the <tt>bit1</tt>'s side, given
     * the direction.
     * <p>
     * Ensure to use {@link #checkInFront(Bit2D, Bit2D, Vector2) checkInFront}
     * before this.
     *
     * @param bit1           the first input bit (reference bit)
     * @param bit2           the second input bit (bit to check)
     * @param localDirection in the local coordinate system of <tt>bit1</tt>
     * @return <tt>true</tt> if the <tt>bit2</tt>'s center is in front of the <tt>bit1</tt>'s side
     */
    private boolean checkEntirelyInFront(Bit2D bit1, Bit2D bit2, Vector2 localDirection) {
        // Horizontal measures
        double h1;
        Vector2 realDirection = localDirection.rotate(bit1.getOrientation());
        Vector2 dist = bit2.getCenter().sub(bit1.getCenter());
        // Direction must be orthogonal or parallel to orientation of bits.
        if (localDirection.x == 0) {
            // If we check with length sides
            h1 = bit1.getLength();
        } else {
            // If we check with width sides
            h1 = bit1.getWidth();
        }
        return (Math.abs(dist.dot(realDirection.getCWAngularRotated())) <= h1 / 2);
    }

    /**
     * Remove old bit and replace new bit with same height and width
     *
     * @param pavement             current state
     * @param key                  origin of bit to be moved
     * @param localDirectionToMove in bit coordinate system. (1;0), (-1;0), (0;1) or (0;-1)
     * @param distance             in mm
     * @return new origin, after moving
     */
    private Vector2 moveBitWithoutKeepingCutpaths(Pavement pavement,
                                                  Vector2 key,
                                                  Vector2 localDirectionToMove,
                                                  double distance) {
        Bit2D bitToMove = pavement.getBit(key);
        Vector2 translationInMesh =
                localDirectionToMove.rotate(bitToMove.getOrientation())
                        .normal()
                        .mul(distance);
        Vector2 newOrigin = bitToMove.getOrigin().add(translationInMesh);
        pavement.removeBit(key);
        Bit2D newBit = new Bit2D(
                newOrigin,
                bitToMove.getOrientation(),
                bitToMove.getLength(),
                bitToMove.getWidth());
        return pavement.addBit(newBit);
    }

    @Override
    public String getCommonName() {
        return "Improved Brick Pattern";
    }

    @Override
    public String getIconName() {
        return "pattern-improved-brick.png";
    }

    @Override
    public String getDescription() {
        return "A pattern improved from Classic Brick Pattern with much more flexibility.";
    }

    @Override
    public String getHowToUse() {
        return "You can add incremental rotations or displacement for layers.";
    }

    @Override
    public void initiateConfig() {
        // bitsLengthSpace
        config.add(new DoubleParam(
                "bitsLengthSpace",
                "Space between bits' lengths",
                "The gap between two consecutive bits' lengths (in mm)",
                1.0,
                100.0,
                1.0,
                1.0));
        // bitsWidthSpace
        config.add(new DoubleParam(
                "bitsWidthSpace",
                "Space between bits' widths",
                "The gap between two consecutive bits' widths (in mm)",
                1.0,
                100.0,
                5.0,
                1.0));
        // diffRotation
        config.add(new DoubleParam("diffRotation",
                "Differential rotation",
                "Rotation of a layer in comparison to the previous one (in degrees °)",
                0.0,
                360.0,
                90.0,
                0.1));
        // diffxOffset
        config.add(new DoubleParam(
                "diffxOffset",
                "Differential X offset",
                "Offset in the X-axe of a layer in comparison to the previous one (in mm)",
                -1000.0,
                1000.0,
                0.0,
                1.0));
        // diffyOffset
        config.add(new DoubleParam(
                "diffyOffset",
                "Differential Y offset",
                "Offset in the Y-axe of a layer in comparison to the previous one (in mm)",
                -1000.0,
                1000.0,
                0.0,
                1.0));

    }

    @Override
    public boolean ready(Mesh mesh) {
        // Setting the skirtRadius and starting/ending points
        double skirtRadius = mesh.getSkirtRadius();
        double maxiSide = Math.max(CraftConfig.lengthFull, CraftConfig.bitWidth);
        this.patternStart = new Vector2(-skirtRadius - maxiSide, -skirtRadius - maxiSide);
        this.patternEnd = new Vector2(skirtRadius + maxiSide, skirtRadius + maxiSide);
        return true;
    }
}