package meshIneBits.gui.view3d.provider;

import meshIneBits.Mesh;
import meshIneBits.Model;
import processing.core.PApplet;
import processing.core.PShape;

public interface IModel3DProvider {

  static IModel3DProvider createDefaultInstance(PApplet context, Model model, Mesh mesh){
    return new BaseModel3DProvider(context,model,mesh);
  }

  PShape getMeshShape();

  PShape getModelShape();
}
