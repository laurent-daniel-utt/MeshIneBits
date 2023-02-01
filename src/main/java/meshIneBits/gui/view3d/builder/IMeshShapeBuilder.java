package meshIneBits.gui.view3d.builder;

import meshIneBits.Mesh;
import meshIneBits.Strip;
import processing.core.PApplet;

import java.util.ArrayList;

public interface IMeshShapeBuilder {
  static IMeshShapeBuilder createInstance(PApplet context, Mesh mesh){
    return new BaseMeshBuilder(context,mesh);
  }
  PavedMeshBuilderResult buildMeshShape();
ArrayList<ArrayList<Strip>> build_strips();
}
