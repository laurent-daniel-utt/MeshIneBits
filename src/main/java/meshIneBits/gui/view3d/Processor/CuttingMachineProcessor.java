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
