package meshIneBits.gui.view3d.animation;

import meshIneBits.gui.view3d.animation.AnimationProcessor.AnimationOption;
import processing.core.PApplet;

public interface IAnimationModel3DProvider {
  AnimationShape getAnimationShape(AnimationOption option);
  PApplet getContext();
}
