package meshIneBits.util;


import meshIneBits.Bit3D;
import meshIneBits.gui.view2d.MeshController;

import java.util.*;
/**
 *
 * */
public class HandlerRedoUndo{
    private final Stack<ActionMoveBit> previousActionBits;
    private final Stack<ActionMoveBit> afterActionBits;

    public HandlerRedoUndo() {
        this.previousActionBits = new Stack<>();
        this.afterActionBits = new Stack<>();
    }

    public interface UndoFunction {
        void undo();
        void redo();
    }

//    public void pushAction(ActionMoveBit a){
//        this.actionBits.push(a);
//    }

    public Stack<ActionMoveBit> getPreviousActionBits() {
        return previousActionBits;
    }

    public Stack<ActionMoveBit> getAfterActionBits() {
        return afterActionBits;
    }
    /**
     * Class that create the action did by user
     * */
    public static class ActionMoveBit{

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
        private Set<Vector2> previousSelectedBits =new HashSet<Vector2>();


        public ActionMoveBit(Set<Bit3D> previousState,Set<Vector2> previousSelectedBits, Set<Vector2> resultKeys,Set<Bit3D> currentSelectedBits,int layerNumber) {
            if(previousState!=null){
                this.previousState = new HashSet<Bit3D>(previousState);
            }
            if(previousSelectedBits!=null){
                this.previousSelectedBits=previousSelectedBits;
            }
            if(resultKeys!=null){
                this.resultKeys =resultKeys;
            }
            if(currentSelectedBits!=null){
                System.out.println("OKKK");
                this.currentSelectedBits=new HashSet<Bit3D>(currentSelectedBits);
            }
            this.layerNum=layerNumber;
        }
        public ActionMoveBit(Set<Vector2> resultKeys,Set<Bit3D> currentSelectedBits,int layerNum){
            this(null,null,resultKeys,currentSelectedBits,layerNum);
        }

        public Collection<Vector2> getResultKeys() {
            return resultKeys;
        }

        public Collection<Bit3D> getPreviousState() {
            return previousState;
        }
    }
    //clear afterActionBit when create new action
    public void addActionBit(ActionMoveBit actionMoveBit){
        if(this.afterActionBits.size()!=0){
            this.afterActionBits.clear();
        }
        this.previousActionBits.push(actionMoveBit);
    }
    /**
     * call by MeshController, for redoing the previous actions
     * */
    public void undo(MeshController meshController){
        System.out.println("Before: "+previousActionBits.size());
        if(previousActionBits !=null && previousActionBits.size()!=0){
            HandlerRedoUndo.ActionMoveBit lastActionMoveBit = previousActionBits.pop();
            afterActionBits.add(lastActionMoveBit);
            meshController.setLayer(lastActionMoveBit.layerNum);
            // Remove the latest bits with registered result keys
            if(lastActionMoveBit.currentSelectedBits!=null&&lastActionMoveBit.resultKeys!=null){
                meshController.deleteBitsByBitsAndKeys(lastActionMoveBit.currentSelectedBits,lastActionMoveBit.resultKeys);
            }
            // Restore deleted bits
            // By adding the previous state
            if(lastActionMoveBit.previousState!=null&&lastActionMoveBit.previousState.size()!=0){
                meshController.addBit3Ds(lastActionMoveBit.previousState);
                meshController.setSelectedBitKeys(lastActionMoveBit.previousSelectedBits);
            }
        }
    }
    /**
     * call by MeshController, for redoing the following actions
     * */
    public void redo(MeshController meshController){
        System.out.println("After: "+afterActionBits.size());
        if(afterActionBits !=null && afterActionBits.size()!=0){
            HandlerRedoUndo.ActionMoveBit lastActionMoveBit = afterActionBits.pop();
            previousActionBits.add(lastActionMoveBit);
            meshController.setLayer(lastActionMoveBit.layerNum);
            // Remove the latest bits with registered result keys
            meshController.setSelectedBitKeys( lastActionMoveBit.previousSelectedBits);
            if(lastActionMoveBit.previousState!=null&&lastActionMoveBit.previousState.size()!=0){
                meshController.deleteBitsByBitsAndKeys(lastActionMoveBit.previousState,lastActionMoveBit.previousSelectedBits);

            }
            // Restore deleted bits
            // By adding the previous state
            if(lastActionMoveBit.currentSelectedBits!=null){
                meshController.addBit3Ds(lastActionMoveBit.currentSelectedBits);
                meshController.setSelectedBitKeys(lastActionMoveBit.resultKeys);
            }
        }
    }



}
