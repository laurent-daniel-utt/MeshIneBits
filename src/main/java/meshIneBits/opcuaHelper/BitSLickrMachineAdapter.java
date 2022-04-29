package meshIneBits.opcuaHelper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class BitSLickrMachineAdapter implements IBitSLickrMachine, IClientHelper {

  ICustomResponse writeVariableNode(
      String nodeId,
      String typeValue,
      Object value) throws ExecutionException, InterruptedException {
    CompletableFuture<ICustomResponse> future = new CompletableFuture<>();
    new ClientRunner(this).runAction(getWriteAction(nodeId, typeValue, value), future);
    return future.get();
  }

  ICustomResponse readVariableNode(String nodeId) throws ExecutionException, InterruptedException {
    CompletableFuture<ICustomResponse> future = new CompletableFuture<>();
    new ClientRunner(this).runAction(getReadAction(nodeId), future);
    return future.get();
  }

  private IWriteNode createVariableNodeWriter() {
    return new BaseWriteNode();
  }

  private IReadNode createVariableNodeReader() {
    return new BaseReadNode();
  }

  private IClientAction<ICustomResponse> getWriteAction(String nodeId, String typeValue,
      Object value) {
    return (client, future1) -> {
      IWriteNode writeNode = createVariableNodeWriter();
      future1.complete(writeNode.writeNode(client, nodeId, typeValue, value));
    };
  }

  private IClientAction<ICustomResponse> getReadAction(String nodeId) {
    return (client, future1) -> {
      IReadNode readNode = createVariableNodeReader();
      future1.complete(readNode.readVariableNode(client, nodeId));
    };
  }
}
