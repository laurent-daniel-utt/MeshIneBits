package meshIneBits.gui.view3d.util.animation;

import meshIneBits.Layer;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.provider.IAnimationModel3DProvider;
import meshIneBits.gui.view3d.provider.IAssemblyWorkingSpaceProvider;
import meshIneBits.gui.view3d.provider.MeshProvider;
import meshIneBits.util.CustomLogger;
import meshIneBits.util.Logger;
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
  private static final AtomicBoolean pausing = new AtomicBoolean(false);
  private final AtomicInteger index = new AtomicInteger(0);

  public static CountDownLatch exported=new CountDownLatch(1);
  private final List<AnimationIndexIncreasedListener> listeners = new ArrayList<>();

  private  int currentlayer=0,currentStrip=0,currentlayer_size=0,size_currentstrip=0;
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
   try { startAnimation();}
   catch (IndexOutOfBoundsException e){
     Logger.error("Refresh the 3d Interface");
     e=new IndexOutOfBoundsException("Refresh the 3d Interface");
     //e.printStackTrace();


   }
  Zpos=0;
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
public static boolean getpausing(){
    return pausing.get();
}
  private void startAnimation()  {

    MultiThreadServiceExecutor.instance.execute(new IndexIncrementTask());
  //throw new IndexOutOfBoundsException("Refresh the 3d interface by clicking on it");
  }




  public void speedUp() {


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
  public class IndexIncrementTask  implements Runnable  {

    @Override
    public void run() {
      /**
       * exportation part of the method
       */
      if(Exportation){exported=new CountDownLatch(1);
        ind.set(0);

      try {
        while (isActivated.get()) {
          final AtomicInteger index = AnimationProcessor.this.index;
          listeners.forEach(listener -> listener.onIndexChangeListener(index.get()));
          Vector<PShape> shapes = currentAnimationShape.setAnimationIndex(index.get()).getDisplayShapes();

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
/**
 * animation part of the method
 * (exportation and animation share the same basics since exportation exports every single shape displayed by the animation
 * functionnalities on the interface)
 */

      else { currentlayer=0;currentStrip=0;currentlayer_size=0;size_currentstrip=0;



        try {
          while (isActivated.get()) {
            final AtomicInteger index = AnimationProcessor.this.index;
            listeners.forEach(listener -> listener.onIndexChangeListener(index.get()));

if(option==AnimationOption.BY_BIT ||option==AnimationOption.BY_SUB_BIT ){
  /**
   * initialisation of the position
   */
            if(index.intValue()==0){pos=-(-printerX / 2 - CraftConfig.workingWidth - 20) +(float) meshstrips.get(0).get(0).getBits().get(0).getMinX();
              Xpos=pos;
            }
  /**
   *when the size of the current stripe at the current moment equals the total size of the current stripe
   */
            if(size_currentstrip>meshstrips.get(currentlayer).get(currentStrip).getBits().size()-1){
              Layer layer=MeshProvider.getInstance().getCurrentMesh().getLayers().get(currentlayer);
              /**
               * if we still in the same layer we move to the next stripe of the same layer
               */
              if(currentlayer_size< (layer.getBits3dKeys().size()-layer.getKeysOfIrregularBits().size())) {
                size_currentstrip=0;
                currentStrip++;
                 System.out.println("currentlayer="+currentlayer+" currentStrip="+currentStrip);
                pos=-(-printerX / 2 - CraftConfig.workingWidth - 20) +(float) meshstrips.get(currentlayer).get(currentStrip).getBits().get(0).getMinX();

              }
              /**
               * when the size of the current layer at the current moment equals the total size of the current layer we move
               * to the next layer and start a new collection of stripes,because stripes are created per layer
               */

              else {size_currentstrip=0;
                currentlayer_size=0;
                currentStrip=0;
                currentlayer++;
                layer=MeshProvider.getInstance().getCurrentMesh().getLayers().get(currentlayer);
                while  ((layer.getBits3dKeys().size()-layer.getKeysOfIrregularBits().size())==0)  {
                  currentlayer++;
                  layer=MeshProvider.getInstance().getCurrentMesh().getLayers().get(currentlayer);
                }
                 pos=-(-printerX / 2 - CraftConfig.workingWidth - 20) +(float) meshstrips.get(currentlayer).get(currentStrip).getBits().get(0).getMinX();
                 Zpos=(float) meshstrips.get(currentlayer).get(currentStrip).getBits().get(0).getLowerAltitude();
              }
            }
  size_currentstrip++;
            currentlayer_size++;

}
            /**
             * Xpos is modified in Class (BaseVisualization3DView)to create an animation effect for the working space(deposing machine)
             * we pause the animation waiting for the working space to reach its destination
             */
            if(Xpos!=pos) movingWorkSpace.await();

            Vector<PShape> shapes = currentAnimationShape.setAnimationIndex(index.get()).getDisplayShapes();
             callback.accept(shapes);
            if (pausing.get()) {
              synchronized (AnimationProcessor.this) {
                AnimationProcessor.this.wait();

              }
            }

            Thread.sleep((long) (animationSpeed * Visualization3DConfig.SECOND));
            AnimationProcessor.this.index.set(index.get() == indexMax ? 0 : index.get() + 1);

          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }






    }
  }


  private void adjusting_workingspace_position_previous(){
    boolean layerchanged=false;
    Layer layer;
    currentlayer_size--;
    size_currentstrip--;
    if(currentlayer_size==0) {
    currentlayer--;
    if(currentlayer<0) return;
    layer=MeshProvider.getInstance().getCurrentMesh().getLayers().get(currentlayer);
      while  (((layer.getBits3dKeys().size()-layer.getKeysOfIrregularBits().size())==0) &&currentlayer>=0 )  {
        currentlayer--;
        layer=MeshProvider.getInstance().getCurrentMesh().getLayers().get(currentlayer);
      }
    if(currentlayer<=0) currentlayer=0;
       layer=MeshProvider.getInstance().getCurrentMesh().getLayers().get(currentlayer);
      currentlayer_size=layer.getBits3dKeys().size()-layer.getKeysOfIrregularBits().size();
      currentStrip=meshstrips.get(currentlayer).size()-1;
      size_currentstrip=meshstrips.get(currentlayer).get(currentStrip).getBits().size();
      layerchanged=true;
    }


    if(size_currentstrip==0 && !layerchanged ){
      currentStrip--;
      if(currentStrip<0)currentStrip=0;
      size_currentstrip=meshstrips.get(currentlayer).get(currentStrip).getBits().size();
    }
System.out.println("currentStrip="+currentStrip);
    System.out.println("size_currentstrip="+size_currentstrip);
    Xpos=-(-printerX / 2 - CraftConfig.workingWidth - 20) +(float) meshstrips.get(currentlayer).get(currentStrip).getBits().get(0).getMinX();
    Zpos=(float) meshstrips.get(currentlayer).get(currentStrip).getBits().get(0).getLowerAltitude();
  }

  private void adjusting_workingspace_position_next(){
    boolean layerchanged=false;
    int number_of_layers=MeshProvider.getInstance().getCurrentMesh().getLayers().size();
    Layer layer=MeshProvider.getInstance().getCurrentMesh().getLayers().get(currentlayer);
    currentlayer_size++;
    size_currentstrip++;

    if(currentlayer_size==layer.getBits3dKeys().size()-layer.getKeysOfIrregularBits().size()) {
      currentlayer++;
      if(currentlayer>=number_of_layers) return;
      layer=MeshProvider.getInstance().getCurrentMesh().getLayers().get(currentlayer);
      while  (((layer.getBits3dKeys().size()-layer.getKeysOfIrregularBits().size())==0) &&currentlayer<number_of_layers )  {
        currentlayer++;
        layer=MeshProvider.getInstance().getCurrentMesh().getLayers().get(currentlayer);
      }
      if(currentlayer>=number_of_layers) currentlayer=number_of_layers-1;
      //layer=MeshProvider.getInstance().getCurrentMesh().getLayers().get(currentlayer);
      currentlayer_size=0;
      currentStrip=0;
      size_currentstrip=0;
      layerchanged=true;
    }


    if(size_currentstrip>=meshstrips.get(currentlayer).get(currentStrip).getBits().size() && !layerchanged ){
      currentStrip++;
      if(currentStrip>=meshstrips.get(currentlayer).size())currentStrip=meshstrips.get(currentlayer).size()-1;
      size_currentstrip=0;
    }
    System.out.println("layerchanged:"+layerchanged);
    System.out.println("currentStrip="+currentStrip);
    System.out.println("size_currentstrip="+size_currentstrip);
    Xpos=-(-printerX / 2 - CraftConfig.workingWidth - 20) +(float) meshstrips.get(currentlayer).get(currentStrip).getBits().get(0).getMinX();
    Zpos=(float) meshstrips.get(currentlayer).get(currentStrip).getBits().get(0).getLowerAltitude();
  }

  public void close(){
    pausing.set(false);
    isActivated.set(false);
  }
public void incrementIndex(){
  AnimationProcessor.this.index.set(index.get() == indexMax ? 0 : index.get() + 1);
  final AtomicInteger index = AnimationProcessor.this.index;
  listeners.forEach(listener -> listener.onIndexChangeListener(index.get()));
  Vector<PShape> shapes = currentAnimationShape.setAnimationIndex(index.get()).getDisplayShapes();
  callback.accept(shapes);
  try {
    if(option==AnimationOption.BY_BIT ||option==AnimationOption.BY_SUB_BIT )adjusting_workingspace_position_next();
    pos=Xpos;
  }catch (IndexOutOfBoundsException e){
    e.printStackTrace();
  }
  }
  public void decrementIndex(){
    AnimationProcessor.this.index.set(index.get() == 0 ? 0 : index.get() - 1);
    final AtomicInteger index = AnimationProcessor.this.index;
    listeners.forEach(listener -> listener.onIndexChangeListener(index.get()));
    Vector<PShape> shapes = currentAnimationShape.setAnimationIndex(index.get()).getDisplayShapes();
    callback.accept(shapes);
    try {
      if(option==AnimationOption.BY_BIT ||option==AnimationOption.BY_SUB_BIT )adjusting_workingspace_position_previous();
      pos=Xpos;
    }catch (IndexOutOfBoundsException e){
      e.printStackTrace();
    }
  }
}
