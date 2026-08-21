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

package meshIneBits.gui.view3d.Processor;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import meshIneBits.Mesh;
import meshIneBits.NewBit3D;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.oldversion.GraphicElementLabel;
import meshIneBits.gui.view3d.provider.CuttingBitShapeProvider;
import meshIneBits.gui.view3d.provider.MeshProvider;
import meshIneBits.gui.view3d.view.UIPWListener;
import meshIneBits.opcuaHelper.CuttingMachineCommander;
import meshIneBits.util.CustomLogger;
import meshIneBits.util.MultiThreadServiceExecutor;
import processing.core.PApplet;
import processing.core.PShape;

public class CuttingMachineProcessor implements UIPWListener {

  private final CustomLogger logger = new CustomLogger(this.getClass());
  public interface BitInCuttingProcessCallback {

    void callback(PShape shape, String bitID, String layerId, String nbSubBit, String position);
  }

  //  private CuttingProcessView view;
  private final CuttingMachineCommander commander;
  private final CuttingBitShapeProvider provider;
  private final BitInCuttingProcessCallback callback;
  private final AtomicBoolean inProcess = new AtomicBoolean(false);
  private final DecimalFormat df;

  {
    df = new DecimalFormat("#.##");
    df.setMaximumFractionDigits(2);
    df.setRoundingMode(RoundingMode.CEILING);
  }

  public CuttingMachineProcessor(PApplet context, BitInCuttingProcessCallback callback) {
    Mesh mesh = MeshProvider.getInstance().getCurrentMesh();
    commander = new CuttingMachineCommander(mesh);
    provider = new CuttingBitShapeProvider(context, mesh);
    initMachineState();
    this.callback = callback;
  }

  private void initMachineState() {
    try {
      inProcess.set(commander.getMachineState());
    } catch (Exception e) {
      e.printStackTrace();
      logger.logERRORMessage("unable to get machine state. Check result in Commander class");
    }
  }

  private void startMachine() {
    try {
      commander.startMachine();
      MultiThreadServiceExecutor.instance.execute(new SubscriptionTask());
      inProcess.set(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void stopMachine() {
    try {
      commander.stopMachine();
      inProcess.set(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public boolean isInProcess() {
    return inProcess.get();
  }

  @Override
  public void onActionListener(Object callbackObj, String event, Object value) {
    switch (event) {
      case (GraphicElementLabel.START_CUTTING_MACHINE):
        startMachine();
        break;
      case (GraphicElementLabel.STOP_CUTTING_MACHINE):
        stopMachine();
        break;
    }
  }

  public class SubscriptionTask implements Runnable {

    @Override
    public void run() {
      try {
        while (inProcess.get()) {
          NewBit3D bit3D = commander.getCuttingBit();
          Mesh mesh = MeshProvider.getInstance().getCurrentMesh();
          callback.callback(
              provider.getCuttingBitShapeByBit(bit3D).getShape(),
              Integer.toString(mesh.getScheduler().getBitIndex(bit3D)),
              Integer.toString(mesh.getScheduler().getLayerContainBit(bit3D).getLayerNumber()),
              Integer.toString(bit3D.getSubBits().size()),
              df.format(bit3D.getOrigin().x) + ", " + df.format(bit3D.getOrigin().y));
          Thread.sleep(Visualization3DConfig.REFRESH_TIME);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
