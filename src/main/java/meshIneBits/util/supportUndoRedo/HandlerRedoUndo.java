/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2021 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020 CLAIRIS Etienne & RUSSO André.
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
 */

package meshIneBits.util.supportUndoRedo;


import meshIneBits.gui.view2d.MeshController;

import java.util.Stack;

/**
 *
 * */
public class HandlerRedoUndo {
    private final Stack<ActionOfUser> previousActionOfUserBits;
    private final Stack<ActionOfUser> afterActionOfUserBits;
    private final UndoRedoListener listener;


    public HandlerRedoUndo(UndoRedoListener undoRedoListener) {
        this.previousActionOfUserBits = new Stack<>();
        this.afterActionOfUserBits = new Stack<>();
        this.listener=undoRedoListener;
    }
    public interface ActionOfUser {

        void runUndo(MeshController meshController);
        void runRedo(MeshController meshController);
    }
    public interface UndoRedoListener {
        void onUndoListener(ActionOfUser a);
        void onRedoListener(ActionOfUser a);
    }

    public Stack<ActionOfUser> getPreviousActionOfUserBits() {
        return previousActionOfUserBits;
    }

    public Stack<ActionOfUser> getAfterActionOfUserBits() {
        return afterActionOfUserBits;
    }

    /**
     * Class that create the action did by user
     * */

    public void addActionBit(ActionOfUser actionMoveBit){
        if(this.afterActionOfUserBits.size()!=0){
            //clear afterActionBit when create new action
            this.afterActionOfUserBits.clear();
        }
        this.previousActionOfUserBits.push(actionMoveBit);
    }
    /**
     * call by MeshController, for redoing the previous actions
     * */
    public void undo(MeshController meshController){
        if(previousActionOfUserBits.size()!=0) {
            ActionOfUser lastAction = previousActionOfUserBits.pop();
            afterActionOfUserBits.add(lastAction);
//            lastAction.runUndo(meshController);
            listener.onUndoListener(lastAction);
        }
    }
    /**
     * call by MeshController, for redoing the following actions
     * */
    public void redo(MeshController meshController){
        if(afterActionOfUserBits.size() != 0) {
            ActionOfUser lastAction = afterActionOfUserBits.pop();
            previousActionOfUserBits.add(lastAction);
//            lastAction.runRedo(meshController);
            listener.onRedoListener(lastAction);
        }
    }
    public void reset(){
        afterActionOfUserBits.clear();
        previousActionOfUserBits.clear();
    }



}
