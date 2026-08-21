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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class BitSLickrMachineAdapter implements IClientHelper {

  protected ClientRunner clientRunner;
  private CompletableFuture<ICustomResponse> future;

  public BitSLickrMachineAdapter()  {
     clientRunner=new ClientRunner(this);

  }

  protected ROBOT robot;
  protected String url;

  public BitSLickrMachineAdapter(ROBOT robot)  {
    this.robot=robot;
    if (this.robot==ROBOT.DECOUPE) {
      this.url= BitSLicRHelperConfig.robot_decoupe_url;
    }
    else {
      this.url= BitSLicRHelperConfig.robot_manip_url;
    }
    clientRunner=new ClientRunner(this);
  }

  ICustomResponse writeVariableNode(
      Object nodeId,
      String typeValue,
      Object value) throws ExecutionException, InterruptedException {
    future = new CompletableFuture<>();
    clientRunner.runAction(getWriteAction(nodeId, typeValue, value), future);
    return future.get();
  }

  ICustomResponse readVariableNode(Object nodeId) throws ExecutionException, InterruptedException {
    future = new CompletableFuture<>();
    clientRunner.runAction(getReadAction(nodeId), future);
    return future.get();
  }

  private IWriteNode createVariableNodeWriter() {
    return new BaseWriteNode();
  }

  private IReadNode createVariableNodeReader() {
    return new BaseReadNode();
  }

  private IClientAction<ICustomResponse> getWriteAction(Object nodeId, String typeValue,
      Object value) {
    return (client, future1) -> {
      IWriteNode writeNode = createVariableNodeWriter();
      future1.complete(writeNode.writeNode(client, nodeId, typeValue, value));
    };
  }

  private IClientAction<ICustomResponse> getReadAction(Object nodeId) {
    return (client, future1) -> {
      IReadNode readNode = createVariableNodeReader();
      future1.complete(readNode.readVariableNode(client, nodeId));
    };
  }
}
