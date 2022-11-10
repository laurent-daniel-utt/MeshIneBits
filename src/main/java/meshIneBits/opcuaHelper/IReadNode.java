package meshIneBits.opcuaHelper;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;

public interface IReadNode {
  ICustomResponse readVariableNode(OpcUaClient client, Object nodeId) throws Exception;

}
