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
import meshIneBits.borderPaver.artificialIntelligence.Acquisition;
import meshIneBits.borderPaver.artificialIntelligence.NNExploitation;
import meshIneBits.borderPaver.util.GeneralTools;
import meshIneBits.borderPaver.util.Section;
import meshIneBits.borderPaver.util.SectionTransformer;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Area;
import java.util.Collection;
import java.util.Vector;

public class AI_Pavement extends PatternTemplate {
    public static final DoubleParam paramPosCorrection = new DoubleParam(
            "posCorrection",
            "Position correction",
            "",
            -10.0, 10.0, 5.0, 0.1);
    public static final DoubleParam paramSafeguardSpace = new DoubleParam(
            "safeguardSpace",
            "Space around bit",
            "In order to keep bits not overlapping or grazing each other",
            1.0, 10.0, 3.0, 0.01);
    public static final DoubleParam paramEarlyStopping = new DoubleParam(
            "earlyStopping",
            "Maximum number of bits before stopping",
            "Set a max number of bits to avoid infinite loop",
            0.0,
            Double.POSITIVE_INFINITY,
            50.0,
            10.0);


    /**
     * The name and location of the trained model saved.
     */
    public static final String MODEL_PATH = "src/main/java/meshIneBits/artificialIntelligence/deepLearning/resources/trained_model.zip";
    /**
     * The name and path of the normalizer saved.
     */
    public static final String NORMALIZER_PATH = "src/main/java/meshIneBits/artificialIntelligence/deepLearning/resources/normalizer_saved.bin";
    /**
     * The name and location of the csv file which contains the raw data for the DataSet.
     */
    public final static String DATA_LOG_FILE_PATH = "src/main/java/meshIneBits/artificialIntelligence/deepLearning/resources/storedBits.csv";
    /**
     * The name and location of the csv file which contains the dataSet.
     */
    public static final String DATASET_FILE_PATH = "src/main/java/meshIneBits/artificialIntelligence/deepLearning/resources/dataSet.csv";


    /**
     * Correct the position of the bits placed by the Neural Network.
     * May be useless when the NN will be correctly trained.
     */

    @Override
    protected void initiateConfig() {
        config.add(paramSafeguardSpace);
        config.add(paramPosCorrection);
        config.add(paramEarlyStopping);
    }

    @Override
    public boolean ready(Mesh mesh) {
        return true;
    }

    @Override
    public Pavement pave(Layer layer) {
        try {
            Collection<Bit2D> bits = startNNPavement(layer.getHorizontalSection());
            updateBitAreasWithSpaceAround(bits);
            return new Pavement(bits);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Pavement(new Vector<>());
    }

    @Override
    public Pavement pave(Layer layer, Area area) {
        System.out.println("Pave layer & area with AI... Not implemented yet.");
        return null;
    }

    @Override
    public int optimize(Layer actualState) {
        // TODO: 2021-01-17 implement optimization for last bit placement as in GeneticPavement.
        return -2;
    }

    @Override
    public String getCommonName() {
        return "AI pavement";
    }

    @Override
    public String getIconName() {
        return "pattern-border.png";
    }

    @Override
    public String getDescription() {
        return "Paves the bounds of the slices with a neural network";
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
    //(Modified function as for border algorithms, the other doesn't work)
    private void updateBitAreasWithSpaceAround(Collection<Bit2D> bits) {
        double safeguardSpace = (double) config.get("safeguardSpace").getCurrentValue();
        for (Bit2D bit2DToCut : bits) {
            Area bit2DToCutArea = bit2DToCut.getAreaCS();
            Area nonAvailableArea = new Area();
            for (Bit2D bit2D : bits) {
                if (!bit2D.equals(bit2DToCut)) {
                    Area expand = AreaTool.expand(bit2D.getAreaCS(), safeguardSpace);
                    nonAvailableArea.add(expand);
                }
            }
            bit2DToCutArea.subtract(nonAvailableArea);
            bit2DToCut.updateBoundaries(bit2DToCutArea);
        }
    }


    /**
     * Pave the whole mesh with AI.
     */
    public @NotNull Collection<Bit2D> startNNPavement(@NotNull Slice slice) throws Exception {
        System.out.println("PAVING SLICE " + slice.getAltitude());
        Vector<Bit2D> bits = new Vector<>();

        Vector<Vector<Vector2>> bounds = GeneralTools.getBoundsAndRearrange(slice);

        NNExploitation nnExploitation = new NNExploitation();

        for (Vector<Vector2> bound : bounds) {
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 startPoint = bound.get(0);

            Section sectionPoints;
            int nbMaxBits = 0;
            do {
                sectionPoints = SectionTransformer.getSectionFromBound(bound,
                                                                       startPoint);
                double angleLocalSystem = SectionTransformer.getLocalCoordinateSystemAngle(sectionPoints);

                Vector<Vector2> transformedPoints = SectionTransformer.getGlobalSectionInLocalCoordinateSystem(sectionPoints,
                                                                                                               angleLocalSystem);
                Vector<Vector2> sectionPointsReg = SectionTransformer.repopulateWithNewPoints(Acquisition.nbPointsSectionDL,
                                                                                              new Section(transformedPoints),
                                                                                              false);
                Bit2D bit = nnExploitation.getBit(sectionPointsReg,
                                                  startPoint,
                                                  angleLocalSystem);
                bits.add(bit);
                startPoint = new GeneralTools().getNextBitStartPoint(bit,
                                                                     bound);
                nbMaxBits++;
            }
            while (hasNotCompletedTheBound(veryFirstStartPoint, startPoint, sectionPoints.getPoints()) && nbMaxBits < paramEarlyStopping.getCurrentValue()); //Add each bit on the bound

        }
        return bits;
    }

    /**
     * Check if the bound of the Slice has been entirely paved.
     *
     * @param veryFirstStartPoint the point of the bound on which the very first bit was placed.
     * @param _nextStartPoint    the point of the bound on which the next bit was placed.
     * @param associatedPoints    the points on which a bit has just been placed.
     * @return <code>true</code> if the bound of the Slice has been entirely paved. <code>false</code> otherwise.
     */
    public boolean hasNotCompletedTheBound(Vector2 veryFirstStartPoint, Vector2 _nextStartPoint, @NotNull Vector<Vector2> associatedPoints) {
        if (associatedPoints.firstElement() == veryFirstStartPoint) //to avoid returning false on the first placement
            return true;
        if (Vector2.dist(veryFirstStartPoint, _nextStartPoint) < paramSafeguardSpace.getCurrentValue() * 10) {
            //standard safe distance between two bits
            return false;
        }

        return !associatedPoints.contains(veryFirstStartPoint);
    }


    /**
     * checks if a bit can be used to train the neural net.
     * A bit can be used by the neural net only if the first intersection between an edge of the bit and the bound
     * (scanning it in the direction of the increasing indices) is made by a short edge of the bit.
     *
     * @param bit   a {@link Bit2D}
     * @param slice a {@link Slice}
     * @return true if the bit can not be used by the neural net.
     */
    public static boolean isIrregular(@NotNull Bit2D bit, @NotNull Slice slice) {
        Vector<Segment2D> bitEdges = bit.getBitSidesSegments();

        Vector<Vector<Vector2>> bounds = GeneralTools.getBoundsAndRearrange(slice);

        for (Vector<Vector2> bound : bounds) {
            for (int i = 0; i < bound.size() - 1; i++) {
                Segment2D boundSegment = new Segment2D(bound.get(i), bound.get(i + 1));

                Segment2D firstIntersectingEdge = null;
                double maxDistance = Double.POSITIVE_INFINITY;

                for (Segment2D bitEdge : bitEdges) {

                    // finds the edge of the bit that intersects the first
                    if (Segment2D.doSegmentsIntersect(bitEdge, boundSegment)
                            && Vector2.dist(bound.get(i), Segment2D.getIntersectionPoint(bitEdge, boundSegment)) < maxDistance) {
                        maxDistance = Vector2.dist(bound.get(i), Segment2D.getIntersectionPoint(bitEdge, boundSegment));
                        firstIntersectingEdge = bitEdge;
                    }

                    // check if the position of the bit is irregular
                    if (firstIntersectingEdge != null) {
                        if (Math.abs(firstIntersectingEdge.getLength() - CraftConfig.bitWidth) < Math.pow(10, -CraftConfig.errorAccepted))
                            return false; // the first intersection is a short edge of the bit
                    }
                }
            }
        }
        // only reached if the bit doesn't intersect with a bound
        return true;
    }
}
