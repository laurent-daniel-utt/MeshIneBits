/*
 * MeshIneBits is a Java software to disintegrate a 3d project (model in .stl)
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

package meshIneBits.gui.view2d;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import meshIneBits.Project;

public class ProjectWindowSelector extends JPanel implements PropertyChangeListener {

  private JSlider layerSlider;
  private JSpinner layerSpinner;

  public ProjectWindowSelector(ProjectController projectController) {
    Project project = projectController.getMesh();
    projectController.addPropertyChangeListener(ProjectController.SETTING_LAYER, this);

    layerSlider = new JSlider(
        SwingConstants.VERTICAL,
        0,
        project.getLayers()
            .size() - 1,
        0);
    //layerSlider.setMinimumSize(new Dimension(20,200));
    //layerSlider.setMaximumSize(new Dimension(20,200));
    layerSlider.setFocusable(false);

        layerSpinner = new JSpinner(
                new SpinnerNumberModel(
                        0,
                        0,
                        project.getLayers().size() - 1,
                        1));
        layerSpinner.setMaximumSize(new Dimension(520,40));
        layerSpinner.setFont( new Font("calibri", Font.PLAIN, 20));

        //Accelerator keys to move to the next/previous layers
        //ALT+UP or ALT+DOWN
        Action actionLayerUp = new AbstractAction("") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                layerSlider.setValue(layerSlider.getValue() + 1);

                updateSlider(projectController);
                updateSpinner(projectController);
            }
        };
        Action actionLayerDown = new AbstractAction("") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                layerSlider.setValue(layerSlider.getValue() - 1);

                updateSlider(projectController);
                updateSpinner(projectController);
            }
        };
        JButton ghostButton1 = new JButton();
        JButton ghostButton2 = new JButton();

        //shortcuts to pass from one sliced layer to another
        //use Alt+Up or Alt+Down
        String keyUp = "UP";
        String keyDown = "DOWN";
        ghostButton1.setAction(actionLayerUp);
        ghostButton2.setAction(actionLayerUp);
        actionLayerUp.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
        actionLayerDown.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK), keyUp);
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK), keyDown);
        this.getActionMap().put(keyUp, actionLayerUp);
        this.getActionMap().put(keyDown, actionLayerDown);


        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(layerSlider);
        add(layerSpinner);
        setBorder(new EmptyBorder(50, 5, 50, 0));

    layerSpinner.addChangeListener(e ->
    {
      updateSlider(projectController);
    });

    layerSlider.addChangeListener(e ->
    {
      updateSpinner(projectController);
    });
    setOpaque(false);
    layerSpinner.setOpaque(false);
    layerSpinner.setOpaque(false);
  }

  private void updateSpinner(ProjectController projectController) {
        projectController.setLayer(layerSlider.getValue());
        layerSpinner.setValue(layerSlider.getValue());
    }

    private void updateSlider(ProjectController projectController) {
        projectController.setLayer((int) layerSpinner.getValue());
        layerSlider.setValue((int) layerSpinner.getValue());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ProjectController.SETTING_LAYER)
                && ((int) evt.getNewValue() != layerSlider.getValue())) {
            layerSlider.setValue((int) evt.getNewValue());
        }
    }
}
