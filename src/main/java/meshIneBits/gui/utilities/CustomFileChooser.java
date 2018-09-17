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

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Image;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

public class CustomFileChooser extends JFileChooser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9019431933537011834L;

	@Override
	protected JDialog createDialog(Component parent) throws HeadlessException {
		JDialog dialog = super.createDialog(parent);
		ImageIcon icon = new ImageIcon(
				new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/" + "icon.png"))).getImage()
						.getScaledInstance(24, 24, Image.SCALE_REPLICATE));
		dialog.setIconImage(icon.getImage());
		return dialog;
	}
}
