/**
 * 
 */
package meshIneBits.gui.view2d;

import java.awt.Dimension;

import javax.swing.JFrame;

import meshIneBits.GeneratedPart;
import meshIneBits.gui.SubWindow;

/**
 * @author Quoc Nhat Han TRAN
 *
 */
public class Window extends JFrame implements SubWindow {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Wrapper viewWrapper;
	
	public Window() {
		this.setTitle("MeshIneBits - 2D View");
		this.setSize(new Dimension(720, 500));
		viewWrapper = new Wrapper();
		this.setContentPane(viewWrapper);
	}

	/* (non-Javadoc)
	 * @see meshIneBits.gui.SubWindow#open()
	 */
	@Override
	public void open() {
		this.setVisible(true);
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

	/* (non-Javadoc)
	 * @see meshIneBits.gui.SubWindow#refresh()
	 */
	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see meshIneBits.gui.SubWindow#setPart(meshIneBits.GeneratedPart)
	 */
	@Override
	public void setPart(GeneratedPart part) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see meshIneBits.gui.SubWindow#getPart()
	 */
	@Override
	public void getPart() {
		// TODO Auto-generated method stub

	}

}
