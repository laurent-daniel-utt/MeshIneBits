package meshIneBits.gui.view3d.view;

import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.oldversion.ProcessingModelView.ModelChangesListener;
import meshIneBits.gui.view3d.provider.MeshProvider;
import processing.core.PApplet;
import processing.core.PShape;
import processing.opengl.PJOGL;
import remixlab.proscene.Scene;

public class AssemblingProcessView extends PApplet {

  private Scene scene;
  private PShape shape;

  private final DecimalFormat df;
  {
    df = new DecimalFormat("#.##");
    df.setMaximumFractionDigits(2);
    df.setRoundingMode(RoundingMode.CEILING);
  }

  public void settings() {
    size(Visualization3DConfig.ASSEMBLING_PROCESS_VIEW_WIDTH, Visualization3DConfig.ASSEMBLING_PROCESS_VIEW_HEIGHT, P3D);
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
//    initWorkspace();
//    init3DScene(Visualization3DConfig.V3D_EYE_POSITION, Visualization3DConfig.V3D_RADIUS);
//    init3DFrame();
//    initProcessor();
//    shape = processor.getModelProvider().getModelShape();
//    meshShape = processor.getModelProvider().getMeshShape();
//    frame.setShape(shape);
//    initControlComponent();
//    initParameterWindow();
//    initModelChangesListener((ModelChangesListener) uipwView);
//    initDisplayParameterWindows();

  }

  private void configWindow(String title, int locationX, int locationY) {
    this.surface.setResizable(true);
    this.surface.setTitle(title);
    this.surface.setLocation(locationX, locationY);
    setCloseOperation();
  }

  public static void startProcessingModelView() {
    PApplet.main(AssemblingProcessView.class.getCanonicalName());
  }
}
