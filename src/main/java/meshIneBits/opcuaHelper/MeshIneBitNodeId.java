package meshIneBits.opcuaHelper;

import java.lang.reflect.Field;
import java.util.Objects;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class MeshIneBitNodeId {

  @NodeIdentifier(BitSLickrHelperConfig.cutting_machine_url)
  public static final NodeId node1 = new NodeId(2, "node1");
  @NodeIdentifier(BitSLickrHelperConfig.cutting_machine_url)
  public static final NodeId node2 = new NodeId(3, "node2");


  public static NodeId getMIBNodeIdByID(String machineIdentifier, String id) {
    for (Field field : MeshIneBitNodeId.class.getFields()) {
      NodeIdentifier a = field.getAnnotation(NodeIdentifier.class);
      if (a != null) {
        try {
          NodeId nodeId = (NodeId) field.get(null);
          if (nodeId.getIdentifier().equals(id) && machineIdentifier.equals(a.value())) {
            return nodeId;
          }
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  public static void main(String[] args) {
    System.out.println("------" + Objects.requireNonNull(
            MeshIneBitNodeId.getMIBNodeIdByID(BitSLickrHelperConfig.cutting_machine_url, "node2"))
        .getNamespaceIndex());
    ;
  }
}
