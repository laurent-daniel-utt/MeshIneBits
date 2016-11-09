package bitSlicer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.Logger;
import bitSlicer.util.LoggingInterface;

public class LogWindow extends JFrame implements LoggingInterface
{
	private static final long serialVersionUID = 1L;
	
	private JLabel statusLabel;
	private JProgressBar progressBar;
	
	public LogWindow()
	{
		this.setTitle("BitSlicer - " + CraftConfig.VERSION);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setLayout(new BorderLayout());
		
		statusLabel = new JLabel("BitSlicerBitSlicerBitSlicerBitSlicerBitSlicer");
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

	public void error(String error)
	{
	}

	public void message(String message)
	{
	}

	public void updateStatus(final String status)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				statusLabel.setText(status);
				progressBar.setIndeterminate(true);
				progressBar.setStringPainted(false);
				LogWindow.this.repaint();
			}
		});
	}

	public void warning(String warning)
	{
	}
	
	public void dispose()
	{
		Logger.unRegister(this);
		super.dispose();
	}

	public void setProgress(final int value, final int max)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				progressBar.setIndeterminate(false);
				progressBar.setStringPainted(true);
				progressBar.setValue(value);
				progressBar.setMaximum(max);
				LogWindow.this.repaint();
			}
		});
		
	}
}