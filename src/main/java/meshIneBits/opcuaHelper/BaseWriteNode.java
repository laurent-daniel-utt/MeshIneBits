package meshIneBits.opcuaHelper;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;

public class BaseWriteNode implements IWriteNode{


  public BaseWriteNode() {
  }

  @Override
  public ICustomResponse writeNode(OpcUaClient client, String nodeId, String typeValue, Object value) {

    return null;
  }
}
