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

package meshIneBits.artificialIntelligence;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.deepLearning.NNExploitation;
import meshIneBits.artificialIntelligence.util.SectionTransformer;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Vector;

/**
 * An AI_Tool lets the user pave the whole mesh, with artificial intelligence.
 * AI_Tool is based on a neural network that learns from how does a human place bits on the bounds of a Slice.
 */
public class AI_Tool {
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
    private static MeshController meshController;
    public static MeshController getMeshController() {
        return meshController;
    }

    public static void setMeshController(MeshController meshController) {
        AI_Tool.meshController = meshController;
    }

    /**
     * Pave the whole mesh with AI.
     */
    public @NotNull Collection<Bit2D> startNNPavement(@NotNull Slice slice) throws Exception {
        System.out.println("PAVING SLICE " + slice.getAltitude());
        Vector<Bit2D> bits = new Vector<>();

        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);

        NNExploitation nnExploitation = new NNExploitation();

        for (Vector<Vector2> bound : bounds) {
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 startPoint = bound.get(0);

            Vector<Vector2> sectionPoints;
            int nbMaxBits = 0;
            do{
                sectionPoints = SectionTransformer.getSectionPointsFromBound(bound, startPoint);
                double angleLocalSystem = SectionTransformer.getLocalCoordinateSystemAngle(sectionPoints);

                Vector<Vector2> transformedPoints = SectionTransformer.getGlobalSectionInLocalCoordinateSystem(sectionPoints, angleLocalSystem, startPoint);//TODO @Etienne TESTER
                Vector<Vector2> sectionPointsReg = GeneralTools.getInputPointsForDL(transformedPoints);
                Bit2D bit = nnExploitation.getBit(sectionPointsReg, startPoint, angleLocalSystem);
                bits.add(bit);
                startPoint = new GeneralTools().getNextBitStartPoint(bit, bound);
                nbMaxBits++;
            }
            while (new AI_Tool().hasNotCompletedTheBound(veryFirstStartPoint, startPoint, sectionPoints) && nbMaxBits < AI_Tool.paramEarlyStopping.getCurrentValue()); //Add each bit on the bound

        }
        return bits;
    }

    /**
     * Check if the bound of the Slice has been entirely paved.
     *
     * @param veryFirstStartPoint the point of the bound on which the very first bit was placed.
     * @param NextStartPoint TODO etienne
     * @param associatedPoints    the points on which a bit has just been placed.
     * @return <code>true</code> if the bound of the Slice has been entirely paved. <code>false</code> otherwise.
     */
    public boolean hasNotCompletedTheBound(Vector2 veryFirstStartPoint, Vector2 NextStartPoint, @NotNull Vector<Vector2> associatedPoints) {
        DebugTools.pointsToDrawGREEN.add(NextStartPoint);
        DebugTools.pointsToDrawRED.add(veryFirstStartPoint);
        //DebugTools.pointsToDrawBLUE=associatedPoints;
        DebugTools.setPaintForDebug(true);
        if (associatedPoints.firstElement() == veryFirstStartPoint) //to avoid returning false on the first placement
            return true;
        if (Vector2.dist(veryFirstStartPoint, NextStartPoint) < AI_Tool.paramSafeguardSpace.getCurrentValue() * 10
            ) { //standard safe distance between two bits
            //todo refactor le param paramSafeguardSpace @Andre
            return false;
        }

        return !associatedPoints.contains(veryFirstStartPoint);
    }

    public boolean hasCompletedTheBound(Vector2 veryFirstStartPoint, Vector2 nextStartPoint, Bit2D bit2D, Vector<Vector2> bound) {
        return bit2D.getArea().contains(veryFirstStartPoint.x, veryFirstStartPoint.y)
                && GeneralTools.getBitAndContourFirstIntersectionPoint(bit2D, bound) != veryFirstStartPoint
                || GeneralTools.getDistViaSegments(nextStartPoint, veryFirstStartPoint, GeneralTools.pointsToSegments(bound)) < AI_Tool.paramSafeguardSpace.getCurrentValue(); //TODO
    }

}