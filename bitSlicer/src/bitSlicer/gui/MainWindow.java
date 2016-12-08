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
//			setMaximumSize(new Dimension(1000000, 100));
			setMinimumSize(new Dimension(100, 200));
			addTab("File", new FileTab());
			addTab("Edit", new JSpinner(new SpinnerNumberModel(5, 0, 10, 1)));
			
			
		}
		
		private class FileTab extends JPanel {
			public FileTab() {
				this.setLayout(new FlowLayout(FlowLayout.LEFT));
				
				JPanel fichierContainer = new JPanel();
				fichierContainer.setLayout(new GridLayout(3, 0));
				
				fichierContainer.setBorder(BorderFactory.createTitledBorder("Fichier"));
				fichierContainer.add(new Button("Nouveau"));
				fichierContainer.add(new Button("Enregistrer"));
				fichierContainer.add(new Button("Quitter"));
				fichierContainer.add(new Button("Nouveau"));
				fichierContainer.add(new Button("Enregistrer"));
				
				add(fichierContainer);
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
