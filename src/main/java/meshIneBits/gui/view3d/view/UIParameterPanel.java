package meshIneBits.gui.view3d.view;

import controlP5.ControlEvent;
import controlP5.ControlP5;
import meshIneBits.util.CustomLogger;
import processing.core.PApplet;
import processing.core.PFont;

/**
 * ControlP5 panel embedded in a parent {@link PApplet} (no separate OS window).
 */
public abstract class UIParameterPanel {

  protected final PApplet parent;
  protected final ControlP5 control;
  protected float originX;
  protected float originY;
  protected float panelWidth;
  protected float panelHeight;
  protected final CustomLogger logger;

  private UIPWListener listener;

  protected UIParameterPanel(PApplet parent, ControlP5 control, UIPWListener listener,
      float originX, float originY, float panelWidth, float panelHeight) {
    this.parent = parent;
    this.control = control;
    this.listener = listener;
    this.originX = originX;
    this.originY = originY;
    this.panelWidth = panelWidth;
    this.panelHeight = panelHeight;
    this.logger = new CustomLogger(getClass());
  }

  protected abstract void generateButton();

  protected abstract void updateButton();

  /** Reposition controllers after the panel bounds change (e.g. window resize). */
  protected abstract void relayout();

  public abstract void controlEvent(ControlEvent theEvent);

  public abstract void onOpen();

  public abstract void onClose();

  public void init() {
    generateButton();
    onOpen();
  }

  public void layout(float originX, float originY, float panelWidth, float panelHeight) {
    this.originX = originX;
    this.originY = originY;
    this.panelWidth = panelWidth;
    this.panelHeight = panelHeight;
    relayout();
    control.setPosition(Math.round(originX), Math.round(originY));
  }

  public void close() {
    onClose();
    listener = null;
  }

  public UIPWListener getListener() {
    return listener;
  }

  public void setListener(UIPWListener listener) {
    this.listener = listener;
  }

  public ControlP5 getControl() {
    return control;
  }

  public float getOriginX() {
    return originX;
  }

  public float getOriginY() {
    return originY;
  }

  public float getPanelWidth() {
    return panelWidth;
  }

  public float getPanelHeight() {
    return panelHeight;
  }

  /** True when screen coordinates fall inside this panel bounds. */
  public boolean containsPoint(float x, float y) {
    return x >= originX && x < originX + panelWidth
        && y >= originY && y < originY + panelHeight;
  }

  protected int panelSizeW(float ratioOfWidth) {
    return Math.max(1, Math.round(panelWidth * ratioOfWidth));
  }

  protected int panelSizeH(float ratioOfHeight) {
    return Math.max(1, Math.round(panelHeight * ratioOfHeight));
  }

  /** Panel-local X for ControlP5 (screen offset is set on the ControlP5 instance). */
  protected float px(float ratioOfWidth) {
    return ratioOfWidth * panelWidth;
  }

  /** Panel-local Y for ControlP5 (screen offset is set on the ControlP5 instance). */
  protected float py(float ratioOfHeight) {
    return ratioOfHeight * panelHeight;
  }

  /** Keep a widget's X within this panel's local coordinate space. */
  protected float clampLocalX(float x, float elementWidth) {
    return Math.max(0, Math.min(x, panelWidth - elementWidth));
  }

  protected PFont createFont(String name, float size) {
    return parent.createFont(name, size);
  }

  protected PFont createFont(String name, float size, boolean smooth) {
    return parent.createFont(name, size, smooth);
  }

  protected int color(float v1, float v2, float v3) {
    return parent.color(v1, v2, v3);
  }

  protected int color(float gray) {
    return parent.color(gray);
  }

  protected int color(float gray, float alpha) {
    return parent.color(gray, alpha);
  }

  protected int mouseX() {
    return parent.mouseX;
  }

  protected int mouseY() {
    return parent.mouseY;
  }

  protected int pmouseX() {
    return parent.pmouseX;
  }

  protected int pmouseY() {
    return parent.pmouseY;
  }

  protected void println(String msg) {
    PApplet.println(msg);
  }
}
