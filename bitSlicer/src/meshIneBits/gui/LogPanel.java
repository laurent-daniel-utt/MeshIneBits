package meshIneBits.gui;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import meshIneBits.util.Logger;
import meshIneBits.util.LoggingInterface;

public class LogPanel extends JPanel implements LoggingInterface{
	private static final long serialVersionUID = 1L;
	private JLabel statusLabel;
	private JProgressBar progressBar;
	
	public LogPanel(){
		
		Logger.register(this);
		
		progressBar = new JProgressBar(0, 2);
		this.add(progressBar);
		progressBar.setVisible(false);
		
		
		statusLabel = new JLabel("Ready");
		statusLabel.setMinimumSize(new Dimension(200, statusLabel.getHeight()));
		this.add(statusLabel);

		
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
		if(value >= max){
			progressBar.setVisible(false);
		}
		else{
			progressBar.setVisible(true);
			progressBar.setValue(value);
			progressBar.setMaximum(max);
		}
		LogPanel.this.repaint();
	}

	@Override
	public void updateStatus(String status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void warning(String warning) {
		// TODO Auto-generated method stub
		
	}
}
