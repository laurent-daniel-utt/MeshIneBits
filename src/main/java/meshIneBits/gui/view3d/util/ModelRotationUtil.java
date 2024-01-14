package meshIneBits.gui.view3d.util;

import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view3d.Processor.OperationModel;
import meshIneBits.gui.view3d.util.ModelInWorkspaceChecker.WorkspaceCheckerResponse;
import meshIneBits.util.Vector3;
import org.jetbrains.annotations.NotNull;
import remixlab.dandelion.geom.Quat;

public class ModelRotationUtil {

  private static final ModelRotationUtil instance = new ModelRotationUtil();

  public static ModelRotationUtil getInstance() {
    return instance;
  }

  @NotNull
  private Quat calculateRotation(float angleX, float angleY, float angleZ) {
    Quat r = new Quat();
    float angXRad = (float) Math.toRadians(angleX);
    float angYRad = (float) Math.toRadians(angleY);
    float angZRad = (float) Math.toRadians(angleZ);
    r.fromEulerAngles(angXRad, angYRad, angZRad);
    return r;
  }

  public void rotateModel(OperationModel modelView, float x, float y, float z) {
    Quat r = calculateRotation(x, y, z);
    modelView.rotateFrame(r);
  }

  public void applyRotate(OperationModel modelView) {
    ModelInWorkspaceChecker checker = new ModelInWorkspaceChecker();
    WorkspaceCheckerResponse res = checker.checkInWorkspace(
        modelView.getFrame().getMinShapeInFrameCoordinate(),
        modelView.getFrame().getMaxShapeInFrameCoordinate());
    if (!res.isInWorkspace()) {
      Vector3 translationDirection = calculateExceedVector(modelView, res);
      modelView.translateFrame(
          (float) translationDirection.x,
          (float) translationDirection.y,
          (float) translationDirection.z);
    }
    modelView.applyRotate();
  }

  public ModelRotationUtil inverseRotationModel(OperationModel modelView) {
    modelView.rotateInverse();
    return this;
  }

  private Vector3 calculateExceedVector(OperationModel modelView, WorkspaceCheckerResponse res) {
    Vector3 translationDirection = new Vector3();
    Vector3 minShape = modelView.getFrame().getMinShapeInFrameCoordinate();
    Vector3 maxShape = modelView.getFrame().getMaxShapeInFrameCoordinate();
    if (res.isExceedMinX()) {
      translationDirection.addToSelf(new Vector3((-CraftConfig.printerX / 2 - minShape.x), 0, 0));
    }
    if (res.isExceedMaxX()) {
      translationDirection.addToSelf(new Vector3((CraftConfig.printerX / 2 - maxShape.x), 0, 0));
    }
    if (res.isExceedMinY()) {
      translationDirection.addToSelf(new Vector3(0, (-CraftConfig.printerY / 2 - minShape.y), 0));
    }
    if (res.isExceedMaxY()) {
      translationDirection.addToSelf(new Vector3(0, (CraftConfig.printerY / 2 - maxShape.y), 0));
    }
    if (res.isExceedMinZ()) {
      translationDirection.addToSelf(new Vector3(0, 0, -minShape.z));
    }
    if (res.isExceedMaxZ()) {
      translationDirection.addToSelf(new Vector3(0, 0, (CraftConfig.printerZ - maxShape.z)));
    }
    return translationDirection;
  }
}
