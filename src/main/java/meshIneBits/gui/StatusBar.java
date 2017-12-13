package meshIneBits.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

import meshIneBits.util.Logger;
import meshIneBits.util.LoggingInterface;

public class StatusBar extends JPanel implements LoggingInterface {
	private static final long serialVersionUID = 1L;
	private JLabel statusLabel;
	private JProgressBar progressBar;

	public StatusBar() {
		// Visual options
		setBackground(Color.white);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.lightGray), new EmptyBorder(3, 3, 3, 3)));
		setLayout(new BorderLayout());

		// Setting up
		Logger.register(this);

		progressBar = new JProgressBar(0, 2);
		progressBar.setVisible(false);

		statusLabel = new JLabel("Ready");
		statusLabel.setMinimumSize(new Dimension(200, statusLabel.getHeight()));

		add(statusLabel, BorderLayout.WEST);
		add(progressBar, BorderLayout.EAST);
	}

	@Override
	public void error(String error) {
		statusLabel.setText("ERROR :" + error);
	}

	@Override
	public void message(String message) {
		statusLabel.setText(message);
	}

	@Override
	public void setProgress(int value, int max) {
		if (value >= max) {
			progressBar.setVisible(false);
		} else {
			progressBar.setVisible(true);
			progressBar.setValue(value);
			progressBar.setMaximum(max);
		}
		StatusBar.this.repaint();
	}

	@Override
	public void updateStatus(String status) {
		statusLabel.setText(status);
	}

	@Override
	public void warning(String warning) {
	}
}
