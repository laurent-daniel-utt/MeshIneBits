package meshIneBits.opcuaHelper;

import java.util.Vector;
import javafx.util.Pair;
import meshIneBits.Bit3D;
import meshIneBits.Mesh;
import meshIneBits.NewBit3D;
import meshIneBits.scheduler.AdvancedScheduler;
import meshIneBits.util.CustomLogger;
import meshIneBits.util.Vector2;

public class CuttingMachineController {

  private final CustomLogger logger = new CustomLogger(this.getClass());
  private final CuttingMachineHelper helper = new CuttingMachineSimulator();
  private final Mesh mesh;

  public CuttingMachineController(Mesh mesh) {
    this.mesh = mesh;
  }

  public void startMachine() {
    ICustomResponse res = helper.startMachine();
    if (res.getCodeStatus() != CustomStatusCode.STATUS_GOOD) {
      logger.logERRORMessage(res.getMessage());
    } else {
      logger.logINFOMessage("Starting...");
    }
  }

  public void stopMachine() {
    ICustomResponse res = helper.stopMachine();
    if (res.getCodeStatus() != CustomStatusCode.STATUS_GOOD) {
      logger.logERRORMessage(res.getMessage());
    } else {
      logger.logINFOMessage("Stoped");
    }
  }

  public NewBit3D getCuttingBit() {
    ICustomResponse res = helper.getCuttingBitId();
    if (res.getCodeStatus() != CustomStatusCode.STATUS_GOOD) {
      logger.logERRORMessage(res.getMessage());
      return null;
    } else {
      int bitId = (int) res.getValue();
      AdvancedScheduler scheduler = (AdvancedScheduler) mesh.getScheduler();
      Vector<Pair<Bit3D, Vector2>> bitSorted = scheduler.getSortedBits();
      if (bitId >= bitSorted.size()) {
        return null;
      }
      return (NewBit3D) bitSorted.get(bitId).getKey();
    }
  }

  public int getCuttingCutPath() {
    return 0;
  }
}
