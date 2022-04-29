package meshIneBits.opcuaHelper;

import meshIneBits.Mesh;
import meshIneBits.SubBit2D;
import meshIneBits.scheduler.AdvancedScheduler;

public class FilterSubBitById {

  public SubBit2D filterSubBitById(Mesh mesh, int subBit) {
    AdvancedScheduler scheduler = (AdvancedScheduler) mesh.getScheduler();
    return scheduler.getSubBitByIndex(subBit);
  }
}
