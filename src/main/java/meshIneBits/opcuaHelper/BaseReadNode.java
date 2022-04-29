package meshIneBits.opcuaHelper;

import java.util.Objects;
import meshIneBits.opcuaHelper.BaseCustomResponse.BaseCustomResponseBuilder;
import meshIneBits.util.CustomLogger;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class BaseReadNode implements IReadNode {

  private static final CustomLogger logger = new CustomLogger(BaseReadNode.class);

  @Override
  public ICustomResponse readVariableNode(OpcUaClient client, String nodeIdString) throws Exception {
    client.connect().get();
    String machineId = client.getConfig().getEndpoint().getEndpointUrl();
    NodeId nodeId = MeshIneBitNodeId.getMIBNodeIdByID(machineId, nodeIdString);

    Objects.requireNonNull(nodeId, () -> {
      client.disconnect();
      return "NodeId not found in MeshIneBitNodeId class";
    });

    UaVariableNode node = client.getAddressSpace().getVariableNode(nodeId);
    DataValue value = node.readValue();

    BaseCustomResponseBuilder responseBuilder = new BaseCustomResponseBuilder();
    assert value.getStatusCode() != null;
    BaseCustomResponse response = responseBuilder.setNodeId(nodeIdString)
        .setMessage(null)
        .setStatusCode(CustomStatusCode.instance.convertStatusCode(value.getStatusCode()))
        .setTypeValue(value.getValue().getValue().getClass().getName())
        .setValue(value.getValue().getValue()).build();

    client.disconnect();
    return response;
  }
}
