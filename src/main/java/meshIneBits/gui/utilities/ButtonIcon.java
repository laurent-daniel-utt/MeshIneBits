/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public class ButtonIcon extends JButton {
	private static final long serialVersionUID = 4439705350058229259L;

	public ButtonIcon(String label, String iconName) {
		this(label, iconName, false);
	}

	public ButtonIcon(String label, String iconName, boolean onlyIcon) {
		this(label, iconName, onlyIcon, 22, 22);
	}

	public ButtonIcon(String label, String iconName, boolean onlyIcon, int width, int height) {
		super((label.isEmpty() ? "" : " ") + label);
		this.setHorizontalAlignment(LEFT);
		this.setMargin(new Insets(0, 0, 0, 2));

		try {
			ImageIcon icon = new ImageIcon(
					new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/" + iconName))).getImage()
							.getScaledInstance(width, height, Image.SCALE_DEFAULT));
			this.setIcon(icon);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (onlyIcon) {
			setContentAreaFilled(false);
			setBorder(new EmptyBorder(3, 3, 3, 3));

			// Actions listener
			addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mouseEntered(java.awt.event.MouseEvent evt) {
					setContentAreaFilled(true);
				}

				@Override
				public void mouseExited(java.awt.event.MouseEvent evt) {
					setContentAreaFilled(false);
				}
			});
		}
	}
}