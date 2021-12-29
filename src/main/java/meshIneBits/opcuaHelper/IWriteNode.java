package meshIneBits.opcuaHelper;

import java.util.concurrent.ExecutionException;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;

public interface IWriteNode {
  ICustomResponse writeNode(OpcUaClient client,String nodeId, String typeValue, Object value)
      throws Exception;
}
