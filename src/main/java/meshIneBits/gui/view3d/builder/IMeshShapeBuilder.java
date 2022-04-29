package meshIneBits.gui.view3d.builder;

import meshIneBits.Mesh;
import processing.core.PApplet;

public interface IMeshShapeBuilder {
  static IMeshShapeBuilder createInstance(PApplet context, Mesh mesh){
    return new BaseMeshBuilder(context,mesh);
  }
  PavedMeshBuilderResult buildMeshShape();
}
