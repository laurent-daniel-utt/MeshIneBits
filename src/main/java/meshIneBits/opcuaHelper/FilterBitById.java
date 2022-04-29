package meshIneBits.opcuaHelper;

import java.util.Vector;
import javafx.util.Pair;
import meshIneBits.Bit3D;
import meshIneBits.Mesh;
import meshIneBits.NewBit3D;
import meshIneBits.scheduler.AdvancedScheduler;
import meshIneBits.util.Vector2;

public class FilterBitById {
  public NewBit3D filterBitById(Mesh mesh, int bitId){
    AdvancedScheduler scheduler = (AdvancedScheduler) mesh.getScheduler();
    Vector<Pair<Bit3D, Vector2>> bitSorted = scheduler.getSortedBits();
    if (bitId >= bitSorted.size()) {
      return null;
    }
    return (NewBit3D) bitSorted.get(bitId).getKey();
  }

}
