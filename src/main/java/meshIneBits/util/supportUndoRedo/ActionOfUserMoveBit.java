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

package meshIneBits.util.supportUndoRedo;

import meshIneBits.Bit2D;
import meshIneBits.Bit3D;
import meshIneBits.borderPaver.borderPaver.Acquisition;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.util.Vector2;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ActionOfUserMoveBit implements HandlerRedoUndo.ActionOfUser {

    /**
     * Clone of bits before transformation
     */
    private Set<Bit3D> previousState;
    /**
     * Keys of bits after transformation
     */
    private Set<Vector2> resultKeys;
    private Set<Bit3D> currentSelectedBits;
    private int layerNum;
    private Set<Vector2> previousSelectedBits =new HashSet<>();


    public ActionOfUserMoveBit(Set<Bit3D> previousState, Set<Vector2> previousSelectedBits, Set<Vector2> resultKeys, Set<Bit3D> currentSelectedBits, int layerNumber) {
        if(previousState!=null){
            this.previousState = new HashSet<>(previousState);
        }
        if(previousSelectedBits!=null){
            this.previousSelectedBits=previousSelectedBits;
        }
        if(resultKeys!=null){
            this.resultKeys =resultKeys;
        }
        if(currentSelectedBits!=null){
            this.currentSelectedBits=new HashSet<>(currentSelectedBits);
        }
        this.layerNum=layerNumber;
    }
    public ActionOfUserMoveBit(Set<Vector2> resultKeys, Set<Bit3D> currentSelectedBits, int layerNum){
        this(null,null,resultKeys,currentSelectedBits,layerNum);
    }

    public Collection<Vector2> getResultKeys() {
        return resultKeys;
    }

    public Collection<Bit3D> getPreviousState() {
        return previousState;
    }

    @Override
    public void runUndo(MeshController meshController) {
        meshController.setLayer(this.layerNum);
        // Remove the latest bits with registered result keys
        if(this.currentSelectedBits!=null&&this.resultKeys!=null) {
            meshController.deleteBitsByBitsAndKeys(this.currentSelectedBits, this.resultKeys);
            RemoveLatestExamples(); //deepLearning
        }
        // Restore deleted bits
        // By adding the previous state
        if(this.previousState!=null&&this.previousState.size()!=0){
            RestoreDeletedExamples(previousState); //deepLearning
            meshController.addBit3Ds(this.previousState);
            meshController.setSelectedBitKeys(this.previousSelectedBits);
        }
    }

    @Override
    public void runRedo(MeshController meshController) {
        meshController.setLayer(this.layerNum);
        // Remove the latest bits with registered result keys
        meshController.setSelectedBitKeys(this.previousSelectedBits);
        if (this.previousState != null && this.previousState.size() != 0) {
            meshController.deleteBitsByBitsAndKeys(this.previousState, this.previousSelectedBits);
            RemoveLatestExamples(); //deepLearning
        }
        // Restore deleted bits
        // By adding the previous state
        if (this.currentSelectedBits != null) {
            RestoreDeletedExamples(currentSelectedBits); //deepLearning
            meshController.addBit3Ds(this.currentSelectedBits);
            meshController.setSelectedBitKeys(this.resultKeys);
        }
    }

    /**
     * Remove the latest examples in the DataSet of deepLearning
     */
    private void RemoveLatestExamples() {
        if (Acquisition.isStoringNewBits()) {//we also have to remove the bit in the dataSet
            Acquisition.deleteLastPlacedBits(currentSelectedBits.size());
        }
    }

    /**
     * Restore the deleted examples in the DataSet of deepLearning
     */
    private void RestoreDeletedExamples(Set<Bit3D> currentSelectedBits) {
        if (Acquisition.isStoringNewBits()) {//we also have to restore the bit in the dataSet
            for (Bit3D bit3D : currentSelectedBits) { //we have to convert 3D bits to 2D bits
                Bit2D bit2D = new Bit2D(bit3D.getOrigin(), bit3D.getOrientation());
                if (!Acquisition.isIrregular(bit2D)) {
                    bit2D.setUsedForNN(true);
                    bit3D.setUsedForNN(true);
                }
                try {
                    Acquisition.addNewExampleBit(bit2D);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}