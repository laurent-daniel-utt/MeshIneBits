/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2016-2026 DANIEL Laurent.
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
