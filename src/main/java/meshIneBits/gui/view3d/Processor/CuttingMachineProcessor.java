package meshIneBits.gui.view3d.Processor;

import java.util.concurrent.atomic.AtomicBoolean;
import meshIneBits.Mesh;
import meshIneBits.NewBit3D;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.oldversion.GraphicElementLabel;
import meshIneBits.gui.view3d.provider.CuttingBitShapeProvider;
import meshIneBits.gui.view3d.provider.MeshProvider;
import meshIneBits.gui.view3d.view.UIPWListener;
import meshIneBits.opcuaHelper.CuttingMachineCommander;
import meshIneBits.util.MultiThreadServiceExecutor;
import processing.core.PApplet;
import processing.core.PShape;

public class CuttingMachineProcessor implements UIPWListener {

  public interface BitInCuttingProcessCallback {

    void callback(PShape shape, String bitID, String layerId, String nbSubBit, String position);
  }

  //  private CuttingProcessView view;
  private final CuttingMachineCommander commander;
  private final CuttingBitShapeProvider provider;
  private final BitInCuttingProcessCallback callback;
  private final AtomicBoolean inProcess = new AtomicBoolean(false);

  public CuttingMachineProcessor(PApplet context, BitInCuttingProcessCallback callback) {
    Mesh mesh = MeshProvider.getInstance().getCurrentMesh();
    commander = new CuttingMachineCommander(mesh);
    provider = new CuttingBitShapeProvider(context, mesh);
    this.callback = callback;

//    if (context instanceof CuttingProcessView) {
//      view = (CuttingProcessView) context;
//    }
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
              bit3D.getOrigin().x + ", " + bit3D.getOrigin().y);
          Thread.sleep(Visualization3DConfig.REFRESH_TIME);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}