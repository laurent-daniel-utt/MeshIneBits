package meshIneBits.gui.utilities;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Image;

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
				new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + "icon.png")).getImage()
						.getScaledInstance(24, 24, Image.SCALE_REPLICATE));
		dialog.setIconImage(icon.getImage());
		return dialog;
	}
}
