package meshIneBits.gui.view3d.view;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Slider;
import controlP5.Toggle;
import meshIneBits.gui.view3d.util.animation.AnimationIndexIncreasedListener;
import processing.core.PFont;
import processing.core.PApplet;

import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.*;

/**
 * "View Animation" side panel integrated into the same PApplet (no top-level window).
 */
public class UIPWAnimationSidePanel implements AnimationIndexIncreasedListener {

  private final PApplet app;
  private final ControlP5 cp5;
  private final UIPWController controller;

  private final float panelWidth;
  private final float panelHeight;
  private final float originX;
  private final float originY;

  private Button exportAll;
  private Button animation;
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

  public UIPWAnimationSidePanel(PApplet app, ControlP5 cp5, UIPWController controller,
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
    PFont font = app.createFont("arial bold", 15);
    int componentBackground = app.color(255, 250);

    animation = cp5.addButton(ANIMATION)
        .setSize(140, 30)
        .setColorLabel(255)
        .setFont(font);

    export = cp5.addButton(EXPORT)
        .setSize(140, 30)
        .setColorLabel(255)
        .setFont(font);

    exportAll = cp5.addButton(EXPORTAll)
        .setSize(140, 30)
        .setColorLabel(255)
        .setFont(font);

    toggleSubBit = cp5.addToggle(BY_SUB_BIT)
        .setSize(20, 20)
        .setColorBackground(componentBackground)
        .setColorActive(color)
        .setColorForeground(color + 50);

    toggleBits = cp5.addToggle(BY_BIT)
        .setSize(20, 20)
        .setColorBackground(componentBackground)
        .setColorActive(color)
        .setColorForeground(color + 50);

    toggleBatch = cp5.addToggle(BY_BATCH)
        .setSize(20, 20)
        .setColorBackground(componentBackground)
        .setColorActive(color)
        .setColorForeground(color + 50);

    toggleLayers = cp5.addToggle(BY_LAYER)
        .setSize(20, 20)
        .setColorBackground(componentBackground)
        .setColorActive(color)
        .setColorForeground(color + 50)
        .setValue(1.0f);

    toggleCurrent = cp5.addToggle(ONE_BY_ONE)
        .setSize(20, 20)
        .setColorBackground(componentBackground)
        .setColorActive(color)
        .setColorForeground(color + 50)
        .setValue(0.0f);

    toggleFull = cp5.addToggle(FULL)
        .setSize(20, 20)
        .setColorBackground(componentBackground)
        .setColorActive(color)
        .setColorForeground(color + 50)
        .setValue(1.0f);

    sliderAnimation = cp5.addSlider(ANIMATION_SLICER)
        .setSize(200, 30)
        .setVisible(true);

    speedUpButton = cp5.addButton(SPEED_UP)
        .setSize(30, 30)
        .setVisible(true)
        .setFont(font);
    speedUpButton.getCaptionLabel().setText(">>");

    speedDownButton = cp5.addButton(SPEED_DOWN)
        .setSize(30, 30)
        .setVisible(true)
        .setFont(font);
    speedDownButton.getCaptionLabel().setText("<<");

    next = cp5.addButton(NEXT)
        .setVisible(true)
        .setSize(30, 30)
        .setFont(font);
    next.getCaptionLabel().setText(">");

    previous = cp5.addButton(PREVIOUS)
        .setVisible(true)
        .setSize(30, 30)
        .setFont(font);
    previous.getCaptionLabel().setText("<");

    pauseButton = cp5.addButton(PAUSE)
        .setVisible(true)
        .setSize(50, 30)
        .setColorLabel(255)
        .setFont(font);
    pauseButton.getCaptionLabel().setText(PAUSE);

    layout();
    attachCallbacks();
    updateComponent();
  }

  private void attachCallbacks() {
    // Selection toggles with exclusivity
    toggleSubBit.onClick(e -> {
      if (e.getController().getValue() == 1.0f) {
        toggleLayers.setState(false);
        toggleBatch.setState(false);
        toggleBits.setState(false);
        controller.onActionListener(this, BY_SUB_BIT, true);
      }
    });

    toggleBits.onClick(e -> {
      if (e.getController().getValue() == 1.0f) {
        toggleLayers.setState(false);
        toggleBatch.setState(false);
        toggleSubBit.setState(false);
        controller.onActionListener(this, BY_BIT, true);
      }
    });

    toggleBatch.onClick(e -> {
      if (e.getController().getValue() == 1.0f) {
        toggleLayers.setState(false);
        toggleBits.setState(false);
        toggleSubBit.setState(false);
        controller.onActionListener(this, BY_BATCH, true);
      }
    });

    toggleLayers.onClick(e -> {
      if (e.getController().getValue() == 1.0f) {
        toggleBits.setState(false);
        toggleBatch.setState(false);
        toggleSubBit.setState(false);
        controller.onActionListener(this, BY_LAYER, true);
      }
    });

    toggleCurrent.onClick(e -> {
      if (e.getController().getValue() == 1.0f) {
        toggleFull.setState(false);
        controller.onActionListener(this, ONE_BY_ONE, true);
      }
    });

    toggleFull.onClick(e -> {
      if (e.getController().getValue() == 1.0f) {
        toggleCurrent.setState(false);
        controller.onActionListener(this, FULL, true);
      }
    });

    // Main action buttons
    animation.onRelease(e -> {
      pausing = false;
      animating = !animating;
      if (pauseButton != null) {
        pauseButton.getCaptionLabel().setText(PAUSE);
      }
      if (animation != null) {
        animation.getCaptionLabel().setText(animating ? STOP : ANIMATION);
      }
      updateComponent();
      controller.onActionListener(this, ANIMATION, e.getController().getValue());
    });

    pauseButton.onRelease(e -> {
      pausing = !pausing;
      if (pauseButton != null) {
        pauseButton.getCaptionLabel().setText(pausing ? PLAY : PAUSE);
      }
      controller.onActionListener(this, PAUSE, e.getController().getValue());
    });

    export.onRelease(e -> controller.onActionListener(this, EXPORT, e.getController().getValue()));
    exportAll.onRelease(e -> controller.onActionListener(this, EXPORTAll, e.getController().getValue()));
    speedUpButton.onRelease(e -> controller.onActionListener(this, SPEED_UP, e.getController().getValue()));
    speedDownButton.onRelease(e -> controller.onActionListener(this, SPEED_DOWN, e.getController().getValue()));
    next.onRelease(e -> controller.onActionListener(this, NEXT, e.getController().getValue()));
    previous.onRelease(e -> controller.onActionListener(this, PREVIOUS, e.getController().getValue()));

    // Slider sends index continuously while paused, as in legacy behavior.
    sliderAnimation.onChange(e -> {
      if (pausing) {
        controller.onActionListener(this, ANIMATION_SLICER, e.getController().getValue());
      }
    });
  }

  private void layout() {
    // Positions are relative to the side panel area inside the host PApplet.
    float x0 = originX;
    float y0 = originY;

    animation.setPosition(x0 + 0.09f * panelWidth, y0 + 0.20f * panelHeight);

    toggleSubBit.setPosition(x0 + 0.09f * panelWidth, y0 + 0.25f * panelHeight);
    toggleBits.setPosition(x0 + 0.09f * panelWidth, y0 + 0.30f * panelHeight);
    toggleBatch.setPosition(x0 + 0.09f * panelWidth, y0 + 0.35f * panelHeight);
    toggleLayers.setPosition(x0 + 0.09f * panelWidth, y0 + 0.40f * panelHeight);

    toggleCurrent.setPosition(x0 + 0.70f * panelWidth, y0 + 0.30f * panelHeight);
    toggleFull.setPosition(x0 + 0.70f * panelWidth, y0 + 0.40f * panelHeight);

    speedDownButton.setPosition(x0 + 0.09f * panelWidth, y0 + 0.51f * panelHeight);
    speedUpButton.setPosition(x0 + 0.45f * panelWidth, y0 + 0.51f * panelHeight);

    previous.setPosition(x0 + 0.60f * panelWidth, y0 + 0.51f * panelHeight);
    next.setPosition(x0 + 0.75f * panelWidth, y0 + 0.51f * panelHeight);

    pauseButton.setPosition(x0 + 0.24f * panelWidth, y0 + 0.51f * panelHeight);

    sliderAnimation.setPosition(x0 + 0.09f * panelWidth, y0 + 0.47f * panelHeight);

    export.setPosition(x0 + 0.09f * panelWidth, y0 + 0.55f * panelHeight);
    exportAll.setPosition(x0 + 0.50f * panelWidth, y0 + 0.55f * panelHeight);
  }

  public void update() {
    // No tooltips in the overlay refactor (keeps it robust).
    // Keep the pause caption synced with local state.
    if (pauseButton != null) {
      pauseButton.getCaptionLabel().setText(pausing ? PLAY : PAUSE);
    }
    if (animation != null) {
      animation.getCaptionLabel().setText(animating ? STOP : ANIMATION);
    }
    updateComponent();
  }

  private void updateComponent() {
    if (toggleSubBit == null) {
      return;
    }
    toggleSubBit.setVisible(!animating);
    toggleBits.setVisible(!animating);
    toggleBatch.setVisible(!animating);
    toggleLayers.setVisible(!animating);
    toggleFull.setVisible(!animating);
    toggleCurrent.setVisible(!animating);
  }

  public void onControlEvent(ControlEvent theEvent) {
    // Legacy fallback, intentionally kept no-op now that callbacks are local on each control.
  }

  @Override
  public void updateIndexRange(int min, int max) {
    if (sliderAnimation != null) {
      sliderAnimation.setRange(min, max);
    }
  }

  @Override
  public void onIndexChangeListener(int index) {
    if (sliderAnimation != null) {
      sliderAnimation.setValue(index);
    }
  }
}

