package meshIneBits.gui.view3d.builder;

import meshIneBits.Project;
import meshIneBits.Strip;
import processing.core.PApplet;

import java.util.ArrayList;

public interface IMeshShapeBuilder {
  static IMeshShapeBuilder createInstance(PApplet context, Project project){
    return new BaseMeshBuilder(context, project);
  }
  PavedMeshBuilderResult buildMeshShape();
ArrayList<ArrayList<Strip>> build_strips();
}
