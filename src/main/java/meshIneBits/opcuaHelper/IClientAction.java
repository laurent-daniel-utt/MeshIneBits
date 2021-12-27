package meshIneBits.opcuaHelper;

import java.util.concurrent.CompletableFuture;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;

public interface IClientAction<T> {

  void run(OpcUaClient client, CompletableFuture<T> future) throws Exception;
}
