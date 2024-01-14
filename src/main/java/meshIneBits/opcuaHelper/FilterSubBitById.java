package meshIneBits.opcuaHelper;

import meshIneBits.Project;
import meshIneBits.SubBit2D;
import meshIneBits.scheduler.AdvancedScheduler;

public class FilterSubBitById {

  public SubBit2D filterSubBitById(Project project, int subBit) {
    AdvancedScheduler scheduler = (AdvancedScheduler) project.getScheduler();
    return scheduler.getSubBitByIndex(subBit);
  }
}
