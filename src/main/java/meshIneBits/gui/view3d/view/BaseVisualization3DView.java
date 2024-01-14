package meshIneBits.gui.view3d.view;

import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import controlP5.ControlP5;
import meshIneBits.Strip;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view3d.Processor.BaseVisualization3DProcessor;
import meshIneBits.gui.view3d.Processor.IVisualization3DProcessor;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.oldversion.ProcessingModelView.ModelChangesListener;
import meshIneBits.gui.view3d.provider.ProjectProvider;
import meshIneBits.gui.view3d.util.animation.AnimationIndexIncreasedListener;
import meshIneBits.util.CustomLogger;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector3;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.event.MouseEvent;
import processing.opengl.PJOGL;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.InteractiveFrame;
import remixlab.proscene.Scene;

import javax.swing.*;
import java.awt.event.MouseListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
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
public class BaseVisualization3DView extends AbstractVisualization3DView implements AutoCloseable,MouseListener {

  private static final CustomLogger logger = new CustomLogger(BaseVisualization3DView.class);

  public static UIParameterWindow uipwAnimation;
  public static UIParameterWindow uipwView;
  public static UIPWController uipwController;
  public static IVisualization3DProcessor processor;
  private ModelChangesListener mcListener;
  public static CountDownLatch waitshaping=new CountDownLatch(1);
  private CustomInteractiveFrame frame;
  private Scene scene;
  private ControlP5 cp5;
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


public void play(){

  if (!ProjectProvider.getInstance().isAvailable()) {
    logger.logERRORMessage("Model and Project are not available!");
    Logger.updateStatus("Model and Project are not available!");
    return;
  }
  WindowStatus=1;
  PApplet.main(BaseVisualization3DView.class.getCanonicalName());


}



  public void settings() {
    size(Visualization3DConfig.V3D_WINDOW_WIDTH, Visualization3DConfig.V3D_WINDOW_HEIGHT, P3D);
    PJOGL.setIcon("resources/icon.png");
  }


  /**
   * Event handler to refresh the 3D interface when clicking inside it
   * @param event
   */
  protected void handleMouseEvent(MouseEvent event) {

    final int action = event.getAction();
    if (action != MouseEvent.EXIT && action==MouseEvent.CLICK ) {

      processor.onTerminated();
      init3DFrame();
      WindowStatus=2;
      noLoop();
      initProcessor();
      meshShapes.put(1,processor.getModelProvider().getMeshShape());
      frame.setShape(shape);

      uipwAnimation.closeWindow();
      uipwView.closeWindow();
      uipwController.close();
      initControlComponent();
      initParameterWindow();
      initModelChangesListener((ModelChangesListener) uipwView);
      runSketch(new String[]{"--display=1", "Projector"}, uipwView);
      runSketch(new String[]{"--display=1", "Projector"}, uipwAnimation);

      pos=0;
      Zpos=0;
      Xpos=0;
      initWorkingSpace();
      processor.deactivateAnimation();
      if(ProjectProvider.getInstance().getCurrentMesh().isPaved()) meshstrips=processor.getModelProvider().getMeshstrips();
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

    handleMethods("mouseEvent", new Object[] { event });

    switch (action) {


      case MouseEvent.CLICK:
        mouseClicked(event);
        break;
      case MouseEvent.ENTER:
        mouseEntered(event);
        break;
      case MouseEvent.EXIT:
        mouseExited(event);
        break;
      case MouseEvent.MOVE:
        mouseMoved(event);
        break;
    }

  }









  private void setCloseOperation() {


    //Removing close listeners
     win = (com.jogamp.newt.opengl.GLWindow) surface.getNative();
    for (com.jogamp.newt.event.WindowListener wl : win.getWindowListeners()) {
      win.removeWindowListener(wl);
    }
    win.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);


    win.addWindowListener(new WindowAdapter() {
      public void windowDestroyed(WindowEvent e) { WindowStatus=0;
      Logger.updateStatus("");


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

    initWorkspace();// create the box of work space and centre the axes and the model
    init3DScene(Visualization3DConfig.V3D_EYE_POSITION, Visualization3DConfig.V3D_RADIUS);
    init3DFrame();
    System.out.println("0");
    initProcessor();
    System.out.println("0.25");
      meshShape = processor.getModelProvider().getMeshShape();
    shape = processor.getModelProvider().getModelShape();
    System.out.println("0.5");
    if(ProjectProvider.getInstance().getCurrentMesh().isPaved()) meshstrips=processor.getModelProvider().getMeshstrips();
     meshShapes.put(0,meshShape);
    frame.setShape(shape);

    initControlComponent();
    System.out.println("1");
    initParameterWindow();
    System.out.println("2");
    initModelChangesListener((ModelChangesListener) uipwView);
    System.out.println("3");
    initDisplayParameterWindows();
    System.out.println("4");
    initWorkingSpace();

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

      processor = new BaseVisualization3DProcessor(ProjectProvider.getInstance().getCurrentMesh(),
            this);
  }

  private void init3DFrame() {
    if (scene == null) {
      return;
    }

    frame = new CustomInteractiveFrame(scene);
    //set position of frame in scene
    frame.translate(
            (float) ProjectProvider.getInstance().getModel().getPos().x,
            (float) ProjectProvider.getInstance().getModel().getPos().y,
            (float) ProjectProvider.getInstance().getModel().getPos().z);
    customFrameBindings(frame);
  }

  private void customFrameBindings(InteractiveFrame frame) {
    frame.removeBindings();
    frame.setHighlightingMode(InteractiveFrame.HighlightingMode.NONE);
    frame.setPickingPrecision(InteractiveFrame.PickingPrecision.ADAPTIVE);
    frame.setGrabsInputThreshold(scene.radius() / 3);
    frame.setRotationSensitivity(3);
    if (!ProjectProvider.getInstance().getCurrentMesh()
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

      stillExporting.countDown();

    }
  }

  private void startExport() {
    if (isExporting) {
      if(pathchoice==0){
        path=chooseDir();
        pathchoice=1;
      }

      String modelName = ProjectProvider.getInstance().getModel().getModelName();
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
          if(option== AnimationOption.BY_LAYER)
          {exportFileName.append("Layers/layer")
                  .append("-")
                  .append(IndexExport)
                  .append(".obj");
          }
          else if(option== AnimationOption.BY_BIT)
          {
            if(IndexExport!=0&&IndexExport % 72==0) num_batch++;
            exportFileName.append("Bits/lot")
                    .append("-"+num_batch)
                    .append("/")
                    .append(IndexExport)
                    .append("_"+num_batch)
                    .append(".obj");
          }
          else if(option== AnimationOption.BY_SUB_BIT)
          {
            if(IndexExport!=0&&IndexExport % 72==0) num_batch++;
            exportFileName.append("SubBits/lot")
                    .append("-"+num_batch)
                    .append("/")
                    .append(IndexExport)
                    .append("_"+num_batch)
                    .append(".obj");
          }
          else if(option== AnimationOption.BY_BATCH)
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
      Vector3 v = ProjectProvider.getInstance().getModel().getPos();
      pushMatrix();
      translate((float) v.x, (float) v.y, (float) v.z);
      shape(meshShape);
      popMatrix();
    }
  }



  private void drawtest(){
    if (meshShapes.get(1) != null) {
      Vector3 v = ProjectProvider.getInstance().getModel().getPos();
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
      Vector3 v = ProjectProvider.getInstance().getCurrentMesh().getModel().getPos();
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
    if(forward())Xpos++;
if(backward())Xpos--;
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
public boolean  forward(){
    if(Xpos-pos<0){
      if(Xpos-pos<-1){
        return true;
      }
       else {
         Xpos=pos;
      return false;
       }
    }
else {return false;}


}
  public boolean  backward(){
    if(Xpos-pos>0){
      if(Xpos-pos>1){
        return true;
      }
      else {
        Xpos=pos;
        return false;
      }
    }
    else {return false;}


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
}
