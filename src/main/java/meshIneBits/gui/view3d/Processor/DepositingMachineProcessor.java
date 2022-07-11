package meshIneBits.gui.view3d.Processor;

import meshIneBits.Mesh;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.*;
import meshIneBits.gui.view3d.provider.MeshProvider;
import meshIneBits.gui.view3d.view.UIPWListener;
import meshIneBits.opcuaHelper.DepositingMachineCommander;
import meshIneBits.util.MultiThreadServiceExecutor;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class DepositingMachineProcessor implements UIPWListener {

  public interface DepositingProcessCallback {
      void callback( String messageError, String[] currentBitData,boolean continueButtonLock, String[] currentAxesPositions);
  }

  private final DepositingMachineCommander commander;
  private final DepositingProcessCallback callback;
  private final DecimalFormat df;

  {
    df = new DecimalFormat("#.##");
    df.setMaximumFractionDigits(2);
    df.setRoundingMode(RoundingMode.CEILING);
  }

  public DepositingMachineProcessor(DepositingProcessCallback callback) {
    Mesh mesh = MeshProvider.getInstance().getCurrentMesh();
    commander = new DepositingMachineCommander(mesh);
    this.callback = callback;
    MultiThreadServiceExecutor.instance.execute(new DepositingMachineProcessor.SubscriptionTask());
  }

  private void startDepose() {
    try {
      this.powershell("ssh pi@voiceslicr-nog ./speak2.sh please clear the machine area ; ssh pi@voiceslicr-nog omxplayer --vol -2000 -o local /home/pi/music/rocknroll.mp3");
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
  private void resetPowerAxes() {
    try {
      commander.resetPowerAxes();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void takeBatch() {
    try {
      commander.takeBatch();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void deposeBatch() {
    try {
      commander.deposeBatch();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void readXMLFile() {
    try {
      commander.readXMLFile();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void renameXMLFile() {
    try {
      commander.renameXMLFile();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void synchroAxesX() {
    try {
      commander.synchroAxesX();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void synchroAxesZ() {
    try {
      commander.synchroAxesZ();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void homingAxisSubX() {
    try {
      commander.homingAxisSubX();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void homingAxisSubZ() {
    try {
      commander.homingAxisSubZ();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void homingAxisTheta() {
    try {
      commander.homingAxisTheta();
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
      case (RESET_POWER_AXES_DEPOSITING_MACHINE):
        resetPowerAxes();
        break;
      case (TAKE_BATCH_DEPOSITING_MACHINE):
        takeBatch();
        break;
      case (DEPOSE_BATCH_DEPOSITING_MACHINE):
        deposeBatch();
        break;
      case (READ_XML_FILE_DEPOSITING_MACHINE):
        readXMLFile();
        break;
      case (RENAME_XML_FILE_DEPOSITING_MACHINE):
        renameXMLFile();
        break;
      case (SYNCHRO_AXES_X_DEPOSITING_MACHINE):
        synchroAxesX();
        break;
      case (SYNCHRO_AXES_Z_DEPOSITING_MACHINE):
        synchroAxesZ();
        break;
      case (HOMING_AXIS_SUBX_DEPOSITING_MACHINE):
        homingAxisSubX();
        break;
      case (HOMING_AXIS_SUBZ_DEPOSITING_MACHINE):
        homingAxisSubZ();
        break;
      case (HOMING_AXIS_Theta_DEPOSITING_MACHINE):
        homingAxisTheta();
        break;
    }
  }

  public class SubscriptionTask implements Runnable {
    private boolean exitThread=false;
    @Override
    public void run() {
      try {
        while(!exitThread){
          callback.callback(commander.getMessageError(),getCurrentBitData(), commander.getLockContinueButton(),getCurrentAxesPositions());
          Thread.sleep(100);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    private String[] getCurrentBitData(){
      String[] currentBitData;
      currentBitData=new String[10];
      try {
        currentBitData[0]= String.valueOf(commander.getCurrentBitId());
        currentBitData[1]= String.valueOf(commander.getCurrentBitIdInBatch());
        currentBitData[2]= String.valueOf(new DecimalFormat("0.00").format(commander.getCurrentBitX()));
        currentBitData[3]= String.valueOf(new DecimalFormat("0.00").format(commander.getCurrentBitZ()));
        currentBitData[4]= String.valueOf(new DecimalFormat("0.00").format(commander.getCurrentBitY()));
        currentBitData[5]= String.valueOf(new DecimalFormat("0.00").format(commander.getCurrentBitSubX()));
        currentBitData[6]= String.valueOf(new DecimalFormat("0.00").format(commander.getCurrentBitRotation()));
        currentBitData[7]= String.valueOf(new DecimalFormat("0.00").format(commander.getCurrentBitReflineVu()));
        currentBitData[8]= String.valueOf(new DecimalFormat("0.00").format(commander.getCurrentBitReflineRot()));
        currentBitData[9]= String.valueOf(new DecimalFormat("0.00").format(commander.getCurrentBitTheta()));
      } catch (Exception e) {
        e.printStackTrace();
      }
      return currentBitData;
    }


    private String[] getCurrentAxesPositions(){
      String[] currentAxesPositions;
      currentAxesPositions=new String[6];
      try {
        currentAxesPositions[0]= String.valueOf(new DecimalFormat("0.00").format(commander.getXCurrentPosition()));
        currentAxesPositions[1]= String.valueOf(new DecimalFormat("0.00").format(commander.getZCurrentPosition()));
        currentAxesPositions[2]= String.valueOf(new DecimalFormat("0.00").format(commander.getYCurrentPosition()));
        currentAxesPositions[3]= String.valueOf(new DecimalFormat("0.00").format(commander.getSubXCurrentPosition()));
        currentAxesPositions[4]= String.valueOf(new DecimalFormat("0.00").format(commander.getSubZCurrentPosition()));
        currentAxesPositions[5]= String.valueOf(new DecimalFormat("0.00").format(commander.getThetaCurrentPosition()));
      } catch (Exception e) {
        e.printStackTrace();
      }
      return currentAxesPositions;
    }

    public void stop(){
      exitThread=true;
    }
  }

  // voice SlicR :)
  public void powershell(String msg){
 //   String command = "powershell.exe ssh pi@voiceslicr-nog "+msg;
    String command = "powershell.exe "+msg;
    try {
      Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
