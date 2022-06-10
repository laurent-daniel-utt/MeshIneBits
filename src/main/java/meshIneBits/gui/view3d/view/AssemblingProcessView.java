package meshIneBits.gui.view3d.view;

import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.START_CUTTING_MACHINE;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.STOP_CUTTING_MACHINE;

import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Vector;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view3d.Processor.AssemblingMachineProcessor;
import meshIneBits.gui.view3d.Visualization3DConfig;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PShape;
import processing.opengl.PJOGL;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.Scene;

public class AssemblingProcessView extends PApplet {


  public interface AssemblingProcessViewListener {

    void onActionListener(Object callbackObj, String event, Object value);
  }

  private Scene scene;
  private Vector<PShape> shapes;
  private AssemblingProcessViewListener listener;
  private ControlP5 controlP5;
  private AssemblingMachineProcessor processor;

  private Button startButton;
  private Button stopButton;

  private final DecimalFormat df;

  {
    df = new DecimalFormat("#.##");
    df.setMaximumFractionDigits(2);
    df.setRoundingMode(RoundingMode.CEILING);
  }

  public void settings() {
    size(Visualization3DConfig.ASSEMBLING_PROCESS_VIEW_WIDTH,
        Visualization3DConfig.ASSEMBLING_PROCESS_VIEW_HEIGHT, P3D);
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

  public void setup() {
    configWindow(
        Visualization3DConfig.ASSEMBLING_PROCESS_VIEW_TITLE,
        Visualization3DConfig.ASSEMBLING_PROCESS_VIEW_LOCATION_X,
        Visualization3DConfig.ASSEMBLING_PROCESS_VIEW_LOCATION_Y);
    init3DScene(Visualization3DConfig.V3D_EYE_POSITION, Visualization3DConfig.V3D_RADIUS);
    initProcessor();
    setupButton();
  }

  private void setupButton() {
    controlP5 = new ControlP5(this);
    controlP5.setAutoDraw(false);
    int component_label_color = 255;
    PFont text_font_default = createFont("arial bold", 14);

    startButton = controlP5.
        addButton(START_CUTTING_MACHINE)
        .setLabel("Start")
        .setSize(100, 100)
        .setColorLabel(component_label_color)
        .setFont(text_font_default);
    stopButton = controlP5.
        addButton(STOP_CUTTING_MACHINE)
        .setLabel("Stop")
        .setSize(100, 100)
        .setColorLabel(component_label_color)
        .setFont(text_font_default);
    initComponentPositions();

  }

  private void initComponentPositions() {
    float[] startPosition = new float[]
        {0.4f * width - (float) startButton.getWidth() / 2, 0.1f * height};
    float[] pausePosition = new float[]
        {0.6f * width - (float) stopButton.getWidth() / 2, 0.1f * height};
    startButton.setPosition(startPosition);
    stopButton.setPosition(pausePosition);
  }

  public void draw() {
    background(Visualization3DConfig.V3D_BACKGROUND.getRGB());
    lights();
    ambientLight(
        Visualization3DConfig.V3D_AMBIENT_LIGHT.getRed(),
        Visualization3DConfig.V3D_AMBIENT_LIGHT.getGreen(),
        Visualization3DConfig.V3D_AMBIENT_LIGHT.getBlue());
    drawWorkspace();
    drawWorkingSpace();
    displayShapes();

    scene.beginScreenDrawing();
    controlP5.draw();
    scene.endScreenDrawing();
  }

  private void displayShapes() {
    if (shapes != null && shapes.size() > 0) {
      shapes.forEach(this::shape);
    }
  }

  private void drawWorkspace() {
    pushMatrix();
    noFill();
    translate(0, 0, Visualization3DConfig.PRINTER_Z / 2);
    strokeWeight(2);
    stroke(0);
    box(Visualization3DConfig.PRINTER_X,
        Visualization3DConfig.PRINTER_Y,
        Visualization3DConfig.PRINTER_Z);
    popMatrix();
    scene.pg().pushStyle();
    stroke(80);
    scene.pg().beginShape(LINES);
    for (int i = -(int) Visualization3DConfig.PRINTER_X / 2;
        i <= Visualization3DConfig.PRINTER_X / 2; i += 100) {
      vertex(i, Visualization3DConfig.PRINTER_Y / 2, 0);
      vertex(i, -Visualization3DConfig.PRINTER_Y / 2, 0);
    }
    for (int i = -(int) Visualization3DConfig.PRINTER_Y / 2;
        i <= Visualization3DConfig.PRINTER_Y / 2; i += 100) {
      vertex(Visualization3DConfig.PRINTER_Y / 2, i, 0);
      vertex(-Visualization3DConfig.PRINTER_Y / 2, i, 0);
    }
    scene.pg().endShape();
    scene.pg().popStyle();
  }

  private void drawWorkingSpace() {
    stroke(255, 0, 0);
    rect(-Visualization3DConfig.PRINTER_X / 2 - CraftConfig.workingWidth - 20,
        -Visualization3DConfig.PRINTER_Y / 2, CraftConfig.workingWidth,
        CraftConfig.printerY);
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

  private void initProcessor() {
    processor = new AssemblingMachineProcessor(this, this::updateInfos);
    listener = processor;
  }

  public void controlEvent(ControlEvent theEvent) {
    if (listener == null) {
      return;
    }
    switch (theEvent.getName()) {
      case START_CUTTING_MACHINE:
      case STOP_CUTTING_MACHINE:
        listener.onActionListener(this, theEvent.getName(), theEvent.getValue());
        break;
    }
  }

  private void configWindow(String title, int locationX, int locationY) {
    this.surface.setResizable(true);
    this.surface.setTitle(title);
    this.surface.setLocation(locationX, locationY);
    setCloseOperation();
  }

  public void updateInfos(Vector<PShape> shapes,
      String subBitID,
      String bitID,
      String layerId,
      String nbSubBit,
      String position) {
    this.shapes = shapes;
  }

  public static void startProcessingModelView() {
    PApplet.main(AssemblingProcessView.class.getCanonicalName());
  }
}
