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

import meshIneBits.util.CustomLogger;

import java.util.concurrent.ExecutionException;

public class CuttingMachineOPCUAHelper extends BitSLickrMachineAdapter {

  private static final CustomLogger logger = new CustomLogger(CuttingMachineOPCUAHelper.class);

  public final String startNodeId = "|var|CPX-E-CEC-M1-PN.Application.GVL.START";
  public final String pauseNodeId = "pauseNode";
  public final String cuttingButNodeId = "cuttingNodeId";
  public final String cuttingPathId = "cuttingPathId";

  public CuttingMachineOPCUAHelper()  {
    super();
  }

  public CuttingMachineOPCUAHelper(ROBOT robot) throws Exception {
    super(robot);
  }


  public ICustomResponse startMachine() {
    try {
      return writeVariableNode(startNodeId, "Boolean", true);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }


  public ICustomResponse stopMachine() {
    try {
      return writeVariableNode(startNodeId, "Boolean", false);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }


  public ICustomResponse getMachineState() {
    try {
      return readVariableNode(startNodeId);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }


  public ICustomResponse pauseMachine() {
    try {
      return writeVariableNode(pauseNodeId, "String", "");
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public ICustomResponse getCuttingBitId(){
    try {
      return readVariableNode(cuttingButNodeId);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public ICustomResponse getCuttingPathId(){
    try {
      return readVariableNode(cuttingPathId);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  public String getEndpointUrl() {
    return BitSLicRHelperConfig.cutting_machine_url;
  }
}
