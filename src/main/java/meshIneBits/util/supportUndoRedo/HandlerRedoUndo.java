package meshIneBits.util.supportUndoRedo;


import meshIneBits.Bit3D;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;

import java.util.*;
import java.util.stream.Collectors;

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



}
