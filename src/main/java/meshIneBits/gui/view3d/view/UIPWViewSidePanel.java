package meshIneBits.gui.view3d.view;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Textfield;
import controlP5.Textlabel;
import controlP5.Toggle;
import meshIneBits.gui.view3d.oldversion.ProcessingModelView.ModelChangesListener;
import processing.core.PFont;
import processing.core.PApplet;

import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.*;

/**
 * "View Configuration" side panel integrated into the same PApplet (no top-level window).
 *
 * Builds and updates ControlP5 controls, then routes events to {@link UIPWController}.
 */
public class UIPWViewSidePanel implements ModelChangesListener {

  private final PApplet app;
  private final ControlP5 cp5;
  private final UIPWController controller;

  private final float panelWidth;
  private final float panelHeight;
  private final float originX;
  private final float originY;

  private Textfield tfRotationX;
  private Textfield tfRotationY;
  private Textfield tfRotationZ;
  private Textfield tfPositionX;
  private Textfield tfPositionY;
  private Textfield tfPositionZ;

  private Toggle toggleViewMesh;
  private Button gravity;
  private Button reset;
  private Button camera;
  private Button apply;

  private Textlabel modelSize;
  private Textlabel currentPositionText;

  // Updated via ModelChangesListener
  private double currentX;
  private double currentY;
  private double currentZ;
  private double currentScale;
  private double currentDepth;
  private double currentWidth;
  private double currentHeight;

  public UIPWViewSidePanel(PApplet app, ControlP5 cp5, UIPWController controller,
      float panelWidth, float panelHeight, float originX, float originY) {
    this.app = app;
    this.cp5 = cp5;
    this.controller = controller;
    this.panelWidth = panelWidth;
    this.panelHeight = panelHeight;
    this.originX = originX;
    this.originY = originY;
    build();
  }

  private void build() {
    int color = 255;
    int componentBackgroundColor = app.color(255, 250);
    int componentLabelColor = 255;

    // Fonts
    PFont font = app.createFont("arial bold", 15);

    // Rotation textfields
    tfRotationX = cp5.addTextfield(ROTATION_X)
        .setSize(45, 30)
        .setInputFilter(0)
        .setColorBackground(componentBackgroundColor)
        .setColor(0)
        .setColorLabel(componentLabelColor)
        .setAutoClear(false)
        .setColorCursor(0)
        .setFont(font);
    tfRotationY = cp5.addTextfield(ROTATION_Y)
        .setSize(45, 30)
        .setInputFilter(0)
        .setColorBackground(componentBackgroundColor)
        .setColor(0)
        .setColorLabel(componentLabelColor)
        .setAutoClear(false)
        .setColorCursor(0)
        .setFont(font);
    tfRotationZ = cp5.addTextfield(ROTATION_Z)
        .setSize(45, 30)
        .setInputFilter(0)
        .setColorBackground(componentBackgroundColor)
        .setColor(0)
        .setColorLabel(componentLabelColor)
        .setAutoClear(false)
        .setColorCursor(0)
        .setFont(font);

    // Position textfields
    tfPositionX = cp5.addTextfield(POSITION_X)
        .setSize(45, 30)
        .setInputFilter(0)
        .setColorBackground(componentBackgroundColor)
        .setColor(0)
        .setColorLabel(componentLabelColor)
        .setAutoClear(false)
        .setColorCursor(0)
        .setFont(font);
    tfPositionY = cp5.addTextfield(POSITION_Y)
        .setSize(45, 30)
        .setInputFilter(0)
        .setColorBackground(componentBackgroundColor)
        .setColor(0)
        .setColorLabel(componentLabelColor)
        .setAutoClear(false)
        .setColorCursor(0)
        .setFont(font);
    tfPositionZ = cp5.addTextfield(POSITION_Z)
        .setSize(45, 30)
        .setInputFilter(0)
        .setColorBackground(componentBackgroundColor)
        .setColor(0)
        .setColorLabel(componentLabelColor)
        .setAutoClear(false)
        .setColorCursor(0)
        .setFont(font);

    // Toggle
    toggleViewMesh = cp5.addToggle(VIEW_MESH)
        .setSize(20, 20)
        .setColorBackground(componentBackgroundColor)
        .setColorActive(color)
        .setColorForeground(color + 50)
        .setFont(font);

    // Buttons
    gravity = cp5.addButton(GRAVITY).setSize(140, 30).setFont(font);
    reset = cp5.addButton(RESET).setSize(140, 30).setFont(font);
    camera = cp5.addButton(CENTER_CAMERA).setSize(140, 30).setFont(font);
    apply = cp5.addButton(APPLY).setSize(140, 30).setFont(font);

    // Labels
    modelSize = cp5.addTextlabel("modelSize")
        .setText("")
        .setFont(font);
    currentPositionText = cp5.addTextlabel("currentPosition")
        .setText("")
        .setFont(font);

    initLayout();
    attachCallbacks();
    update();
  }

  private void attachCallbacks() {
    // Textfields dispatch their float value to controller
    tfRotationX.onRelease(e -> dispatchFloatEvent(ROTATION_X, e.getController().getStringValue()));
    tfRotationY.onRelease(e -> dispatchFloatEvent(ROTATION_Y, e.getController().getStringValue()));
    tfRotationZ.onRelease(e -> dispatchFloatEvent(ROTATION_Z, e.getController().getStringValue()));
    tfPositionX.onRelease(e -> dispatchFloatEvent(POSITION_X, e.getController().getStringValue()));
    tfPositionY.onRelease(e -> dispatchFloatEvent(POSITION_Y, e.getController().getStringValue()));
    tfPositionZ.onRelease(e -> dispatchFloatEvent(POSITION_Z, e.getController().getStringValue()));

    // Toggle + buttons
    toggleViewMesh.onClick(e -> controller.onActionListener(this, VIEW_MESH,
        e.getController().getValue() == 1.0f));
    apply.onRelease(e -> controller.onActionListener(this, APPLY, e.getController().getValue()));
    gravity.onRelease(e -> controller.onActionListener(this, GRAVITY, e.getController().getValue()));
    reset.onRelease(e -> controller.onActionListener(this, RESET, e.getController().getValue()));
    camera.onRelease(e -> controller.onActionListener(this, CENTER_CAMERA, e.getController().getValue()));
  }

  private void dispatchFloatEvent(String eventName, String rawValue) {
    if (controller == null) {
      return;
    }
    try {
      controller.onActionListener(this, eventName, Float.parseFloat(rawValue));
    } catch (NumberFormatException ignored) {
      // Ignore malformed textfield content; user can correct and release again.
    }
  }

  private void initLayout() {
    // Rotation
    tfRotationX.setPosition(originX + 0.09f * panelWidth, originY + 0.23f * panelHeight);
    tfRotationY.setPosition(originX + 0.09f * panelWidth, originY + 0.29f * panelHeight);
    tfRotationZ.setPosition(originX + 0.09f * panelWidth, originY + 0.35f * panelHeight);

    // Position
    tfPositionX.setPosition(originX + 0.39f * panelWidth, originY + 0.23f * panelHeight);
    tfPositionY.setPosition(originX + 0.39f * panelWidth, originY + 0.29f * panelHeight);
    tfPositionZ.setPosition(originX + 0.39f * panelWidth, originY + 0.35f * panelHeight);

    // Toggle
    toggleViewMesh.setPosition(originX + 0.09f * panelWidth, originY + 0.49f * panelHeight);

    // Buttons
    gravity.setPosition(originX + 0.09f * panelWidth, originY + 0.60f * panelHeight);
    reset.setPosition(originX + 0.09f * panelWidth, originY + 0.65f * panelHeight);
    camera.setPosition(originX + 0.09f * panelWidth, originY + 0.70f * panelHeight);
    apply.setPosition(originX + 0.09f * panelWidth, originY + 0.75f * panelHeight);

    // Labels
    modelSize.setPosition(originX + 0.09f * panelWidth, originY + 0.07f * panelHeight);
    currentPositionText.setPosition(originX + 0.45f * panelWidth, originY + 0.07f * panelHeight);
  }

  public void update() {
    if (modelSize != null) {
      modelSize.setText(
          "Model Size :\n" +
              " Depth : " + currentDepth + "\n" +
              " Height : " + currentHeight + "\n" +
              " Width : " + currentWidth + "\n" +
              " Scale  : " + currentScale
      );
    }
    if (currentPositionText != null) {
      currentPositionText.setText(
          "Current position :\n" +
              " x : " + currentX + "\n" +
              " y : " + currentY + "\n" +
              " z : " + currentZ
      );
    }
  }

  /**
   * À appeler depuis {@code BaseVisualization3DView.controlEvent(...)}.
   */
  public void onControlEvent(ControlEvent theEvent) {
    // Legacy fallback, intentionally kept no-op now that callbacks are local on each control.
  }

  @Override
  public void onSizeChange(double scale, double dept, double width, double height) {
    currentScale = scale;
    currentDepth = dept;
    currentWidth = width;
    currentHeight = height;
    update();
  }

  @Override
  public void onPositionChange(double x, double y, double z) {
    currentX = x;
    currentY = y;
    currentZ = z;
    update();
  }

  @Override
  public void onRotationChange(double x, double y, double z) {
    // no-op
  }
}

