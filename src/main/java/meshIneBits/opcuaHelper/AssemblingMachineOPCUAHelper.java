package meshIneBits.opcuaHelper;

import java.util.concurrent.ExecutionException;

public class AssemblingMachineOPCUAHelper extends BitSLickrMachineAdapter {

  public final String startNodeId = "startNode";
  public final String pauseNodeId = "pauseNode";
  public final String assemblingBitNodeId = "assemblingBitNodeId";
  public final String assemblingSubBitNodeId = "assemblingSubBitNodeId";
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

  public ICustomResponse getAssemblingBit(){
    try {
      return readVariableNode(assemblingBitNodeId);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public ICustomResponse getAssemblingSubBit(){
    try {
      return readVariableNode(assemblingSubBitNodeId);
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

  @Override
  public String getEndpointUrl() {
    return BitSLickrHelperConfig.assembling_machine_url;
  }
}
