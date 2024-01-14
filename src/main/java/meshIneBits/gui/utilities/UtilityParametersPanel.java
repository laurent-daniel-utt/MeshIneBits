/*
 * MeshIneBits is a Java software to disintegrate a 3d project (model in .stl)
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

package meshIneBits.gui.utilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class UtilityParametersPanel extends JPanel {

  public UtilityParametersPanel(String title) {
    super();
    TitledBorder titledBorder = BorderFactory.createTitledBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK),
        title);
    titledBorder.setTitleJustification(TitledBorder.LEFT);
    titledBorder.setTitleFont(new Font(this.getFont()
        .toString(), Font.BOLD, 12));
    titledBorder.setTitleColor(Color.GRAY);
    this.setBorder(titledBorder);
//        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"deletePanel");
//        getActionMap().put("deletePanel", new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                UtilityParametersPanel.this.setVisible(false);
//            }
//        });
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(getWidth(), 100);
  }

  @Override
  public Dimension getMinimumSize() {
    return new Dimension(getWidth(), 100);
  }


}
