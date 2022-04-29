package meshIneBits.gui.view3d.provider;

import meshIneBits.gui.view3d.animation.AnimationProcessor.AnimationOption;
import meshIneBits.gui.view3d.animation.AnimationShape;
import processing.core.PApplet;

public interface IAnimationModel3DProvider {
  AnimationShape getAnimationShape(AnimationOption option);
  PApplet getContext();
}
