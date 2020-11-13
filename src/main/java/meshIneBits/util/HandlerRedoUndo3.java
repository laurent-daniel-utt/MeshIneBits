//package meshIneBits.util;
//
//
//import meshIneBits.Bit3D;
//import meshIneBits.gui.view2d.MeshController;
//
//import java.util.*;
//
///**
// *
// * */
//public class HandlerRedoUndo3 {
//    public static final int ACTION_SCALE_BIT = 1;
//    public static final int ACTION_MOVE_BIT = 2;
//
//    private final Stack<ActionOfUser> previousActionOfUserBits;
//    private final Stack<ActionOfUser> afterActionOfUserBits;
//
//    private final OnEventUndoRedoListener mListener;
//
//    public HandlerRedoUndo3(OnEventUndoRedoListener listener) {
//        this.previousActionOfUserBits = new Stack<ActionOfUser>();
//        this.afterActionOfUserBits = new Stack<>();
//        this.mListener=listener;
//    }
//    public interface OnEventUndoRedoListener {
//        void runUndo(ActionOfUser action);
//        void runRedo(ActionOfUser action);
//    }
//    public static abstract class ActionOfUser {
//        private final int type = getTypeOfAction();
//
//        public abstract int getTypeOfAction();
//        public int getType(){
//            return type;
//        }
//
//    }
//
//    public Stack<ActionOfUser> getPreviousActionOfUserBits() {
//        return previousActionOfUserBits;
//    }
//
//    public Stack<ActionOfUser> getAfterActionOfUserBits() {
//        return afterActionOfUserBits;
//    }
//    /**
//    * Save actions scaling bits
//    * */
//    public static class ActionOfUserScaleBit extends ActionOfUser {
//        @Override
//        public int getTypeOfAction() {
//            return ACTION_SCALE_BIT;
//        }
//
//        private double setPercentageLength;
//        private double setPercentageWidth;
//        private Map<Vector2,Double[]> keyMapLengthWidth;
//
//
//        public ActionOfUserScaleBit(Map<Vector2,Double[]> keyMapLengthWidth,double setPercentageLength, double setPercentageWidth) {
//            this.setPercentageLength = setPercentageLength;
//            this.setPercentageWidth = setPercentageWidth;
//            this.keyMapLengthWidth =keyMapLengthWidth;
//        }
//
//    }
//    /**
//     * Class that create the action did by user
//     * */
//    public static class ActionOfUserMoveBit extends ActionOfUser {
//        @Override
//        public int getTypeOfAction() {
//            return ACTION_MOVE_BIT;
//        }
//
//        /**
//         * Clone of bits before transformation
//         */
//        private Set<Bit3D> previousState;
//        /**
//         * Keys of bits after transformation
//         */
//        private Set<Vector2> resultKeys;
//        private Set<Bit3D> currentSelectedBits;
//        private int layerNum;
//        private Set<Vector2> previousSelectedBits =new HashSet<Vector2>();
//
//
//        public ActionOfUserMoveBit(Set<Bit3D> previousState, Set<Vector2> previousSelectedBits, Set<Vector2> resultKeys, Set<Bit3D> currentSelectedBits, int layerNumber) {
//            if(previousState!=null){
//                this.previousState = new HashSet<Bit3D>(previousState);
//            }
//            if(previousSelectedBits!=null){
//                this.previousSelectedBits=previousSelectedBits;
//            }
//            if(resultKeys!=null){
//                this.resultKeys =resultKeys;
//            }
//            if(currentSelectedBits!=null){
//                System.out.println("OKKK");
//                this.currentSelectedBits=new HashSet<Bit3D>(currentSelectedBits);
//            }
//            this.layerNum=layerNumber;
//        }
//        public ActionOfUserMoveBit(Set<Vector2> resultKeys, Set<Bit3D> currentSelectedBits, int layerNum){
//            this(null,null,resultKeys,currentSelectedBits,layerNum);
//        }
//    }
//
//    public void addActionBit(ActionOfUser actionMoveBit){
//        if(this.afterActionOfUserBits.size()!=0){
//            //clear afterActionBit when create new action
//            this.afterActionOfUserBits.clear();
//        }
//        this.previousActionOfUserBits.push(actionMoveBit);
//    }
//    /**
//     * call by MeshController, for redoing the previous actions
//     * */
//    public void undo(MeshController meshController){
//        if(previousActionOfUserBits !=null && previousActionOfUserBits.size()!=0) {
//            ActionOfUser action = previousActionOfUserBits.pop();
//            afterActionOfUserBits.add(action);
//            mListener.runUndo(action);
//        }
//    }
//    /**
//     * call by MeshController, for redoing the following actions
//     * */
//    public void redo(MeshController meshController){
//        if(afterActionOfUserBits !=null && afterActionOfUserBits.size()!=0) {
//            ActionOfUser action = afterActionOfUserBits.pop();
//            previousActionOfUserBits.add(action);
//           mListener.runRedo(action);
//        }
//    }
//
//
//
//}
