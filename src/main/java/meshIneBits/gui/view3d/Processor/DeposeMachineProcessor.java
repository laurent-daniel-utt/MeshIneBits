package meshIneBits.gui.view3d.Processor;

import meshIneBits.Mesh;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.*;
import meshIneBits.gui.view3d.provider.MeshProvider;
import meshIneBits.gui.view3d.view.UIPWListener;
import meshIneBits.opcuaHelper.DeposeMachineCommander;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class DeposeMachineProcessor implements UIPWListener {

  public interface DepositingProcessCallback {
      void callback( String messageError);
  }

  private final DeposeMachineCommander commander;
  private final DepositingProcessCallback callback;
  private final DecimalFormat df;

  {
    df = new DecimalFormat("#.##");
    df.setMaximumFractionDigits(2);
    df.setRoundingMode(RoundingMode.CEILING);
  }

  public SubscriptionTask subscriptionTask;
  private Thread t;

  public DeposeMachineProcessor(DepositingProcessCallback callback) {
    Mesh mesh = MeshProvider.getInstance().getCurrentMesh();
    commander = new DeposeMachineCommander(mesh);
    this.callback = callback;
    subscriptionTask=new SubscriptionTask();
    t=new Thread(subscriptionTask);
    t.start();
 //   MultiThreadServiceExecutor.instance.execute(new DeposeMachineProcessor.SubscriptionTask());
 //   MultiThreadServiceExecutor.instance.execute(subscriptionTask);
  }

  private void startDepose() {
    try {
      commander.startDepose();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void stopDepose() {
    try {
      commander.stopDepose();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void resetDepose() {
    try {
      commander.resetDepose();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void continueDepose() {
    try {
      commander.continueDepose();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void pauseDepose() {
    try {
      commander.pauseDepose();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void continueAfterTurnOff() {
    try {
      commander.continueAfterTurnOff();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void continueAfterESTOP() {
    try {
      commander.continueAfterESTOP();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void cameraLogin() {
    try {
      commander.cameraLogin();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void cameraCaptureImage() {
    try {
      commander.cameraCaptureImage();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void acknowledgeError() {
    try {
      commander.acknowledgeError();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onActionListener(Object callbackObj, String event, Object value) {
    switch (event) {
      case (START_DEPOSITING_MACHINE):
        startDepose();
        break;
      case (STOP_DEPOSITING_MACHINE):
        stopDepose();
        break;
      case (PAUSE_DEPOSITING_MACHINE):
        pauseDepose();
        break;
      case (CONTINUE_DEPOSITING_MACHINE):
        continueDepose();
        break;
      case (RESET_DEPOSITING_MACHINE):
        resetDepose();
        break;
      case (CONTINUE_AFTER_E_STOP_DEPOSITING_MACHINE):
        continueAfterESTOP();
        break;
      case (CONTINUE_AFTER_TURN_OFF_DEPOSITING_MACHINE):
        continueAfterTurnOff();
        break;
      case (CAMERA_LOGIN_DEPOSITING_MACHINE):
        cameraLogin();
        break;
      case (CAMERA_CAPTURE_IMAGE_DEPOSITING_MACHINE):
        cameraCaptureImage();
        break;
      case (ACKNOWLEDGE_ERROR_DEPOSITING_MACHINE):
        acknowledgeError();
        break;
    }
  }

  public class SubscriptionTask implements Runnable {
    private boolean exitThread=false;// to exit th thread when we close the window interface
    @Override
    public void run() {
      try {
        while(!exitThread){
          String messageError=commander.getMessageError();
          callback.callback(messageError);
          Thread.sleep(100);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    public void stop(){
      exitThread=true;
    }
  }
}
