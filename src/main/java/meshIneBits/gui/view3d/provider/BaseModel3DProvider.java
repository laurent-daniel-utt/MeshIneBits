package meshIneBits.gui.view3d.provider;

import meshIneBits.Mesh;
import meshIneBits.Model;
import meshIneBits.Strip;
import meshIneBits.gui.view3d.builder.*;
import meshIneBits.gui.view3d.util.AlternatingColorPaintPattern;
import meshIneBits.gui.view3d.util.IPaintShapePattern;
import meshIneBits.gui.view3d.util.animation.AnimationProcessor.AnimationOption;
import meshIneBits.gui.view3d.util.animation.AnimationShape;
import meshIneBits.util.Logger;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.stream.Collectors;

public class BaseModel3DProvider implements IModel3DProvider, IAnimationModel3DProvider, IAssemblyWorkingSpaceProvider {

  private PShape modelShape;
  private PavedMeshBuilderResult meshPavedResult;

  private ArrayList<ArrayList<Strip>> meshstrips;
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
     //FIXME this line below is causing the probs
      meshPavedResult = IMeshShapeBuilder.createInstance(context, mesh).buildMeshShape();
    if(MeshProvider.getInstance().getCurrentMesh().isPaved()) meshstrips=IMeshShapeBuilder.createInstance(context, mesh).build_strips();
      wsBuilder = new AssemblyWorkingSpaceBuilder(context);
  }

  @Override
  public PShape getMeshShape() {
    if (paint) {System.out.println("in paint "+getClass().getCanonicalName());
      paintPattern.paintMesh(meshPavedResult);
    }
    return meshPavedResult.getMeshShape();
  }

  public ArrayList<ArrayList<Strip>> getMeshstrips() {
    return meshstrips;
  }

  public Vector<BitShape> getbitShapes() {

    return meshPavedResult.getBitShapes();
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
      try {
        paintPattern.paintAnimation(meshPavedResult, option);
      }catch ( IndexOutOfBoundsException e){
        Logger.error("Refresh the 3d Interface");
        e=new IndexOutOfBoundsException("Refresh the 3d Interface");
       // e.printStackTrace();
      }
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

          for(SubBitShape subBitShape : bitShape.getSubBitShapes()){


            if (subBitShape.getBatchId() >= batchShapes.size()
                || batchShapes.get(subBitShape.getBatchId()) == null) {

              batchShapes.add(subBitShape.getBatchId(), context.createShape(PConstants.GROUP));
            }
            batchShapes.get(subBitShape.getBatchId()).addChild(subBitShape.getShape());
          }

        }
        return new AnimationShape(batchShapes);
      case BY_SUB_BIT:
        Vector<PShape> subBitShapes = meshPavedResult.getBitShapes()
            .stream()
            .map(BitShape::getSubBitShapes)
            .flatMap(Collection::stream)
            .map(SubBitShape::getShape)
            .collect(Collectors.toCollection(Vector::new));
        return new AnimationShape(subBitShapes);
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
