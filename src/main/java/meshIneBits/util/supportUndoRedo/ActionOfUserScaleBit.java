package meshIneBits.util.supportUndoRedo;

import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Save actions scaling bits
 * */
public class ActionOfUserScaleBit implements HandlerRedoUndo.ActionOfUser {
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
        meshController.setSelectedBitKeys(new HashSet<>(this.keyMapLengthWidth.keySet()));
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
        meshController.setSelectedBitKeys(new HashSet<>(this.keyMapLengthWidth.keySet()));
        if (meshController.getSelectedBitKeys().isEmpty()) {
            Logger.warning("There is no bit selected");
        } else {
            meshController.setSelectedBitKeys(meshController.getSelectedBits().stream()
                    .map(bit -> meshController.getCurrentLayer().scaleBit(bit,setPercentageLength, setPercentageWidth))
                    .collect(Collectors.toSet()));
        }
    }
}
