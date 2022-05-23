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
import meshIneBits.borderPaver.BorderedPatternAlgorithm;
import meshIneBits.borderPaver.util.GeneralTools;
import meshIneBits.borderPaver.util.Placement;
import meshIneBits.borderPaver.util.Section;
import meshIneBits.borderPaver.util.SectionTransformer;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Vector2;

import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collection;
import java.util.Vector;

public class BorderedPattern extends PatternTemplate {
    @Override
    protected void initiateConfig() {
        config.add(new DoubleParam(
                "minWidth",
                "Min width to keep",
                "Minimum bit width kept after cut",
                0.0,
                CraftConfig.bitWidth / 2,
                3.0,
                5.0));
        config.add(new DoubleParam(
                "safeguardSpace",
                "Space around bit",
                "In order to keep bits not overlapping or grazing each other",
                1.0, 10.0, 3.0, 0.01));
        config.add(new DoubleParam(
                "numberMaxBits",
                "Bits max number",
                "The maximum number of bits to place",
                0.0,
                Double.POSITIVE_INFINITY,
                50.0,
                1.0));

    }

    @Override
    public boolean ready(Mesh mesh) {
        return true;
    }

    @Override
    public Pavement pave(Layer layer) {
        try {
            double numberMaxBits = (double) config.get("numberMaxBits").getCurrentValue();
            double minWidth = (double) config.get("minWidth").getCurrentValue();
            //TODO @Etienne, loop to find the best config ?
            Collection<Bit2D> bits = getBits(layer.getHorizontalSection(),minWidth,numberMaxBits);
            updateBitAreasWithSpaceAround(bits);
            return new Pavement(bits);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Pavement(new Vector<>());
    }


    /**
     * Compute each bit to place on the border of the Slice using BorderedPattern algorithms
     *
     * @param slice          the slice to pave
     * @param minWidthToKeep minimum distance of wood needed to be kept when placing, in order to avoid the cut bit to be too fragile
     * @param numberMaxBits  the maximum number of bits to place on each border
     * @return the list of bits for this Slice
     */
    public Vector<Bit2D> getBits(Slice slice, double minWidthToKeep, double numberMaxBits) throws NoninvertibleTransformException {
        Vector<Bit2D> bits = new Vector<>();

        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);
        Area areaSlice = AreaTool.getAreaFrom(slice);


        for (Vector<Vector2> bound : bounds) {
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 nextStartPoint = bound.get(0);

            System.out.println("++++++++++++++ BOUND " + bounds.indexOf(bound) + " ++++++++++++++++");

            int iBit = 0;
            Placement placement;
            do {
                System.out.println("\tPLACEMENT BIT " + iBit + "====================");

                Section sectionPoints = SectionTransformer.getSectionFromBound(bound, nextStartPoint);
                placement = BorderedPatternAlgorithm.getBitPlacement(sectionPoints, areaSlice, minWidthToKeep);
                bits.add(placement.bit2D);
                nextStartPoint = placement.end;

                System.out.println("\t FIN PLACEMENT BIT " + iBit + "====================");
                iBit++;

            } while (!((Section.listContainsAsGoodAsEqual(veryFirstStartPoint, placement.sectionCovered.getPoints()) && iBit > 1)
                    || Section.listContainsAllAsGoodAsEqual(bound, placement.sectionCovered.getPoints())) && iBit < numberMaxBits);
        }
        return bits;

    }

    @Override
    public Pavement pave(Layer layer, Area area) {
        System.out.println("Pave layer & area with algorithms... Not implemented yet.");
        return null;
    }

    @Override
    public int optimize(Layer actualState) {
        return -2;
    }

    @Override
    public String getCommonName() {
        return "Bordered Pattern";
    }

    @Override
    public String getIconName() {
        return "pattern-border.png";
    }

    @Override
    public String getDescription() {
        return "Paves the bounds of the slices. Best performance for slices with curves.";
    }

    @Override
    public String getHowToUse() {
        return "";
    }

    /**
     * Cut the bits with the others according to the safeguardSpace
     *
     * @param bits the collection of bits to cut
     */
    //(Modified function for border algorithms, the other doesn't work)
    private void updateBitAreasWithSpaceAround(Collection<Bit2D> bits) {
//        double safeguardSpace = (double) config.get("safeguardSpace").getCurrentValue();
//        for (Bit2D bit2DToCut : bits) {
//            Area bit2DToCutArea = bit2DToCut.getArea();
//            Area nonAvailableArea = new Area();
//            for (Bit2D bit2D : bits) {
//                if (!bit2D.equals(bit2DToCut)) {
//                    Area expand = AreaTool.expand(bit2D.getArea(), safeguardSpace);
//                    nonAvailableArea.add(expand);
//                }
//            }
//            bit2DToCutArea.subtract(nonAvailableArea);
//            bit2DToCut.updateBoundaries(bit2DToCutArea);
//        }
    }
}
