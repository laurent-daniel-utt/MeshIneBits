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
 * Copyright (C) 2020 CLAIRIS Etienne & RUSSO André.
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

import meshIneBits.config.WorkspaceConfig;
import meshIneBits.gui.utilities.ButtonIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class MeshWindowZoomer extends JPanel {
    private static final double MIN_ZOOM_VALUE = 0.5;
    private final int MIN_ZOOM_SLIDER_VALUE = 1;
    private final int MAX_ZOOM_SLIDER_VALUE = 500;
    // Following attributes are the coefficients from the formula Y = a * EXP(b * x)
    // used for the zoom's log scale
    private final double bCoefficient;
    private final double aCoefficient;
    /**
     * Replica of {@link MeshController#getZoom()} to simplify callee
     */
    private double zoom = 1;
    // Components
    private JSlider zoomSlider = new JSlider(
            SwingConstants.HORIZONTAL,
           MIN_ZOOM_SLIDER_VALUE,
            MAX_ZOOM_SLIDER_VALUE,
            MIN_ZOOM_SLIDER_VALUE);

    JButton zoomButton =new JButton("Init Zoom");
    private MeshController meshController;
    private MeshWindowCore meshWindowCore;

    MeshWindowZoomer(MeshController meshController, MeshWindowCore core) {
        this.meshController = meshController;
        this.meshWindowCore = core;
        bCoefficient = Math.log(MIN_ZOOM_VALUE / 10) / (MIN_ZOOM_SLIDER_VALUE - MAX_ZOOM_SLIDER_VALUE);
        aCoefficient = MIN_ZOOM_VALUE / Math.exp(bCoefficient * MIN_ZOOM_SLIDER_VALUE);
        //zoomSlider.setMaximumSize(new Dimension(500, 20));
        setOpaque(false);

        setLayout(new FlowLayout(FlowLayout.CENTER));
        ButtonIcon zoomMinusBtn = new ButtonIcon("", "search-minus.png", true);
        add(zoomMinusBtn);

        add(zoomButton);
        zoomButton.setFont(new Font("calibri", Font.PLAIN, 20));
        zoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                meshController.setZoom(1);
                core.setViewOffset();
            }
        });
        ButtonIcon zoomPlusBtn = new ButtonIcon("", "search-plus.png", true);
        add(zoomPlusBtn);
        setAlignmentX(Component.CENTER_ALIGNMENT);
        zoomSlider.addChangeListener(e ->
                setZoom(getConvertedZoomValue(zoomSlider.getValue())));
        zoomMinusBtn.addActionListener(e ->
        {
            setZoom(zoom / WorkspaceConfig.zoomSpeed);
            zoomSlider.setValue(getSliderZoomValueFrom(zoom));
        });
        zoomPlusBtn.addActionListener(e ->
        {
            setZoom(zoom * WorkspaceConfig.zoomSpeed);
            zoomSlider.setValue(getSliderZoomValueFrom(zoom));
        });
        zoomSlider.setOpaque(false);

        // Not show if mesh not sliced yet
        if (meshController.getMesh() == null || !(meshController.getMesh().isSliced()))
            setVisible(false);
    }

    private double getConvertedZoomValue(int zoomSliderValue) {
        return aCoefficient * Math.exp(bCoefficient * zoomSliderValue);
    }

    private int getSliderZoomValueFrom(double real) {
        return (int) Math.round(Math.log(real / aCoefficient) / bCoefficient);
    }

    private void setZoom(double zoomValue) {
        this.zoom = zoomValue <= MIN_ZOOM_VALUE ? MIN_ZOOM_VALUE : zoomValue;
        meshController.setZoom(this.zoom);
    }
}