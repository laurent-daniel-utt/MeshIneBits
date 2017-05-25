package meshIneBits.gui.GUIUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

import meshIneBits.Config.Setting;
import meshIneBits.gui.Ribbon;

public class LabeledSpinner extends JPanel {

	private static final long serialVersionUID = 6726754934854914029L;

	private JSpinner spinner;

	private JLabel name;

	private String getNameFromAnnotation(HashMap<String, Setting> hm, Setting annotation) {
		for (String str : hm.keySet()) {
			if (annotation.equals(hm.get(str))) {
				return str;
			}
		}
		return "";
	}

	public JSpinner getSpinner() {
		return spinner;
	}

	public JLabel getTitle() {
		return name;
	}

	public void setEnabled(boolean enabled) {
		getSpinner().setEnabled(enabled);
		getTitle().setEnabled(enabled);
	}

	public LabeledSpinner(Ribbon ribbon, Setting parameters) {
		// Visual options
		this.setLayout(new BorderLayout());
		this.setBackground(Color.WHITE);
		this.setBorder(new EmptyBorder(4, 0, 0, 0));

		// Setting up
		name = new JLabel(parameters.title());
		name.setToolTipText(parameters.description());
		this.add(name, BorderLayout.WEST);
		String attributeName = getNameFromAnnotation(ribbon.getSetupAnnotations(), parameters);
		Field attribute;
		double defaultValue = 0;
		try {
			attribute = Class.forName("meshIneBits.Config.CraftConfig").getDeclaredField(attributeName);
			attribute.setAccessible(true);
			defaultValue = attribute.getDouble(attribute);
		} catch (NoSuchFieldException | SecurityException | ClassNotFoundException | IllegalArgumentException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
		spinner = new JSpinner(new SpinnerNumberModel(defaultValue, parameters.minValue(), parameters.maxValue(),
				parameters.step()));
		this.add(spinner, BorderLayout.EAST);
	}

	public void addChangeListener(ChangeListener listener) {
		spinner.addChangeListener(listener);
	}

	public Double getValue() {
		return (Double) spinner.getValue();
	}
}