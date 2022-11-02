package meshIneBits.opcuaHelper;

import meshIneBits.opcuaHelper.BaseCustomResponse.BaseCustomResponseBuilder;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static meshIneBits.opcuaHelper.CustomStatusCode.*;

public class BaseWriteNode implements IWriteNode {


  public BaseWriteNode() {
  }

  @Override
  public ICustomResponse writeNode(OpcUaClient client,
                                   Object nodeIdString,
                                   String typeValue,
                                    Object value)
      throws Exception {
    client.connect().get();
    String machineId = client.getConfig().getEndpoint().getEndpointUrl();
    System.out.println("machineId:"+machineId);
    NodeId nodeId = MeshIneBitNodeId.getMIBNodeIdByID(machineId, nodeIdString);

    Objects.requireNonNull(nodeId, () -> {
      client.disconnect();
      return "NodeId not found in MeshIneBItNodeId class";
    });

    Variant v = new Variant(value);
    DataValue dv = new DataValue(v, null, null);
    CompletableFuture<StatusCode> future = client.writeValue(nodeId, dv);

    StatusCode statusCode = future.get();

    return buildStatusCode(nodeIdString, typeValue, statusCode);
  }

  private ICustomResponse buildStatusCode(
          Object nodeIdString,
          String typeValue,
          StatusCode statusCode) {

    BaseCustomResponseBuilder builder = new BaseCustomResponseBuilder()
        .setMessage(statusCode.toString())
        .setNodeId(nodeIdString)
        .setTypeValue(typeValue)
        .setValue(statusCode.getValue());

    if (statusCode.isBad()) {
      builder.setStatusCode(STATUS_BAD);
    } else if (statusCode.isGood()) {
      builder.setStatusCode(STATUS_GOOD);
    } else if (statusCode.isSecurityError()) {
      builder.setStatusCode(STATUS_SECURITY_ERROR);
    } else if (statusCode.isUncertain()) {
      builder.setStatusCode(STATUS_UNCERTAIN);
    } else {
      builder.setStatusCode(STATUS_UNKNOWN);
    }
    return builder.build();
  }
}
