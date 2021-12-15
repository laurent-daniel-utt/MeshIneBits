package meshIneBits.gui.view3d.Processor;

import meshIneBits.gui.view3d.provider.IModel3DProvider;

public interface IVisualization3DProcessor {

  void rotationX(float x);

  void rotationY(float y);

  void rotationZ(float z);

  void translateX(float x);

  void translateY(float y);

  void translateZ(float z);

  void scaleModel(float s);

  void apply();

  void applyGravity();

  void centerCamera();

  void reset();

  void displayModel(boolean boo);

  void displayMesh(boolean boo);

  void setAnimationByBit(boolean boo);

  void setAnimationByBatch(boolean boo);

  void setAnimationByLayer(boolean boo);

  void setDisplayOneByOne(boolean boo);

  void setDisplayFull(boolean b);

  void export();

  void activateAnimation();

  void pauseAnimation();

  void deactivateAnimation();

  void onTerminated();

  IModel3DProvider getModelProvider();

  DisplayState getDisplayState();

  void speedUp();

  void speedDown();

  void setAnimationIndex(int i);

}

