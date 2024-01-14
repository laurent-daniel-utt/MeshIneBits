package meshIneBits.gui.view3d.view;

import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.BIT_ID_LABEL;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.LAYER_ID_LABEL;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.NUMBER_SUB_BIT_LABEL;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.POSITION_BIT_LABEL;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.START_CUTTING_MACHINE;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.STOP_CUTTING_MACHINE;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.Textlabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import meshIneBits.gui.view3d.Processor.CuttingMachineProcessor;
import meshIneBits.gui.view3d.provider.ProjectProvider;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PShape;

public class CuttingProcessView extends UIParameterWindow {

  public static final String cutting_machine_icon = "resources/cutting-machine-icon.png";
  public static final String cutting_machine_photo = "resources/cutting-machine-photo.png";
  private static final Color rect1_color = new Color(49, 51, 48);
  private static final Color rect2_color = new Color(79, 79, 75);
  private static final Color button_color = new Color(10, 115, 119);
  private static final Color button_color_on_mouse = new Color(14, 156, 161);
  private static final Color in_process_state = new Color(0, 255, 0);
  private static final Color out_process_state = new Color(253, 186, 33);
  private static final Color font_color = new Color(253, 186, 33);
  private static Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
  private PImage icon;
  private PImage machinePhoto;


  public static void main(String[] args) {
    PApplet.main(CuttingProcessView.class.getCanonicalName());
  }

  public static void startProcessingModelView() {
    if (!ProjectProvider.getInstance().isAvailable()) {
      return;
    }
    PApplet.main(CuttingProcessView.class.getCanonicalName());
  }

  private Button startButton;
  private Button stopButton;
  private PShape circleState;
  private PShape rect1Background;
  private PShape rect2Background;
  private Textlabel bitId;
  private Textlabel nbSubBit;
  private Textlabel position;
  private Textlabel layerId;
  private PShape shapeBit;
  private CuttingMachineProcessor processor;

  private void initComponentPositions() {
    float[] startPosition = new float[]
        {0.01f * width, 0.4f * height};
    float[] pausePosition = new float[]
        {0.01f * width, 0.47f * height};
    startButton.setPosition(startPosition);
    stopButton.setPosition(pausePosition);
    bitId.setPosition(0.30f * width, 0.70f * height);
    layerId.setPosition(0.30f * width, 0.75f * height);
    nbSubBit.setPosition(0.30f * width, 0.80f * height);
    position.setPosition(0.30f * width, 0.85f * height);
  }

  public void settings() {
    size(SCREEN_SIZE.width / 2, SCREEN_SIZE.height / 2, P3D);
  }

  @Override
  public void setup() {
    super.setup();
    processor = new CuttingMachineProcessor(this, this::updateInfos);
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

    machinePhoto = loadImage(cutting_machine_photo);
    machinePhoto.resize(400, 300);

    int component_label_color = 255;
    int sizeFont = 15;
    PFont text_font_default = createFont("arial bold", 14);

    circleState = createShape(ELLIPSE, (float) width / 8, 0.6f * height, 50, 50);
    circleState.setFill(out_process_state.getRGB());

    startButton = getControl().
        addButton(START_CUTTING_MACHINE)
        .setLabel("Start")
        .setSize((int) (width / 4 - 2 * 0.01f * width), 30)
        .setColorLabel(component_label_color)
        .setColorBackground(button_color.getRGB())
        .setColorForeground(button_color_on_mouse.getRGB())
        .setFont(text_font_default);
    stopButton = getControl().
        addButton(STOP_CUTTING_MACHINE)
        .setLabel("Stop")
        .setSize((int) (width / 4 - 2 * 0.01f * width), 30)
        .setColorLabel(component_label_color)
        .setColorBackground(button_color.getRGB())
        .setColorForeground(button_color_on_mouse.getRGB())
        .setFont(text_font_default);

    bitId = getControl()
        .addTextlabel(BIT_ID_LABEL)
        .setText("Identifier of Bit: N/A")
        .setColor(font_color.getRGB())
        .setFont(createFont("arial bold", sizeFont));
    nbSubBit = getControl()
        .addTextlabel(NUMBER_SUB_BIT_LABEL)
        .setText("Number of sub bit: N/A")
        .setColor(font_color.getRGB())
        .setFont(createFont("arial bold", sizeFont));
    layerId = getControl()
        .addTextlabel(LAYER_ID_LABEL)
        .setText("Layer ID: N/A")
        .setColor(font_color.getRGB())
        .setFont(createFont("arial bold", sizeFont));
    position = getControl()
        .addTextlabel(POSITION_BIT_LABEL)
        .setText("Position of Bit: N/A")
        .setColor(font_color.getRGB())
        .setFont(createFont("arial bold", sizeFont));
    initComponentPositions();
  }

  public void setShape(PShape shape) {
    shapeBit = shape;
    shape.resetMatrix();
    shapeBit.scale(3.5f);
    shapeBit.translate((float) width / 4 + 20, (float) height / 4);
  }

  @Override
  public void controlEvent(ControlEvent theEvent) {
    if (getListener() == null) {
      return;
    }
    switch (theEvent.getName()) {
      case START_CUTTING_MACHINE:
      case STOP_CUTTING_MACHINE:
        getListener().onActionListener(this, theEvent.getName(), theEvent.getValue());
        break;
    }
  }

  @Override
  protected void updateButton() {
    shape(rect1Background);
    shape(rect2Background);
    image(icon, 50, 50);
    if (!processor.isInProcess()) {
      image(machinePhoto, (float) width / 4 + 100, 50);
      circleState.setFill(out_process_state.getRGB());
    } else {
      circleState.setFill(in_process_state.getRGB());
      if (shapeBit != null) {
        shape(shapeBit);
      }
    }
    shape(circleState);
  }

  public void setBitId(String bitId) {
    this.bitId.setText("Bit ID: " + bitId);
  }

  public void setNumberSubBit(String nbSubBit) {
    this.nbSubBit.setText("Nb SubBit: " + nbSubBit);
  }

  public void setBitPosition(String bitPosition) {
    this.position.setText("Position of bit: " + bitPosition);
  }

  public void setLayerId(String layerId) {
    this.layerId.setText("Layer ID: " + layerId);
  }

  private void updateInfos(PShape shape,
      String bitID,
      String layerId,
      String nbSubBit,
      String position) {
    setShape(shape);
    setBitId(bitID);
    setBitPosition(position);
    setLayerId(layerId);
    setNumberSubBit(nbSubBit);
  }

}
