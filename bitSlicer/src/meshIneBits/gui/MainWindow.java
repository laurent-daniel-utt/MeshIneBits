package meshIneBits.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.ColorUIResource;

import meshIneBits.Bit3D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Slicer.Config.CraftConfig;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

public class MainWindow extends JFrame {
	
	private static MainWindow instance = null;
	private GeneratedPart part;
	
	private PreviewFrame pf;

	public static MainWindow getInstance() {
		if (instance == null) {
			instance = new MainWindow();
		}
		return instance;
	}

	private MainWindow() {
		this.setIconImage(new ImageIcon(this.getClass().getClassLoader().getResource("resources/icon.png")).getImage());

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
			UIManager.put("Separator.foreground", new Color(10, 10, 10, 50));
			UIManager.put("Button.focus", new ColorUIResource(new Color(0, 0, 0, 0)));
			UIManager.put("Slider.focus", new ColorUIResource(new Color(0, 0, 0, 0)));
			UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
		} catch (Exception e) {
			e.printStackTrace();
		}

		setTitle("MeshIneBits");
		setSize(1280, 700);
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		pf = new PreviewFrame(null);
		Ribbon ribbon = new Ribbon();

		Container content = getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		content.add(ribbon);
		content.add(pf);

		setVisible(true);
	}

	public void closePart() {
		this.part = null;
		pf.revalidate();
		pf.repaint();
		pf.bg.setVisible(true);
	}

	public void setPart(GeneratedPart part) {
		this.part = part;
		this.remove(pf);
		pf = new PreviewFrame(part);
		this.add(pf);
		pf.init();
		pf.revalidate();
		pf.repaint();
	}
}
