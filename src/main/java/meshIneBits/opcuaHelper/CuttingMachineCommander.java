package meshIneBits.opcuaHelper;

import meshIneBits.Mesh;
import meshIneBits.NewBit3D;
import meshIneBits.util.CustomLogger;

public class CuttingMachineCommander {

  private final CustomLogger logger = new CustomLogger(this.getClass());
  private final CuttingMachineOPCUAHelper helper = new CuttingMachineSimulator();
  private final Mesh mesh;

  public CuttingMachineCommander(Mesh mesh) {
    this.mesh = mesh;
  }

  public void startMachine() throws Exception {
    ICustomResponse res = helper.startMachine();
    if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
      logger.logERRORMessage(res.getMessage());
      throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
    } else {
      logger.logINFOMessage("Starting...");
    }
  }

  public void stopMachine() throws Exception {
    ICustomResponse res = helper.stopMachine();
    if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
      logger.logERRORMessage(res.getMessage());
      throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
    } else {
      logger.logINFOMessage("Stopped");
    }
  }

  public NewBit3D getCuttingBit() throws Exception {
    ICustomResponse res = helper.getCuttingBitId();
    if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
      logger.logERRORMessage(res.getMessage());
      throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
    } else {
      NewBit3D bit3D = new FilterBitById().filterBitById(mesh, (int) res.getValue());
      if (bit3D != null) {
        return bit3D;
      } else {
        throw new Exception("Bit not found in Mesh");
      }
    }
  }

  public int getCuttingCutPath() {
    return 0;
  }

  public boolean getMachineState() throws Exception {
    ICustomResponse res = helper.getMachineState();
    if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
      logger.logERRORMessage(res.getMessage());
      throw new Exception(
          "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
              + res.getStatusCode());
    } else {
      logger.logINFOMessage("Starting...");
      if (res.getValue() instanceof Boolean) {
        return (boolean) res.getValue();
      } else {
        throw new Exception(
            "Value returned must be boolean type, Type of obj actual: " + res.getTypeValue());
      }
    }
  }
}
