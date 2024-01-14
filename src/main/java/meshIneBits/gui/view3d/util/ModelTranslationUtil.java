package meshIneBits.gui.view3d.util;

import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view3d.Processor.OperationModel;
import meshIneBits.gui.view3d.util.ModelInWorkspaceChecker.WorkspaceCheckerResponse;
import meshIneBits.util.Vector3;
import org.jetbrains.annotations.NotNull;

public class ModelTranslationUtil {

  private static final ModelTranslationUtil instance = new ModelTranslationUtil();

  public static ModelTranslationUtil getInstance() {
    return instance;
  }

  public ModelTranslationUtil translateModel(OperationModel modelView, float transX, float transY, float transZ) {
    modelView.translateFrame(transX, transY, transZ);
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
    return this;
  }

  @NotNull
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
    if (res.isExceedMaxY()) {
      translationDirection.addToSelf(new Vector3(0, 0, (CraftConfig.printerZ - maxShape.z)));
    }
    return translationDirection;
  }

  public void applyTranslation(OperationModel modelView) {
    modelView.applyTranslation();
  }
}
