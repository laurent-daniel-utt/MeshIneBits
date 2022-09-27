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
import meshIneBits.gui.view3d.provider.MeshProvider;
import meshIneBits.gui.view3d.util.animation.AnimationIndexIncreasedListener;
import meshIneBits.gui.view3d.util.animation.AnimationProcessor;
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

import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static meshIneBits.gui.utilities.UPPPaveMesh.button;
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
  //private  int IndexExport = 0;
//private float Xpos=-printerX / 2 - CraftConfig.workingWidth - 20,Ypos=-printerY / 2,Zpos=0;

public static float Xpos=0,Ypos=0,Zpos=0;
    public static ArrayList<ArrayList<Strip>> meshstrips;
  private boolean isExporting = false;
  // false=closed/true=opened
  public static int WindowStatus=0;
  private int it=0;
public static Thread thread;
private ActionListener ev;
private ActionListener refListener;
private PShape meshShape2=new PShape();
private MouseListener refMouseListener;

private PShape rectange;
  //private MouseEvent event;
private CountDownLatch stillExporting=new CountDownLatch(1);
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

  if (!MeshProvider.getInstance().isAvailable()) {
    logger.logERRORMessage("Model and Mesh are not available!");
    Logger.updateStatus("Model and Mesh are not available!");
    return;
  }
  WindowStatus=1;
  System.out.println("canonical name="+BaseVisualization3DView.class.getCanonicalName()+" Thread="+Thread.currentThread().getName());
  PApplet.main(BaseVisualization3DView.class.getCanonicalName());


}



  public void settings() {System.out.println("in settings");
    size(Visualization3DConfig.V3D_WINDOW_WIDTH, Visualization3DConfig.V3D_WINDOW_HEIGHT, P3D);

      PJOGL.setIcon("resources/icon.png");

  }






    protected void handleMouseEvent(MouseEvent event) {

      final int action = event.getAction();
      if (action != MouseEvent.EXIT && action==MouseEvent.CLICK ) {



          //background(Visualization3DConfig.V3D_BACKGROUND.getRGB());
         //init3DScene(Visualization3DConfig.V3D_EYE_POSITION, Visualization3DConfig.V3D_RADIUS);//this line is making the window stuck

        processor.onTerminated();
        init3DFrame();

       WindowStatus=2;
                noLoop();
                initProcessor();
                //meshShapes.put(1, meshShape2);
                meshShapes.put(1,processor.getModelProvider().getMeshShape());
                frame.setShape(shape);

                System.out.println("depthR="+shape.getChild(1).getDepth());
                uipwAnimation.closeWindow();
                uipwView.closeWindow();
                uipwController.close();
                initControlComponent();
                initParameterWindow();
                initModelChangesListener((ModelChangesListener) uipwView);
                runSketch(new String[]{"--display=1", "Projector"}, uipwView);
                runSketch(new String[]{"--display=1", "Projector"}, uipwAnimation);
                // initDisplayParameterWindows();
        Xpos=0;
        Zpos=0;
        initWorkingSpace();


                loop();
                System.out.println("where");

         // c'est cette ligne qui permet de changer le meshShape mais c'est aussi qui rend les bits bizzares
        //shape = processor.getModelProvider().getModelShape();//with this line the mesh disappear then pop up red


          //meshShapes.put(1, processor.getModelProvider().getMeshShape());



        //meshShape=processor.getModelProvider().getMeshShape();

      }

       if(action==MouseEvent.MOVE){  // System.out.println("Moved");
          // Xpos+=10;
           //Ypos+=10;
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








/*

  public synchronized void refresh(){

    refListener= new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(WindowStatus>0) {WindowStatus=2;
          // scene.dispose();
          noLoop();

          System.out.println("Noloop");
System.out.println("Thread in ref="+Thread.currentThread().getName());

              try {
                r.await();
                System.out.println("lqslqlqslq");
                r=new CountDownLatch(1);
              } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
              }
              //initWorkspace();// create the box of work space and centre the axes and the model
              // init3DScene(Visualization3DConfig.V3D_EYE_POSITION, Visualization3DConfig.V3D_RADIUS);
              //init3DScene(Visualization3DConfig.V3D_EYE_POSITION, Visualization3DConfig.V3D_RADIUS);
              processor.onTerminated();
              init3DFrame();
              initProcessor();// c'est cette ligne qui permet change le meshShape
              shape = processor.getModelProvider().getModelShape();//with this line the mesh disappear then pop up red
              meshShapes.put(1, processor.getModelProvider().getMeshShape());
              frame.setShape(shape);


              uipwAnimation.closeWindow();
              uipwView.closeWindow();
              uipwController.close();
              initControlComponent();
              initParameterWindow();
              initModelChangesListener((ModelChangesListener) uipwView);
              initDisplayParameterWindows();

              loop();

              System.out.println("meshShapes1  in refresh="+meshShapes.get(1)+"Its size="+processor.getModelProvider().getMeshShape().getChildren().length);



        }
      }
    };

  button.addActionListener(refListener);
}

*/
  // TODO uncomment the line that remove the ActioneListener at(l.288) if u want this method below to work

 /*
  public synchronized void refresh(){

     ev=new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(WindowStatus>0) {WindowStatus=2;

//exit();
          // win.destroy();
          Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                System.out.println("waiting");
                r.await();
                System.out.println("done waiting");
              } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
              }
              exit();
              win.destroy();

              startProcessingModelView();
              r=new CountDownLatch(1);

            }
          });t.start();
//setCloseOperation();
        }
      }
    };



    button.addActionListener(ev);
  }
*/
  private void setCloseOperation() {


    //Removing close listeners
     win = (com.jogamp.newt.opengl.GLWindow) surface.getNative();
    for (com.jogamp.newt.event.WindowListener wl : win.getWindowListeners()) {
      win.removeWindowListener(wl);
    }
    win.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);


    win.addWindowListener(new WindowAdapter() {
      public void windowDestroyed(WindowEvent e) { WindowStatus=0;
       // System.out.println(" WindowStatus"+ WindowStatus);
        button.removeActionListener( ev);
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
    System.out.println("Thread in setUp="+Thread.currentThread().getName());
    configWindow(
            Visualization3DConfig.VISUALIZATION_3D_WINDOW_TITLE,
            Visualization3DConfig.V3D_WINDOW_LOCATION_X,
            Visualization3DConfig.V3D_WINDOW_LOCATION_Y);

    initWorkspace();// create the box of work space and centre the axes and the model
    init3DScene(Visualization3DConfig.V3D_EYE_POSITION, Visualization3DConfig.V3D_RADIUS);
    init3DFrame();

    initProcessor();
      meshShape = processor.getModelProvider().getMeshShape();
    shape = processor.getModelProvider().getModelShape();
      meshShape2=meshShape;
    if(MeshProvider.getInstance().getCurrentMesh().isPaved()) meshstrips=processor.getModelProvider().getMeshstrips();
    int s=0;
     //for(int i=0;i<meshstrips.size();i++)
     //{s=s+meshstrips.get(i).size();

     //}
   // System.out.println("we have "+s+" strips");
     meshShapes.put(0,meshShape);


    frame.setShape(shape);

    initControlComponent();
    initParameterWindow();
    initModelChangesListener((ModelChangesListener) uipwView);
    initDisplayParameterWindows();
    initWorkingSpace();


  }
private void initWorkingSpace(){
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
  System.out.println("Or="+(-printerX / 2 - CraftConfig.workingWidth - 20) );
//  System.out.println("Size1="+meshstrips.get(0).get(0).getBits().size());
  //System.out.println("Min1="+(float) meshstrips.get(0).get(0).getBits().get(0).getMinX());


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
    if (mcListener != null) { System.out.println("depth="+df.format(shape.getDepth() ));
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

  private void initProcessor() {System.out.println("CurrentMesh="+MeshProvider.getInstance().getCurrentMesh());

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

//System.out.println("drawing");

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
    System.out.println("exporting ended");
      stillExporting.countDown();

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
          {exportFileName.append("Layer/layer")
                  .append("-")
                  .append(IndexExport)
                  //.append("-Animation")
                  .append(".obj");
          }
          else if(option== AnimationProcessor.AnimationOption.BY_BIT)
          {exportFileName.append("Bit/bit")
                  .append("-")
                  .append(IndexExport)
                 // .append("-Animation")
                  .append(".obj");
          }
          else if(option== AnimationProcessor.AnimationOption.BY_SUB_BIT)
          {exportFileName.append("Sub_Bit/subBit")
                  .append("-")
                  .append(IndexExport)
                 // .append("-Animation")
                  .append(".obj");
          }
          else if(option== AnimationProcessor.AnimationOption.BY_BATCH)
          {exportFileName.append("Batch/lot")
                  .append("-")
                  .append(IndexExport)
                  //.append("-Animation")
                  .append(".obj");
          }

          break;
        default:
          throw new IllegalStateException(
                  "Unexpected value: " + processor.getDisplayState().getState());
      }
      logger.logDEBUGMessage("Exporting " + exportFileName);

      beginRaw(Visualization3DConfig.EXPORT_3D_RENDERER, exportFileName.toString());
      System.out.println("after Raw");
      IndexExport++;
    }
  }
/*
  public void exportAll() {
System.out.println("thread="+Thread.currentThread().getName());
  int l=0;
   // System.out.println("ExpInd="+ExpInd.get());
    animationSpeed=0.3;
    System.out.println("Speed="+animationSpeed);

    processor.pauseAnimation();
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    processor.pauseAnimation();
    while (ExpInd.get()<animationShapes.size()){//processor.pauseAnimation();
      //System.out.println("SIZE="+animationShapes.size()+" EXpInd="+ExpInd.get());
      //System.out.println("expInd="+ExpInd.get()+" ind="+ind.get());

      if(ExpInd.get()==ind.get()) { System.out.println("in expo ?");
    processor.export();
        //System.out.println("ExpindInIF="+ExpInd);
    ExpInd.set(ExpInd.get()+1);
        //processor.pauseAnimation();

      }
      if(ExpInd.get()==animationShapes.size())
      {
        try {
          Thread.sleep(1500);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        processor.deactivateAnimation();

      }

}
    System.out.println("ExpindFinal="+ExpInd);
    processor.deactivateAnimation();

  }

*/
  public void exportAll(){ExpInd.set(0);
    try {

      waitshaping.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    //System.out.println("SIZE="+animationShapes.size());

    animationSpeed=0.001;

    while(ExpInd.get() < animationShapes.size()){
      try {
        notyet.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      System.out.println("ExpInd="+ExpInd.get()+" ind="+ind.get());

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
 System.out.println("exported freedB");
   exported=new CountDownLatch(1);

 }


}
   exported.countDown();
    processor.deactivateAnimation();
    stillExporting=new CountDownLatch(1);
    waitshaping=new CountDownLatch(1);
    notyet=new CountDownLatch(1);
  }








/*
  public void exportAll2(String fileName, PShape s){
Thread t=new Thread(new Runnable() {
  @Override
  public void run() {
    StringBuilder sb = new StringBuilder();
    sb.append("what da heck");
    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), Charset.forName("UTF-8"),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
      try {
        writer.write(s.toString());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }
});
t.start();
  //animationShapes.forEach(this::shape);

}*/










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
      //case MODIFYED_VIEW:


        //break;
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
   // System.out.println("drawingTest ?");
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

      //animationShapes.forEach(this::shape);
for(int i=0;i<animationShapes.size();i++) {

shape(animationShapes.get(i));


}

popMatrix();
//Xpos++;
     //   if(option== AnimationOption.BY_BIT){

       // }


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

    pushMatrix();
    translate(Xpos,Ypos,Zpos);


    shape(rectange);
     popMatrix();
if(forward())Xpos++;
if(backward())Xpos--;
if(Xpos==pos){
  movingWorkSpace.countDown();
  movingWorkSpace=new CountDownLatch(1);
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
System.out.println("u in");
  }

  @Override
  public void mouseExited(java.awt.event.MouseEvent e) {

  }
}
