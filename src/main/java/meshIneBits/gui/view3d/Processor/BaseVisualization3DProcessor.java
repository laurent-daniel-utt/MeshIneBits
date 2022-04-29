package meshIneBits.gui.view3d.Processor;

import java.util.Vector;
import java.util.function.Consumer;
import meshIneBits.Mesh;
import meshIneBits.gui.view3d.Processor.DisplayState.State;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.animation.AnimationProcessor;
import meshIneBits.gui.view3d.animation.AnimationProcessor.AnimationMode;
import meshIneBits.gui.view3d.animation.AnimationProcessor.AnimationOption;
import meshIneBits.gui.view3d.provider.IAnimationModel3DProvider;
import meshIneBits.gui.view3d.provider.IAssemblyWorkingSpaceProvider;
import meshIneBits.gui.view3d.provider.IModel3DProvider;
import meshIneBits.gui.view3d.util.ModelRotationUtil;
import meshIneBits.gui.view3d.util.ModelScaleUtil;
import meshIneBits.gui.view3d.util.ModelTranslationUtil;
import meshIneBits.gui.view3d.view.AbstractVisualization3DView;
import processing.core.PShape;
import remixlab.dandelion.geom.Vec;

public class BaseVisualization3DProcessor implements IVisualization3DProcessor {

  private final AbstractVisualization3DView view3D;
  private final IModel3DProvider modelProvider;
  private final AnimationProcessor animation;
  private final OperationModel modelView;
  private final DisplayState state;
  //  private MultiThreadServiceExecutor executor = MultiThreadServiceExecutor.instance;

  public BaseVisualization3DProcessor(Mesh mesh, AbstractVisualization3DView view3D) {
    this.view3D = view3D;
    modelView = new OperationModel(mesh.getModel(), view3D.getFrame());
    modelProvider = IModel3DProvider.createDefaultInstance(view3D, mesh.getModel(), mesh);

    if (modelProvider instanceof IAssemblyWorkingSpaceProvider
        && modelProvider instanceof IAnimationModel3DProvider) {
      animation = new AnimationProcessor(
          (IAnimationModel3DProvider) modelProvider,
          (IAssemblyWorkingSpaceProvider) modelProvider);
    } else {
      throw new RuntimeException("AnimationProcessor can't not initialized");
    }

    state = new DisplayState();
    initState(mesh);
  }

  private void initState(Mesh mesh) {
    state.setState(mesh.isPaved() ? State.PAVED_VIEW : State.MODEL_VIEW);
  }

  @Override
  public void rotationX(float x) {
    rotateModel(x, 0, 0);
  }


  @Override
  public void rotationY(float y) {
    rotateModel(0, y, 0);
  }

  @Override
  public void rotationZ(float z) {
    rotateModel(0, 0, z);
  }

  @Override
  public void translateX(float x) {
    translateModel(x, 0, 0);
  }


  @Override
  public void translateY(float y) {
    translateModel(0, y, 0);

  }

  @Override
  public void translateZ(float z) {
    translateModel(0, 0, z);
  }

  @Override
  public void scaleModel(float s) {
    ModelScaleUtil.getInstance().scaleModel(modelView, s);
  }

  @Override
  public void apply() {
    ModelRotationUtil.getInstance().applyRotate(modelView);
    ModelTranslationUtil.getInstance().applyTranslation(modelView);
    ModelScaleUtil.getInstance().applyScale(modelView);
    modelView.applyScale();
  }

  @Override
  public void applyGravity() {
    float minZ = (float) modelView.getFrame().getMinShapeInFrameCoordinate().z;
    ModelTranslationUtil.getInstance().translateModel(modelView, 0, 0, -minZ);
  }

  @Override
  public void centerCamera() {
    view3D.getScene().eye().setPosition(new Vec(
        modelView.getFrame().position().x(),
        Visualization3DConfig.EYE_POSITION_Y,
        Visualization3DConfig.EYE_POSITION_Z));
    view3D.getScene().eye()
        .lookAt(modelView.getFrame().position());
  }

  @Override
  public void reset() {

    ModelRotationUtil.getInstance()
        .inverseRotationModel(modelView)
        .applyRotate(modelView);

    ModelTranslationUtil.getInstance()
        .translateModel(modelView, 0, 0, 0)
        .applyTranslation(modelView);

    applyGravity();
    //TODO notidy listener to update position and size changes
  }

  @Override
  public void displayModel(boolean boo) {
    if (boo) {
      state.setState(State.MODEL_VIEW);
      view3D.setDisplayModelShape(modelProvider.getModelShape());
    }
  }

  @Override
  public void displayMesh(boolean boo) {
    if (boo) {
      state.setState(State.PAVED_VIEW);
      view3D.setDisplayMeshShape(modelProvider.getMeshShape());
    }
  }

  @Override
  public void setAnimationBySubBit(boolean boo) {
    if (boo) {
      animation.setAnimationOption(AnimationOption.BY_SUB_BIT);
    }
  }

  @Override
  public void setAnimationByBit(boolean boo) {
    if (boo) {
      animation.setAnimationOption(AnimationOption.BY_BIT);
    }
  }

  @Override
  public void setAnimationByBatch(boolean boo) {
    if (boo) {
      animation.setAnimationOption(AnimationOption.BY_BATCH);
    }

  }

  @Override
  public void setAnimationByLayer(boolean boo) {
    if (boo) {
      animation.setAnimationOption(AnimationOption.BY_LAYER);
    }
  }

  @Override
  public void setDisplayOneByOne(boolean boo) {
    if (boo) {
      animation.setAnimationMode(AnimationMode.ONE_BY_ONE);
    }
  }

  @Override
  public void setDisplayFull(boolean b) {
    if (b) {
      animation.setAnimationMode(AnimationMode.FULL);
    }
  }

  @Override
  public void export() {
    view3D.export();
  }

  @Override
  public void activateAnimation() {
    state.setState(State.ANIMATION_VIEW);
    //TODO update index of slider
    Consumer<Vector<PShape>> c = view3D::setDisplayShapes;
    animation.activate(c);
  }

  @Override
  public void pauseAnimation() {
    animation.pause();
  }

  @Override
  public void deactivateAnimation() {
    animation.deactivate();
    displayMesh(true);
  }

  private void rotateModel(float x, float y, float z) {
    ModelRotationUtil.getInstance().rotateModel(modelView, x, y, z);
  }

  private void translateModel(float x, float y, float z) {
    ModelTranslationUtil.getInstance().translateModel(modelView, x, y, z);
    //TODO call listener to update new changes of size and position
  }

  @Override
  public void onTerminated() {
    animation.close();
  }

  @Override
  public IModel3DProvider getModelProvider() {
    return modelProvider;
  }

  public DisplayState getDisplayState() {
    return state;
  }

  @Override
  public void speedUp() {
    animation.speedUp();
  }

  @Override
  public void speedDown() {
    animation.speedDown();
  }

  public AnimationProcessor getAnimationProcessor() {
    return animation;
  }

  @Override
  public void setAnimationIndex(int i) {
    animation.setAnimationIndex(i);
  }
}
