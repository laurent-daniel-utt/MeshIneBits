package meshIneBits.util.supportUndoRedo;

import meshIneBits.Bit3D;
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
            System.out.println("OKKK");
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