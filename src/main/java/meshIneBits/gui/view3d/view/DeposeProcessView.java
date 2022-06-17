package meshIneBits.gui.view3d.view;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.Textlabel;
import meshIneBits.gui.view3d.Processor.DeposeMachineProcessor;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PShape;

import java.awt.*;

import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.*;

public class DeposeProcessView extends UIParameterWindow {

  public static final String cutting_machine_icon = "resources/cutting-machine-icon.png";
  public static final String depositing_machine_photo = "resources/depositing-machine-photo.png";
  private static final Color rect1_color = new Color(49, 51, 48);
  private static final Color rect2_color = new Color(79, 79, 75);
  private static final Color button_color = new Color(10, 115, 119);
  private static final Color button_color_on_mouse = new Color(14, 156, 161);
  private static final Color font_color = new Color(253, 186, 33);

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

  private Button exitButton;

  private DeposeMachineProcessor processor;

  public static void main(String[] args) {
    PApplet.main(DeposeProcessView.class.getCanonicalName());
  }

  private void initComponentPositions() {

    startButton.setPosition(0.01f*width,0.35f*height);
    pauseButton.setPosition(0.01f*width,0.42f*height);
    continueButton.setPosition(0.01f*width,0.49f*height);
    stopButton.setPosition(0.01f*width,0.56f*height);
    resetButton.setPosition(0.01f*width,0.63f*height);
    continueAfterEStopButton.setPosition(0.01f*width,0.7f*height);
    continueAfterTurnOffButton.setPosition(0.01f*width,0.77f*height);
    cameraLoginButton.setPosition(width-width/4,0.35f*height);
    cameraCaptureImageButton.setPosition(width-width/4,0.42f*height);
    messageError.setPosition(0.30f * width, 0.70f * height);
    acknowledgeErrorButton.setPosition(0.30f * width, 0.75f * height);

    exitButton.setPosition(width-0.18f*width,height-0.08f*height);

  }

  public void settings() {
    size(SCREEN_SIZE.width / 2, SCREEN_SIZE.height / 2, P3D);
  }

  @Override
  public void setup() {
    super.setup();
    processor = new DeposeMachineProcessor(this::updateInfos);
    setUIControllerListener(processor);
  }

  @Override
  public void onOpen() {
  }

  @Override
  public void onClose() {
  }

  @Override
  protected void generateButton() {

    rect1Background = createShape(RECT, 0, 0, (float) width / 4, height);
    rect1Background.setFill(rect1_color.getRGB());

    rect2Background = createShape(RECT, (float) width / 4, 0, width - (float) width / 4, height);
    rect2Background.setFill(rect2_color.getRGB());

    icon = loadImage(cutting_machine_icon);
    icon.resize(100, 100);

    machinePhoto = loadImage(depositing_machine_photo);
    machinePhoto.resize(400, 300);

    int component_label_color = 255;
    int sizeFont = 15;
    PFont text_font_default = createFont("arial bold", 14);

    startButton = getControl().
        addButton(START_DEPOSITING_MACHINE)
        .setLabel("Start")
        .setSize((int) (width / 4 - 2 * 0.01f * width), 30)
        .setColorLabel(component_label_color)
        .setColorBackground(button_color.getRGB())
        .setColorForeground(button_color_on_mouse.getRGB())
        .setFont(text_font_default);
    stopButton = getControl().
        addButton(STOP_DEPOSITING_MACHINE)
        .setLabel("Stop")
        .setSize((int) (width / 4 - 2 * 0.01f * width), 30)
        .setColorLabel(component_label_color)
        .setColorBackground(button_color.getRGB())
        .setColorForeground(button_color_on_mouse.getRGB())
        .setFont(text_font_default);
    pauseButton = getControl().
            addButton(PAUSE_DEPOSITING_MACHINE)
            .setLabel("Pause")
            .setSize((int) (width / 4 - 2 * 0.01f * width), 30)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    continueButton = getControl().
            addButton(CONTINUE_DEPOSITING_MACHINE)
            .setLabel("Continue")
            .setSize((int) (width / 4 - 2 * 0.01f * width), 30)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    resetButton = getControl().
            addButton(RESET_DEPOSITING_MACHINE)
            .setLabel("Reset")
            .setSize((int) (width / 4 - 2 * 0.01f * width), 30)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    continueAfterEStopButton = getControl().
            addButton(CONTINUE_AFTER_E_STOP_DEPOSITING_MACHINE)
            .setLabel("Continue After E Stop")
            .setSize((int) (width / 4 - 2 * 0.01f * width), 30)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    continueAfterTurnOffButton = getControl().
            addButton(CONTINUE_AFTER_TURN_OFF_DEPOSITING_MACHINE)
            .setLabel("Continue After turn off")
            .setSize((int) (width / 4 - 2 * 0.01f * width), 30)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    cameraLoginButton = getControl().
            addButton(CAMERA_LOGIN_DEPOSITING_MACHINE)
            .setLabel("Camera login")
            .setSize((int) (width / 4 - 2 * 0.01f * width), 30)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    cameraCaptureImageButton = getControl().
            addButton(CAMERA_CAPTURE_IMAGE_DEPOSITING_MACHINE)
            .setLabel("Camera Capture image")
            .setSize((int) (width / 4 - 2 * 0.01f * width), 30)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    acknowledgeErrorButton = getControl().
            addButton(ACKNOWLEDGE_ERROR_DEPOSITING_MACHINE)
            .setLabel("Acknowledge error")
            .setSize((int) (width / 4 - 2 * 0.01f * width), 30)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);
    messageError = getControl()
            .addTextlabel(MESSAGE_ERROR_DEPOSITING_MACHINE)
            .setText("Message error: ")
            .setColor(font_color.getRGB())
            .setFont(createFont("arial bold", sizeFont));

    exitButton = getControl()
            .addButton(EXIT_WINDOW_DEPOSITING_MACHINE)
            .setLabel("Exit")
            .setSize((int) (width / 4 - 8 * 0.01f * width), 30)
            .setColorLabel(component_label_color)
            .setColorBackground(button_color.getRGB())
            .setColorForeground(button_color_on_mouse.getRGB())
            .setFont(text_font_default);

    initComponentPositions();
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
      case EXIT_WINDOW_DEPOSITING_MACHINE:
        processor.subscriptionTask.stop();
        this.closeWindow();
        break;
    }
  }

  @Override
  protected void updateButton() {
    shape(rect1Background);
    shape(rect2Background);
    image(icon, 50, 50);
    image(machinePhoto, (float) width / 4 + 50, 50);
  }

  public void setMessageError(String messageError){
    this.messageError.setText("Message error: "+messageError);
  }

  private void updateInfos(String messageError) {
    setMessageError(messageError);
  }
}
