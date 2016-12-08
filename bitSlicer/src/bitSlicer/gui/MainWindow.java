package bitSlicer.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.*;

public class MainWindow extends JFrame {
	public static void main(String[] args)
	{
		new MainWindow();
	}
	
	public MainWindow() {
			setTitle("MeshIneBits");
			setSize(400, 300);
			setResizable(true);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			Ribbon ribbon = new Ribbon();
			PreviewPanel pp = new PreviewPanel();
			
//			JPanel container = new JPanel();
			setLayout(new BorderLayout());
			add(ribbon, BorderLayout.NORTH);
			add(pp, BorderLayout.SOUTH);	
			
//			add(mainPanel);
			
			
			setVisible(true);
			
			
	//		final JPanel panel = new JPanel();
	//		final Ribbon ribbon = new Ribbon();
	//		
	//		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	//		panel.add(ribbon);
	//		
	//		this.setLayout(new BorderLayout());
	//		this.add(ribbon, BorderLayout.NORTH);
	//		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//		this.pack();
	//		this.setSize(700, 700);
	//		this.setVisible(true);
		}

	private class Ribbon extends JTabbedPane {
		public Ribbon() {
			setMinimumSize(new Dimension(100, 200));
			addTab("Slicer", new SlicerTab());
			addTab("Edit", new JSpinner(new SpinnerNumberModel(5, 0, 10, 1)));
		}
		
		private class SlicerTab extends JPanel {
			public SlicerTab() {
				this.setLayout(new FlowLayout(FlowLayout.LEFT));
				
				JPanel fichierContainer = new JPanel();
				fichierContainer.setLayout(new GridLayout(3, 0));
				fichierContainer.setBorder(BorderFactory.createTitledBorder("Fichier"));
				fichierContainer.add(new Button("New"));
				fichierContainer.add(new Button("Save"));
				fichierContainer.add(new Button("Close"));				
				add(fichierContainer);
				
				JPanel slicerContainer = new JPanel();
				slicerContainer.setLayout(new GridLayout(3, 0));
				slicerContainer.setBorder(BorderFactory.createTitledBorder("Slicer options"));
				
				JPanel spinPan = new JPanel(new BorderLayout());
				spinPan.add(new JLabel("Slice height (mm) :  "), BorderLayout.WEST);
				spinPan.add(new JSpinner(new SpinnerNumberModel(5, 0, 10, 1)), BorderLayout.EAST);
				slicerContainer.add(spinPan);
				
				JPanel spinPan2 = new JPanel(new BorderLayout());
				spinPan2.add(new JLabel("First slice height (%) :  "), BorderLayout.WEST);
				spinPan2.add(new JSpinner(new SpinnerNumberModel(5, 0, 10, 1)), BorderLayout.EAST);
				slicerContainer.add(spinPan2);
				
				add(slicerContainer);
			}
		}
		
		private class TabContainer extends JPanel {
			
		}
	}
	
	private class PreviewPanel extends JPanel {
		public PreviewPanel() {
			
		}
	}
}
