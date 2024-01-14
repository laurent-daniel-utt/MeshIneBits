/*
 * MeshIneBits is a Java software to disintegrate a 3d project (model in .stl)
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

import meshIneBits.Bit3D;
import meshIneBits.SubBit2D;
import meshIneBits.borderPaver.artificialIntelligence.Acquisition;
import meshIneBits.gui.view2d.ProjectController;
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
    private Set<SubBit2D> previousStateSubs ;


    public ActionOfUserMoveBit(Set<SubBit2D> previousStateSubs, Set<Bit3D> previousState, Set<Vector2> previousSelectedBits, Set<Vector2> resultKeys, Set<Bit3D> currentSelectedBits, int layerNumber) {

        if(previousStateSubs!=null){
            this.previousStateSubs = new HashSet<>(previousStateSubs);
        }
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
        this(null,null,null,resultKeys,currentSelectedBits,layerNum);
    }

    public Collection<Vector2> getResultKeys() {
        return resultKeys;
    }

    public Collection<Bit3D> getPreviousState() {
        return previousState;
    }

    @Override
    public void runUndo(ProjectController projectController) {
        projectController.setLayer(this.layerNum);
        // Remove the latest bits with registered result keys
        if(this.currentSelectedBits!=null&&this.resultKeys!=null) {
            projectController.deleteBitsByBitsAndKeys(this.currentSelectedBits, this.resultKeys);
            Acquisition.RemoveLatestExamples(currentSelectedBits.size()); //deepLearning
        }
        // Restore deleted bits
        // By adding the previous state
        if(this.previousState!=null&&this.previousState.size()!=0){
            Acquisition.RestoreDeletedExamples(previousState); //deepLearning
            projectController.addBit3Ds(this.previousState);
            projectController.setSelectedBitKeys(this.previousSelectedBits);
        }
        if(this.previousStateSubs!=null&&this.previousStateSubs.size()!=0){
            //  Acquisition.RestoreDeletedExamples(previousState); //deepLearning
            projectController.addSubBit3Ds(this.previousStateSubs);
            //projectController.setSelectedSubBit(this.previousStateSubs);
        }


    }

    @Override
    public void runRedo(ProjectController projectController) {
        projectController.setLayer(this.layerNum);
        // Remove the latest bits with registered result keys
        projectController.setSelectedBitKeys(this.previousSelectedBits);
        if (this.previousState != null && this.previousState.size() != 0) {
            projectController.deleteBitsByBitsAndKeys(this.previousState, this.previousSelectedBits);
            Acquisition.RemoveLatestExamples(currentSelectedBits.size()); //deepLearning
        }
        // Restore deleted bits
        // By adding the previous state
        if (this.currentSelectedBits != null) {
            Acquisition.RestoreDeletedExamples(currentSelectedBits); //deepLearning
            projectController.addBit3Ds(this.currentSelectedBits);
            projectController.setSelectedBitKeys(this.resultKeys);
        }

        if(this.previousStateSubs!=null&&this.previousStateSubs.size()!=0){
            //  Acquisition.RestoreDeletedExamples(previousState); //deepLearning
            projectController.deleteSubbits(this.previousStateSubs);
            //projectController.setSelectedBitKeys(this.previousSelectedBits);
        }

    }
}