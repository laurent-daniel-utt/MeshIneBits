package meshIneBits.gui.utilities.patternParamRenderer;

import meshIneBits.config.Setting;
import meshIneBits.config.patternParameter.DoubleParam;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.lang.reflect.Field;

public class LabeledSpinner extends Renderer {

	private static final long serialVersionUID = 6726754934854914029L;

	private JSpinner spinner;

	private JLabel lblName;

	public JSpinner getSpinner() {
		return spinner;
	}

	public JLabel getTitle() {
		return lblName;
	}

	public void setEnabled(boolean enabled) {
		getSpinner().setEnabled(enabled);
		getTitle().setEnabled(enabled);
	}

	public LabeledSpinner(String attributeName, Setting parameters) {
		// Visual options
		this.setLayout(new BorderLayout());
		this.setBackground(Color.WHITE);
		this.setBorder(new EmptyBorder(4, 0, 0, 0));

		// Setting up
		lblName = new JLabel(parameters.title());
		lblName.setToolTipText(parameters.description());
		this.add(lblName, BorderLayout.WEST);
		final Field attribute;
		double defaultValue = 0;
		try {
			attribute = Class.forName("meshIneBits.config.CraftConfig").getDeclaredField(attributeName);
			attribute.setAccessible(true);
			defaultValue = attribute.getDouble(attribute);

			spinner = new JSpinner(new SpinnerNumberModel(defaultValue, parameters.minValue(), parameters.maxValue(),
					parameters.step()));
			spinner.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					try {
						attribute.setDouble(null, (double) spinner.getValue());
					} catch (IllegalArgumentException | IllegalAccessException e1) {
						e1.printStackTrace();
					}
				}
			});
			this.add(spinner, BorderLayout.EAST);
		} catch (NoSuchFieldException | SecurityException | ClassNotFoundException | IllegalArgumentException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This constructor is to render {@link DoubleParam}
	 * 
	 * @param config
	 */
	public LabeledSpinner(DoubleParam config) {
		// Visual options
		this.setLayout(new BorderLayout());
		this.setBackground(Color.WHITE);
		this.setBorder(new EmptyBorder(4, 0, 0, 0));

		// Setting up
		lblName = new JLabel(config.getTitle());
		lblName.setToolTipText("<html><div>" + config.getDescription() + "</div></html>");
		this.add(lblName, BorderLayout.WEST);

		spinner = new JSpinner(new SpinnerNumberModel(config.getCurrentValue(), config.getMinValue(),
				config.getMaxValue(), config.getStep()));
		spinner.addChangeListener(e -> config.setCurrentValue(spinner.getValue()));
		this.add(spinner, BorderLayout.EAST);
	}
}