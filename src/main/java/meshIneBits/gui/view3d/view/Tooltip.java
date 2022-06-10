package meshIneBits.gui.view3d.view;

import controlP5.ControllerInterface;

public class Tooltip<T, U> {

  final private ControllerInterface<T> tip;
  final private ControllerInterface<U> component;

  public Tooltip(ControllerInterface<T> tip, ControllerInterface<U> component) {
    this.tip = tip;
    this.component = component;
  }

  @SuppressWarnings("unused")
  public void setComponentPosition(float[] position) {
    component.setPosition(position);
  }

  public void setTooltipPosition(float[] position) {
    tip.setPosition(position);
  }

  public float[] positionOfComponent() {
    return component.getPosition();
  }

  public float[] sizeOfComponent() {
    return new float[]{component.getWidth(), component.getHeight()};
  }

  public ControllerInterface<T> getTooltipText() {
    return tip;
  }

  public ControllerInterface<U> getComponent() {
    return component;
  }

  public void showTooltip(boolean b) {
    if (b) {
      tip.show();
    } else {
      tip.hide();
    }
  }

  public boolean mouseEntered(double mouseX, double mouseY) {
    float[] position = this.positionOfComponent();
    float[] size = this.sizeOfComponent();
    return (mouseX > position[0])
        && (mouseX < position[0] + size[0])
        && (mouseY > position[1])
        && (mouseY < position[1] + size[1]);
  }
}
