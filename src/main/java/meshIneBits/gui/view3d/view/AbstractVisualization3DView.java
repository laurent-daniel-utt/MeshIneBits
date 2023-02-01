package meshIneBits.gui.view3d.view;

import processing.core.PApplet;
import processing.core.PShape;
import remixlab.proscene.Scene;

import java.util.Vector;

public abstract class AbstractVisualization3DView extends PApplet {

  public abstract Scene getScene();

  public abstract CustomInteractiveFrame getFrame();

  public abstract void setDisplayModelShape(PShape pShape);

  public abstract void setDisplayMeshShape(PShape pShape);

  public abstract void setDisplayShapes(Vector<PShape> displayShapes);

  public abstract void export();

  public abstract void exportAll();

}
