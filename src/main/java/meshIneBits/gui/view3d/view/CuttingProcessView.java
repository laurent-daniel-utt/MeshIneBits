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
import java.awt.Dimension;
import java.awt.Toolkit;
import meshIneBits.gui.view3d.Processor.CuttingMachineProcessor;
import meshIneBits.gui.view3d.provider.MeshProvider;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PShape;

public class CuttingProcessView extends UIParameterWindow {

  private static Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

  public static void startProcessingModelView() {
    if (!MeshProvider.getInstance().isAvailable()) {
      return;
    }
    PApplet.main(CuttingProcessView.class.getCanonicalName());
  }

  private Button startButton;
  private Button pauseButton;
  private PShape circleState;
  private Textlabel bitId;
  private Textlabel nbSubBit;
  private Textlabel position;
  private Textlabel layerId;
  private PShape shapeBit;
  private CuttingMachineProcessor processor;

  private void initComponentPositions() {
    float[] startPosition = new float[]
        {0.4f * width - (float) startButton.getWidth() / 2, 0.1f * height};
    float[] pausePosition = new float[]
        {0.6f * width - (float) pauseButton.getWidth() / 2, 0.1f * height};
    startButton.setPosition(startPosition);
    pauseButton.setPosition(pausePosition);
    bitId.setPosition(0.04f * width, 0.70f * height);
    layerId.setPosition(0.04f * width, 0.75f * height);
    nbSubBit.setPosition(0.04f * width, 0.80f * height);
    position.setPosition(0.04f * width, 0.85f * height);
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
    circleState = createShape(ELLIPSE, (float) width / 2, (float) height / 2, 30, 30);
    circleState.setFill(color(255, 0, 0));

    int component_label_color = 255;
    PFont text_font_default = createFont("arial bold", 14);

    startButton = getControl().
        addButton(START_CUTTING_MACHINE)
        .setLabel("Start")
        .setSize(100, 100)
        .setColorLabel(component_label_color)
        .setFont(text_font_default);
    pauseButton = getControl().
        addButton(STOP_CUTTING_MACHINE)
        .setLabel("Stop")
        .setSize(100, 100)
        .setColorLabel(component_label_color)
        .setFont(text_font_default);

    bitId = getControl()
        .addTextlabel(BIT_ID_LABEL)
        .setText("Identifier of Bit")
        .setColor(component_label_color)
        .setFont(createFont("arial bold", 20));
    nbSubBit = getControl()
        .addTextlabel(NUMBER_SUB_BIT_LABEL)
        .setText("Number of sub bit")
        .setColor(component_label_color)
        .setFont(createFont("arial bold", 20));
    layerId = getControl()
        .addTextlabel(LAYER_ID_LABEL)
        .setText("Layer ID")
        .setColor(component_label_color)
        .setFont(createFont("arial bold", 20));
    position = getControl()
        .addTextlabel(POSITION_BIT_LABEL)
        .setText("Position of Bit")
        .setColor(component_label_color)
        .setFont(createFont("arial bold", 20));
    initComponentPositions();
  }

  public void setShape(PShape shape) {
    shapeBit = shape;
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
    if (shapeBit != null) {
      shape(shapeBit);
    }
    if (processor.isInProcess()) {
      circleState.setFill(color(0, 255, 0));
    } else {
      circleState.setFill(color(255, 0, 0));
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
