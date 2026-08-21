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

import meshIneBits.Mesh;
import meshIneBits.NewBit3D;
import meshIneBits.util.CustomLogger;

public class CuttingMachineCommander {

  private final CustomLogger logger = new CustomLogger(this.getClass());
  private final CuttingMachineOPCUAHelper helper;

  {
    try {
      helper = new CuttingMachineSimulator();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private final Mesh mesh;

  public CuttingMachineCommander(Mesh mesh) {
    this.mesh = mesh;
  }

  public void startMachine() throws Exception {
    ICustomResponse res = helper.startMachine();
    if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
      logger.logERRORMessage(res.getMessage());
      throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
    } else {
      logger.logINFOMessage("Starting...");
    }
  }

  public void stopMachine() throws Exception {
    ICustomResponse res = helper.stopMachine();
    if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
      logger.logERRORMessage(res.getMessage());
      throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
    } else {
      logger.logINFOMessage("Stopped");
    }
  }

  public NewBit3D getCuttingBit() throws Exception {
    ICustomResponse res = helper.getCuttingBitId();
    if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
      logger.logERRORMessage(res.getMessage());
      throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
    } else {
      NewBit3D bit3D = new FilterBitById().filterBitById(mesh, (int) res.getValue());
      if (bit3D != null) {
        return bit3D;
      } else {
        throw new Exception("Bit not found in Mesh");
      }
    }
  }

  public int getCuttingCutPath() {
    return 0;
  }

  public boolean getMachineState() throws Exception {
    ICustomResponse res = helper.getMachineState();
    if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
      logger.logERRORMessage(res.getMessage());
      throw new Exception(
          "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
              + res.getStatusCode());
    } else {
      logger.logINFOMessage("Starting...");
      if (res.getValue() instanceof Boolean) {
        return (boolean) res.getValue();
      } else {
        throw new Exception(
            "Value returned must be boolean type, Type of obj actual: " + res.getTypeValue());
      }
    }
  }
}
