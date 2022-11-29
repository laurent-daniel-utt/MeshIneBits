package meshIneBits.opcuaHelper;

import meshIneBits.util.CustomLogger;

import java.util.concurrent.ExecutionException;

public class CuttingMachineOPCUAHelper extends BitSLickrMachineAdapter {

  private static final CustomLogger logger = new CustomLogger(CuttingMachineOPCUAHelper.class);

  public final String startNodeId = "|var|CPX-E-CEC-M1-PN.Application.GVL.START";
  public final String pauseNodeId = "pauseNode";
  public final String cuttingButNodeId = "cuttingNodeId";
  public final String cuttingPathId = "cuttingPathId";

  public CuttingMachineOPCUAHelper()  {
    super();
  }

  public CuttingMachineOPCUAHelper(ROBOT robot) throws Exception {
    super(robot);
  }


  public ICustomResponse startMachine() {
    try {
      return writeVariableNode(startNodeId, "Boolean", true);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }


  public ICustomResponse stopMachine() {
    try {
      return writeVariableNode(startNodeId, "Boolean", false);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }


  public ICustomResponse getMachineState() {
    try {
      return readVariableNode(startNodeId);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }


  public ICustomResponse pauseMachine() {
    try {
      return writeVariableNode(pauseNodeId, "String", "");
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public ICustomResponse getCuttingBitId(){
    try {
      return readVariableNode(cuttingButNodeId);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public ICustomResponse getCuttingPathId(){
    try {
      return readVariableNode(cuttingPathId);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  public String getEndpointUrl() {
    return BitSLicRHelperConfig.cutting_machine_url;
  }
}
