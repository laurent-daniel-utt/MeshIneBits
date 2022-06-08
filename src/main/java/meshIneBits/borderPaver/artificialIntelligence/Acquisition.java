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

package meshIneBits.borderPaver.artificialIntelligence;

import meshIneBits.Bit2D;
import meshIneBits.Bit3D;
import meshIneBits.borderPaver.util.*;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.patterntemplates.AI_Pavement;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

/**
 * Let the user save new inputs to store in the dataSet to later train the neural network.
 */
public class Acquisition {
    /**
     * If true, the bits placed manually by the user will be stored.
     */
    private static boolean isStoringNewBits = false;
    private static HashMap<Bit2D, Vector<Vector2>> bit2DVectorHashMap;
    private static Vector<Bit2D> storedExamplesBits;//useful to delete lasts placed bit
    private static MeshController meshController;

    public static void setMeshController(MeshController meshController) {
        Acquisition.meshController = meshController;
    }

    /**
     * The bits placed manually by the user will be stored.
     */
    public static void startStoringBits() {
        isStoringNewBits = true;
        bit2DVectorHashMap = new HashMap<>();
        storedExamplesBits = new Vector<>();
    }

    /**
     * The bits placed manually by the user will not be stored.
     */
    public static void stopStoringBits() throws IOException {
        isStoringNewBits = false;
        saveExamples();
    }

    /**
     * Delete the last manually placed bit by the user from the dataSet.
     */
    public static void deleteLastPlacedBit() {
        storedExamplesBits.lastElement().setUsedForNN(false);
        bit2DVectorHashMap.remove(storedExamplesBits.lastElement());
        storedExamplesBits.remove(storedExamplesBits.lastElement());
    }

    public static void deleteLastPlacedBits(int numberOfBitsToDelete) {
        for (int i = 0; i < numberOfBitsToDelete; i++) {
            deleteLastPlacedBit();
        }
    }

    /**
     * Add a new example bit in the dataSet.
     *
     * @param bit the example to be added.
     */
    public static void addNewExampleBit(@NotNull Bit2D bit, Slice currentSlice) throws Exception {
        if (AI_Pavement.isIrregular(bit, currentSlice)) {
            throw new Exception("Example not added !");
        }

        Section sectionPoints = new GeneralTools().getCurrentLayerBitAssociatedPoints(bit,meshController.getCurrentLayer().getHorizontalSection());
        bit2DVectorHashMap.put(bit,
                               sectionPoints.getPoints());
        storedExamplesBits.add(bit);
        bit.setUsedForNN(true);
    }

    /**
     * Save all examples in a file.
     */
    private static void saveExamples() throws IOException {
        for (Bit2D bit : bit2DVectorHashMap.keySet()) {
            DataLogEntry entry = new DataLogEntry(bit,
                                                  bit2DVectorHashMap.get(bit));
            DataLogger.saveEntry(entry);
        }
        DataSetGenerator.generateCsvFile();
    }

    public static boolean isStoringNewBits() {
        return isStoringNewBits;
    }

    public static Slice getCurrentSlice() {
        return meshController.getCurrentLayer().getHorizontalSection();
    }

    /**
     * Restore the deleted examples in the DataSet of deepLearning
     */
    public static void RestoreDeletedExamples(Set<Bit3D> currentSelectedBits) {
        if (isStoringNewBits()) {//we also have to restore the bit in the dataSet
            for (Bit3D bit3D : currentSelectedBits) { //we have to convert 3D bits to 2D bits
                Bit2D bit2D = new Bit2D(bit3D.getOrigin(), bit3D.getOrientation());
                if (!AI_Pavement.isIrregular(bit2D,
                                             getCurrentSlice())) {
                    bit2D.setUsedForNN(true);
                    bit3D.setUsedForNN(true);
                }
                try {
                    addNewExampleBit(bit2D, getCurrentSlice());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Remove the latest examples in the DataSet of deepLearning
     */
    public static void RemoveLatestExamples(int size) {
        if (isStoringNewBits()) {//we also have to remove the bit in the dataSet
            deleteLastPlacedBits(size);
        }
    }
}
