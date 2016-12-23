package meshIneBits.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import meshIneBits.Slicer.Config.CraftConfig;
import meshIneBits.util.Logger;
import meshIneBits.util.LoggingInterface;

public class OldLogWindow extends JFrame implements LoggingInterface {
	private static final long serialVersionUID = 1L;

	private JLabel statusLabel;
	private JProgressBar progressBar;

	public OldLogWindow() {
		this.setTitle("MeshIneBits - " + CraftConfig.VERSION);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.setLayout(new BorderLayout());

		statusLabel = new JLabel("MeshIneBitsMeshIneBitsMeshIneBitsMeshIneBitsMeshIneBits");
		statusLabel.setMinimumSize(new Dimension(200, statusLabel.getHeight()));
		this.add(statusLabel, BorderLayout.NORTH);

		progressBar = new JProgressBar(0, 2);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(false);
		this.add(progressBar, BorderLayout.CENTER);

		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		Logger.register(this);
	}

	@Override
	public void dispose() {
		Logger.unRegister(this);
		super.dispose();
	}

	@Override
	public void error(String error) {
	}

	@Override
	public void message(String message) {
	}

	@Override
	public void setProgress(final int value, final int max) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				progressBar.setIndeterminate(false);
				progressBar.setStringPainted(true);
				progressBar.setValue(value);
				progressBar.setMaximum(max);
				OldLogWindow.this.repaint();
			}
		});

	}

	@Override
	public void updateStatus(final String status) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				statusLabel.setText(status);
				progressBar.setIndeterminate(true);
				progressBar.setStringPainted(false);
				OldLogWindow.this.repaint();
			}
		});
	}

	@Override
	public void warning(String warning) {
	}
}