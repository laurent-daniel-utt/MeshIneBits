

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

import meshIneBits.util.CustomLogger;

import java.util.concurrent.ExecutionException;

public class RobotOPCUAHelper extends BitSLickrMachineAdapter {

    private static final CustomLogger logger = new CustomLogger(RobotOPCUAHelper.class);

    public final String robotDiscreteInput = "301";
    public final String robotCoils = "302";
    public final String robotInputRegisters = "303";
    public final short robotHoldingRegisters = 304;
    public final String robotCommand = "305";

    public RobotOPCUAHelper(ROBOT robot)  {
        super(robot);
    }


    public ICustomResponse getHoldingRegisters(){
        try {
            return readVariableNode(robotHoldingRegisters);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse setHoldingRegisters(short[] s) {
        try {
            return writeVariableNode(robotHoldingRegisters, "short", s);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String getEndpointUrl() {

        return this.url;
    }
}

