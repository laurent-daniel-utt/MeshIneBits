package meshIneBits.gui.view3d.builder;

import meshIneBits.Model;
import processing.core.PApplet;
import processing.core.PShape;

public interface IModelShapeBuilder {

  static IModelShapeBuilder createInstance(PApplet pApplet, Model model) {
    //TODO for now, we have just STL shape builder. but in the future with new implementation of other file 3D, we must handle several type of model here.
    return STLModelShapeBuilder.createInstance(pApplet,model);
  }

  PShape buildModelShape();
}
