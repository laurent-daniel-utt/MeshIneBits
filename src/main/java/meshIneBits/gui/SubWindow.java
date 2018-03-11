/**
 * 
 */
package meshIneBits.gui;

import java.util.Observer;

import meshIneBits.GeneratedPart;

/**
 * @author Quoc Nhat Han TRAN
 *
 *         Describe the view (2D or 3D) of the loaded model. Windows
 *         implementing this interface should have {@link javax.swing.WindowConstants#HIDE_ON_CLOSE}
 *         behavior.
 */
public interface SubWindow {
	/**
	 * Show up the window
	 */
	public void open();

	/**
	 * Close but not terminate window
	 */
	public void hide();
	
	/**
	 * Switch visibility
	 */
	public void toggle();
	
	/**
	 * Update the current figure
	 */
	public void refresh();
	
	/**
	 * Set the generatedPart
	 */
	public void setPart(GeneratedPart part);
	
	/**
	 * Get the generatedPart
	 */
	public void getPart();
}
