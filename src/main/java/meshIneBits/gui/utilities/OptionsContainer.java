package meshIneBits.gui.utilities;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * Container for options of a module.
 */
public class OptionsContainer extends JPanel {

	private static final long serialVersionUID = 136154266552080732L;

	public OptionsContainer(String title) {
		// Visual options
		this.setMinimumSize(new Dimension(500, 500));
		this.setLayout(new GridLayout(3, 0, 10, 0));
		this.setBackground(Color.WHITE);

		TitledBorder centerBorder = BorderFactory.createTitledBorder(title);
		centerBorder.setTitleJustification(TitledBorder.CENTER);
		centerBorder.setTitleFont(new Font(this.getFont().toString(), Font.BOLD, 12));
		centerBorder.setTitleColor(Color.gray);
		centerBorder.setBorder(BorderFactory.createEmptyBorder());
		this.setBorder(centerBorder);

	}

	@Override
	public int getBaseline(int width, int height) {
		return 0;
	}

	@Override
	public Component.BaselineResizeBehavior getBaselineResizeBehavior() {
		return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
	}
}