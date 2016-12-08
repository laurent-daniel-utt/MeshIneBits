package bitSlicer.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class MainWindow extends JFrame {
	public static void main(String[] args)
	{
		new MainWindow();
	}

	public MainWindow() {

		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
            UIManager.put("Separator.foreground", new Color(10, 10, 10, 50));
        } catch (Exception e) {
            e.printStackTrace();
        }

		setTitle("MeshIneBits");
		setSize(1000, 300);
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Ribbon ribbon = new Ribbon();
		PreviewPanel pp = new PreviewPanel();

		setLayout(new BorderLayout());
		add(ribbon, BorderLayout.NORTH);
		add(pp, BorderLayout.SOUTH);	

		setVisible(true);
	}

	private class Ribbon extends JTabbedPane {
		public Ribbon() {
			setFont( new Font(this.getFont().toString(), Font.PLAIN, 15) );;
			addTab("Slicer", new SlicerTab());
			addTab("Edit", new JSpinner(new SpinnerNumberModel(5, 0, 10, 1)));
		}
		
		private class RibbonTab extends JPanel {
			public RibbonTab() {
				this.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
				this.setBackground(Color.WHITE);
				this.setFocusable(false);
			}
		}

		private class SlicerTab extends RibbonTab {
			public SlicerTab() {
				super();

				OptionsContainer fileCont = new OptionsContainer("File");
				fileCont.add(new JButton("New"));
				fileCont.add(new JButton("Save"));
				fileCont.add(new JButton("Close"));

				OptionsContainer slicerCont = new OptionsContainer("Slicer options");
				slicerCont.add(new LabeledSpinner("Slice height (mm) :  ", 2, 0, 10, 1));
				slicerCont.add(new LabeledSpinner("First slice height (%) :  ", 5, 0, 10, 1));
				
				OptionsContainer bitsCont = new OptionsContainer("Bits options");
				bitsCont.add(new LabeledSpinner("Bit thickness (mm) :  ", 2, 0, 10, 1));
				bitsCont.add(new LabeledSpinner("Bit width (mm) :  ", 2, 0, 10, 1));
				bitsCont.add(new LabeledSpinner("Bit height (mm) :  ", 2, 0, 10, 1));
				
				GalleryContainer patternGallery = new GalleryContainer("Pattern");
				patternGallery.addButton(new JToggleButton(), "p1.png");
				patternGallery.addButton(new JToggleButton(), "p2.png");

				add(fileCont);
				add(new TabContainerSeparator());
				add(slicerCont);
				add(new TabContainerSeparator());
				add(bitsCont);
				add(new TabContainerSeparator());
				add(patternGallery);
			}
		}

		private class OptionsContainer extends JPanel {
			public OptionsContainer(String title) {
				this.setLayout(new GridLayout(3, 0, 3, 3));
				this.setBackground(Color.WHITE);
				TitledBorder centerBorder = BorderFactory.createTitledBorder(title);
				centerBorder.setTitleJustification(TitledBorder.CENTER);
				centerBorder.setTitleFont(new Font(this.getFont().toString(), Font.BOLD, 12));
				centerBorder.setTitleColor(Color.gray);
				centerBorder.setBorder(BorderFactory.createEmptyBorder());
				this.setBorder(centerBorder);
				this.setMinimumSize(new Dimension(500, 500));
			}
		}

		private class TabContainerSeparator extends JSeparator {
			public TabContainerSeparator() {
				this.setOrientation(SwingConstants.VERTICAL);
				Dimension d = this.getPreferredSize();
				d.height = 90;
				this.setPreferredSize(d);
			}
		}
		
		private class LabeledSpinner extends JPanel {
			public LabeledSpinner(String label, double defaultValue, double minValue, double maxValue, double step) {
				this.setLayout(new BorderLayout());
				this.setBackground(Color.WHITE);
				this.add(new JLabel(label), BorderLayout.WEST);
				this.add(new JSpinner(new SpinnerNumberModel(defaultValue, minValue, maxValue, step)), BorderLayout.EAST);
			}
		}
		
		private class GalleryContainer extends OptionsContainer {

			public GalleryContainer(String title) {
				super(title);
				this.setLayout(new GridLayout(1, 2, 3, 3));
			}
			
			public void addButton(JToggleButton btn, String iconName) {
				Icon icon = new ImageIcon(this.getClass().getClassLoader().getResource("resources/" +  iconName));
				btn.setIcon(icon);
				btn.setPreferredSize(new Dimension(60, 60));
				this.add(btn);
			}
		}
	}

	private class PreviewPanel extends JPanel {
		public PreviewPanel() {

		}
	}
}
