package meshIneBits.opcuaHelper;

import java.util.concurrent.ExecutionException;
import meshIneBits.util.CustomLogger;

public class CuttingMachineHelper extends BitSLickrMachineAdapter {

  private static final CustomLogger logger = new CustomLogger(CuttingMachineHelper.class);

  public final String startNodeId = "startNode";
  public final String pauseNodeId = "pauseNode";
  public final String cuttingButNodeId = "cuttingNodeId";
  public final String cuttingPathId = "cuttingPathId";

  @Override
  public ICustomResponse startMachine() {
    try {
      return writeVariableNode(startNodeId, "Boolean", true);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  public ICustomResponse stopMachine() {
    try {
      return writeVariableNode(startNodeId, "Boolean", false);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
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
    return BitSLickrHelperConfig.cutting_machine_url;
  }
}
