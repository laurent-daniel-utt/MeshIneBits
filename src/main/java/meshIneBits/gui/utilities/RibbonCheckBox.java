package meshIneBits.gui.utilities;

import javax.swing.*;
import java.awt.*;

/**
 * Options of viewing.
 */
public class RibbonCheckBox extends JCheckBox {

	private static final long serialVersionUID = 9143671052675167109L;

	public RibbonCheckBox(String label) {
		super(label);
		// Visual options
		this.setBackground(Color.WHITE);
		this.setFocusable(false);
	}
}
