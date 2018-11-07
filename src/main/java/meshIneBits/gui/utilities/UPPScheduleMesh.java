/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 Vallon BENJAMIN.
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

import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.scheduler.AScheduler;

import javax.swing.*;
import java.awt.*;

public class UPPScheduleMesh extends UtilityParametersPanel {
    public UPPScheduleMesh(MeshController meshController) {
        super("Schedule Mesh");
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
