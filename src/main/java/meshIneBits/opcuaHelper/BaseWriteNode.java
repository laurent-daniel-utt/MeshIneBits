/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2016-2026 DANIEL Laurent.
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
