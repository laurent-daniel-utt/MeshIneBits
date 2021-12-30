package meshIneBits.gui.view3d.Processor;

import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import meshIneBits.Bit2D;
import meshIneBits.Bit3D;
import meshIneBits.Mesh;
import meshIneBits.SubBit2D;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.builder.BitShape;
import meshIneBits.gui.view3d.builder.IMeshShapeBuilder;
import meshIneBits.gui.view3d.builder.PavedMeshBuilderResult;
import meshIneBits.gui.view3d.builder.SubBitShape;
import meshIneBits.gui.view3d.oldversion.GraphicElementLabel;
import meshIneBits.gui.view3d.provider.MeshProvider;
import meshIneBits.gui.view3d.view.AssemblingProcessView.AssemblingProcessViewListener;
import meshIneBits.opcuaHelper.AssemblingMachineCommander;
import meshIneBits.scheduler.AdvancedScheduler;
import meshIneBits.util.MultiThreadServiceExecutor;
import processing.core.PApplet;
import processing.core.PShape;

public class AssemblingMachineProcessor implements AssemblingProcessViewListener {

  public interface AssemblingProcessCallback {

    void callback(Vector<PShape> shapes,
        String subBitID,
        String bitID,
        String layerId,
        String nbSubBit,
        String position);
  }

  private final AtomicBoolean inProcess = new AtomicBoolean(false);
  private final AssemblingMachineCommander commander;
  private final AssemblingProcessCallback callback;
  private final Vector<PShape> subBitShapes = new Vector<>();

  public AssemblingMachineProcessor(PApplet context, AssemblingProcessCallback callback) {
    Mesh mesh = MeshProvider.getInstance().getCurrentMesh();
    this.commander = new AssemblingMachineCommander(mesh);
    this.callback = callback;

    PavedMeshBuilderResult pavedResult = IMeshShapeBuilder.createInstance(context, mesh)
        .buildMeshShape();

    subBitShapes.addAll(pavedResult
        .getBitShapes()
        .stream()
        .map(BitShape::getSubBitShapes)
        .flatMap(Collection::stream)
        .map(SubBitShape::getShape)
        .collect(Collectors.toList()));
  }

  private void startMachine() {
    try {
      commander.startMachine();
      inProcess.set(true);
      MultiThreadServiceExecutor.instance.execute(new SubscriptionTask());
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
          SubBit2D subBit2D = commander.getAssemblingSubBit();
          Mesh mesh = MeshProvider.getInstance().getCurrentMesh();
          int subBitId = ((AdvancedScheduler) mesh.getScheduler()).getIndexOfSubBit(subBit2D);
          Bit2D parentBit = subBit2D.getParentBit();
          Bit3D bit3D = ((AdvancedScheduler) mesh.getScheduler()).getBit3DFrom(parentBit);
          callback.callback(
              new Vector<>(subBitShapes.subList(0, subBitId)),
              Integer.toString(subBitId),
              Integer.toString(mesh.getScheduler().getBitIndex(bit3D)),
              Integer.toString(mesh.getScheduler().getLayerContainBit(bit3D).getLayerNumber()),
              Integer.toString(bit3D.getSubBits().size()),
              subBit2D.getLiftPointCS().x + ", " + subBit2D.getLiftPointCS().y);
          Thread.sleep(Visualization3DConfig.REFRESH_TIME);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
