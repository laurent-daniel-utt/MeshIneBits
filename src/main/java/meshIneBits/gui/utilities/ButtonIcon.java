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