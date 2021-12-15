package meshIneBits.gui.view3d.Processor;

import meshIneBits.Model;
import meshIneBits.gui.view3d.view.CustomInteractiveFrame;
import meshIneBits.util.Vector3;
import remixlab.dandelion.geom.Rotation;

public class OperationModel {

  private final Model model;
  private final CustomInteractiveFrame frame;

  public OperationModel(Model model, CustomInteractiveFrame frame) {
    this.model = model;
    this.frame = frame;
  }

  public void rotateFrame(Rotation r) {
    frame.rotate(r);
  }

  public void rotateInverse() {
    frame.rotate(frame.rotation().inverse());
  }

  public void scale(float s){
    frame.scale(s);
  }

  public void applyRotate() {
    model.rotate(frame.rotation());
  }

  public void translateFrame(float x, float y, float z) {
    frame.translate(x, y, z);
  }

  public Model getModel() {
    return model;
  }

  public CustomInteractiveFrame getFrame() {
    return frame;
  }

  public void applyTranslation() {
    model.setPos(new Vector3(frame.position().x(), frame.position().y(), frame.position().z()));
  }

  public void applyScale() {
    model.applyScale(frame.scaling());
  }
}
