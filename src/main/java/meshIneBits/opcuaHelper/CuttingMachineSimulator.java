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

import meshIneBits.opcuaHelper.BaseCustomResponse.BaseCustomResponseBuilder;
import meshIneBits.util.MultiThreadServiceExecutor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CuttingMachineSimulator extends CuttingMachineOPCUAHelper {

  private MultiThreadServiceExecutor executor = MultiThreadServiceExecutor.instance;
  private CuttingIncreaseTask task = new CuttingIncreaseTask();

  public CuttingMachineSimulator() throws Exception {
  }

  public CuttingMachineSimulator(ROBOT robot) throws Exception {
    super(robot);
  }

  @Override
  public ICustomResponse startMachine() {
    task.activate(true);
    return new BaseCustomResponseBuilder()
        .setNodeId(startNodeId)
        .setMessage("Machine turned on")
        .setTypeValue("Boolean")
        .setValue("true")
        .setStatusCode(CustomStatusCode.STATUS_GOOD)
        .build();
  }

  @Override
  public ICustomResponse stopMachine() {
    task.activate(false);
    return new BaseCustomResponseBuilder()
        .setNodeId(startNodeId)
        .setMessage("Machine turned off")
        .setTypeValue("Boolean")
        .setValue("false")
        .setStatusCode(CustomStatusCode.STATUS_GOOD)
        .build();
  }

  @Override
  public ICustomResponse pauseMachine() {
    task.pause(!task.pausing.get());
    return super.pauseMachine();
  }

  @Override
  public ICustomResponse getCuttingBitId() {
    return new BaseCustomResponseBuilder()
        .setNodeId(cuttingButNodeId)
        .setMessage("Bit in cutting process")
        .setTypeValue("Integer")
        .setValue(task.bitId.get())
        .setStatusCode(CustomStatusCode.STATUS_GOOD)
        .build();
  }

  @Override
  public ICustomResponse getCuttingPathId() {
    return new BaseCustomResponseBuilder()
        .setNodeId(cuttingButNodeId)
        .setMessage("Cut path in cutting process")
        .setTypeValue("Integer")
        .setValue(task.bitId.get()%2)
        .setStatusCode(CustomStatusCode.STATUS_GOOD)
        .build();
  }

  @Override
  public ICustomResponse getMachineState() {
    return new BaseCustomResponseBuilder()
        .setNodeId(startNodeId)
        .setMessage("Machine is turned off")
        .setTypeValue("Boolean")
        .setValue(false)
        .setStatusCode(CustomStatusCode.STATUS_GOOD)
        .build();
  }

  private class CuttingIncreaseTask implements Runnable {

    private final AtomicInteger bitId = new AtomicInteger(0);
    private final AtomicBoolean isActivated = new AtomicBoolean(false);
    private final AtomicBoolean pausing = new AtomicBoolean(false);

    private final int max = 10;
    private final int min = 1;
    private final int range = max - min + 1;


    @Override
    public void run() {
      try {
        while (isActivated.get()) {
          if (pausing.get()) {
            synchronized (CuttingIncreaseTask.this) {
              CuttingIncreaseTask.this.wait();
            }
          }
          Thread.sleep(((int) (Math.random() * range) + min) * 1000);
          bitId.set(bitId.get() + 1);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    private synchronized void pause(boolean boo) {
      pausing.set(boo);
    }

    private synchronized void activate(boolean boo) {
      isActivated.set(boo);
      if (!boo) {
        task.pause(false);
      }else{
        executor.execute(this);
      }
    }
  }

}
