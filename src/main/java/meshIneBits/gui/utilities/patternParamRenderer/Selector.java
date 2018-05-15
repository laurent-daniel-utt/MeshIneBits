/**
 * 
 */
package meshIneBits.gui.utilities.patternParamRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
public class Selector extends Renderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4913058735577067521L;

	private JLabel lblName;
	private JComboBox<String> options;
	private OptionParam config;

	/**
	 * @param config
	 */
	public Selector(OptionParam config) {
		super();
		this.config = config;
		this.initGUI();
	}

	private void initGUI() {
		// Visual options
		this.setLayout(new BorderLayout());
		this.setBackground(Color.WHITE);
		this.setBorder(new EmptyBorder(4, 0, 0, 0));
		
		// Label
		lblName = new JLabel(config.getTitle());
		lblName.setToolTipText("<html><div>" + config.getDescription() + "</div></html>");
		this.add(lblName, BorderLayout.WEST);
		
		// Choices
		options = new JComboBox<String>((String[]) config.getChoices().toArray());
		options.setSelectedItem(config.getDefaultValue());
		options.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// Update config
				config.setCurrentValue(options.getSelectedItem());
			}
		});
		this.add(options, BorderLayout.EAST);
	}
}
