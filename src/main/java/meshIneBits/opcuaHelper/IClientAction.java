package meshIneBits.opcuaHelper;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;

import java.util.concurrent.CompletableFuture;

public interface IClientAction<T> {

  void run(OpcUaClient client, CompletableFuture<T> future) throws Exception;
}
