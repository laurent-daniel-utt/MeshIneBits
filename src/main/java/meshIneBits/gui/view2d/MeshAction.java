/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2021 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020 CLARIS Etienne & RUSSO André.
 * Copyright (C) 2020-2021 DO Quang Bao.
 * Copyright (C) 2021 VANNIYASINGAM Mithulan.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package meshIneBits.gui.view2d;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import meshIneBits.gui.utilities.IconLoader;

public class MeshAction extends AbstractAction {

  public final String uuid;
  public final KeyStroke acceleratorKey;
  public final String combo;
  public final MeshActionInterface action;

  public MeshAction(String uuid,
      String name,
      String iconName,
      String description,
      String acceleratorKey,
      MeshActionInterface action) {
    super(name, IconLoader.get(iconName));
    putValue(SHORT_DESCRIPTION, description);
    this.acceleratorKey = KeyStroke.getKeyStroke(acceleratorKey);
    putValue(ACCELERATOR_KEY, this.acceleratorKey);
    this.uuid = uuid;
    combo = translate(acceleratorKey);
    this.action = action;
  }

  private String translate(String acceleratorKey) {
    return acceleratorKey
        .trim()
        .replace(" ", " + ")
        .replace("control", "Ctrl")
        .replace("shift", "Shift")
        .replace("alt", "Alt");
  }

  public String getToolTipText() {
    return "<html>"
        + "<p><b>" + this.getValue(NAME)
        .toString() + "</b>" +
        (!combo.equals("") ? " (" + combo + ")" : "") + "</p>"
        + "<p><i>" + this.getValue(SHORT_DESCRIPTION) + "</i></p>"
        ;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    action.execute();
  }
}
