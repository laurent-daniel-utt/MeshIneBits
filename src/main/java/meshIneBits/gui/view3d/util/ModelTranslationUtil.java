/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2015-2026 DANIEL Laurent.
 -  Copyright (C) 2015 Cédric Siourakhan
 -  Copyright (C) 2016 Gabriel Magny
 -  Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 -  Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 -  Copyright (C) 2018 VALLON Benjamin.
 -  Copyright (C) 2018 LORIMER Campbell.
 -  Copyright (C) 2018 D'AUTUME Christian.
 -  Copyright (C) 2019 DURINGER Nathan (Tests).
 -  Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
 -  Copyright (C) 2020-2021 DO Quang Bao.
 -  Copyright (C) 2021 VANNIYASINGAM Mithulan.
 -  Copyright (C) 2021  Quang Long
 -  Copyright (C) 2022 Mhamad Atlab
 -  Copyright (C) 2022 Majed Hlaihel
 -  Copyright (C) 2026 Aymeric Sirejol
 -  Copyright (C) 2026 Hugo BENOIT
 -
 -  This program is free software: you can redistribute it and/or modify
 -  it under the terms of the GNU General Public License as published by
 -  the Free Software Foundation, either version 3 of the License, or
 -  (at your option) any later version.
 -
 -  This program is distributed in the hope that it will be useful,
 -  but WITHOUT ANY WARRANTY; without even the implied warranty of
 -  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 -  GNU General Public License for more details.
 -
 -  You should have received a copy of the GNU General Public License
 -  along with this program. If not, see <https://www.gnu.org/licenses/>.
 -----------------------------------------------------------------------------*/

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
    ModelInWorkspaceChecker.WorkspaceCheckerResponse res = checker.checkInWorkspace(
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
