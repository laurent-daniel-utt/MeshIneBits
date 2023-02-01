package meshIneBits.gui.view3d.view;

import controlP5.*;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.util.animation.AnimationIndexIncreasedListener;
import processing.core.PApplet;
import processing.core.PFont;

import java.util.ArrayList;
import java.util.Arrays;

import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.*;

public class UIPWAnimation extends UIParameterWindow implements
    AnimationIndexIncreasedListener {

  private Button exportAll;
  public static Button Animation;
  private Button export;
  private Toggle toggleSubBit;
  private Toggle toggleBits;
  private Toggle toggleBatch;
  private Toggle toggleLayers;
  private Toggle toggleCurrent;
  private Toggle toggleFull;
  private Slider sliderAnimation;
  private Button speedUpButton;
  private Button speedDownButton;

  private Button next;
  private Button previous;
  private Button pauseButton;

  private boolean pausing = false;
  private boolean animating = false;

  private final ArrayList<Tooltip> tooltipsToShow = new ArrayList<>();

//  public void settings() {
//    size(SCREEN_DIMENSION.width / 5, SCREEN_DIMENSION.height - 60, P3D);
//  }

  @Override
  public void setup() {
    super.setup();
    surface.setLocation(
        Visualization3DConfig.UIPW_ANIMATION.x,
        Visualization3DConfig.UIPW_ANIMATION.y);
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
    PFont font = createFont("arial bold", 15);
    Animation = getControl().addButton(ANIMATION)
        .setSize(140, 30)
        .setColorLabel(255)
        .setFont(font);
    export = getControl().addButton(EXPORT)
        .setSize(140, 30)
        .setColorLabel(255)
        .setFont(font);
    exportAll= getControl().addButton(EXPORTAll)// MH
            .setSize(140, 30)
            .setColorLabel(255)
            .setFont(font);
    toggleSubBit = getControl().addToggle(BY_SUB_BIT)
        .setSize(20, 20)
        .setColorBackground(color(255, 250))
        .setColorActive(color)
        .setColorForeground(color + 50)
        .setLabel(BY_SUB_BIT)
        .setFont(font)
        .onClick((e) -> {
          if (e.getController()
              .getValue() == 1.0) {
            toggleLayers.setState(false);
            toggleBatch.setState(false);
            toggleBits.setState(false);
          }
        });

    toggleBits = getControl().addToggle(BY_BIT)
        .setSize(20, 20)
        .setColorBackground(color(255, 250))
        .setColorActive(color)
        .setColorForeground(color + 50)
        .setLabel(BY_BIT)
        .setFont(font)
        .onClick((e) -> {
          if (e.getController()
              .getValue() == 1.0) {
            toggleLayers.setState(false);
            toggleBatch.setState(false);
            toggleSubBit.setState(false);
          }
        });

    toggleBatch = getControl().addToggle(BY_BATCH)
        .setSize(20, 20)
        .setColorBackground(color(255, 250))
        .setColorActive(color)
        .setColorForeground(color + 50)
        .setLabel(BY_BATCH)
        .setFont(font)
        .onClick((e) -> {
          if (e.getController()
              .getValue() == 1.0) {
            toggleLayers.setState(false);
            toggleBits.setState(false);
            toggleSubBit.setState(false);
          }
        });

    toggleLayers = getControl().addToggle(BY_LAYER)
        .setSize(20, 20)
        .setColorBackground(color(255, 250))
        .setColorActive(color)
        .setColorForeground(color + 50)
        .setLabel(BY_LAYER)
        .setFont(font)
        .onClick((e) -> {
          if (e.getController()
              .getValue() == 1.0) {
            toggleBits.setState(false);
            toggleBatch.setState(false);
            toggleSubBit.setState(false);
          }
        });
    toggleLayers.setState(true);

    toggleCurrent = getControl().addToggle(ONE_BY_ONE)
        .setSize(20, 20)
        .setColorBackground(color(255, 250))
        .setColorActive(color)
        .setColorForeground(color + 50)
        .setLabel(ONE_BY_ONE)
        .setFont(font)
        .onClick((e) -> {
          if (e.getController()
              .getValue() == 1.0) {
            toggleFull.setState(false);
          }
        })
        .setState(false);
    toggleFull = getControl().addToggle(FULL)
        .setSize(20, 20)
        .setColorBackground(color(255, 250))
        .setColorActive(color)
        .setColorForeground(color + 50)
        .setLabel(FULL)
        .setFont(font)
        .onClick((e) -> {
          if (e.getController()
              .getValue() == 1.0) {
            toggleCurrent.setState(false);
          }
        })
        .setState(true);

    sliderAnimation = getControl().addSlider(ANIMATION_SLICER);
    sliderAnimation.setVisible(true)
        .setSize(200, 30)
        .getCaptionLabel()
        .setFont(font);

    speedUpButton = getControl().addButton(SPEED_UP)
        .setVisible(true)
        .setSize(30, 30)
        .setColorLabel(255)
        .setFont(font);
    speedUpButton.getCaptionLabel()
        .setText(">>");
    speedDownButton = getControl().addButton(SPEED_DOWN)
        .setVisible(true)
        .setSize(30, 30)
        .setColorLabel(255)
        .setFont(font);
    speedDownButton.getCaptionLabel()
        .setText("<<");

    next = getControl().addButton(NEXT)
            .setVisible(true)
            .setSize(30, 30)
            .setColorLabel(255)
            .setFont(font);
    next.getCaptionLabel()
            .setText(">");

    previous = getControl().addButton(PREVIOUS)
            .setVisible(true)
            .setSize(30, 30)
            .setColorLabel(255)
            .setFont(font);
    previous.getCaptionLabel()
            .setText("<");

    pauseButton = getControl().addButton(PAUSE)
        .setVisible(true)
        .setSize(50, 30)
        .setColorLabel(255);
    pauseButton.getCaptionLabel()
        .setText(PAUSE)
        .setFont(font);



    PFont tooltipFont = createFont("arial bold", 10);

    Textarea exportTooltipTextare = getControl()
        .addTextarea("tooltipExport")
        .setText("Export to OBJ")
        .setSize(145, 18)
        .setColorBackground(color(220))
        .setColor(color(50)).setFont(tooltipFont).setLineHeight(12).hide()
        .hideScrollbar();
    exportTooltipTextare.getValueLabel()
        .getStyle()
        .setMargin(1, 0, 0, 5);

    Textarea exportAllTooltipTextare = getControl()
            .addTextarea("tooltipExportAll")
            .setText("Export All from chosen option to OBJ")
            .setSize(52, 80)
            .setColorBackground(color(220))
            .setColor(color(50)).setFont(tooltipFont).setLineHeight(12).hide()
            .hideScrollbar();
    exportTooltipTextare.getValueLabel()
            .getStyle()
            .setMargin(1, 0, 0, 5);


    Textarea SpeedUpTooltipTextare = getControl()
            .addTextarea("tooltSpeedUp")
            .setText("Speed up")
            .setSize(145, 18)
            .setColorBackground(color(220))
            .setColor(color(50)).setFont(tooltipFont).setLineHeight(12).hide()
            .hideScrollbar();
            SpeedUpTooltipTextare.getValueLabel()
            .getStyle()
            .setMargin(1, 0, 0, 5);


    Textarea SpeedDownTooltipTextare = getControl()
            .addTextarea("tooltSpeeddown")
            .setText("Speed down")
            .setSize(145, 18)
            .setColorBackground(color(220))
            .setColor(color(50)).setFont(tooltipFont).setLineHeight(12).hide()
            .hideScrollbar();
    SpeedUpTooltipTextare.getValueLabel()
            .getStyle()
            .setMargin(1, 0, 0, 5);


    Textarea NextStepTooltipTextare = getControl()
            .addTextarea("tooltNext")
            .setText("Next")
            .setSize(145, 18)
            .setColorBackground(color(220))
            .setColor(color(50)).setFont(tooltipFont).setLineHeight(12).hide()
            .hideScrollbar();
    SpeedUpTooltipTextare.getValueLabel()
            .getStyle()
            .setMargin(1, 0, 0, 5);

    Textarea PreviousStepTooltipTextare = getControl()
            .addTextarea("tooltPrevious")
            .setText("Previous")
            .setSize(145, 18)
            .setColorBackground(color(220))
            .setColor(color(50)).setFont(tooltipFont).setLineHeight(12).hide()
            .hideScrollbar();
    SpeedUpTooltipTextare.getValueLabel()
            .getStyle()
            .setMargin(1, 0, 0, 5);

    Textarea bitTooltipTextarea = getControl()
        .addTextarea("tooltipBits")
        .setText("Animation/Export by Bits")
        .setSize(145, 18)
        .setColorBackground(color(220))
        .setColor(color(50)).setFont(tooltipFont).setLineHeight(12).hide()
        .hideScrollbar();
    bitTooltipTextarea.getValueLabel()
        .getStyle()
        .setMargin(1, 0, 0, 5);

    Textarea batchTooltipTextarea = getControl()
        .addTextarea("tooltipBatch")
        .setText("Animation/Export by Batch")
        .setSize(145, 18)
        .setColorBackground(color(220))
        .setColor(color(50)).setFont(tooltipFont).setLineHeight(12).hide()
        .hideScrollbar();
    batchTooltipTextarea.getValueLabel()
        .getStyle()
        .setMargin(1, 0, 0, 5);

    Textarea layerTooltipTextarea = getControl().addTextarea("tooltipLayers")
        .setText("Animation/Export by Layers")
        .setSize(150, 18)
        .setColorBackground(color(220))
        .setColor(color(50)).setFont(tooltipFont).setLineHeight(12).hide()
        .hideScrollbar();
    layerTooltipTextarea.getValueLabel()
        .getStyle()
        .setMargin(1, 0, 0, 5);

    Textarea currentTooltipTextarea = getControl().addTextarea("tooltipCurrent")
        .setText("Animation/Export \n one by one")
        .setSize(145, 36)
        .setColorBackground(color(220))
        .setColor(color(50)).setFont(tooltipFont).setLineHeight(12).hide()
        .hideScrollbar();
    currentTooltipTextarea.getValueLabel()
        .getStyle()
        .setMargin(1, 0, 0, 5);

    Textarea fullTooltipTextarea = getControl().addTextarea("tooltipFull")
        .setText("Animation/Export \n the evolution")
        .setSize(145, 36)
        .setColorBackground(color(220))
        .setColor(color(50)).setFont(tooltipFont).setLineHeight(12).hide()
        .hideScrollbar();
    fullTooltipTextarea.getValueLabel()
        .getStyle()
        .setMargin(1, 0, 0, 5);

    Tooltip<Textarea, Toggle> batchTooltip = new Tooltip<>(batchTooltipTextarea, toggleBatch);
    Tooltip<Textarea, Toggle> bitTooltip = new Tooltip<>(bitTooltipTextarea, toggleBits);
    Tooltip<Textarea, Toggle> layerTooltip = new Tooltip<>(layerTooltipTextarea, toggleLayers);
    Tooltip<Textarea, Toggle> currentTooltip = new Tooltip<>(currentTooltipTextarea, toggleCurrent);
    Tooltip<Textarea, Toggle> fullTooltip = new Tooltip<>(fullTooltipTextarea, toggleFull);
    Tooltip<Textarea, Button> exportTooltip = new Tooltip<>(exportTooltipTextare, export);
    Tooltip<Textarea, Button> SpeedUpTooltip = new Tooltip<>(SpeedUpTooltipTextare, speedUpButton);
    Tooltip<Textarea, Button> SpeedDownTooltip = new Tooltip<>(SpeedDownTooltipTextare, speedDownButton);
    Tooltip<Textarea, Button> NextTooltip = new Tooltip<>(NextStepTooltipTextare, next);
    Tooltip<Textarea, Button> PreviousTooltip = new Tooltip<>(PreviousStepTooltipTextare, previous);
    Tooltip<Textarea, Button> exportAllTooltip = new Tooltip<>(exportAllTooltipTextare, exportAll);

    tooltipsToShow.addAll(
        Arrays.asList(batchTooltip, bitTooltip, layerTooltip, currentTooltip, fullTooltip,
            exportTooltip,SpeedUpTooltip,SpeedDownTooltip,NextTooltip,PreviousTooltip,exportAllTooltip));

    initComponentPosition();
  }

  @Override
  protected void updateButton() {
    displayTooltips();
  }

  private void initComponentPosition() {
    Animation.setPosition(0.09f * width, 0.20f * height);
    toggleSubBit.setPosition(0.09f * width, 0.25f * height);
    toggleBits.setPosition(0.09f * width, 0.3f * height);
    toggleBatch.setPosition(0.09f * width, 0.35f * height);
    toggleLayers.setPosition(0.09f * width, 0.4f * height);
    toggleCurrent.setPosition(0.7f * width, 0.3f * height);
    toggleFull.setPosition(0.7f * width, 0.4f * height);
    speedDownButton.setPosition(0.09f * width, 0.51f * height);
    speedUpButton.setPosition(0.45f * width, 0.51f * height);
   previous.setPosition(0.6f * width,0.51f * height);
   next.setPosition(0.75f* width,0.51f * height);
   pauseButton.setPosition(0.24f * width, 0.51f * height);

    sliderAnimation.setPosition(0.09f * width, 0.47f * height);
    export.setPosition(0.09f * width, 0.55f * height);
    exportAll.setPosition(0.5f * width, 0.55f * height);
    for (Tooltip tooltip : tooltipsToShow) {
      float[] position = tooltip.positionOfComponent();
      float[] size = tooltip.sizeOfComponent();
      tooltip.setTooltipPosition(new float[]{position[0] + size[0], position[1]});
    }
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

  @Override
  public void controlEvent(ControlEvent theEvent) {
    switch (theEvent.getName()) {
      case BY_SUB_BIT:
      case BY_BIT:
      case BY_BATCH:
      case BY_LAYER:
      case FULL:
      case ONE_BY_ONE:
        if (theEvent.getValue() == 1.0) {
          System.out.println(theEvent.getName() + ": " + theEvent.getValue());
          if (getListener() != null) {
            getListener().onActionListener(this, theEvent.getName(),
                (float) 1.0 == theEvent.getValue());
          }
        }
        break;
      case PAUSE:

        pausing = !pausing;
        if(pausing)pauseButton.getCaptionLabel().setText(PLAY);
        else { pauseButton.getCaptionLabel().setText(PAUSE); }
        if (getListener() != null) {
          getListener().onActionListener(this, theEvent.getName(), theEvent.getValue());
        }
        break;

        case ANIMATION:
          if(Animation.getCaptionLabel().getText().equals(STOP)) pauseButton.getCaptionLabel().setText(PAUSE);
        pausing = false;

        animating = !animating;
        updateComponent();
        if (getListener() != null) {
          getListener().onActionListener(this, theEvent.getName(), theEvent.getValue());
        }
        break;
      case ANIMATION_SLICER:
        if (pausing && getListener() != null) {
          getListener().onActionListener(this, theEvent.getName(), theEvent.getValue());
        }
        break;
        case NEXT:
        case PREVIOUS:
        case EXPORTAll:
        case EXPORT:
        case SPEED_DOWN:
        case SPEED_UP:
        if (getListener() != null) {
          getListener().onActionListener(this, theEvent.getName(), theEvent.getValue());
        }
        break;
      default:
        logger.logWARNMessage("The event invoked is not handled by method ControlEvent");
        break;
    }
  }

  private void updateComponent() {
    toggleSubBit.setVisible(!animating);
    toggleBits.setVisible(!animating);
    toggleBatch.setVisible(!animating);
    toggleLayers.setVisible(!animating);
    toggleFull.setVisible(!animating);
    toggleCurrent.setVisible(!animating);
  }

  public static void main(String[] args) {
    PApplet.main(UIPWAnimation.class.getCanonicalName());
  }

  @Override
  public void updateIndexRange(int min, int max) {
    if (sliderAnimation != null) {
      sliderAnimation.setRange(min, max);
    }
  }

  @Override
  @SuppressWarnings("all")
  public void onIndexChangeListener(int index) {
    if (sliderAnimation != null) {
      try {
        sliderAnimation.setValue(index);
      } catch (NoClassDefFoundError ignored) {
        logger.logERRORMessage(ignored.getMessage());
      }
    }
  }
}
