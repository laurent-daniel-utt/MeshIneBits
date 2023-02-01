package meshIneBits.gui.view3d.util;

import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.builder.BitShape;
import meshIneBits.gui.view3d.builder.PavedMeshBuilderResult;
import meshIneBits.gui.view3d.util.animation.AnimationProcessor.AnimationOption;

import java.util.Vector;

public interface IPaintShapePattern {

  void paintAnimation(PavedMeshBuilderResult pavedMesh, AnimationOption animationOption);

  default void paintMesh(PavedMeshBuilderResult pavedMesh) {
    Vector<BitShape> bitShapes = pavedMesh.getBitShapes();
    bitShapes.forEach(
        bitShape -> bitShape.getShape().setFill(Visualization3DConfig.MESH_COLOR.getRGB()));
  }
}
