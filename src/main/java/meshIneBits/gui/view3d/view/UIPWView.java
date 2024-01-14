package meshIneBits.gui.view3d.view;

import controlP5.*;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.oldversion.ProcessingModelView;
import processing.core.PApplet;
import processing.core.PFont;

import java.util.ArrayList;
import java.util.Arrays;

import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.*;

public class UIPWView extends UIParameterWindow implements
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
  private Textarea shortcut;
  private Textlabel slicingWarning;

  private Textarea tooltipRotation;
  private Textarea tooltipGravity;
  private Textarea tooltipReset;
  private Textarea tooltipCamera;
  private Textarea tooltipApply;

  private final ArrayList<Tooltip> tooltipsToShow = new ArrayList<>();


  private double currentX;
  private double currentY;
  private double currentZ;
  private double currentScale;
  private double currentDepth;
  private double currentWidth;
  private double currentHeight;


  @Override
  public void setup() {
    super.setup();
    surface.setLocation(Visualization3DConfig.UIPW_VIEW.x, Visualization3DConfig.UIPW_VIEW.y);
  }

  private void initComponentPositions() {
    float[] rotationX = new float[]{0.09f * width, 0.23f * height};
    float[] rotationY = new float[]{0.09f * width, 0.29f * height};
    float[] rotationZ = new float[]{0.09f * width, 0.35f * height};
    TFRotationX.setPosition(rotationX);
    TFRotationY.setPosition(rotationY);
    TFRotationZ.setPosition(rotationZ);

    float[] positionX = new float[]{0.39f * width, 0.23f * height};
    float[] positionY = new float[]{0.39f * width, 0.29f * height};
    float[] positionZ = new float[]{0.39f * width, 0.35f * height};
    TFPositionX.setPosition(positionX);
    TFPositionY.setPosition(positionY);
    TFPositionZ.setPosition(positionZ);

    float[] gravityPosition = new float[]{0.09f * width, 0.6f * height};
    float[] resetPosition = new float[]{0.09f * width, 0.65f * height};
    float[] cameraPosition = new float[]{0.09f * width, 0.7f * height};
    float[] applyPosition = new float[]{0.09f * width, 0.75f * height};
    gravity.setPosition(gravityPosition);
    reset.setPosition(resetPosition);
    camera.setPosition(cameraPosition);
    apply.setPosition(applyPosition);

    toggleViewMesh.setPosition(0.09f * width, 0.49f * height);
    modelSize.setPosition(0.09f * width, 0.07f * height);
    txt.setPosition(0.45f * width, 0.07f * height);
    slicingWarning.setPosition(0.09f * width, 0.58f * height);
    shortcut.setPosition(0.09f * width, 0.79f * height);
    modelPosition.setPosition(0.09f * width, 0.95f * height);

    for (Tooltip tooltip : tooltipsToShow) {
      float[] position = tooltip.positionOfComponent();
      float[] size = tooltip.sizeOfComponent();
      tooltip.setTooltipPosition(new float[]{position[0] + size[0], position[1]});
    }
  }

  @Override
  protected void updateButton() {
//        PSurfaceAWT win = ((GLWindow) surface.getNative());

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
        .setFont(text_font_default);

    modelSize = getControl()
        .addTextlabel("model size")
        .setText(
            "Model Size :\n Depth:" + currentDepth + "\n Height :" + currentHeight + "\n Width : "
                + currentWidth + "\n Scale : " + currentScale)
        .setColor(255)
        .setFont(text_font_default);

    modelPosition = getControl()
        .addTextlabel("model position")
        .setText("Model Position in \n Printing Space ")
        .setColor(component_label_color)
        .setFont(createFont("arial bold", 20));

    shortcut = getControl()
        .addTextarea("shortcut")
        .setText(
            "Shortcut : \n Rotation : CTRL + Mouse Left Click, Cannot be used when Project is sliced \n Translation : CTRL + Mouse Right Click \n Change Model Size : Mouse on the Model + Mouse Wheel , Cannot be used when Project is sliced\n Zoom : Mouse Wheel\n Export to Obj: press button 'S'")
        .setColor(component_label_color)
        .setFont(text_font_default)
        .hideScrollbar();

    slicingWarning = getControl()
        .addTextlabel("slicingWarning")
        .setText("The Model is Sliced \n You can't rotate \n You can't scale")
        .setColor(component_label_color)
        .setFont(createFont("arial bold", 20))
        .hide();

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

    Textarea rotationTooltipTextarea = getControl()
        .addTextarea("tooltipRotation")
        .setText("You can't rotate a sliced Model")
        .setSize(220, 36)
        .setColorBackground(color(255, 0, 0))
        .setColor(color(50))
        .setFont(text_font_default)
        .setLineHeight(12)
        .hide()
        .hideScrollbar();

    rotationTooltipTextarea.getValueLabel()
        .getStyle()
        .setMargin(1, 0, 0, 5);

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
      logger.logWARNMessage(
          "This window " + this.getClass()
              .getName() + " need to be setted a listener!!!");
      println(theEvent.getName());
      System.out.println(theEvent.getValue());
      System.out.println(theEvent.getStringValue());
      return;
    }
    switch (theEvent.getName()) {
      case ROTATION_X:
      case ROTATION_Y:
      case ROTATION_Z:
      case POSITION_X:
      case POSITION_Y:
      case POSITION_Z:
        getListener().onActionListener(this,theEvent.getName(),
            Float.parseFloat(theEvent.getStringValue()));
        break;
      case VIEW_MESH:
        getListener().onActionListener(this,theEvent.getName(), (float) 1.0 == theEvent.getValue());
        break;
      case APPLY:
      case GRAVITY:
      case RESET:
      case CENTER_CAMERA:
        getListener().onActionListener(this,theEvent.getName(), theEvent.getValue());
        break;
      default:
        logger.logWARNMessage("The event invoked is not handled by method ControlEvent");
        break;
    }
  }

  public static void main(String[] args) {
    PApplet.main(UIPWView.class.getCanonicalName());
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
    if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
      for (Tooltip tooltip : tooltipsToShow) {
        boolean isFocused = tooltip.mouseEntered(mouseX, mouseY);
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
