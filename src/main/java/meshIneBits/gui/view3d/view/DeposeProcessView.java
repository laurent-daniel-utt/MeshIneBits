package meshIneBits.gui.view3d.view;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.Textlabel;
import meshIneBits.gui.view3d.Processor.DeposeMachineProcessor;
import processing.awt.PSurfaceAWT;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PShape;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.*;

public class DeposeProcessView extends UIParameterWindow {

  public static final String cutting_machine_icon = "resources/cutting-machine-icon.png";
  public static final String depositing_machine_photo = "resources/depositing-machine-photo.png";
  private static final Color rect1_color = new Color(49, 51, 48);
  private static final Color rect2_color = new Color(79, 79, 75);
  private static final Color button_color = new Color(10, 115, 119);
  private static final Color button_color_on_mouse = new Color(14, 156, 161);
  private static final Color font_color = new Color(253, 186, 33);
  private static final Color bit_data_color = new Color(254, 254, 254);

  private static Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

  private PImage icon;
  private PImage machinePhoto;

  private Button startButton;
  private Button stopButton;
  private Button pauseButton;
  private Button continueButton;
  private Button resetButton;
  private Button continueAfterEStopButton;
  private Button continueAfterTurnOffButton;
  private Button cameraLoginButton;
  private Button cameraCaptureImageButton;
  private Button acknowledgeErrorButton;
  private Textlabel messageError;
  private PShape rect1Background;
  private PShape rect2Background;
  private Textlabel[] currentBitData;

 // private Button exitButton;

  private DeposeMachineProcessor processor;

  public static void main(String[] args) {
    PApplet.main(DeposeProcessView.class.getCanonicalName());
  }

  public static void startProcessingModelView() {
    PApplet.main(DeposeProcessView.class.getCanonicalName());
  }


  public void settings() {
   // size(SCREEN_SIZE.width/2, SCREEN_SIZE.height, P3D);
  //  surface.setSize(100,100);
  }

  @Override
  public void setup() {
    PSurfaceAWT.SmoothCanvas smoothCanvas = (PSurfaceAWT.SmoothCanvas)surface.getNative();
    JFrame jframe = (JFrame) smoothCanvas.getFrame();
    jframe.setSize(SCREEN_SIZE.width/2, SCREEN_SIZE.height);
//    jframe.setSize(SCREEN_SIZE.width, SCREEN_SIZE.height);
    jframe.setLocation(0,0);
 //   jframe.setSize(SCREEN_SIZE.width/2, SCREEN_SIZE.height);
 //   jframe.setExtendedState(jframe.getExtendedState() | JFrame.MAXIMIZED_BOTH);
 //   jframe.setExtendedState(JFrame.MAXIMIZED_BOTH);
//    jframe.setResizable(true);
    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    super.setup();
    processor = new DeposeMachineProcessor(this::updateInfos);
    setUIControllerListener(processor);
  }

  @Override
  public void onOpen() {
  }

  @Override
  public void onClose() {
  //  processor.subscriptionTask.stop();
 //   exitActual();
  }

  @Override
  protected void generateButton() {

    rect1Background = createShape(RECT, 0, 0, (float) 1.15*width / 4, height);
    rect1Background.setFill(rect1_color.getRGB());

    rect2Background = createShape(RECT, (float) 1.15*width / 4, 0, width - (float) 1.15*width / 4, height);
    rect2Background.setFill(rect2_color.getRGB());

    icon = loadImage(cutting_machine_icon);
    icon.resize(width / 7, width / 7);

    machinePhoto = loadImage(depositing_machine_photo);
    machinePhoto.resize(436, 329);

    int component_label_color = 255;
    int sizeFont = 15;
    PFont text_font_default = createFont("arial bold", 14);

    startButton = getControl().
        addButton(START_DEPOSITING_MACHINE)
        .setLabel("Start")
        .setSize(width / 4 , 50)
        .setColorLabel(component_label_color)
        .setColorBackground(button_color.getRGB())
        .setColorForeground(button_color_on_mouse.getRGB())
        .setFont(text_font_default);
    stopButton = getControl().
        addButton(STOP_DEPOSITING_MACHINE)
        .setLabel("Stop")
          .setSize(width / 4 , 50)
        .setColorLabel(component_label_color)
        .setColorBackground(button_color.getRGB())
        .setColorForeground(button_color_on_mouse.getRGB())
        .setFont(text_font_default);
    pauseButton = getControl().
            addButton(PAUSE_DEPOSITING_MACHINE)
            .setLabel("Pause")
            .setSize(width / 4 , 50)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    continueButton = getControl().
            addButton(CONTINUE_DEPOSITING_MACHINE)
            .setLabel("Continue")
            .setSize(width / 4 , 50)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    resetButton = getControl().
            addButton(RESET_DEPOSITING_MACHINE)
            .setLabel("Reset")
            .setSize(width / 4 , 50)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    continueAfterEStopButton = getControl().
            addButton(CONTINUE_AFTER_E_STOP_DEPOSITING_MACHINE)
            .setLabel("Continue After E Stop")
            .setSize(width / 4 , 50)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    continueAfterTurnOffButton = getControl().
            addButton(CONTINUE_AFTER_TURN_OFF_DEPOSITING_MACHINE)
            .setLabel("Continue After turn off")
            .setSize(width / 4 , 50)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    cameraLoginButton = getControl().
            addButton(CAMERA_LOGIN_DEPOSITING_MACHINE)
            .setLabel("Camera login")
            .setSize((int) (width / 4 - 2 * 0.01f * width), 50)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    cameraCaptureImageButton = getControl().
            addButton(CAMERA_CAPTURE_IMAGE_DEPOSITING_MACHINE)
            .setLabel("Camera Capture image")
            .setSize((int) (width / 4 - 2 * 0.01f * width), 50)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    acknowledgeErrorButton = getControl().
            addButton(ACKNOWLEDGE_ERROR_DEPOSITING_MACHINE)
            .setLabel("Acknowledge error")
            .setSize(width / 4 , 50)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    messageError = getControl()
            .addTextlabel(MESSAGE_ERROR_DEPOSITING_MACHINE)
            .setText("Message error: ")
            .setColor(font_color.getRGB())
            .setFont(createFont("arial bold", sizeFont));
    currentBitData = new Textlabel[11];
    for (int i=0;i<11;i++){
      currentBitData[i] =getControl()
              .addTextlabel("CURRENT_BIT_DATA_DEPOSITING_MACHINE "+i)
              .setText("current bit data")
              .setColor(bit_data_color.getRGB())
              .setFont(createFont("arial bold", sizeFont));

    }
        initComponentPositions();
        showWebCamera();
  }

  public void showWebCamera(){
      SwingUtilities.invokeLater(new WebcamViewer(SCREEN_SIZE.width/2, SCREEN_SIZE.height));
  }

  private void initComponentPositions() {

    startButton.setPosition(0.0175f*width,0.3f*height);
    pauseButton.setPosition(0.0175f*width,0.37f*height);
    continueButton.setPosition(0.0175f*width,0.44f*height);
    stopButton.setPosition(0.0175f*width,0.51f*height);
    resetButton.setPosition(0.0175f*width,0.58f*height);
    continueAfterEStopButton.setPosition(0.0175f*width,0.65f*height);
    continueAfterTurnOffButton.setPosition(0.0175f*width,0.72f*height);

    cameraLoginButton.setPosition(0.31f * width,0.55f*height);
    cameraCaptureImageButton.setPosition(0.31f * width,0.62f*height);

    messageError.setPosition(0.31f * width, 0.38f * height);
    acknowledgeErrorButton.setPosition(0.31f * width, 0.42f * height);


    for (int i=0;i<11;i++){
      currentBitData[i].setPosition(width-width/5,0.06f*height+i*0.025f*height);
    }

    //   exitButton.setPosition(width-0.18f*width,height-0.08f*height);
  }
  @Override
  public void controlEvent(ControlEvent theEvent) {
    if (getListener() == null) {
      return;
    }
    switch (theEvent.getName()) {
      case START_DEPOSITING_MACHINE:
      case STOP_DEPOSITING_MACHINE:
      case PAUSE_DEPOSITING_MACHINE:
      case CONTINUE_DEPOSITING_MACHINE:
      case RESET_DEPOSITING_MACHINE:
      case CONTINUE_AFTER_E_STOP_DEPOSITING_MACHINE:
      case CONTINUE_AFTER_TURN_OFF_DEPOSITING_MACHINE:
      case CAMERA_LOGIN_DEPOSITING_MACHINE:
      case CAMERA_CAPTURE_IMAGE_DEPOSITING_MACHINE:
      case ACKNOWLEDGE_ERROR_DEPOSITING_MACHINE:
        getListener().onActionListener(this, theEvent.getName(), theEvent.getValue());
        break;
    }
  }

  @Override
  protected void updateButton() {
    initComponentPositions();
    shape(rect1Background);
    shape(rect2Background);
    image(icon, 50, 75);
    image(machinePhoto, (float) width / 4 + 60, 40);
  }

  public void setMessageError(String messageError){
    this.messageError.setText("Message error: "+messageError);
  }
  public void setCurrentBitData(String[] data){
    currentBitData[1].setText("id : "+data[0]);
    currentBitData[2].setText("id in batch : "+data[1]);
    currentBitData[3].setText("X : "+data[2]);
    currentBitData[4].setText("Z : "+data[3]);
    currentBitData[5].setText("Y : "+data[4]);
    currentBitData[6].setText("SubX : "+data[5]);
    currentBitData[7].setText("Rotation : "+data[6]);
    currentBitData[8].setText("Refline_vu : "+data[7]);
    currentBitData[9].setText("Refline_rot : "+data[8]);
    currentBitData[10].setText("Theta : "+data[9]);
  }
  public void setContinueButton(boolean b){// after pause, continue will lock during deceleration
    if(b) continueButton.lock();
    else continueButton.unlock();
  }

  private void updateInfos(String messageError,
                           String[] currentBitData,
                           boolean continueButtonLock)
  {
    setMessageError(messageError);
    setCurrentBitData(currentBitData);
    setContinueButton(continueButtonLock);
  }


}


