package meshIneBits.gui.view3d.provider;

import meshIneBits.Project;
import meshIneBits.Model;
import meshIneBits.Strip;
import meshIneBits.gui.view3d.builder.BitShape;
import processing.core.PApplet;
import processing.core.PShape;

import java.util.ArrayList;
import java.util.Vector;

public interface IModel3DProvider {

  static IModel3DProvider createDefaultInstance(PApplet context, Model model, Project project){

    return new BaseModel3DProvider(context,model, project);
  }

  PShape getMeshShape();

  PShape getModelShape();

  ArrayList<ArrayList<Strip>> getMeshstrips();
  public Vector<BitShape> getbitShapes();
}
