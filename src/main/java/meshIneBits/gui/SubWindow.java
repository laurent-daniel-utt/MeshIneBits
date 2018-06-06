/**
 * 
 */
package meshIneBits.gui;

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
	 * Switch visibility
	 */
	void toggle();

	void setCurrentPart(GeneratedPart currentPart);
}
