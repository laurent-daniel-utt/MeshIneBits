package meshIneBits.util;


import meshIneBits.Bit3D;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view2d.MeshController;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * */
public class HandlerRedoUndo {
    private final Stack<ActionOfUser> previousActionOfUserBits;
    private final Stack<ActionOfUser> afterActionOfUserBits;

    public HandlerRedoUndo() {
        this.previousActionOfUserBits = new Stack<ActionOfUser>();
        this.afterActionOfUserBits = new Stack<>();
    }
    private interface ActionOfUser {

        public void runUndo(MeshController meshController);
        public void runRedo(MeshController meshController);
    }
    public interface UndoFunction {
        void undo();
        void redo();
    }

    public Stack<ActionOfUser> getPreviousActionOfUserBits() {
        return previousActionOfUserBits;
    }

    public Stack<ActionOfUser> getAfterActionOfUserBits() {
        return afterActionOfUserBits;
    }
    /**
     * Save actions scaling bits
     * */
    public static class ActionOfUserScaleBit implements ActionOfUser {
        private double setPercentageLength;
        private double setPercentageWidth;
        private Map<Vector2,Double[]> keyMapLengthWidth;


        public ActionOfUserScaleBit(Map<Vector2,Double[]> keyMapLengthWidth,double setPercentageLength, double setPercentageWidth) {
            this.setPercentageLength = setPercentageLength;
            this.setPercentageWidth = setPercentageWidth;
            this.keyMapLengthWidth =keyMapLengthWidth;
        }

        @Override
        public void runUndo(MeshController meshController) {
            meshController.setSelectedBitKeys(this.keyMapLengthWidth.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet()));
            if (meshController.getSelectedBitKeys().isEmpty()) {
                Logger.warning("There is no bit selected");
            } else {
                meshController.setSelectedBitKeys(meshController.getSelectedBits().stream()
                        .map(bit -> meshController.getCurrentLayer().scaleBit(bit,(this.keyMapLengthWidth.get(bit.getOrigin())[0]/ CraftConfig.bitLength)*100, (this.keyMapLengthWidth.get(bit.getOrigin())[1] / CraftConfig.bitWidth)*100))
                        .collect(Collectors.toSet()));
            }
        }

        @Override
        public void runRedo(MeshController meshController) {
            meshController.setSelectedBitKeys(this.keyMapLengthWidth.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet()));
            if (meshController.getSelectedBitKeys().isEmpty()) {
                Logger.warning("There is no bit selected");
            } else {
                meshController.setSelectedBitKeys(meshController.getSelectedBits().stream()
                        .map(bit -> meshController.getCurrentLayer().scaleBit(bit,setPercentageLength, setPercentageWidth))
                        .collect(Collectors.toSet()));
            }
        }
    }
    /**
     * Class that create the action did by user
     * */
    public static class ActionOfUserMoveBit implements ActionOfUser {

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


        public ActionOfUserMoveBit(Set<Bit3D> previousState, Set<Vector2> previousSelectedBits, Set<Vector2> resultKeys, Set<Bit3D> currentSelectedBits, int layerNumber) {
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
            if(this.currentSelectedBits!=null&&this.resultKeys!=null){
                meshController.deleteBitsByBitsAndKeys(this.currentSelectedBits,this.resultKeys);
            }
            // Restore deleted bits
            // By adding the previous state
            if(this.previousState!=null&&this.previousState.size()!=0){
                meshController.addBit3Ds(this.previousState);
                meshController.setSelectedBitKeys(this.previousSelectedBits);
            }
        }

        @Override
        public void runRedo(MeshController meshController) {
            meshController.setLayer(this.layerNum);
            // Remove the latest bits with registered result keys
            meshController.setSelectedBitKeys( this.previousSelectedBits);
            if(this.previousState!=null&&this.previousState.size()!=0){
                meshController.deleteBitsByBitsAndKeys(this.previousState,this.previousSelectedBits);

            }
            // Restore deleted bits
            // By adding the previous state
            if(this.currentSelectedBits!=null){
                meshController.addBit3Ds(this.currentSelectedBits);
                meshController.setSelectedBitKeys(this.resultKeys);
            }
        }
    }
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
        if(previousActionOfUserBits !=null && previousActionOfUserBits.size()!=0) {
            ActionOfUser lastAction = previousActionOfUserBits.pop();
            afterActionOfUserBits.add(lastAction);
            lastAction.runUndo(meshController);
        }
    }
    /**
     * call by MeshController, for redoing the following actions
     * */
    public void redo(MeshController meshController){
        if(afterActionOfUserBits !=null && afterActionOfUserBits.size()!=0) {
            ActionOfUser lastAction = afterActionOfUserBits.pop();
            previousActionOfUserBits.add(lastAction);
            lastAction.runRedo(meshController);
        }
    }



}
