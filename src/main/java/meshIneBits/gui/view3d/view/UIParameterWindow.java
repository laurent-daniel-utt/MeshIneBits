package meshIneBits.gui.view3d.view;

import controlP5.ControlEvent;
import controlP5.ControlP5;
import java.awt.Frame;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.util.CustomLogger;
import processing.awt.PSurfaceAWT;
import processing.core.PApplet;

public abstract class UIParameterWindow extends PApplet {

  public static class WindowBuilder {

    private String title;
    private int width;
    private int height;
    private UIPWListener listener;

    public WindowBuilder setTitle(String title) {
      this.title = title;
      return this;
    }

    public WindowBuilder setSize(int width, int height) {
      this.width = width;
      this.height = height;
      return this;
    }

    public WindowBuilder setListener(UIPWListener listener) {
      this.listener = listener;
      return this;
    }

    public <T extends UIParameterWindow> T build(Class<T> c)
        throws NoSuchMethodException,
        InvocationTargetException,
        InstantiationException,
        IllegalAccessException {
      Constructor<T> constructor = c.getConstructor();
      T obj = constructor.newInstance();
      obj.setTitle(title).setSizeWindow(width, height).setUIControllerListener(listener);
      return obj;
    }

  }


  private UIPWListener listener;
  final CustomLogger logger = new CustomLogger(this.getClass());

  private ControlP5 control;
  private String title;
  private boolean exited = false;

  public abstract void onOpen();

  public abstract void onClose();

  protected abstract void generateButton();

  protected abstract void updateButton();

  @SuppressWarnings("unused")
  public abstract void controlEvent(ControlEvent theEvent);

  public void setup() {
    surface.setTitle(title);
    surface.setResizable(true);
    frameRate(Visualization3DConfig.UIPW_FRAMERATE);
    control = new ControlP5(this);
    generateButton();
    control.enableShortcuts();
  }

  public void draw() {
    background(
        Visualization3DConfig.UIPW_BACKGROUND.getRed(),
        Visualization3DConfig.UIPW_BACKGROUND.getGreen(),
        Visualization3DConfig.UIPW_BACKGROUND.getBlue());
    noStroke();
    updateButton();
  }

  public UIPWListener getListener() {
    return listener;
  }

  public ControlP5 getControl() {
    return control;
  }

  public UIParameterWindow setTitle(String title) {
    this.title = title;
    return this;
  }

  public UIParameterWindow setUIControllerListener(UIPWListener listener) {
    this.listener = listener;
    return this;
  }

  @Override
  public void exitActual() {
    PSurfaceAWT.SmoothCanvas surf = (PSurfaceAWT.SmoothCanvas) this.getSurface().getNative();
    Frame frame = surf.getFrame();
    frame.dispose();
  }

  public void closeWindow() {
    onClose();
    listener = null;
    exit();
  }

  @Override
  public void exit() {
    if (!exited) {
      super.exit();
      exited = true;
    }
  }

  public UIParameterWindow setSizeWindow(int width, int height) {
    super.setSize(width, height);
    return this;
  }
}
