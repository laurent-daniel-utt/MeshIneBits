package meshIneBits.gui.utilities;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JPanel;

/**
 * The tab on panel
 */
public abstract class RibbonTab extends JPanel {

	private static final long serialVersionUID = 5540398663631111329L;

	public RibbonTab() {
		// Visual options
		FlowLayout layout = new FlowLayout(FlowLayout.LEFT, 10, 0);
		layout.setAlignOnBaseline(true);
		this.setLayout(layout);
		this.setBackground(Color.WHITE);
		this.setFocusable(false);
	}
}