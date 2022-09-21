package meshIneBits.gui.view3d.util.animation;

import meshIneBits.Layer;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.provider.IAnimationModel3DProvider;
import meshIneBits.gui.view3d.provider.IAssemblyWorkingSpaceProvider;
import meshIneBits.gui.view3d.provider.MeshProvider;
import meshIneBits.util.CustomLogger;
import meshIneBits.util.MultiThreadServiceExecutor;
import processing.core.PShape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static meshIneBits.config.CraftConfig.printerX;
import static meshIneBits.gui.view3d.view.BaseVisualization3DView.*;
import static meshIneBits.gui.view3d.view.BaseVisualization3DView.meshstrips;
import static meshIneBits.gui.view3d.view.UIPWController.Exportation;


public class AnimationProcessor {

  public static final CustomLogger logger = new CustomLogger(AnimationProcessor.class);

  public enum AnimationOption {BY_BIT, BY_BATCH, BY_LAYER, BY_SUB_BIT}

  public enum AnimationMode {FULL, ONE_BY_ONE}

  private final IAnimationModel3DProvider animationProvider;
  private IAssemblyWorkingSpaceProvider wsProvider;
  private AnimationShape currentAnimationShape;
  private AnimationOption option = Visualization3DConfig.defaultAnimationOption;
  private AnimationMode mode = Visualization3DConfig.defaultAnimationMode;
  private Consumer<Vector<PShape>> callback;

  public static CountDownLatch movingWorkSpace=new CountDownLatch(1);
  public static double animationSpeed = Visualization3DConfig.speed_coefficient_default;
  private int indexMax;
public static AtomicInteger ind= new AtomicInteger(0);
  private final AtomicBoolean isActivated = new AtomicBoolean(false);
  private final AtomicBoolean pausing = new AtomicBoolean(false);
  private final AtomicInteger index = new AtomicInteger(0);

  public static CountDownLatch exported=new CountDownLatch(1);
  private final List<AnimationIndexIncreasedListener> listeners = new ArrayList<>();

   public static float pos;
  public AnimationProcessor(IAnimationModel3DProvider animationProvider,
      IAssemblyWorkingSpaceProvider wsProvider) {
    this.animationProvider = animationProvider;
    this.wsProvider = wsProvider;
  }

  public AnimationProcessor(IAnimationModel3DProvider animationProvider) {
    this.animationProvider = animationProvider;
  }

  public void setAnimationOption(AnimationOption option) {
    this.option = option;
  }

  public void setAnimationMode(AnimationMode mode) {
    this.mode = mode;
  }

  public void activate(Consumer<Vector<PShape>> listener) {
    isActivated.set(true);
    pausing.set(false);
    animationSpeed = Visualization3DConfig.speed_coefficient_default;
    callback = listener;
    initAnimationShape();
    startAnimation();
  }

  private void initIndex() {
    index.set(0);
    indexMax = currentAnimationShape.size() - 1;
    listeners.forEach(listener -> listener.updateIndexRange(0, indexMax));
  }

  private void initAnimationShape() {
    currentAnimationShape = animationProvider.getAnimationShape(this.option);
    currentAnimationShape.setModeDisplay(mode);
    initIndex();
  }

  public void deactivate() {
    isActivated.set(false);
    pausing.set(false);
  }

  public synchronized void pause() {
    pausing.set(!pausing.get());
    notifyAll();
  }

  private void startAnimation() {

    MultiThreadServiceExecutor.instance.execute(new IndexIncrementTask());
  }

  public void speedUp(boolean sim) {


            animationSpeed -=
        (Visualization3DConfig.speed_coefficient_min - Visualization3DConfig.speed_coefficient_max)
            / Visualization3DConfig.speed_level_number;
    if (animationSpeed < Visualization3DConfig.speed_coefficient_max) {
      animationSpeed = Visualization3DConfig.speed_coefficient_max;
    }
  }

  public void speedDown() {
    animationSpeed +=
        (Visualization3DConfig.speed_coefficient_min - Visualization3DConfig.speed_coefficient_max)
            / Visualization3DConfig.speed_level_number;
    if (animationSpeed > Visualization3DConfig.speed_coefficient_min) {
      animationSpeed = Visualization3DConfig.speed_coefficient_min;
    }
  }

  public void addOnIndexIncreasedListener(AnimationIndexIncreasedListener... listeners) {
    this.listeners.addAll(Arrays.asList(listeners));
  }

  public void setAnimationIndex(int i) {
    if (i > indexMax || i < 0) {
      logger.logERRORMessage(
          " Animation Index value is not allowed, has to be between " + indexMax + " and 0");
    } else {
      index.set(i);
      callback.accept(currentAnimationShape.setAnimationIndex(i).getDisplayShapes());
    }
  }

  @SuppressWarnings("all")
  public class IndexIncrementTask implements Runnable {

    @Override
    public void run() {
      if(Exportation){exported=new CountDownLatch(1);
        ind.set(0);

      try {
        while (isActivated.get()) {
          final AtomicInteger index = AnimationProcessor.this.index;
          listeners.forEach(listener -> listener.onIndexChangeListener(index.get()));
          Vector<PShape> shapes = currentAnimationShape.setAnimationIndex(index.get()).getDisplayShapes();
        //  System.out.println("index="+index);
          //System.out.println("Thread in AniProc="+Thread.currentThread().getName());
          //ind=index;

          //System.out.println("stuck with ind="+ind.get());

          // System.out.println("");
//
//          if (option == AnimationOption.BY_BIT && wsProvider != null) {
//            PShape finalShape = AnimationProcessor.this.animationProvider.getContext()
//                .createShape(PConstants.GROUP);
//            PShape currentShape = shapes.get(index.get());
//            double positionX = PShapeUtil.getInstance().getMinShapeInFrameCoordinate(currentShape).x;
//            finalShape.addChild(currentShape);
//            PShape ws = wsProvider.getAssemblyWorkingSpace();
//            ws.translate((float)positionX,0);
//
//            finalShape.addChild(ws);
//            shapes.remove(index);
//            shapes.add(index.get(),finalShape);
//          }
          callback.accept(shapes);
          waitshaping.countDown();

          if (pausing.get()) {
            synchronized (AnimationProcessor.this) {
              AnimationProcessor.this.wait();
            }
          }
          Thread.sleep((long) (animationSpeed * Visualization3DConfig.SECOND));
          ind=index;
          notyet.countDown();

          notyet=new CountDownLatch(1);

          exported.await();

          AnimationProcessor.this.index.set(index.get() == indexMax ? 0 : index.get() + 1);

        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      }
    else {int l=0,s=0,size_layer=0,size_strip=0;



        try {
          while (isActivated.get()) {
            final AtomicInteger index = AnimationProcessor.this.index;
            listeners.forEach(listener -> listener.onIndexChangeListener(index.get()));

if(option==AnimationOption.BY_BIT ||option==AnimationOption.BY_SUB_BIT ){
            if(index.intValue()==0){pos=-(-printerX / 2 - CraftConfig.workingWidth - 20) +(float) meshstrips.get(0).get(0).getBits().get(0).getMinX();
              Xpos=pos;
            }// System.out.println("l="+l+" s="+s);
            //System.out.println("sizeLayer="+size_layer+" sizeStrip="+size_strip);
            if(size_strip>meshstrips.get(l).get(s).getBits().size()-1){
              Layer layer=MeshProvider.getInstance().getCurrentMesh().getLayers().get(l);
//System.out.println("layer_Capa:"+(layer.getBits3dKeys().size()-layer.getKeysOfIrregularBits().size()));
              if(size_layer< (layer.getBits3dKeys().size()-layer.getKeysOfIrregularBits().size())) {
                size_strip=0;
                s++;
                 pos=-(-printerX / 2 - CraftConfig.workingWidth - 20) +(float) meshstrips.get(l).get(s).getBits().get(0).getMinX();



                // Xpos=-(-printerX / 2 - CraftConfig.workingWidth - 20) +(float) meshstrips.get(l).get(s).getBits().get(0).getMinX();

              }
              else {size_strip=0;
                size_layer=0;
                s=0;
                l++;
                 pos=-(-printerX / 2 - CraftConfig.workingWidth - 20) +(float) meshstrips.get(l).get(s).getBits().get(0).getMinX();

                //Xpos=-(-printerX / 2 - CraftConfig.workingWidth - 20) +(float) meshstrips.get(l).get(s).getBits().get(0).getMinX();
                Zpos=(float) meshstrips.get(l).get(s).getBits().get(0).getLowerAltitude();
              }
            }
            size_strip++;
            size_layer++;

}
            if(Xpos!=pos) movingWorkSpace.await();
            Vector<PShape> shapes = currentAnimationShape.setAnimationIndex(index.get()).getDisplayShapes();










             callback.accept(shapes);

             //Xpos=-(-printerX / 2 - CraftConfig.workingWidth - 20) +(float) meshstrips.get(0).get(0).getBits().get(0).getMinX();

            if (pausing.get()) {
              synchronized (AnimationProcessor.this) {
                AnimationProcessor.this.wait();
              }
            }
            Thread.sleep((long) (animationSpeed * Visualization3DConfig.SECOND));
            AnimationProcessor.this.index.set(index.get() == indexMax ? 0 : index.get() + 1);
            //ind=index;



          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }



      }






    }
  }

  public void close(){
    pausing.set(false);
    isActivated.set(false);
  }
}
