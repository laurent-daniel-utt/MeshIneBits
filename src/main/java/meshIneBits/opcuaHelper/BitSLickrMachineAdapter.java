package meshIneBits.opcuaHelper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class BitSLickrMachineAdapter implements IBitSLickrMachine, IClientHelper {

  protected ClientRunner clientRunner;
  private CompletableFuture<ICustomResponse> future;
  public BitSLickrMachineAdapter(){
    clientRunner=new ClientRunner(this);
    //future = new CompletableFuture<>();
  }

  protected ROBOT robot;
  protected String url;

  public BitSLickrMachineAdapter(ROBOT robot){
    this.robot=robot;
    if (this.robot==ROBOT.DECOUPE) {
      this.url= BitSLicRHelperConfig.robot_decoupe_url;
    }
    else {
      this.url= BitSLicRHelperConfig.robot_manip_url;
    }

    clientRunner=new ClientRunner(this);

    //future = new CompletableFuture<>();
  }

  ICustomResponse writeVariableNode(
      Object nodeId,
      String typeValue,
      Object value) throws ExecutionException, InterruptedException {
    future = new CompletableFuture<>();
    //new ClientRunner(this).runAction(getWriteAction(nodeId, typeValue, value), future);
    clientRunner.runAction(getWriteAction(nodeId, typeValue, value), future);
    return future.get();
  }

  ICustomResponse readVariableNode(Object nodeId) throws ExecutionException, InterruptedException {
    //CompletableFuture<ICustomResponse> future = new CompletableFuture<>();
    future = new CompletableFuture<>();
    //new ClientRunner(this).runAction(getReadAction(nodeId), future);
    clientRunner.runAction(getReadAction(nodeId), future);
    return future.get();
  }

  private IWriteNode createVariableNodeWriter() {
    return new BaseWriteNode();
  }

  private IReadNode createVariableNodeReader() {
    return new BaseReadNode();
  }

  private IClientAction<ICustomResponse> getWriteAction(Object nodeId, String typeValue,
      Object value) {
    return (client, future1) -> {
      IWriteNode writeNode = createVariableNodeWriter();
      future1.complete(writeNode.writeNode(client, nodeId, typeValue, value));
    };
  }

  private IClientAction<ICustomResponse> getReadAction(Object nodeId) {
    return (client, future1) -> {
      IReadNode readNode = createVariableNodeReader();
      future1.complete(readNode.readVariableNode(client, nodeId));
    };
  }
}
