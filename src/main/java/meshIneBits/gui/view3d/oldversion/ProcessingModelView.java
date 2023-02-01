/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2021 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020 CLARIS Etienne & RUSSO André.
 * Copyright (C) 2020-2021 DO Quang Bao.
 * Copyright (C) 2021 VANNIYASINGAM Mithulan.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package meshIneBits.gui.view3d.oldversion;

import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import controlP5.ControlP5;
import controlP5.Textlabel;
import javafx.util.Pair;
import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.Model;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.SubWindow;
import meshIneBits.gui.view3d.util.animation.AnimationIndexIncreasedListener;
import meshIneBits.gui.view3d.view.*;
import meshIneBits.util.CustomLogger;
import meshIneBits.util.Vector2;
import meshIneBits.util.Vector3;
import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PSurface;
import processing.event.MouseEvent;
import processing.opengl.PJOGL;
import remixlab.dandelion.geom.Quat;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.InteractiveFrame;
import remixlab.proscene.Scene;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import static remixlab.bias.BogusEvent.CTRL;
import static remixlab.proscene.MouseAgent.*;


/**
 * @author Vallon BENJAMIN
 * <p>
 * 3D view + GUI Use Builder to creates Meshes
 */
public class ProcessingModelView extends PApplet implements Observer, SubWindow,
    IVisualization3DProcessor,
    AnimationIndexIncreasedListener {


  public interface ModelChangesListener {

    void onSizeChange(double scale, double dept, double width, double height);

    void onPositionChange(double x, double y, double z);

    void onRotationChange(double x, double y, double z);
  }

  public static final String IMPORTED_MODEL = "import";
  public static final int ANIMATION_BITS = 100;
  public static final int ANIMATION_LAYERS = 111;
  public static final int ANIMATION_BATCHES = 121;
  public static final int ANIMATION_FULL = 211;
  public static final int ANIMATION_CURRENT = 221;


  private final int BACKGROUND_COLOR = color(150, 150, 150);
  private float printerX;
  private float printerY;
  private float printerZ;

  private final ArrayList<Integer> listIndexWorkingSpace = new ArrayList<>();
  private double workingSpacePosition;

  private double minXDistancePoint;
  private double maxXDistancePoint;

  private final double safetySpace = CraftConfig.margin;

  private Builder builder;
  private static ProcessingModelView currentInstance = null;

  private final ControllerView3D controllerView3D;
  private Model model;
  private PShape shape;
  private PShape shapeMeshPaved;
  private Vector<Pair<Layer, PShape>> shapeMapByLayer = new Vector<>();
  private Vector<Pair<Bit3D, PShape>> shapeMapByBits = new Vector<>();
  private Scene scene;
  private InteractiveFrame frame;
  private ControlP5 cp5;

  private Textlabel txt;
  private Textlabel modelPosition;
  private Textlabel modelSize;
  private Textlabel shortcut;
  private Textlabel slicingWarning;

  private boolean viewModel = true;
  private boolean isAnimated = false;

  //For Module Export OBJ
  private boolean exportOBJ = false;
  private boolean record = false;
  private boolean firstExport = true;

  private int index = 0;
  private int counterBits = 0;
  private int counterBatch = 0;
  private int animationType = ANIMATION_LAYERS;
  private int animationWays = ANIMATION_FULL;
  private Vector<PShape> currentShapeMap;


  private DecimalFormat df;
  {
    df = new DecimalFormat("#.##");
    df.setMaximumFractionDigits(2);
    df.setRoundingMode(RoundingMode.CEILING);
  }

  private boolean[] borders;
  private boolean viewMeshPaved = false;
  private boolean applied = false;

  private double scale = 1;

  private final CustomLogger logger = new CustomLogger(this.getClass());
  private UIPWController uipwController;
  private UIParameterWindow uipwView;
  private UIParameterWindow uipwAnimation;
  private ModelChangesListener mcListener;


  public static void startProcessingModelView() {
    if (!ControllerView3D.getInstance()
        .isAvailable()) {
      return;
    }
    if (currentInstance == null) {
      PApplet.main(ProcessingModelView.class.getCanonicalName());
    }
  }


  public void settings() {
    currentInstance = this;
    Dimension screenSize = Toolkit.getDefaultToolkit()
        .getScreenSize();
    size(screenSize.width * 3 / 5, screenSize.height, P3D);
    PJOGL.setIcon("resources/icon.png");
  }

  private void setCloseOperation() {
    //Removing close listeners
    com.jogamp.newt.opengl.GLWindow win = (com.jogamp.newt.opengl.GLWindow) surface.getNative();
    for (com.jogamp.newt.event.WindowListener wl : win.getWindowListeners()) {
      win.removeWindowListener(wl);
    }

    win.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);

    win.addWindowListener(new WindowAdapter() {
      public void windowDestroyed(WindowEvent e) {
        controllerView3D.deleteObserver(currentInstance);
        currentInstance = null;
        builder.onTerminated();
        uipwAnimation.closeWindow();
        uipwView.closeWindow();
        uipwController.close();
      }
    });

    win.addWindowListener(new WindowAdapter() {
      @Override
      public void windowResized(WindowEvent e) {
        super.windowResized(e);
        surface.setSize(win.getWidth(), win.getHeight());
      }
    });
  }

  public ProcessingModelView() {
    controllerView3D = ControllerView3D.getInstance();
    controllerView3D.addObserver(this);
  }

  //TODO to see
  public static void closeInstance() {
    currentInstance = null;
  }

  /**
   *
   */
  public void setup() {
    Dimension screenSize = Toolkit.getDefaultToolkit()
        .getScreenSize();

    configWindow("MeshIneBits - Model view", (int) (screenSize.getWidth() / 5), 0);
    initWorkspace();
    init3DScene(new Vec(0, 1, 1), 2500);
    init3DShapes(controllerView3D.getModel());
    init3DFrame();
    initControlComponent();
    initParameterWindow();
    initModelChangesListener((ModelChangesListener) uipwView);
    displayParameterWindows();
  }

  private void displayParameterWindows() {
    Dimension screenSize = Toolkit.getDefaultToolkit()
        .getScreenSize();
    if (uipwView == null || uipwAnimation == null) {
      logger.logWARNMessage("Parameter window should be initialized, call initParameterWindow");
      return;
    }
    String[] args = {"--location=0,0", "Foo"};
    runSketch(new String[]{"--display=1",
        "--location=0,0", "--width=" + screenSize.width / 5,
        "--height=" + (screenSize.height - 100),
        "Projector"}, uipwView);

    runSketch(new String[]{"--display=1",
        "--location=0,0", "--width=" + screenSize.width / 5,
        "--height=" + (screenSize.height - 100),
        "Projector"}, uipwAnimation);

    updateSizeChangesOnModel();
    updatePositionChangesOnModel();
  }

  private void initModelChangesListener(ModelChangesListener listener) {
    mcListener = listener;
  }

  private void initParameterWindow() {
    Dimension screenSize = Toolkit.getDefaultToolkit()
        .getScreenSize();
//    uipwController = new UIPWController(this, controllerView3D.getCurrentMesh());
    uipwView = buildControllerWindow(UIPWView.class,
        uipwController,
        "View Configuration",
        screenSize.width / 5,
        screenSize.height - 100);
    uipwAnimation = buildControllerWindow(UIPWAnimation.class,
        uipwController,
        "View Animation",
        screenSize.width / 5,
        screenSize.height - 100);
//    uipwController.setAnimationIndexListener(this, (AnimationIndexIncreasedListener) uipwAnimation);

  }

  private <T extends UIParameterWindow> T buildControllerWindow(Class<T> c, UIPWListener listener,
      String title, int width, int height) {
    UIParameterWindow.WindowBuilder windowBuilder = new UIParameterWindow.WindowBuilder();
    try {
      T obj = windowBuilder.setTitle(title)
          .setListener(listener)
          .setSize(width, height)
          .build(c);
      return obj;
    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void initWorkspace() {
    // each bool corresponds to 1 face of the workspace.
    // is true if the the shape is crossing the associated face.
    // borders[0] =  xmin border / borders[1] = xmax border ...
    borders = new boolean[6];
    for (int i = 0; i < 6; i++) {
      borders[i] = false;
    }
    // Setup workspace
    printerX = CraftConfig.printerX;
    printerY = CraftConfig.printerY;
    printerZ = CraftConfig.printerZ;
  }

  private void initControlComponent() {
    cp5 = new ControlP5(this);
    cp5.setAutoDraw(false);
    createButtons(cp5);

  }

  private void init3DFrame() {
    if (scene == null
        || shape == null
        || controllerView3D == null
        || controllerView3D.getModel() == null) {
      return;
    }
    frame = new InteractiveFrame(scene, shape);
    //set position of frame in scene
    frame.translate(
        (float) controllerView3D.getModel().getPos().x,
        (float) controllerView3D.getModel().getPos().y,
        (float) controllerView3D.getModel().getPos().z);
    customFrameBindings(frame);
    //move frame to position (x,y,0)
    applyGravity();
  }

  public void init3DShapes(Model model) {
    this.model = model;
    builder = new Builder(this);
    shapeMapByLayer = new Vector<>();
    shapeMapByBits = new Vector<>();
    shape = createShape(GROUP);
    builder.buildShape(model, shape);
    shapeMeshPaved = builder.buildMeshPaved(shapeMapByLayer, shapeMapByBits);

    if (controllerView3D.getCurrentMesh().isSliced()) {
      shape.setFill(color(205, 92, 92));
    }

  }

  private void init3DScene(Vec eyePosition, float radius) {
    scene = new Scene(this);
    scene.eye().setPosition(eyePosition);
    scene.eye().lookAt(scene.eye().sceneCenter());
    scene.setRadius(radius);
    scene.showAll();
    scene.disableKeyboardAgent();
    scene.toggleGridVisualHint();
  }

  private void configWindow(String title, int locationX, int locationY) {
    this.surface.setResizable(true);
    this.surface.setTitle(title);
    this.surface.setLocation(locationX, locationY);
    setCloseOperation();
  }


  // used to Start export the model to OBJ
  public void keyPressed() {
    if (key == 's' || key == 'S') {
      record = true;
      counterBits++;
    }
  }


  @Override
  public void mouseWheel(MouseEvent event) {
    if (!frame.isEyeFrame()) {
//            modelSize.setText("Model Size :\n Depth:" + df.format(shape.getDepth() * frame.scaling()) + "\n Height :" + df.format(shape.getHeight() * frame.scaling()) + "\n Width : " + df.format(shape.getWidth() * frame.scaling()) + "\n Scale : " + df.format(frame.scaling()));
      updateSizeChangesOnModel();
    }
    if (frame.scaling() != 1) {
      shape.setFill(color(205, 92, 92));
    }
  }


  private void updatePositionChangesOnModel() {
    if (mcListener != null) {
      mcListener.onPositionChange(Double.parseDouble(df.format(frame.position()
              .x())),
          Double.parseDouble(df.format(frame.position()
              .y())),
          Double.parseDouble(df.format(frame.position()
              .z())));
    }
  }

  private void updateSizeChangesOnModel() {
    if (mcListener != null) {
      mcListener.onSizeChange(Double.parseDouble(df.format(frame.scaling())),
          Double.parseDouble(df.format(shape.getDepth() * frame.scaling())),
          Double.parseDouble(df.format(shape.getWidth() * frame.scaling())),
          Double.parseDouble(df.format(shape.getHeight() * frame.scaling())));
    }

  }

  @Override
  public void mouseDragged() {
    super.mouseDragged();
    if (key == '\uFFFF') {
      updatePositionChangesOnModel();
    }
  }

  private void customFrameBindings(InteractiveFrame frame) {
    frame.removeBindings();
    frame.setHighlightingMode(InteractiveFrame.HighlightingMode.NONE);
    frame.setPickingPrecision(InteractiveFrame.PickingPrecision.ADAPTIVE);
    frame.setGrabsInputThreshold(scene.radius() / 3);
    frame.setRotationSensitivity(3);
    if (!controllerView3D.getCurrentMesh()
        .isSliced()) {
      frame.setMotionBinding(CTRL, LEFT_CLICK_ID, "rotate");
      frame.setMotionBinding(WHEEL_ID,
          scene.is3D() ? (frame.isEyeFrame() ? "translateZ" : "scale") : "scale");
    }
    frame.setMotionBinding(CTRL, RIGHT_CLICK_ID, "translate");
  }

  private int compteur = 0;


  public synchronized void draw() {
    background(0);
//    updateNewData();
    background(BACKGROUND_COLOR);
    lights();
    ambientLight(255, 255, 255);

    drawWorkspace();
    drawWorkingSpace();
    //mouseConstraints();

    // To start Export the model in .obj
    if (record) {
      if (isAnimated) {
        switch (animationType) {
          case ANIMATION_LAYERS:
            switch (animationWays) {
              case ANIMATION_FULL:
                beginRaw("nervoussystem.obj.OBJExport",
                    model.getModelName() + "_" + "layer_Evolution_" + counterBits + ".obj");
                break;
              case ANIMATION_CURRENT:
                beginRaw("nervoussystem.obj.OBJExport",
                    model.getModelName() + "_" + "Current_layer_" + counterBits + ".obj");
                break;
            }
            break;
          case ANIMATION_BITS:
            switch (animationWays) {
              case ANIMATION_FULL:
                beginRaw("nervoussystem.obj.OBJExport",
                    model.getModelName() + "_" + "bits_Evolution_" + counterBits + ".obj");
                break;
              case ANIMATION_CURRENT:
                beginRaw("nervoussystem.obj.OBJExport",
                    counterBatch + "/" + counterBits + "_" + counterBatch + ".obj");
                break;
            }
            break;
          case ANIMATION_BATCHES:
            switch (animationWays) {
              case ANIMATION_FULL:
                beginRaw("nervoussystem.obj.OBJExport",
                    model.getModelName() + "_" + "Batch_Evolution_" + counterBits + ".obj");
                break;
              case ANIMATION_CURRENT:
                beginRaw("nervoussystem.obj.OBJExport", "Final_" + counterBatch + ".obj");
                break;
            }
            break;
        }
      }
      if (viewMeshPaved) {
        beginRaw("nervoussystem.obj.OBJExport", model.getModelName() + "_" + "Paved" + ".obj");
      }
      if (viewModel) {
        beginRaw("nervoussystem.obj.OBJExport", model.getModelName() + ".obj");
      }
    }
    if (viewModel) {
      scene.drawFrames();
    }
    animationProcess();
    if (viewMeshPaved) {
      drawBits();
    }
    //To end the export
    if (record) {
      endRaw();
      record = false;
    }

//        if(isAnimated)drawAnimation();
    scene.beginScreenDrawing();
//        updateButtons();
//        cp5.draw();
//        displayTooltips();
    scene.endScreenDrawing();
    if (exportOBJ) {
      if (index == currentShapeMap.size()) {
        activateAnimation();
      }
    }
  }

//  private void updateNewData() {
//    if (updatingNewData) {
//      shapeMapByLayer = newShapeMapByLayer;
//      shapeMapByBits = newShapeMapByBits;
//      shapeMeshPaved = newShapeMeshPaved;
//      //shape=this.createShape(GROUP);
//      //builder.buildShape(model,shape);
//      System.out.println("shape size " + shape.getChildCount());
//      System.out.println("new shape size " + newShape.getChildCount());
//      shape = newShape;
//      frame.resetShape();
//      frame.setShape(shape);
//      updatingNewData = false;
//
//    }
//  }

  /*
   * Draw 3D Bits
   *
   */

  private void drawBits() {
    Vector3 v = controllerView3D.getModel()
        .getPos();
    pushMatrix();
    translate((float) v.x, (float) v.y, (float) v.z);
    shape(shapeMeshPaved);
    popMatrix();
  }

  /*
   *  Display the printer workspace
   *
   */
  private void drawWorkspace() {
    pushMatrix();
    noFill();
    translate(0, 0, printerZ / 2);
    strokeWeight(2);
    stroke(0);
    box(printerX, printerY, printerZ);
    popMatrix();
    scene.pg()
        .pushStyle();
    stroke(80);
    scene.pg()
        .beginShape(LINES);
//        stroke(255, 255, 0);
    for (int i = -(int) printerX / 2; i <= printerX / 2; i += 100) {
      vertex(i, printerY / 2, 0);
      vertex(i, -printerY / 2, 0);
    }
    for (int i = -(int) printerY / 2; i <= printerY / 2; i += 100) {
      vertex(printerX / 2, i, 0);
      vertex(-printerX / 2, i, 0);
    }
    scene.pg()
        .endShape();
    scene.pg()
        .popStyle();

  }

  private void drawWorkingSpace() {
    stroke(255, 0, 0);
    rect(-printerX / 2 - CraftConfig.workingWidth - 20, -printerY / 2, CraftConfig.workingWidth,
        CraftConfig.printerY);
  }

  /*
   *  GUI - Buttons + tooltips
   *
   */

  private void createButtons(ControlP5 cp5) {
    GLWindow win = ((GLWindow) surface.getNative());
    int color = 255;

    txt = createModelPositionText(cp5);

    modelSize = createModelSizeText(cp5);

    modelPosition = createModelinPringtingSpace(cp5);

    shortcut = cp5.addTextlabel("shortcut")
        .setText(
            "Shortcut : \n Rotation : CTRL + Mouse Left Click, Cannot be used when Mesh is sliced \n Translation : CTRL + Mouse Right Click \n Change Model Size : Mouse on the Model + Mouse Wheel , Cannot be used when Mesh is sliced\n Zoom : Mouse Wheel\n Export to Obj: press button 'S'")
        .setColor(255)
        .setFont(createFont("arial bold", 15));

    slicingWarning = cp5.addTextlabel("slicingWarning")
        .setText("The Model is Sliced \n You can't rotate \n You can't scale")
        .setColor(255)
        .setFont(createFont("arial bold", 20))
        .hide();
    if (controllerView3D.getCurrentMesh()
        .isSliced()) {
      slicingWarning.show();
    }

//        toggleViewModel = cp5.addToggle("Model").setSize(20, 20)
//                .setColorBackground(color(255, 250)).setColorActive(color).setColorForeground(color + 50).toggle().setFont(createFont("arial bold", 15));
//
//        toggleViewBits = cp5.addToggle("Bits").setSize(20, 20)
//                .setColorBackground(color(255, 250)).setColorActive(color).setColorForeground(color + 50).setFont(createFont("arial bold", 15));

//        sliderM = cp5.addSlider("M").setRange(0, 100).setSize(100, 20).setValue(100)
//                .setColorForeground(color + 50).setColorActive(color + 40).setFont(createFont("arial bold", 15));
//        sliderB = cp5.addSlider("B").setRange(0, 100).setSize(100, 20).setValue(100)
//                .setColorForeground(color + 50).setColorActive(color + 40).setFont(createFont("arial bold", 15));

    cp5.setAutoDraw(false);
  }

  private Textlabel createModelinPringtingSpace(ControlP5 cp5) {
    return cp5.addTextlabel("model position")
        .setText("Model Position in \n Printing Space ")
        .setColor(255)
        .setFont(createFont("arial bold", 20));
  }

  private Textlabel createModelSizeText(ControlP5 cp5) {
    return cp5.addTextlabel("model size")
        .setText(
            "Model Size :\n Depth:" + df.format(shape.getDepth()) + "\n Height :" + df.format(
                shape.getHeight()) + "\n Width : " + df.format(shape.getWidth()) + "\n Scale : "
                + df.format(frame.scaling()))
        .setColor(255)
        .setFont(createFont("arial bold", 15));
  }

  private Textlabel createModelPositionText(ControlP5 cp5) {
    return cp5.addTextlabel("label")
        .setText("Current Position : (0,0,0)")
        .setSize(80, 40)
        .setColor(255)
        .setFont(createFont("arial bold", 15));
  }

  private void updateButtons() {
    pushMatrix();
    GLWindow win = ((GLWindow) surface.getNative());
    modelSize.setPosition(win.getWidth() - 150, 10);
    txt.setPosition(win.getWidth() - 150, win.getHeight() - 80);
    txt.setText(
        "Current position :\n" + " x : " + df.format(frame.position()
            .x()) + "\n y : " + df.format(
            frame.position()
                .y()) + "\n z : " + df.format(frame.position()
            .z()));
    slicingWarning.setPosition(win.getWidth() - 450, 10);
    shortcut.setPosition(30, win.getHeight() - 120);

    modelPosition.setPosition(30, win.getHeight() / 5 - 50);
//        tooltipRotation.setPosition(30, win.getHeight() / 5 - 30);
//        TFRotationX.setPosition(30, win.getHeight() / 5);
//        TFRotationY.setPosition(30, win.getHeight() / 5 + 60);
//        TFRotationZ.setPosition(30, win.getHeight() / 5 + 120);
//        TFPositionX.setPosition(130, win.getHeight() / 5);
//        TFPositionY.setPosition(130, win.getHeight() / 5 + 60);
//        TFPositionZ.setPosition(130, win.getHeight() / 5 + 120);
//
//        sliderM.setPosition(30, win.getHeight() / 5 + 180);
//        sliderB.setPosition(30, win.getHeight() / 5 + 210);
//
//        toggleViewModel.setPosition(30, win.getHeight() / 5 + 250);
//        toggleViewBits.setPosition(90, win.getHeight() / 5 + 250);
//
//        apply.setPosition(win.getWidth() - 220, win.getHeight() / 4);
//        tooltipApply.setPosition(win.getWidth() - 220, win.getHeight() / 4 - 18);
//        gravity.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 50);
//        tooltipGravity.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 32);
//        reset.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 100);
//        tooltipReset.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 82);
//        camera.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 150);
//        tooltipCamera.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 132);
//        animation.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 200);
//        export.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 370);
//        tooltipExport.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 347);
//
//        toggleBits.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 235);
//        tooltipBits.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 217);
//        toggleBatch.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 280);
//        tooltipBatch.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 262);
//        toggleLayers.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 325);
//        tooltipLayers.setPosition(win.getWidth() - 220, win.getHeight() / 4 + 307);
//        toggleCurrent.setPosition(win.getWidth() - 100, win.getHeight() / 4 + 235);
//        tooltipCurrent.setPosition(win.getWidth() - 100, win.getHeight() / 4 + 255);
//        toggleFull.setPosition(win.getWidth() - 100, win.getHeight() / 4 + 280);
//        tooltipFull.setPosition(win.getWidth() - 100, win.getHeight() / 4 + 300);
//        speedDownButton.setPosition(win.getWidth() / 2 + 20, win.getHeight() - 50);
//        speedUpButton.setPosition(win.getWidth() / 2 + 60, win.getHeight() - 50);
//        pauseButton.setPosition(win.getWidth() / 2 + 100, win.getHeight() - 50);
//        sliderAnimation.setPosition(win.getWidth() / 2 - 200, win.getHeight() - 50);

    if (mouseX < 100) {
      scene.disableMotionAgent();
    } else {
      scene.enableMotionAgent();
    }
    popMatrix();
  }

  private void displayTooltips() {
//        tooltipGravity.hide();
//        tooltipReset.hide();
//        tooltipCamera.hide();
//        tooltipApply.hide();
//        tooltipExport.hide();
//        tooltipBits.hide();
//        tooltipBatch.hide();
//        tooltipLayers.hide();
//        tooltipCurrent.hide();
//        tooltipFull.hide();
//
//        float[] rotationTooltipsPostition = tooltipRotation.getPosition();
//        float[] gravityPosition = tooltipGravity.getPosition();
//        float[] resetPosition = tooltipReset.getPosition();
//        float[] cameraPosition = tooltipCamera.getPosition();
//        float[] applyPosition = tooltipApply.getPosition();
//        float[] exportPosition = tooltipExport.getPosition();
//        float[] byBitsPosition = tooltipBits.getPosition();
//        float[] byBatchPosition = tooltipBatch.getPosition();
//        float[] byLayersPosition = tooltipLayers.getPosition();
//        float[] currentPosition = toggleCurrent.getPosition();
//        float[] fullPosition = toggleFull.getPosition();
//
//
//        if ((mouseX > rotationTooltipsPostition[0]) && (mouseX < rotationTooltipsPostition[0] + 220) && (mouseY > rotationTooltipsPostition[1]) && (mouseY < rotationTooltipsPostition[1] + 36)) {
//            tooltipRotation.hide();
//        }
//
//        if ((mouseX > gravityPosition[0]) && (mouseX < gravityPosition[0] + 140) && (mouseY > gravityPosition[1]) && (mouseY < gravityPosition[1] + 48)) {
//            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
//                tooltipGravity.show();
//            }
//        }
//        if ((mouseX > resetPosition[0]) && (mouseX < resetPosition[0] + 140) && (mouseY > resetPosition[1]) && (mouseY < resetPosition[1] + 48)) {
//            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
//                tooltipReset.show();
//            }
//        }
//        if ((mouseX > cameraPosition[0]) && (mouseX < cameraPosition[0] + 140) && (mouseY > cameraPosition[1]) && (mouseY < cameraPosition[1] + 48)) {
//            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
//                tooltipCamera.show();
//            }
//        }
//        if ((mouseX > applyPosition[0]) && (mouseX < applyPosition[0] + 140) && (mouseY > applyPosition[1]) && (mouseY < applyPosition[1] + 48)) {
//            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
//                tooltipApply.show();
//            }
//        }
//        if ((mouseX > exportPosition[0]) && (mouseX < exportPosition[0] + 140) && (mouseY > exportPosition[1]) && (mouseY < exportPosition[1] + 48)) {
//            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
//                tooltipExport.show();
//            }
//        }
//        if ((mouseX > byBitsPosition[0]) && (mouseX < byBitsPosition[0] + 20) && (mouseY > byBitsPosition[1]) && (mouseY < byBitsPosition[1] + 40)) {
//            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
//                tooltipBits.show();
//            }
//        }
//        if ((mouseX > byBatchPosition[0]) && (mouseX < byBatchPosition[0] + 20) && (mouseY > byBatchPosition[1]) && (mouseY < byBatchPosition[1] + 40)) {
//            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
//                tooltipBatch.show();
//            }
//        }
//        if ((mouseX > byLayersPosition[0]) && (mouseX < byLayersPosition[0] + 20) && (mouseY > byLayersPosition[1]) && (mouseY < byLayersPosition[1] + 40)) {
//            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
//                tooltipLayers.show();
//            }
//        }
//        if ((mouseX > currentPosition[0]) && (mouseX < currentPosition[0] + 20) && (mouseY > currentPosition[1]) && (mouseY < currentPosition[1] + 20)) {
//            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
//                tooltipCurrent.show();
//            }
//        }
//        if ((mouseX > fullPosition[0]) && (mouseX < fullPosition[0] + 20) && (mouseY > fullPosition[1]) && (mouseY < fullPosition[1] + 20)) {
//            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
//                tooltipFull.show();
//            }
//        }
  }

  /*
   * Rotate the shape
   *
   */
  private void rotateShape(float angleX, float angleY, float angleZ) {
    if (!controllerView3D.getCurrentMesh()
        .isSliced()) {
      applied = false;
      Quat r = new Quat();
      float angXRad = (float) Math.toRadians(angleX);
      float angYRad = (float) Math.toRadians(angleY);
      float angZRad = (float) Math.toRadians(angleZ);
      r.fromEulerAngles(angXRad, angYRad, angZRad);
      frame.rotate(r);
      applyGravity();
//            if(mcListener!=null)mcListener.onRotationChange(frame.rotation().a);
    } else {
//            tooltipRotation.show();
    }
  }

  /*
   *	Move the shape
   *
   */
  private void translateShape(float transX, float transY, float transZ) {
    applied = false;
    frame.translate(transX, transY, transZ);
    boolean checkIn = checkShapeInWorkspace();
    if (!checkIn) {
      if (borders[0]) {
        frame.translate((-printerX / 2 - (float) getMinShape().x), 0, 0);
      }
      if (borders[1]) {
        frame.translate((printerX / 2 - (float) getMaxShape().x), 0, 0);
      }
      if (borders[2]) {
        frame.translate(0, (-printerY / 2 - (float) getMinShape().y), 0);
      }
      if (borders[3]) {
        frame.translate(0, (printerY / 2 - (float) getMaxShape().y), 0);
      }
      if (borders[4]) {
        frame.translate(0, 0, (-(float) getMinShape().z));
      }
      if (borders[5]) {
        frame.translate(0, 0, (printerZ - (float) getMaxShape().z));
      }
    }
    updatePositionChangesOnModel();
  }

  /**
   * Return minimum x, y, and z of the shape
   */

  private Vector3 getMinShape() {
    double minx = Double.MAX_VALUE;
    double miny = Double.MAX_VALUE;
    double minz = Double.MAX_VALUE;
    int size = shape.getChildCount();
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < 3; j++) {
        Vec vertex = new Vec(shape.getChild(i)
            .getVertex(j).x, shape.getChild(i)
            .getVertex(j).y,
            shape.getChild(i)
                .getVertex(j).z);
        Vec v = frame.inverseCoordinatesOf(vertex);
        if (minx > v.x()) {
          minx = v.x();
        }
        if (miny > v.y()) {
          miny = v.y();
        }
        if (minz > v.z()) {
          minz = v.z();
        }
      }
    }
    return new Vector3(minx, miny, minz);
  }

  /**
   * Return maximum x, y, and z of the shape
   */

  private Vector3 getMaxShape() {
    double maxx = -printerX;
    double maxy = -printerY;
    double maxz = -printerZ;
    int size = shape.getChildCount();
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < 3; j++) {
        Vec vertex = new Vec(shape.getChild(i)
            .getVertex(j).x, shape.getChild(i)
            .getVertex(j).y,
            shape.getChild(i)
                .getVertex(j).z);
        Vec v = frame.inverseCoordinatesOf(vertex);
        if (maxx < v.x()) {
          maxx = v.x();
        }
        if (maxy < v.y()) {
          maxy = v.y();
        }
        if (maxz < v.z()) {
          maxz = v.z();
        }
      }
    }
    return new Vector3(maxx, maxy, maxz);
  }

  /*
   * 	Apply modifications of the shape to the model
   *
   */
  private void applyRotation() {
    model.rotate(frame.rotation());
  }

  private void applyTranslation() {
    model.setPos(new Vector3(frame.position()
        .x(), frame.position()
        .y(), 0));
    model.move(new Vector3(0, 0, -model.getMin().z));


  }

  @Override
  public void rotationX(float x) {
    rotateShape(x, 0, 0);
  }

  @Override
  public void rotationY(float y) {
    rotateShape(0, y, 0);

  }

  @Override
  public void rotationZ(float z) {
    rotateShape(0, 0, z);

  }

  @Override
  public void translateX(float x) {
    translateShape(x, 0, 0);
  }

  @Override
  public void translateY(float y) {
    translateShape(0, y, 0);

  }

  @Override
  public void translateZ(float z) {
    translateShape(0, 0, z);

  }

  @Override
  public void apply() {
    if (!applied) {
      applyRotation();
      applyScale();
      applyTranslation();
      applied = true;
    }
    applyGravity();
  }

  public void applyGravity() {
    float minZ = (float) getMinShape().z;
    if (minZ != 0) {
      translateShape(0, 0, -minZ);
    }
  }

  /*
   *	Reset the orientation and the position of the shape
   *
   */
  private void resetModel() {
    model.rotate(frame.rotation()
        .inverse());
    model.setPos(new Vector3(0, 0, 0));
    frame.setPosition(new Vec(0, 0, 0));
    frame.rotate(frame.rotation()
        .inverse());
    applyGravity();
    updatePositionChangesOnModel();
    updateSizeChangesOnModel();
  }

  /*
   *  Move camera to center the shape
   */
  public void centerCamera() {
    scene.eye()
        .setPosition(new Vec(frame.position()
            .x(), 3000, 3000));
    scene.eye()
        .lookAt(frame.position());
  }

  @Override
  public void reset() {
    resetModel();
    centerCamera();
    viewMeshPaved = false;
    applied = false;
  }

  @Override
  public void displayModel(boolean boo) {
    viewModel = boo;
    if (!boo) {
      shape.setVisible(false);
      frame.removeMotionBindings();
    } else {
      shape.setVisible(true);
      customFrameBindings(frame);
    }
  }

  @Override
  public void displayMesh(boolean boo) {
    shape.setVisible(!boo);
    viewMeshPaved = boo;
  }

  private void fixPositionCamera(float x, float y, float z) {
    scene.eye()
        .setPosition(new Vec(x, y, z));
  }

  private void fixAngleCamera(float x, float y, float z) {
    scene.eye()
        .lookAt(new Vec(x, y, z));
  }

  /**
   * Check if the model is in the workspace
   *
   * @return true if in the workspace
   */
  private boolean checkShapeInWorkspace() {
    for (int i = 0; i < 6; i++) {
      borders[i] = false;
    }
    float minX = -printerX / 2;
    float maxX = printerX / 2;
    float minY = -printerY / 2;
    float maxY = printerY / 2;
    float minZ = 0;
    float maxZ = printerZ;
    Vector3 vMin = getMinShape();
    Vec minPos = new Vec((float) vMin.x, (float) vMin.y, (float) vMin.z);
    Vector3 vMax = getMaxShape();
    Vec maxPos = new Vec((float) vMax.x, (float) vMax.y, (float) vMax.z);
    boolean inWorkspace = true;
    if (minPos.x() < minX) {
      inWorkspace = false;
      borders[0] = true;
    }
    if (maxPos.x() >= maxX) {
      inWorkspace = false;
      borders[1] = true;
    }
    if (minPos.y() < minY) {
      inWorkspace = false;
      borders[2] = true;
    }
    if (maxPos.y() >= maxY) {
      inWorkspace = false;
      borders[3] = true;
    }
    if (minPos.z() < minZ) {
      inWorkspace = false;
      borders[4] = true;
    }
    if (maxPos.z() >= maxZ) {
      inWorkspace = false;
      borders[5] = true;
    }
    return inWorkspace;
  }

//    private void mouseConstraints() {
//        boolean checkIn = checkShapeInWorkspace();
//        if (!checkIn) {
//            if (borders[0]) {
//                frame.translate((-printerX / 2 - (float) getMinShape().x), 0, 0);
//            }
//            if (borders[1]) {
//                frame.translate((printerX / 2 - (float) getMaxShape().x), 0, 0);
//            }
//            if (borders[2]) {
//                frame.translate(0, (-printerY / 2 - (float) getMinShape().y), 0);
//            }
//            if (borders[3]) {
//                frame.translate(0, (printerY / 2 - (float) getMaxShape().y), 0);
//            }
//            if (borders[4]) {
//                frame.translate(0, 0, (-(float) getMinShape().z));
//            }
//            if (borders[5]) {
//                frame.translate(0, 0, (printerZ - (float) getMaxShape().z));
//            }
//        }
//    }

  private void applyScale() {
    if (frame.scaling() != scale) {
      model.applyScale(frame.scaling());
      scale = frame.scaling();
      modelSize.setText(
          "Model Size :\n Depth:" + df.format(shape.getDepth() * scale) + "\n Height :" + df.format(
              shape.getHeight() * scale) + "\n Width : " + df.format(shape.getWidth() * scale)
              + "\n Scale : " + df.format(scale));
    }
    if (frame.scaling() != 1) {
      shape.setFill(color(205, 92, 92));
    }
  }

  public void Model(boolean flag) {
    viewModel = flag;
    if (!flag) {
      shape.setVisible(false);
      frame.removeMotionBindings();
    } else {
      shape.setVisible(true);
      customFrameBindings(frame);
    }
  }


  public void animationProcess() {

    if (!this.isAnimated) {
      exportOBJ = false;
      counterBits = 0;
      counterBatch = 0;
      firstExport = true;
      return;
    }

    // Boucle de raffraichissement
    switch (animationType) {
      case ANIMATION_LAYERS:
      case ANIMATION_BATCHES:
      case ANIMATION_BITS:
        switch (animationWays) {
          case ANIMATION_FULL:
            for (PShape aShapeMap : currentShapeMap.subList(0, this.index)) {
              aShapeMap.setVisible(true);
            }
            break;
          case ANIMATION_CURRENT:
            for (PShape aShapeMap : currentShapeMap.subList(0, this.index)) {
              aShapeMap.setVisible(true);
            }
            if (this.index != 0) {
              for (PShape aShapeMap : currentShapeMap.subList(0, this.index - 1)) {
                aShapeMap.setVisible(false);
              }
            }
            break;
        }
        if (!exportOBJ && animationType == ANIMATION_BITS) {
          //Hide old workingSpace in the Animation
          int lastIndexWorkingspace = 0;
          for (int i : listIndexWorkingSpace) {
            if (i < this.index) {
              lastIndexWorkingspace++;
            }
          }
          if (lastIndexWorkingspace != 0) {
            for (int i = 0; i < lastIndexWorkingspace - 1; i++) {
              currentShapeMap.get(listIndexWorkingSpace.get(i))
                  .setVisible(false);
            }
          }
        }
        break;
    }

    if (this.index < currentShapeMap.size()) {
      for (PShape aShapeMap : currentShapeMap.subList(this.index + 1, currentShapeMap.size())) {
        aShapeMap.setVisible(false);
      }
    }
    Vector3 v = controllerView3D.getCurrentMesh()
        .getModel()
        .getPos();
    pushMatrix();
    translate((float) v.x, (float) v.y, (float) v.z);
    for (PShape aShapeMap : currentShapeMap) {
      if (aShapeMap != null) {
        shape(aShapeMap);
      }
    }
    popMatrix();


  }

  /**
   * Change the eye position and orientation. To use only when exporting to OBJ used in
   * increaseLayerIndex()
   * TODO Change the function after being able to have an animation by sub-bit to be able to export by sub-bit
   */
  private void changeEyePosition() {
    //when export bits one by one, the eyes have to be in the bits at the lift point or at the cednter of the bits when there is several lift Point
    if (animationType == ANIMATION_BITS && animationWays == ANIMATION_CURRENT) {
      if (this.index != 0) {
        //get the bit's informations
        Bit3D bit = shapeMapByBits.get(this.index - 1)
            .getKey();
        float bitOrientation =
            (float) bit.getOrientation()
                .getEquivalentAngle() * (float) Math.PI / 180;
        float x = 0;
        float y = 0;
        float z = (float) (bit.getHigherAltitude() + bit.getLowerAltitude()) / 2;

        if (bit.getLiftPointsCS()
            .size() > 1) {
          // the eye will be at the center of a normal bit.
          x = (float) CraftConfig.lengthFull / 2;
          y = (float) CraftConfig.bitWidth / 2;
        } else {
          // the eye will at the lift point of the bit
          x = (float) bit.getLiftPointsCS()
              .get(0).x;
          y = (float) bit.getLiftPointsCS()
              .get(0).y;
        }

        fixPositionCamera(x, y, z);
        fixAngleCamera(scene.eye()
            .position()
            .x(), scene.eye()
            .position()
            .y(), printerZ);
        scene.eye()
            .setOrientation(new Quat(0, 0, bitOrientation + (float) Math.PI / 2));
      }
    } else {
      fixPositionCamera(0, 0, printerZ);
      fixAngleCamera(0, 0, 0);
    }
  }

  @Override
  public void update(Observable o, Object arg) {
//        if (IMPORTED_MODEL.equals(arg)) model = controllerView3D.getModel();
//        loadNewData();
//        updateButtons();
//        logger.logDEBUGMessage("update called");
  }

  @Override
  public void toggle() {

  }

  @Override
  public void setCurrentMesh(Mesh mesh) {
    controllerView3D.setMesh(mesh);
    model = mesh.getModel();
    controllerView3D.setModel(model);
  }

  public void setModel(Model m) {
    controllerView3D.setModel(m);
    model = m;
  }

  @Override
  public void setAnimationByBit(boolean boo) {
    if (boo && controllerView3D.getCurrentMesh()
        .isPaved()) {
      animationType = ANIMATION_BITS;
      index = 0;

      workingSpacePosition = printerX / 2 - CraftConfig.workingWidth - 20;
      Vector<PShape> current = new Vector<>();

      shapeMapByBits.forEach((ele) -> {
        Bit3D currentBit = ele.getKey();
        //init + find the min and max position x of the distance points from the currentBit.
        Vector<Vector2> allDistancePoints = currentBit.getTwoDistantPointsCS();
        minXDistancePoint = allDistancePoints.size() == 0 ? 0 : allDistancePoints.get(0).x;
        maxXDistancePoint = allDistancePoints.size() == 0 ? 0 : allDistancePoints.get(0).x;
        for (int i = 0; i < allDistancePoints.size(); i++) {
          if (allDistancePoints.get(i).x < minXDistancePoint) {
            minXDistancePoint = allDistancePoints.get(i).x;
          }
          if (allDistancePoints.get(i).x > maxXDistancePoint) {
            maxXDistancePoint = allDistancePoints.get(i).x;
          }
        }

        // Look if we need to move working space
        if (workingSpacePosition == printerX / 2 - CraftConfig.workingWidth - 20) {
          workingSpacePosition = minXDistancePoint - safetySpace;
          if (!exportOBJ) {
            current.add(createShape(RECT, Math.round(workingSpacePosition), -printerY / 2,
                CraftConfig.workingWidth, CraftConfig.printerY));
            listIndexWorkingSpace.add(0);
          }

        }
        if (Math.round(minXDistancePoint - safetySpace) <= workingSpacePosition
            || Math.round(maxXDistancePoint + safetySpace) >= (workingSpacePosition
            + CraftConfig.workingWidth)) {
          workingSpacePosition = minXDistancePoint - safetySpace;
          if (!exportOBJ) {
            current.add(createShape(RECT, Math.round(workingSpacePosition), -printerY / 2,
                CraftConfig.workingWidth, CraftConfig.printerY));
            listIndexWorkingSpace.add(current.size() - 1);
          }

        }
        current.add(ele.getValue());
      });
      current.add(shapeMapByBits.get(shapeMapByBits.size() - 1)
          .getValue());
      currentShapeMap = current;
//      uipwController.setAnimationRange(currentShapeMap.size());
    }
  }

  @Override
  public void setAnimationByBatch(boolean boo) {
    if (boo && controllerView3D.getCurrentMesh()
        .isPaved()) {
      index = 0;
      animationType = ANIMATION_BATCHES;
      Vector<PShape> current = new Vector<>();
      int numberOfBatches = controllerView3D.getCurrentMesh()
          .getScheduler()
          .getBitBatch(shapeMapByBits.get(shapeMapByBits.size() - 1)
              .getKey());
      for (int i = 0; i <= numberOfBatches; i++) {
        current.add(builder.getBatchPShapeForm(shapeMapByBits, i));
      }
      current.add(builder.getBatchPShapeForm(shapeMapByBits, numberOfBatches + 1));
      currentShapeMap = current;
//      uipwController.setAnimationRange(currentShapeMap.size());
    }
  }

  @Override
  public void setAnimationByLayer(boolean boo) {
    if (boo && controllerView3D.getCurrentMesh()
        .isPaved()) {
      index = 0;
      animationType = ANIMATION_LAYERS;
      Vector<PShape> current = new Vector<>();

      shapeMapByLayer.forEach((ele) -> {
        current.add(ele.getValue());
      });
      current.add(shapeMapByLayer.get(shapeMapByLayer.size() - 1)
          .getValue());
      currentShapeMap = current;
//      uipwController.setAnimationRange(currentShapeMap.size());
    }
  }

  @Override
  public void setDisplayOneByOne(boolean boo) {
    if (boo) {
      animationWays = ANIMATION_CURRENT;
    }
  }

  @Override
  public void setDisplayFull(boolean boo) {
    if (boo) {
      animationWays = ANIMATION_FULL;
    }
  }

  @Override
  public void export() {
    exportOBJ = !exportOBJ;
    activateAnimation();
  }

  @Override
  public void activateAnimation() {
    if (isAnimated) {
      return;
    }
    isAnimated = true;
    viewMeshPaved = false;
    displayModel(false);
  }

  @Override
  public void deactivateAnimation() {
    isAnimated = false;
    viewMeshPaved = true;
  }

  @Override
  protected PSurface initSurface() {
    if (surface == null) {
      return super.initSurface();
    }
    return this.getSurface();
  }

  @Override
  public void updateIndexRange(int min, int max) {

  }

  @Override
  public void onIndexChangeListener(int index) {
    this.index = index;
  }
}