package meshIneBits.gui.utilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import meshIneBits.config.Setting;
import meshIneBits.config.patternParameter.DoubleParam;

public class LabeledSpinner extends JPanel {

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
		Field attribute;
		double defaultValue = 0;
		try {
			attribute = Class.forName("meshIneBits.config.CraftConfig").getDeclaredField(attributeName);
			attribute.setAccessible(true);
			defaultValue = attribute.getDouble(attribute);
		} catch (NoSuchFieldException | SecurityException | ClassNotFoundException | IllegalArgumentException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
		spinner = new JSpinner(
				new SpinnerNumberModel(defaultValue, parameters.minValue(), parameters.maxValue(), parameters.step()));
		this.add(spinner, BorderLayout.EAST);
	}

	/**
	 * This constructor is only for attributes whose type is {@link Double}. For the
	 * {@link List}, use {@link LabeledListReceiver}.
	 * 
	 * @param config
	 *            a parameter of type {@link Double}
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
		spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				config.setCurrentValue(spinner.getValue());
			}
		});
		this.add(spinner, BorderLayout.EAST);

	}

	public void addChangeListener(ChangeListener listener) {
		spinner.addChangeListener(listener);
	}

	public Double getValue() {
		return (Double) spinner.getValue();
	}
}