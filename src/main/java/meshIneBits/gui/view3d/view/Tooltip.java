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

package meshIneBits.gui.view3d.view;

import controlP5.ControllerInterface;

public class Tooltip<T, U> {

  final private ControllerInterface<T> tip;
  final private ControllerInterface<U> component;

  public Tooltip(ControllerInterface<T> tip, ControllerInterface<U> component) {
    this.tip = tip;
    this.component = component;
  }

  @SuppressWarnings("unused")
  public void setComponentPosition(float[] position) {
    component.setPosition(position);
  }

  public void setTooltipPosition(float[] position) {
    tip.setPosition(position);
  }

  public float[] positionOfComponent() {
    return component.getPosition();
  }

  public float[] sizeOfComponent() {
    return new float[]{component.getWidth(), component.getHeight()};
  }

  public ControllerInterface<T> getTooltipText() {
    return tip;
  }

  public ControllerInterface<U> getComponent() {
    return component;
  }

  public void showTooltip(boolean b) {
    if (b) {
      tip.show();
    } else {
      tip.hide();
    }
  }

  public boolean mouseEntered(double mouseX, double mouseY) {
    float[] position = this.positionOfComponent();
    float[] size = this.sizeOfComponent();
    return (mouseX > position[0])
        && (mouseX < position[0] + size[0])
        && (mouseY > position[1])
        && (mouseY < position[1] + size[1]);
  }
}
