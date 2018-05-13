/**
 * 
 */
package meshIneBits.gui.utilities.patternParamRenderer;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import meshIneBits.config.patternParameter.OptionParam;

/**
 * Renders {@link OptionParam}
 * 
 * @author Quoc Nhat Han TRAN
 *
 */
public class OptionSelector extends Renderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4913058735577067521L;

	private JLabel lblName;
	private JComboBox<String> options;
	private OptionParam opConfig;

	/**
	 * @param opConfig
	 */
	public OptionSelector(OptionParam opConfig) {
		super();
		this.opConfig = opConfig;
		this.initGUI();
	}

	private void initGUI() {
		// TODO Auto-generated method stub
		// Visual options
		this.setLayout(new BorderLayout());
		this.setBackground(Color.WHITE);
		this.setBorder(new EmptyBorder(4, 0, 0, 0));
		
		
	}
}
