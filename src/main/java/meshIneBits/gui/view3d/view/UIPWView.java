package meshIneBits.gui.view3d.view;

import controlP5.*;
import meshIneBits.gui.view3d.oldversion.ProcessingModelView;
import processing.core.PFont;

import java.util.ArrayList;
import java.util.Arrays;

import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.*;

public class UIPWView extends UIParameterPanel implements
    ProcessingModelView.ModelChangesListener {

  private Textfield TFRotationX;
  private Textfield TFRotationY;
  private Textfield TFRotationZ;
  private Textfield TFPositionX;
  private Textfield TFPositionY;
  private Textfield TFPositionZ;
  private Toggle toggleViewMesh;
  private Button gravity;
  private Button reset;
  private Button camera;
  private Button apply;

  private Textlabel txt;
  private Textlabel modelPosition;
  private Textlabel modelSize;
  private Textlabel shortcut;
  private Textlabel slicingWarning;

  private final ArrayList<Tooltip> tooltipsToShow = new ArrayList<>();

  private double currentX;
  private double currentY;
  private double currentZ;
  private double currentScale;
  private double currentDepth;
  private double currentWidth;
  private double currentHeight;

  public UIPWView(processing.core.PApplet parent, ControlP5 control, UIPWListener listener,
      float originX, float originY, float panelWidth, float panelHeight) {
    super(parent, control, listener, originX, originY, panelWidth, panelHeight);
  }

  private void initComponentPositions() {
    int fieldW = panelSizeW(0.18f);
    int btnW = panelSizeW(0.55f);
    int labelW = panelSizeW(0.85f);
    int labelSmallW = panelSizeW(0.45f);

    TFRotationX.setPosition(px(0.09f), py(0.23f)).setSize(fieldW, 30);
    TFRotationY.setPosition(px(0.09f), py(0.29f)).setSize(fieldW, 30);
    TFRotationZ.setPosition(px(0.09f), py(0.35f)).setSize(fieldW, 30);

    TFPositionX.setPosition(px(0.39f), py(0.23f)).setSize(fieldW, 30);
    TFPositionY.setPosition(px(0.39f), py(0.29f)).setSize(fieldW, 30);
    TFPositionZ.setPosition(px(0.39f), py(0.35f)).setSize(fieldW, 30);

    gravity.setPosition(px(0.09f), py(0.6f)).setSize(btnW, 30);
    reset.setPosition(px(0.09f), py(0.65f)).setSize(btnW, 30);
    camera.setPosition(px(0.09f), py(0.7f)).setSize(btnW, 30);
    apply.setPosition(px(0.09f), py(0.75f)).setSize(btnW, 30);

    toggleViewMesh.setPosition(px(0.09f), py(0.49f)).setSize(20, 20);
    modelSize.setPosition(px(0.09f), py(0.07f)).setSize(labelW, panelSizeH(0.14f));
    txt.setPosition(px(0.45f), py(0.07f)).setSize(labelSmallW, panelSizeH(0.12f));
    slicingWarning.setPosition(px(0.09f), py(0.58f)).setSize(labelW, panelSizeH(0.08f));
    shortcut.setPosition(px(0.09f), py(0.79f)).setSize(labelW, panelSizeH(0.16f));
    modelPosition.setPosition(px(0.09f), py(0.95f)).setSize(labelW, panelSizeH(0.05f));

    for (Tooltip tooltip : tooltipsToShow) {
      float[] position = tooltip.positionOfComponent();
      float[] size = tooltip.sizeOfComponent();
      float tooltipX = clampLocalX(position[0] + size[0], tooltip.getTooltipText().getWidth());
      tooltip.setTooltipPosition(new float[]{tooltipX, position[1]});
    }
  }

  @Override
  protected void relayout() {
    initComponentPositions();
  }

  @Override
  protected void updateButton() {
    modelSize.setText(
        "Model Size :\n Depth : " + currentDepth + "\n Height : " + currentHeight + "\n Width : "
            + currentWidth + "\n Scale  : " + currentScale);
    txt.setText(
        "Current position :\n" + " x : " + currentX + "\n y : " + currentY + "\n z : " + currentZ);

    displayTooltips();
  }

  @Override
  public void onOpen() {
  }

  @Override
  public void onClose() {
  }

  @Override
  protected void generateButton() {
    int color = 255;
    PFont text_font_default = createFont("arial bold", 15);
    int component_background_color = color(255, 250);
    int component_label_color = 255;

    TFRotationX = getControl()
        .addTextfield(ROTATION_X)
        .setSize(45, 30)
        .setInputFilter(0)
        .setColorBackground(component_background_color)
        .setColor(0)
        .setColorLabel(component_label_color)
        .setAutoClear(false)
        .setColorCursor(0)
        .setFont(text_font_default);

    TFRotationY = getControl().addTextfield(ROTATION_Y)
        .setSize(45, 30)
        .setInputFilter(0)
        .setColorBackground(component_background_color)
        .setColor(0)
        .setColorLabel(component_label_color)
        .setAutoClear(false)
        .setColorCursor(0)
        .setFont(text_font_default);

    TFRotationZ = getControl()
        .addTextfield(ROTATION_Z)
        .setSize(45, 30)
        .setInputFilter(0)
        .setColorBackground(component_background_color)
        .setColor(0)
        .setColorLabel(component_label_color)
        .setAutoClear(false)
        .setColorCursor(0)
        .setFont(text_font_default);

    TFPositionX = getControl()
        .addTextfield(POSITION_X)
        .setSize(45, 30)
        .setInputFilter(0)
        .setColorBackground(component_background_color)
        .setColor(0)
        .setColorLabel(component_label_color)
        .setAutoClear(false)
        .setColorCursor(0)
        .setFont(text_font_default);

    TFPositionY = getControl().addTextfield(POSITION_Y)
        .setSize(45, 30)
        .setInputFilter(0)
        .setColorBackground(component_background_color)
        .setColor(0)
        .setColorLabel(component_label_color)
        .setAutoClear(false)
        .setColorCursor(0)
        .setFont(text_font_default);

    TFPositionZ = getControl()
        .addTextfield(POSITION_Z)
        .setSize(45, 30)
        .setInputFilter(0)
        .setColorBackground(component_background_color)
        .setColor(0)
        .setColorLabel(component_label_color)
        .setAutoClear(false)
        .setColorCursor(0)
        .setFont(text_font_default);

    toggleViewMesh = getControl()
        .addToggle(VIEW_MESH)
        .setSize(20, 20)
        .setColorBackground(component_background_color)
        .setColorActive(color)
        .setColorForeground(color + 50)
        .setFont(text_font_default);

    apply = getControl()
        .addButton(APPLY)
        .setSize(140, 30)
        .setColorLabel(component_label_color)
        .setFont(text_font_default);

    gravity = getControl()
        .addButton(GRAVITY)
        .setSize(140, 30)
        .setColorLabel(component_label_color)
        .setFont(createFont("arial bold", 15, false));

    reset = getControl()
        .addButton(RESET)
        .setSize(140, 30)
        .setColorLabel(component_label_color)
        .setFont(text_font_default);

    camera = getControl()
        .addButton(CENTER_CAMERA)
        .setSize(140, 30)
        .setColorLabel(component_label_color)
        .setFont(text_font_default);

    txt = getControl()
        .addTextlabel("label")
        .setText("Current Position : (0,0,0)")
        .setSize(80, 40)
        .setColor(255)
        .setFont(text_font_default)
        .setLock(true);

    modelSize = getControl()
        .addTextlabel("model size")
        .setText(
            "Model Size :\n Depth:" + currentDepth + "\n Height :" + currentHeight + "\n Width : "
                + currentWidth + "\n Scale : " + currentScale)
        .setColor(255)
        .setFont(text_font_default)
        .setLock(true);

    modelPosition = getControl()
        .addTextlabel("model position")
        .setText("Model Position in \n Printing Space ")
        .setColor(component_label_color)
        .setFont(createFont("arial bold", 20))
        .setLock(true);

    shortcut = getControl()
        .addTextlabel("shortcut")
        .setText(
            "Shortcut : \n Rotation : CTRL + Mouse Left Click, Cannot be used when Mesh is sliced \n Translation : CTRL + Mouse Right Click \n Change Model Size : Mouse on the Model + Mouse Wheel , Cannot be used when Mesh is sliced\n Zoom : Mouse Wheel\n Export to Obj: press button 'S'")
        .setColor(component_label_color)
        .setFont(text_font_default)
        .setLock(true);

    slicingWarning = getControl()
        .addTextlabel("slicingWarning")
        .setText("The Model is Sliced \n You can't rotate \n You can't scale")
        .setColor(component_label_color)
        .setFont(createFont("arial bold", 20))
        .hide()
        .setLock(true);

    Textarea gravityTooltipTextarea = getControl()
        .addTextarea("tooltipGravity")
        .setText("Set the model")
        .setSize(90, 18)
        .setColorBackground(color(220))
        .setColor(color(50))
        .setFont(createFont("arial bold", 10))
        .setLineHeight(12)
        .hide()
        .hideScrollbar();

    gravityTooltipTextarea.getValueLabel()
        .getStyle()
        .setMargin(1, 0, 0, 5);
    gravityTooltipTextarea.setMousePressed(false);

    Textarea resetTooltipTextarea = getControl()
        .addTextarea("tooltipReset")
        .setText("Reset to zero")
        .setSize(85, 18)
        .setColorBackground(color(220))
        .setColor(color(50))
        .setFont(createFont("arial bold", 10))
        .setLineHeight(12)
        .hide()
        .hideScrollbar();

    resetTooltipTextarea.getValueLabel()
        .getStyle()
        .setMargin(1, 0, 0, 5);
    resetTooltipTextarea.setMousePressed(false);

    Textarea cameraTooltipTextarea = getControl()
        .addTextarea("tooltipCamera")
        .setText("Center model")
        .setSize(105, 18)
        .setColorBackground(color(220))
        .setColor(color(50))
        .setFont(createFont("arial bold", 10))
        .setLineHeight(12)
        .hide()
        .hideScrollbar();

    cameraTooltipTextarea.getValueLabel()
        .getStyle()
        .setMargin(1, 0, 0, 5);
    cameraTooltipTextarea.setMousePressed(false);

    Textarea applyTooltipTextarea = getControl()
        .addTextarea("tooltipApply")
        .setText("Apply the modifications")
        .setSize(145, 18)
        .setColorBackground(color(220))
        .setColor(color(50))
        .setFont(createFont("arial bold", 10))
        .setLineHeight(12)
        .hide()
        .hideScrollbar();

    applyTooltipTextarea.getValueLabel()
        .getStyle()
        .setMargin(1, 0, 0, 5);
    applyTooltipTextarea.setMousePressed(false);

    Tooltip<Textarea, Button> gravityTooltip = new Tooltip<>(gravityTooltipTextarea, gravity);
    Tooltip<Textarea, Button> resetTooltip = new Tooltip<>(resetTooltipTextarea, reset);
    Tooltip<Textarea, Button> cameraTooltip = new Tooltip<>(cameraTooltipTextarea, camera);
    Tooltip<Textarea, Button> applyTooltip = new Tooltip<>(applyTooltipTextarea, apply);

    tooltipsToShow.addAll(Arrays.asList(gravityTooltip, resetTooltip, cameraTooltip, applyTooltip));

    initComponentPositions();
  }

  @Override
  public void controlEvent(ControlEvent theEvent) {
    if (getListener() == null) {
      return;
    }
    switch (theEvent.getName()) {
      case ROTATION_X:
      case ROTATION_Y:
      case ROTATION_Z:
      case POSITION_X:
      case POSITION_Y:
      case POSITION_Z:
        getListener().onActionListener(this, theEvent.getName(),
            Float.parseFloat(theEvent.getStringValue()));
        break;
      case VIEW_MESH:
        getListener().onActionListener(this, theEvent.getName(), (float) 1.0 == theEvent.getValue());
        break;
      case APPLY:
      case GRAVITY:
      case RESET:
      case CENTER_CAMERA:
        getListener().onActionListener(this, theEvent.getName(), theEvent.getValue());
        break;
      default:
        break;
    }
  }

  @Override
  public void onSizeChange(double scale, double dept, double width, double height) {
    currentScale = scale;
    currentDepth = dept;
    currentWidth = width;
    currentHeight = height;
  }

  @Override
  public void onPositionChange(double x, double y, double z) {
    currentX = x;
    currentY = y;
    currentZ = z;
  }

  @Override
  public void onRotationChange(double x, double y, double z) {

  }

  private void displayTooltips() {
    hideTooltip();
    Tooltip tooltipToShow = getTooltipsEnteredMouse();
    if (tooltipToShow != null) {
      tooltipToShow.showTooltip(true);
    }
  }

  public Tooltip getTooltipsEnteredMouse() {
    if ((pmouseX() - mouseX()) == 0 && (pmouseY() - mouseY()) == 0) {
      for (Tooltip tooltip : tooltipsToShow) {
        boolean isFocused = tooltip.mouseEntered(mouseX(), mouseY());
        if (isFocused) {
          return tooltip;
        }
      }
    }
    return null;
  }

  private void hideTooltip() {
    tooltipsToShow.forEach((tooltip -> {
      if (tooltip.getTooltipText().isVisible()) {
        tooltip.showTooltip(false);
      }
    }));
  }
}
