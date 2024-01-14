package meshIneBits.gui.view3d.view;

import meshIneBits.gui.view3d.Processor.IVisualization3DProcessor;
import meshIneBits.gui.view3d.provider.ProjectProvider;
import meshIneBits.util.CustomLogger;
import processing.core.PShape;

import java.util.concurrent.atomic.AtomicBoolean;

import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.*;
import static meshIneBits.gui.view3d.util.animation.AnimationProcessor.getpausing;
import static meshIneBits.gui.view3d.view.BaseVisualization3DView.IndexExport;
import static meshIneBits.gui.view3d.view.UIPWAnimation.Animation;

public class UIPWController implements UIPWListener {


  private final CustomLogger logger = new CustomLogger(UIPWController.class);
  private final AtomicBoolean isAnimating = new AtomicBoolean(false);
private PShape pp;

  private IVisualization3DProcessor processor;
public static Boolean Exportation=false;
private int i=0;
  public UIPWController(IVisualization3DProcessor view) {
    this.processor = view;
  }

  @Override
  public void onActionListener(Object callbackObj, String event, Object value) {
    switch (event) {
      case ROTATION_X:
        processor.rotationX((float) value);
        break;
      case ROTATION_Y:
        processor.rotationY((float) value);
        break;
      case ROTATION_Z:
        processor.rotationZ((float) value);
        break;
      case POSITION_X:
        processor.translateX((float) value);
        break;
      case POSITION_Y:
        processor.translateY((float) value);
        break;
      case POSITION_Z:
        processor.translateZ((float) value);
        break;
      case VIEW_MESH:
        if ((boolean) value) {
          processor.displayMesh(true);
        }else{
          processor.displayModel(true);
        }
        break;
      case APPLY:
        processor.apply();
        break;
      case GRAVITY:
        processor.applyGravity();
        break;
      case CENTER_CAMERA:
        processor.centerCamera();
        break;
      case RESET:
        processor.reset();
        break;
      case BY_SUB_BIT:
        processor.setAnimationBySubBit((boolean) value);
        break;
      case BY_BIT:
        processor.setAnimationByBit((boolean) value);
        break;
      case BY_BATCH:
        processor.setAnimationByBatch((boolean) value);
        break;
      case BY_LAYER:
        processor.setAnimationByLayer((boolean) value);
        break;
      case ONE_BY_ONE:
        processor.setDisplayOneByOne((boolean) value);
        break;
      case FULL:
        processor.setDisplayFull((boolean) value);
        break;
      case EXPORT:
        processor.export();
        break;
      case ANIMATION:

        isAnimating.set(!isAnimating.get());
        if (isAnimating.get()) {Animation.getCaptionLabel().setText(STOP);

          processor.activateAnimation();
        } else {Animation.getCaptionLabel().setText(ANIMATION);
          processor.deactivateAnimation();
        }
        break;
      case SPEED_UP:
        processor.speedUp();
        break;
      case SPEED_DOWN:
        processor.speedDown();
        break;
      case PAUSE:

        processor.pauseAnimation();
        break;
      case ANIMATION_SLICER:
        processor.setAnimationIndex(Math.round((float) value));
        break;
      case EXPORTAll:
        if (ProjectProvider.getInstance().getCurrentMesh().isPaved()){
          processor.setDisplayOneByOne(true);
          Exportation=true;
          processor.activateAnimation();
          IndexExport=0;
          processor.exportAll();
          Exportation=false;}
        break;
      case NEXT:
      if(isAnimating.get() && getpausing()){
        processor.incrementIndex();
      }
        break;
      case PREVIOUS:
        if(isAnimating.get() && getpausing()){
          processor.decrementIndex();
        }
        break;
        default:
        logger.logERRORMessage("The event invoked is not handled by UserControllerListener object: "
            + this.getClass());
        break;
    }
  }

  public void close() {
    processor = null;
  }


}
