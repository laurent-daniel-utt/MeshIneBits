package meshIneBits.gui.view3d.animation;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import meshIneBits.gui.view3d.animation.AnimationProcessor.AnimationMode;
import processing.core.PShape;

public class AnimationShape {

  private final Vector<PShape> shapeList;
  private AnimationMode mode;
  private final AtomicInteger animationIndex = new AtomicInteger();

  public AnimationShape(Vector<PShape> shapeList) {
    this.shapeList = shapeList;
  }

  public AnimationShape setAnimationIndex(int index) {
    animationIndex.set(index);
    return this;
  }

  public int size() {
    return shapeList.size();
  }

  public void setModeDisplay(AnimationMode mode) {
    this.mode = mode;
  }

  public final Vector<PShape> getDisplayShapes() {
    setupAnimationShapeDisplay();
    return new Vector<>(shapeList);
  }

  private void setupAnimationShapeDisplay() {
    boolean full = mode == AnimationMode.FULL;
    int index = animationIndex.get();
    for (int i = 0; i < shapeList.size(); i++) {
      PShape shape = shapeList.get(i);
      if (i < index) {
        shape.setVisible(full);
      } else
        shape.setVisible(i == index);
    }
  }

  @SuppressWarnings("unused")
  public int getAnimationIndex(){
    return animationIndex.get();
  }
}
