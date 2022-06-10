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

package meshIneBits.gui.view2d;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import meshIneBits.Mesh;

public class MeshWindowSelector extends JPanel implements PropertyChangeListener {

  private JSlider layerSlider;
  private JSpinner layerSpinner;

  public MeshWindowSelector(MeshController meshController) {
    Mesh mesh = meshController.getMesh();
    meshController.addPropertyChangeListener(MeshController.SETTING_LAYER, this);

    layerSlider = new JSlider(
        SwingConstants.VERTICAL,
        0,
        mesh.getLayers()
            .size() - 1,
        0);
    //layerSlider.setMinimumSize(new Dimension(20,200));
    //layerSlider.setMaximumSize(new Dimension(20,200));
    layerSlider.setFocusable(false);

        layerSpinner = new JSpinner(
                new SpinnerNumberModel(
                        0,
                        0,
                        mesh.getLayers().size() - 1,
                        1));
        layerSpinner.setMaximumSize(new Dimension(520,40));
        layerSpinner.setFont( new Font("calibri", Font.PLAIN, 20));

        //Accelerator keys to move to the next/previous layers
        //ALT+UP or ALT+DOWN
        Action actionLayerUp = new AbstractAction("") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                layerSlider.setValue(layerSlider.getValue() + 1);

                updateSlider(meshController);
                updateSpinner(meshController);
            }
        };
        Action actionLayerDown = new AbstractAction("") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                layerSlider.setValue(layerSlider.getValue() - 1);

                updateSlider(meshController);
                updateSpinner(meshController);
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
      updateSlider(meshController);
    });

    layerSlider.addChangeListener(e ->
    {
      updateSpinner(meshController);
    });
    setOpaque(false);
    layerSpinner.setOpaque(false);
    layerSpinner.setOpaque(false);
  }

  private void updateSpinner(MeshController meshController) {
        meshController.setLayer(layerSlider.getValue());
        layerSpinner.setValue(layerSlider.getValue());
    }

    private void updateSlider(MeshController meshController) {
        meshController.setLayer((int) layerSpinner.getValue());
        layerSlider.setValue((int) layerSpinner.getValue());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(MeshController.SETTING_LAYER)
                && ((int) evt.getNewValue() != layerSlider.getValue())) {
            layerSlider.setValue((int) evt.getNewValue());
        }
    }
}
