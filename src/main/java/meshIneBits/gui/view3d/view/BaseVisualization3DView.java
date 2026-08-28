package meshIneBits.gui.view3d.view;

import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import meshIneBits.Strip;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view2d.MeshWindow;
import meshIneBits.gui.view3d.Processor.BaseVisualization3DProcessor;
import meshIneBits.gui.view3d.Processor.IVisualization3DProcessor;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.oldversion.ProcessingModelView.ModelChangesListener;
import meshIneBits.gui.view3d.provider.MeshProvider;
import meshIneBits.gui.view3d.util.animation.AnimationProcessor;
import meshIneBits.util.CustomLogger;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector3;
import processing.core.*;
import processing.event.MouseEvent;
import processing.opengl.PJOGL;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.InteractiveFrame;
import remixlab.proscene.Scene;

import javax.swing.*;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static meshIneBits.gui.view3d.Processor.BaseVisualization3DProcessor.option;
import static meshIneBits.gui.view3d.util.animation.AnimationProcessor.*;
import static remixlab.bias.BogusEvent.CTRL;
import static remixlab.proscene.MouseAgent.*;
public class BaseVisualization3DView extends AbstractVisualization3DView implements AutoCloseable,MouseListener, PropertyChangeListener {

  private static final CustomLogger logger = new CustomLogger(BaseVisualization3DView.class);

  //In order to listen to meshWindow, a reference has to be passed. See @setup in the last lines.
  public static MeshWindow meshWindow;

  public static UIPWAnimation uipwAnimation;
  public static UIPWView uipwView;
  public static UIPWController uipwController;
  public static IVisualization3DProcessor processor;
  private ModelChangesListener mcListener;
  public static CountDownLatch waitshaping=new CountDownLatch(1);
  private CustomInteractiveFrame frame;
  private Scene scene;
  private ControlP5 cp5View;
  private ControlP5 cp5Animation;
  public static CountDownLatch notyet=new CountDownLatch(1);
  private float printerX;
  private float printerY;
  private float printerZ;
  private AtomicInteger ExpInd=new AtomicInteger(0);
  private PShape shape;
  private HashMap<Integer,PShape> meshShapes=new HashMap<Integer,PShape>();
  private PShape meshShape;
  private Vector<PShape> animationShapes;
  private com.jogamp.newt.opengl.GLWindow win;
  private final DecimalFormat df;
  public static int IndexExport = 0;
  private  int pathchoice = 0;

  public static float Xpos=0,Ypos=0,Zpos=0;
  public static ArrayList<ArrayList<Strip>> meshstrips;

  private boolean isExporting = false;
  public static int WindowStatus=0;// false=closed/true=opened
  private PShape rectange;
  private int num_batch=0;
  private CountDownLatch stillExporting=new CountDownLatch(1);
  private String path="";
  private int lastLayoutW = -1;
  private int lastLayoutH = -1;

  public BaseVisualization3DView(){

  }

  private int i=0;
  {

    df = new DecimalFormat("#.##");
    df.setMaximumFractionDigits(2);
    df.setRoundingMode(RoundingMode.CEILING);
  }

  public  void startProcessingModelView() {
  play();

  }

  public void setMeshWindow(MeshWindow meshWindow){
    BaseVisualization3DView.meshWindow = meshWindow;
  }

public void play(){

  if (!MeshProvider.getInstance().isAvailable()) {
    logger.logERRORMessage("Model and Mesh are not available!");
    Logger.updateStatus("Model and Mesh are not available!");
    return;
  }
  WindowStatus=1;
  PApplet.main(BaseVisualization3DView.class.getCanonicalName());


}



  public void settings() {
    size(Visualization3DConfig.V3D_WINDOW_WIDTH, Visualization3DConfig.V3D_WINDOW_HEIGHT, P3D);
    smooth(8);
    PJOGL.setIcon("resources/icon.png");
  }


  /**
   * Event handler to refresh the 3D interface when clicking inside it
   * @param event
   */
  protected void handleMouseEvent(MouseEvent event) {
    final int action = event.getAction();

    if (action == MouseEvent.CLICK && !isOverSidePanel(event.getX())) {
      processor.onTerminated();
      init3DFrame();
      WindowStatus=2;
      noLoop();
      initProcessor();
      meshShapes.put(1,processor.getModelProvider().getMeshShape());
      frame.setShape(shape);

      disposeParameterPanels();
      initControlComponent();
      initParameterWindow();
      initModelChangesListener(uipwView);

      resetAnimationSessionState();
      initWorkingSpace();
      processor.deactivateAnimation();
      if(MeshProvider.getInstance().getCurrentMesh().isPaved()) meshstrips=processor.getModelProvider().getMeshstrips();
      loop();
      Thread t=new Thread(() -> {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        Logger.updateStatus("");
      });t.start();
      Logger.updateStatus("3d interface Refreshed");
    }

    // Relayout before ControlP5 handles the click (split-screen resize can lag draw()).
    layoutEmbeddedPanelsIfNeeded(false);

    // Delegate to Processing so mouseX/mouseY/mousePressed are updated and
    // ControlP5 receives events through the standard registered-method path.
    super.handleMouseEvent(event);
  }









  private void setCloseOperation() {


    //Removing close listeners
     win = (com.jogamp.newt.opengl.GLWindow) surface.getNative();
    for (com.jogamp.newt.event.WindowListener wl : win.getWindowListeners()) {
      win.removeWindowListener(wl);
    }
    win.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);


    win.addWindowListener(new WindowAdapter() {
      public void windowDestroyed(WindowEvent e) {
        WindowStatus=0;
        Logger.updateStatus("");
        closeEntire3DView();
      }
    });
    win.addWindowListener(new WindowAdapter() {
      @Override
      public void windowResized(WindowEvent e) {
        super.windowResized(e);
        int newW = win.getWidth();
        int newH = win.getHeight();
        if (newW > 0 && newH > 0) {
          surface.setSize(newW, newH);
          layoutEmbeddedPanelsIfNeeded(true, newW, newH);
        }
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

    resetAnimationSessionState();

    initWorkspace();// create the box of work space and centre the axes and the model
    init3DScene(Visualization3DConfig.V3D_EYE_POSITION, Visualization3DConfig.V3D_RADIUS);
    init3DFrame();

    initProcessor();
      meshShape = processor.getModelProvider().getMeshShape();
    shape = processor.getModelProvider().getModelShape();

    if(MeshProvider.getInstance().getCurrentMesh().isPaved()) meshstrips=processor.getModelProvider().getMeshstrips();
     meshShapes.put(0,meshShape);
    frame.setShape(shape);

    initControlComponent();
    initParameterWindow();
    initModelChangesListener(uipwView);
    updateSizeChangesOnModel();
    updatePositionChangesOnModel();
    initWorkingSpace();

    // The registering has to be made in this way because BaseVisualization3DView creates a new instance of
    // the class each time a BaseVisualization3DView is opened and meshWindow has to reference to this new instance.
    meshWindow.addPropertyChangeListener(this);

    //System.out.println(this.toString());
  }
private void initWorkingSpace(){
  rectange=null;
    stroke(255, 0, 0);
  strokeWeight(5);
  noFill();
  rectange=createShape();
  rectange.beginShape();
  rectange.vertex(-printerX / 2 - CraftConfig.workingWidth - 20,-printerY / 2,0);
  rectange.vertex(-printerX / 2 - CraftConfig.workingWidth - 20,-printerY / 2+CraftConfig.printerY,0);
  rectange.vertex(-printerX / 2 - CraftConfig.workingWidth - 20+CraftConfig.workingWidth,-printerY / 2+CraftConfig.printerY,0);
  rectange.vertex(-printerX / 2 - CraftConfig.workingWidth - 20+CraftConfig.workingWidth,-printerY / 2,0);
  rectange.endShape(PConstants.CLOSE);

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
    float panelW = sidePanelWidth();
    float panelH = height;

    uipwView = new UIPWView(
        this, cp5View, uipwController,
        0, 0, panelW, panelH);
    uipwView.init();

    uipwAnimation = new UIPWAnimation(
        this, cp5Animation, uipwController,
        width - panelW, 0, panelW, panelH);
    uipwAnimation.init();

    uipwView.layout(0, 0, panelW, panelH);
    uipwAnimation.layout(width - panelW, 0, panelW, panelH);

    lastLayoutW = width;
    lastLayoutH = height;

    if (processor instanceof BaseVisualization3DProcessor) {
      ((BaseVisualization3DProcessor) processor).getAnimationProcessor()
          .addOnIndexIncreasedListener(uipwAnimation);
    }
  }

  private void disposeParameterPanels() {
    if (uipwAnimation != null) {
      uipwAnimation.close();
      uipwAnimation = null;
    }
    if (uipwView != null) {
      uipwView.close();
      uipwView = null;
    }
    if (uipwController != null) {
      uipwController.close();
      uipwController = null;
    }
    if (cp5View != null) {
      cp5View.dispose();
      cp5View = null;
    }
    if (cp5Animation != null) {
      cp5Animation.dispose();
      cp5Animation = null;
    }
  }

  private void initControlComponent() {
    cp5View = new ControlP5(this);
    cp5View.setAutoDraw(false);
    cp5View.enableShortcuts();

    cp5Animation = new ControlP5(this);
    cp5Animation.setAutoDraw(false);
  }

  private void initProcessor() {

      processor = new BaseVisualization3DProcessor(MeshProvider.getInstance().getCurrentMesh(),
            this);
  }

  /** Clears static animation/working-space state between simulations or view rebuilds. */
  private static void resetAnimationSessionState() {
    pos = 0;
    Xpos = 0;
    Ypos = 0;
    Zpos = 0;
    ind.set(0);
    movingWorkSpace = new CountDownLatch(1);
    waitshaping = new CountDownLatch(1);
    notyet = new CountDownLatch(1);
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
   // refresh();
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

  /**
   * ControlP5 callback: dispatch events to the embedded side panels.
   */
  public void controlEvent(ControlEvent theEvent) {
    if (uipwView != null) {
      uipwView.controlEvent(theEvent);
    }
    if (uipwAnimation != null) {
      uipwAnimation.controlEvent(theEvent);
    }
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

    layoutEmbeddedPanelsIfNeeded(false);
    updateMouseAgentForUI();
    drawEmbeddedUI();
  }

  /** Prefer the native GL window size (reliable after snap/split-screen resize). */
  private int layoutWidth() {
    if (win != null && win.getWidth() > 0) {
      return win.getWidth();
    }
    return width;
  }

  private int layoutHeight() {
    if (win != null && win.getHeight() > 0) {
      return win.getHeight();
    }
    return height;
  }

  /** Keep Processing dimensions aligned with the OS window after snap resize. */
  private void syncSurfaceSizeIfNeeded() {
    if (win == null) {
      return;
    }
    int nativeW = win.getWidth();
    int nativeH = win.getHeight();
    if (nativeW > 0 && nativeH > 0 && (nativeW != width || nativeH != height)) {
      surface.setSize(nativeW, nativeH);
    }
  }

  /**
   * Camera / zoom (Proscene) stay active in the center view.
   * Mouse agent is only disabled over the left/right HUD strips.
   */
  private void updateMouseAgentForUI() {
    if (scene == null) {
      return;
    }
    if (isMouseOverSidePanel()) {
      scene.disableMouseAgent();
    } else {
      scene.enableMouseAgent();
      if (cp5View != null) {
        cp5View.getWindow().resetMouseOver();
      }
      if (cp5Animation != null) {
        cp5Animation.getWindow().resetMouseOver();
      }
    }
  }

  /**
   * Keep left/right HUD panels aligned when the window is resized or maximized.
   */
  private void layoutEmbeddedPanelsIfNeeded(boolean force, int layoutW, int layoutH) {
    if (uipwView == null || uipwAnimation == null || layoutW <= 0 || layoutH <= 0) {
      return;
    }
    if (!force && layoutW == lastLayoutW && layoutH == lastLayoutH) {
      return;
    }
    float panelW = sidePanelWidth(layoutW);
    float panelH = layoutH;
    uipwView.layout(0, 0, panelW, panelH);
    uipwAnimation.layout(layoutW - panelW, 0, panelW, panelH);
    cp5View.getWindow().resetMouseOver();
    cp5Animation.getWindow().resetMouseOver();
    lastLayoutW = layoutW;
    lastLayoutH = layoutH;
  }

  private void layoutEmbeddedPanelsIfNeeded(boolean force) {
    syncSurfaceSizeIfNeeded();
    layoutEmbeddedPanelsIfNeeded(force, layoutWidth(), layoutHeight());
  }

  /** True when x is over a left/right embedded panel (not the 3D center). */
  private boolean isOverSidePanel(int x) {
    int layoutW = layoutWidth();
    int panelW = sidePanelWidth(layoutW);
    return x < panelW || x >= layoutW - panelW;
  }

  private boolean isMouseOverSidePanel() {
    return isOverSidePanel(mouseX);
  }

  private boolean isMouseOverUI() {
    return isMouseOverSidePanel();
  }

  /** Panel width follows the current window (1/5), capped so panels never overlap. */
  private int sidePanelWidth() {
    return sidePanelWidth(layoutWidth());
  }

  private int sidePanelWidth(int windowWidth) {
    if (windowWidth <= 0) {
      return 160;
    }
    int preferred = Math.max(160, windowWidth / 5);
    // Keep a usable center view; on split-screen/narrow windows shrink side strips.
    int minCenter = Math.max(120, windowWidth / 4);
    int maxPerSide = (windowWidth - minCenter) / 2;
    if (maxPerSide < 80) {
      maxPerSide = Math.max(1, windowWidth / 4);
    }
    return Math.min(preferred, maxPerSide);
  }

  private void drawEmbeddedUI() {
    if (scene == null || cp5View == null || cp5Animation == null) {
      return;
    }
    scene.beginScreenDrawing();
    drawSidePanelBackgrounds();
    if (uipwView != null) {
      uipwView.updateButton();
    }
    if (uipwAnimation != null) {
      uipwAnimation.updateButton();
    }
    cp5View.update();
    cp5View.draw();
    cp5Animation.update();
    cp5Animation.draw();
    scene.endScreenDrawing();
  }

  private void drawSidePanelBackgrounds() {
    noStroke();
    fill(
        Visualization3DConfig.UIPW_BACKGROUND.getRed(),
        Visualization3DConfig.UIPW_BACKGROUND.getGreen(),
        Visualization3DConfig.UIPW_BACKGROUND.getBlue(),
        220);
    int layoutW = layoutWidth();
    int layoutH = layoutHeight();
    int panelW = sidePanelWidth(layoutW);
    rect(0, 0, panelW, layoutH);
    rect(layoutW - panelW, 0, panelW, layoutH);
  }

  @Override
  public synchronized void export() {
    isExporting = true;
  }

  private void endExport() {
    if (isExporting) {
      isExporting = false;
      endRaw();

      stillExporting.countDown();

    }
  }

  private void startExport() {
    if (isExporting) {
      if(pathchoice==0){
        path=chooseDir();
        pathchoice=1;
      }

      String modelName = MeshProvider.getInstance().getModel().getModelName();
      StringBuilder exportFileName = new StringBuilder();
      switch (processor.getDisplayState().getState()) {
        case MODEL_VIEW:
          exportFileName.append(modelName)
                  .append("-")
                  .append(IndexExport)
                  .append(".obj");
          break;
        case PAVED_VIEW:
          exportFileName.append(modelName)
                  .append("-")
                  .append(IndexExport)
                  .append("-Paved")
                  .append(".obj");
          break;
        case ANIMATION_VIEW:
          if(option== AnimationProcessor.AnimationOption.BY_LAYER)
          {exportFileName.append("Layers/layer")
                  .append("-")
                  .append(IndexExport)
                  .append(".obj");
          }
          else if(option== AnimationProcessor.AnimationOption.BY_BIT)
          {
            if(IndexExport!=0&&IndexExport % 72==0) num_batch++;
            exportFileName.append("Bits/lot")
                    .append("-"+num_batch)
                    .append("/")
                    .append(IndexExport)
                    .append("_"+num_batch)
                    .append(".obj");
          }
          else if(option== AnimationProcessor.AnimationOption.BY_SUB_BIT)
          {
            if(IndexExport!=0&&IndexExport % 72==0) num_batch++;
            exportFileName.append("SubBits/lot")
                    .append("-"+num_batch)
                    .append("/")
                    .append(IndexExport)
                    .append("_"+num_batch)
                    .append(".obj");
          }
          else if(option== AnimationProcessor.AnimationOption.BY_BATCH)
          {exportFileName.append("Batches/lot")
                  .append("-")
                  .append(IndexExport)
                  .append(".obj");
          }
          break;
        default:
          throw new IllegalStateException(
                  "Unexpected value: " + processor.getDisplayState().getState());
      }
      logger.logDEBUGMessage("Exporting " + exportFileName);
      beginRaw(Visualization3DConfig.EXPORT_3D_RENDERER,path+"\\"+ exportFileName.toString());
    IndexExport++;
    }
  }


  /**
   * Method to choose a directory for the exported 3d objects
   * @return the path of the chosen directory
   */
  private String  chooseDir(){
    JFileChooser jf=new JFileChooser();
    jf.setDialogTitle("choose a directory");
    jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    jf.showOpenDialog(null);
    File f=jf.getSelectedFile();
    return f.getAbsolutePath();
  }

  /**
   * exports all shapes as 3d obj files when EXPORTSIM button is clicked.
   * the option is automatically put on ONE BY ONE because we need each object by itself.
   * We only have to precise the type of objects to export(SubBits,Layer,Batch...)
   * exportAll method rely on export() method of EXPORT BUTTON that exports the current displayed shape, in exportAll we
   * display then export each shape one by one
   */
  public void exportAll(){
    ExpInd.set(0);
    try {
      waitshaping.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    animationSpeed=0.001;
    while(ExpInd.get() < animationShapes.size()){
      try {
        notyet.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      if(ExpInd.get()==ind.get()){
        processor.export();
        try {
          stillExporting.await();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        stillExporting=new CountDownLatch(1);
        ExpInd.set(ExpInd.get()+1);
        exported.countDown();
        exported=new CountDownLatch(1);
      }
    }
    exported.countDown();
    processor.deactivateAnimation();
    stillExporting=new CountDownLatch(1);
    waitshaping=new CountDownLatch(1);
    notyet=new CountDownLatch(1);
    pathchoice=0;
  }




  private synchronized void displayShape() {
    switch (processor.getDisplayState().getState()) {
      case MODEL_VIEW:
        scene.drawFrames();
        break;
      case PAVED_VIEW:
        if(WindowStatus==1) drawMesh();
        if(WindowStatus>=2) drawtest();
        break;
      case ANIMATION_VIEW:
        drawAnimationShape();
        break;
      default:
        throw new IllegalStateException(
                "Unexpected value: " + processor.getDisplayState().getState());
    }
  }

  private void drawMesh() {//System.out.println("drawingMesh ?");
    if (meshShape != null) {
      Vector3 v = MeshProvider.getInstance().getModel().getPos();
      pushMatrix();
      translate((float) v.x, (float) v.y, (float) v.z);
      shape(meshShape);
      popMatrix();
    }
  }



  private void drawtest(){
    if (meshShapes.get(1) != null) {
      Vector3 v = MeshProvider.getInstance().getModel().getPos();
      pushMatrix();
      translate((float) v.x, (float) v.y, (float) v.z);
      shape(meshShapes.get(1));
     /*
        for (int i=0;i<meshShapes.get(1).getChildren().length-meshShapes.get(1).getChildren().length+3;i++){

         shape(meshShapes.get(1).getChild(i));
      meshShapes.get(1).getChild(0).setFill(255);
     }*/
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

  private  void drawWorkingSpace() {

    pushMatrix();
    translate(Xpos,Ypos,Zpos);


    shape(rectange);
     popMatrix();

if(!getpausing()){
    float step = getWorkingSpaceMoveStep();
    if (Xpos < pos) {
      Xpos = Math.min(Xpos + step, pos);
    } else if (Xpos > pos) {
      Xpos = Math.max(Xpos - step, pos);
    }
if(Xpos==pos){
  movingWorkSpace.countDown();
  movingWorkSpace=new CountDownLatch(1);
  }
}

     /* rect(-printerX / 2 - CraftConfig.workingWidth - 20,
            -printerY / 2, CraftConfig.workingWidth,
            CraftConfig.printerY);*/

  //CraftConfig.workingWidth=300
  //   CraftConfig.printerY=2000.00
  }

  /**
   * Movement step of the red working-space rectangle, scaled with {@link AnimationProcessor#animationSpeed}.
   * Halving animationSpeed (bits placed 2x faster) doubles the rectangle's travel speed.
   */
  private float getWorkingSpaceMoveStep() {
    double speed = animationSpeed <= 0
        ? Visualization3DConfig.speed_coefficient_max
        : animationSpeed;
    return (float) (Visualization3DConfig.speed_coefficient_default / speed);
  }

    @Override
  public void close() throws Exception {
  }

  @Override
  public void mouseClicked(java.awt.event.MouseEvent e) {

  }

  @Override
  public void mousePressed(java.awt.event.MouseEvent e) {

  }

  @Override
  public void mouseReleased(java.awt.event.MouseEvent e) {

  }

  @Override
  public void mouseEntered(java.awt.event.MouseEvent e) {
  }

  @Override
  public void mouseExited(java.awt.event.MouseEvent e) {

  }

  /**
   * Currently allows communications between the 2D view and the 3D view.
   *
   * @param evt A PropertyChangeEvent object describing the event source
   *          and the property that has changed.
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    switch (evt.getPropertyName()){
      case "CLOSE_PROJECT" :
        this.closeEntire3DView();
        break;
    }
  }

  /**
   * Ends the current shown instance of the 3D view
   *
   * Stops listening to MeshWindow.
   * Terminates embedded UI panels and the current displayed 3D view.
   */
  private void closeEntire3DView(){
    BaseVisualization3DView.meshWindow.removePropertyChangeListener(this);
    processor.onTerminated();
    resetAnimationSessionState();
    disposeParameterPanels();
    //Should get rid of the PApplet objects
    this.dispose();
    if(WindowStatus!=0){
      //Necessary, otherwise the window will stay opened (although completely disabled)
      this.win.destroy();
      WindowStatus=0;
    }

  }
}
