package meshIneBits.opcuaHelper;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;

public interface IWriteNode {
  ICustomResponse writeNode(OpcUaClient client,String nodeId, String typeValue, Object value);
}
