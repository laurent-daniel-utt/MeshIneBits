package meshIneBits.gui.utilities.patternParamRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;

import meshIneBits.config.patternParameter.DoubleListParam;

/**
 * Contains a label and a text area, which receive multiple input.<br>
 * Renders {@link DoubleListParam}
 * 
 * @author NHATHAN
 *
 */
public class LabeledListReceiver extends Renderer {

	private JLabel btnName;
	private DoubleListParam dlconfig;
	private static String delimiter = ";";
	private static String msgInstruction = "<html>" + "<p>Please enter a new array of double values</p>"
			+ "<p><small>Use " + delimiter + " to separate values and . to mark decimal point.</small></p>" + "</html>";

	/**
	 * 
	 */
	private static final long serialVersionUID = 5463905865176363388L;

	/**
	 * Render {@link DoubleListParam}
	 * 
	 * @param config
	 */
	public LabeledListReceiver(DoubleListParam config) {
		this.dlconfig = config;
		// Visual options
		this.setLayout(new BorderLayout());
		this.setBackground(Color.WHITE);
		this.setBorder(new EmptyBorder(4, 0, 0, 0));

		// Setting up
		btnName = new JLabel(config.getTitle());
		this.add(btnName, BorderLayout.WEST);
		btnName.setToolTipText(generateToolTipText());
		JButton btnChangeValue = new JButton("Modify");
		this.add(btnChangeValue, BorderLayout.EAST);
		btnChangeValue.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String s = (String) JOptionPane.showInputDialog(null, msgInstruction, convertToString());
				if (s != null) {
					// Just change the value in case
					// user hits Ok button
					LabeledListReceiver.this.dlconfig.setCurrentValue(parseToList(s));
					btnName.setToolTipText(generateToolTipText());
				}
			}
		});
	}

	private String generateToolTipText() {
		StringBuilder str = new StringBuilder();
		str.append("<html><div>");
		str.append("<p>" + dlconfig.getDescription() + "</p>");
		str.append("<p><strong>Current Value</strong><br/>" + convertToString() + "</p>");
		str.append("</div></html>");
		return str.toString();
	}

	private String convertToString() {
		List<Double> l = dlconfig.getCurrentValue();
		StringBuilder str = new StringBuilder();
		if (!l.isEmpty()) {
			for (Iterator<Double> iterator = l.iterator(); iterator.hasNext();) {
				Double obj = iterator.next();
				if (iterator.hasNext()) {
					str.append(obj + " ; ");
				} else {
					str.append(obj);
				}
			}
		}
		return str.toString();
	}

	private List<Double> parseToList(String s) {
		if (s == null)
			return new ArrayList<Double>();
		String[] values = s.split(delimiter, 0);
		ArrayList<Double> list = new ArrayList<Double>();
		for (int i = 0; i < values.length; i++) {
			try {
				list.add(Double.valueOf(values[i]));
			} catch (Exception e) {
				continue;
			}
		}
		return list;
	}
}
