/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2022 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
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
 *
 */

package meshIneBits.gui.utilities;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.scheduler.AScheduler;

public class UPPScheduleMesh extends UtilityParametersPanel {

  public UPPScheduleMesh(MeshController meshController) {
    super("Bit cut and place scheduling");
    setLayout(new FlowLayout(FlowLayout.LEADING));
    setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

    JComboBox<AScheduler> schedulerChoice = new JComboBox<>(CraftConfig.schedulerPreloaded);
    JButton processingViewSchedulerBtn = new JButton("Run");
    add(schedulerChoice);
    add(processingViewSchedulerBtn);

    processingViewSchedulerBtn.addActionListener(e -> {
      try {
        meshController.scheduleMesh();
      } catch (Exception e1) {
        meshController.handleException(e1);
      }
    });

    schedulerChoice.addActionListener(e -> {
      JComboBox sC = (JComboBox) e.getSource();
      try {
        meshController.setScheduler((AScheduler) sC.getSelectedItem());
      } catch (Exception e1) {
        meshController.handleException(e1);
      }
    });
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(getWidth(), 70);
  }
}
