/**
 *
 */
package meshIneBits.gui.view2d;

import meshIneBits.GeneratedPart;
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

	Wrapper viewWrapper;

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
	public void setCurrentPart(GeneratedPart currentPart) {
		viewWrapper.getController().setCurrentPart(currentPart);
	}
}
