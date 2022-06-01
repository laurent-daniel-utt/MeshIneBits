package meshIneBits.opcuaHelper;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import java.lang.reflect.Field;
import java.util.Objects;

public class MeshIneBitNodeId {

  @NodeIdentifier(BitSLickrHelperConfig.cutting_machine_url)
  public static final NodeId node1 = new NodeId(2, "node1");
  @NodeIdentifier(BitSLickrHelperConfig.cutting_machine_url)
  public static final NodeId node2 = new NodeId(3, "node2");


// nodes machine de depose
  @NodeIdentifier(BitSLickrHelperConfig.depose_machine_url)
  public static final NodeId deposeStart = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GVL.START");
  @NodeIdentifier(BitSLickrHelperConfig.depose_machine_url)
  public static final NodeId deposeGearMethode = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GVL.gearMethod");
  @NodeIdentifier(BitSLickrHelperConfig.depose_machine_url)
  public static final NodeId deposePosition = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GVL.pos");
  @NodeIdentifier(BitSLickrHelperConfig.depose_machine_url)
  public static final NodeId deposeVitesse = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GVL.vitesse");
  @NodeIdentifier(BitSLickrHelperConfig.depose_machine_url)
  public static final NodeId deposeMoveX1 = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GVL.x1");
  @NodeIdentifier(BitSLickrHelperConfig.depose_machine_url)
  public static final NodeId deposeMoveX2 = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GVL.x2");
  @NodeIdentifier(BitSLickrHelperConfig.depose_machine_url)
  public static final NodeId deposeSynchro = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GVL.synchrox1x2");
  @NodeIdentifier(BitSLickrHelperConfig.depose_machine_url)
  public static final NodeId deposeMove = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GVL.movex1x3");

// nodes robot manip
  @NodeIdentifier(BitSLickrHelperConfig.robot_manip_url)
  public static final NodeId manipDiscreteInput = new NodeId(1,  301);
  @NodeIdentifier(BitSLickrHelperConfig.robot_manip_url)
  public static final NodeId manipCoils = new NodeId(1,302);
  @NodeIdentifier(BitSLickrHelperConfig.robot_manip_url)
  public static final NodeId manipInputRegisters = new NodeId(1,303);
  @NodeIdentifier(BitSLickrHelperConfig.robot_manip_url)
  public static final NodeId manipHoldingRegisters = new NodeId(1,304);
  @NodeIdentifier(BitSLickrHelperConfig.robot_manip_url)
  public static final NodeId manipCommand = new NodeId(1,305);

  // nodes robot decoupe
  @NodeIdentifier(BitSLickrHelperConfig.robot_decoupe_url)
  public static final NodeId decoupeDiscreteInput = new NodeId(1,  301);
  @NodeIdentifier(BitSLickrHelperConfig.robot_decoupe_url)
  public static final NodeId decoupeCoils = new NodeId(1,302);
  @NodeIdentifier(BitSLickrHelperConfig.robot_decoupe_url)
  public static final NodeId decoupeInputRegisters = new NodeId(1,303);
  @NodeIdentifier(BitSLickrHelperConfig.robot_decoupe_url)
  public static final NodeId decoupeHoldingRegisters = new NodeId(1,304);
  @NodeIdentifier(BitSLickrHelperConfig.robot_decoupe_url)
  public static final NodeId decoupeCommand = new NodeId(1,305);



  public static NodeId getMIBNodeIdByID(String machineIdentifier, Object id) {
    for (Field field : MeshIneBitNodeId.class.getFields()) {
      NodeIdentifier a = field.getAnnotation(NodeIdentifier.class);
      if (a != null) {
        try {
          NodeId nodeId = (NodeId) field.get(null);
          if (nodeId.getIdentifier().toString().equals(id.toString()) && machineIdentifier.equals(a.value())) {
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
  }
}
