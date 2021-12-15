package meshIneBits.gui.view3d.provider;

import java.util.Vector;
import java.util.stream.Collectors;
import meshIneBits.Mesh;
import meshIneBits.Model;
import meshIneBits.gui.view3d.builder.AssemblyWorkingSpaceBuilder;
import meshIneBits.gui.view3d.util.AlternatingColorPaintPattern;
import meshIneBits.gui.view3d.animation.AnimationShape;
import meshIneBits.gui.view3d.builder.BitShape;
import meshIneBits.gui.view3d.animation.IAnimationModel3DProvider;
import meshIneBits.gui.view3d.builder.IMeshShapeBuilder;
import meshIneBits.gui.view3d.builder.IModelShapeBuilder;
import meshIneBits.gui.view3d.util.IPaintShapePattern;
import meshIneBits.gui.view3d.builder.PavedMeshBuilderResult;
import meshIneBits.gui.view3d.animation.AnimationProcessor.AnimationOption;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

public class BaseModel3DProvider implements IModel3DProvider, IAnimationModel3DProvider, IAssemblyWorkingSpaceProvider {

  private PShape modelShape;
  private PavedMeshBuilderResult meshPavedResult;
  private AssemblyWorkingSpaceBuilder wsBuilder;
  private final PApplet context;
  private final IPaintShapePattern paintPattern = new AlternatingColorPaintPattern();
  private boolean paint = true;

  public BaseModel3DProvider(PApplet pApplet, Model model, Mesh mesh) {
    //TODO testing with STLModel
    this.context = pApplet;
    setup(model, mesh);
  }

  private void setup(Model model, Mesh mesh) {
    modelShape = IModelShapeBuilder.createInstance(context, model).buildModelShape();
    meshPavedResult = IMeshShapeBuilder.createInstance(context, mesh).buildMeshShape();
    wsBuilder = new AssemblyWorkingSpaceBuilder(context);
  }

  @Override
  public PShape getMeshShape() {
    if (paint) {
      paintPattern.paintMesh(meshPavedResult);
    }
    return meshPavedResult.getMeshShape();
  }

  @SuppressWarnings("unused")
  public void setPaintColor(boolean b) {
    paint = true;
  }

  @Override
  public PShape getModelShape() {
    return modelShape;
  }

  @Override
  public AnimationShape getAnimationShape(AnimationOption option) {
    if (meshPavedResult.isNull()) {
      return new AnimationShape(new Vector<>());
    }
    if (paint) {
      paintPattern.paintAnimation(meshPavedResult, option);
    }
    switch (option) {
      case BY_BIT:
        Vector<PShape> bitShapes = meshPavedResult.getBitShapes()
            .stream()
            .map(BitShape::getShape)
            .collect(Collectors.toCollection(Vector::new));
        return new AnimationShape(bitShapes);
      case BY_BATCH:
        Vector<PShape> batchShapes = new Vector<>();
        for (BitShape bitShape : meshPavedResult.getBitShapes()) {
          if (bitShape.getBatchId() >= batchShapes.size()
              || batchShapes.get(bitShape.getBatchId()) == null) {
            batchShapes.add(bitShape.getBatchId(), context.createShape(PConstants.GROUP));
          }
          batchShapes.get(bitShape.getBatchId()).addChild(bitShape.getShape());
        }
        return new AnimationShape(batchShapes);
      case BY_SUB_BIT:
        return null;
      case BY_LAYER:
      default:
        Vector<PShape> layerShapes = new Vector<>();
        for (BitShape bitShape : meshPavedResult.getBitShapes()) {
          if (bitShape.getLayerId() >= layerShapes.size()
              || layerShapes.get(bitShape.getLayerId()) == null) {
            layerShapes.add(bitShape.getLayerId(), context.createShape(PConstants.GROUP));
          }
          layerShapes.get(bitShape.getLayerId()).addChild(bitShape.getShape());
        }
        return new AnimationShape(layerShapes);
    }
  }

  @Override
  public PApplet getContext() {
    return context;
  }

  @Override
  public PShape getAssemblyWorkingSpace() {
    return wsBuilder.build();
  }
}
