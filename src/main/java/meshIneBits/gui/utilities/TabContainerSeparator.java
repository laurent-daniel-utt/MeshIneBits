package meshIneBits.gui.utilities;

import java.awt.Dimension;

import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 * A separating line between modules.
 */
public class TabContainerSeparator extends JSeparator {
	private static final long serialVersionUID = 7739612020735334296L;

	public TabContainerSeparator() {
		// Visual options
		this.setOrientation(SwingConstants.VERTICAL);
		Dimension d = this.getPreferredSize();
		d.height = 105;
		this.setPreferredSize(d);
	}
}