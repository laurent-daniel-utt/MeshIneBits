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

package meshIneBits.gui.view2d;

import meshIneBits.Mesh;
import meshIneBits.gui.SubWindow;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * @author Quoc Nhat Han TRAN
 */
public class Window extends JFrame implements SubWindow {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Wrapper viewWrapper;

    public Window() {
        this.setIconImage(new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/icon.png"))).getImage());
        this.setTitle("MeshIneBits - 2D View");
        this.setSize(new Dimension(1080, 720));
        viewWrapper = new Wrapper();
        this.setContentPane(viewWrapper);
    }

    /* (non-Javadoc)
     * @see meshIneBits.gui.SubWindow#toggle()
     */
    @Override
    public void toggle() {
        if (this.isShowing()) {
            this.setVisible(false);
        } else {
            this.setVisible(true);
        }
    }

    @Override
    public void setCurrentMesh(Mesh mesh) {
        viewWrapper.getController().setCurrentMesh(mesh);
    }
}
