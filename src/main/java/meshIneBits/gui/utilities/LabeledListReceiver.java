package meshIneBits.gui.utilities;

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
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import meshIneBits.config.PatternParameterConfig;

/**
 * Contains a label and a text area, which receive multiple input
 * 
 * @author NHATHAN
 *
 */
public class LabeledListReceiver extends JPanel {

	private JLabel btnName;
	private PatternParameterConfig config;
	private static String delimiter = ";";
	private static String msgInstruction = "<html>" + "<p>Please enter a new array of double values</p>"
			+ "<p><small>Use " + delimiter + " to separate values and . to mark decimal point.</small></p>" + "</html>";

	/**
	 * 
	 */
	private static final long serialVersionUID = 5463905865176363388L;

	/**
	 * Only for entering {@link List} of {@link Double}
	 * 
	 * @param config a specified setting for a certain parameter
	 */
	public LabeledListReceiver(PatternParameterConfig config) {
		if (!(config.defaultValue instanceof List<?>)) {
			return;
		}
		this.config = config;
		// Visual options
		this.setLayout(new BorderLayout());
		this.setBackground(Color.WHITE);
		this.setBorder(new EmptyBorder(4, 0, 0, 0));

		// Setting up
		btnName = new JLabel(config.title);
		this.add(btnName, BorderLayout.WEST);
		btnName.setToolTipText(generateToolTipText(config));
		JButton btnChangeValue = new JButton("Modify");
		this.add(btnChangeValue, BorderLayout.EAST);
		btnChangeValue.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String s = (String) JOptionPane.showInputDialog(null, msgInstruction, convertToString(config.getCurrentValue()));
				if (s != null){
					// Just change the value in case
					// user hits Ok button
					LabeledListReceiver.this.config.setCurrentValue(parseToList(s));
					btnName.setToolTipText(generateToolTipText(config));
				}
			}
		});
	}
	
	private String generateToolTipText(PatternParameterConfig config){
		StringBuilder str = new StringBuilder();
		str.append("<html><div>");
		str.append("<p>" + config.description + "</p>");
		str.append("<p><strong>Current Value</strong><br/>" + convertToString(config.getCurrentValue()) + "</p>");
		str.append("</div></html>");
		return str.toString();
	}

	private String convertToString(Object object) {
		if (!(object instanceof List<?>)) {
			return "";
		}
		List<?> o = (List<?>) object;
		StringBuilder str = new StringBuilder();
		if (!o.isEmpty()){
			for (Iterator<?> iterator = o.iterator(); iterator.hasNext();) {
				Object obj = (Object) iterator.next();
				if (iterator.hasNext()){
					str.append(obj + " ; ");
				} else {
					str.append(obj);
				}
			}
		}
		return str.toString();
	}

	private List<Double> parseToList(String s) {
		if (s == null) return new ArrayList<Double>();
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
