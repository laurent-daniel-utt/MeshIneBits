package meshIneBits.gui.view3d.view;

import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.ANIMATION;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.ANIMATION_SLICER;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.APPLY;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.BY_BATCH;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.BY_BIT;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.BY_LAYER;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.BY_SUB_BIT;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.CENTER_CAMERA;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.EXPORT;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.FULL;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.GRAVITY;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.ONE_BY_ONE;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.PAUSE;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.POSITION_X;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.POSITION_Y;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.POSITION_Z;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.RESET;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.ROTATION_X;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.ROTATION_Y;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.ROTATION_Z;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.SPEED_DOWN;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.SPEED_UP;
import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.VIEW_MESH;

import java.util.concurrent.atomic.AtomicBoolean;
import meshIneBits.gui.view3d.Processor.IVisualization3DProcessor;
import meshIneBits.util.CustomLogger;

public class UIPWController implements UIPWListener {


  private final CustomLogger logger = new CustomLogger(UIPWController.class);
  private final AtomicBoolean isAnimating = new AtomicBoolean(false);

  private IVisualization3DProcessor processor;


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
        if (isAnimating.get()) {
          processor.activateAnimation();
        } else {
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
