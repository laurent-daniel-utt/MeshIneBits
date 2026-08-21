/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2015-2026 DANIEL Laurent.
 -  Copyright (C) 2015 Cédric Siourakhan
 -  Copyright (C) 2016 Gabriel Magny
 -  Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 -  Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 -  Copyright (C) 2018 VALLON Benjamin.
 -  Copyright (C) 2018 LORIMER Campbell.
 -  Copyright (C) 2018 D'AUTUME Christian.
 -  Copyright (C) 2019 DURINGER Nathan (Tests).
 -  Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
 -  Copyright (C) 2020-2021 DO Quang Bao.
 -  Copyright (C) 2021 VANNIYASINGAM Mithulan.
 -  Copyright (C) 2021  Quang Long
 -  Copyright (C) 2022 Mhamad Atlab
 -  Copyright (C) 2022 Majed Hlaihel
 -  Copyright (C) 2026 Aymeric Sirejol
 -  Copyright (C) 2026 Hugo BENOIT
 -
 -  This program is free software: you can redistribute it and/or modify
 -  it under the terms of the GNU General Public License as published by
 -  the Free Software Foundation, either version 3 of the License, or
 -  (at your option) any later version.
 -
 -  This program is distributed in the hope that it will be useful,
 -  but WITHOUT ANY WARRANTY; without even the implied warranty of
 -  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 -  GNU General Public License for more details.
 -
 -  You should have received a copy of the GNU General Public License
 -  along with this program. If not, see <https://www.gnu.org/licenses/>.
 -----------------------------------------------------------------------------*/

package meshIneBits.opcuaHelper;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import java.lang.reflect.Field;
import java.util.Objects;

public class MeshIneBitNodeId {

  @NodeIdentifier(BitSLicRHelperConfig.cutting_machine_url)
  public static final NodeId node1 = new NodeId(2, "node1");
  @NodeIdentifier(BitSLicRHelperConfig.cutting_machine_url)
  public static final NodeId node2 = new NodeId(3, "node2");


// nodes machine de depose
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId start_depose = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.start_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId stop_depose = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.stop_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId reset_depose = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.reset_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId continue_depose = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.continue_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId pause_depose = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.pause_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId continue_after_turn_off = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.continue_after_turn_off_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId continue_after_E_STOP = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.continue_after_E_STOP_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId acknowledge_error = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.acknowledge_error_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId message_error = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GDV.message_error");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId lock_continue_button = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.lock_continue_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId x_current_position = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.Synchronization_X.T1_Position");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId z_current_position = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.Synchronization_Z.T3_Position");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId y_current_position = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.Control_Y.T5_Position");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId subx_current_position = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.Control_SubX.T6_Position");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId subz_current_position = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.Control_SubZ.T7_Position");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId theta_current_position = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.Control_Theta.T8_Position");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId current_bit_id = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.id");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId current_bit_id_in_batch = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.id_in_batch");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId current_bit_x = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.x");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId current_bit_z = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.z");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId current_bit_y = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.y");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId current_bit_subx = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.subx");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId current_bit_rotation = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.rotation");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId current_bit_refline_vu = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.refline_vu");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId current_bit_refline_rot = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.refline_rot");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId current_bit_theta = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.theta");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId camera_login = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.camera_login_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId camera_capture_image = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.camera_capture_image_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId reset_power = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.reset_power_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId take_batch = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.take_batch_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId depose_batch = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.depose_batch_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId read_xml_file = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.read_xml_file_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId rename_xml_file = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.rename_xml_file_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId synchro_axes_x = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.synchro_X_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId synchro_axes_z = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.synchro_Z_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId homing_axis_subx = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.homing_subx_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId homing_axis_subz = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.homing_subz_button");
  @NodeIdentifier(BitSLicRHelperConfig.depose_machine_url)
  public static final NodeId homing_axis_theta = new NodeId(4, "|var|CPX-E-CEC-M1-PN.Application.visu.homing_theta_button");


// nodes robot manip
  @NodeIdentifier(BitSLicRHelperConfig.robot_manip_url)
  public static final NodeId manipDiscreteInput = new NodeId(1,  301);
  @NodeIdentifier(BitSLicRHelperConfig.robot_manip_url)
  public static final NodeId manipCoils = new NodeId(1,302);
  @NodeIdentifier(BitSLicRHelperConfig.robot_manip_url)
  public static final NodeId manipInputRegisters = new NodeId(1,303);
  @NodeIdentifier(BitSLicRHelperConfig.robot_manip_url)
  public static final NodeId manipHoldingRegisters = new NodeId(1,304);
  @NodeIdentifier(BitSLicRHelperConfig.robot_manip_url)
  public static final NodeId manipCommand = new NodeId(1,305);

  // nodes robot decoupe
  @NodeIdentifier(BitSLicRHelperConfig.robot_decoupe_url)
  public static final NodeId decoupeDiscreteInput = new NodeId(1,  301);
  @NodeIdentifier(BitSLicRHelperConfig.robot_decoupe_url)
  public static final NodeId decoupeCoils = new NodeId(1,302);
  @NodeIdentifier(BitSLicRHelperConfig.robot_decoupe_url)
  public static final NodeId decoupeInputRegisters = new NodeId(1,303);
  @NodeIdentifier(BitSLicRHelperConfig.robot_decoupe_url)
  public static final NodeId decoupeHoldingRegisters = new NodeId(1,304);
  @NodeIdentifier(BitSLicRHelperConfig.robot_decoupe_url)
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
            MeshIneBitNodeId.getMIBNodeIdByID(BitSLicRHelperConfig.cutting_machine_url, "node2"))
        .getNamespaceIndex());
  }
}
