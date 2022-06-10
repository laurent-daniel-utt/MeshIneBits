package meshIneBits.gui.view3d.view;

import static remixlab.bias.BogusEvent.CTRL;
import static remixlab.proscene.MouseAgent.LEFT_CLICK_ID;
import static remixlab.proscene.MouseAgent.RIGHT_CLICK_ID;
import static remixlab.proscene.MouseAgent.WHEEL_ID;

import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import controlP5.ControlP5;
import java.lang.reflect.InvocationTargetException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Vector;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view3d.Processor.BaseVisualization3DProcessor;
import meshIneBits.gui.view3d.Processor.IVisualization3DProcessor;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.animation.AnimationIndexIncreasedListener;
import meshIneBits.gui.view3d.oldversion.ProcessingModelView.ModelChangesListener;
import meshIneBits.gui.view3d.provider.MeshProvider;
import meshIneBits.util.CustomLogger;
import meshIneBits.util.Vector3;
import processing.core.PApplet;
import processing.core.PShape;
import processing.event.MouseEvent;
import processing.opengl.PJOGL;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.InteractiveFrame;
import remixlab.proscene.Scene;

public class BaseVisualization3DView extends AbstractVisualization3DView {

  private static final CustomLogger logger = new CustomLogger(BaseVisualization3DView.class);

  private UIParameterWindow uipwAnimation;
  private UIParameterWindow uipwView;
  private UIPWController uipwController;
  private IVisualization3DProcessor processor;
  private ModelChangesListener mcListener;

  private CustomInteractiveFrame frame;
  private Scene scene;
  private ControlP5 cp5;

  private float printerX;
  private float printerY;
  private float printerZ;

  private PShape shape;
  private PShape meshShape;
  private Vector<PShape> animationShapes;

  private final DecimalFormat df;
  private int indexExport = 0;
  private boolean isExporting = false;

  {
    df = new DecimalFormat("#.##");
    df.setMaximumFractionDigits(2);
    df.setRoundingMode(RoundingMode.CEILING);
  }

  public static void startProcessingModelView() {
    if (!MeshProvider.getInstance().isAvailable()) {
      logger.logERRORMessage("Model and Mesh are not available!");
      return;
    }
    PApplet.main(BaseVisualization3DView.class.getCanonicalName());
  }

  public void settings() {
    size(Visualization3DConfig.V3D_WINDOW_WIDTH, Visualization3DConfig.V3D_WINDOW_HEIGHT, P3D);
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
        processor.onTerminated();
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

  public void mouseWheel(MouseEvent event) {
    if (!frame.isEyeFrame()) {
      //TODO something
    }
    if (frame.scaling() != 1) {
      shape.setFill(color(205, 92, 92));
    }
  }

  public void setup() {
    configWindow(
            Visualization3DConfig.VISUALIZATION_3D_WINDOW_TITLE,
            Visualization3DConfig.V3D_WINDOW_LOCATION_X,
            Visualization3DConfig.V3D_WINDOW_LOCATION_Y);
    initWorkspace();
    init3DScene(Visualization3DConfig.V3D_EYE_POSITION, Visualization3DConfig.V3D_RADIUS);
    init3DFrame();
    initProcessor();
    shape = processor.getModelProvider().getModelShape();
    meshShape = processor.getModelProvider().getMeshShape();
    frame.setShape(shape);
    initControlComponent();
    initParameterWindow();
    initModelChangesListener((ModelChangesListener) uipwView);
    initDisplayParameterWindows();

  }

  private void initDisplayParameterWindows() {
    if (uipwView == null || uipwAnimation == null) {
      logger.logWARNMessage("Parameter window should be initialized, call initParameterWindow");
      return;
    }
    runSketch(new String[]{"--display=1", "Projector"}, uipwView);
    runSketch(new String[]{"--display=1", "Projector"}, uipwAnimation);

    updateSizeChangesOnModel();
    updatePositionChangesOnModel();
  }

  private void updatePositionChangesOnModel() {
    if (mcListener != null) {
      mcListener.onPositionChange(Double.parseDouble(df.format(frame.position()
                      .x()).replace(",", ".")),
              Double.parseDouble(df.format(frame.position()
                      .y()).replace(",", ".")),
              Double.parseDouble(df.format(frame.position()
                      .z()).replace(",", ".")));
    }
  }

  private void updateSizeChangesOnModel() {
    if (mcListener != null) {
      mcListener.onSizeChange(Double.parseDouble(df.format(frame.scaling())),
              Double.parseDouble(df.format(shape.getDepth() * frame.scaling()).replace(",", ".")),
              Double.parseDouble(df.format(shape.getWidth() * frame.scaling()).replace(",", ".")),
              Double.parseDouble(df.format(shape.getHeight() * frame.scaling()).replace(",", ".")));
    }

  }

  @Override
  public void mouseDragged() {
    super.mouseDragged();
    if (key == '\uFFFF') {
      updatePositionChangesOnModel();
    }
  }

  private void initModelChangesListener(ModelChangesListener listener) {
    mcListener = listener;
  }

  private void initParameterWindow() {

    uipwController = new UIPWController(processor);
    uipwView = buildControllerWindow(UIPWView.class,
            uipwController,
            "View Configuration",
            Visualization3DConfig.UIP_WINDOW_WIDTH,
            Visualization3DConfig.UIP_WINDOW_HEIGHT);
    uipwAnimation = buildControllerWindow(UIPWAnimation.class,
            uipwController,
            "View Animation",
            Visualization3DConfig.UIP_WINDOW_WIDTH,
            Visualization3DConfig.UIP_WINDOW_HEIGHT);

    if (processor instanceof BaseVisualization3DProcessor && uipwAnimation != null) {
      ((BaseVisualization3DProcessor) processor).getAnimationProcessor()
              .addOnIndexIncreasedListener((AnimationIndexIncreasedListener) uipwAnimation);
    }
  }

  private void initControlComponent() {
    cp5 = new ControlP5(this);
    cp5.setAutoDraw(false);
//    createButtons(cp5);
  }

  private void initProcessor() {
    processor = new BaseVisualization3DProcessor(MeshProvider.getInstance().getCurrentMesh(),
            this);
  }

  private void init3DFrame() {
    if (scene == null) {
      return;
    }
    frame = new CustomInteractiveFrame(scene);
    //set position of frame in scene
    frame.translate(
            (float) MeshProvider.getInstance().getModel().getPos().x,
            (float) MeshProvider.getInstance().getModel().getPos().y,
            (float) MeshProvider.getInstance().getModel().getPos().z);
    customFrameBindings(frame);
  }

  private void customFrameBindings(InteractiveFrame frame) {
    frame.removeBindings();
    frame.setHighlightingMode(InteractiveFrame.HighlightingMode.NONE);
    frame.setPickingPrecision(InteractiveFrame.PickingPrecision.ADAPTIVE);
    frame.setGrabsInputThreshold(scene.radius() / 3);
    frame.setRotationSensitivity(3);
    if (!MeshProvider.getInstance().getCurrentMesh()
            .isSliced()) {
      frame.setMotionBinding(CTRL, LEFT_CLICK_ID, "rotate");
      frame.setMotionBinding(WHEEL_ID,
              scene.is3D() ? (frame.isEyeFrame() ? "translateZ" : "scale") : "scale");
    }
    frame.setMotionBinding(CTRL, RIGHT_CLICK_ID, "translate");
  }

  private void initWorkspace() {
    // Setup workspace
    printerX = CraftConfig.printerX;
    printerY = CraftConfig.printerY;
    printerZ = CraftConfig.printerZ;
  }

  @SuppressWarnings("all")
  private void configWindow(String title, int locationX, int locationY) {
    this.surface.setResizable(true);
    this.surface.setTitle(title);
    this.surface.setLocation(locationX, locationY);
    setCloseOperation();
  }

  @SuppressWarnings("all")
  private void init3DScene(Vec eyePosition, float radius) {
    scene = new Scene(this);
    scene.eye().setPosition(eyePosition);
    scene.eye().lookAt(scene.eye().sceneCenter());
    scene.setRadius(radius);
    scene.showAll();
    scene.disableKeyboardAgent();
    scene.toggleGridVisualHint();
  }

  @Override
  public Scene getScene() {
    return scene;
  }

  @Override
  public void setDisplayModelShape(PShape pShape) {
    frame.setShape(pShape);
  }

  @Override
  public void setDisplayMeshShape(PShape pShape) {
    meshShape = pShape;
  }

  @Override
  public void
  setDisplayShapes(Vector<PShape> displayShapes) {
    animationShapes = displayShapes;
  }

  @Override
  public CustomInteractiveFrame getFrame() {
    return frame;
  }

  @SuppressWarnings("all")
  private <T extends UIParameterWindow> T
  buildControllerWindow(Class<T> c, UIPWListener listener,
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

  @Override
  public synchronized void draw() {
    background(Visualization3DConfig.V3D_BACKGROUND.getRGB());
    lights();
    ambientLight(
            Visualization3DConfig.V3D_AMBIENT_LIGHT.getRed(),
            Visualization3DConfig.V3D_AMBIENT_LIGHT.getGreen(),
            Visualization3DConfig.V3D_AMBIENT_LIGHT.getBlue());
    drawWorkspace();
    drawWorkingSpace();
    startExport();
    displayShape();
    endExport();

  }

  @Override
  public synchronized void export() {
    isExporting = true;
  }

  private void endExport() {
    if (isExporting) {
      isExporting = false;
      endRaw();
    }
  }

  private void startExport() {
    if (isExporting) {
      String modelName = MeshProvider.getInstance().getModel().getModelName();
      StringBuilder exportFileName = new StringBuilder();
      switch (processor.getDisplayState().getState()) {
        case MODEL_VIEW:
          exportFileName.append(modelName)
                  .append("-")
                  .append(indexExport)
                  .append(".obj");
          break;
        case PAVED_VIEW:
          exportFileName.append(modelName)
                  .append("-")
                  .append(indexExport)
                  .append("-Paved")
                  .append(".obj");
          break;
        case ANIMATION_VIEW:
          exportFileName.append(modelName)
                  .append("-")
                  .append(indexExport)
                  .append("-Animation")
                  .append(".obj");
          break;
        default:
          throw new IllegalStateException(
                  "Unexpected value: " + processor.getDisplayState().getState());
      }
      logger.logDEBUGMessage("Exporting " + exportFileName);
      beginRaw(Visualization3DConfig.EXPORT_3D_RENDERER, exportFileName.toString());
      indexExport++;
    }
  }

  private synchronized void displayShape() {
    switch (processor.getDisplayState().getState()) {
      case MODEL_VIEW:
        scene.drawFrames();
        break;
      case PAVED_VIEW:
        drawMesh();
        break;
      case ANIMATION_VIEW:
        drawAnimationShape();
        break;
      default:
        throw new IllegalStateException(
                "Unexpected value: " + processor.getDisplayState().getState());
    }
  }

  private void drawMesh() {
    if (meshShape != null) {
      Vector3 v = MeshProvider.getInstance().getModel().getPos();
      pushMatrix();
      translate((float) v.x, (float) v.y, (float) v.z);
      shape(meshShape);
      popMatrix();
    }
  }

  private void drawAnimationShape() {
    if (animationShapes != null) {
      Vector3 v = MeshProvider.getInstance().getCurrentMesh().getModel().getPos();
      pushMatrix();
      translate((float) v.x, (float) v.y, (float) v.z);
      animationShapes.forEach(this::shape);
      popMatrix();
    }
  }

  private void drawWorkspace() {
    pushMatrix();
    noFill();
    translate(0, 0, printerZ / 2);
    strokeWeight(2);
    stroke(0);
    box(printerX, printerY, printerZ);
    popMatrix();
    scene.pg().pushStyle();
    stroke(80);
    scene.pg().beginShape(LINES);
    for (int i = -(int) printerX / 2; i <= printerX / 2; i += 100) {
      vertex(i, printerY / 2, 0);
      vertex(i, -printerY / 2, 0);
    }
    for (int i = -(int) printerY / 2; i <= printerY / 2; i += 100) {
      vertex(printerX / 2, i, 0);
      vertex(-printerX / 2, i, 0);
    }
    scene.pg().endShape();
    scene.pg().popStyle();
  }

  private void drawWorkingSpace() {
    stroke(255, 0, 0);
    rect(-printerX / 2 - CraftConfig.workingWidth - 20,
            -printerY / 2, CraftConfig.workingWidth,
            CraftConfig.printerY);
  }
}
