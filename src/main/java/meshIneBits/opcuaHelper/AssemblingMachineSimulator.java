package meshIneBits.opcuaHelper;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import meshIneBits.opcuaHelper.BaseCustomResponse.BaseCustomResponseBuilder;
import meshIneBits.util.MultiThreadServiceExecutor;

public class AssemblingMachineSimulator extends AssemblingMachineOPCUAHelper {

  private final MultiThreadServiceExecutor executor = MultiThreadServiceExecutor.instance;
  private final AssemblingIncreaseTask task = new AssemblingIncreaseTask();

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
  public ICustomResponse getMachineState() {
    return new BaseCustomResponseBuilder()
        .setNodeId(startNodeId)
        .setMessage("Machine is off")
        .setTypeValue("Boolean")
        .setValue("false")
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
  public ICustomResponse getAssemblingBit() {
    return new BaseCustomResponseBuilder()
        .setNodeId(assemblingBitNodeId)
        .setMessage("Bit in assembling process")
        .setTypeValue("Integer")
        .setValue(task.bitId.get())
        .setStatusCode(CustomStatusCode.STATUS_GOOD)
        .build();
  }

  @Override
  public ICustomResponse getAssemblingSubBit() {
    return new BaseCustomResponseBuilder()
        .setNodeId(assemblingSubBitNodeId)
        .setMessage("SubBit in assembling process")
        .setTypeValue("Integer")
        .setValue(task.bitId.get())
        .setStatusCode(CustomStatusCode.STATUS_GOOD)
        .build();  }

  @Override
  public ICustomResponse pauseMachine() {
    return super.pauseMachine();
  }

  private class AssemblingIncreaseTask implements Runnable {

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
            synchronized (AssemblingMachineSimulator.AssemblingIncreaseTask.this) {
              AssemblingMachineSimulator.AssemblingIncreaseTask.this.wait();
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
      } else {
        executor.execute(this);
      }
    }
  }
}
