package meshIneBits.gui.utilities.patternParamRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import meshIneBits.config.patternParameter.BooleanParam;

/**
 * Render {@link BooleanParam}
 * 
 * @author Quoc Nhat Han TRAN
 *
 */
public class Checkbox extends Renderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6286431984037692066L;

	private JLabel lblName;
	private JCheckBox checkbox;
	private BooleanParam config;

	/**
	 * @param config
	 */
	public Checkbox(BooleanParam config) {
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

		// Checkbox
		checkbox = new JCheckBox();
		checkbox.setSelected(config.getCurrentValue());
		checkbox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				config.setCurrentValue(checkbox.isSelected());
			}
		});
		this.add(checkbox, BorderLayout.EAST);
	}
}